/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.scene.shape.Rectangle;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * The minimap representation of a single node in the graph editor.
 */
public class MinimapNode extends Rectangle {

    private static final String STYLE_CLASS_DEFAULT = "g-minimap-node-default";
    private static final String STYLE_CLASS_SELECTED = "g-minimap-node-selected";

    /**
     * Creates a new {@link MinimapNode} instance.
     *
     * @param node the {@link GNode} that this minimap node is representing
     * @param skinLookup the {@link SkinLookup} instance in use by the graph editor
     */
    public MinimapNode(final GNode node, final SkinLookup skinLookup) {

        if (skinLookup != null) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin != null) {
                setStyleClass(nodeSkin.isSelected());
                nodeSkin.selectedProperty().addListener((observable, oldValue, newValue) -> setStyleClass(newValue));
            } else {
                setStyleClass(false);
            }
        } else {
            setStyleClass(false);
        }
    }

    /**
     * Sets the style class of the minimap node according to whether it's node is selected or not
     *
     * @param selected {@code true} if the corresponding node is selected
     */
    private void setStyleClass(final boolean selected) {
        getStyleClass().setAll(selected ? STYLE_CLASS_SELECTED : STYLE_CLASS_DEFAULT);
    }
}
