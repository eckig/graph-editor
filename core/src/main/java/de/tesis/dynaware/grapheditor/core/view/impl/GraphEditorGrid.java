package de.tesis.dynaware.grapheditor.core.view.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;


/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Region
{

    // This is to make the stroke be drawn 'on pixel'.
    private static final double HALF_PIXEL_OFFSET = -0.5;

    private static final String STYLE_CLASS = "graph-editor-grid";
    private static final String GRID_COLOR_SELECTOR = "-grid-color";
    private static final String GRID_COLOR_PROPERTY_NAME = "gridColor";

    private static final Color DEFAULT_GRID_COLOR = Color.rgb(222, 248, 255);

    private double mLastWidth = -1;
    private double mLastHeight = -1;
    private final Path mGrid = new Path();

    private final StyleableObjectProperty<Color> mGridColor = new StyleableObjectProperty<Color>(DEFAULT_GRID_COLOR)
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
            draw(getWidth(), getHeight());
        }

    };

    /**
     * Creates a new grid manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid()
    {
        // The grid should under NO circumstances interfere with (a) the layout
        // of its parent, or (b) mouse events.
        setManaged(false);
        setMouseTransparent(true);
        getStyleClass().add(STYLE_CLASS);
        mGrid.strokeProperty().bind(mGridColor);
        getChildren().add(mGrid);
    }

    @Override
    public void resize(double pWidth, double pHeight)
    {
        super.resize(pWidth, pHeight);

        if (mLastHeight != pHeight || mLastWidth != pWidth)
        {
            mLastHeight = pHeight;
            mLastWidth = pWidth;
            draw(pWidth, pHeight);
        }
    }

    /**
     * Draws the grid for the given width and height.
     *
     * @param pWidth
     *            the width of the editor region
     * @param pHeight
     *            the height of the editor region
     */
    void draw(final double pWidth, final double pHeight)
    {
        final double spacing = getGridSpacing();
        final int hLineCount = (int) Math.floor((pHeight + 1) / spacing);
        final int vLineCount = (int) Math.floor((pWidth + 1) / spacing);

        for (int i = 0; i < hLineCount; i++)
        {
            final double y = (i + 1) * spacing + HALF_PIXEL_OFFSET;
            mGrid.getElements().add(new MoveTo(0, y));
            mGrid.getElements().add(new LineTo(pWidth, y));
        }

        for (int i = 0; i < vLineCount; i++)
        {
            final double x = (i + 1) * spacing + HALF_PIXEL_OFFSET;
            mGrid.getElements().add(new MoveTo(x, 0));
            mGrid.getElements().add(new LineTo(x, pHeight));
        }
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

        private static final CssMetaData<GraphEditorGrid, Color> GRID_COLOR = new CssMetaData<GraphEditorGrid, Color>(GRID_COLOR_SELECTOR,
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
