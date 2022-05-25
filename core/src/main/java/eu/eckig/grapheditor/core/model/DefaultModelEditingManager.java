/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package eu.eckig.grapheditor.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import eu.eckig.grapheditor.Commands;
import eu.eckig.grapheditor.SkinLookup;
import eu.eckig.grapheditor.utils.RemoveContext;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor.Registry;

import eu.eckig.grapheditor.core.DefaultGraphEditor;
import eu.eckig.grapheditor.core.ModelEditingManager;
import eu.eckig.grapheditor.model.GConnection;
import eu.eckig.grapheditor.model.GConnector;
import eu.eckig.grapheditor.model.GModel;
import eu.eckig.grapheditor.model.GNode;
import eu.eckig.grapheditor.model.GraphPackage;


/**
 * Default {@link ModelEditingManager} implementation
 */
public class DefaultModelEditingManager implements ModelEditingManager
{

    private static final URI DEFAULT_URI = URI.createFileURI("");

    private final CommandStackListener commandStackListener;

    private EditingDomain editingDomain;
    private GModel model;

    private BiFunction<RemoveContext, GConnection, Command> mOnConnectionRemoved;
    private BiFunction<RemoveContext, GNode, Command> mOnNodeRemoved;

    /**
     * Creates a new model editing manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     *
     * @param pCommandStackListener
     *            the {@link CommandStackListener} that listens for changes in
     *            the model
     */
    public DefaultModelEditingManager(final CommandStackListener pCommandStackListener)
    {
        commandStackListener = pCommandStackListener;
    }

    @Override
    public void initialize(final GModel pModel)
    {
        // Only initialize the editing domain if the model object has actually changed.
        if (!pModel.equals(model))
        {
            initializeEditingDomain(model, pModel);
        }
        model = pModel;
    }

    @Override
    public void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved)
    {
        mOnConnectionRemoved = pOnConnectionRemoved;
    }

    @Override
    public void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved)
    {
        mOnNodeRemoved = pOnNodeRemoved;
    }

    @Override
    public void updateLayoutValues(final SkinLookup skinLookup)
    {
        final CompoundCommand command = new CompoundCommand();

        Commands.updateLayoutValues(command, model, skinLookup);

        editingDomain.getCommandStack().removeCommandStackListener(commandStackListener);

        if (command.canExecute())
        {
            editingDomain.getCommandStack().execute(command);
        }

        editingDomain.getCommandStack().addCommandStackListener(commandStackListener);
    }

    @Override
    public void remove(final Collection<EObject> pToRemove)
    {
        if (pToRemove == null || pToRemove.isEmpty())
        {
            return;
        }

        final CompoundCommand command = new CompoundCommand();
        final RemoveContext editContext = new RemoveContext();
        final List<EObject> delete = new ArrayList<>(pToRemove.size());

        // pre-fill the RemoveContext with all elements to be removed:
        for (final EObject obj : pToRemove)
        {
            if (obj instanceof GNode n && editContext.canRemove(obj))
            {
                delete.add(obj);
                for (final GConnector connector : n.getConnectors())
                {
                    for (final GConnection connection : connector.getConnections())
                    {
                        if (connection != null && editContext.canRemove(connection))
                        {
                            delete.add(connection);
                        }
                    }
                }
            }
            else if (obj instanceof GConnection && editContext.canRemove(obj))
            {
                delete.add(obj);
            }
        }

        // delete the elements and call business logic add-ins:
        for (final EObject obj : delete)
        {
            if (obj instanceof GNode)
            {
                command.append(RemoveCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__NODES, obj));

                final Command onRemoved = mOnNodeRemoved == null ? null : mOnNodeRemoved.apply(editContext, (GNode) obj);
                if (onRemoved != null)
                {
                    command.append(onRemoved);
                }
            }
            else if (obj instanceof GConnection)
            {
                remove(editContext, command, (GConnection) obj);
            }
        }

        if (!command.isEmpty() && command.canExecute())
        {
            editingDomain.getCommandStack().execute(command);
        }
    }

    private void remove(final RemoveContext pRemoveContext, final CompoundCommand pCommand, final GConnection pToDelete)
    {
        final GConnector source = pToDelete.getSource();
        final GConnector target = pToDelete.getTarget();

        pCommand.append(RemoveCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__CONNECTIONS, pToDelete));
        pCommand.append(RemoveCommand.create(editingDomain, source, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, pToDelete));
        pCommand.append(RemoveCommand.create(editingDomain, target, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, pToDelete));

        final Command onRemoved = mOnConnectionRemoved == null ? null : mOnConnectionRemoved.apply(pRemoveContext, pToDelete);
        if (onRemoved != null)
        {
            pCommand.append(onRemoved);
        }
    }

    /**
     * Initializes the editing domain and resource for the new model.
     *
     * <p>
     * If a resource and/or editing domain are already associated to this model,
     * these will be used. Otherwise they will be created.
     * </p>
     */
    private void initializeEditingDomain(final GModel oldModel, final GModel newModel)
    {
        // First remove the listener from the old model, if it exists.
        if (oldModel != null)
        {
            final EditingDomain oldDomain = AdapterFactoryEditingDomain.getEditingDomainFor(oldModel);
            if (oldDomain != null)
            {
                oldDomain.getCommandStack().removeCommandStackListener(commandStackListener);
            }
        }

        if (newModel.eResource() == null)
        {
            final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
            final Resource resource = resourceFactory.createResource(DEFAULT_URI);
            resource.getContents().add(newModel);
        }

        editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(newModel);

        if (editingDomain == null)
        {
            final Registry registry = ComposedAdapterFactory.Descriptor.Registry.INSTANCE;
            final AdapterFactory adapterFactory = new ComposedAdapterFactory(registry);

            editingDomain = new AdapterFactoryEditingDomain(adapterFactory, new BasicCommandStack());
            editingDomain.getResourceSet().getResources().add(newModel.eResource());
        }

        editingDomain.getCommandStack().addCommandStackListener(commandStackListener);
    }
}
