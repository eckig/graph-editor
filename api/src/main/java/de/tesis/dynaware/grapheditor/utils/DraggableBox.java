/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A draggable box that can display children.
 *
 * <p>
 * This is a subclass of {@link StackPane} and will lay out its children accordingly. The size of the box should be set
 * via {@code resize(width, height)}, and will not be affected by parent layout.
 * </p>
 */
public class DraggableBox extends StackPane {

    private static final double DEFAULT_ALIGNMENT_THRESHOLD = 5;

    // Safety mechanism for exceptional case when 'drag' events occur without a 'pressed' event occuring first.
    protected boolean dragActive;

    protected double lastLayoutX;
    protected double lastLayoutY;

    protected double lastMouseX;
    protected double lastMouseY;

    protected double lastParentWidth;
    protected double lastParentHeight;

    protected double absoluteMaxWidth;
    protected double absoluteMaxHeight;

    protected GraphEditorProperties editorProperties;

    protected Region container;

    // Note that ResizableBox subclass currently pays no attention to alignment targets!
    private List<Double> alignmentTargetsX;
    private List<Double> alignmentTargetsY;

    private double alignmentThreshold = DEFAULT_ALIGNMENT_THRESHOLD;

    private final BooleanProperty dragEnabledXProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty dragEnabledYProperty = new SimpleBooleanProperty(true);

    private Point2D snapToGridOffset = new Point2D(0, 0);

    /**
     * Creates an empty draggable box.
     */
    public DraggableBox() {

        setPickOnBounds(false);

        // Must be default or quality for re-rasterization to occur after a scale transform.
        cacheHintProperty().set(CacheHint.DEFAULT);

        addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    /**
     * Sets the editor properties object that the drag logic should respect.
     *
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b> call it. Editor properties should instead
     * be set via the graph editor instance.
     * </p>
     *
     * @param editorProperties the {@link GraphEditorProperties} instance for the graph editor
     */
    public void setEditorProperties(final GraphEditorProperties editorProperties) {
        this.editorProperties = editorProperties;
    }

    /**
     * Gets whether dragging of the box is enabled in the x (horizontal) direction.
     *
     * @return {@code true} if dragging is enabled in the x direction, {@code false} if not
     */
    public boolean isDragEnabledX() {
        return dragEnabledXProperty.get();
    }

    /**
     * Sets whether dragging of the box is enabled in the x (horizontal) direction.
     *
     * @param dragEnabledX {@code true} if dragging is enabled in the x direction, {@code false} if not
     */
    public void setDragEnabledX(final boolean dragEnabledX) {
        dragEnabledXProperty.set(dragEnabledX);
    }

    /**
     * The property for whether dragging of the box is enabled in the x (horizontal) direction.
     *
     * @return the {@link BooleanProperty} for whether dragging is enabled in the x direction
     */
    public BooleanProperty dragEnabledXProperty() {
        return dragEnabledXProperty;
    }

    /**
     * Gets whether dragging of the box is enabled in the y (vertical) direction.
     *
     * @return {@code true} if dragging is enabled in the y direction, {@code false} if not
     */
    public boolean isDragEnabledY() {
        return dragEnabledYProperty.get();
    }

    /**
     * Sets whether dragging of the box is enabled in the y (vertical) direction.
     *
     * @param dragEnabledY {@code true} if dragging is enabled in the y direction, {@code false} if not
     */
    public void setDragEnabledY(final boolean dragEnabledY) {
        dragEnabledYProperty.set(dragEnabledY);
    }

    /**
     * The property for whether dragging of the box is enabled in the y (vertical) direction.
     *
     * @return the {@link BooleanProperty} for whether dragging is enabled in the y direction
     */
    public BooleanProperty dragEnabledYProperty() {
        return dragEnabledYProperty;
    }

    /**
     * Gets the set of x values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @return a list of x values that the box will align to when dragged, or {@code null}
     */
    public List<Double> getAlignmentTargetsX() {
        return alignmentTargetsX;
    }

    /**
     * Sets the set of x values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @param alignmentTargetsX a list of x values that the box will align to when dragged, or {@code null}
     */
    public void setAlignmentTargetsX(final List<Double> alignmentTargetsX) {
        this.alignmentTargetsX = alignmentTargetsX;
    }

    /**
     * Gets the set of y values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @return a list of y values that the box will align to when dragged, or {@code null}
     */
    public List<Double> getAlignmentTargetsY() {
        return alignmentTargetsY;
    }

    /**
     * Sets the set of y values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @param alignmentTargetsY a list of y values that the box will align to when dragged, or {@code null}
     */
    public void setAlignmentTargetsY(final List<Double> alignmentTargetsY) {
        this.alignmentTargetsY = alignmentTargetsY;
    }

    /**
     * Gets the alignment threshold value.
     *
     * <p>
     * If the distance between the box and an alignment value is less than or equal to this threshold, the box will move
     * to the alignment value.
     * </p>
     *
     * @return the alignment threshold value
     */
    public double getAlignmentThreshold() {
        return alignmentThreshold;
    }

    /**
     * Sets the alignment threshold value.
     *
     * <p>
     * If the distance between the box and an alignment target is lower than this threshold, the box will move to the
     * alignment target.
     * </p>
     *
     * @param alignmentThreshold the alignment threshold value
     */
    public void setAlignmentThreshold(final double alignmentThreshold) {
        this.alignmentThreshold = alignmentThreshold;
    }

    /**
     * Gets the offset that the box will snap to when a drag finishes and snap-to-grid is active.
     *
     * @return the snap offset for snap-to-grid calculations
     */
    public Point2D getSnapToGridOffset() {
        return snapToGridOffset;
    }

    /**
     * Sets the offset that the box will snap to when a drag finishes and snap-to-grid is active.
     *
     * <p>
     * The offset is taken from the top-left corner and is <b>(0, 0)</b> by default. If the default value is used, the
     * top-left corner of the box will snap exactly onto a grid line.
     * </p>
     *
     * @param snapToGridOffset the snap offset for snap-to-grid calculations
     */
    public void setSnapToGridOffset(final Point2D snapToGridOffset) {
        this.snapToGridOffset = snapToGridOffset;
    }

    @Override
    public boolean isResizable() {
        return false;
    }
    
    protected boolean isEditable() {
        return editorProperties != null && !editorProperties.isReadOnly();
    }
    
    protected boolean isDragGestureActive() {
        return editorProperties != null
                && editorProperties.getGraphEventManager().isInputGestureActiveOrInactive(GraphInputGesture.DRAG);
    }
    
    void notifyDragActive() {
        
        dragActive = true;
        if(editorProperties != null) {
            editorProperties.getGraphEventManager().setInputGesture(GraphInputGesture.DRAG);
        }
    }
    
    private void notifyDragInactive() {
        
        dragActive = false;
        if(editorProperties != null) {
            editorProperties.getGraphEventManager().setInputGesture(null);
        }
    }

    /**
     * Handles mouse-pressed events.
     *
     * @param event a {@link MouseEvent}
     */
    protected void handleMousePressed(final MouseEvent event) {

        if (!event.isPrimaryButtonDown() || !isEditable() || !isDragGestureActive()) {
            return;
        }

        container = getContainer(this);

        final Point2D cursorPosition = GeometryUtils.getCursorPosition(event, container);
        storeClickValuesForDrag(cursorPosition.getX(), cursorPosition.getY());
        notifyDragActive();
        event.consume();
    }

    /**
     * Handles mouse-dragged events.
     *
     * @param event {@link MouseEvent}
     */
    protected void handleMouseDragged(final MouseEvent event) {

        if (!event.isPrimaryButtonDown() || !isEditable() || !isDragGestureActive()) {
            return;
        }
        
        if(container == null || !dragActive) {
            return;
        }

        final Point2D cursorPosition = GeometryUtils.getCursorPosition(event, container);
        handleDrag(cursorPosition.getX(), cursorPosition.getY());
        notifyDragActive();
        event.consume();
    }

    /**
     * Handles mouse-released events.
     *
     * @param event {@link MouseEvent}
     */
    protected void handleMouseReleased(final MouseEvent event) {

        if (!event.isPrimaryButtonDown() || !isEditable() || !isDragGestureActive()) {
            return;
        }

        notifyDragInactive();
        event.consume();
    }

    /**
     * Stores relevant layout values at the time of the last mouse click (mouse-pressed event).
     *
     * @param x the container-x position of the click event
     * @param y the container-y position of the click event
     */
    protected void storeClickValuesForDrag(final double x, final double y) {

        lastLayoutX = getLayoutX();
        lastLayoutY = getLayoutY();

        lastMouseX = x;
        lastMouseY = y;

        if (container != null && container.getWidth() > 0) {
            lastParentWidth = container.getWidth();
        } else {
            lastParentWidth = Double.MAX_VALUE;
        }

        if (container != null && container.getHeight() > 0) {
            lastParentHeight = container.getHeight();
        } else {
            lastParentHeight = Double.MAX_VALUE;
        }

        if (container != null && container.getMaxWidth() > 0) {
            absoluteMaxWidth = container.getMaxWidth();
        } else {
            absoluteMaxWidth = Double.MAX_VALUE;
        }

        if (container != null && container.getMaxHeight() > 0) {
            absoluteMaxHeight = container.getMaxHeight();
        } else {
            absoluteMaxHeight = Double.MAX_VALUE;
        }
    }

    /**
     * Rounds some value to the nearest multiple of the grid spacing.
     *
     * @param value a double value
     *
     * @return the input value rounded to the nearest multiple of the grid spacing
     */
    protected double roundToGridSpacing(final double value) {

        final double spacing = editorProperties == null ? GraphEditorProperties.DEFAULT_GRID_SPACING
                : editorProperties.getGridSpacing();
        return spacing * Math.round(value / spacing);
    }

    /**
     * Handles a drag event to the given cursor position.
     *
     * @param x the cursor x position relative to the container
     * @param y the cursor y position relative to the container
     */
    private void handleDrag(final double x, final double y) {

        if (dragEnabledXProperty.get()) {
            handleDragX(x);
        }

        if (dragEnabledYProperty.get()) {
            handleDragY(y);
        }
    }

    /**
     * Handles the x component of a drag event to the given cursor x position.
     *
     * @param x the cursor x position
     */
    private void handleDragX(final double x) {

        final double maxParentWidth = editorProperties != null && editorProperties.isEastBoundActive() ? lastParentWidth
                : absoluteMaxWidth;

        final double minLayoutX = editorProperties == null ? GraphEditorProperties.DEFAULT_BOUND_VALUE
                : editorProperties.getWestBoundValue();
        final double maxLayoutX = maxParentWidth - getWidth()
                - (editorProperties == null ? GraphEditorProperties.DEFAULT_BOUND_VALUE
                        : editorProperties.getEastBoundValue());

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        double newLayoutX = lastLayoutX + (x - lastMouseX) / scaleFactor;

        if (editorProperties != null && editorProperties.isSnapToGridOn()) {
            // The -1 here is to put the rectangle border exactly on top of a grid line.
            newLayoutX = roundToGridSpacing(newLayoutX - snapToGridOffset.getX()) + snapToGridOffset.getX() - 1;
        } else {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newLayoutX = Math.round(newLayoutX);

            if (alignmentTargetsX != null) {
                newLayoutX = align(newLayoutX, alignmentTargetsX);
            }
        }

        if (editorProperties != null && editorProperties.isWestBoundActive() && newLayoutX < minLayoutX) {
            newLayoutX = minLayoutX;
        } else if (newLayoutX > maxLayoutX) {
            newLayoutX = maxLayoutX;
        }

        setLayoutX(newLayoutX);
    }

    /**
     * Handles the y component of a drag event to the given cursor y position.
     *
     * @param y the cursor y position
     */
    private void handleDragY(final double y) {

        final double maxParentHeight = editorProperties != null && editorProperties.isSouthBoundActive() ? lastParentHeight : absoluteMaxHeight;

        final double minLayoutY = editorProperties.getNorthBoundValue();
        final double maxLayoutY = maxParentHeight - getHeight() - editorProperties.getSouthBoundValue();

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        double newLayoutY = lastLayoutY + (y - lastMouseY) / scaleFactor;

        if (editorProperties != null && editorProperties.isSnapToGridOn()) {
            // The -1 here is to put the rectangle border exactly on top of a grid line.
            newLayoutY = roundToGridSpacing(newLayoutY - snapToGridOffset.getY()) + snapToGridOffset.getY() - 1;
        } else {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newLayoutY = Math.round(newLayoutY);

            if (alignmentTargetsY != null) {
                newLayoutY = align(newLayoutY, alignmentTargetsY);
            }
        }

        if (editorProperties != null && editorProperties.isNorthBoundActive() && newLayoutY < minLayoutY) {
            newLayoutY = minLayoutY;
        } else if (newLayoutY > maxLayoutY) {
            newLayoutY = maxLayoutY;
        }

        setLayoutY(newLayoutY);
    }

    /**
     * Gets the closest ancestor (e.g. parent, grandparent) to a node that is a subclass of {@link Region}.
     *
     * @param node a JavaFX {@link Node}
     * @return the node's closest ancestor that is a subclass of {@link Region}, or {@code null} if none exists
     */
    private Region getContainer(final Node node) {

        final Parent parent = node.getParent();

        if (parent == null) {
            return null;
        } else if (parent instanceof Region) {
            return (Region) parent;
        } else {
            return getContainer(parent);
        }
    }

    /**
     * Aligns the given position to the first alignment value that is closer than the alignment threshold.
     *
     * <p>
     * Returns the original position if no alignment values are nearby.
     * </p>
     *
     * @param position the position to be aligned
     * @param alignmentValues the list of the alignment values
     * @return the new position after alignment
     */
    private double align(final double position, final List<Double> alignmentValues) {

        for (final Double alignmentValue : alignmentValues) {
            if (Math.abs(alignmentValue - position) <= alignmentThreshold) {
                return alignmentValue;
            }
        }

        return position;
    }
}
