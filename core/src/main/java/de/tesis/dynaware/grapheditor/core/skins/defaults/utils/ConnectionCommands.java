/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.utils;

import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.core.connections.ConnectionEventManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;


/**
 * Provides utility methods for adding and removing connections via EMF
 * commands.
 */
public class ConnectionCommands
{

    /**
     * Static class, not to be instantiated.
     */
    private ConnectionCommands()
    {
    }

    /**
     * Adds a connection to the model.
     *
     * @param model
     *            the {@link GModel} to which the connection should be added
     * @param source
     *            the source {@link GConnector} of the new connection
     * @param target
     *            the target {@link GConnector} of the new connection
     * @param type
     *            the type attribute for the new connection
     * @param joints
     *            the list of {@link GJoint} instances to be added inside the
     *            new connection
     */
    public static void addConnection(final GModel model, final GConnector source, final GConnector target, final String type,
            final List<GJoint> joints, final ConnectionEventManager connectionEventManager)
    {
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain != null)
        {
            final CompoundCommand command = new CompoundCommand();

            // prepare new connection:
            final GConnection connection = GraphFactory.eINSTANCE.createGConnection();
            connection.setType(type);
            connection.setSource(source);
            connection.setTarget(target);
            connection.getJoints().addAll(joints);

            // attributes that involve other members of the model, are modified through commands:
            command.append(AddCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__CONNECTIONS, connection));
            command.append(AddCommand.create(editingDomain, source, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
            command.append(AddCommand.create(editingDomain, target, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));

            final Command onCreate;
            if (connectionEventManager != null && (onCreate = connectionEventManager.notifyConnectionAdded(connection)) != null)
            {
                command.append(onCreate);
            }

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Removes a connection from the model.
     *
     * @param model
     *            the {@link GModel} from which the connection should be removed
     * @param connection
     *            the {@link GConnection} to be removed
     * @param connectionEventManager
     */
    public static void removeConnection(final GModel model, final GConnection connection, ConnectionEventManager connectionEventManager)
    {
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain != null)
        {
            final CompoundCommand command = new CompoundCommand();

            final GConnector source = connection.getSource();
            final GConnector target = connection.getTarget();

            command.append(RemoveCommand.create(editingDomain, model, GraphPackage.Literals.GMODEL__CONNECTIONS, connection));
            command.append(RemoveCommand.create(editingDomain, source, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));
            command.append(RemoveCommand.create(editingDomain, target, GraphPackage.Literals.GCONNECTOR__CONNECTIONS, connection));

            final Command onRemove;
            if (connectionEventManager != null && (onRemove = connectionEventManager.notifyConnectionRemoved(connection)) != null)
            {
                command.append(onRemove);
            }

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }

        }
    }
}
