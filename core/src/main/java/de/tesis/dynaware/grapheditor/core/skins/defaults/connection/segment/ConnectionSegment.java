/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * A single segment of the default connection skin.
 *
 * <p>
 * A segment is a horizontal or vertical line either (a) between two joints, or (b) between a connector and a joint.
 * </p>
 */
public abstract class ConnectionSegment {

    // True for horizontal segment, false for vertical segment.
    protected final boolean horizontal;

    // +1 if position coordinate is increasing, -1 if it is decreasing.
    protected final int sign;

    private static final int EDGE_OFFSET = 5;

    private final List<PathElement> pathElements = new ArrayList<>();
    private final Point2D start;
    private final Point2D end;
    private final List<Double> intersections;

    /**
     * Creates a new connection segment for the given start and end points.
     *
     * @param start the point where the segment starts
     * @param end the point where the segment ends
     * @param intersections the intersection-points of this segment with other connections
     */
    public ConnectionSegment(final Point2D start, final Point2D end, final List<Double> intersections) {

        this.start = start;
        this.end = end;
        this.intersections = intersections;

        horizontal = start.getY() == end.getY();

        if (horizontal) {
            sign = start.getX() < end.getX() ? 1 : -1;
        } else {
            sign = start.getY() < end.getY() ? 1 : -1;
        }

        filterIntersections();
    }

    /**
     * Draws this connection segment.
     *
     * <p>
     * Draws a <b>detour</b> for each of the other connections that vertically intersect this segment, provided there is
     * enough room to do so. Does not draw a detour at points where a connection intersects itself.
     * </p>
     */
    public void draw() {

        if (!intersections.isEmpty()) {
            drawToFirstIntersection(intersections.get(0));
            drawBetweenIntersections();
            drawFromLastIntersection(intersections.get(intersections.size() - 1));
        } else {
            drawStraight();
        }
    }

    /**
     * Returns the {@link PathElement path elements} this connection segment is made of.
     *
     * @return the path elements this segment is made of
     */
    public List<PathElement> getPathElements() {
        return pathElements;
    }

    /**
     * Returns the start {@link Point2D point} of this connection segment.
     *
     * @return the start point of this connection segment
     */
    public Point2D getStart() {
        return start;
    }

    /**
     * Returns the end {@link Point2D point} of this connection segment.
     *
     * @return the end point of this connection segment
     */
    public Point2D getEnd() {
        return end;
    }

    /**
     * Draws the path elements for this connection segment from the start to the first intersection.
     *
     * @param intersection the intersection position to take into account when drawing this element
     */
    protected abstract void drawToFirstIntersection(final double intersection);

    /**
     * Draws the intermediate path elements for this connection segment between two intersections.
     *
     * @param intersection the intersection position to take into account when drawing this element
     * @param lastIntersection the previous intersection position to take into account when drawing this element
     */
    protected abstract void drawBetweenIntersections(final double intersection, final double lastIntersection);

    /**
     * Draws the path elements for this connection segment from the last intersection to the end.
     *
     * @param intersection the intersection position to take into account when drawing this element
     */
    protected abstract void drawFromLastIntersection(final double intersection);

    /**
     * Adds a horizontal line to the path, to the given position.
     *
     * @param x the final x position of the line
     */
    protected void addHLineTo(final double x) {
        pathElements.add(new HLineTo(GeometryUtils.moveOffPixel(x)));
    }

    /**
     * Adds a vertical line to the path, to the given position.
     *
     * @param y the final y position of the line
     */
    protected void addVLineTo(final double y) {
        pathElements.add(new VLineTo(GeometryUtils.moveOffPixel(y)));
    }

    /**
     * Removes all intersections that are too close to the edge of the segment to draw properly.
     */
    private void filterIntersections() {

        final List<Double> intersectionsToIgnore = new ArrayList<>();

        for (final Double intersection : intersections) {
            if (isTooCloseToTheEdge(intersection)) {
                intersectionsToIgnore.add(intersection);
            }
        }
        intersections.removeAll(intersectionsToIgnore);
    }

    /**
     * Checks if a given intersection is too close to the edge of the segment to draw properly.
     *
     * @param intersection the intersection to check
     * @return true if the given intersection is too close to the edge, false otherwise
     */
    private boolean isTooCloseToTheEdge(final double intersection) {

        final double startCoordinate = horizontal ? start.getX() : start.getY();
        final double endCoordinate = horizontal ? end.getX() : end.getY();

        final boolean tooCloseToStart = sign * (intersection - startCoordinate) < EDGE_OFFSET;
        final boolean tooCloseToEnd = sign * (endCoordinate - intersection) < EDGE_OFFSET;

        return tooCloseToStart || tooCloseToEnd;
    }

    /**
     * Draws all path elements between all intersections in this connection segment.
     */
    private void drawBetweenIntersections() {

        for (int i = 1; i < intersections.size(); i++) {
            final double intersection = intersections.get(i);
            final double lastIntersection = intersections.get(i - 1);
            drawBetweenIntersections(intersection, lastIntersection);
        }
    }

    /**
     * Draws a straight line all the way to the end of the segment.
     */
    private void drawStraight() {

        if (horizontal) {
            addHLineTo(end.getX());
        } else {
            addVLineTo(end.getY());
        }
    }
}
