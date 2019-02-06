/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
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
 * The view currently has two layers - a <b>node</b> layer and a
 * <b>connection</b> layer. The node layer is in front. Graph nodes are added to
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

    /**
     * default view stylesheet
     */
    public static final String STYLESHEET_VIEW = "view.css";
    /**
     * default view stylesheet
     */
    public static final String STYLESHEET_DEFAULTS = "defaults.css";

    private static final String STYLE_CLASS = "graph-editor";
    private static final String STYLE_CLASS_NODE_LAYER = "graph-editor-node-layer";
    private static final String STYLE_CLASS_CONNECTION_LAYER = "graph-editor-connection-layer";

    private final Pane mNodeLayer = new Pane();

    private final Pane mConnectionLayer = new Pane()
    {

        @Override
        protected void layoutChildren()
        {
            super.layoutChildren();
            layoutConnections();
        }
    };

    private final GraphEditorGrid mGrid = new GraphEditorGrid();
    private ConnectionLayouter mConnectionLayouter;

    private final SelectionBox mSelectionBox = new SelectionBox();

    private GraphEditorProperties mEditorProperties;

    /**
     * Creates a new {@link GraphEditorView} to which skin instances can be
     * added and removed.
     */
    public GraphEditorView()
    {
        getStyleClass().addAll(STYLE_CLASS);

        setMaxWidth(GraphEditorProperties.DEFAULT_MAX_WIDTH);
        setMaxHeight(GraphEditorProperties.DEFAULT_MAX_HEIGHT);

        initializeLayers();
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
        if (pNodeSkin != null)
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
        if (pConnectionSkin != null)
        {
            mConnectionLayer.getChildren().add(0, pConnectionSkin.getRoot());
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
        if (pJointSkin != null)
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
        if (pTailSkin != null)
        {
            // add to back:
            mConnectionLayer.getChildren().add(0, pTailSkin.getRoot());
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
        if (pNodeSkin != null)
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
        if (pConnectionSkin != null)
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
        if (pJointSkin != null)
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
        if (pTailSkin != null)
        {
            mConnectionLayer.getChildren().remove(pTailSkin.getRoot());
        }
    }

    /**
     * Sets the layout properties of the view.
     *
     * <p>
     * This is used specify information like whether the grid should be shown
     * and/or snapped to.
     * </p>
     *
     * @param pEditorProperties
     *            the {@link GraphEditorProperties} instance to be used by the
     *            view
     */
    public void setEditorProperties(final GraphEditorProperties pEditorProperties)
    {
        mEditorProperties = pEditorProperties;

        if (mEditorProperties != null)
        {
            mGrid.visibleProperty().bind(mEditorProperties.gridVisibleProperty());
            mGrid.gridSpacingProperty().bind(mEditorProperties.gridSpacingProperty());
        }
        else
        {
            mGrid.visibleProperty().unbind();
            mGrid.gridSpacingProperty().unbind();
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
        mGrid.resizeRelocate(0, 0, width, height);
        layoutConnections();
    }

    /**
     * calls {@link ConnectionLayouter#redrawAll()}
     *
     * @since 31.01.2019
     */
    void layoutConnections()
    {
        if (mConnectionLayouter != null)
        {
            mConnectionLayouter.redrawAll();
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
}
