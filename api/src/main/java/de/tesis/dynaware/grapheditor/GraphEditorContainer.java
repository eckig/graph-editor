/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
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
    private final EventHandler<ScrollEvent> scrollHandler = this::handleScroll;
    private final EventHandler<ZoomEvent> zoomHandler = this::handleZoom;

	private final ChangeListener<GraphInputMode> inputModeListener = (w,o,n) -> setPanningActive(n == GraphInputMode.NAVIGATION);

    /**
     * Creates a new {@link GraphEditorContainer}.
     */
    public GraphEditorContainer() {
        
        getChildren().add(minimap);

        minimap.setWindow(this);
        minimap.setVisible(false);
    }
    
    private void handleScroll(final ScrollEvent event) {
    	// touch events are synthesized by JavaFX to scroll events
    	// so in effect both mouse scroll wheel rotations and touch-zoom-gestures will be handled here
    	// but: only scroll when the mouse-cursor/finger is hovering over the view
    	//      skip if the target is for example a node
    	if(!isPanningActive() || graphEditor == null || event.getTarget() != graphEditor.getView()) {
    		return;
    	}
		panBy(-event.getDeltaX(), -event.getDeltaY());
    }
    
    private boolean isZooming = false;
    private double currentZoomFactor = 1;
    private final Scale scaleTransform = new Scale(1, 1, 0, 0);
    
    private void handleZoom(final ZoomEvent event) {
    	if(!isPanningActive()) {
    		return;
    	}
    	isZooming=true;
    	try
		{
			double zoomFactor = event.getTotalZoomFactor();
			final double zoomFactorRatio = zoomFactor / currentZoomFactor;

			final double currentCenterX = windowXProperty().get();
			final double currentCenterY = windowYProperty().get();

			final double newZoomLevel = zoomFactorRatio * scaleTransform.getX();
			if (newZoomLevel < 1.3 && newZoomLevel > 0.3) {
				scaleTransform.setX(newZoomLevel);
				scaleTransform.setY(newZoomLevel);
				panTo(currentCenterX * zoomFactorRatio, currentCenterY * zoomFactorRatio);
				currentZoomFactor = zoomFactor;
			}
		}
    	finally {
    		isZooming=false;
    	}
    }
    
    private void filterEventsWhileZooming(final Event event) {
    	if(isZooming) {
    		event.consume();
    	}
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
            this.graphEditor.getView().removeEventHandler(ZoomEvent.ZOOM, zoomHandler);
            this.graphEditor.getProperties().inputModeProperty().removeListener(inputModeListener);
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
            view.addEventHandler(ZoomEvent.ZOOM, zoomHandler);
            
            graphEditor.getProperties().inputModeProperty().addListener(inputModeListener);
            setPanningActive(graphEditor.getProperties().getInputMode() == GraphInputMode.NAVIGATION);
            
            //TODO
            graphEditor.getView().getTransforms().add(scaleTransform);
            graphEditor.getView().addEventFilter(MouseEvent.ANY, this::filterEventsWhileZooming);

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
