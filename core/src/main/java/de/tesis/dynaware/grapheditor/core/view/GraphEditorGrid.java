/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * The alignment grid that appears in the background of the editor.
 */
public class GraphEditorGrid extends Region {

    private static final String STYLE_CLASS = "graph-editor-grid";
    private static final String GRID_COLOR_SELECTOR = "-grid-color";
    private static final String GRID_COLOR_PROPERTY_NAME = "gridColor";

    private static final Color DEFAULT_GRID_COLOR = Color.rgb(222, 248, 255);

    private GraphEditorProperties editorProperties;

    private final StyleableObjectProperty<Color> gridColor = new StyleableObjectProperty<Color>(DEFAULT_GRID_COLOR) {

        @Override
        public CssMetaData<? extends Styleable, Color> getCssMetaData() {
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
        	updateGrid();
        }
    };

    /**
     * Creates a new grid manager. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     */
    public GraphEditorGrid() {

        setMouseTransparent(true);
        getStyleClass().add(STYLE_CLASS);
        updateGrid();
    }
    
	private void updateGrid() {
		final Color color = gridColor.get();
		final double space = editorProperties == null ? GraphEditorProperties.DEFAULT_GRID_SPACING
				: editorProperties.getGridSpacing();
		final LinearGradient lgH = new LinearGradient(0, 0, 0, space, false, CycleMethod.REPEAT, new Stop(0, color),
				new Stop(0.0625, color), new Stop(0.0625, Color.TRANSPARENT), new Stop(1, Color.TRANSPARENT));
		final LinearGradient lgV = new LinearGradient(0, 0, space, 0, false, CycleMethod.REPEAT, new Stop(0, color),
				new Stop(0.0625, color), new Stop(0.0625, Color.TRANSPARENT), new Stop(1, Color.TRANSPARENT));
		setBackground(new Background(new BackgroundFill(lgH, CornerRadii.EMPTY, Insets.EMPTY),
				new BackgroundFill(lgV, CornerRadii.EMPTY, Insets.EMPTY)));
	}

    @Override
    protected void layoutChildren() {
    	// empty
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

		private static final CssMetaData<GraphEditorGrid, Color> GRID_COLOR = new CssMetaData<GraphEditorGrid, Color>(
				GRID_COLOR_SELECTOR, StyleConverter.getColorConverter()) {

			@Override
			public boolean isSettable(final GraphEditorGrid node) {
				return !node.gridColor.isBound();
			}

			@Override
			public StyleableProperty<Color> getStyleableProperty(final GraphEditorGrid node) {
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
