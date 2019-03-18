/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import java.util.ArrayList;
import java.util.List;

import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.utils.Arrow;
import javafx.geometry.Point2D;
import javafx.scene.Node;

/**
 * Tail skin for the 'tree-like' graph. Pretty much just an arrow.
 */
public class TreeTailSkin extends GTailSkin {

    private static final String STYLE_CLASS = "tree-tail"; //$NON-NLS-1$
    private static final double OFFSET_DISTANCE = 15;

    private final Arrow arrow = new Arrow();

    public TreeTailSkin(final GConnector connector) {

        super(connector);

        arrow.getStyleClass().add(STYLE_CLASS);
    }

    @Override
    public Node getRoot() {
        return arrow;
    }

    @Override
    public void draw(final Point2D start, final Point2D end) {
        drawArrow(start, end);
    }

    @Override
    public void draw(final Point2D start, final Point2D end, final List<Point2D> jointPositions) {
        drawArrow(start, end);
    }

    @Override
    public void draw(final Point2D start, final Point2D end, final GConnector target, final boolean valid) {
        drawArrow(start, end);
    }

    @Override
    public void draw(final Point2D start, final Point2D end, final List<Point2D> jointPositions,
            final GConnector target, final boolean valid) {
        drawArrow(start, end);
    }

    @Override
    public List<Point2D> allocateJointPositions() {
        return new ArrayList<>();
    }

    @Override
    protected void selectionChanged(boolean isSelected) {
        // Not implemented
    }

    /**
     * Draws an arrow from the start to end point.
     *
     * @param start the start point of the arrow
     * @param end the end point (tip) of the arrow
     */
    private void drawArrow(final Point2D start, final Point2D end) {

        if (getItem().getType().equals(TreeSkinConstants.TREE_OUTPUT_CONNECTOR)) {
            ArrowUtils.draw(arrow, start, end, OFFSET_DISTANCE);
        } else {
            ArrowUtils.draw(arrow, end, start, OFFSET_DISTANCE);
        }
    }
}
