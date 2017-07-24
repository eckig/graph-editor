/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
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
    private double contentX, contentY;

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
    private final AtomicInteger currentTouchCount = new AtomicInteger();
    private final Scale scaleTransform = new Scale(1, 1, 0, 0);

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow() {

        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }
    
    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        
        if(content != null) {
            content.relocate(contentX * -1, contentY * -1);
        }
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

        contentX = checkContentX(x);
        contentY = checkContentY(y);
        requestLayout();
    }
    
    /**
     * Pans the window to the specified x coordinate.
     *
     * <p>
     * The window cannot be panned outside the content. When the window 'hits the edge' of the content it will stop.
     * </p>
     *
     * @param x the x position of the window relative to the top-left corner of the content
     */
    public void panToX(final double x) {

        contentX = checkContentX(x);
        requestLayout();
    }
    
    /**
     * Pans the window to the specified x coordinate.
     *
     * <p>
     * The window cannot be panned outside the content. When the window 'hits the edge' of the content it will stop.
     * </p>
     *
     * @param y the y position of the window relative to the top-left corner of the content
     */
    public void panToY(final double y) {

        contentY = checkContentY(y);
        requestLayout();
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
            panTo((content.getWidth() - getWidth()) / 2, 0);
            break;

        case CENTER:
            panTo((content.getWidth() - getWidth()) / 2, (content.getHeight() - getHeight()) / 2);
            break;
            
        }
    }

    /**
     * @return the x coordinate of the window relative to the top-left corner of the content.
     */
    public double getContentX() {
        return contentX;
    }
    
    /**
     * @return the y coordinate of the window relative to the top-left corner of the content.
     */
    public double getContentY() {
        return contentY;
    }

    /**
     * Checks that the window bounds are completely inside the content bounds, and repositions if necessary.
     *
     * <p>
     * Also checks that the window position values are integers to ensure strokes are drawn cleanly.
     * </p>
     */
    protected void checkWindowBounds() {
        panTo(checkContentX(getContentX()), checkContentY(getContentY()));
    }
    
    private double checkContentX(final double xToCheck) {

        double x = Math.max(xToCheck, 0);
        if (content != null) {
            final double zoomFactor = content.getLocalToSceneTransform().getMxx();
            final double maxX = zoomFactor * content.getWidth() - getWidth();
            x = x > maxX ? maxX : x;
        }
        return snapPosition(x);
    }
    
    private double checkContentY(final double yToCheck) {

        double y = Math.max(yToCheck, 0);
        if (content != null) {
            final double zoomFactor = content.getLocalToSceneTransform().getMxx();
            final double maxY = zoomFactor * content.getHeight() - getHeight();
            y = y > maxY ? maxY : y;
        }
        return snapPosition(y);
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
            this.content.getTransforms().remove(scaleTransform);
        }

        this.content = content;

        if (content != null) {

            content.setManaged(false);
            getChildren().add(content);
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
    
    protected boolean isMultiTouchActive() {
        return currentTouchCount.get() > 1;
    }

    private void handlePanningMousePressed(final MouseEvent event) {
    	if (isPanningActive() && currentTouchCount.get() < 2) {
        	startPanning(event.getSceneX(), event.getSceneY());
        }
    }
    
    private void handlePanningMouseDragged(final MouseEvent event) {
    	if (!isPanningActive() || isMultiTouchActive()) {
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
        final int current = currentTouchCount.get();
		if (pEvent.getEventType() == TouchEvent.TOUCH_PRESSED) {
		    currentTouchCount.compareAndSet(current, pEvent.getTouchCount());
		} else {
		    Platform.runLater(() -> currentTouchCount.compareAndSet(current, 0));
		}
		pEvent.consume();
	}
    
    private void handleZoom(final ZoomEvent event) {
        if (!isPanningActive() || currentTouchCount.get() != 2) {
            return;
        }
        final double zoomFactor = event.getZoomFactor() > 1 ? 1.01 : 0.99;
        final double newZoomLevel = scaleTransform.getX() * zoomFactor;

        final double currentX = content.getLayoutX();
        final double currentY = content.getLayoutY();
        final double diffX = event.getX() - currentX;
        final double diffY = event.getY() - currentY;
        final double newX = event.getX() - (diffX * zoomFactor);
        final double newY = event.getY() - (diffY * zoomFactor);

        if (newZoomLevel < 1.5 && newZoomLevel > 0.4) {
            scaleTransform.setX(newZoomLevel);
            scaleTransform.setY(newZoomLevel);
            panTo(newX, newY);
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

        windowXAtClick = getContentX();
        windowYAtClick = getContentY();
    }
}