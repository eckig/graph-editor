/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

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

    private final EventHandler<MouseEvent> mousePressedHandler = this::handlePanningMousePressed;
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handlePanningMouseDragged;
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handlePanningMouseReleased;

    private Point2D clickPosition;

    private double windowXAtClick;
    private double windowYAtClick;

    private boolean panningGestureActive;
    private boolean panningActive = true;
    
    private final EventHandler<ZoomEvent> zoomHandler = this::handleZoom;
    private final EventHandler<TouchEvent> touchHandler = this::handleTouch;
    private int currentTouchCount = 0;
    private final Scale scaleTransform = new Scale(1, 1, 0, 0);

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
     *
     * @param position the {@link WindowPosition} to pan to
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

			double x = Math.max(windowXProperty.get(), 0);
			double y = Math.max(windowYProperty.get(), 0);

			final double zoomFactor = content.getLocalToSceneTransform().getMxx();
			final double maxX = zoomFactor * content.getWidth() - getWidth();
			final double maxY = zoomFactor * content.getHeight() - getHeight();

			if (x > maxX) {
				x = (maxX);
			}

			if (y > maxY) {
				y = (maxY);
			}

			windowXProperty.set(Math.round(x));
			windowYProperty.set(Math.round(y));
		}
    }

    @Override
    public ObservableList<Node> getChildren() {
        return super.getChildren();
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
            this.content.getTransforms().remove(scaleTransform);
        }

        this.content = content;

        if (content != null) {

            content.setManaged(false);

            getChildren().add(content);

            content.layoutXProperty().bind(windowXProperty.multiply(-1));
            content.layoutYProperty().bind(windowYProperty.multiply(-1));
            content.getTransforms().add(scaleTransform);

            addMouseHandlersToContent();
        }
    }
    
    public void setPanningActive(boolean panningActive) {
		this.panningActive = panningActive;
	}
    
    public boolean isPanningActive() {
		return panningActive;
	}

    private void handlePanningMousePressed(final MouseEvent event) {
    	if (isPanningActive() && currentTouchCount < 2) {
        	startPanning(event.getSceneX(), event.getSceneY());
        }
    }
    
    private void handlePanningMouseDragged(final MouseEvent event) {
    	if (!isPanningActive() || currentTouchCount > 1) {
            return;
        }

        if (!panningGestureActive) {
            startPanning(event.getSceneX(), event.getSceneY());
        }

        final Point2D currentPosition = new Point2D(event.getSceneX(), event.getSceneY());

        final double deltaX = currentPosition.getX() - clickPosition.getX();
        final double deltaY = currentPosition.getY() - clickPosition.getY();

        final double newWindowX = windowXAtClick - deltaX;
        final double newWindowY = windowYAtClick - deltaY;

        panTo(newWindowX, newWindowY);
    }
    
    private void handlePanningMouseReleased(final MouseEvent event) {
    	if (!isPanningActive()) {
            return;
        }

        setCursor(null);

        panningGestureActive = false;
    }
    
    private void handleTouch(final TouchEvent pEvent) {
		if (pEvent.getEventType() == TouchEvent.TOUCH_PRESSED) {
			currentTouchCount = pEvent.getTouchCount();
		} else {
			currentTouchCount = 0;
		}
		pEvent.consume();
	}
    
    private void handleZoom(final ZoomEvent event) {
		if (!isPanningActive() || event.getTarget() != content || currentTouchCount != 2) {
            return;
        }
        final double zoomFactor = event.getZoomFactor() > 1 ? 0.05 : -0.05;
        final double zoomFactorRatio = (scaleTransform.getX() + zoomFactor) / scaleTransform.getX();

        final double currentCenterX = event.getX();
        final double currentCenterY = event.getY();

        final double newZoomLevel =  scaleTransform.getX() + zoomFactor;
        if (newZoomLevel < 1.5 && newZoomLevel > 0.4) {
            scaleTransform.setX(newZoomLevel);
            scaleTransform.setY(newZoomLevel);
            panTo(currentCenterX * zoomFactorRatio, currentCenterY * zoomFactorRatio);
        }
        event.consume();
    }
    
    /**
     * Adds handlers to the content for panning and zooming.
     */
    private void addMouseHandlersToContent() {
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        content.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.addEventHandler(ZoomEvent.ZOOM, zoomHandler);
        content.addEventHandler(TouchEvent.TOUCH_PRESSED, touchHandler);
        content.addEventHandler(TouchEvent.TOUCH_RELEASED, touchHandler);
    }

    /**
     * Removes existing handlers from the content, if possible.
     */
    private void removeMouseHandlersFromContent() {
        content.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        content.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.removeEventHandler(ZoomEvent.ZOOM, zoomHandler);
        content.removeEventHandler(TouchEvent.TOUCH_PRESSED, touchHandler);
        content.removeEventHandler(TouchEvent.TOUCH_RELEASED, touchHandler);
    }

    /**
     * Starts panning. Should be called on mouse-pressed or when a drag event occurs without a pressed event having been
     * registered. This can happen if e.g. a context menu closes and consumes the pressed event.
     * 
     * @param x the scene-x position of the cursor
     * @param y the scene-y position of the cursor
     */
    private void startPanning(final double x, final double y) {

        setCursor(Cursor.MOVE);

        panningGestureActive = true;

        clickPosition = new Point2D(x, y);

        windowXAtClick = windowXProperty().get();
        windowYAtClick = windowYProperty().get();
    }
}