/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.Arrow;

/**
 * Connection skin for the 'tree-like' graph. Pretty much just an arrow.
 */
public class TreeConnectionSkin extends GConnectionSkin {

    private static final String STYLE_CLASS = "tree-connection";
    private static final double OFFSET_DISTANCE = 15;

    private final Arrow arrow = new Arrow();

    private List<Point2D> points;

    /**
     * Creates a new {@link TreeConnectionSkin} instance.
     *
     * @param connection the {@link GConnection} that this skin is representing
     */
    public TreeConnectionSkin(final GConnection connection) {

        super(connection);

        arrow.setManaged(false);
        arrow.getStyleClass().setAll(STYLE_CLASS);
    }

    @Override
    public Node getRoot() {
        return arrow;
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {
        // This skin is not intended to show joints.
    }

    @Override
    public void draw(final List<Point2D> points, final Map<GConnection, List<Point2D>> allPoints) {

        if (!points.equals(this.points) && points.size() == 2) {

            final Point2D start = points.get(0);
            final Point2D end = points.get(1);

            if (getConnection().getSource().getType().equals(TreeSkinConstants.TREE_OUTPUT_CONNECTOR)) {
                ArrowUtils.draw(arrow, start, end, OFFSET_DISTANCE);
            } else {
                ArrowUtils.draw(arrow, end, start, OFFSET_DISTANCE);
            }

            this.points = points;
        }
    }
}
