/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Group {

    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;
    private static final Color GRID_COLOR = Color.rgb(222, 248, 255);

    private GraphEditorProperties editorProperties;

    /**
     * Creates a new grid manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid() {

        // The grid should under NO circumstances interfere with (a) the layout of its parent, or (b) mouse events.
        setManaged(false);
        setMouseTransparent(true);
    }

    /**
     * Sets the editor properties object where the grid spacing is stored.
     *
     * @param editorProperties a {@link GraphEditorProperties} instance
     */
    public void setProperties(final GraphEditorProperties editorProperties) {
        this.editorProperties = editorProperties;
    }

    /**
     * Draws the grid for the given width and height.
     *
     * @param width the width of the editor region
     * @param height the height of the editor region
     */
    public void draw(final double width, final double height) {

        final double spacing = editorProperties.getGridSpacing();

        getChildren().clear();

        final int hLineCount = (int) Math.floor((height + 1) / spacing);
        final int vLineCount = (int) Math.floor((width + 1) / spacing);

        for (int i = 0; i < hLineCount; i++) {

            final Line hLine = new Line();

            hLine.setStartX(0);
            hLine.setEndX(width);
            hLine.setStartY((i + 1) * spacing + HALF_PIXEL_OFFSET);
            hLine.setEndY((i + 1) * spacing + HALF_PIXEL_OFFSET);

            hLine.setStroke(GRID_COLOR);

            getChildren().add(hLine);
        }

        for (int i = 0; i < vLineCount; i++) {

            final Line vLine = new Line();

            vLine.setStartX((i + 1) * spacing + HALF_PIXEL_OFFSET);
            vLine.setEndX((i + 1) * spacing + HALF_PIXEL_OFFSET);
            vLine.setStartY(0);
            vLine.setEndY(height);

            vLine.setStroke(GRID_COLOR);

            getChildren().add(vLine);
        }
    }
}