/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import javafx.collections.ObservableList;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * The {@link Region} that all visual elements in the graph editor are added to.
 *
 * <p>
 * There is one instance of this class per {@link DefaultGraphEditor}. It is the outermost JavaFX node of the editor.
 * </p>
 *
 * <p>
 * The view currently has two layers - a <b>node</b> layer and a <b>connection</b> layer. The node layer is in front.
 * Graph nodes are added to the node layer, while connections, joints, and tails are added to the connection layer. This
 * means nodes will always be in front of connections.
 * </p>
 *
 * <p>
 * Calling toFront() or toBack() on the associated JavaFX nodes will just reposition them inside their layer. The layers
 * always have the same dimensions as the editor region itself.
 * </p>
 */
public class GraphEditorView extends Region {

    private static final String STYLESHEET_VIEW = "view.css";
    private static final String STYLESHEET_DEFAULTS = "defaults.css";

    private static final String STYLE_CLASS = "graph-editor";
    private static final String STYLE_CLASS_NODE_LAYER = "graph-editor-node-layer";
    private static final String STYLE_CLASS_CONNECTION_LAYER = "graph-editor-connection-layer";

    private static final String NODE_LAYER_ID = "nodeLayer";
    private static final String CONNECTION_LAYER_ID = "connectionLayer";

    private final GraphEditorViewLayer nodeLayer = new GraphEditorViewLayer();
    private final GraphEditorViewLayer connectionLayer = new GraphEditorViewLayer();

    private final GraphEditorGrid grid = new GraphEditorGrid();
    private ConnectionLayouter connectionLayouter;

    private final SelectionBox selectionBox = new SelectionBox();

    private GraphEditorProperties editorProperties;

    /**
     * Creates a new {@link GraphEditorView} to which skin instances can be added and removed.
     */
    public GraphEditorView() {

        getStylesheets().add(GraphEditorView.class.getResource(STYLESHEET_VIEW).toExternalForm());
        getStylesheets().add(GraphEditorView.class.getResource(STYLESHEET_DEFAULTS).toExternalForm());

        getStyleClass().addAll(STYLE_CLASS);

        setMaxWidth(GraphEditorProperties.DEFAULT_MAX_WIDTH);
        setMaxHeight(GraphEditorProperties.DEFAULT_MAX_HEIGHT);

        initializeLayers();
        initializeGrid();

        getChildren().add(selectionBox);
    }

    /**
     * Sets the connection-layouter to be used by the view.
     *
     * @param connectionLayouter the graph editor's {@link ConnectionLayouter} instance
     */
    public void setConnectionLayouter(final ConnectionLayouter connectionLayouter) {
        this.connectionLayouter = connectionLayouter;
    }

    /**
     * Clears all elements from the view.
     */
    public void clear() {
        nodeLayer.getChildren().clear();
        connectionLayer.getChildren().clear();
    }

    /**
     * Adds a node skin to the view.
     *
     * @param nodeSkin the {@link GNodeSkin} instance to be added
     */
    public void add(final GNodeSkin nodeSkin) {
        nodeLayer.getChildren().add(nodeSkin.getRoot());
    }

    /**
     * Adds a connection skin to the view.
     *
     * @param connectionSkin the {@link GConnectionSkin} instance to be added
     */
    public void add(final GConnectionSkin connectionSkin) {
        connectionLayer.getChildren().add(connectionSkin.getRoot());
    }

    /**
     * Adds a joint skin to the view.
     *
     * @param jointSkin the {@link GJointSkin} instance to be added
     */
    public void add(final GJointSkin jointSkin) {
        connectionLayer.getChildren().add(jointSkin.getRoot());
    }

    /**
     * Adds a tail skin to the view.
     *
     * @param tailSkin the {@link GTailSkin} instance to be added
     */
    public void add(final GTailSkin tailSkin) {
        connectionLayer.getChildren().add(tailSkin.getRoot());
    }

    /**
     * Removes a node skin from the view. Does nothing if the skin is not present.
     *
     * @param nodeSkin the {@link GNodeSkin} instance to remove
     */
    public void remove(final GNodeSkin nodeSkin) {
        nodeLayer.getChildren().remove(nodeSkin.getRoot());
    }

    /**
     * Removes a connection skin from the view. Does nothing if the skin is not present.
     *
     * @param connectionSkin the {@link GConnectionSkin} instance to remove
     */
    public void remove(final GConnectionSkin connectionSkin) {
        connectionLayer.getChildren().remove(connectionSkin.getRoot());
    }

    /**
     * Removes a joint skin from the view. Does nothing if the skin is not present.
     *
     * @param jointSkin the {@link GJointSkin} instance to remove
     */
    public void remove(final GJointSkin jointSkin) {
        connectionLayer.getChildren().remove(jointSkin.getRoot());
    }

    /**
     * Removes a tail skin from the view. Does nothing if the skin is not present.
     *
     * @param tailSkin the {@link GTailSkin} instance to remove
     */
    public void remove(final GTailSkin tailSkin) {
        connectionLayer.getChildren().remove(tailSkin.getRoot());
    }

    /**
     * Sets the layout properties of the view.
     *
     * <p>
     * This is used specify information like whether the grid should be shown and/or snapped to.
     * </p>
     *
     * @param editorProperties the {@link GraphEditorProperties} instance to be used by the view
     */
    public void setEditorProperties(final GraphEditorProperties editorProperties) {

        this.editorProperties = editorProperties;

        grid.setProperties(editorProperties);
        grid.setVisible(editorProperties.isGridVisible());

        editorProperties.gridVisibleProperty().addListener((v, o, n) -> grid.setVisible(n));
        editorProperties.gridSpacingProperty().addListener((v, o, n) -> grid.draw(getWidth(), getHeight()));
    }

    /**
     * Gets the editor properties instance used by the view.
     *
     * @return editorProperties the {@link GraphEditorProperties} instance used by the view
     */
    public GraphEditorProperties getEditorProperties() {
        return editorProperties;
    }

    /**
     * Draws a selection box in the view.
     *
     * @param x the x position of the selection box
     * @param y the y position of the selection box
     * @param width the width of the selection box
     * @param height the height of the selection box
     */
    public void drawSelectionBox(final double x, final double y, final double width, final double height) {
        selectionBox.draw(x, y, width, height);
    }

    /**
     * Hides the selection box.
     */
    public void hideSelectionBox() {
        selectionBox.setVisible(false);
    }

    /**
     * Enables / disables caching of the view content (node and connection layers).
     *
     * <p>
     * This increases performance if the content does not need to be redrawn. It <b>decreases</b> performance when the
     * content is redrawn. Use with care.
     * </p>
     * 
     * <p>
     * <b>Note:</b> Currently leads to poor performance when scale transforms are used, or on retina displays.
     * </p>
     *
     * @param cache {@code true} to enable caching, {@code false} to disable it
     */
    public void setContentCache(final boolean cache) {
        nodeLayer.setCache(cache);
        connectionLayer.setCache(cache);
    }

    @Override
    protected void layoutChildren() {
        nodeLayer.resizeRelocate(0, 0, getWidth(), getHeight());
        connectionLayer.resizeRelocate(0, 0, getWidth(), getHeight());
    }

    /**
     * Initializes the two layers (node and connection) that the view is composed of.
     */
    private void initializeLayers() {

        nodeLayer.setPickOnBounds(false);
        connectionLayer.setPickOnBounds(false);

        nodeLayer.cacheHintProperty().set(CacheHint.SPEED);
        connectionLayer.cacheHintProperty().set(CacheHint.SPEED);

        nodeLayer.getStyleClass().addAll(STYLE_CLASS_NODE_LAYER);
        connectionLayer.getStyleClass().addAll(STYLE_CLASS_CONNECTION_LAYER);

        nodeLayer.setId(NODE_LAYER_ID);
        connectionLayer.setId(CONNECTION_LAYER_ID);

        nodeLayer.maxWidthProperty().bind(maxWidthProperty());
        nodeLayer.maxHeightProperty().bind(maxHeightProperty());
        nodeLayer.minWidthProperty().bind(minWidthProperty());
        nodeLayer.minHeightProperty().bind(minHeightProperty());

        connectionLayer.maxWidthProperty().bind(maxWidthProperty());
        connectionLayer.maxHeightProperty().bind(maxHeightProperty());
        connectionLayer.minWidthProperty().bind(minWidthProperty());
        connectionLayer.minHeightProperty().bind(minHeightProperty());

        // Node layer should be on top of connection layer, so we add it second.
        getChildren().add(connectionLayer);
        getChildren().add(nodeLayer);
    }

    /**
     * Adds a listener to the width and height properties of the view to tell the grid to redraw.
     */
    private void initializeGrid() {

        getChildren().add(grid);
        grid.toBack();

        widthProperty().addListener((v, o, n) -> grid.draw(getWidth(), getHeight()));
        heightProperty().addListener((v, o, n) -> grid.draw(getWidth(), getHeight()));
    }

    /**
     * A layer inside the graph editor view. Used for grouping elements together in the z-direction.
     *
     * <p>
     * All connections are redrawn when the children of a view layer are layed out.
     * </p>
     */
    private class GraphEditorViewLayer extends Region {

        @Override
        public ObservableList<Node> getChildren() {
            return super.getChildren();
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            connectionLayouter.redraw();
        }
    }
}