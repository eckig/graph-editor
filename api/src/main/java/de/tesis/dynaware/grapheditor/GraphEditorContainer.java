/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.window.AutoScrollingWindow;
import de.tesis.dynaware.grapheditor.window.GraphEditorMinimap;

/**
 * A container for the graph editor.
 *
 * <p>
 * This is intended for graphs that can be larger than the space available in the scene. The user can pan around by
 * right-clicking and dragging. A minimap can be shown to help with navigation.
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
 * The graph editor container is a {@link Region} and can be added to the JavaFX scene graph in the usual way.
 * </p>
 *
 * <p>
 * When a {@link GraphEditor} is set inside this container, its view becomes <b>unmanaged</b> and its width and height
 * values are set to those in the {@link GModel} instance.
 * </p>
 */
public class GraphEditorContainer extends AutoScrollingWindow {

    private static final double MINIMAP_INDENT = 10;

    private final GraphEditorMinimap minimap = new GraphEditorMinimap();

    private GraphEditor graphEditor;
    private final ChangeListener<GModel> modelChangeListener = (observable, oldValue, newValue) -> modelChanged(newValue);
    private final EventHandler<ScrollEvent> scrollHandler = event -> panBy(-event.getDeltaX(), -event.getDeltaY());

    /**
     * Creates a new {@link GraphEditorContainer}.
     */
    public GraphEditorContainer() {
        
        getChildren().add(minimap);

        minimap.setWindow(this);
        minimap.setVisible(false);
    }
    
    private void modelChanged(final GModel newValue) {

        if (newValue != null) {
            graphEditor.getView().resize(newValue.getContentWidth(), newValue.getContentHeight());
        }
        checkWindowBounds();
        minimap.setModel(newValue);
    }

    /**
     * Sets the graph editor to be displayed in this container.
     *
     * @param graphEditor a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor graphEditor) {

        if (this.graphEditor != null) {
            this.graphEditor.modelProperty().removeListener(modelChangeListener);
            this.graphEditor.getView().removeEventHandler(ScrollEvent.SCROLL, scrollHandler);
        }

        this.graphEditor = graphEditor;

        if (graphEditor != null) {

            graphEditor.modelProperty().addListener(modelChangeListener);

            final Region view = graphEditor.getView();
            final GModel model = graphEditor.getModel();

            if (model != null) {
                view.resize(model.getContentWidth(), model.getContentHeight());
            }

            setContent(view);
            minimap.setContent(view);
            minimap.setModel(model);
            minimap.setSelectionManager(graphEditor.getSelectionManager());

            view.toBack();
            view.addEventHandler(ScrollEvent.SCROLL, scrollHandler);

        } else {
            minimap.setContent(null);
            minimap.setModel(null);
        }
    }

    /**
     * Returns the {@link GraphEditorMinimap}
     * 
     * @param the graph editor minimap
     */
    public GraphEditorMinimap getMinimap() {
        return minimap;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        if (getChildren().contains(minimap)) {
            minimap.relocate(getWidth() - (minimap.getWidth() + MINIMAP_INDENT), MINIMAP_INDENT);
        }
    }
}
