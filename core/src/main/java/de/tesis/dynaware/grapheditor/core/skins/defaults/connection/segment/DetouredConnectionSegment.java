/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;

import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.shape.ArcTo;

/**
 * A connection segment that draws a detour (a small semicircle) at points where it intersects other connections.
 *
 * <p>
 * Does not look good when a horizontal detour is drawn on directly on top of a vertical detour, i.e. if many
 * connections intersect around the same point.
 * </p>
 */
public class DetouredConnectionSegment extends ConnectionSegment {

    private static final int DETOUR_RADIUS = 5;
    private static final int DETOUR_TOLERANCE = 20;

    /**
     * Creates a new {@link DetouredConnectionSegment} instance.
     *
     * @param start the point where the segment starts
     * @param end the point where the segment ends
     * @param intersections the intersection-points of this segment with other connections
     */
    public DetouredConnectionSegment(final Point2D start, final Point2D end, final double[] intersections)
    {
        super(start, end, intersections);
    }

    @Override
    protected void drawToFirstIntersection(final double intersection) {

        if (horizontal) {

            if (sign * (intersection - getStart().getX()) > DETOUR_RADIUS) {
                addHLineTo(intersection - sign * DETOUR_RADIUS);
            }
            addArcTo(intersection, getStart().getY() - DETOUR_RADIUS);

        } else {

            if (sign * (intersection - getStart().getY()) > DETOUR_RADIUS) {
                addVLineTo(intersection - sign * DETOUR_RADIUS);
            }
            addArcTo(getStart().getX() + DETOUR_RADIUS, intersection);
        }
    }

    @Override
    protected void drawBetweenIntersections(final double intersection, final double lastIntersection) {

        if (horizontal) {

            if (sign * (intersection - lastIntersection) <= DETOUR_TOLERANCE) {
                addHLineTo(intersection);
            } else {
                addArcTo(lastIntersection + sign * DETOUR_RADIUS, getStart().getY());
                addHLineTo(intersection - sign * DETOUR_RADIUS);
                addArcTo(intersection, getStart().getY() - DETOUR_RADIUS);
            }

        } else {

            if (sign * (intersection - lastIntersection) <= DETOUR_TOLERANCE) {
                addVLineTo(intersection);
            } else {
                addArcTo(getStart().getX(), lastIntersection + sign * DETOUR_RADIUS);
                addVLineTo(intersection - sign * DETOUR_RADIUS);
                addArcTo(getStart().getX() + DETOUR_RADIUS, intersection);
            }
        }
    }

    @Override
    protected void drawFromLastIntersection(final double intersection) {

        if (horizontal) {
            addArcTo(intersection + sign * DETOUR_RADIUS, getStart().getY());
            addHLineTo(getEnd().getX());
        } else {
            addArcTo(getStart().getX(), intersection + sign * DETOUR_RADIUS);
            addVLineTo(getEnd().getY());
        }
    }

    /**
     * Adds a circular detour arc to the path, to the given position.
     *
     * @param x the final x position of the arc
     * @param y the final y position of the arc
     */
    private void addArcTo(final double x, final double y) {

        final ArcTo arcTo = new ArcTo();

        arcTo.setRadiusX(DETOUR_RADIUS);
        arcTo.setRadiusY(DETOUR_RADIUS);
        arcTo.setSweepFlag(sign > 0);
        arcTo.setX(GeometryUtils.moveOffPixel(x));
        arcTo.setY(GeometryUtils.moveOffPixel(y));

        getPathElements().add(arcTo);
    }
}
