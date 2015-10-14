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
import de.tesis.dynaware.grapheditor.GraphEditorSkins;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultJointSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.util.Callback;

/**
 * Manages skins for all elements of a {@link GModel}.
 *
 * <p>
 * Provides lookup methods, for example to find the {@link GNodeSkin} instance
 * associated to a {@link GNode} instance.
 * </p>
 */
public class SkinManager implements SkinLookup, GraphEditorSkins {

    private final GraphEditor graphEditor;

    private Callback<GNode, GNodeSkin> nodeSkinFactory;
    private Callback<GConnector, GConnectorSkin> connectorSkinFactory;
    private Callback<GConnection, GConnectionSkin> connectionSkinFactory;
    private Callback<GJoint, GJointSkin> jointSkinFactory;
    private Callback<GConnector, GTailSkin> tailSkinFactory;

    private final Map<GNode, GNodeSkin> nodeSkins = new HashMap<>();
    private final Map<GConnector, GConnectorSkin> connectorSkins = new HashMap<>();
    private final Map<GConnection, GConnectionSkin> connectionSkins = new HashMap<>();
    private final Map<GJoint, GJointSkin> jointSkins = new HashMap<>();
    private final Map<GConnector, GTailSkin> tailSkins = new HashMap<>();

    /**
     * Creates a new skin manager instance. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public SkinManager(final GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> skinFactory) {
        this.nodeSkinFactory = skinFactory;
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> connectorSkinFactory) {
        this.connectorSkinFactory = connectorSkinFactory;
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> connectionSkinFactory) {
        this.connectionSkinFactory = connectionSkinFactory;
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> jointSkinFactory) {
        this.jointSkinFactory = jointSkinFactory;
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> tailSkinFactory) {
        this.tailSkinFactory = tailSkinFactory;
    }

    /**
     * Adds a list of nodes.
     *
     * <p>
     * Skin instances will be created for these nodes and be available via the
     * lookup methods.
     * </p>
     *
     * @param nodesToAdd a list of {@link GNode} instances for which skin
     * instances should be created
     */
    public void addNodes(final List<GNode> nodesToAdd) {

        if (nodesToAdd != null && !nodesToAdd.isEmpty()) {
            
            // prevent ConcurrentModification
            final GNode[] updates = nodesToAdd.toArray(new GNode[nodesToAdd.size()]);
            for (final GNode node : updates) {

                final GNodeSkin nodeSkin = nodeSkins.computeIfAbsent(node, this::createSkin);

                nodeSkin.setGraphEditor(graphEditor);
                nodeSkin.getRoot().setEditorProperties(graphEditor.getProperties());
                nodeSkin.initialize();

                addConnectors(node);
            }
        }
    }

    private GNodeSkin createSkin(final GNode node) {
        final GNodeSkin skin = nodeSkinFactory == null ? null : nodeSkinFactory.call(node);
        return skin == null ? new DefaultNodeSkin(node) : skin;
    }

    /**
     * Removes a list of nodes.
     *
     * <p>
     * The skin instances for these nodes will be removed (if they exist).
     * </p>
     *
     * @param nodesToRemove a list of {@link GNode} instances for which skin
     * instances should be removed
     */
    public void removeNodes(final List<GNode> nodesToRemove) {

        if (nodesToRemove != null && !nodesToRemove.isEmpty()) {

            // prevent ConcurrentModification
            final GNode[] updates = nodesToRemove.toArray(new GNode[nodesToRemove.size()]);
            for (final GNode node : updates) {

                GNodeSkin removedSkin = nodeSkins.remove(node);

                if (removedSkin != null) {
                    removedSkin.dispose();
                }
                removeConnectors(node.getConnectors());
            }
        }
    }

    /**
     * Updates a list of nodes
     *
     * <p>
     * The connector skins for these nodes will be created and re-set.
     * </p>
     *
     * @param nodesToUpdate a list of {@link GNode} instances which should be
     * updated
     */
    public void updateNodes(final List<GNode> nodesToUpdate) {

        if (nodesToUpdate != null && !nodesToUpdate.isEmpty()) {

            // prevent ConcurrentModification
            final GNode[] updates = nodesToUpdate.toArray(new GNode[nodesToUpdate.size()]);
            for (final GNode node : updates) {
                removeConnectors(node.getConnectors());
                addConnectors(node);
            }
        }
    }

    /**
     * Removes a list of connectors.
     *
     * <p>
     * The skin instances for these connectors will be removed (if they exist).
     * </p>
     *
     * @param connectorsToRemove a list of {@link GConnector} instances for
     * which skin instances should be removed
     */
    public void removeConnectors(final List<GConnector> connectorsToRemove) {

        if (connectorsToRemove != null && !connectorsToRemove.isEmpty()) {

            // prevent ConcurrentModification
            final GConnector[] updates = connectorsToRemove.toArray(new GConnector[connectorsToRemove.size()]);
            for (final GConnector connector : updates) {

                final GConnectorSkin removedSkin = connectorSkins.remove(connector);

                if (removedSkin != null) {
                    removedSkin.dispose();
                }
                tailSkins.remove(connector);

            }
        }
    }

    /**
     * Adds a list of connections.
     *
     * <p>
     * Skin instances will be created for these connections and be available via
     * the lookup methods.
     * </p>
     *
     * @param connectionsToAdd a list of {@link GConnection} instances for which
     * skin instances should be created
     */
    public void addConnections(final List<GConnection> connectionsToAdd) {

        // prevent ConcurrentModification
        final GConnection[] updates = connectionsToAdd == null ? new GConnection[0] : connectionsToAdd.toArray(new GConnection[connectionsToAdd.size()]);
        for (final GConnection connection : updates) {

            final GConnectionSkin connectionSkin = connectionSkins.computeIfAbsent(connection, this::createSkin);
            connectionSkin.setGraphEditor(graphEditor);

            addJoints(connection);
        }
    }

    private GConnectionSkin createSkin(final GConnection connection) {
        final GConnectionSkin skin = connectionSkinFactory == null ? null : connectionSkinFactory.call(connection);
        return skin == null ? new DefaultConnectionSkin(connection) : skin;
    }

    /**
     * Removes a list of connections.
     *
     * <p>
     * The skin instances for these connections will be removed (if they exist).
     * </p>
     *
     * @param connectionsToRemove a list of {@link GConnection} instances for
     * which skin instances should be removed
     */
    public void removeConnections(final List<GConnection> connectionsToRemove) {

        if (connectionsToRemove != null && !connectionsToRemove.isEmpty()) {

            // prevent ConcurrentModification
            final GConnection[] updates = connectionsToRemove.toArray(new GConnection[connectionsToRemove.size()]);
            for (final GConnection connection : updates) {

                final GConnectionSkin removedSkin = connectionSkins.remove(connection);

                if (removedSkin != null) {
                    removedSkin.dispose();
                }
            }
        }
    }

    /**
     * Adds the given list of joints to a particular connection.
     *
     * <p>
     * Skin instances will be created for these joints and be available via the
     * lookup methods. Furthermore connection's list of joints will be updated.
     * </p>
     *
     * @param connection the {@link GConnection} to which joints should be added
     * @param jointsToAdd a list of {@link GJoint} instances for which skin
     * instances should be created and added
     */
    public void addJoints(final GConnection connection, final List<GJoint> jointsToAdd) {

        for (final GJoint joint : jointsToAdd) {

            final GJointSkin jointSkin = jointSkins.computeIfAbsent(joint, this::createSkin);
            jointSkin.setGraphEditor(graphEditor);
            jointSkin.getRoot().setEditorProperties(graphEditor.getProperties());
        }

        final List<GJointSkin> connectionJointSkins = new ArrayList<>();

        for (final GJoint joint : connection.getJoints()) {
            connectionJointSkins.add(lookupJoint(joint));
        }

        lookupConnection(connection).setJointSkins(connectionJointSkins);
    }

    private GJointSkin createSkin(final GJoint joint) {
        final GJointSkin skin = jointSkinFactory == null ? null : jointSkinFactory.call(joint);
        return skin == null ? new DefaultJointSkin(joint) : skin;
    }

    /**
     * Removes a list of joints.
     *
     * <p>
     * The skin instances for these joints will be removed (if they exist).
     * </p>
     *
     * @param jointsToRemove a list of {@link GJoint} instances for which skin
     * instances should be removed
     */
    public void removeJoints(final List<GJoint> jointsToRemove) {
        // prevent ConcurrentModification
        final GJoint[] updates = jointsToRemove == null ? new GJoint[0] : jointsToRemove.toArray(new GJoint[jointsToRemove.size()]);
        for (final GJoint joint : updates) {

            final GJointSkin removedSkin = jointSkins.remove(joint);

            if (removedSkin != null) {
                removedSkin.dispose();
            }
        }
    }

    /**
     * Initializes all node and joint skins, so that their layout values are
     * reloaded from their model instances.
     */
    public void initializeAll() {
        nodeSkins.values().forEach(GNodeSkin::initialize);
        jointSkins.values().forEach(GJointSkin::initialize);
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

            final GConnectorSkin connectorSkin = connectorSkins.computeIfAbsent(connector, this::createConnectorSkin);
            connectorSkin.setGraphEditor(graphEditor);
            nodeConnectorSkins.add(connectorSkin);

            final GTailSkin tailSkin = tailSkins.computeIfAbsent(connector, this::createTailSkin);
            tailSkin.setGraphEditor(graphEditor);
        }

        nodeSkins.get(node).setConnectorSkins(nodeConnectorSkins);
    }

    private GConnectorSkin createConnectorSkin(final GConnector connector) {
        final GConnectorSkin skin = connectorSkinFactory == null ? null : connectorSkinFactory.call(connector);
        return skin == null ? new DefaultConnectorSkin(connector) : skin;
    }

    private GTailSkin createTailSkin(final GConnector connector) {
        final GTailSkin skin = tailSkinFactory == null ? null : tailSkinFactory.call(connector);
        return skin == null ? new DefaultTailSkin(connector) : skin;
    }

    /**
     * Adds all joints belonging to the given connection.
     *
     * @param connection the {@link GConnection} instance whose joints should be
     * added
     */
    private void addJoints(final GConnection connection) {

        final List<GJointSkin> connectionJointSkins = new ArrayList<>();

        for (final GJoint joint : connection.getJoints()) {

            final GJointSkin jointSkin = jointSkins.computeIfAbsent(joint, this::createSkin);
            jointSkin.setGraphEditor(graphEditor);
            jointSkin.getRoot().setEditorProperties(graphEditor.getProperties());

            connectionJointSkins.add(jointSkin);
        }

        connectionSkins.get(connection).setJointSkins(connectionJointSkins);
    }
}
