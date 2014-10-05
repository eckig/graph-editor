/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.connections;

import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

/**
 * Provides utility methods for adding and removing connections via EMF commands.
 */
public class ConnectionCommands {

    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;

    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

    private static final EAttribute CONNECTION_TYPE = GraphPackage.Literals.GCONNECTION__TYPE;
    private static final EReference SOURCE = GraphPackage.Literals.GCONNECTION__SOURCE;
    private static final EReference TARGET = GraphPackage.Literals.GCONNECTION__TARGET;
    private static final EReference JOINTS = GraphPackage.Literals.GCONNECTION__JOINTS;

    /**
     * Static class, not to be instantiated.
     */
    private ConnectionCommands() {
    }

    /**
     * Adds a connection to the model.
     *
     * @param model the {@link GModel} to which the connection should be added
     * @param source the source {@link GConnector} of the new connection
     * @param target the target {@link GConnector} of the new connection
     * @param type the type attribute for the new connection
     * @param joints the list of {@link GJoint} instances to be added inside the new connection
     * @return the newly-executed {@link CompoundCommand} that added the connection
     */
    public static CompoundCommand addConnection(final GModel model, final GConnector source, final GConnector target,
            final String type, final List<GJoint> joints) {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain != null) {
            final CompoundCommand command = new CompoundCommand();

            final GConnection connection = GraphFactory.eINSTANCE.createGConnection();

            command.append(AddCommand.create(editingDomain, model, CONNECTIONS, connection));

            if (type != null) {
                command.append(SetCommand.create(editingDomain, connection, CONNECTION_TYPE, type));
            }

            command.append(SetCommand.create(editingDomain, connection, SOURCE, source));
            command.append(SetCommand.create(editingDomain, connection, TARGET, target));
            command.append(AddCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
            command.append(AddCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));

            for (final GJoint joint : joints) {
                command.append(AddCommand.create(editingDomain, connection, JOINTS, joint));
            }

            if (command.canExecute()) {
                editingDomain.getCommandStack().execute(command);
            }
            return command;

        } else {
            return null;
        }
    }

    /**
     * Removes a connection from the model.
     *
     * @param model the {@link GModel} from which the connection should be removed
     * @param connection the {@link GConnection} to be removed
     * @return the newly-executed {@link CompoundCommand} that removed the connection
     */
    public static CompoundCommand removeConnection(final GModel model, final GConnection connection) {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain != null) {
            final CompoundCommand command = new CompoundCommand();

            final GConnector source = connection.getSource();
            final GConnector target = connection.getTarget();

            command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, connection));
            command.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
            command.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));

            if (command.canExecute()) {
                editingDomain.getCommandStack().execute(command);
            }
            return command;

        } else {
            return null;
        }
    }
}
