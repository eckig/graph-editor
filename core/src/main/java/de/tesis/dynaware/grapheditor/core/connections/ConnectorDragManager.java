/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.connections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.VirtualSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.ConnectionCommands;
import de.tesis.dynaware.grapheditor.core.utils.EventUtils;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import de.tesis.dynaware.grapheditor.utils.GraphEventManager;
import de.tesis.dynaware.grapheditor.utils.GraphInputGesture;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
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

    private final EventHandler<MouseEvent> mouseExitedHandler = this::handleMouseExited;
    /**
     * Consume the Event so the parent container (ResizableBox/DraggableBox) does
     * not move on connection detach
     */
    private final EventHandler<MouseEvent> mousePressedHandler = Event::consume;

    private final Map<Node, EventHandler<MouseEvent>> mouseEnteredHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseReleasedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> dragDetectedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseDraggedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragEnteredHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragExitedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseDragEvent>> mouseDragReleasedHandlers = new HashMap<>();

    private GConnectorValidator validator = new DefaultConnectorValidator();

    private GConnector hoveredConnector;
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

    /**
     * Clears all parameters that track things like what connector is currently
     * hovered over, and so on.
     */
    private void clearTrackingParameters() {

        tailManager.cleanUp();
        hoveredConnector = null;
        removalConnector = null;
        repositionAllowed = true;
    }

    public void addConnector(final GConnector connector) {
        addMouseHandlers(connector);
    }

    public void removeConnector(final GConnector pConnectorToRemove)
    {
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(pConnectorToRemove);
        if (connectorSkin != null)
        {
            final Node root = connectorSkin.getRoot();
            if (root != null)
            {
                removeSingleEventHandler(root, mouseEnteredHandlers, MouseEvent.MOUSE_ENTERED);
                removeSingleEventHandler(root, mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
                removeSingleEventHandler(root, dragDetectedHandlers, MouseEvent.DRAG_DETECTED);
                removeSingleEventHandler(root, mouseDraggedHandlers, MouseEvent.MOUSE_DRAGGED);
                removeSingleEventHandler(root, mouseDragEnteredHandlers, MouseDragEvent.MOUSE_DRAG_ENTERED);
                removeSingleEventHandler(root, mouseDragExitedHandlers, MouseDragEvent.MOUSE_DRAG_EXITED);
                removeSingleEventHandler(root, mouseDragReleasedHandlers, MouseDragEvent.MOUSE_DRAG_RELEASED);

                removeGeneralEventHandlers(root);
            }
        }

        // the connector's tail we are dragging around has been removed..
        if (sourceConnector == pConnectorToRemove || targetConnector == pConnectorToRemove || removalConnector == pConnectorToRemove)
        {
            clearTrackingParameters();
        }
    }

    private void removeGeneralEventHandlers(final Node node)
    {
        node.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        node.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
    }

    private static <T extends Event> void removeSingleEventHandler(final Node node, final Map<Node, EventHandler<T>> eventHandlerMap,
            final EventType<T> eventType)
    {
        final EventHandler<T> handler = eventHandlerMap.remove(node);
        if (handler != null)
        {
            node.removeEventHandler(eventType, handler);
        }
    }

    /**
     * Sets all mouse and mouse-drag handlers for all connectors in the current
     * model.
     */
    private void setHandlers()
    {
        // here we assume that all event handler maps are of exactly the same size
        for (final Node node : mouseEnteredHandlers.keySet())
        {
            removeGeneralEventHandlers(node);
        }

        EventUtils.removeEventHandlers(mouseEnteredHandlers, MouseEvent.MOUSE_ENTERED);
        EventUtils.removeEventHandlers(mouseReleasedHandlers, MouseEvent.MOUSE_RELEASED);
        EventUtils.removeEventHandlers(dragDetectedHandlers, MouseEvent.DRAG_DETECTED);
        EventUtils.removeEventHandlers(mouseDraggedHandlers, MouseEvent.MOUSE_DRAGGED);
        EventUtils.removeEventHandlers(mouseDragEnteredHandlers, MouseDragEvent.MOUSE_DRAG_ENTERED);
        EventUtils.removeEventHandlers(mouseDragExitedHandlers, MouseDragEvent.MOUSE_DRAG_EXITED);
        EventUtils.removeEventHandlers(mouseDragReleasedHandlers, MouseDragEvent.MOUSE_DRAG_RELEASED);

        for (final GNode node : model.getNodes())
        {
            for (final GConnector connector : node.getConnectors())
            {
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

            final Node root = skinLookup.lookupConnector(connector).getRoot();
            if(root == null || mouseEnteredHandlers.containsKey(root)) {
                return;
            }

            final EventHandler<MouseEvent> newMouseEnteredHandler = event -> handleMouseEntered(event, connector);
            final EventHandler<MouseEvent> newMouseReleasedHandler = this::handleMouseReleased;

            final EventHandler<MouseEvent> newDragDetectedHandler = event -> handleDragDetected(event, connectorSkin);
            final EventHandler<MouseEvent> newMouseDraggedHandler = event -> handleMouseDragged(event, connector);
            final EventHandler<MouseDragEvent> newMouseDragEnteredHandler = event -> handleDragEntered(event, connectorSkin);
            final EventHandler<MouseDragEvent> newMouseDragExitedHandler = event -> handleDragExited(event, connectorSkin);
            final EventHandler<MouseDragEvent> newMouseDragReleasedHandler = event -> handleDragReleased(event, connectorSkin);

            root.addEventHandler(MouseEvent.MOUSE_ENTERED, newMouseEnteredHandler);
            root.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
            root.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
            root.addEventHandler(MouseEvent.MOUSE_RELEASED, newMouseReleasedHandler);

            root.addEventHandler(MouseEvent.DRAG_DETECTED, newDragDetectedHandler);
            root.addEventHandler(MouseEvent.MOUSE_DRAGGED, newMouseDraggedHandler);
            root.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, newMouseDragEnteredHandler);
            root.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, newMouseDragExitedHandler);
            root.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, newMouseDragReleasedHandler);

            mouseEnteredHandlers.put(root, newMouseEnteredHandler);
            mouseReleasedHandlers.put(root, newMouseReleasedHandler);

            dragDetectedHandlers.put(root, newDragDetectedHandler);
            mouseDraggedHandlers.put(root, newMouseDraggedHandler);
            mouseDragEnteredHandlers.put(root, newMouseDragEnteredHandler);
            mouseDragExitedHandlers.put(root, newMouseDragExitedHandler);
            mouseDragReleasedHandlers.put(root, newMouseDragReleasedHandler);
        }
    }

    /**
     * Handles mouse-entered events on the given connector.
     *
     * @param event
     *            a mouse-entered event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleMouseEntered(final MouseEvent event, final GConnector connector)
    {
        hoveredConnector = connector;
        event.consume();
    }

    /**
     * Handles mouse-exited events on the given connector.
     *
     * @param event
     *            a mouse-exited event
     */
    private void handleMouseExited(final MouseEvent event)
    {
        hoveredConnector = null;
        event.consume();
    }


    /**
     * Handles mouse-released events on the given connector.
     *
     * @param event
     *            a mouse-released event
     */
    private void handleMouseReleased(final MouseEvent event)
    {
        final GConnectorSkin targetConnectorSkin;
        if (targetConnector != null && (targetConnectorSkin = skinLookup.lookupConnector(targetConnector)) != null)
        {
            targetConnectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        }

        sourceConnector = null;
        removalConnector = null;
        repositionAllowed = true;

        tailManager.cleanUp();
        finishGesture();

        event.consume();
    }

    /**
     * Handles drag-detected events on the given connector.
     *
     * @param pEvent
     *            a drag-detected event
     * @param connectorSkin
     *            the {@link GConnectorSkin} on which this event occurred
     */
    private void handleDragDetected(final MouseEvent pEvent, final GConnectorSkin connectorSkin)
    {
        if (pEvent.getButton() != MouseButton.PRIMARY)
        {
            return;
        }

        final GConnector connector = connectorSkin.getItem();
        if (checkCreatable(connector) && activateGesture(pEvent))
        {
            sourceConnector = connector;
            connectorSkin.getRoot().startFullDrag();
            tailManager.cleanUp();
            tailManager.create(connector, pEvent);
        }
        else if (checkRemovable(connector) && activateGesture(pEvent))
        {
            removalConnector = connector;
            connectorSkin.getRoot().startFullDrag();
        }

        pEvent.consume();
    }

    /**
     * Handles mouse-dragged events on the given connector.
     *
     * @param event
     *            a mouse-dragged event
     * @param connector
     *            the {@link GConnector} on which this event occurred
     */
    private void handleMouseDragged(final MouseEvent event, final GConnector connector)
    {
        if (repositionAllowed && activateGesture(event))
        {
            // Case for when the mouse first exits a connector during a drag gesture.
            if (removalConnector != null && !removalConnector.equals(hoveredConnector))
            {
                detachConnection(event, connector);
            }
            else
            {
                tailManager.updatePosition(event);
            }
            event.consume();
        }
    }

    /**
     * Handles drag-entered events on the given connector.
     *
     * @param event
     *            a drag-entered event
     * @param connectorSkin
     *            the {@link GConnectorSkin} on which this event occurred
     */
    private void handleDragEntered(final MouseEvent event, final GConnectorSkin connectorSkin)
    {
        if (!activateGesture(event))
        {
            return;
        }

        final GConnector connector = connectorSkin.getItem();
        if (validator.prevalidate(sourceConnector, connector))
        {
            final boolean valid = validator.validate(sourceConnector, connector);
            tailManager.snapPosition(sourceConnector, connector, valid);

            repositionAllowed = false;

            if (valid)
            {
                connectorSkin.applyStyle(GConnectorStyle.DRAG_OVER_ALLOWED);
            }
            else
            {
                connectorSkin.applyStyle(GConnectorStyle.DRAG_OVER_FORBIDDEN);
            }
        }

        event.consume();
    }

    /**
     * Handles drag-exited events on the given connector.
     *
     * @param event
     *            a drag-exited event
     * @param connectorSkin
     *            the {@link GConnectorSkin} on which this event occurred
     */
    private void handleDragExited(final MouseEvent event, final GConnectorSkin connectorSkin)
    {
        connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        repositionAllowed = true;

        tailManager.updatePosition(event);

        event.consume();
    }

    /**
     * Handles drag-released events on the given connector.
     *
     * @param event
     *            a drag-released event
     * @param connectorSkin
     *            the {@link GConnectorSkin} on which this event occurred
     */
    private void handleDragReleased(final MouseEvent event, final GConnectorSkin connectorSkin)
    {
        if (event.isConsumed())
        {
            return;
        }

        // Consume the event now so it doesn't fire repeatedly after re-initialization.
        event.consume();

        final GConnector connector = connectorSkin.getItem();
        if (validator.prevalidate(sourceConnector, connector) && validator.validate(sourceConnector, connector))
        {
            addConnection(sourceConnector, connector);
        }

        connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        tailManager.cleanUp();
        finishGesture();
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
        return connector != null && connector.eContainer() instanceof GNode && checkEditable()
                && (connector.getConnections().isEmpty() || !connector.isConnectionDetachedOnDrag());
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
        return checkEditable() && !connector.getConnections().isEmpty() && connector.isConnectionDetachedOnDrag();
    }

    private boolean checkEditable()
    {
        return getEditorProperties() != null && !getEditorProperties().isReadOnly();
    }

    private GraphEditorProperties getEditorProperties()
    {
        return view == null ? null : view.getEditorProperties();
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

        ConnectionCommands.addConnection(model, source, target, connectionType, joints, connectionEventManager);
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
    private void detachConnection(final MouseEvent event, final GConnector connector)
    {
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        final int connectorCount = getConnectorCount(connector);
        if (connectorSkin != null)
        {
            connectorSkin.applyStyle(GConnectorStyle.DEFAULT);
        }

        if (connector.getConnections().isEmpty())
        {
            return;
        }

        boolean followUpCreated = false;

        final GConnection[] connections = connector.getConnections().toArray(new GConnection[0]);
        for (final GConnection connection : connections)
        {
            if (skinLookup.lookupConnection(connection) instanceof VirtualSkin)
            {
                // do not touch virtual connections
                continue;
            }
            final GConnector opposingConnector = getOpposingConnector(connection, connector);
            final List<Point2D> jointPositions = GeometryUtils.getJointPositions(connection, skinLookup);
            final GConnector newSource;
            if (connector.equals(connection.getSource()))
            {
                Collections.reverse(jointPositions);
                newSource = connection.getTarget();
            }
            else
            {
                newSource = connection.getSource();
            }

            ConnectionCommands.removeConnection(model, connection, connectionEventManager);

            final GConnectorSkin updateConnectorSkin;
            if ((updateConnectorSkin = skinLookup.lookupConnector(connector)) == null || updateConnectorSkin != connectorSkin
                    || connectorCount != getConnectorCount(connector))
            {
                // business logic decided to remove this connection or structurally change the parent node
                continue;
            }

            // check if the new source connector allows to create a new connection on the fly:
            if (!followUpCreated && checkCreatable(opposingConnector))
            {
                tailManager.updateToNewSource(jointPositions, newSource, event);
                sourceConnector = opposingConnector;
                targetConnector = connector;
                followUpCreated = true;
            }
        }

        // no follow up tail created -> clean up
        if (!followUpCreated)
        {
            clearTrackingParameters();
            sourceConnector = null;
            targetConnector = null;
            finishGesture();
        }

        removalConnector = null;
    }

    private static int getConnectorCount(final GConnector pConnector)
    {
        return pConnector == null || pConnector.getParent() == null ? 1 : pConnector.getParent().getConnectors().size();
    }

    private GConnector getOpposingConnector(final GConnection pConnection, final GConnector pConnector)
    {
        if (!pConnection.getSource().equals(pConnector))
        {
            return pConnection.getSource();
        }
        else
        {
            return pConnection.getTarget();
        }
    }

    private boolean activateGesture(final Event pEvent)
    {
        final GraphEventManager eventManager = getEditorProperties();
        if (eventManager != null)
        {
            eventManager.activateGesture(GraphInputGesture.CONNECT, pEvent, this);
        }
        return true;
    }

    private void finishGesture()
    {
        final GraphEventManager eventManager = getEditorProperties();
        if (eventManager != null)
        {
            eventManager.finishGesture(GraphInputGesture.CONNECT, this);
        }
    }
}
