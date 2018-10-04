/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private Function<GConnection, Command> mOnConnectionRemoved;
    private Function<GNode, Command> mOnNodeRemoved;

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
     *            a {@link Function} creating the additional command
     */
    public void setOnConnectionRemoved(final Function<GConnection, Command> pOnConnectionRemoved)
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
    public void setOnNodeRemoved(Function<GNode, Command> pOnNodeRemoved)
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

    public Optional<CompoundCommand> remove(final Collection<EObject> pToRemove)
    {
        if (pToRemove == null || pToRemove.isEmpty())
        {
            return Optional.empty();
        }

        final List<Command> additionalCommands = new ArrayList<>();
        final Set<GNode> nodesToDelete = new HashSet<>();
        final Set<GConnection> connectionsToDelete = new HashSet<>();

        for (final EObject obj : pToRemove)
        {
            if (obj instanceof GNode)
            {
                if (nodesToDelete.add((GNode) obj))
                {
                    final Command onRemoved = mOnNodeRemoved == null ? null : mOnNodeRemoved.apply((GNode) obj);
                    if (onRemoved != null)
                    {
                        additionalCommands.add(onRemoved);
                    }
                }

                for (final GConnector connector : ((GNode) obj).getConnectors())
                {
                    for (final GConnection connection : connector.getConnections())
                    {
                        if (connection != null && connectionsToDelete.add(connection))
                        {
                            final Command ononnRemoved = mOnConnectionRemoved == null ? null : mOnConnectionRemoved.apply(connection);
                            if (ononnRemoved != null)
                            {
                                additionalCommands.add(ononnRemoved);
                            }
                        }
                    }
                }
            }
            else if (obj instanceof GConnection)
            {
                if (connectionsToDelete.add((GConnection) obj))
                {
                    final Command onRemoved = mOnConnectionRemoved == null ? null : mOnConnectionRemoved.apply((GConnection) obj);
                    if (onRemoved != null)
                    {
                        additionalCommands.add(onRemoved);
                    }
                }
            }
        }

        if (!nodesToDelete.isEmpty() || !connectionsToDelete.isEmpty())
        {
            final CompoundCommand command = remove(nodesToDelete, connectionsToDelete);

            if (!additionalCommands.isEmpty())
            {
                additionalCommands.forEach(command::append);
            }

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }
        }
        return Optional.empty();
    }

    /**
     * Removes all specified nodes and connections from the model in a single
     * compound command.
     *
     * <p>
     * All references to the removed elements are also removed.
     * </p>
     *
     * @param nodesToRemove
     *            the nodes to be removed
     * @param connectionsToRemove
     *            the connections to be removed
     */
    private CompoundCommand remove(final Collection<GNode> nodesToRemove, final Collection<GConnection> connectionsToRemove)
    {
        final CompoundCommand command = new CompoundCommand();

        for (final GNode node : nodesToRemove)
        {
            command.append(RemoveCommand.create(editingDomain, model, NODES, node));
        }

        for (final GConnection connection : connectionsToRemove)
        {
            command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, connection));

            final GConnector source = connection.getSource();
            final GConnector target = connection.getTarget();

            if (!nodesToRemove.contains(source.getParent()))
            {
                command.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
            }

            if (!nodesToRemove.contains(target.getParent()))
            {
                command.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));
            }
        }
        return command;
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
