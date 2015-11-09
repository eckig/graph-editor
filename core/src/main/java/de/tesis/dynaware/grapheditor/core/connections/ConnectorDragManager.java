/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.EventUtils;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;

/**
 * Responsible for what happens when connectors are dragged in the graph editor.
 *
 * <p>
 * Namely, the creation, removal, and repositioning of connections.
 * </p>
 */
public class ConnectorDragManager {

    private final TailManager tailManager;

    private final GraphEditorView view;
    private final SkinLookup skinLookup;
    private final ConnectionEventManager connectionEventManager;

    private GModel model;

    private final Map<Node, EventHandler<MouseEvent>> mouseEnteredHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseReleasedHandlers = new HashMap<>();

    private final Map<Node, EventHandler<MouseEvent>> dragDetectedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseDraggedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragEnteredHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragExitedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragReleasedHandlers = new HashMap<>();

    private GConnectorValidator validator = new DefaultConnectorValidator();

    private GConnector sourceConnector;
    
    private boolean repositionAllowed;

    /**
     * Creates a new {@link ConnectorDragManager}. Only one instance should
     * exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup
     *            the {@link SkinLookup} used to look up connector and tail
     *            skins
     * @param connectionEventManager
     *            the {@link ConnectionEventManager} used to notify users of
     *            connection events
     * @param view
     *            the {@link GraphEditorView} to which tail skins will be added
     *            and removed during drag events
     */
    public ConnectorDragManager(final SkinLookup skinLookup, final ConnectionEventManager connectionEventManager,
            final GraphEditorView view) {

        this.view = view;
        this.skinLookup = skinLookup;
        this.connectionEventManager = connectionEventManager;

        tailManager = new TailManager(skinLookup, view);
    }

    /**
     * Initializes the drag manager for the given model.
     *
     * @param model
     *            the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        this.model = model;

        clearTrackingParameters();
        removeEventHandler();
        setHandlers();
    }

    /**
     * Sets the validator that determines what connections can be created.
     * 
     * @param validator
     *            a {@link GConnectorValidator} implementation, or null to use
     *            the default
     */
    public void setValidator(final GConnectorValidator validator) {
        if (validator != null) {
            this.validator = validator;
        } else {
            this.validator = new DefaultConnectorValidator();
        }
    }
    
    private boolean isEditable() {
        return view != null && view.getEditorProperties() != null && !view.getEditorProperties().isReadOnly();
    }

    /**
     * Clears all parameters that track things like what connector is currently
     * hovered over, and so on.
     */
    private void clearTrackingParameters() {
        repositionAllowed = true;
    }

    private void removeEventHandler() {
        EventUtils.removeEventHandlers(mouseEnteredHandlers, MouseEvent.MOUSE_ENTERED);
        EventUtils.removeEventHandlers(mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
        EventUtils.removeEventHandlers(dragDetectedHandlers, MouseEvent.DRAG_DETECTED);
        EventUtils.removeEventHandlers(mouseDraggedHandlers, MouseEvent.MOUSE_DRAGGED);
        EventUtils.removeEventHandlers(mouseDragEnteredHandlers, MouseDragEvent.MOUSE_DRAG_ENTERED);
        EventUtils.removeEventHandlers(mouseDragExitedHandlers, MouseDragEvent.MOUSE_DRAG_EXITED);
        EventUtils.removeEventHandlers(mouseDragReleasedHandlers, MouseDragEvent.MOUSE_DRAG_RELEASED);
    }

    /**
     * Sets all mouse and mouse-drag handlers for all connectors in the current
     * model.
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
     * @param connector
     *            the {@link GConnector} for which mouse and drag handlers
     *            should be set
     */
    private void setHandlers(final GConnector connector) {
        addMouseHandlers(connector);
        addMouseDragHandlers(connector);
    }

    /**
     * Adds mouse handlers to a particular connector.
     *
     * @param connector
     *            the {@link GConnector} to which mouse handlers should be added
     */
    private void addMouseHandlers(final GConnector connector) {

        final EventHandler<MouseEvent> newMouseEnteredHandler = event -> handleMouseEntered(event, connector);
        final EventHandler<MouseEvent> newMouseReleasedHandler = event -> handleMouseReleased(event);

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        root.addEventHandler(MouseEvent.MOUSE_ENTERED, newMouseEnteredHandler);
        root.addEventHandler(MouseEvent.MOUSE_RELEASED, newMouseReleasedHandler);

        mouseEnteredHandlers.put(root, newMouseEnteredHandler);
        mouseReleasedHandlers.put(root, newMouseReleasedHandler);
    }

    /**
     * Adds mouse-drag handlers to a particular connector.
     *
     * @param connector
     *            the {@link GConnector} to which mouse-drag handlers should be
     *            added
     */
    private void addMouseDragHandlers(final GConnector connector) {

        final EventHandler<MouseEvent> newDragDetectedHandler = event -> handleDragDetected(event, connector);
        final EventHandler<MouseEvent> newMouseDraggedHandler = this::handleMouseDragged;
        final EventHandler<MouseDragEvent> newMouseDragEnteredHandler = event -> handleDragEntered(event, connector);
        final EventHandler<MouseDragEvent> newMouseDragExitedHandler = event -> handleDragExited(event, connector);
        final EventHandler<MouseDragEvent> newMouseDragReleasedHandler = event -> handleDragReleased(event, connector);

        final Node root = skinLookup.lookupConnector(connector).getRoot();

        root.addEventHandler(MouseEvent.DRAG_DETECTED, newDragDetectedHandler);
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, newMouseDraggedHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, newMouseDragEnteredHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, newMouseDragExitedHandler);
        root.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, newMouseDragReleasedHandler);

        dragDetectedHandlers.put(root, newDragDetectedHandler);
        mouseDraggedHandlers.put(root, newMouseDraggedHandler);
        mouseDragEnteredHandlers.put(root, newMouseDragEnteredHandler);
        mouseDragExitedHandlers.put(root, newMouseDragExitedHandler);
        mouseDragReleasedHandlers.put(root, newMouseDragReleasedHandler);
    }

    /**
     * Handles mouse-entered events on the given connector.
     *
     * @param event
     *            a mouse-entered event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleMouseEntered(final MouseEvent event, final GConnector connector) {

        final Parent parent = skinLookup.lookupConnector(connector).getRoot().getParent();
        if (parent != null) {
            parent.setCursor(null);
        }

        event.consume();
    }

    /**
     * Handles mouse-released events on the given connector.
     *
     * @param event
     *            a mouse-released event
     */
    private void handleMouseReleased(final MouseEvent event) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || !isEditable()) {
            return;
        }

        sourceConnector = null;
        repositionAllowed = true;

        tailManager.cleanUp();

        event.consume();
    }

    /**
     * Handles drag-detected events on the given connector.
     *
     * @param event
     *            a drag-detected event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleDragDetected(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || !isEditable()) {
            return;
        }

        if (checkCreatable(connector)) {

            sourceConnector = connector;
            skinLookup.lookupConnector(connector).getRoot().startFullDrag();
            tailManager.create(connector, event);

        } else if (checkRemovable(connector)) {

            skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);
            detachConnection(connector);
            handleMouseReleased(event);
        }

        event.consume();
    }

    /**
     * Handles mouse-dragged events on the given connector.
     *
     * @param event
     *            a mouse-dragged event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleMouseDragged(final MouseEvent event) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || !isEditable()) {
            return;
        }

        if (!repositionAllowed) {
            event.consume();
            return;
        }

        tailManager.updatePosition(event);
        event.consume();
    }

    /**
     * Handles drag-entered events on the given connector.
     *
     * @param event
     *            a drag-entered event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleDragEntered(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || !isEditable()) {
            return;
        }

        if (event.getButton().equals(MouseButton.PRIMARY) && validator.prevalidate(sourceConnector, connector)) {

            final boolean valid = validator.validate(sourceConnector, connector);
            tailManager.snapPosition(sourceConnector, connector, valid);

            repositionAllowed = false;

            if (valid) {
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
     * @param event
     *            a drag-exited event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleDragExited(final MouseEvent event, final GConnector connector) {

        skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);
        repositionAllowed = true;

        if (event.getButton().equals(MouseButton.PRIMARY)) {
            tailManager.updatePosition(event);
        }

        event.consume();
    }

    /**
     * Handles drag-released events on the given connector.
     *
     * @param event
     *            a drag-released event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleDragReleased(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY) || event.isConsumed() || !isEditable()) {
            return;
        }

        final GConnectorSkin targetConnectorSkin = skinLookup.lookupConnector(connector);

        if (validator.prevalidate(sourceConnector, connector) && validator.validate(sourceConnector, connector)) {

            // Remember that adding a connection will fire a listener and
            // reinitialize this class.
            addConnection(sourceConnector, connector);
        }

        targetConnectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        handleMouseReleased(event); // consumes the event
    }

    /**
     * Checks if a connection can be created from the given connector.
     *
     * @param connector
     *            a {@link GConnector} instance
     * @return {@code true} if a connection can be created from the given
     *         {@link GConnector}, {@code false} if not
     */
    private boolean checkCreatable(final GConnector connector) {
        return connector.getConnections().isEmpty() || !connector.isConnectionDetachedOnDrag();
    }

    /**
     * Checks if a connection can be removed from the given connector.
     *
     * @param connector
     *            a {@link GConnector} instance
     * @return {@code true} if a connection can be removed from the given
     *         {@link GConnector}, {@code false} if not
     */
    private boolean checkRemovable(final GConnector connector) {
        return !connector.getConnections().isEmpty() && connector.isConnectionDetachedOnDrag();
    }

    /**
     * Adds a new connection to the model.
     *
     * <p>
     * This will trigger the model listener and cause everything to be
     * reinitialized.
     * </p>
     *
     * @param source
     *            the source {@link GConnector} for the new connection
     * @param target
     *            the target {@link GConnector} for the new connection
     */
    private void addConnection(final GConnector source, final GConnector target) {

        final String connectionType = validator.createConnectionType(source, target);
        final String jointType = validator.createJointType(source, target);
        final List<Point2D> jointPositions = skinLookup.lookupTail(source).allocateJointPositions();

        final List<GJoint> joints = new ArrayList<>();

        for (final Point2D position : jointPositions) {

            final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
            joint.setX(position.getX());
            joint.setY(position.getY());
            joint.setType(jointType);

            joints.add(joint);
        }

        final CompoundCommand command = ConnectionCommands.addConnection(model, source, target, connectionType, joints);

        // Notify the event manager so additional commands may be appended to
        // this compound command.
        final GConnection addedConnection = model.getConnections().get(model.getConnections().size() - 1);
        connectionEventManager.notifyConnectionAdded(addedConnection, command);
    }

    /**
     * Detaches the first connection from the given connector - i.e. removes the
     * connection and replaces it with a tail.
     *
     * @param connector
     *            the connector that the connection was detached from
     */
    private void detachConnection(final GConnector connector) {

        sourceConnector = getFirstOpposingConnector(connector);

        skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);

        if (connector.getConnections().isEmpty()) {
            return;
        }

        final GConnection connection = connector.getConnections().get(0);
        final CompoundCommand command = ConnectionCommands.removeConnection(model, connection);

        // Notify the event manager so additional commands may be appended to
        // this compound command.
        connectionEventManager.notifyConnectionRemoved(connection, command);
    }

    /**
     * Gets the 'opposing' connector that the given connector's first connection
     * is connected to.
     *
     * @param connector
     *            a {@link GConnector} instance
     *
     * @return the {@link GConnector} on the other end of the connection, or
     *         {@code null} if no connection is present
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
