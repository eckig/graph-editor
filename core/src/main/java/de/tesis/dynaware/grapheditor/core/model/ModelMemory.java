/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Responsible for remembering the old model state and calculating the difference between old and new states.
 *
 * <p>
 * When a new model state is set, we compare it to the old state and decide what nodes, connectors, connections and
 * joints need to be added or removed from the graph.
 * </p>
 *
 * <p>
 * <b> Note: </b><br>
 * Adding and removing JavaFX nodes from the scene graph is <em>slow</em>. When the model is reinitialized (e.g. after
 * an undo command is executed), we want to disturb the view as little as possible. That's why we want to calculate the
 * <em>difference</em> between the old and new model states, rather than clearing and re-adding everything.
 * </p>
 */
public class ModelMemory {

    private final List<GNode> oldNodes = new ArrayList<>();
    private final Map<GNode, List<GConnector>> oldConnectors = new HashMap<>();
    private final List<GConnection> oldConnections = new ArrayList<>();
    private final Map<GConnection, List<GJoint>> oldJoints = new HashMap<>();

    private final List<GNode> nodesToAdd = new ArrayList<>();
    private final List<GNode> nodesToRemove = new ArrayList<>();
    private final List<GNode> nodesToUpdate = new ArrayList<>();

    private final List<GConnector> connectorsToRemove = new ArrayList<>();

    private final List<GConnection> connectionsToAdd = new ArrayList<>();
    private final List<GConnection> connectionsToRemove = new ArrayList<>();

    private final Map<GConnection, List<GJoint>> jointsToAdd = new HashMap<>();
    private final Map<GConnection, List<GJoint>> jointsToRemove = new HashMap<>();

    /**
     * Sets the new model state.
     *
     * <p>
     * This should be called whenever a model is loaded / reinitialized. It compares the newly-set model to the old
     * model state (if one exists) and finds all nodes, connectors, connections, and joints that need to be added,
     * removed, and so on.
     * </p>
     *
     * @param model the new {@link GModel} state
     */
    public void setNewModelState(final GModel model) {

        clearAll();
        findNodes(model);
        findConnectors(model);
        findConnections(model);
        findJoints(model);
        rememberOldElements(model);
    }

    /**
     * Wipes the model memory.
     *
     * <p>
     * The next time a new model state is set after this method is called, the model memory will think that all elements
     * have to be added, because the old model state is gone.
     * </p>
     */
    public void wipe() {

        oldNodes.clear();
        oldConnections.clear();
        oldConnectors.clear();
        oldJoints.clear();

        clearAll();
    }

    /**
     * Gets the list of nodes that need to be added.
     *
     * <p>
     * In other words, nodes that are present in the new model state but were not present in the old model state.
     * </p>
     *
     * @return a list of {@link GNode} instances to be added
     */
    public List<GNode> getNodesToAdd() {
        return nodesToAdd;
    }

    /**
     * Gets the list of nodes that need to be removed.
     *
     * <p>
     * In other words, nodes that were present in the old model state but are not present in the new model state.
     * </p>
     *
     * @return a list of {@link GNode} instances to be removed
     */
    public List<GNode> getNodesToRemove() {
        return nodesToRemove;
    }

    /**
     * Gets the list of nodes that need to be updated.
     *
     * <p>
     * A node should be updated for example if it's list of connectors is different in the new model state than the old
     * model state.
     * </p>
     *
     * @return the list of {@link GNode} instances to be updated
     */
    public List<GNode> getNodesToUpdate() {
        return nodesToUpdate;
    }

    /**
     * Gets the list of connectors that need to be removed.
     *
     * <p>
     * Even if the connector's parent node is removed, it still appear in this list so it's skin can be removed from the
     * skin manager.
     * </p>
     *
     * @return a list of {@link GConnector} instances to be removed
     */
    public List<GConnector> getConnectorsToRemove() {
        return connectorsToRemove;
    }

    /**
     * Gets the list of connections that need to be added.
     *
     * <p>
     * In other words, connections that are present in the new model state but were not present in the old model state.
     * </p>
     *
     * @return a list of {@link GConnection} instances to be added
     */
    public List<GConnection> getConnectionsToAdd() {
        return connectionsToAdd;
    }

    /**
     * Gets the list of connections that need to be removed.
     *
     * <p>
     * In other words, connections that were present in the old model state but are not present in the new model state.
     * </p>
     *
     * @return a list of {@link GConnection} instances to be removed
     */
    public List<GConnection> getConnectionsToRemove() {
        return connectionsToRemove;
    }

    /**
     * Gets the map of joints to be added.
     *
     * <p>
     * The joints to be added are given for each connection, so the connection's list of joint skins can be updated
     * accordingly.
     * </p>
     *
     * @return the map of {@link GJoint} instances to be added
     */
    public Map<GConnection, List<GJoint>> getJointsToAdd() {
        return jointsToAdd;
    }

    /**
     * Gets the map of joints to be removed.
     *
     * @return the map of {@link GJoint} instances to be removed
     */
    public Map<GConnection, List<GJoint>> getJointsToRemove() {
        return jointsToRemove;
    }

    /**
     * Clears all lists and maps that contain information about elements to be added, removed, and updated.
     */
    private void clearAll() {

        nodesToAdd.clear();
        nodesToRemove.clear();
        nodesToUpdate.clear();
        connectorsToRemove.clear();
        connectionsToAdd.clear();
        connectionsToRemove.clear();
        jointsToAdd.clear();
        jointsToRemove.clear();
    }

    /**
     * Finds the nodes to be added and removed based on the old and new model states.
     *
     * @param model the new {@link GModel} state
     */
    private void findNodes(final GModel model) {

        nodesToAdd.addAll(model.getNodes());
        nodesToAdd.removeAll(oldNodes);

        nodesToRemove.addAll(oldNodes);
        nodesToRemove.removeAll(model.getNodes());
    }

    /**
     * Finds the connectors to be removed and the nodes to be updated based on the old and new model states.
     *
     * @param model the new {@link GModel} state
     */
    private void findConnectors(final GModel model) {

        for (final GNode node : oldNodes) {
            if (!nodesToRemove.contains(node)) {
                connectorsToRemove.addAll(oldConnectors.get(node));
            }
        }

        for (final GNode node : model.getNodes()) {

            connectorsToRemove.removeAll(node.getConnectors());

            final boolean addRemove = nodesToRemove.contains(node) || nodesToAdd.contains(node);
            final boolean connectorsChanged = !node.getConnectors().equals(oldConnectors.get(node));

            if (!addRemove && connectorsChanged) {
                nodesToUpdate.add(node);
            }
        }
    }

    /**
     * Finds the connections to be added and removed based on the old and new model states.
     *
     * @param model the new {@link GModel} state
     */
    private void findConnections(final GModel model) {

        connectionsToAdd.addAll(model.getConnections());
        connectionsToAdd.removeAll(oldConnections);

        connectionsToRemove.addAll(oldConnections);
        connectionsToRemove.removeAll(model.getConnections());
    }

    /**
     * Finds the joints to be added and removed based on the old and new model states.
     *
     * @param model the new {@link GModel} state
     */
    private void findJoints(final GModel model) {

        for (final GConnection connection : oldConnections) {
            jointsToRemove.put(connection, new ArrayList<>(oldJoints.get(connection)));
        }

        for (final GConnection connection : model.getConnections()) {
            if (jointsToRemove.containsKey(connection)) {
                jointsToRemove.get(connection).removeAll(connection.getJoints());
            }
        }

        for (final GConnection connection : model.getConnections()) {
            jointsToAdd.put(connection, new ArrayList<>(connection.getJoints()));
        }

        for (final GConnection connection : oldConnections) {
            if (jointsToAdd.containsKey(connection)) {
                jointsToAdd.get(connection).removeAll(oldJoints.get(connection));
            }
        }
    }

    /**
     * Stores the model state in a set of lists to be remembered for the next comparison.
     *
     * <p>
     * The references to all model elements (nodes, connections, etc) are stored in <b>new</b> lists. This is important
     * because the {@link GModel} instance will be changed afterwards, and we want to remember what elements were
     * present at the time of the call.
     * </p>
     *
     * @param a {@link GModel} instance
     */
    private void rememberOldElements(final GModel model) {

        oldNodes.clear();
        oldConnections.clear();
        oldConnectors.clear();
        oldJoints.clear();

        oldNodes.addAll(model.getNodes());
        for (final GNode node : model.getNodes()) {
            oldConnectors.put(node, new ArrayList<>());
            oldConnectors.get(node).addAll(node.getConnectors());
        }

        oldConnections.addAll(model.getConnections());
        for (final GConnection connection : model.getConnections()) {
            oldJoints.put(connection, new ArrayList<>());
            oldJoints.get(connection).addAll(connection.getJoints());
        }
    }
}
