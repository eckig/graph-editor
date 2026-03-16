package io.github.eckig.grapheditor.core.view.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;


/**
 * <p>The alignment grid that appears in the background of the editor.</p>
 *
 * <p>The grid should not interfere with the layout of its parent or mouse events.</p>
 */
public class GraphEditorGrid extends Region
{

    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;

    private static final String STYLE_CLASS = "graph-editor-grid";
    private static final String GRID_COLOR_SELECTOR = "-grid-color";
    private static final String GRID_COLOR_PROPERTY_NAME = "gridColor";

    private static final Color DEFAULT_GRID_COLOR = Color.rgb(222, 248, 255);

    private boolean mNeedsLayout = false;
    private final Canvas mGrid = new Canvas();

    private final StyleableObjectProperty<Color> mGridColor = new StyleableObjectProperty<>(DEFAULT_GRID_COLOR)
    {

        @Override
        public CssMetaData<? extends Styleable, Color> getCssMetaData()
        {
            return StyleableProperties.GRID_COLOR;
        }

        @Override
        public Object getBean()
        {
            return GraphEditorGrid.this;
        }

        @Override
        public String getName()
        {
            return GRID_COLOR_PROPERTY_NAME;
        }

        @Override
        protected void invalidated()
        {
            mNeedsLayout = true;
            requestLayout();
        }
    };

    private final DoubleProperty mGridSpacing = new DoublePropertyBase(GraphEditorProperties.DEFAULT_GRID_SPACING)
    {

        @Override
        public Object getBean()
        {
            return GraphEditorGrid.this;
        }

        @Override
        public String getName()
        {
            return "gridSpacing";
        }

        @Override
        protected void invalidated()
        {
            mNeedsLayout = true;
            requestLayout();
        }

    };

    /**
     * Creates a new grid manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid()
    {
        setManaged(false);
        setMouseTransparent(true);
        getStyleClass().add(STYLE_CLASS);
        getChildren().add(mGrid);
    }

    @Override
    protected void layoutChildren()
    {
        final var top = (int) snappedTopInset();
        final var right = (int) snappedRightInset();
        final var bottom = (int) snappedBottomInset();
        final var left = (int) snappedLeftInset();
        final var width = (int) getWidth() - left - right;
        final var height = (int) getHeight() - top - bottom;
        final var spacing = getGridSpacing();

        mGrid.relocate(left, top);

        if (width != mGrid.getWidth() || height != mGrid.getHeight() || mNeedsLayout)
        {
            mGrid.setWidth(width);
            mGrid.setHeight(height);

            final var g = mGrid.getGraphicsContext2D();
            g.clearRect(0, 0, width, height);
            g.setStroke(mGridColor.get());
            g.setFill(mGridColor.get());

            final var hLineCount = (int) Math.floor((height + 1) / spacing);
            final var vLineCount = (int) Math.floor((width + 1) / spacing);

            for (int i = 1; i <= hLineCount; i++)
            {
                g.strokeLine(0, snap(i * spacing), width, snap(i * spacing));
            }

            for (int i = 1; i <= vLineCount; i++)
            {
                g.strokeLine(snap(i * spacing), 0, snap(i * spacing), height);
            }

            mNeedsLayout = false;
        }
    }

    private static double snap(double y)
    {
        return ((int) y) + HALF_PIXEL_OFFSET;
    }

    /**
     * Gets the current grid spacing in pixels.
     *
     * @return the current grid spacing
     */
    public double getGridSpacing()
    {
        return mGridSpacing.get();
    }

    /**
     * Sets the grid spacing to be used if the grid is visible and/or
     * snap-to-grid is enabled.
     *
     * <p>
     * Integer values are recommended to avoid sub-pixel positioning effects.
     * </p>
     *
     * @param gridSpacing
     *            the grid spacing to be used
     */
    public void setGridSpacing(final double gridSpacing)
    {
        mGridSpacing.set(gridSpacing);
    }

    /**
     * Gets the grid spacing property.
     *
     * @return the grid spacing {@link DoubleProperty}.
     */
    public DoubleProperty gridSpacingProperty()
    {
        return mGridSpacing;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
    {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * Helper class for styling properties of the grid via CSS.
     *
     * <p>
     * Currently only the grid color is styled in this way.
     * </p>
     */
    private static class StyleableProperties
    {

        private static final CssMetaData<GraphEditorGrid, Color> GRID_COLOR = new CssMetaData<>(GRID_COLOR_SELECTOR,
                StyleConverter.getColorConverter())
        {

            @Override
            public boolean isSettable(final GraphEditorGrid node)
            {
                return !node.mGridColor.isBound();
            }

            @Override
            public StyleableProperty<Color> getStyleableProperty(final GraphEditorGrid node)
            {
                return node.mGridColor;
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static
        {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Node.getClassCssMetaData());
            styleables.add(GRID_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
