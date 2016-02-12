/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

/**
 * The minimap representation of a single node in the graph editor.
 */
public class MinimapNode extends Rectangle {

    private static final String STYLE_CLASS = "minimap-node";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private final ChangeListener<Boolean> selectionListener = (v, o, n) -> setSelected(n);
    private Pair<BooleanProperty, ChangeListener<Boolean>> activeListener;

    /**
     * Creates a new {@link MinimapNode} instance.
     *
     * @param node the {@link GNode} that this minimap node is representing
     * @param skinLookup the {@link SkinLookup} instance in use by the graph editor
     */
    public MinimapNode(final GNode node, final SkinLookup skinLookup) {

        getStyleClass().setAll(STYLE_CLASS);

        if (skinLookup != null) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin != null) {
                setSelected(nodeSkin.isSelected());
                final ChangeListener<Boolean> weakListener = new WeakChangeListener<>(selectionListener); 
                nodeSkin.selectedProperty().addListener(weakListener);
                activeListener = new Pair<>(nodeSkin.selectedProperty(), weakListener);
            } else {
                setSelected(false);
            }
        } else {
            setSelected(false);
        }
    }
    
    public void dispose() {
        if(activeListener != null) {
            activeListener.getKey().removeListener(activeListener.getValue());
            activeListener = null;
        }
    }

    /**
     * Sets the style class of the minimap node according to whether its node is selected or not.
     *
     * @param selected {@code true} if the corresponding node is selected
     */
    private void setSelected(final boolean selected) {
        pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, selected);
    }
}
