/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.EventUtils;
import de.tesis.dynaware.grapheditor.core.utils.GModelUtils;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

/**
 * Responsible for creating selections of nodes, connections, and joints in the graph editor.
 *
 * <p>
 * Nodes can currently be selected by clicking on them. Additionally, one or more nodes, connections, and joints can be
 * selected by dragging a box around them.
 * </p>
 */
public class SelectionCreator {

    private final SkinLookup skinLookup;
    private final GraphEditorView view;
    private final SelectionDragManager selectionDragManager;

    private GModel model;

    private final Map<Node, EventHandler<MouseEvent>> mousePressedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseReleasedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseClickedHandlers = new HashMap<>();

    private final EventHandler<MouseEvent> viewPressedHandler = this::handleViewPressed;
    private final EventHandler<MouseEvent> viewDraggedHandler = this::handleViewDragged;
    private final EventHandler<MouseEvent> viewReleasedHandler = this::handleViewReleased;

    private List<GJoint> allJoints;

    private final List<GNode> selectedNodesBackup = new ArrayList<>();
    private final List<GJoint> selectedJointsBackup = new ArrayList<>();
    private final List<GConnection> selectedConnectionsBackup = new ArrayList<>();

    private final Callback<MouseEvent, Boolean> selectionActive;
    
    private Rectangle2D selection;

    private Point2D selectionBoxStart;
    private Point2D selectionBoxEnd;

    private BiPredicate<GConnectionSkin, Rectangle2D> connectionPredicate;

    /**
     * Creates a new selection creator instance. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up skins
     * @param view the {@link GraphEditorView} instance
     * @param selectionDragManager the {@link SelectionDragManager} instance for this graph editor
     */
    public SelectionCreator(final SkinLookup skinLookup, final GraphEditorView view,
            final SelectionDragManager selectionDragManager, final Callback<MouseEvent, Boolean> selectionActive) {

        this.skinLookup = skinLookup;
        this.view = view;
        this.selectionDragManager = selectionDragManager;
        this.selectionActive = selectionActive;
        
        view.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(viewPressedHandler));
        view.addEventHandler(MouseEvent.MOUSE_DRAGGED, new WeakEventHandler<>(viewDraggedHandler));
        view.addEventHandler(MouseEvent.MOUSE_RELEASED, new WeakEventHandler<>(viewReleasedHandler));
    }

    /**
     * Initializes the selection creator for the current model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {
        
        this.model = model;
        allJoints = GModelUtils.getAllJoints(model);
        
        addClickSelectionMechanism();
    }

    /**
     * Sets a predicate to be called when the selection-box changes to see if connections should be selected.
     *
     * <p>
     * Can be null (in which case no connections will be selected by the selection-box).
     * </p>
     *
     * @param connectionPredicate a predicate that checks if a connection is inside the selection-box
     */
    public void setConnectionSelectionPredicate(final BiPredicate<GConnectionSkin, Rectangle2D> connectionPredicate) {
        this.connectionPredicate = connectionPredicate;
    }

    /**
     * Sets the selected value of all nodes.
     *
     * @param selected {@code true} to select all nodes, {@code false} to deselect them
     */
    public void selectAllNodes(final boolean selected) {
        if (model != null) {
            model.getNodes().stream().map(skinLookup::lookupNode).forEach(node -> node.setSelected(selected));
        }
    }

    /**
     * Sets the selected value of all joints.
     *
     * @param selected {@code true} to select all joints, {@code false} to deselect them
     */
    public void selectAllJoints(final boolean selected) {
        if (model != null) {
            model.getConnections().stream()
                    .flatMap(connection -> connection.getJoints().stream())
                    .map(skinLookup::lookupJoint)
                    .forEach(joint -> joint.setSelected(selected));
        }
    }
    
    /**
     * Sets the selected value of all connectors.
     *
     * @param selected {@code true} to select all connectors, {@code false} to deselect them
     */
    public void selectAllConnectors(final boolean selected) {
        if (model != null) {
            model.getNodes().stream()
                    .flatMap(node -> node.getConnectors().stream())
                    .map(skinLookup::lookupConnector)
                    .forEach(connector -> connector.setSelected(selected));
        }
    }

    /**
     * Sets the selected value of all connections.
     *
     * @param selected {@code true} to select all connections, {@code false} to deselect them
     */
    public void selectAllConnections(final boolean selected) {
        if (model != null) {
            model.getConnections().stream()
                    .map(skinLookup::lookupConnection)
                    .forEach(connection -> connection.setSelected(selected));
        }
    }

    /**
     * Deselects all selectable elements.
     */
    public void deselectAll() {
        selectAllNodes(false);
        selectAllJoints(false);
        selectAllConnections(false);
        selectAllConnectors(false);
    }

    /**
     * Adds a mechanism to select nodes by clicking on them.
     *
     * <p>
     * Holding the <b>shortcut</b> key while clicking will add to the existing selection.
     * </p>
     */
    private void addClickSelectionMechanism() {
        
        // remove all listeners:
        EventUtils.removeEventHandlers(mousePressedHandlers, MouseEvent.MOUSE_PRESSED);
        EventUtils.removeEventHandlers(mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
        EventUtils.removeEventHandlers(mouseClickedHandlers, MouseEvent.MOUSE_CLICKED);
        
        addClickSelectionForNodes();
        addClickSelectionForJoints();
    }

    private void handleSelectionClick(final MouseEvent event, final GSkin skin) {
        
        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        if (!skin.isSelected()) {
            if (!event.isShortcutDown()) {
                deselectAll();
            } else {
                backupSelections();
            }
            skin.setSelected(true);
        } else {
            if (event.isShortcutDown()) {
                skin.setSelected(false);
            }
        }

        // Consume this event so it's not passed up to the parent (i.e. the view).
        event.consume();
    }
    
    public void addNode(final GNode node) {
        
        final GNodeSkin skin = skinLookup.lookupNode(node);
        if (skin != null) {
            final Region nodeRegion = skin.getRoot();

            if(!mousePressedHandlers.containsKey(nodeRegion)) {
                final EventHandler<MouseEvent> newNodePressedHandler = event -> handleNodePressed(event, node);
                nodeRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, newNodePressedHandler);
                mousePressedHandlers.put(nodeRegion, newNodePressedHandler);
            }
            
            if(!mouseReleasedHandlers.containsKey(nodeRegion)) {
                final EventHandler<MouseEvent> newNodeReleasedHandler = event -> handleNodeReleased(event, node);
                nodeRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, newNodeReleasedHandler);
                mouseReleasedHandlers.put(nodeRegion, newNodeReleasedHandler);
            }

            for (final GConnector connector : node.getConnectors()) {
                addConnector(connector);
            }
        }
    }
    
    public void removeNode(final GNode node) {

        final GNodeSkin skin = skinLookup.lookupNode(node);
        if (skin != null) {
            final Region nodeRegion = skin.getRoot();

            final EventHandler<MouseEvent> newNodePressedHandler = mousePressedHandlers.remove(nodeRegion);
            final EventHandler<MouseEvent> newNodeReleasedHandler = mouseReleasedHandlers.remove(nodeRegion);

            if (newNodePressedHandler != null) {
                nodeRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, newNodePressedHandler);
            }
            if (newNodeReleasedHandler != null) {
                nodeRegion.removeEventHandler(MouseEvent.MOUSE_RELEASED, newNodeReleasedHandler);
            }

            for (final GConnector connector : node.getConnectors()) {
                removeConnector(connector);
            }
        }
    }
    
    public void addConnector(final GConnector connector) {
        
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        if (connectorSkin != null) {
            final Node connectorRoot = connectorSkin.getRoot();
            
            if (!mouseClickedHandlers.containsKey(connectorRoot)) {
                final EventHandler<MouseEvent> connectorClickedHandler = event -> handleSelectionClick(event, connectorSkin);
                connectorRoot.addEventHandler(MouseEvent.MOUSE_CLICKED, connectorClickedHandler);
                mouseClickedHandlers.put(connectorRoot, connectorClickedHandler);
            }
        }
    }
    
    public void removeConnector(final GConnector connector) {

        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        if (connectorSkin != null) {
            final Node connectorRoot = connectorSkin.getRoot();
            final EventHandler<MouseEvent> connectorClickedHandler = mouseClickedHandlers.remove(connectorRoot);
            if (connectorClickedHandler != null) {
                connectorRoot.removeEventHandler(MouseEvent.MOUSE_CLICKED, connectorClickedHandler);
            }
        }
    }
    
    public void addConnection(final GConnection connection) {

        final GConnectionSkin connSkin = skinLookup.lookupConnection(connection);
        if (connSkin != null) {

            final Node skinRoot = connSkin.getRoot();
            if (!mousePressedHandlers.containsKey(skinRoot)) {
                final EventHandler<MouseEvent> connectionPressedHandler = event -> handleConnectionPressed(event, connection);
                skinRoot.addEventHandler(MouseEvent.MOUSE_PRESSED, connectionPressedHandler);
                mousePressedHandlers.put(skinRoot, connectionPressedHandler);
            }
        }

        for (final GJoint joint : connection.getJoints()) {
            addJoint(joint);
        }
    }
    
    public void removeConnection(final GConnection connection) {

        final GConnectionSkin connSkin = skinLookup.lookupConnection(connection);
        if (connSkin != null) {

            final EventHandler<MouseEvent> connectionPressedHandler = mousePressedHandlers.remove(connSkin.getRoot());
            if (connectionPressedHandler != null) {
                connSkin.getRoot().removeEventHandler(MouseEvent.MOUSE_PRESSED, connectionPressedHandler);
            }
        }

        for (final GJoint joint : connection.getJoints()) {
            removeJoint(joint);
        }
    }
    
    public void addJoint(final GJoint joint) {

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {
            
            final Region jointRegion = jointSkin.getRoot();

            if(!mousePressedHandlers.containsKey(jointRegion)) {
                
                final EventHandler<MouseEvent> jointPressedHandler = event -> handleJointPressed(event, joint);
                jointRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, jointPressedHandler);
                mousePressedHandlers.put(jointRegion, jointPressedHandler);
            }
            if(!mouseReleasedHandlers.containsKey(jointRegion)) {
                
                final EventHandler<MouseEvent> jointReleasedHandler = event -> handleJointReleased(event, joint);
                jointRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, jointReleasedHandler);
                mouseReleasedHandlers.put(jointRegion, jointReleasedHandler);
            }
        }
        
        if(allJoints == null) {
            allJoints = GModelUtils.getAllJoints(model);
        }
        else {
            allJoints.add(joint);
        }
    }
    
    public void removeJoint(final GJoint joint) {

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {

            final Region jointRegion = jointSkin.getRoot();

            final EventHandler<MouseEvent> jointPressedHandler = mousePressedHandlers.remove(jointRegion);
            final EventHandler<MouseEvent> jointReleasedHandler = mouseReleasedHandlers.remove(jointRegion);

            if (jointPressedHandler != null) {
                jointRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, jointPressedHandler);
            }
            if (jointReleasedHandler != null) {
                jointRegion.removeEventHandler(MouseEvent.MOUSE_RELEASED, jointReleasedHandler);
            }
        }
        
        if(allJoints == null) {
            allJoints = GModelUtils.getAllJoints(model);
        }
        else {
            allJoints.remove(joint);
        }
    }
    
    /**
     * Adds a click selection mechanism for nodes.
     */
    private void addClickSelectionForNodes() {
        for (final GNode node : model.getNodes()) {
            addNode(node);
        }
    }

    /**
     * Adds a click selection mechanism for joints.
     */
    private void addClickSelectionForJoints() {
        for (final GConnection connection : model.getConnections()) {
            addConnection(connection);
        }
    }

    /**
     * Handles mouse-pressed events on the given node.
     *
     * @param event a mouse-pressed event
     * @param node the {@link GNode} on which this event occurred
     */
    private void handleNodePressed(final MouseEvent event, final GNode node) {

        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

        // Do not bind the positions of other selected nodes if this node is about to be resized.
        if (!nodeSkin.getRoot().isMouseInPositionForResize()) {
            selectionDragManager.bindPositions(node, model);
        }

        handleSelectionClick(event, nodeSkin);
        
        // Consume this event so it's not passed up to the parent (i.e. the view).
        event.consume();
    }

    /**
     * Handles mouse-released events on the given node.
     *
     * @param event a mouse-released event
     * @param node the {@link GNode} on which this event occurred
     */
    private void handleNodeReleased(final MouseEvent event, final GNode node) {

        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        selectionDragManager.unbindPositions(node);
        event.consume();
    }
    
    /**
     * Handles mouse-pressed events on the given connection.
     *
     * @param event a mouse-pressed event
     * @param connection the {@link GConnection} on which this event occurred
     */
    private void handleConnectionPressed(final MouseEvent event, final GConnection connection) {

        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        final GConnectionSkin connSkin = skinLookup.lookupConnection(connection);
        handleSelectionClick(event, connSkin);

        event.consume();
    }

    /**
     * Handles mouse-pressed events on the given joint.
     *
     * @param event a mouse-pressed event
     * @param joint the {@link GJoint} on which this event occured
     */
    private void handleJointPressed(final MouseEvent event, final GJoint joint) {

        if (!MouseButton.PRIMARY.equals(event.getButton()) || event.isConsumed()) {
            return;
        }

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        handleSelectionClick(event, jointSkin);

        selectionDragManager.bindPositions(joint, model);
        event.consume();
    }

    /**
     * Handles mouse-released events on the given joint.
     *
     * @param event a mouse-released event
     * @param joint the {@link GJoint} on which this event occured
     */
    private void handleJointReleased(final MouseEvent event, final GJoint joint) {

        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        selectionDragManager.unbindPositions(joint);
        event.consume();
    }
    
    /**
     * Handles mouse-pressed events on the view.
     *
     * @param event a mouse-pressed event
     */
    private void handleViewPressed(final MouseEvent event) {

        if (model == null || !selectionActive.call(event) || event.isConsumed()) {
            return;
        }

        if (!event.isShortcutDown()) {
            deselectAll();
        } else {
            backupSelections();
        }

        final double scale = view.getLocalToSceneTransform().getMxx();
        final Point2D cursorPosition = GeometryUtils.getCursorPosition(event, view);

        selectionBoxStart = new Point2D(cursorPosition.getX() / scale, cursorPosition.getY() / scale);
    }

    /**
     * Handles mouse-dragged events on the view.
     *
     * @param event a mouse-dragged event
     */
    private void handleViewDragged(final MouseEvent event) {

        if (model == null || !selectionActive.call(event) || event.isConsumed() || selectionBoxStart == null) {
            return;
        }

        final double scale = view.getLocalToSceneTransform().getMxx();
        final Point2D cursorPosition = GeometryUtils.getCursorPosition(event, view);

        selectionBoxEnd = new Point2D(cursorPosition.getX() / scale, cursorPosition.getY() / scale);

        evaluateSelectionBoxParameters();

        view.drawSelectionBox(selection.getMinX(), selection.getMinY(), selection.getWidth(), selection.getHeight());
        updateSelection(event.isShortcutDown());
    }

    /**
     * Handles mouse-released events on the view.
     *
     * @param event a mouse-released event
     */
    private void handleViewReleased(final MouseEvent event) {
        selectionBoxStart = null;
        view.hideSelectionBox();
    }
    
    private boolean isNodeSelected(final GNode node, final boolean isShortcutDown) {
        return selection.contains(node.getX(), node.getY(), node.getWidth(), node.getHeight())
                || isShortcutDown && selectedNodesBackup.contains(node);
    }
    
    private boolean isJointSelected(final GJoint joint, final boolean isShortcutDown) {
        return selection.contains(joint.getX(), joint.getY()) || isShortcutDown && selectedJointsBackup.contains(joint);
    }
    
    private boolean isConnectionSelected(final GConnection connection, final boolean isShortcutDown) {
        return connectionPredicate != null
                && connectionPredicate.test(skinLookup.lookupConnection(connection), selection)
                || isShortcutDown && selectedConnectionsBackup.contains(connection);
    }

    /**
     * Updates the selection according to what nodes & joints are inside / outside the selection box.
     */
    private void updateSelection(final boolean isShortcutDown) {

        for (int i = 0; i < model.getNodes().size(); i++) {
            final GNode node = model.getNodes().get(i);
            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
            if (nodeSkin != null) {
                nodeSkin.setSelected(isNodeSelected(node, isShortcutDown));
            }
        }
        
        for (int i = 0; i < allJoints.size(); i++) {
            final GJoint joint = allJoints.get(i);
            final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
            if (jointSkin != null) {
                jointSkin.setSelected(isJointSelected(joint, isShortcutDown));
            }
        }
        
        for (int i = 0; i < model.getConnections().size(); i++) {
            final GConnection connection = model.getConnections().get(i);
            final GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
            if (connectionSkin != null) {
                connectionSkin.setSelected(isConnectionSelected(connection, isShortcutDown));
            }
        }
    }

    /**
     * Stores the currently selected objects in this class' backup lists.
     *
     * <p>
     * This is used to add to an existing selection when holding the shortcut key (e.g. Ctrl in Windows).
     * <p>
     */
    private void backupSelections() {

        selectedNodesBackup.clear();
        selectedJointsBackup.clear();
        selectedConnectionsBackup.clear();

        for (final GNode node : model.getNodes()) {
            if (skinLookup.lookupNode(node).isSelected()) {
                selectedNodesBackup.add(node);
            }
        }

        for (final GConnection connection : model.getConnections()) {

            if (skinLookup.lookupConnection(connection).isSelected()) {
                selectedConnectionsBackup.add(connection);
            }

            for (final GJoint joint : connection.getJoints()) {
                if (skinLookup.lookupJoint(joint).isSelected()) {
                    selectedJointsBackup.add(joint);
                }
            }
        }
    }

    /**
     * Updates the current selection box values based on the cursor start- and endpoints.
     */
    private void evaluateSelectionBoxParameters() {

        final double x = Math.min(selectionBoxStart.getX(), selectionBoxEnd.getX());
        final double y = Math.min(selectionBoxStart.getY(), selectionBoxEnd.getY());

        final double width = Math.abs(selectionBoxStart.getX() - selectionBoxEnd.getX());
        final double height = Math.abs(selectionBoxStart.getY() - selectionBoxEnd.getY());

        selection = new Rectangle2D(x, y, width, height);
    }
}
