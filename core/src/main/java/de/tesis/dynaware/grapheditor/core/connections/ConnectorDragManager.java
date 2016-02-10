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

    private final Map<Node, EventHandler<MouseEvent>> mouseEventHandlers = new HashMap<>();

    private GConnectorValidator validator = new DefaultConnectorValidator();

    private GConnector sourceConnector;
    private GConnector targetConnector;
    private GConnector removalConnector;

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

        removalConnector = null;
        repositionAllowed = true;
    }

    public void addConnector(final GConnector pConnector) {
        addMouseHandlers(pConnector);
    }

    public void removeConnector(final GConnector pConnector) {
        
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(pConnector);
        if (connectorSkin != null) {
            final Node root = connectorSkin.getRoot();
            if (root != null) {
                final EventHandler<MouseEvent> handler = mouseEventHandlers.remove(root);
                if (handler != null) {
                    root.removeEventHandler(MouseEvent.ANY, handler);
                }
            }
        }
    }

    /**
     * Sets all mouse and mouse-drag handlers for all connectors in the current
     * model.
     */
    private void setHandlers() {

        EventUtils.removeEventHandlers(mouseEventHandlers, MouseEvent.ANY);

        for (final GNode node : model.getNodes()) {
            for (final GConnector connector : node.getConnectors()) {
                addMouseHandlers(connector);
            }
        }
    }

    /**
     * Adds mouse handlers to a particular connector.
     *
     * @param connector
     *            the {@link GConnector} to which mouse handlers should be added
     */
    private void addMouseHandlers(final GConnector connector) {

        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        if (connectorSkin != null) {

            final Node root = connectorSkin.getRoot();
            if (root != null && !mouseEventHandlers.containsKey(root)) {

                final EventHandler<MouseEvent> newMouseHandler = event -> handleMouseEvent(event, connector);
                root.addEventHandler(MouseEvent.ANY, newMouseHandler);
                mouseEventHandlers.put(root, newMouseHandler);
            }
        }
    }

    private void handleMouseEvent(final MouseEvent event, final GConnector connector) {

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            // Consume the Event so the parent container
            // (ResizableBox/DraggableBox) does not move on connection detach
            event.consume();
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            handleMouseReleased(event, connector);
        } else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
            handleDragDetected(event, connector);
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            handleMouseDragged(event, connector);
        } else if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_ENTERED) {
            handleDragEntered(event, connector);
        } else if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_EXITED) {
            handleDragExited(event, connector);
        } else if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_RELEASED) {
            handleDragReleased(event, connector);
        }
    }

    /**
     * Handles mouse-released events on the given connector.
     *
     * @param event
     *            a mouse-released event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleMouseReleased(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (targetConnector != null && skinLookup.lookupConnector(targetConnector) != null) {
            skinLookup.lookupConnector(targetConnector).applyStyle(GConnectorStyle.DEFAULT);
        }

        sourceConnector = null;
        removalConnector = null;
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

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (checkCreatable(connector)) {

            sourceConnector = connector;
            skinLookup.lookupConnector(connector).getRoot().startFullDrag();
            tailManager.create(connector, event);

        } else if (checkRemovable(connector)) {

            removalConnector = connector;
            skinLookup.lookupConnector(connector).getRoot().startFullDrag();
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
    private void handleMouseDragged(final MouseEvent event, final GConnector connector) {

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
            return;
        }

        if (!repositionAllowed) {
            event.consume();
            return;
        }

        // Case for when the mouse first exits a connector during a drag gesture.
        if (removalConnector != null) {
            detachConnection(event, connector);
        } else {
            tailManager.updatePosition(event);
        }

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

        if (!event.getButton().equals(MouseButton.PRIMARY)) {
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

        if (!event.getButton().equals(MouseButton.PRIMARY) || event.isConsumed()) {
            return;
        }

        // Consume the event now so it doesn't fire repeatedly after re-initialization.
        event.consume();

        final GConnectorSkin targetConnectorSkin = skinLookup.lookupConnector(connector);

        if (validator.prevalidate(sourceConnector, connector) && validator.validate(sourceConnector, connector)) {
            addConnection(sourceConnector, connector);
        }

        targetConnectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        tailManager.cleanUp();
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
        return isEditable() && (connector.getConnections().isEmpty() || !connector.isConnectionDetachedOnDrag());
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
        return isEditable() && (!connector.getConnections().isEmpty() && connector.isConnectionDetachedOnDrag());
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

        // Notify the event manager so additional commands may be appended to this compound command.
        final GConnection addedConnection = model.getConnections().get(model.getConnections().size() - 1);
        connectionEventManager.notifyConnectionAdded(addedConnection, command);
    }

    /**
     * Detaches the first connection from the given connector - i.e. removes the
     * connection and replaces it with a tail.
     *
     * @param event
     *            the {@link MouseEvent} that caused the connection to be
     *            detached
     * @param connector
     *            the connector that the connection was detached from
     */
    private void detachConnection(final MouseEvent event, final GConnector connector) {

        sourceConnector = getFirstOpposingConnector(connector);
        targetConnector = connector;

        skinLookup.lookupConnector(connector).applyStyle(GConnectorStyle.DEFAULT);

        if (connector.getConnections().isEmpty()) {
            return;
        }

        final GConnection connection = connector.getConnections().get(0);
        tailManager.createFromConnection(connector, connection, event);

        final CompoundCommand command = ConnectionCommands.removeConnection(model, connection);

        // Notify the event manager so additional commands may be appended to this compound command.
        connectionEventManager.notifyConnectionRemoved(connection, command);

        removalConnector = null;
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
