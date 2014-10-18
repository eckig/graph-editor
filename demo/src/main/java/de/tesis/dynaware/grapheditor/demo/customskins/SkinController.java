package de.tesis.dynaware.grapheditor.demo.customskins;

/**
 * Responsible for skin-specific logic in the graph editor demo.
 */
public interface SkinController {

    /**
     * Adds a node to the graph.
     * 
     * @param currentZoomFactor the current zoom factor (1 for 100%)
     */
    void addNode(final double currentZoomFactor);

    /**
     * Adds an input connector to all selected nodes.
     */
    void addInputConnector();

    /**
     * Adds an output connector to all selected nodes.
     */
    void addOutputConnector();

    /**
     * Handles the paste operation.
     */
    void handlePaste();
}
