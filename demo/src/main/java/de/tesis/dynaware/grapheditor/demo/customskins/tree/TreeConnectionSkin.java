/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.Arrow;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

/**
 * Connection skin for the 'tree-like' graph. Pretty much just an arrow.
 */
public class TreeConnectionSkin extends GConnectionSkin {

    private static final String STYLE_CLASS = "tree-connection"; //$NON-NLS-1$
    private static final String STYLE_CLASS_BACKGROUND = "tree-connection-background"; //$NON-NLS-1$
    private static final String STYLE_CLASS_SELECTION_HALO = "tree-connection-selection-halo"; //$NON-NLS-1$

    private static final double OFFSET_FROM_CONNECTOR = 15;
    private static final double HALO_BREADTH_OFFSET = 5;
    private static final double HALO_LENGTH_OFFSET_START = 1;
    private static final double HALO_LENGTH_OFFSET_END = 12;

    private final Arrow arrow = new Arrow();
    private final Arrow background = new Arrow();
    private final Line haloFirstSide = new Line();
    private final Line haloSecondSide = new Line();
    private final Group selectionHalo = new Group(haloFirstSide, haloSecondSide);
    private final Group root = new Group(background, selectionHalo, arrow);

    /**
     * Creates a new {@link TreeConnectionSkin} instance.
     *
     * @param connection the {@link GConnection} that this skin is representing
     */
    public TreeConnectionSkin(final GConnection connection) {

        super(connection);

        arrow.setManaged(false);
        arrow.getStyleClass().setAll(STYLE_CLASS);

        background.setManaged(false);
        background.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);

        root.setOnMousePressed(this::handleMousePressed);
        root.setOnMouseDragged(this::handleMouseDragged);

        haloFirstSide.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
        haloSecondSide.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
        selectionHalo.setVisible(false);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {
        // This skin is not intended to show joints.
    }

    @Override
    public void draw(final Map<GConnectionSkin, Point2D[]> allPoints)
    {
        final Point2D[] points = allPoints == null ? null : allPoints.get(this);
        if (points != null && points.length == 2)
        {
            final Point2D start = points[0];
            final Point2D end = points[1];

            if (getItem().getSource().getType().equals(TreeSkinConstants.TREE_OUTPUT_CONNECTOR))
            {
                ArrowUtils.draw(arrow, start, end, OFFSET_FROM_CONNECTOR);
                ArrowUtils.draw(background, start, end, OFFSET_FROM_CONNECTOR);
            }
            else
            {
                ArrowUtils.draw(arrow, end, start, OFFSET_FROM_CONNECTOR);
                ArrowUtils.draw(background, start, end, OFFSET_FROM_CONNECTOR);
            }
        }

        if (isSelected())
        {
            drawSelectionHalo();
        }
    }

    /**
     * Handles mouse-pressed events on the connection skin to select / de-select the connection.
     *
     * @param event the mouse-pressed event
     */
	private void handleMousePressed(final MouseEvent event) {

		final GraphEditor editor = getGraphEditor();
		if (editor == null) {
			return;
		}

		if (event.isShortcutDown()) {
			if (isSelected()) {
				editor.getSelectionManager().clearSelection(getItem());
			} else {
				editor.getSelectionManager().select(getItem());
			}
		} else if (!isSelected()) {
			getGraphEditor().getSelectionManager().clearSelection();
			editor.getSelectionManager().select(getItem());
		}

		event.consume();
	}

    /**
     * Handles mouse-dragged events on the connection skin. Consumes the event so it doesn't reach the view.
     *
     * @param event the mouse-dragged event
     */
    private void handleMouseDragged(final MouseEvent event) {
        event.consume();
    }


    @Override
    protected void selectionChanged(final boolean selected) {
        selectionHalo.setVisible(selected);
        if (selected) {
            drawSelectionHalo();
        }
    }

    /**
     * Draws the 'selection halo' that indicates that the connection is selected.
     */
    private void drawSelectionHalo() {

        final Point2D arrowStart = arrow.getStart();
        final Point2D arrowEnd = arrow.getEnd();

        final double deltaX = arrowEnd.getX() - arrowStart.getX();
        final double deltaY = arrowEnd.getY() - arrowStart.getY();

        final double angle = Math.atan2(deltaX, deltaY);

        final double breadthOffsetX = HALO_BREADTH_OFFSET * Math.cos(angle);
        final double breadthOffsetY = HALO_BREADTH_OFFSET * Math.sin(angle);

        final double lengthOffsetStartX = HALO_LENGTH_OFFSET_START * Math.sin(angle);
        final double lengthOffsetStartY = HALO_LENGTH_OFFSET_START * Math.cos(angle);

        final double lengthOffsetEndX = HALO_LENGTH_OFFSET_END * Math.sin(angle);
        final double lengthOffsetEndY = HALO_LENGTH_OFFSET_END * Math.cos(angle);

        haloFirstSide.setStartX(GeometryUtils.moveOffPixel(arrowStart.getX() - breadthOffsetX + lengthOffsetStartX));
        haloFirstSide.setStartY(GeometryUtils.moveOffPixel(arrowStart.getY() + breadthOffsetY + lengthOffsetStartY));

        haloSecondSide.setStartX(GeometryUtils.moveOffPixel(arrowStart.getX() + breadthOffsetX + lengthOffsetStartX));
        haloSecondSide.setStartY(GeometryUtils.moveOffPixel(arrowStart.getY() - breadthOffsetY + lengthOffsetStartY));

        haloFirstSide.setEndX(GeometryUtils.moveOffPixel(arrowEnd.getX() - breadthOffsetX - lengthOffsetEndX));
        haloFirstSide.setEndY(GeometryUtils.moveOffPixel(arrowEnd.getY() + breadthOffsetY - lengthOffsetEndY));

        haloSecondSide.setEndX(GeometryUtils.moveOffPixel(arrowEnd.getX() + breadthOffsetX - lengthOffsetEndX));
        haloSecondSide.setEndY(GeometryUtils.moveOffPixel(arrowEnd.getY() - breadthOffsetY - lengthOffsetEndY));
    }
}
