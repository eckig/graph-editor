/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.GModelUtils;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

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

    // Keep track of all added handlers, because adding a handler twice is punishable by death.
    private final Map<GNode, EventHandler<MouseEvent>> nodePressedHandlers = new HashMap<>();
    private final Map<GNode, EventHandler<MouseEvent>> nodeReleasedHandlers = new HashMap<>();

    private final Map<GConnector, EventHandler<MouseEvent>> connectorPressedHandlers = new HashMap<>();

    private final Map<GJoint, EventHandler<MouseEvent>> jointPressedHandlers = new HashMap<>();
    private final Map<GJoint, EventHandler<MouseEvent>> jointReleasedHandlers = new HashMap<>();

    private EventHandler<MouseEvent> viewPressedHandler;
    private EventHandler<MouseEvent> viewDraggedHandler;
    private EventHandler<MouseEvent> viewReleasedHandler;

    private List<GJoint> allJoints;

    private final List<GNode> selectedNodesBackup = new ArrayList<>();
    private final List<GJoint> selectedJointsBackup = new ArrayList<>();
    private final List<GConnection> selectedConnectionsBackup = new ArrayList<>();

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
            final SelectionDragManager selectionDragManager) {

        this.skinLookup = skinLookup;
        this.view = view;
        this.selectionDragManager = selectionDragManager;
    }

    /**
     * Initializes the selection creator for the current model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        this.model = model;

        addClickSelectionMechanism();
        addDragSelectionMechanism();

        allJoints = GModelUtils.getAllJoints(model);
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
            model.getNodes().forEach(node -> skinLookup.lookupNode(node).setSelected(selected));
        }
    }

    /**
     * Sets the selected value of all joints.
     *
     * @param selected {@code true} to select all joints, {@code false} to deselect them
     */
    public void selectAllJoints(final boolean selected) {
        if (model != null) {
            model.getConnections().forEach(connection -> connection.getJoints().forEach(joint -> {
                skinLookup.lookupJoint(joint).setSelected(selected);
            }));
        }
    }

    /**
     * Sets the selected value of all connections.
     *
     * @param selected {@code true} to select all connections, {@code false} to deselect them
     */
    public void selectAllConnections(final boolean selected) {
        if (model != null) {
            model.getConnections().forEach(connection -> {
                skinLookup.lookupConnection(connection).setSelected(selected);
            });
        }
    }

    /**
     * Deselects all selectable elements.
     */
    public void deselectAll() {
        selectAllNodes(false);
        selectAllJoints(false);
        selectAllConnections(false);
    }

    /**
     * Adds a mechanism to select nodes by clicking on them.
     *
     * <p>
     * Holding the <b>shortcut</b> key while clicking will add to the existing selection.
     * </p>
     */
    private void addClickSelectionMechanism() {
        addClickSelectionForNodes();
        addClickSelectionForJoints();
    }

    /**
     * Adds a click selection mechanism for nodes.
     */
    private void addClickSelectionForNodes() {

        for (final GNode node : model.getNodes()) {

            final Region nodeRegion = skinLookup.lookupNode(node).getRoot();

            final EventHandler<MouseEvent> oldNodePressedHandler = nodePressedHandlers.get(node);
            final EventHandler<MouseEvent> oldNodeReleasedHandler = nodeReleasedHandlers.get(node);

            if (oldNodePressedHandler != null) {
                nodeRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, oldNodePressedHandler);
            }

            if (oldNodeReleasedHandler != null) {
                nodeRegion.removeEventHandler(MouseEvent.MOUSE_RELEASED, oldNodeReleasedHandler);
            }

            final EventHandler<MouseEvent> newNodePressedHandler = event -> handleNodePressed(event, node);
            final EventHandler<MouseEvent> newNodeReleasedHandler = event -> handleNodeReleased(event, node);

            nodeRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, newNodePressedHandler);
            nodeRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, newNodeReleasedHandler);

            nodePressedHandlers.put(node, newNodePressedHandler);
            nodeReleasedHandlers.put(node, newNodeReleasedHandler);

            for (final GConnector connector : node.getConnectors()) {

                final Node connectorRoot = skinLookup.lookupConnector(connector).getRoot();

                final EventHandler<MouseEvent> oldConnectorPressedHandler = connectorPressedHandlers.get(connector);

                if (oldConnectorPressedHandler != null) {
                    connectorRoot.removeEventHandler(MouseEvent.MOUSE_PRESSED, oldConnectorPressedHandler);
                }

                final EventHandler<MouseEvent> newConnectorPressedHandler = event -> handleConnectorPressed(event);

                connectorRoot.addEventHandler(MouseEvent.MOUSE_PRESSED, newConnectorPressedHandler);
                connectorPressedHandlers.put(connector, newConnectorPressedHandler);
            }
        }
    }

    /**
     * Adds a click selection mechanism for joints.
     */
    private void addClickSelectionForJoints() {

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                final Region jointRegion = skinLookup.lookupJoint(joint).getRoot();

                final EventHandler<MouseEvent> oldJointPressedHandler = jointPressedHandlers.get(joint);
                final EventHandler<MouseEvent> oldJointReleasedHandler = jointReleasedHandlers.get(joint);

                if (oldJointPressedHandler != null) {
                    jointRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, oldJointPressedHandler);
                }

                if (oldJointReleasedHandler != null) {
                    jointRegion.removeEventHandler(MouseEvent.MOUSE_RELEASED, oldJointReleasedHandler);
                }

                final EventHandler<MouseEvent> newJointPressedHandler = event -> handleJointPressed(event, joint);
                final EventHandler<MouseEvent> newJointReleasedHandler = event -> handleJointReleased(event, joint);

                jointRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, newJointPressedHandler);
                jointRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, newJointReleasedHandler);

                jointPressedHandlers.put(joint, newJointPressedHandler);
                jointReleasedHandlers.put(joint, newJointReleasedHandler);
            }
        }
    }

    /**
     * Handles mouse-pressed events on the given node.
     *
     * @param event a mouse-pressed event
     * @param node the {@link GNode} on which this event occurred
     */
    private void handleNodePressed(final MouseEvent event, final GNode node) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

        if (!nodeSkin.isSelected()) {
            if (!event.isShortcutDown()) {
                deselectAll();
            } else {
                backupSelections();
            }
            nodeSkin.setSelected(true);
        } else {
            if (event.isShortcutDown()) {
                nodeSkin.setSelected(false);
            }
        }

        // Do not bind the positions of other selected nodes if this node is about to be resized.
        if (!nodeSkin.getRoot().isMouseInPositionForResize()) {
            selectionDragManager.bindPositions(node, model);
        }

        // Consume this event so it's not passed up to the parent (i.e. the view).
        event.consume();
    }

    /**
     * Handles mouse-released events on the given node.
     *
     * @param event a mouse-released event
     * @param node the {@link GNode} on which this event occured
     */
    private void handleNodeReleased(final MouseEvent event, final GNode node) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        selectionDragManager.unbindPositions(node);
        event.consume();
    }

    /**
     * Handles mouse-pressed events on any connector.
     *
     * @param event a mouse-pressed event
     */
    private void handleConnectorPressed(final MouseEvent event) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (!event.isShortcutDown()) {
            deselectAll();
        }

        event.consume();
    }

    /**
     * Handles mouse-pressed events on the given joint.
     *
     * @param event a mouse-pressed event
     * @param joint the {@link GJoint} on which this event occured
     */
    private void handleJointPressed(final MouseEvent event, final GJoint joint) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

        if (!jointSkin.isSelected()) {
            if (!event.isShortcutDown()) {
                deselectAll();
            } else {
                backupSelections();
                jointSkin.setSelected(true);
            }
            jointSkin.getRoot().toFront();
        } else {
            if (event.isShortcutDown()) {
                jointSkin.setSelected(false);
            }
        }

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

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        selectionDragManager.unbindPositions(joint);
        event.consume();
    }

    /**
     * Adds a mechanism to select one or more joints and / or nodes by dragging a box around them.
     */
    private void addDragSelectionMechanism() {

        if (viewPressedHandler != null) {
            view.removeEventHandler(MouseEvent.MOUSE_PRESSED, viewPressedHandler);
        }

        if (viewDraggedHandler != null) {
            view.removeEventHandler(MouseEvent.MOUSE_PRESSED, viewDraggedHandler);
        }

        if (viewReleasedHandler != null) {
            view.removeEventHandler(MouseEvent.MOUSE_PRESSED, viewReleasedHandler);
        }

        viewPressedHandler = event -> handleViewPressed(event);
        viewDraggedHandler = event -> handleViewDragged(event);
        viewReleasedHandler = event -> handleViewReleased(event);

        view.addEventHandler(MouseEvent.MOUSE_PRESSED, viewPressedHandler);
        view.addEventHandler(MouseEvent.MOUSE_DRAGGED, viewDraggedHandler);
        view.addEventHandler(MouseEvent.MOUSE_RELEASED, viewReleasedHandler);
    }

    /**
     * Handles mouse-pressed events on the view.
     *
     * @param event a mouse-pressed event
     */
    private void handleViewPressed(final MouseEvent event) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
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

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (selectionBoxStart == null) {

            if (!event.isShortcutDown()) {
                deselectAll();
            } else {
                backupSelections();
            }
            final double scale = view.getLocalToSceneTransform().getMxx();
            final Point2D cursorPosition = GeometryUtils.getCursorPosition(event, view);

            selectionBoxStart = new Point2D(cursorPosition.getX() / scale, cursorPosition.getY() / scale);
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

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        selectionBoxStart = null;
        view.hideSelectionBox();
    }

    /**
     * Updates the selection according to what nodes & joints are inside / outside the selection box.
     */
    private void updateSelection(final boolean isShortcutDown) {

        final List<GNode> selectedNodes = getAllNodesInBox();
        final List<GJoint> selectedJoints = getAllJointsInBox();
        final List<GConnection> selectedConnections = getAllConnectionsInBox();

        if (isShortcutDown) {
            selectedNodes.addAll(selectedNodesBackup);
            selectedJoints.addAll(selectedJointsBackup);
            selectedConnections.addAll(selectedConnectionsBackup);
        }

        final List<GNode> deselectedNodes = new ArrayList<>(model.getNodes());
        final List<GJoint> deselectedJoints = new ArrayList<>(allJoints);
        final List<GConnection> deselectedConnections = new ArrayList<>(model.getConnections());

        deselectedNodes.removeAll(selectedNodes);
        deselectedJoints.removeAll(selectedJoints);
        deselectedConnections.removeAll(selectedConnections);

        selectedNodes.forEach(node -> skinLookup.lookupNode(node).setSelected(true));
        deselectedNodes.forEach(node -> skinLookup.lookupNode(node).setSelected(false));
        selectedJoints.forEach(joint -> skinLookup.lookupJoint(joint).setSelected(true));
        deselectedJoints.forEach(joint -> skinLookup.lookupJoint(joint).setSelected(false));
        selectedConnections.forEach(connection -> skinLookup.lookupConnection(connection).setSelected(true));
        deselectedConnections.forEach(connection -> skinLookup.lookupConnection(connection).setSelected(false));
    }

    /**
     * Gets all nodes inside the current selection box.
     */
    private List<GNode> getAllNodesInBox() {

        final List<GNode> nodesToSelect = new ArrayList<>();

        for (final GNode node : model.getNodes()) {

            if (selection.contains(node.getX(), node.getY(), node.getWidth(), node.getHeight())) {
                nodesToSelect.add(node);
            }
        }

        return nodesToSelect;
    }

    /**
     * Gets all joints inside the current selection box.
     */
    private List<GJoint> getAllJointsInBox() {

        final List<GJoint> jointsToSelect = new ArrayList<>();

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                if (selection.contains(joint.getX(), joint.getY())) {
                    jointsToSelect.add(joint);
                }
            }
        }

        return jointsToSelect;
    }

    /**
     * Gets all connections inside the current selection box.
     */
    private List<GConnection> getAllConnectionsInBox() {

        final List<GConnection> connectionsToSelect = new ArrayList<>();

        if (connectionPredicate != null) {
            for (final GConnection connection : model.getConnections()) {
                if (connectionPredicate.test(skinLookup.lookupConnection(connection), selection)) {
                    connectionsToSelect.add(connection);
                }
            }
        }

        return connectionsToSelect;
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
