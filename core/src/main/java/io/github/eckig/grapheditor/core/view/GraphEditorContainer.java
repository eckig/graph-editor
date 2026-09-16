/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.view;

import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;
import io.github.eckig.grapheditor.window.GraphEditorMinimap;
import io.github.eckig.grapheditor.window.PanningWindow;
import javafx.beans.value.ChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.layout.Region;


/**
 * A container for the graph editor.
 *
 * <p>
 * This is intended for graphs that can be larger than the space available in
 * the scene. The user can pan around by dragging with the middle mouse button,
 * or by holding {@code SPACE} and dragging with the primary mouse button. A
 * minimap can be shown to help with navigation.
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
public class GraphEditorContainer extends PanningWindow
{

    /**
     * default view stylesheet
     */
    private static final String STYLESHEET_VIEW = GraphEditorContainer.class.getResource("defaults.css").toExternalForm(); //$NON-NLS-1$

    private static final double MINIMAP_INDENT = 10;

    private static final String ACCESSIBLE_ROLE_DESCRIPTION = "graphEditor.accessibleRoleDescription"; //$NON-NLS-1$
    private static final String ACCESSIBLE_TEXT = "graphEditor.accessibleText"; //$NON-NLS-1$
    private static final String ACCESSIBLE_TEXT_EMPTY = "graphEditor.accessibleTextEmpty"; //$NON-NLS-1$

    private final GraphEditorMinimap minimap = new GraphEditorMinimap();

    private GraphEditor graphEditor;
    private final ChangeListener<GModel> modelChangeListener = (_, _, newValue) -> modelChanged(newValue);

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
        minimap.setModel(newValue);
    }

    @Override
    protected void setContent(final Region pContent)
    {
        super.setContent(pContent);
        if (pContent instanceof GraphEditorView v)
        {
            v.setPanningWindow(this);
            // share the gesture arbitration so panning cannot start while another
            // gesture (move, resize, connect, select) is in progress
            setEventManager(v.getEditorProperties());
        }
        else
        {
            setEventManager(null);
        }
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
        }

        graphEditor = pGraphEditor;

        if (pGraphEditor != null)
        {
            pGraphEditor.modelProperty().addListener(modelChangeListener);

            final Region view = pGraphEditor.getView();
            final GModel model = pGraphEditor.getModel();

            setContent(view);
            minimap.setContent(view);
            minimap.setModel(model);
            minimap.setSelectionManager(pGraphEditor.getSelectionManager());

            view.toBack();
        }
        else
        {
            minimap.setContent(null);
            minimap.setModel(null);
        }
    }

    @Override
    public Object queryAccessibleAttribute(final AccessibleAttribute pAttribute, final Object... pParameters)
    {
        // computed on demand so that assistive technology always gets the current
        // state, without having to observe every model change
        final GraphEditorProperties properties = graphEditor == null ? null : graphEditor.getProperties();
        if (properties != null)
        {
            switch (pAttribute)
            {
                case ROLE_DESCRIPTION:
                    return properties.getString(ACCESSIBLE_ROLE_DESCRIPTION);
                case TEXT:
                    return getAccessibleDescriptionOfModel(properties);
                default:
                    break;
            }
        }
        return super.queryAccessibleAttribute(pAttribute, pParameters);
    }

    private String getAccessibleDescriptionOfModel(final GraphEditorProperties pProperties)
    {
        final GModel model = graphEditor.getModel();
        if (model == null)
        {
            return pProperties.getString(ACCESSIBLE_TEXT_EMPTY);
        }
        return pProperties.getString(ACCESSIBLE_TEXT, Integer.valueOf(model.getNodes().size()),
                Integer.valueOf(model.getConnections().size()));
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
