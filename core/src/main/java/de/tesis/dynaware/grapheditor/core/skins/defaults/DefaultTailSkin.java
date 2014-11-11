/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * The default tail skin.
 *
 * <p>
 * The styling is intended to match with the default connector and connection skins. See those classes for more
 * information.
 * </p>
 */
public class DefaultTailSkin extends GTailSkin {

    private static final String STYLE_CLASS = "default-tail";
    private static final String STYLE_CLASS_ENDPOINT_INPUT = "default-tail-endpoint-input";
    private static final String STYLE_CLASS_ENDPOINT_OUTPUT = "default-tail-endpoint-output";

    private static final String TOP_SIDE = "top";
    private static final String RIGHT_SIDE = "right";
    private static final String BOTTOM_SIDE = "bottom";
    private static final String LEFT_SIDE = "left";

    private static final double ENDPOINT_WIDTH = 25;
    private static final double ENDPOINT_HEIGHT = 25;

    private static final double MINIMUM_START_SEGMENT = 23;
    private static final double MINIMUM_END_SEGMENT = 27;

    private final Polyline line = new Polyline();
    private final Polygon endpoint = new Polygon();
    private final Group group = new Group(line, endpoint);

    /**
     * Creates a new default tail skin instance.
     *
     * @param connector the {@link GConnector} the skin is being created for
     */
    public DefaultTailSkin(final GConnector connector) {

        super(connector);

        performChecks();

        line.getStyleClass().setAll(STYLE_CLASS);

        // Tails coming from an 'input' connector have an 'output' endpoint and vice versa.
        if (connector.getType().contains(LEFT_SIDE)) {
            endpoint.getStyleClass().setAll(STYLE_CLASS_ENDPOINT_OUTPUT);
        } else if (connector.getType().contains(RIGHT_SIDE)) {
            endpoint.getStyleClass().setAll(STYLE_CLASS_ENDPOINT_INPUT);
        }

        endpoint.getPoints().addAll(new Double[] { 0D, 0D, ENDPOINT_WIDTH, ENDPOINT_HEIGHT / 2, 0D, ENDPOINT_HEIGHT });
        endpoint.setStrokeType(StrokeType.INSIDE);

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

        final boolean isInput = getConnector().getType().contains(LEFT_SIDE);
        final boolean isOutput = getConnector().getType().contains(RIGHT_SIDE);

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

        endpoint.setLayoutX(GeometryUtils.moveOffPixel(endX) - ENDPOINT_WIDTH / 2);
        endpoint.setLayoutY(GeometryUtils.moveOffPixel(endY) - ENDPOINT_HEIGHT / 2);
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

        endpoint.setLayoutX(GeometryUtils.moveOffPixel(endX) - ENDPOINT_WIDTH / 2);
        endpoint.setLayoutY(GeometryUtils.moveOffPixel(endY) - ENDPOINT_HEIGHT / 2);
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

        // The -2 is because default connector skins are offset by +2.
        final double middleX = (startX + endX) / 2 - 2;

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

    /**
     * Checks that the connector has the correct values to use this skin.
     */
    private void performChecks() {

    }
}
