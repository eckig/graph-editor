/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import javafx.geometry.Point2D;
import javafx.scene.layout.Region;

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
public class Commands
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;

    private static final EAttribute NODE_X = GraphPackage.Literals.GNODE__X;
    private static final EAttribute NODE_Y = GraphPackage.Literals.GNODE__Y;
    private static final EAttribute NODE_WIDTH = GraphPackage.Literals.GNODE__WIDTH;
    private static final EAttribute NODE_HEIGHT = GraphPackage.Literals.GNODE__HEIGHT;
    private static final EReference NODE_CONNECTORS = GraphPackage.Literals.GNODE__CONNECTORS;

    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

    private static final EAttribute JOINT_X = GraphPackage.Literals.GJOINT__X;
    private static final EAttribute JOINT_Y = GraphPackage.Literals.GJOINT__Y;

    /**
     * Static class, not to be instantiated.
     */
    private Commands()
    {
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
    public static void addNode(final GModel model, final GNode node)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null)
        {
            final Command command = AddCommand.create(editingDomain, model, NODES, node);

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Removes a node from the model.
     *
     * <p>
     * Also removes any connections that were attached to the node.
     * </p>
     *
     * @param model the {@link GModel} from which the node should be removed
     * @param node the {@link GNode} to remove from the model
     */
    public static void removeNode(final GModel model, final GNode node)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null)
        {
            final CompoundCommand command = new CompoundCommand();
            command.append(RemoveCommand.create(editingDomain, model, NODES, node));

            final List<GConnection> connectionsToDelete = new ArrayList<>();

            for (final GConnector connector : node.getConnectors())
            {
                for (final GConnection connection : connector.getConnections())
                {
                    if (connection != null && !connectionsToDelete.contains(connection))
                    {
                        connectionsToDelete.add(connection);
                    }
                }
            }

            for (final GConnection connection : connectionsToDelete)
            {
                command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, connection));

                final GConnector source = connection.getSource();
                final GConnector target = connection.getTarget();

                if (!node.equals(source.getParent()))
                {
                    command.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
                }

                if (!node.equals(target.getParent()))
                {
                    command.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));
                }
            }

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Clears everything in the given model.
     *
     * @param model the {@link GModel} to be cleared
     */
    public static void clear(final GModel model)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null)
        {
            final CompoundCommand command = new CompoundCommand();

            command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, model.getConnections()));
            command.append(RemoveCommand.create(editingDomain, model, NODES, model.getNodes()));

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
            }
        }
    }

    /**
     * Removes all connectors from the given nodes, and all connections attached to them.
     *
     * @param model the {@link GModel} being edited
     * @param nodes a list of {@link GNode} instances whose connectors should be removed
     */
    public static void clearConnectors(final GModel model, final List<GNode> nodes)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null)
        {
            final CompoundCommand command = new CompoundCommand();

            final Set<GConnection> connectionsToRemove = new HashSet<>();
            final Set<GConnector> connectorsToRemove = new HashSet<>();

            for (final GNode node : nodes)
            {
                command.append(RemoveCommand.create(editingDomain, node, NODE_CONNECTORS, node.getConnectors()));
                connectorsToRemove.addAll(node.getConnectors());

                for (final GConnector connector : node.getConnectors())
                {
                    connectionsToRemove.addAll(connector.getConnections());
                }
            }

            for (final GConnection connection : connectionsToRemove)
            {
                final GConnector source = connection.getSource();
                final GConnector target = connection.getTarget();

                if (!connectorsToRemove.contains(source))
                {
                    command.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
                }

                if (!connectorsToRemove.contains(target))
                {
                    command.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));
                }
            }

            command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, connectionsToRemove));

            if (command.canExecute())
            {
                editingDomain.getCommandStack().execute(command);
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
    public static void updateLayoutValues(final CompoundCommand command, final GModel model,
            final SkinLookup skinLookup)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null)
        {
            for (final GNode node : model.getNodes())
            {
                final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
                if (nodeSkin != null && checkNodeChanged(node, nodeSkin))
                {
                    final Region nodeRegion = nodeSkin.getRoot();
                    command.append(SetCommand.create(editingDomain, node, NODE_X, nodeRegion.getLayoutX()));
                    command.append(SetCommand.create(editingDomain, node, NODE_Y, nodeRegion.getLayoutY()));
                    command.append(SetCommand.create(editingDomain, node, NODE_WIDTH, nodeRegion.getWidth()));
                    command.append(SetCommand.create(editingDomain, node, NODE_HEIGHT, nodeRegion.getHeight()));
                }
            }

            for (final GConnection connection : model.getConnections())
            {
                updateConnector(connection.getSource(), command, editingDomain, skinLookup);
                updateConnector(connection.getTarget(), command, editingDomain, skinLookup);

                for (final GJoint joint : connection.getJoints())
                {
                    final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
                    if (jointSkin != null && checkJointChanged(joint, jointSkin))
                    {
                        final Region jointRegion = jointSkin.getRoot();
                        final double x = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
                        final double y = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

                        command.append(SetCommand.create(editingDomain, joint, JOINT_X, x));
                        command.append(SetCommand.create(editingDomain, joint, JOINT_Y, y));
                    }
                }
            }
        }
    }

    private static void updateConnector(final GConnector connector, final CompoundCommand command,
            final EditingDomain editingDomain, final SkinLookup skinLookup)
    {
        final GNode node = connector.getParent();
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null && connectorSkin != null)
        {
            final Point2D connectorPosition = nodeSkin.getConnectorPosition(connectorSkin);
            if (checkConnectorChanged(connector, connectorPosition))
            {
                command.append(SetCommand.create(editingDomain, connector, GraphPackage.Literals.GCONNECTOR__X,
                        connectorPosition.getX()));
                command.append(SetCommand.create(editingDomain, connector, GraphPackage.Literals.GCONNECTOR__Y,
                        connectorPosition.getY()));
            }
        }
    }

    /**
     * Checks if a connector's JavaFX region has different layout values than those
     * currently stored in the model.
     *
     * @param connector
     *            the model instance for the connector
     *
     * @return {@code true} if any layout value has changed, {@code false if not}
     */
    private static boolean checkConnectorChanged(final GConnector connector, final Point2D connectorPosition)
    {
        if (connectorPosition.getX() != connector.getX())
        {
            return true;
        }
        else if (connectorPosition.getY() != connector.getY())
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if a node's JavaFX region has different layout values than those
     * currently stored in the model.
     *
     * @param node
     *            the model instance for the node
     *
     * @return {@code true} if any layout value has changed,
     *         {@code false if not}
     */
    private static boolean checkNodeChanged(final GNode node, final GNodeSkin nodeSkin)
    {
        final Region nodeRegion = nodeSkin.getRoot();

        if (nodeRegion.getLayoutX() != node.getX())
        {
            return true;
        }
        else if (nodeRegion.getLayoutY() != node.getY())
        {
            return true;
        }
        else if (nodeRegion.getWidth() != node.getWidth())
        {
            return true;
        }
        else if (nodeRegion.getHeight() != node.getHeight())
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if a joint's JavaFX region has different layout values than those
     * currently stored in the model.
     *
     * @param joint
     *            the model instance for the joint
     *
     * @return {@code true} if any layout value has changed,
     *         {@code false if not}
     */
    private static boolean checkJointChanged(final GJoint joint, final GJointSkin jointSkin)
    {
        final Region jointRegion = jointSkin.getRoot();

        final double jointRegionX = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
        final double jointRegionY = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

        if (jointRegionX != joint.getX())
        {
            return true;
        }
        else if (jointRegionY != joint.getY())
        {
            return true;
        }
        return false;
    }

    /**
     * Attempts to undo the given model to its previous state.
     *
     * @param model the {@link GModel} to undo
     */
    public static void undo(final GModel model)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null && editingDomain.getCommandStack().canUndo())
        {
            editingDomain.getCommandStack().undo();
        }
    }

    /**
     * Attempts to redo the given model to its next state.
     *
     * @param model the {@link GModel} to redo
     */
    public static void redo(final GModel model)
    {
        final EditingDomain editingDomain = getEditingDomain(model);

        if (editingDomain != null && editingDomain.getCommandStack().canRedo())
        {
            editingDomain.getCommandStack().redo();
        }
    }

    /**
     * Gets the editing domain associated to the model.
     *
     * <p>
     * Logs an error if none is found.
     * </p>
     *
     * @param model a {@link GModel} instance
     * @return the {@link EditingDomain} associated to this model instance
     */
    private static EditingDomain getEditingDomain(final GModel model)
    {
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        if (editingDomain == null)
        {
            LOGGER.error("No editing domain found for this model. Maybe it hasn't been set inside a graph editor?"); //$NON-NLS-1$
        }

        return editingDomain;
    }
}
