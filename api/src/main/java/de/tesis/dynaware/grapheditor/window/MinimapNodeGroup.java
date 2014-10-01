/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * The minimap representation of all nodes in the graph editor.
 *
 * <p>
 * This is responsible for drawing mini versions of all nodes in a {@link GModel}. This group of mini-nodes is then
 * displayed inside the {@link GraphEditorMinimap}.
 * </p>
 */
public class MinimapNodeGroup extends MinimapContentRepresentation {

    private SkinLookup skinLookup;
    private GModel model;

    /**
     * Sets the skin lookup instance currently in use by this graph editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param skinLookup a {@link SkinLookup} instance
     */
    public void setSkinLookup(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
    }

    /**
     * Sets the model whose nodes will be drawn in the minimap.
     *
     * @param model athe {@link GModel} whose nodes are to be drawn
     */
    public void setModel(final GModel model) {
        this.model = model;
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

                final MinimapNode minimapNode = new MinimapNode(node, skinLookup);

                minimapNode.setX(Math.round(node.getX() * scaleFactor));
                minimapNode.setY(Math.round(node.getY() * scaleFactor));
                minimapNode.setWidth(Math.round(node.getWidth() * scaleFactor));
                minimapNode.setHeight(Math.round(node.getHeight() * scaleFactor));

                getChildren().add(minimapNode);
            }
        }
    }
}
