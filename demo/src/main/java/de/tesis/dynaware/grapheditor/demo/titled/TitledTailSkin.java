/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.titled;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

public class TitledTailSkin extends GTailSkin {

    private static final String STYLE_CLASS = "titled-tail";
    private static final String STYLE_CLASS_ENDPOINT = "titled-tail-endpoint";

    private static final double SIZE = 16;

    private static final double MINIMUM_START_SEGMENT = 20;
    private static final double MINIMUM_END_SEGMENT = 20;

    private final Polyline line = new Polyline();
    private final Rectangle endpoint = new Rectangle(SIZE, SIZE);
    private final Group group = new Group(line, endpoint);

    /**
     * Creates a new default tail skin instance.
     *
     * @param connector the {@link GConnector} the skin is being created for
     */
    public TitledTailSkin(final GConnector connector) {

        super(connector);

        line.getStyleClass().setAll(STYLE_CLASS);
        endpoint.getStyleClass().setAll(STYLE_CLASS_ENDPOINT);

        group.setManaged(false);
    }

    @Override
    public Node getRoot() {
        return group;
    }

    @Override
    public void draw(final Point2D start, final Point2D end) {

        final double startX = start.getX();
        final double startY = start.getY();

        final double endX = end.getX();
        final double endY = end.getY();

        line.getPoints().clear();
        line.getPoints().addAll(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

        final String type = getConnector().getType();

        final boolean isInput = type != null && type.contains("input");
        final boolean isOutput = type != null && type.contains("output");

        final boolean bendsBackward = isOutput && endX < startX + MINIMUM_START_SEGMENT + MINIMUM_END_SEGMENT;
        final boolean bendsForward = isInput && endX > startX - MINIMUM_START_SEGMENT - MINIMUM_END_SEGMENT;

        if (bendsBackward) {
            drawBendingBackward(startX, endX, startY, endY);
        } else if (bendsForward) {
            drawBendingForward(startX, endX, startY, endY);
        } else {
            drawDirect(startX, endX, startY, endY);
        }

        line.getPoints().addAll(GeometryUtils.moveOffPixel(endX), GeometryUtils.moveOffPixel(endY));

        endpoint.setLayoutX(GeometryUtils.moveOnPixel(endX) - SIZE / 2);
        endpoint.setLayoutY(GeometryUtils.moveOnPixel(endY) - SIZE / 2);
    }

    @Override
    public void draw(final Point2D start, final Point2D end, final List<Point2D> jointPositions) {

        final double startX = start.getX();
        final double startY = start.getY();

        final double endX = end.getX();
        final double endY = end.getY();

        line.getPoints().clear();
        line.getPoints().addAll(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

        for (int i = 0; i < jointPositions.size() - 1; i++) {

            final double jointX = jointPositions.get(i).getX();
            final double jointY = jointPositions.get(i).getY();

            line.getPoints().addAll(GeometryUtils.moveOffPixel(jointX), GeometryUtils.moveOffPixel(jointY));
        }

        final double lastJointX = jointPositions.get(jointPositions.size() - 1).getX();

        line.getPoints().addAll(GeometryUtils.moveOffPixel(lastJointX), GeometryUtils.moveOffPixel(endY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(endX), GeometryUtils.moveOffPixel(endY));

        endpoint.setLayoutX(GeometryUtils.moveOnPixel(endX) - SIZE / 2);
        endpoint.setLayoutY(GeometryUtils.moveOnPixel(endY) - SIZE / 2);
    }

    @Override
    public List<Point2D> allocateJointPositions() {

        final List<Point2D> jointPositions = new ArrayList<>();

        for (int i = 2; i < line.getPoints().size() - 2; i = i + 2) {

            final double x = GeometryUtils.moveOnPixel(line.getPoints().get(i));
            final double y = GeometryUtils.moveOnPixel(line.getPoints().get(i + 1));

            jointPositions.add(new Point2D(x, y));
        }

        return jointPositions;
    }

    @Override
    public void setEndpointVisible(final boolean visible) {
        endpoint.setVisible(visible);
    }

    /**
     * Draws the tail directly between the start and end points - adding only 2 joints.
     *
     * <p>
     * This is the simplest possible shape that can be drawn and still keep the tail rectangular.
     * </p>
     *
     * @param startX the start X position of the tail
     * @param endX the end X position of the tail
     * @param startY the start Y position of the tail
     * @param endY the end Y position of the tail
     */
    private void drawDirect(final double startX, final double endX, final double startY, final double endY) {

        // The -1 is because shaped connector skins are offset by +1.
        final double middleX = (startX + endX) / 2 - 1;

        line.getPoints().addAll(GeometryUtils.moveOffPixel(middleX), GeometryUtils.moveOffPixel(startY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(middleX), GeometryUtils.moveOffPixel(endY));
    }

    /**
     * Draws the tail starting out forwards and then bending backwards - adding 4 joints.
     *
     * @param startX the start X position of the tail
     * @param endX the end X position of the tail
     * @param startY the start Y position of the tail
     * @param endY the end Y position of the tail
     */
    private void drawBendingBackward(final double startX, final double endX, final double startY, final double endY) {

        final double rightX = startX + MINIMUM_START_SEGMENT;
        final double leftX = endX - MINIMUM_END_SEGMENT;
        final double middleY = (startY + endY) / 2;

        line.getPoints().addAll(GeometryUtils.moveOffPixel(rightX), GeometryUtils.moveOffPixel(startY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(rightX), GeometryUtils.moveOffPixel(middleY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(leftX), GeometryUtils.moveOffPixel(middleY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(leftX), GeometryUtils.moveOffPixel(endY));
    }

    /**
     * Draws the tail starting out backwards and then bending forwards - adding 4 joints.
     *
     * @param startX the start X position of the tail
     * @param endX the end X position of the tail
     * @param startY the start Y position of the tail
     * @param endY the end Y position of the tail
     */
    private void drawBendingForward(final double startX, final double endX, final double startY, final double endY) {

        final double leftX = startX - MINIMUM_END_SEGMENT;
        final double rightX = endX + MINIMUM_START_SEGMENT;

        final double middleY = (startY + endY) / 2;

        line.getPoints().addAll(GeometryUtils.moveOffPixel(leftX), GeometryUtils.moveOffPixel(startY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(leftX), GeometryUtils.moveOffPixel(middleY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(rightX), GeometryUtils.moveOffPixel(middleY));
        line.getPoints().addAll(GeometryUtils.moveOffPixel(rightX), GeometryUtils.moveOffPixel(endY));
    }
}
