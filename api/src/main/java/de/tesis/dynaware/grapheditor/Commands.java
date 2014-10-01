/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.List;

import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import de.tesis.dynaware.grapheditor.utils.LogMessages;

/**
 * Provides utility methods for editing a {@link GModel} via EMF commands.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>GModel model = GraphFactory.eINSTANCE.createGModel();
 * GNode node = GraphFactory.eINSTANCE.createGNode();
 *
 * node.setX(100);
 * node.setY(50);
 * node.setWidth(150);
 * node.setHeight(200);
 *
 * Commands.addNode(model, node);
 * Commands.undo(model);
 * Commands.redo(model);</code>
 * </pre>
 */
public class Commands {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;

    private static final EAttribute NODE_X = GraphPackage.Literals.GNODE__X;
    private static final EAttribute NODE_Y = GraphPackage.Literals.GNODE__Y;
    private static final EAttribute NODE_WIDTH = GraphPackage.Literals.GNODE__WIDTH;
    private static final EAttribute NODE_HEIGHT = GraphPackage.Literals.GNODE__HEIGHT;

    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

    private static final EAttribute CONNECTION_TYPE = GraphPackage.Literals.GCONNECTION__TYPE;
    private static final EReference SOURCE = GraphPackage.Literals.GCONNECTION__SOURCE;
    private static final EReference TARGET = GraphPackage.Literals.GCONNECTION__TARGET;
    private static final EReference JOINTS = GraphPackage.Literals.GCONNECTION__JOINTS;

    private static final EAttribute JOINT_X = GraphPackage.Literals.GJOINT__X;
    private static final EAttribute JOINT_Y = GraphPackage.Literals.GJOINT__Y;

    /**
     * Static class, not to be instantiated.
     */
    private Commands() {
    }

    /**
     * Adds a node to the model.
     *
     * <p>
     * The node's x, y, width, and height values should be set before calling this method.
     * </p>
     *
     * @param model the {@link GModel} to which the node should be added
     * @param node the {@link GNode} to add to the model
     */
    public static void addNode(final GModel model, final GNode node) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null) {
            final Command command = AddCommand.create(editingDomain, model, NODES, node);

            if (command.canExecute()) {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Removes a node from the model.
     *
     * @param model the {@link GModel} from which the node should be removed
     * @param node the {@link GNode} to remove from the model
     */
    public static void removeNode(final GModel model, final GNode node) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null) {
            final Command command = RemoveCommand.create(editingDomain, model, NODES, node);

            if (command.canExecute()) {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Adds a connection to the model.
     *
     * @param model the {@link GModel} to which the connection should be added
     * @param source the source {@link GConnector} of the new connection
     * @param target the target {@link GConnector} of the new connection
     * @param type the type attribute for the new connection
     * @param joints the list of {@link GJoint} instances to be added inside the new connection
     */
    public static void addConnection(final GModel model, final GConnector source, final GConnector target,
            final String type, final List<GJoint> joints) {

        final EditingDomain editingDomain = getEditingDomain(model);

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
        }
    }

    /**
     * Removes a connection from the model.
     *
     * @param model the {@link GModel} from which the connection should be removed
     * @param connection the {@link GConnection} to be removed
     */
    public static void removeConnection(final GModel model, final GConnection connection) {

        final EditingDomain editingDomain = getEditingDomain(model);

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
        }
    }

    /**
     * Clears everything in the given model.
     *
     * @param model the {@link GModel} to be cleared
     */
    public static void clear(final GModel model) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null) {
            final CompoundCommand compoundCommand = new CompoundCommand();

            compoundCommand.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, model.getConnections()));
            compoundCommand.append(RemoveCommand.create(editingDomain, model, NODES, model.getNodes()));

            if (compoundCommand.canExecute()) {
                editingDomain.getCommandStack().execute(compoundCommand);
            }
        }
    }

    /**
     * Updates the model's layout values to match those in the skin instances.
     *
     * <p>
     * This method adds set operations to the given compound command but does <b>not</b> execute it.
     * </p>
     *
     * @param command a {@link CompoundCommand} to which the set commands will be added
     * @param model the {@link GModel} whose layout values should be updated
     * @param skinLookup the {@link SkinLookup} in use for this graph editor instance
     */
    public static void updateLayoutValues(final CompoundCommand command, final GModel model, final SkinLookup skinLookup) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null) {

            for (final GNode node : model.getNodes()) {

                final Region nodeRegion = skinLookup.lookupNode(node).getRoot();

                command.append(SetCommand.create(editingDomain, node, NODE_X, nodeRegion.getLayoutX()));
                command.append(SetCommand.create(editingDomain, node, NODE_Y, nodeRegion.getLayoutY()));
                command.append(SetCommand.create(editingDomain, node, NODE_WIDTH, nodeRegion.getWidth()));
                command.append(SetCommand.create(editingDomain, node, NODE_HEIGHT, nodeRegion.getHeight()));
            }

            for (final GConnection connection : model.getConnections()) {

                for (final GJoint joint : connection.getJoints()) {

                    final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
                    final Region jointRegion = jointSkin.getRoot();

                    final double x = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
                    final double y = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

                    command.append(SetCommand.create(editingDomain, joint, JOINT_X, x));
                    command.append(SetCommand.create(editingDomain, joint, JOINT_Y, y));
                }
            }
        }
    }

    /**
     * Attempts to undo the given model to its previous state.
     *
     * @param model the {@link GModel} to undo
     */
    public static void undo(final GModel model) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null && editingDomain.getCommandStack().canUndo()) {
            editingDomain.getCommandStack().undo();
        }
    }

    /**
     * Attempts to redo the given model to its next state.
     *
     * @param model the {@link GModel} to redo
     */
    public static void redo(final GModel model) {

        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null && editingDomain.getCommandStack().canRedo()) {
            editingDomain.getCommandStack().redo();
        }
    }

    private static EditingDomain getEditingDomain(final GModel model) {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain == null) {
            LOGGER.error(LogMessages.NO_EDITING_DOMAIN);
        }

        return editingDomain;
    }
}
