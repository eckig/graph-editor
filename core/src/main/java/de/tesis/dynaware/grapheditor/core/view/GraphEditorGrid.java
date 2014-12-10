/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import com.sun.javafx.css.converters.PaintConverter;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.paint.Paint;

/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Group {

    private static final String GRID_STYLE_CLASS = "graph-grid";
    
    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;
    
    private static final Color DEFAULT_GRID_COLOR = Color.rgb(222, 248, 255);

    private GraphEditorProperties editorProperties;

    private final StyleableObjectProperty<Paint> gridColor = new StyleableObjectProperty<Paint>(DEFAULT_GRID_COLOR) {

        @Override
        public CssMetaData<? extends Styleable, Paint> getCssMetaData() {
            return StyleableProperties.GRID_COLOR;
        }

        @Override
        public Object getBean() {
            return GraphEditorGrid.this;
        }

        @Override
        public String getName() {
            return "gridColor";
        }

        @Override
        protected void invalidated() {
        }
    };
    
    /**
     * Creates a new grid manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid() {

        // The grid should under NO circumstances interfere with (a) the layout of its parent, or (b) mouse events.
        setManaged(false);
        setMouseTransparent(true);
        getStyleClass().add(GRID_STYLE_CLASS);
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
            hLine.strokeProperty().bind(gridColor);

            getChildren().add(hLine);
        }

        for (int i = 0; i < vLineCount; i++) {

            final Line vLine = new Line();

            vLine.setStartX((i + 1) * spacing + HALF_PIXEL_OFFSET);
            vLine.setEndX((i + 1) * spacing + HALF_PIXEL_OFFSET);
            vLine.setStartY(0);
            vLine.setEndY(height);
            vLine.strokeProperty().bind(gridColor);

            getChildren().add(vLine);
        }
    }
    
    /**
     * @return The CssMetaData associated with this class, including the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    private static class StyleableProperties {

        private static final CssMetaData<GraphEditorGrid, Paint> GRID_COLOR = new CssMetaData<GraphEditorGrid, Paint>(
            "-graph-grid-color", PaintConverter.getInstance()) {

            @Override
            public boolean isSettable(GraphEditorGrid node) {
                return !node.gridColor.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(GraphEditorGrid node) {
                return node.gridColor;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Group.getClassCssMetaData());
            styleables.add(GRID_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}