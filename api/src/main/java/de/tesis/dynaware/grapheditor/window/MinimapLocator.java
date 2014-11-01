/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * The minimap-representation of the currently-visible region of the graph editor.
 *
 * <p>
 * This looks like a rectangle in the minimap. It's position 'locates' the currently-visible region relative to the
 * entire content.
 * </p>
 */
public class MinimapLocator extends DraggableBox {

    private static final String STYLE_CLASS_LOCATOR = "minimap-locator";

    private final double minimapPadding;

    /**
     * Creates a new {@link MinimapLocator}.
     *
     * @param minimapPadding the padding value used by the minimap
     */
    public MinimapLocator(final double minimapPadding) {

        this.minimapPadding = minimapPadding;

        getStyleClass().addAll(STYLE_CLASS_LOCATOR);

        final GraphEditorProperties locatorProperties = new GraphEditorProperties();

        locatorProperties.setNorthBoundValue(minimapPadding);
        locatorProperties.setSouthBoundValue(minimapPadding);
        locatorProperties.setEastBoundValue(minimapPadding);
        locatorProperties.setWestBoundValue(minimapPadding);

        setEditorProperties(locatorProperties);
    }

    /**
     * Draws the locator based on the given window, content, and scale factor values.
     *
     * @param window the {@link Region} occupied by the window (i.e. the visible region)
     * @param content the {@link Region} occupied by the entire content of the editor
     * @param scaleFactor the ratio between the size of the minimap and the size of the content (between 0 and 1)
     * @param zoomFactor the factor by which the content is zoomed in (1 for no zoom)
     */
    public void draw(final Region window, final Region content, final double scaleFactor, final double zoomFactor) {

        final double x = Math.round(-content.getLayoutX() * scaleFactor / zoomFactor);
        final double y = Math.round(-content.getLayoutY() * scaleFactor / zoomFactor);
        final double width = Math.round(window.getWidth() * scaleFactor / zoomFactor);
        final double height = Math.round(window.getHeight() * scaleFactor / zoomFactor);

        setLayoutX(x + minimapPadding);
        setLayoutY(y + minimapPadding);
        setWidth(width);
        setHeight(height);
    }
}
