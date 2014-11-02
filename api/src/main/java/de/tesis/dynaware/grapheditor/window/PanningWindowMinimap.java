/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Transform;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;

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

    private MinimapContentRepresentation contentRepresentation;

    private PanningWindow window;
    private Region content;

    private ChangeListener<Transform> zoomListener;

    private ChangeListener<Number> drawListener;
    private ChangeListener<Number> drawLocatorListener;

    private boolean locatorPositionListenersMuted;
    private boolean drawLocatorListenerMuted;

    /**
     * Creates a new {@link PanningWindowMinimap} instance.
     */
    public PanningWindowMinimap() {

        getStylesheets().add(PanningWindowMinimap.class.getResource(STYLESHEET).toExternalForm());
        getStyleClass().add(STYLE_CLASS);

        setPickOnBounds(false);

        createZoomListener();
        createVisibilityChangeListener();
        createDrawListener();
        createDrawLocatorListener();
        createLocatorPositionListeners();
        createMinimapClickHandlers();
        createContentCacheHandlersForLocator();

        getChildren().addAll(locator);
    }

    /**
     * Sets the content representation to be displayed in this minimap.
     *
     * @param contentRepresentation a {@link MinimapContentRepresentation} to be displayed
     */
    public void setContentRepresentation(final MinimapContentRepresentation contentRepresentation) {

        if (this.contentRepresentation != null) {
            getChildren().remove(this.contentRepresentation);
        }

        this.contentRepresentation = contentRepresentation;

        if (contentRepresentation != null) {

            contentRepresentation.setLayoutX(MINIMAP_PADDING);
            contentRepresentation.setLayoutY(MINIMAP_PADDING);

            getChildren().add(contentRepresentation);
            locator.toFront();
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
            this.window.widthProperty().removeListener(drawLocatorListener);
            this.window.heightProperty().removeListener(drawLocatorListener);
        }

        this.window = window;

        if (this.window != null) {
            window.widthProperty().addListener(drawLocatorListener);
            window.heightProperty().addListener(drawLocatorListener);
        }

        if (isVisible()) {
            drawLocator();
        }
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
            this.content.layoutXProperty().removeListener(drawLocatorListener);
            this.content.layoutYProperty().removeListener(drawLocatorListener);
            this.content.widthProperty().removeListener(drawListener);
            this.content.heightProperty().removeListener(drawListener);
            this.content.localToSceneTransformProperty().removeListener(zoomListener);
        }

        this.content = content;

        if (content != null) {
            content.widthProperty().addListener(drawListener);
            content.heightProperty().addListener(drawListener);
            content.layoutXProperty().addListener(drawLocatorListener);
            content.layoutYProperty().addListener(drawLocatorListener);
            content.localToSceneTransformProperty().addListener(zoomListener);
        } else if (contentRepresentation != null) {
            contentRepresentation.getChildren().clear();
        }

        if (isVisible()) {
            drawLocator();
        }
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
    protected double calculateScaleFactor() {

        final double scaleFactorX = (getWidth() - 2 * MINIMAP_PADDING) / content.getWidth();
        final double scaleFactorY = (getHeight() - 2 * MINIMAP_PADDING) / content.getHeight();

        // The scale factors should be the same but take the smallest just in case, so that everything fits.
        return Math.min(scaleFactorX, scaleFactorY);
    }

    /**
     * Creates a listener to redraw the locator if the zoom-value changes.
     */
    private void createZoomListener() {
        zoomListener = (observable, oldValue, newValue) -> {
            drawLocator();
        };
    }

    /**
     * Creates a change listener that will listen to changes in the visibility of the minimap.
     *
     * <p>
     * This is necessary because we don't redraw the minimap if it is not visible (for performance reasons). Therefore,
     * when the user shows the minimap we make a redraw call to make sure things are up-to-date.
     * </p>
     */
    private void createVisibilityChangeListener() {

        visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                drawAll();
            }
        });
    }

    /**
     * Creates a change listener that will redraw everything.
     *
     * <p>
     * This listener is intended to be fired when the size of the content changes. In this case we need to redraw the
     * model and everything else. This is slow, but should not happen very often.
     * </p>
     */
    private void createDrawListener() {
        drawListener = (observable, oldValue, newValue) -> {
            if (isVisible()) {
                drawAll();
            }
        };
    }

    /**
     * Creates a change listener that will only redraw the locator.
     *
     * <p>
     * This listener is intended to be fired when the size or panning X & Y of the panning window changes. Just
     * redrawing the locator is fast, so it's fine if this happens frequently.
     * </p>
     */
    private void createDrawLocatorListener() {

        drawLocatorListener = (observable, oldValue, newValue) -> {
            if (isVisible()) {
                drawLocator();
            }
        };

        widthProperty().addListener(drawLocatorListener);
        heightProperty().addListener(drawLocatorListener);
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

        locator.layoutXProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {

            if (!locatorPositionListenersMuted && checkContentExists() && checkWindowExists()) {

                drawLocatorListenerMuted = true;

                final double effectiveScaleFactor = calculateScaleFactor() / calculateZoomFactor();
                final double targetX = ((Double) newValue - MINIMAP_PADDING) / effectiveScaleFactor;
                window.panTo(targetX, window.windowYProperty().get());

                drawLocatorListenerMuted = false;
            }
        });

        locator.layoutYProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {

            if (!locatorPositionListenersMuted && checkContentExists() && checkWindowExists()) {

                drawLocatorListenerMuted = true;

                final double effectiveScaleFactor = calculateScaleFactor() / calculateZoomFactor();
                final double targetY = ((Double) newValue - MINIMAP_PADDING) / effectiveScaleFactor;

                window.panTo(window.windowXProperty().get(), targetY);

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

            if (window.isCacheWhilePanning()) {
                content.setCache(true);
            }

            final double x = event.getX() - MINIMAP_PADDING - locator.getWidth() / 2;
            final double y = event.getY() - MINIMAP_PADDING - locator.getHeight() / 2;

            final double scaleFactor = calculateScaleFactor();
            final double zoomFactor = calculateZoomFactor();

            window.panTo(x / scaleFactor * zoomFactor, y / scaleFactor * zoomFactor);

            locator.fireEvent(event);
        });

        setOnMouseDragged(event -> {
            if (checkReadyForClickEvent(event)) {
                locator.fireEvent(event);
            }
        });

        setOnMouseReleased(event -> {
            if (checkReadyForClickEvent(event) && window.isCacheWhilePanning()) {
                content.setCache(false);
            }
        });
    }

    /**
     * Creates handlers to set the content cache when the locator is pressed / released.
     *
     * <p>
     * Extends the existing pressed and released handlers of the {@link DraggableBox}.
     * </p>
     */
    private void createContentCacheHandlersForLocator() {

        final EventHandler<? super MouseEvent> existingPressedHandler = locator.getOnMousePressed();
        final EventHandler<? super MouseEvent> existingReleasedHandler = locator.getOnMouseReleased();

        locator.setOnMousePressed(event -> {

            if (checkReadyForClickEvent(event) && window.isCacheWhilePanning()) {
                content.setCache(true);
            }

            if (existingPressedHandler != null) {
                existingPressedHandler.handle(event);
            }
        });

        locator.setOnMouseReleased(event -> {

            if (checkReadyForClickEvent(event) && window.isCacheWhilePanning()) {
                content.setCache(false);
            }

            if (existingReleasedHandler != null) {
                existingReleasedHandler.handle(event);
            }
        });
    }

    /**
     * Redraws everything in the minimap.
     */
    private void drawAll() {
        drawContentRepresentation();
        drawLocator();
    }

    /**
     * Redraws the content representation in the minimap.
     */
    private void drawContentRepresentation() {
        if (checkContentExists() && checkWindowExists() && contentRepresentation != null) {
            contentRepresentation.draw(calculateScaleFactor());
        }
    }

    /**
     * Redraws the minimap locator.
     */
    private void drawLocator() {

        if (checkContentExists() && checkWindowExists() && !drawLocatorListenerMuted) {
            locatorPositionListenersMuted = true;
            locator.draw(window, content, calculateScaleFactor(), calculateZoomFactor());
            locatorPositionListenersMuted = false;
        }
    }

    /**
     * Calculates how much the content is zoomed in by.
     *
     * @return the zoom factor of the content (1 for no zoom)
     */
    private double calculateZoomFactor() {
        return content.getLocalToSceneTransform().getMxx();
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
