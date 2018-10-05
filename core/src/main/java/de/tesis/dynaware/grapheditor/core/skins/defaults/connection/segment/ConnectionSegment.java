/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;


/**
 * A single segment of the default connection skin.
 *
 * <p>
 * A segment is a horizontal or vertical line either (a) between two joints, or
 * (b) between a connector and a joint.
 * </p>
 */
public abstract class ConnectionSegment
{

    // True for horizontal segment, false for vertical segment.
    protected final boolean horizontal;

    // +1 if position coordinate is increasing, -1 if it is decreasing.
    protected final int sign;

    private static final int EDGE_OFFSET = 5;

    private final List<PathElement> pathElements = new ArrayList<>();
    private final Point2D start;
    private final Point2D end;
    private final double[] intersections;

    /**
     * Creates a new connection segment for the given start and end points.
     *
     * @param start
     *            the point where the segment starts
     * @param end
     *            the point where the segment ends
     * @param intersections
     *            the intersection-points of this segment with other connections
     */
    public ConnectionSegment(final Point2D start, final Point2D end, final double[] intersections)
    {
        this.start = start;
        this.end = end;

        horizontal = start.getY() == end.getY();

        if (horizontal)
        {
            sign = start.getX() < end.getX() ? 1 : -1;
        }
        else
        {
            sign = start.getY() < end.getY() ? 1 : -1;
        }

        this.intersections = filterIntersections(intersections);
    }

    /**
     * Draws this connection segment.
     *
     * <p>
     * Draws a <b>detour</b> for each of the other connections that vertically
     * intersect this segment, provided there is enough room to do so. Does not
     * draw a detour at points where a connection intersects itself.
     * </p>
     */
    public void draw()
    {
        if (intersections != null && intersections.length > 0)
        {
            drawToFirstIntersection(intersections[0]);
            drawBetweenIntersections();
            drawFromLastIntersection(intersections[intersections.length - 1]);
        }
        else
        {
            drawStraight();
        }
    }

    /**
     * Returns the {@link PathElement path elements} this connection segment is
     * made of.
     *
     * @return the path elements this segment is made of
     */
    public List<PathElement> getPathElements()
    {
        return pathElements;
    }

    /**
     * Returns the start {@link Point2D point} of this connection segment.
     *
     * @return the start point of this connection segment
     */
    public Point2D getStart()
    {
        return start;
    }

    /**
     * Returns the end {@link Point2D point} of this connection segment.
     *
     * @return the end point of this connection segment
     */
    public Point2D getEnd()
    {
        return end;
    }

    /**
     * Draws the path elements for this connection segment from the start to the
     * first intersection.
     *
     * @param intersection
     *            the intersection position to take into account when drawing
     *            this element
     */
    protected abstract void drawToFirstIntersection(final double intersection);

    /**
     * Draws the intermediate path elements for this connection segment between
     * two intersections.
     *
     * @param intersection
     *            the intersection position to take into account when drawing
     *            this element
     * @param lastIntersection
     *            the previous intersection position to take into account when
     *            drawing this element
     */
    protected abstract void drawBetweenIntersections(final double intersection, final double lastIntersection);

    /**
     * Draws the path elements for this connection segment from the last
     * intersection to the end.
     *
     * @param intersection
     *            the intersection position to take into account when drawing
     *            this element
     */
    protected abstract void drawFromLastIntersection(final double intersection);

    /**
     * Adds a horizontal line to the path, to the given position.
     *
     * @param x
     *            the final x position of the line
     */
    protected void addHLineTo(final double x)
    {
        pathElements.add(new HLineTo(GeometryUtils.moveOffPixel(x)));
    }

    /**
     * Adds a vertical line to the path, to the given position.
     *
     * @param y
     *            the final y position of the line
     */
    protected void addVLineTo(final double y)
    {
        pathElements.add(new VLineTo(GeometryUtils.moveOffPixel(y)));
    }

    /**
     * Removes all intersections that are too close to the edge of the segment
     * to draw properly.
     */
    private double[] filterIntersections(final double[] pIntersections)
    {
        if (pIntersections == null || pIntersections.length == 0)
        {
            return null;
        }
        final double[] intersectionsFiltered = new double[pIntersections.length];
        int len = 0;
        for (final double intersection : pIntersections)
        {
            if (intersection != 0 && !isTooCloseToTheEdge(intersection))
            {
                intersectionsFiltered[len++] = intersection;
            }
        }

        if (len == 0)
        {
            return null;
        }
        if (len != pIntersections.length)
        {
            return Arrays.copyOf(intersectionsFiltered, len);
        }
        return pIntersections;
    }

    /**
     * Checks if a given intersection is too close to the edge of the segment to
     * draw properly.
     *
     * @param intersection
     *            the intersection to check
     * @return true if the given intersection is too close to the edge, false
     *         otherwise
     */
    private boolean isTooCloseToTheEdge(final double intersection)
    {

        final double startCoordinate = horizontal ? start.getX() : start.getY();
        final double endCoordinate = horizontal ? end.getX() : end.getY();

        final boolean tooCloseToStart = sign * (intersection - startCoordinate) < EDGE_OFFSET;
        final boolean tooCloseToEnd = sign * (endCoordinate - intersection) < EDGE_OFFSET;

        return tooCloseToStart || tooCloseToEnd;
    }

    /**
     * Draws all path elements between all intersections in this connection
     * segment.
     */
    private void drawBetweenIntersections()
    {
        for (int i = 1; i < intersections.length; i++)
        {
            final double intersection = intersections[i];
            final double lastIntersection = intersections[i - 1];
            drawBetweenIntersections(intersection, lastIntersection);
        }
    }

    /**
     * Draws a straight line all the way to the end of the segment.
     */
    private void drawStraight()
    {
        if (horizontal)
        {
            addHLineTo(end.getX());
        }
        else
        {
            addVLineTo(end.getY());
        }
    }
}
