/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor.Registry;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import de.tesis.dynaware.grapheditor.utils.RemoveContext;


/**
 * Provides utility methods to edit the graph model via EMF commands.
 */
public class ModelEditingManager
{

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;
    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

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
     * @param commandStackListener
     *            the {@link CommandStackListener} that listens for changes in
     *            the model
     */
    public ModelEditingManager(final CommandStackListener commandStackListener)
    {
        this.commandStackListener = commandStackListener;
    }

    /**
     * Initializes the model editing manager for the given model instance.
     *
     * @param model
     *            the new {@link GModel} to be edited
     */
    public void initialize(final GModel model)
    {

        // Only initialize the editing domain if the model object has actually
        // changed.
        if (!model.equals(this.model))
        {
            initializeEditingDomain(this.model, model);
        }
        this.model = model;
    }

    /**
     * Sets a method to be called when a connection is removed in the editor.
     *
     * <p>
     * This can be used to create additional commands to the one that removed
     * the connection.
     * </p>
     *
     * @param pOnConnectionRemoved
     *            a {@link BiFunction} creating the additional command
     */
    public void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved)
    {
        mOnConnectionRemoved = pOnConnectionRemoved;
    }

    /**
     * Sets a method to be called when a node is removed in the editor.
     *
     * <p>
     * This can be used to create additional commands to the one that removed
     * the node.
     * </p>
     *
     * @param pOnNodeRemoved
     *            a {@link Function} creating the additional command
     */
    public void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved)
    {
        mOnNodeRemoved = pOnNodeRemoved;
    }

    /**
     * Silently updates the model's layout values to match those in the skin
     * instances.
     *
     * @param skinLookup
     *            the {@link SkinLookup} used to lookup skin instances
     */
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

    public void remove(final Collection<EObject> pToRemove)
    {
        if (pToRemove == null || pToRemove.isEmpty())
        {
            return;
        }

        final CompoundCommand command = new CompoundCommand();
        final RemoveContext editContext = new RemoveContext();

        for (final EObject obj : pToRemove)
        {
            if (obj instanceof GNode)
            {
                if (editContext.canRemove(obj))
                {
                    command.append(RemoveCommand.create(editingDomain, model, NODES, obj));

                    final Command onRemoved = mOnNodeRemoved == null ? null : mOnNodeRemoved.apply(editContext, (GNode) obj);
                    if (onRemoved != null)
                    {
                        command.append(onRemoved);
                    }
                }

                for (final GConnector connector : ((GNode) obj).getConnectors())
                {
                    for (final GConnection connection : connector.getConnections())
                    {
                        if (connection != null && editContext.canRemove(connection))
                        {
                            remove(pToRemove, editContext, command, connection);
                        }
                    }
                }
            }
            else if (obj instanceof GConnection)
            {
                if (editContext.canRemove(obj))
                {
                    remove(pToRemove, editContext, command, (GConnection) obj);
                }
            }
        }

        if (!command.isEmpty() && command.canExecute())
        {
            editingDomain.getCommandStack().execute(command);
        }
    }

    private void remove(final Collection<EObject> pToRemove, final RemoveContext pRemoveContext, final CompoundCommand pCommand,
            final GConnection pToDelete)
    {
        final GConnector source = pToDelete.getSource();
        final GConnector target = pToDelete.getTarget();

        pCommand.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, pToDelete));

        if (!pToRemove.contains(source.getParent()) && !pRemoveContext.contains(source.getParent()))
        {
            pCommand.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, pToDelete));
        }

        if (!pToRemove.contains(target.getParent()) && !pRemoveContext.contains(target.getParent()))
        {
            pCommand.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, pToDelete));
        }

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
