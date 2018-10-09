/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.BitSet;
import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import javafx.geometry.Point2D;

/**
 * A set of helper methods to add and remove joints from the default connection skin using EMF commands.
 */
public class JointCommands {

    private static final EReference JOINTS = GraphPackage.Literals.GCONNECTION__JOINTS;

    /**
     * Static class.
     */
    private JointCommands() {
    }

    /**
     * Removes any existing joints from the connection and creates a new set of joints at the given positions.
     *
     * <p>
     * This is executed as a single compound command and is therefore a single element in the undo-redo stack.
     * </p>
     *
     * @param positions a list of {@link Point2D} instances speciying the x and y positions of the new joints
     * @param connection the connection in which the joints will be set
     */
    public static void setNewJoints(final List<Point2D> positions, final GConnection connection) {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(connection);
        final CompoundCommand command = new CompoundCommand();

        command.append(RemoveCommand.create(editingDomain, connection, JOINTS, connection.getJoints()));

        for (final Point2D position : positions) {

            final GJoint newJoint = GraphFactory.eINSTANCE.createGJoint();
            newJoint.setX(position.getX());
            newJoint.setY(position.getY());

            command.append(AddCommand.create(editingDomain, connection, JOINTS, newJoint));
        }

        if (command.canExecute()) {
            editingDomain.getCommandStack().execute(command);
        }
    }

    /**
     * Removes joints from a connection.
     *
     * <p>
     * This method adds the remove operations to the given compound command and does not execute it.
     * </p>
     *
     * @param command a {@link CompoundCommand} to which the remove commands will be added
     * @param indices the indices within the connection's list of joints specifying the joints to be removed
     * @param connection the connection whose joints are to be removed
     */
    public static void removeJoints(final CompoundCommand command, final BitSet indices,
            final GConnection connection)
    {
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(connection);

        for (int i = 0; i < connection.getJoints().size(); i++)
        {
            if (indices.get(i))
            {
                final GJoint joint = connection.getJoints().get(i);
                command.append(RemoveCommand.create(editingDomain, connection, JOINTS, joint));
            }
        }
    }
}
