/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;

import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.shape.MoveTo;

/**
 * A connection segment that draws a gap at points where it intersects other connections.
 */
public class GappedConnectionSegment extends ConnectionSegment {

    private static final int GAP_SIZE = 4;

    /**
     * Creates a new {@link GappedConnectionSegment} instance.
     *
     * @param start the point where the segment starts
     * @param end the point where the segment ends
     * @param intersections the intersection-points of this segment with other connections
     */
    public GappedConnectionSegment(final Point2D start, final Point2D end, final double[] intersections)
    {
        super(start, end, intersections);
    }

    @Override
    protected void drawToFirstIntersection(final double intersection) {

        if (horizontal) {
            addHLineTo(intersection - sign * GAP_SIZE);
            addHGapTo(intersection + sign * GAP_SIZE);
        } else {
            addVLineTo(intersection - sign * GAP_SIZE);
            addVGapTo(intersection + sign * GAP_SIZE);
        }
    }

    @Override
    protected void drawBetweenIntersections(final double intersection, final double lastIntersection) {

        if (sign * (intersection - lastIntersection) > 2 * GAP_SIZE) {
            if (horizontal) {
                addHLineTo(intersection - sign * GAP_SIZE);
            } else {
                addVLineTo(intersection - sign * GAP_SIZE);
            }
        }

        if (horizontal) {
            addHGapTo(intersection + sign * GAP_SIZE);
        } else {
            addVGapTo(intersection + sign * GAP_SIZE);
        }
    }

    @Override
    protected void drawFromLastIntersection(final double intersection) {

        if (horizontal) {
            addHLineTo(getEnd().getX());
        } else {
            addVLineTo(getEnd().getY());
        }
    }

    /**
     * Moves horizontally along the path to the given x coordinate.
     *
     * @param x the x coordinate to move to
     */
    private void addHGapTo(final double x) {
        getPathElements().add(new MoveTo(GeometryUtils.moveOffPixel(x), GeometryUtils.moveOffPixel(getStart().getY())));
    }

    /**
     * Moves vertically along the path to the given y coordinate.
     *
     * @param y the y coordinate to move to
     */
    private void addVGapTo(final double y) {
        getPathElements().add(new MoveTo(GeometryUtils.moveOffPixel(getStart().getX()), GeometryUtils.moveOffPixel(y)));
    }
}
