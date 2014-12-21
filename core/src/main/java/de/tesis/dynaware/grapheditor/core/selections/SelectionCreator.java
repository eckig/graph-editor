/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
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

/**
 * Responsible for creating selections of nodes and joints in the graph editor.
 *
 * <p>
 * Nodes can currently be selected by clicking on them. Additionally, one or more joints and/or nodes can be selected by
 * dragging a box around them.
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

    private final SelectionBoxParameters selection = new SelectionBoxParameters();

    private Point2D selectionBoxStart;
    private Point2D selectionBoxEnd;

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
     * Selects all 'selectable' elements (i.e. nodes and joints) in the editor.
     */
    public void selectAll() {
        selectAll(true);
    }

    /**
     * Deselects all nodes and joints.
     */
    public void deselectAll() {
        selectAll(false);
    }

    /**
     * Adds a mechanism to select nodes by clicking on them.
     *
     * <p>
     * Holding the <b>control</b> key while clicking will add to the existing selection.
     * </p>
     */
    private void addClickSelectionMechanism() {

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
            if (!event.isControlDown()) {
                deselectAll();
            } else {
                backupSelections();
            }
            nodeSkin.setSelected(true);
        } else {
            if (event.isControlDown()) {
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

        if (!event.isControlDown()) {
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
            if (!event.isControlDown()) {
                deselectAll();
            } else {
                backupSelections();
                jointSkin.setSelected(true);
            }
            jointSkin.getRoot().toFront();
        } else {
            if (event.isControlDown()) {
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

        if (!event.isControlDown()) {
            deselectAll();
        } else {
            backupSelections();
        }

        selectionBoxStart = new Point2D(event.getX(), event.getY());
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

            if (!event.isControlDown()) {
                deselectAll();
            } else {
                backupSelections();
            }
            selectionBoxStart = new Point2D(event.getX(), event.getY());
        }

        selectionBoxEnd = new Point2D(event.getX(), event.getY());
        evaluateSelectionBoxParameters();

        view.drawSelectionBox(selection.x, selection.y, selection.width, selection.height);
        updateSelection(event.isControlDown());
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
    private void updateSelection(final boolean isControlDown) {

        final List<GNode> selectedNodes = getAllNodesInBox();
        final List<GJoint> selectedJoints = getAllJointsInBox();

        if (isControlDown) {
            selectedNodes.addAll(selectedNodesBackup);
            selectedJoints.addAll(selectedJointsBackup);
        }

        final List<GNode> deselectedNodes = new ArrayList<>(model.getNodes());
        final List<GJoint> deselectedJoints = new ArrayList<>(allJoints);

        deselectedNodes.removeAll(selectedNodes);
        deselectedJoints.removeAll(selectedJoints);

        for (final GNode node : selectedNodes) {
            skinLookup.lookupNode(node).setSelected(true);
        }

        for (final GNode node : deselectedNodes) {
            skinLookup.lookupNode(node).setSelected(false);
        }

        for (final GJoint joint : selectedJoints) {
            skinLookup.lookupJoint(joint).setSelected(true);
        }

        for (final GJoint joint : deselectedJoints) {
            skinLookup.lookupJoint(joint).setSelected(false);
        }
    }

    /**
     * Gets all nodes inside the current selection box.
     */
    private List<GNode> getAllNodesInBox() {

        final List<GNode> nodesToSelect = new ArrayList<>();

        for (final GNode node : model.getNodes()) {

            if (isInSelection(node.getX(), node.getY(), node.getWidth(), node.getHeight())) {
                nodesToSelect.add(node);
            }
        }

        return nodesToSelect;
    }

    /**
     * Selects all nodes and joints inside the current selection box.
     */
    private List<GJoint> getAllJointsInBox() {

        final List<GJoint> jointsToSelect = new ArrayList<>();

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                if (isInSelection(joint.getX(), joint.getY(), 0, 0)) {
                    jointsToSelect.add(joint);
                }
            }
        }

        return jointsToSelect;
    }

    /**
     * Checks if an object is fully inside the selection box.
     *
     * @param x the x position of the object
     * @param y the y position of the object
     * @param width the width of the object
     * @param height the height of the object
     *
     * @return {@code true} if the object is inside the selection box, {@code false} if not
     */
    private boolean isInSelection(final double x, final double y, final double width, final double height) {

        final boolean xInRange = selection.x < x && selection.x + selection.width > x + width;
        final boolean yInRange = selection.y < y && selection.y + selection.height > y + height;

        if (xInRange && yInRange) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the selected value of all nodes and joints.
     *
     * @param selected {@code true} to select all nodes and joints, {@code false} to deselect them
     */
    private void selectAll(final boolean selected) {

        for (final GNode node : model.getNodes()) {
            skinLookup.lookupNode(node).setSelected(selected);
        }

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {
                skinLookup.lookupJoint(joint).setSelected(selected);
            }
        }
    }

    /**
     * Stores the currently selected nodes and joints in this classes backup lists.
     *
     * <p>
     * This is used to add to an existing selection when holding the Ctrl key.
     * <p>
     */
    private void backupSelections() {

        selectedNodesBackup.clear();
        selectedJointsBackup.clear();

        for (final GNode node : model.getNodes()) {
            if (skinLookup.lookupNode(node).isSelected()) {
                selectedNodesBackup.add(node);
            }
        }

        for (final GConnection connection : model.getConnections()) {

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

        selection.x = Math.min(selectionBoxStart.getX(), selectionBoxEnd.getX());
        selection.y = Math.min(selectionBoxStart.getY(), selectionBoxEnd.getY());

        selection.width = Math.abs(selectionBoxStart.getX() - selectionBoxEnd.getX());
        selection.height = Math.abs(selectionBoxStart.getY() - selectionBoxEnd.getY());
    }

    /**
     * A struct to store the size & position parameters of the selection box.
     */
    private class SelectionBoxParameters {
        public double x;
        public double y;
        public double width;
        public double height;
    }
}
