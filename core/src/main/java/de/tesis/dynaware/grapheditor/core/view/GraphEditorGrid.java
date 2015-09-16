/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Pane {

    private static final String STYLE_CLASS = "graph-editor-grid";
    private static final String GRID_COLOR_SELECTOR = "-grid-color";
    private static final String GRID_COLOR_PROPERTY_NAME = "gridColor";

    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;

    private static final Color DEFAULT_GRID_COLOR = Color.rgb(222, 248, 255);

    private final Canvas canvas = new Canvas();
    private boolean needsLayout = false;
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
            return GRID_COLOR_PROPERTY_NAME;
        }

        @Override
        protected void invalidated() {
            needsLayout = true;
            requestLayout();
        }
    };

    /**
     * Creates a new grid manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid() {

        getChildren().add(canvas);
        setMouseTransparent(true);
        getStyleClass().add(STYLE_CLASS);
    }

    @Override
    protected void layoutChildren() {

        final int top = (int) snappedTopInset();
        final int right = (int) snappedRightInset();
        final int bottom = (int) snappedBottomInset();
        final int left = (int) snappedLeftInset();
        final int width = (int) getWidth() - left - right;
        final int height = (int) getHeight() - top - bottom;
        final double spacing = editorProperties == null ? GraphEditorProperties.DEFAULT_GRID_SPACING : editorProperties.getGridSpacing();

        canvas.relocate(left, top);

        if (width != canvas.getWidth() || height != canvas.getHeight() || needsLayout) {
            canvas.setWidth(width);
            canvas.setHeight(height);

            final GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, width, height);
            g.setStroke(gridColor.get());
            g.setFill(gridColor.get());

            final int hLineCount = (int) Math.floor((height + 1) / spacing);
            final int vLineCount = (int) Math.floor((width + 1) / spacing);

            for (int i = 1; i <= hLineCount; i++) {
                g.strokeLine(0, snap(i * spacing), width, snap(i * spacing));
            }

            for (int i = 1; i <= vLineCount; i++) {
                g.strokeLine(snap(i * spacing), 0, snap(i * spacing), height);
            }

            needsLayout = false;
        }
    }

    private static double snap(double y) {
        return ((int) y) + HALF_PIXEL_OFFSET;
    }

    /**
     * Sets the editor properties object where the grid spacing is stored.
     *
     * @param editorProperties a {@link GraphEditorProperties} instance
     */
    public void setProperties(final GraphEditorProperties editorProperties) {
        visibleProperty().unbind();
        this.editorProperties = editorProperties;
        if(this.editorProperties != null) {
            visibleProperty().bind(editorProperties.gridVisibleProperty());
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * Helper class for styling properties of the grid via CSS.
     *
     * <p>
     * Currently only the grid color is styled in this way.
     * </p>
     */
    private static class StyleableProperties {

        private static final CssMetaData<GraphEditorGrid, Paint> GRID_COLOR = new CssMetaData<GraphEditorGrid, Paint>(
                GRID_COLOR_SELECTOR, StyleConverter.getPaintConverter()) {

                    @Override
                    public boolean isSettable(final GraphEditorGrid node) {
                        return !node.gridColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(final GraphEditorGrid node) {
                        return node.gridColor;
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Node.getClassCssMetaData());
            styleables.add(GRID_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
