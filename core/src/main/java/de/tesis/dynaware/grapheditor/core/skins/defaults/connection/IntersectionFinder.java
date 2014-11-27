/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.RectangularConnectionUtils;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * Responsible for finding the intersection points between a connection and other connections.
 */
public class IntersectionFinder {

    private final GConnection connection;
    private SkinLookup skinLookup;

    /**
     * Creates a new {@link IntersectionFinder} instance for the given connection.
     *
     * @param connection a {@link GConnection} instance
     */
    public IntersectionFinder(final GConnection connection) {
        this.connection = connection;
    }

    /**
     * Sets the skin lookup instance currently in use by the graph editor
     *
     * @param skinLookup a {@link SkinLookup} instance
     */
    public void setSkinLookup(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
    }

    /**
     * Finds the intersection points of the connection with all other connections that are in front of / behind it.
     *
     * @param allPoints the map of all current points of all connections in the model
     * @param behind {@code true} for intersections with connections behind this one, {@code false} for those in front
     * @return a map of intersection points for each segment of the connection
     */
    public Map<Integer, List<Double>> find(final Map<GConnection, List<Point2D>> allPoints, final boolean behind) {

        final List<Point2D> points = allPoints.get(connection);

        Map<Integer, List<Double>> intersections = null;

        for (int i = 0; i < points.size() - 1; i++) {

            List<Double> segmentIntersections = null;

            if (RectangularConnectionUtils.isSegmentHorizontal(connection, i)) {

                for (final GConnection otherConnection : allPoints.keySet()) {

                    final boolean ignoreConnection = checkInFront(otherConnection) ^ behind;

                    if (otherConnection.equals(connection) || ignoreConnection) {
                        continue;
                    }

                    final List<Point2D> otherPoints = allPoints.get(otherConnection);

                    for (int j = 0; j < otherPoints.size() - 1; j++) {

                        if (!RectangularConnectionUtils.isSegmentHorizontal(otherConnection, j)) {
                            final Point2D a = points.get(i);
                            final Point2D b = points.get(i + 1);
                            final Point2D c = otherPoints.get(j);
                            final Point2D d = otherPoints.get(j + 1);

                            if (GeometryUtils.checkIntersection(a, b, c, d)) {

                                if (segmentIntersections == null) {
                                    segmentIntersections = new ArrayList<>();
                                }
                                segmentIntersections.add(c.getX());
                            }
                        }
                    }
                }

                if (segmentIntersections != null) {

                    Collections.sort(segmentIntersections);

                    if (points.get(i + 1).getX() < points.get(i).getX()) {
                        Collections.reverse(segmentIntersections);
                    }
                }

            } else {

                for (final GConnection otherConnection : allPoints.keySet()) {

                    final boolean ignoreConnection = checkInFront(otherConnection) ^ behind;

                    if (otherConnection.equals(connection) || ignoreConnection) {
                        continue;
                    }

                    final List<Point2D> otherPoints = allPoints.get(otherConnection);

                    for (int j = 0; j < otherPoints.size() - 1; j++) {

                        if (RectangularConnectionUtils.isSegmentHorizontal(otherConnection, j)) {
                            final Point2D a = otherPoints.get(j);
                            final Point2D b = otherPoints.get(j + 1);
                            final Point2D c = points.get(i);
                            final Point2D d = points.get(i + 1);

                            if (GeometryUtils.checkIntersection(a, b, c, d)) {

                                if (segmentIntersections == null) {
                                    segmentIntersections = new ArrayList<>();
                                }

                                segmentIntersections.add(a.getY());
                            }
                        }
                    }
                }

                if (segmentIntersections != null) {

                    Collections.sort(segmentIntersections);

                    if (points.get(i + 1).getY() < points.get(i).getY()) {
                        Collections.reverse(segmentIntersections);
                    }
                }
            }

            if (segmentIntersections != null) {
                if (intersections == null) {
                    intersections = new HashMap<>();
                }
                intersections.put(i, segmentIntersections);
            }
        }

        return intersections;
    }

    /**
     * Checks if this connection is in front of another connection in the z-direction.
     *
     * @param other another {@link GConnection} instance
     *
     * @return {@code true} if the first connection is in front of the second, {@code false} otherwise
     */
    private boolean checkInFront(final GConnection other) {

        final Node connectionNode = skinLookup.lookupConnection(connection).getRoot();
        final Node otherNode = skinLookup.lookupConnection(other).getRoot();

        if (connectionNode.getParent() == null) {
            return false;
        }

        for (final Node sibling : connectionNode.getParent().getChildrenUnmodifiable()) {

            if (sibling.equals(connectionNode)) {
                return false;
            } else if (sibling.equals(otherNode)) {
                return true;
            }
        }
        return false;
    }
}
