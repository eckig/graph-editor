/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.scene.control.Hyperlink;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * A minimap that displays the current position of a {@link PanningWindow} relative to its content.
 *
 * <p>
 * Also provides mechanisms for navigating the window to other parts of the content by clicking or dragging.
 * </p>
 */
public class PanningWindowMinimap extends Pane {

    protected static final double MINIMAP_PADDING = 5;

    private static final String STYLESHEET = "minimap.css";
    private static final String STYLE_CLASS = "minimap";

    private final MinimapLocator locator = new MinimapLocator(MINIMAP_PADDING);

    private MinimapNodeGroup contentRepresentation;

    private PanningWindow window;
    private Region content;

    private final InvalidationListener drawListener = observable -> requestLayout();

    private double scaleFactor = 0.75;
    private boolean locatorPositionListenersMuted;
    private boolean drawLocatorListenerMuted;
    
    private final Hyperlink zoomIn = new Hyperlink("++");
    private final Hyperlink zoomOut = new Hyperlink("--");
    private final Hyperlink zoomExact = new Hyperlink("1:1");

    /**
     * Creates a new {@link PanningWindowMinimap} instance.
     */
    public PanningWindowMinimap() {

        getStylesheets().add(PanningWindowMinimap.class.getResource(STYLESHEET).toExternalForm());
        getStyleClass().add(STYLE_CLASS);

        setPickOnBounds(false);

        createLocatorPositionListeners();
        createMinimapClickHandlers();

        getChildren().add(locator);
        
        zoomOut.getStyleClass().addAll("zoom", "zoom-out");
        zoomIn.getStyleClass().addAll("zoom", "zoom-in");
        zoomExact.getStyleClass().addAll("zoom", "zoom-exact");
        
        zoomOut.setOnAction(this::zoomOut);
        zoomIn.setOnAction(this::zoomIn);
        zoomExact.setOnAction(this::zoomExact);
        
        getChildren().addAll(zoomIn, zoomOut, zoomExact);
    }
    
    private void zoomIn(final ActionEvent event) {
        if(window != null) {
            window.setZoom(window.getZoom() + 0.15);
        }
        event.consume();
    }

    private void zoomExact(final ActionEvent event) {
        if(window != null) {
            window.setZoom(1);
        }
        event.consume();
    }

    private void zoomOut(final ActionEvent event) {
        if(window != null) {
            window.setZoom(window.getZoom() - 0.15);
        }
        event.consume();
    }
    
    /**
     * Sets the content representation to be displayed in this minimap.
     *
     * @param contentRepresentation a {@link MinimapContentRepresentation} to be displayed
     */
    public void setContentRepresentation(final MinimapNodeGroup contentRepresentation) {

        if (this.contentRepresentation != null) {
            getChildren().remove(this.contentRepresentation);
        }

        this.contentRepresentation = contentRepresentation;

        if (contentRepresentation != null) {
            getChildren().add(0, contentRepresentation);
        }
    }

    /**
     * Sets the {@link PanningWindow} that this minimap is representing.
     *
     * <p>
     * This window will be visualised inside the minimap as a a rectangular shape, showing the user the current position
     * of the window over its content.
     * <p>
     *
     * @param window a {@link PanningWindow} instance
     */
    public void setWindow(final PanningWindow window) {

        if (this.window != null) {
            this.window.widthProperty().removeListener(drawListener);
            this.window.heightProperty().removeListener(drawListener);
        }

        this.window = window;

        if (this.window != null) {
            window.widthProperty().addListener(drawListener);
            window.heightProperty().addListener(drawListener);
        }

        requestLayout();
    }

    /**
     * Sets the content that this minimap is representing.
     *
     * <p>
     * For sensible behaviour, this instance should be the same as the content inside the {@link PanningWindow}.
     * </p>
     *
     * @param content a {@link Region} containing some content to be visualised in the minimap
     */
    public void setContent(final Region content) {

        if (this.content != null) {
            this.content.layoutXProperty().removeListener(drawListener);
            this.content.layoutYProperty().removeListener(drawListener);
            this.content.widthProperty().removeListener(drawListener);
            this.content.heightProperty().removeListener(drawListener);
            this.content.localToSceneTransformProperty().removeListener(drawListener);
        }

        this.content = content;

        if (content != null) {
            content.widthProperty().addListener(drawListener);
            content.heightProperty().addListener(drawListener);
            content.layoutXProperty().addListener(drawListener);
            content.layoutYProperty().addListener(drawListener);
            content.localToSceneTransformProperty().addListener(drawListener);
        }

        requestLayout();
    }
    
    /**
     * @return content a {@link Region} containing some content to be visualised
     *         in the minimap
     */
    public Region getContent() {
        return content;
    }

    /**
     * @return the scale factor that indicates how much smaller the minimap is
     *         than the content it is representing.
     */
    protected final double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Calculates the scale factor that indicates how much smaller the minimap is than the content it is representing.
     *
     * <p>
     * This number should be greater than 0 and probably much less than 1.
     * </p>
     *
     * @return the ratio of the minimap size to the content size
     */
    private double calculateScaleFactor() {

        final double scaleFactorX = (getWidth() - 2 * MINIMAP_PADDING) / content.getWidth();
        final double scaleFactorY = (getHeight() - 2 * MINIMAP_PADDING) / content.getHeight();

        // The scale factors should be the same but take the smallest just in case, so that everything fits.
        return Math.min(scaleFactorX, scaleFactorY);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        
        scaleFactor = calculateScaleFactor();
        
        final double width = getWidth();
        final double height = getHeight();
        
        if (checkContentExists() && checkWindowExists() && contentRepresentation != null) {
            contentRepresentation.relocate(MINIMAP_PADDING, MINIMAP_PADDING);
            contentRepresentation.setScaleFactor(scaleFactor);
            contentRepresentation.resize(width - MINIMAP_PADDING * 2, height - MINIMAP_PADDING * 2);
        }
        
        final double maxLocWidth = width - MINIMAP_PADDING * 2;
        final double maxLocHeight = height - MINIMAP_PADDING * 2;
        
        if(!drawLocatorListenerMuted) {
            locatorPositionListenersMuted = true;
            
            final double zoomFactor = calculateZoomFactor();
            
            
            final double x = Math.round(-content.getLayoutX() * scaleFactor / zoomFactor);
            final double y = Math.round(-content.getLayoutY() * scaleFactor / zoomFactor);
            final double locWidth = Math.min(maxLocWidth, Math.round(window.getWidth() * scaleFactor / zoomFactor));
            final double locHeight = Math.min(maxLocHeight, Math.round(window.getHeight() * scaleFactor / zoomFactor));

            locator.resizeRelocate(x + MINIMAP_PADDING, y + MINIMAP_PADDING, locWidth, locHeight);
            locatorPositionListenersMuted = false;
        }
        
        zoomOut.relocate(MINIMAP_PADDING, height - zoomOut.getHeight());
        zoomIn.relocate(maxLocWidth - zoomIn.getWidth(), height - zoomOut.getHeight());
        zoomExact.relocate(maxLocWidth / 2 - zoomExact.getWidth() / 2, height - zoomOut.getHeight());
    }

    /**
     * Creates a change listener to react to changes in the position of the locator.
     *
     * <p>
     * The job of this listener is to update the panning X & Y values of the panning window when the user drags the
     * locator around in the minimap.
     * </p>
     *
     * <p>
     * Before we pan the window, we mute the listener that redraws the locator, because otherwise we could have an
     * infinite cycle of listeners firing each other.
     * </p>
     */
    private void createLocatorPositionListeners() {

        locator.layoutXProperty().addListener((observable, oldValue, newValue) -> {

            if (!locatorPositionListenersMuted && checkContentExists() && checkWindowExists()) {

                drawLocatorListenerMuted = true;
                final double effectiveScaleFactor = scaleFactor / calculateZoomFactor();
                final double targetX = (newValue.doubleValue() - MINIMAP_PADDING) / effectiveScaleFactor;
                window.panToX(targetX);
                drawLocatorListenerMuted = false;
            }
        });

        locator.layoutYProperty().addListener((observable, oldValue, newValue) -> {

            if (!locatorPositionListenersMuted && checkContentExists() && checkWindowExists()) {

                drawLocatorListenerMuted = true;
                final double effectiveScaleFactor = scaleFactor / calculateZoomFactor();
                final double targetY = (newValue.doubleValue() - MINIMAP_PADDING) / effectiveScaleFactor;
                window.panToY(targetY);
                drawLocatorListenerMuted = false;
            }
        });
    }

    /**
     * Creates and sets a mouse-pressed handler to pan appropriately when the user clicks on the minimap.
     *
     * <p>
     * The mouse-dragged event is also passed on to the locator so it can be dragged as part of the same gesture.
     * </p>
     */
    private void createMinimapClickHandlers() {

        setOnMousePressed(event -> {

            if (!checkReadyForClickEvent(event)) {
                return;
            }

            final double x = event.getX() - MINIMAP_PADDING - locator.getWidth() / 2;
            final double y = event.getY() - MINIMAP_PADDING - locator.getHeight() / 2;

            final double zoomFactor = calculateZoomFactor();

            window.panTo(x / scaleFactor * zoomFactor, y / scaleFactor * zoomFactor);
        });
    }

    /**
     * Calculates how much the content is zoomed in by.
     *
     * @return the zoom factor of the content (1 for no zoom)
     */
    private double calculateZoomFactor() {
        return content == null ? 1 : content.getLocalToSceneTransform().getMxx();
    }

    /**
     * Checks that everything is initialized and ready for the given mouse event.
     *
     * @param event a mouse event
     * @return {@code true} if conditions are right for the drag event
     */
    private boolean checkReadyForClickEvent(final MouseEvent event) {
        return event.getButton().equals(MouseButton.PRIMARY) && checkContentExists() && checkWindowExists();
    }

    /**
     * Checks that the content is not null and has been drawn, i.e. has a nonzero width and height.
     *
     * @return {@code true} if the content is not null and has a nonzero width & height
     */
    private boolean checkContentExists() {
        return content != null && content.getWidth() > 0 && content.getHeight() > 0;
    }

    /**
     * Checks that the window is not null and has been drawn, i.e. has a nonzero width and height.
     *
     * @return {@code true} if the window is not null and has a nonzero width & height
     */
    private boolean checkWindowExists() {
        return window != null && window.getWidth() > 0 && window.getHeight() > 0;
    }
}
