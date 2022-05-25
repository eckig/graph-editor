/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.view;

import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.window.AutoScrollingWindow;
import io.github.eckig.grapheditor.window.GraphEditorMinimap;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;


/**
 * A container for the graph editor.
 *
 * <p>
 * This is intended for graphs that can be larger than the space available in
 * the scene. The user can pan around by right-clicking and dragging. A minimap
 * can be shown to help with navigation.
 * </p>
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>GraphEditorContainer graphEditorContainer = new GraphEditorContainer();
 * GraphEditor graphEditor = new DefaultGraphEditor();
 *
 * graphEditorContainer.setGraphEditor(graphEditor);
 * graphEditorContainer.getMinimap().setVisible(true);</code>
 * </pre>
 *
 * The graph editor container is a {@link Region} and can be added to the JavaFX
 * scene graph in the usual way.
 * </p>
 *
 * <p>
 * When a {@link GraphEditor} is set inside this container, its view becomes
 * <b>unmanaged</b> and its width and height values are set to those in the
 * {@link GModel} instance.
 * </p>
 */
public class GraphEditorContainer extends AutoScrollingWindow
{

    /**
     * default view stylesheet
     */
    private static final String STYLESHEET_VIEW = GraphEditorContainer.class.getResource("defaults.css").toExternalForm(); //$NON-NLS-1$

    private static final double MINIMAP_INDENT = 10;

    private final GraphEditorMinimap minimap = new GraphEditorMinimap();

    private GraphEditor graphEditor;
    private final ChangeListener<GModel> modelChangeListener = (observable, oldValue, newValue) -> modelChanged(newValue);

    /**
     * Creates a new {@link GraphEditorContainer}.
     */
    public GraphEditorContainer()
    {
        getChildren().add(minimap);

        minimap.setWindow(this);
        minimap.setVisible(false);
    }

    @Override
    public String getUserAgentStylesheet()
    {
        return STYLESHEET_VIEW;
    }

    private void modelChanged(final GModel newValue)
    {
        if (newValue != null)
        {
            graphEditor.getView().resize(newValue.getContentWidth(), newValue.getContentHeight());
        }
        checkWindowBounds();
        minimap.setModel(newValue);
    }

    /**
     * Sets the graph editor to be displayed in this container.
     *
     * @param pGraphEditor
     *            a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor pGraphEditor)
    {
        final GraphEditor previous = graphEditor;
        if (previous != null)
        {
            previous.modelProperty().removeListener(modelChangeListener);
            setEditorProperties(null);
        }

        graphEditor = pGraphEditor;

        if (pGraphEditor != null)
        {
            pGraphEditor.modelProperty().addListener(modelChangeListener);

            final Region view = pGraphEditor.getView();
            final GModel model = pGraphEditor.getModel();

            if (model != null)
            {
                view.resize(model.getContentWidth(), model.getContentHeight());
            }

            setContent(view);
            minimap.setContent(view);
            minimap.setModel(model);
            minimap.setSelectionManager(pGraphEditor.getSelectionManager());

            view.toBack();

            setEditorProperties(pGraphEditor.getProperties());
        }
        else
        {
            setEditorProperties(null);
            minimap.setContent(null);
            minimap.setModel(null);
        }
    }

    /**
     * Returns the {@link GraphEditorMinimap}
     *
     * @return the graph editor minimap
     */
    public GraphEditorMinimap getMinimap()
    {
        return minimap;
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();

        if (getChildren().contains(minimap))
        {
            minimap.relocate(getWidth() - (minimap.getWidth() + MINIMAP_INDENT), MINIMAP_INDENT);
        }
    }
}
