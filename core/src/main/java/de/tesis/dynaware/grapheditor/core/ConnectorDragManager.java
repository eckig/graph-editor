/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.validators.ValidatorManager;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * Responsible for what happens when connectors are dragged in the graph editor.
 *
 * <p>
 * Namely, the creation, removal, and repositioning of connections.
 * </p>
 */
public class ConnectorDragManager {

    private final SkinLookup skinLookup;
    private final ValidatorManager validatorManager;
    private final GraphEditorView view;

    private GModel model;

    private final Map<GConnector, EventHandler<MouseEvent>> mouseEnteredHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseExitedHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseReleasedHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseMovedHandlers = new HashMap<>();

    private final Map<GConnector, EventHandler<MouseEvent>> dragDetectedHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseDraggedHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseDragEnteredHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseDragExitedHandlers = new HashMap<>();
    private final Map<GConnector, EventHandler<MouseEvent>> mouseDragReleasedHandlers = new HashMap<>();

    private GConnector hoveredConnector;
    private GConnector sourceConnector;
    private GConnector targetConnector;
    private GConnector removalConnector;

    private boolean repositionAllowed;

    private List<Point2D> jointPositions;

    /**
     * Creates a new {@link ConnectorDragManager}. Only one instance should exist per {@link DefaultGraphEditor}
     * instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up connector and tail skins
     * @param validatorManager the {@link ValidatorManager} used to determine which connectors can be connected
     * @param view the {@link GraphEditorView} to which tail skins will be added and removed during drag events
     */
    public ConnectorDragManager(final SkinLookup skinLookup, final ValidatorManager validatorManager,
            final GraphEditorView view) {

        this.skinLookup = skinLookup;
        this.validatorManager = validatorManager;
        this.view = view;
    }

    /**
     * Initializes the drag manager for the given model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        this.model = model;

        clearTrackingParameters();
        setHandlers();
    }

    /**
     * Clears all parameters that track things like what connector is currently hovered over, and so on.
     */
    private void clearTrackingParameters() {

        hoveredConnector = null;
        removalConnector = null;
        repositionAllowed = true;
        jointPositions = null;
    }

    /**
     * Sets all mouse and mouse-drag handlers for all connectors in the current model.
     */
    private void setHandlers() {

        for (final GNode node : model.getNodes()) {
            for (final GConnector connector : node.getConnectors()) {
                setHandlers(connector);
            }
        }
    }

    /**
     * Sets all mouse and mouse-drag handlers for a particular connector.
     *
     * @param connector the {@link GConnector} for which mouse and drag handlers should be set
     */
    private void setHandlers(final GConnector connector) {

        removeOldMouseHandlers(connector);
        removeOldMouseDragHandlers(connector);

        addMouseHandlers(connector);
        addMouseDragHandlers(connector);
    }

    /**
     * Removes any previously existing mouse event handlers from the connector's root JavaFX node that were added by
     * this class.
     *
     * @param connector the {@link GConnector} whose old handlers should be removed
     */
    private void removeOldMouseHandlers(final GConnector connector) {

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        if (mouseEnteredHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandlers.get(connector));
        }

        if (mouseExitedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandlers.get(connector));
        }

        if (mouseReleasedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandlers.get(connector));
        }

        if (mouseMovedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.MOUSE_MOVED, mouseMovedHandlers.get(connector));
        }
    }

    /**
     * Removes any previously existing mouse-drag event handlers from the connector's root JavaFX node that were added
     * by this class.
     *
     * @param connector the {@link GConnector} whose old handlers should be removed
     */
    private void removeOldMouseDragHandlers(final GConnector connector) {

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        if (dragDetectedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.DRAG_DETECTED, dragDetectedHandlers.get(connector));
        }

        if (mouseDraggedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandlers.get(connector));
        }

        if (mouseDragEnteredHandlers.get(connector) != null) {
            root.removeEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, mouseDragEnteredHandlers.get(connector));
        }

        if (mouseDragExitedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, mouseDragExitedHandlers.get(connector));
        }

        if (mouseDragReleasedHandlers.get(connector) != null) {
            root.removeEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, mouseDragReleasedHandlers.get(connector));
        }
    }

    /**
     * Adds mouse handlers to a particular connector.
     *
     * @param connector the {@link GConnector} to which mouse handlers should be added
     */
    private void addMouseHandlers(final GConnector connector) {

        final EventHandler<MouseEvent> newMouseEnteredHandler = event -> handleMouseEntered(event, connector);
        final EventHandler<MouseEvent> newMouseExitedHandler = event -> handleMouseExited(event, connector);
        final EventHandler<MouseEvent> newMouseReleasedHandler = event -> handleMouseReleased(event, connector);
        final EventHandler<MouseEvent> newMouseMovedHandler = event -> event.consume();

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        root.addEventHandler(MouseEvent.MOUSE_ENTERED, newMouseEnteredHandler);
        root.addEventHandler(MouseEvent.MOUSE_EXITED, newMouseExitedHandler);
        root.addEventHandler(MouseEvent.MOUSE_RELEASED, newMouseReleasedHandler);
        root.addEventHandler(MouseEvent.MOUSE_MOVED, newMouseMovedHandler);
    }

    /**
     * Handles mouse-entered events on the given connector.
     *
     * @param event a mouse-entered event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleMouseEntered(final MouseEvent event, final GConnector connector) {

        skinLookup.lookupConnector(connector).getRoot().setCursor(Cursor.DEFAULT);
        hoveredConnector = connector;
        event.consume();
    }

    /**
     * Handles mouse-exited events on the given connector.
     *
     * @param event a mouse-exited event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleMouseExited(final MouseEvent event, final GConnector connector) {

        skinLookup.lookupConnector(connector).getRoot().setCursor(Cursor.DEFAULT);
        hoveredConnector = null;
        event.consume();
    }

    /**
     * Handles mouse-released events on the given connector.
     *
     * @param event a mouse-released event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleMouseReleased(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (sourceConnector != null && skinLookup.lookupTail(sourceConnector) != null) {
            view.remove(skinLookup.lookupTail(sourceConnector));
        }

        if (targetConnector != null && skinLookup.lookupConnector(targetConnector) != null) {
            skinLookup.lookupConnector(targetConnector).applyStyle(GConnectorStyle.DEFAULT);
        }

        sourceConnector = null;
        removalConnector = null;
        jointPositions = null;
        repositionAllowed = true;

        event.consume();
    }

    /**
     * Adds mouse-drag handlers to a particular connector.
     *
     * @param connector the {@link GConnector} to which mouse-drag handlers should be added
     */
    private void addMouseDragHandlers(final GConnector connector) {

        final EventHandler<MouseEvent> newDragDetectedHandler = event -> handleDragDetected(event, connector);
        final EventHandler<MouseEvent> newMouseDraggedHandler = event -> handleMouseDragged(event, connector);
        final EventHandler<MouseDragEvent> newMouseDragEnteredHandler = event -> handleDragEntered(event, connector);
        final EventHandler<MouseDragEvent> newMouseDragExitedHandler = event -> handleDragExited(event, connector);
        final EventHandler<MouseDragEvent> newMouseDragReleasedHandler = event -> handleDragReleased(event, connector);

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        root.addEventHandler(MouseEvent.DRAG_DETECTED, newDragDetectedHandler);
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, newMouseDraggedHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, newMouseDragEnteredHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, newMouseDragExitedHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, newMouseDragReleasedHandler);
    }

    /**
     * Handles drag-detected events on the given connector.
     *
     * @param event a drag-detected event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleDragDetected(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (checkCreatable(connector)) {

            skinLookup.lookupConnector(connector).getRoot().startFullDrag();

            sourceConnector = connector;

            final GTailSkin tailSkin = skinLookup.lookupTail(connector);

            final double x = event.getX();
            final double y = event.getY();

            final Point2D connectorPosition = GeometryUtils.getConnectorPosition(connector, skinLookup);
            final Point2D cursorPosition = GeometryUtils.getCursorPosition(connector, x, y, skinLookup);

            tailSkin.draw(connectorPosition, cursorPosition);

            if (tailSkin.getRoot().getParent() == null) {
                view.add(tailSkin);
            }

            tailSkin.getRoot().toBack();

        } else if (checkRemovable(connector)) {

            skinLookup.lookupConnector(connector).getRoot().startFullDrag();

            removalConnector = connector;
        }

        event.consume();
    }

    /**
     * Handles mouse-dragged events on the given connector.
     *
     * @param event a mouse-dragged event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleMouseDragged(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (!repositionAllowed) {
            event.consume();
            return;
        }

        // Case for when the mouse first exits a connector during a drag gesture.
        if (removalConnector != null && !removalConnector.equals(hoveredConnector)) {
            detachConnection(event, connector);
        }

        if (sourceConnector != null) {

            final double x = event.getX();
            final double y = event.getY();

            final Point2D sourcePosition = GeometryUtils.getConnectorPosition(sourceConnector, skinLookup);
            final Point2D cursorPosition = GeometryUtils.getCursorPosition(connector, x, y, skinLookup);

            final GTailSkin tailSkin = skinLookup.lookupTail(sourceConnector);

            if (jointPositions != null) {
                tailSkin.draw(sourcePosition, cursorPosition, jointPositions);
            } else {
                tailSkin.draw(sourcePosition, cursorPosition);
            }

            tailSkin.setEndpointVisible(true);
        }

        event.consume();
    }

    /**
     * Handles drag-entered events on the given connector.
     *
     * @param event a drag-entered event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleDragEntered(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        final GConnectorValidator validator = validatorManager.getConnectorValidator();

        if (event.getButton().equals(MouseButton.PRIMARY) && validator.prevalidate(sourceConnector, connector)) {

            final Point2D sourcePosition = GeometryUtils.getConnectorPosition(sourceConnector, skinLookup);
            final Point2D targetPosition = GeometryUtils.getConnectorPosition(connector, skinLookup);

            final GTailSkin tailSkin = skinLookup.lookupTail(sourceConnector);

            if (jointPositions != null) {
                tailSkin.draw(sourcePosition, targetPosition, jointPositions);
            } else {
                tailSkin.draw(sourcePosition, targetPosition);
            }
            tailSkin.setEndpointVisible(false);

            repositionAllowed = false;

            if (validator.validate(sourceConnector, connector)) {
                skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DRAG_OVER_ALLOWED);
            } else {
                skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DRAG_OVER_FORBIDDEN);
            }
        }

        event.consume();
    }

    /**
     * Handles drag-exited events on the given connector.
     *
     * @param event a drag-exited event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleDragExited(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        final GConnectorValidator validator = validatorManager.getConnectorValidator();

        if (event.isPrimaryButtonDown() && validator.prevalidate(sourceConnector, connector)) {
            skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);
            repositionAllowed = true;
        }

        event.consume();
    }

    /**
     * Handles drag-released events on the given connector.
     *
     * @param event a drag-released event
     * @param connector the {@link GConnector} on which this event occured
     */
    private void handleDragReleased(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || event.isConsumed()) {
            return;
        }

        // Consume the event now so it doesn't fire repeatedly after re-initialization.
        event.consume();

        final GConnectorSkin targetConnectorSkin = skinLookup.lookupConnector(connector);
        final GConnectorValidator validator = validatorManager.getConnectorValidator();

        if (validator.prevalidate(sourceConnector, connector) && validator.validate(sourceConnector, connector)) {

            // Remember that adding a connection will fire a listener and reinitialize this class.
            addConnection(sourceConnector, connector);

            // After reinitialization we are still hovering over a connector, so we have to re-set that.
            hoveredConnector = connector;
        }

        targetConnectorSkin.applyStyle(GConnectorStyle.DEFAULT);
    }

    /**
     * Checks if a connection can be created from the given connector.
     *
     * @param connector a {@link GConnector} instance
     * @return {@code true} if a connection can be created from the given {@link GConnector}, {@code false} if not
     */
    private boolean checkCreatable(final GConnector connector) {
        return connector.getConnections().isEmpty() || !connector.isConnectionDetachedOnDrag();
    }

    /**
     * Checks if a connection can be removed from the given connector.
     *
     * @param connector a {@link GConnector} instance
     * @return {@code true} if a connection can be removed from the given {@link GConnector}, {@code false} if not
     */
    private boolean checkRemovable(final GConnector connector) {
        return !connector.getConnections().isEmpty() && connector.isConnectionDetachedOnDrag();
    }

    /**
     * Adds a new connection to the model.
     *
     * <p>
     * This will trigger the model listener and cause everything to be reinitialized.
     * </p>
     *
     * @param source the source {@link GConnector} for the new connection
     * @param target the target {@link GConnector} for the new connection
     */
    private void addConnection(final GConnector source, final GConnector target) {

        final String type = validatorManager.getConnectorValidator().createType(source, target);
        final List<Point2D> jointPositions = skinLookup.lookupTail(source).allocateJointPositions();

        final List<GJoint> joints = new ArrayList<>();

        for (final Point2D position : jointPositions) {

            final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
            joint.setX(position.getX());
            joint.setY(position.getY());
            joint.setType(type);

            joints.add(joint);
        }

        view.remove(skinLookup.lookupTail(source));

        Commands.addConnection(model, source, target, type, joints);
    }

    /**
     * Detaches the first connection from the given connector - i.e. removes the connection and replaces it with a tail.
     *
     * <p>
     * The connector instance is <b>silently</b> removed from the model, without triggering the listener to reinitialize
     * the graph editor. The tail is then drawn to the cursor location given by the mouse event.
     * <p>
     *
     * @param event the {@link MouseEvent} that caused the connection to be detached
     * @param connector the connector that the connection was detached from
     */
    private void detachConnection(final MouseEvent event, final GConnector connector) {

        sourceConnector = getFirstOpposingConnector(connector);
        targetConnector = connector;

        skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);

        if (connector.getConnections().isEmpty()) {
            return;
        }

        final GConnection connection = connector.getConnections().get(0);
        final GTailSkin tailSkin = skinLookup.lookupTail(sourceConnector);

        final double x = event.getX();
        final double y = event.getY();

        final Point2D sourcePosition = GeometryUtils.getConnectorPosition(sourceConnector, skinLookup);
        final Point2D cursorPosition = GeometryUtils.getCursorPosition(connector, x, y, skinLookup);

        // Save the joint positions of the connection so we can access them after the connection is removed.
        final List<Point2D> jointPositions = GeometryUtils.getJointPositions(connection, skinLookup);

        // If we are removing a source, the old target will be the new source and we need to reverse the joint
        // positions.
        if (connector.equals(connection.getSource())) {
            Collections.reverse(jointPositions);
        }

        Commands.removeConnection(model, connection);

        // Store joint positions in member variable so they can be accessed in future drag events.
        this.jointPositions = new ArrayList<>(jointPositions);

        tailSkin.draw(sourcePosition, cursorPosition, jointPositions);
        view.add(tailSkin);

        removalConnector = null;
    }

    /**
     * Gets the 'opposing' connector that the given connector's first connection is connected to.
     *
     * @param connector a {@link GConnector} instance
     *
     * @return the {@link GConnector} on the other end of the connection, or {@code null} if no connection is present
     */
    private GConnector getFirstOpposingConnector(final GConnector connector) {

        if (connector.getConnections().isEmpty()) {
            return null;
        } else {

            final GConnection connection = connector.getConnections().get(0);

            if (!connection.getSource().equals(connector)) {
                return connection.getSource();
            } else {
                return connection.getTarget();
            }
        }
    }
}
