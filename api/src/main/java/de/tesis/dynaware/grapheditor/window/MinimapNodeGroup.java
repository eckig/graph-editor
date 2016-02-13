/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import java.util.HashMap;
import java.util.Map;

import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.css.PseudoClass;
import javafx.scene.shape.Rectangle;

/**
 * The minimap representation of all nodes in the graph editor.
 *
 * <p>
 * This is responsible for drawing mini versions of all nodes in a
 * {@link GModel}. This group of mini-nodes is then displayed inside the
 * {@link GraphEditorMinimap}.
 * </p>
 */
public class MinimapNodeGroup extends MinimapContentRepresentation {

    private static final String STYLE_CLASS = "minimap-node";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private final InvalidationListener checkSelectionListener = obs -> checkSelection();
    private final InvalidationListener checkSelectionWeakListener = new WeakInvalidationListener(checkSelectionListener);

	private SelectionManager selectionManager;
	private GModel model;
	
	private final Map<GNode, Rectangle> nodes = new HashMap<>();
    

    /**
     * Sets the selection manager instance currently in use by this graph
     * editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param selectionManager
     *            a {@link SelectionManager} instance
     */
    public void setSelectionManager(final SelectionManager selectionManager) {
    	
    	if(this.selectionManager != null) {
    		this.selectionManager.getSelectedNodes().removeListener(checkSelectionWeakListener);
    	}
    	
        this.selectionManager = selectionManager;
        
        if(this.selectionManager != null) {
    		this.selectionManager.getSelectedNodes().addListener(checkSelectionWeakListener);
    	}
        checkSelection();
    }

    /**
     * Sets the model whose nodes will be drawn in the minimap.
     *
     * @param model athe {@link GModel} whose nodes are to be drawn
     */
    public void setModel(final GModel model) {
        this.model = model;
    }
    
    private void checkSelection() {

        for (final Rectangle rectangle : nodes.values()) {
            rectangle.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
        }

        if (selectionManager != null) {
            for (final GNode selection : selectionManager.getSelectedNodes()) {
                nodes.get(selection).pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            }
        }
    }

    /**
     * Draws the model's nodes at a scaled-down size to be displayed in the minimap.
     *
     * @param scaleFactor the ratio between the size of the content and the size of the minimap (between 0 and 1)
     */
    @Override
    public void draw(final double scaleFactor) {

        getChildren().clear();
        
        if (model != null) {
            for (final GNode node : model.getNodes()) {

                final Rectangle minimapNode = new Rectangle();
                minimapNode.getStyleClass().add(STYLE_CLASS);

                minimapNode.setX(Math.round(node.getX() * scaleFactor));
                minimapNode.setY(Math.round(node.getY() * scaleFactor));
                minimapNode.setWidth(Math.round(node.getWidth() * scaleFactor));
                minimapNode.setHeight(Math.round(node.getHeight() * scaleFactor));

                getChildren().add(minimapNode);
                nodes.put(node, minimapNode);
            }
            checkSelection();
        }
    }
}
