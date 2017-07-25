/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import de.tesis.dynaware.grapheditor.utils.GraphInputGesture;
import de.tesis.dynaware.grapheditor.utils.GraphInputMode;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
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
    
    private final ChangeListener<GraphInputMode> inputModeListener = (w,o,n) -> setPanningActive(n == GraphInputMode.NAVIGATION);

    private Point2D clickPosition;
    private Point2D windowPosAtClick;
    
    private boolean panningGestureActive;
    private boolean panningActive = true;
    
    private final EventHandler<ZoomEvent> zoomHandler = this::handleZoom;
    private final Scale scaleTransform = new Scale(1, 1, 0, 0);
    
    private GraphEditorProperties editorProperties;

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow() {

        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
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
        
        if(this.editorProperties != null) {
            this.editorProperties.getGraphEventManager().inputModeProperty().removeListener(inputModeListener);
        }
        this.editorProperties = editorProperties;
        if(editorProperties != null) {
            editorProperties.getGraphEventManager().inputModeProperty().addListener(inputModeListener);
            setPanningActive(editorProperties.getGraphEventManager().getInputMode() == GraphInputMode.NAVIGATION);
        }
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
    
    private void setPanningActive(boolean panningActive) {
		this.panningActive = panningActive;
	}
    
    boolean isPanningActive() {
		return panningActive;
	}
    
    private void handlePanningMousePressed(final MouseEvent event) {
        
        if (!isPanningActive() || editorProperties == null
                || !editorProperties.getGraphEventManager().isInputGestureActiveOrInactive(GraphInputGesture.PAN)) {
            return;
        }
    	startPanning(event.getSceneX(), event.getSceneY());
    }
    
    private void handlePanningMouseDragged(final MouseEvent event) {
        if (!isPanningActive() || editorProperties == null
                || !editorProperties.getGraphEventManager().isInputGestureActiveOrInactive(GraphInputGesture.PAN)) {
            return;
        }

        if (!panningGestureActive) {
            startPanning(event.getSceneX(), event.getSceneY());
        }

        final Point2D currentPosition = new Point2D(event.getSceneX(), event.getSceneY());

        final double deltaX = currentPosition.getX() - clickPosition.getX();
        final double deltaY = currentPosition.getY() - clickPosition.getY();

        final double newWindowX = windowPosAtClick.getX() - deltaX;
        final double newWindowY = windowPosAtClick.getY() - deltaY;

        panTo(newWindowX, newWindowY);
    }
    
    private void handlePanningMouseReleased(final MouseEvent event) {
    	if (!isPanningActive()) {
            return;
        }

        setCursor(null);

        editorProperties.getGraphEventManager().setInputGesture(null);
        panningGestureActive = false;
    }
    
    private void handleZoom(final ZoomEvent event) {
        if (!isPanningActive() || editorProperties == null
                || !editorProperties.getGraphEventManager().isInputGestureActiveOrInactive(GraphInputGesture.ZOOM)) {
            return;
        }
        
        event.consume();
        if(event.getEventType() == ZoomEvent.ZOOM_STARTED) {
            editorProperties.getGraphEventManager().setInputGesture(GraphInputGesture.ZOOM);
            return;
        }
        else if(event.getEventType() == ZoomEvent.ZOOM_STARTED) {
            editorProperties.getGraphEventManager().setInputGesture(null);
            return;
        }

        final double zoomFactor = event.getZoomFactor() > 1 ? 1.02 : 0.98;
        final double newZoomLevel = scaleTransform.getX() * zoomFactor;

        final double currentX = getContentX();
        final double currentY = getContentY();
        final double diffX = event.getX();
        final double diffY = event.getY();
        final double newX = currentX + diffX - (diffX / zoomFactor);
        final double newY = currentY + diffY - (diffY / zoomFactor);

        
        if (newZoomLevel < 1.5 && newZoomLevel > 0.4) {
            scaleTransform.setX(newZoomLevel);
            scaleTransform.setY(newZoomLevel);
            panTo(newX, newY);
        }
    }
    
    /**
     * Adds handlers to the content for panning and zooming.
     */
    private void addMouseHandlersToContent() {
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        content.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.addEventHandler(ZoomEvent.ANY, zoomHandler);
    }

    /**
     * Removes existing handlers from the content, if possible.
     */
    private void removeMouseHandlersFromContent() {
        content.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        content.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        content.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        content.removeEventHandler(ZoomEvent.ANY, zoomHandler);
    }

    /**
     * Starts panning. Should be called on mouse-pressed or when a drag event occurs without a pressed event having been
     * registered. This can happen if e.g. a context menu closes and consumes the pressed event.
     * 
     * @param x the scene-x position of the cursor
     * @param y the scene-y position of the cursor
     */
    private void startPanning(final double x, final double y) {

        editorProperties.getGraphEventManager().setInputGesture(GraphInputGesture.PAN);
        setCursor(Cursor.MOVE);

        panningGestureActive = true;

        clickPosition = new Point2D(x, y);
        windowPosAtClick = new Point2D(getContentX(), getContentY());
    }
}