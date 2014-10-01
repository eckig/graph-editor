/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Manages skins for all elements of a {@link GModel}.
 *
 * <p>
 * Provides lookup methods, for example to find the {@link GNodeSkin} instance associated to a {@link GNode} instance.
 * </p>
 */
public class SkinManager implements SkinLookup {

    private final GraphEditor graphEditor;
    private final SkinFactory skinFactory;

    private final Map<GNode, GNodeSkin> nodeSkins = new HashMap<>();
    private final Map<GConnector, GConnectorSkin> connectorSkins = new HashMap<>();
    private final Map<GConnection, GConnectionSkin> connectionSkins = new HashMap<>();
    private final Map<GJoint, GJointSkin> jointSkins = new HashMap<>();
    private final Map<GConnector, GTailSkin> tailSkins = new HashMap<>();

    /**
     * Creates a new skin manager instance. Only one instance should exist per {@link DefaultGraphEditor} instance.
     */
    public SkinManager(final GraphEditor graphEditor) {
        this(graphEditor, new SkinFactory());
    }

    /**
     * Package-private constructor used only to inject mocks in unit tests.
     *
     * @param skinFactory a mock {@link SkinFactory} instance
     */
    SkinManager(final GraphEditor graphEditor, final SkinFactory skinFactory) {
        this.graphEditor = graphEditor;
        this.skinFactory = skinFactory;
    }

    /**
     * Sets the custom node skin class for the given type.
     *
     * @param type the {@link GNode} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GNodeSkin}
     */
    public void setNodeSkin(final String type, final Class<? extends GNodeSkin> skin) {
        skinFactory.setNodeSkin(type, skin);
    }

    /**
     * Sets the custom connector skin class for the given type.
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectorSkin}
     */
    public void setConnectorSkin(final String type, final Class<? extends GConnectorSkin> skin) {
        skinFactory.setConnectorSkin(type, skin);
    }

    /**
     * Sets the custom connection skin class for the given type.
     *
     * @param type the {@link GConnection} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectionSkin}
     */
    public void setConnectionSkin(final String type, final Class<? extends GConnectionSkin> skin) {
        skinFactory.setConnectionSkin(type, skin);
    }

    /**
     * Sets the custom joint skin class for the given type.
     *
     * @param type the {@link GJoint} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GJointSkin}
     */
    public void setJointSkin(final String type, final Class<? extends GJointSkin> skin) {
        skinFactory.setJointSkin(type, skin);
    }

    /**
     * Sets the custom tail skin class for the given type.
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GTailSkin}
     */
    public void setTailSkin(final String type, final Class<? extends GTailSkin> skin) {
        skinFactory.setTailSkin(type, skin);
    }

    /**
     * Adds a list of nodes.
     *
     * <p>
     * Skin instances will be created for these nodes and be available via the lookup methods.
     * </p>
     *
     * @param nodesToAdd a list of {@link GNode} instances for which skin instances should be created
     */
    public void addNodes(final List<GNode> nodesToAdd) {

        for (final GNode node : nodesToAdd) {

            final GNodeSkin nodeSkin = skinFactory.createNodeSkin(node);

            nodeSkin.setGraphEditor(graphEditor);
            nodeSkin.getRoot().setEditorProperties(graphEditor.getProperties());
            nodeSkin.initialize();

            nodeSkins.put(node, nodeSkin);

            addConnectors(node);
        }
    }

    /**
     * Removes a list of nodes.
     *
     * <p>
     * The skin instances for these nodes will be removed (if they exist).
     * </p>
     *
     * @param nodesToRemove a list of {@link GNode} instances for which skin instances should be removed
     */
    public void removeNodes(final List<GNode> nodesToRemove) {

        for (final GNode node : nodesToRemove) {
            nodeSkins.remove(node);
            removeConnectors(node.getConnectors());
        }
    }

    /**
     * Updates a list of nodes
     *
     * <p>
     * The connector skins for these nodes will be created and re-set.
     * </p>
     *
     * @param nodesToUpdate a list of {@link GNode} instances which should be updated
     */
    public void updateNodes(final List<GNode> nodesToUpdate) {

        for (final GNode node : nodesToUpdate) {
            removeConnectors(node.getConnectors());
            addConnectors(node);
        }
    }

    /**
     * Removes a list of connectors.
     *
     * <p>
     * The skin instances for these connectors will be removed (if they exist).
     * </p>
     *
     * @param connectorsToRemove a list of {@link GConnector} instances for which skin instances should be removed
     */
    public void removeConnectors(final List<GConnector> connectorsToRemove) {

        for (final GConnector connector : connectorsToRemove) {
            connectorSkins.remove(connector);
            tailSkins.remove(connector);
        }
    }

    /**
     * Adds a list of connections.
     *
     * <p>
     * Skin instances will be created for these connections and be available via the lookup methods.
     * </p>
     *
     * @param connectionsToAdd a list of {@link GConnection} instances for which skin instances should be created
     */
    public void addConnections(final List<GConnection> connectionsToAdd) {

        for (final GConnection connection : connectionsToAdd) {

            final GConnectionSkin connectionSkin = skinFactory.createConnectionSkin(connection);
            connectionSkin.setGraphEditor(graphEditor);

            connectionSkins.put(connection, connectionSkin);

            addJoints(connection);
        }
    }

    /**
     * Removes a list of connections.
     *
     * <p>
     * The skin instances for these connections will be removed (if they exist).
     * </p>
     *
     * @param connectionsToRemove a list of {@link GConnection} instances for which skin instances should be removed
     */
    public void removeConnections(final List<GConnection> connectionsToRemove) {

        for (final GConnection connection : connectionsToRemove) {
            connectionSkins.remove(connection);
        }
    }

    /**
     * Adds the given list of joints to a particular connection.
     *
     * <p>
     * Skin instances will be created for these joints and be available via the lookup methods. Furthermore connection's
     * list of joints will be updated.
     * </p>
     *
     * @param connection the {@link GConnection} to which joints should be added
     * @param jointsToAdd a list of {@link GJoint} instances for which skin instances should be created and added
     */
    public void addJoints(final GConnection connection, final List<GJoint> jointsToAdd) {

        for (final GJoint joint : jointsToAdd) {

            final GJointSkin jointSkin = skinFactory.createJointSkin(joint);
            jointSkin.setGraphEditor(graphEditor);
            jointSkin.getRoot().setEditorProperties(graphEditor.getProperties());

            jointSkins.put(joint, jointSkin);
        }

        final List<GJointSkin> connectionJointSkins = new ArrayList<>();

        for (final GJoint joint : connection.getJoints()) {
            connectionJointSkins.add(lookupJoint(joint));
        }

        lookupConnection(connection).setJointSkins(connectionJointSkins);
    }

    /**
     * Removes a list of joints.
     *
     * <p>
     * The skin instances for these joints will be removed (if they exist).
     * </p>
     *
     * @param jointsToRemove a list of {@link GJoint} instances for which skin instances should be removed
     */
    public void removeJoints(final List<GJoint> jointsToRemove) {
        for (final GJoint joint : jointsToRemove) {
            jointSkins.remove(joint);
        }
    }

    /**
     * Initializes all node and joint skins, so that their layout values are reloaded from their model instances.
     */
    public void initializeAll() {

        for (final GNodeSkin nodeSkin : nodeSkins.values()) {
            nodeSkin.initialize();
        }

        for (final GJointSkin jointSkin : jointSkins.values()) {
            jointSkin.initialize();
        }
    }

    @Override
    public GNodeSkin lookupNode(final GNode node) {
        return nodeSkins.get(node);
    }

    @Override
    public GConnectorSkin lookupConnector(final GConnector connector) {
        return connectorSkins.get(connector);
    }

    @Override
    public GConnectionSkin lookupConnection(final GConnection connection) {
        return connectionSkins.get(connection);
    }

    @Override
    public GJointSkin lookupJoint(final GJoint joint) {
        return jointSkins.get(joint);
    }

    @Override
    public GTailSkin lookupTail(final GConnector connector) {
        return tailSkins.get(connector);
    }

    /**
     * Adds a list of connector skins for the given node.
     *
     * <p>
     * The node skin's list of connector skins will be updated.
     * </p>
     *
     * @param node the {@link GNode} whose connectors should be added
     */
    private void addConnectors(final GNode node) {

        final List<GConnectorSkin> nodeConnectorSkins = new ArrayList<>();

        for (final GConnector connector : node.getConnectors()) {

            final GConnectorSkin connectorSkin = skinFactory.createConnectorSkin(connector);
            connectorSkin.setGraphEditor(graphEditor);

            connectorSkins.put(connector, connectorSkin);
            nodeConnectorSkins.add(connectorSkin);

            final GTailSkin tailSkin = skinFactory.createTailSkin(connector);
            tailSkin.setGraphEditor(graphEditor);

            tailSkins.put(connector, tailSkin);
        }

        nodeSkins.get(node).setConnectorSkins(nodeConnectorSkins);
    }

    /**
     * Adds all joints belonging to the given connection.
     *
     * @param connection the {@link GConnection} instance whose joints should be added
     */
    private void addJoints(final GConnection connection) {

        final List<GJointSkin> connectionJointSkins = new ArrayList<>();

        for (final GJoint joint : connection.getJoints()) {

            final GJointSkin jointSkin = skinFactory.createJointSkin(joint);
            jointSkin.setGraphEditor(graphEditor);
            jointSkin.getRoot().setEditorProperties(graphEditor.getProperties());

            jointSkins.put(joint, jointSkin);
            connectionJointSkins.add(jointSkin);
        }

        connectionSkins.get(connection).setJointSkins(connectionJointSkins);
    }
}
