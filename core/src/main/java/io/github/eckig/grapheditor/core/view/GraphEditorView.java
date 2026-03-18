/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.view;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GTailSkin;
import io.github.eckig.grapheditor.VirtualSkin;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.utils.SelectionBox;
import io.github.eckig.grapheditor.core.view.impl.GraphEditorGrid;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;
import io.github.eckig.grapheditor.window.PanningWindow;
import javafx.beans.InvalidationListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;


/**
 * The {@link Region} that all visual elements in the graph editor are added to.
 *
 * <p>
 * There is one instance of this class per {@link DefaultGraphEditor}. It is the
 * outermost JavaFX node of the editor.
 * </p>
 *
 * <p>
 * The view currently has three layers - a <b>node</b> layer, a
 * <b>connection</b> layer and the background grid.
 * The node layer is in front. Graph nodes are added to
 * the node layer, while connections, joints, and tails are added to the
 * connection layer. This means nodes will always be in front of connections.
 * </p>
 *
 * <p>
 * Calling toFront() or toBack() on the associated JavaFX nodes will just
 * reposition them inside their layer. The layers always have the same
 * dimensions as the editor region itself.
 * </p>
 */
public class GraphEditorView extends Region
{

    private static final String STYLE_CLASS = "graph-editor";
    private static final String STYLE_CLASS_NODE_LAYER = "graph-editor-node-layer";
    private static final String STYLE_CLASS_CONNECTION_LAYER = "graph-editor-connection-layer";
    private static final double VIEWPORT_PADDING = 300;

    private final Pane mNodeLayer = new Pane();

    private final Pane mConnectionLayer = new Pane()
    {

        @Override
        protected void layoutChildren()
        {
            super.layoutChildren();
            drawConnections();
        }
    };

    private final GraphEditorGrid mGrid = new GraphEditorGrid();
    private final InvalidationListener mGridListener = _ -> resizeRelocateGrid();
    private final SelectionBox mSelectionBox = new SelectionBox();
    private final GraphEditorProperties mEditorProperties;

    private ConnectionLayouter mConnectionLayouter;

    /**
     * Creates a new {@link GraphEditorView} to which skin instances can be
     * added and removed.
     */
    public GraphEditorView(final GraphEditorProperties pEditorProperties)
    {
        getStyleClass().addAll(STYLE_CLASS);

        setMaxSize(GraphEditorProperties.DEFAULT_MAX_WIDTH, GraphEditorProperties.DEFAULT_MAX_HEIGHT);
        setMinSize(GraphEditorProperties.DEFAULT_MIN_WIDTH, GraphEditorProperties.DEFAULT_MIN_HEIGHT);

        initializeLayers();

        mEditorProperties = pEditorProperties;

        if (mEditorProperties != null)
        {
            mGrid.visibleProperty().bind(mEditorProperties.gridVisibleProperty());
            mGrid.gridSpacingProperty().bind(mEditorProperties.gridSpacingProperty());
        }

        parentProperty().addListener((_, oldParent, newParent) -> parentChanged(oldParent, newParent));
        layoutXProperty().addListener(mGridListener);
        layoutYProperty().addListener(mGridListener);
    }

    private void parentChanged(final Parent pOldParent, final Parent pNewParent)
    {
        if (pOldParent instanceof PanningWindow pw)
        {
            pw.widthProperty().removeListener(mGridListener);
            pw.heightProperty().removeListener(mGridListener);
        }
        if (pNewParent instanceof PanningWindow pw)
        {
            pw.widthProperty().addListener(mGridListener);
            pw.heightProperty().addListener(mGridListener);
        }
    }

    /**
     * Sets the connection-layouter to be used by the view.
     *
     * @param pConnectionLayouter
     *            the graph editor's {@link ConnectionLayouter} instance
     */
    public void setConnectionLayouter(final ConnectionLayouter pConnectionLayouter)
    {
        mConnectionLayouter = pConnectionLayouter;
    }

    /**
     * Clears all elements from the view.
     */
    public void clear()
    {
        mNodeLayer.getChildren().clear();
        mConnectionLayer.getChildren().clear();
    }

    /**
     * Adds a node skin to the view.
     *
     * @param pNodeSkin
     *            the {@link GNodeSkin} instance to be added
     */
    public void add(final GNodeSkin pNodeSkin)
    {
        if (pNodeSkin != null && !(pNodeSkin instanceof VirtualSkin))
        {
            mNodeLayer.getChildren().add(pNodeSkin.getRoot());
        }
    }

    /**
     * Adds a connection skin to the view.
     *
     * @param pConnectionSkin
     *            the {@link GConnectionSkin} instance to be added
     */
    public void add(final GConnectionSkin pConnectionSkin)
    {
        if (pConnectionSkin != null && !(pConnectionSkin instanceof VirtualSkin))
        {
            mConnectionLayer.getChildren().addFirst(pConnectionSkin.getRoot());
        }
    }

    /**
     * Adds a joint skin to the view.
     *
     * @param pJointSkin
     *            the {@link GJointSkin} instance to be added
     */
    public void add(final GJointSkin pJointSkin)
    {
        if (pJointSkin != null && !(pJointSkin instanceof VirtualSkin))
        {
            mConnectionLayer.getChildren().add(pJointSkin.getRoot());
        }
    }

    /**
     * Adds a tail skin to the view.
     *
     * @param pTailSkin
     *            the {@link GTailSkin} instance to be added
     */
    public void add(final GTailSkin pTailSkin)
    {
        if (pTailSkin != null && !(pTailSkin instanceof VirtualSkin))
        {
            // add to back:
            mConnectionLayer.getChildren().addFirst(pTailSkin.getRoot());
        }
    }

    /**
     * Removes a node skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param pNodeSkin
     *            the {@link GNodeSkin} instance to remove
     */
    public void remove(final GNodeSkin pNodeSkin)
    {
        if (pNodeSkin != null && !(pNodeSkin instanceof VirtualSkin))
        {
            mNodeLayer.getChildren().remove(pNodeSkin.getRoot());
        }
    }

    /**
     * Removes a connection skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param pConnectionSkin
     *            the {@link GConnectionSkin} instance to remove
     */
    public void remove(final GConnectionSkin pConnectionSkin)
    {
        if (pConnectionSkin != null && !(pConnectionSkin instanceof VirtualSkin))
        {
            mConnectionLayer.getChildren().remove(pConnectionSkin.getRoot());
        }
    }

    /**
     * Removes a joint skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param pJointSkin
     *            the {@link GJointSkin} instance to remove
     */
    public void remove(final GJointSkin pJointSkin)
    {
        if (pJointSkin != null && !(pJointSkin instanceof VirtualSkin))
        {
            mConnectionLayer.getChildren().remove(pJointSkin.getRoot());
        }
    }

    /**
     * Removes a tail skin from the view. Does nothing if the skin is not
     * present.
     *
     * @param pTailSkin
     *            the {@link GTailSkin} instance to remove
     */
    public void remove(final GTailSkin pTailSkin)
    {
        if (pTailSkin != null && !(pTailSkin instanceof VirtualSkin))
        {
            mConnectionLayer.getChildren().remove(pTailSkin.getRoot());
        }
    }

    /**
     * Gets the editor properties instance used by the view.
     *
     * @return editorProperties the {@link GraphEditorProperties} instance used
     *         by the view
     */
    public GraphEditorProperties getEditorProperties()
    {
        return mEditorProperties;
    }

    /**
     * Draws a selection box in the view.
     *
     * @param pX
     *            the x position of the selection box
     * @param pY
     *            the y position of the selection box
     * @param pWidth
     *            the width of the selection box
     * @param pHeight
     *            the height of the selection box
     */
    public void drawSelectionBox(final double pX, final double pY, final double pWidth, final double pHeight)
    {
        mSelectionBox.draw(pX, pY, pWidth, pHeight);
    }

    /**
     * Hides the selection box.
     */
    public void hideSelectionBox()
    {
        mSelectionBox.setVisible(false);
    }

    @Override
    protected void layoutChildren()
    {
        final double width = getWidth();
        final double height = getHeight();
        mNodeLayer.resizeRelocate(0, 0, width, height);
        mConnectionLayer.resizeRelocate(0, 0, width, height);
        drawConnections();
    }

    private void resizeRelocateGrid()
    {
        final var panningWindow = getParent() instanceof PanningWindow pw ? pw : null;
        final var s = mGrid.getGridSpacing();
        final var width = panningWindow == null ? 0 : panningWindow.getWidth() + s;
        final var height = panningWindow == null ? 0 : panningWindow.getHeight() + s;
        final var x = getLayoutX();
        final var y = getLayoutY();
        mGrid.resizeRelocate(x % s + x * -1.0, y % s + y * -1.0, width, height);
    }

    /**
     * calls {@link ConnectionLayouter#draw()}
     *
     * @since 31.01.2019
     */
    private void drawConnections()
    {
        if (mConnectionLayouter != null)
        {
            mConnectionLayouter.draw();
        }
    }

    /**
     * Initializes the two layers (node and connection) that the view is
     * composed of.
     */
    private void initializeLayers()
    {
        mNodeLayer.setPickOnBounds(false);
        mConnectionLayer.setPickOnBounds(false);

        mNodeLayer.getStyleClass().add(STYLE_CLASS_NODE_LAYER);
        mConnectionLayer.getStyleClass().add(STYLE_CLASS_CONNECTION_LAYER);

        // Node layer should be on top of connection layer, so we add it second.
        getChildren().addAll(mGrid, mConnectionLayer, mNodeLayer, mSelectionBox);
    }

    /**
     * set the views size depending on the given model bounds, but
     * respecting the minimum and maximum dimensions
     *
     * @param pModelBounds
     *            the bounds of the model
     */
    public void setModelBounds(final Rectangle2D pModelBounds)
    {
        if (pModelBounds != null)
        {
            resize(Math.max(getMinWidth(), Math.min(pModelBounds.getWidth() + VIEWPORT_PADDING, getMaxWidth())),
                    Math.max(getMinHeight(), Math.min(pModelBounds.getHeight() + VIEWPORT_PADDING, getMaxHeight())));
        }
    }
}
