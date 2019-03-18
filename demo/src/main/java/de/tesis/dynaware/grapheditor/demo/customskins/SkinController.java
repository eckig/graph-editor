package de.tesis.dynaware.grapheditor.demo.customskins;

import de.tesis.dynaware.grapheditor.demo.selections.SelectionCopier;
import javafx.geometry.Side;

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
     * activates this skin
     */
    void activate();

    /**
     * Adds a connector of the given type to all selected nodes.
     *
     * @param position the currently selected connector position
     * @param input {@code true} for input, {@code false} for output
     */
    void addConnector(Side position, boolean input);

    /**
     * Clears all connectors from all selected nodes.
     */
    void clearConnectors();

    /**
     * Handles the paste operation.
     * @param selectionCopier {@link SelectionCopier}
     */
    void handlePaste(SelectionCopier selectionCopier);

    /**
     * Handles the select-all operation.
     */
    void handleSelectAll();
}
