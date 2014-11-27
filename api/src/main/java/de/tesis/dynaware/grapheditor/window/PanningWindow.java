/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

/**
 * A window over a large {@link Region} of content.
 *
 * <p>
 * This window can be panned around relative to its content. Only the parts of the content that are inside the window
 * will be rendered. Everything outside it is clipped.
 * </p>
 *
 */
public class PanningWindow extends Region {

    private final Rectangle clip = new Rectangle();

    private Region content;

    private final DoubleProperty windowXProperty = new SimpleDoubleProperty();
    private final DoubleProperty windowYProperty = new SimpleDoubleProperty();

    protected EventHandler<MouseEvent> mousePressedHandler;
    protected EventHandler<MouseEvent> mouseDraggedHandler;
    protected EventHandler<MouseEvent> mouseReleasedHandler;

    private Point2D clickPosition;

    private double windowXAtClick;
    private double windowYAtClick;

    private boolean panningGestureActive;
    private boolean cacheWhilePanning = true;

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow() {

        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());

        setClip(clip);

        final ChangeListener<Number> windowSizeChangeListener = (observableValue, oldValue, newValue) -> checkWindowBounds();

        widthProperty().addListener(windowSizeChangeListener);
        heightProperty().addListener(windowSizeChangeListener);
    }

    /**
     * Pans the window to the specified x and y coordinates.
     *
     * <p>
     * The window cannot be panned outside the content. When the window 'hits the edge' of the content it will stop.
     * </p>
     *
     * @param x the x position of the window relative to the top-left corner of the content
     * @param y the y position of the window relative to the top-left corner of the content
     */
    public void panTo(final double x, final double y) {

        windowXProperty.set(x);
        windowYProperty.set(y);

        checkWindowBounds();
    }

    /**
     * Pans the window by the specified x and y values.
     *
     * <p>
     * The window cannot be panned outside the content. When the window 'hits the edge' of the content it will stop.
     * </p>
     *
     * @param x the horizontal distance to move the window by
     * @param y the vertical distance to move the window by
     */
    public void panBy(final double x, final double y) {

        windowXProperty.set(windowXProperty.get() + x);
        windowYProperty.set(windowYProperty.get() + y);

        checkWindowBounds();
    }

    /**
     * Pans the window to the given position.
     *
     * <p>
     * <b>Note: </b><br>
     * The current width & height values of the window and its content are used in this method. It should therefore be
     * called <em>after</em> the scene has been drawn.
     * </p>
     */
    public void panTo(final WindowPosition position) {

        switch (position) {

        case TOP_CENTER:

            windowXProperty.set((content.getWidth() - getWidth()) / 2);
            windowYProperty.set(0);

            break;

        case CENTER:

            windowXProperty.set((content.getWidth() - getWidth()) / 2);
            windowYProperty.set((content.getHeight() - getHeight()) / 2);

            break;
        }

        checkWindowBounds();
    }

    /**
     * The property for the x coordinate of the window relative to the top-left corner of the content.
     *
     * @return the {@link DoubleProperty} for the x coordinate of the window
     */
    public DoubleProperty windowXProperty() {
        return windowXProperty;
    }

    /**
     * The property for the y coordinate of the window relative to the top-left corner of the content.
     *
     * @return the {@link DoubleProperty} for the y coordinate of the window
     */
    public DoubleProperty windowYProperty() {
        return windowYProperty;
    }

    /**
     * Checks that the window bounds are completely inside the content bounds, and repositions if necessary.
     *
     * <p>
     * Also checks that the window position values are integers to ensure strokes are drawn cleanly.
     * </p>
     */
    public void checkWindowBounds() {

        if (content != null) {

            if (windowXProperty.get() < 0) {
                windowXProperty.set(0);
            }

            if (windowYProperty.get() < 0) {
                windowYProperty.set(0);
            }

            final double zoomFactor = content.getLocalToSceneTransform().getMxx();
            final double maxX = zoomFactor * content.getWidth() - getWidth();
            final double maxY = zoomFactor * content.getHeight() - getHeight();

            if (windowXProperty.get() > maxX) {
                windowXProperty.set(maxX);
            }

            if (windowYProperty.get() > maxY) {
                windowYProperty.set(maxY);
            }

            windowXProperty.set(Math.round(windowXProperty.get()));
            windowYProperty.set(Math.round(windowYProperty.get()));
        }
    }

    /**
     * Gets whether the content's cache will be set to true on the content while panning.
     *
     * @return {@code true} if the content will be cached while panning is active
     * @see javafx.scene.Node#setCache
     */
    public boolean isCacheWhilePanning() {
        return cacheWhilePanning;
    }

    /**
     * Sets the content cache value while panning is active. Defaults to true.
     *
     * <p>
     * If this is set to true, large graphs will pan more smoothly but require more memory.
     * </p>
     * 
     * <p>
     * <em>Warning:</em> currently works like crap if the content has a scale transform applied to it.
     * </p>
     *
     * @param cacheWhilePanning {@code true} if the content will be cached while panning is active
     */
    public void setCacheWhilePanning(final boolean cacheWhilePanning) {
        this.cacheWhilePanning = cacheWhilePanning;
    }

    /**
     * Gets the content of the panning window.
     *
     * @return the {@link Region} that stores the content of the panning window, or {@code null}
     */
    protected Region getContent() {
        return content;
    }

    /**
     * Sets the content of the panning window.
     *
     * <p>
     * Note that the content's {@code managed} attribute will be set to false. Its size must therefore be set manually
     * using the {@code resize()} method of the {@link Node} class.
     * </p>
     *
     * @param content the {@link Region} to be displayed inside the panning window
     */
    protected void setContent(final Region content) {

        // Remove children and release bindings from old content, if any exists.
        if (this.content != null) {

            removeMouseHandlersFromContent();

            getChildren().remove(this.content);

            this.content.layoutXProperty().unbind();
            this.content.layoutYProperty().unbind();
        }

        this.content = content;

        if (content != null) {

            content.setManaged(false);
            content.cacheHintProperty().set(CacheHint.SPEED);

            getChildren().add(content);

            content.layoutXProperty().bind(windowXProperty.multiply(-1));
            content.layoutYProperty().bind(windowYProperty.multiply(-1));

            addMouseHandlersToContent();
        }
    }

    /**
     * Adds mouse handlers to the content to pan the {@link PanningWindow} upon right mouse click-and-drag.
     */
    private void addMouseHandlersToContent() {

        mousePressedHandler = event -> {

            if (!event.getButton().equals(MouseButton.SECONDARY)) {
                return;
            }

            setCursor(Cursor.MOVE);

            if (cacheWhilePanning) {
                content.setCache(true);
            }

            panningGestureActive = true;

            clickPosition = new Point2D(event.getSceneX(), event.getSceneY());

            windowXAtClick = windowXProperty().get();
            windowYAtClick = windowYProperty().get();
        };

        mouseDraggedHandler = event -> {

            if (!panningGestureActive) {
                return;
            }

            final Point2D currentPosition = new Point2D(event.getSceneX(), event.getSceneY());

            final double deltaX = currentPosition.getX() - clickPosition.getX();
            final double deltaY = currentPosition.getY() - clickPosition.getY();

            final double newWindowX = windowXAtClick - deltaX;
            final double newWindowY = windowYAtClick - deltaY;

            panTo(newWindowX, newWindowY);
        };

        mouseReleasedHandler = event -> {

            if (!event.getButton().equals(MouseButton.SECONDARY)) {
                return;
            }

            setCursor(null);

            if (cacheWhilePanning) {
                content.setCache(false);
            }

            panningGestureActive = false;
        };

        content.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        content.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
    }

    /**
     * Removes existing mouse pressed, drag, and released handlers from the content, if possible.
     */
    private void removeMouseHandlersFromContent() {

        if (mousePressedHandler != null) {
            content.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        }
        if (mouseDraggedHandler != null) {
            content.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        }
        if (mouseReleasedHandler != null) {
            content.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        }
    }
}
