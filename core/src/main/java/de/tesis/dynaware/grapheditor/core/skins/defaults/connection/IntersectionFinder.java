/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

    private Map<GConnection, List<Point2D>> allPoints;
    private List<Point2D> points;

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
     * @param behind {@code true} to find intersections with the connections that are behind
     * @return a map of intersection points for each segment of the connection
     */
    public Map<Integer, List<Double>> find(final Map<GConnection, List<Point2D>> allPoints, final boolean behind) {

        this.allPoints = allPoints;
        points = allPoints.get(connection);

        final Map<Integer, List<Double>> intersections = new HashMap<>();

        for (int i = 0; i < points.size() - 1; i++) {

            final boolean isHorizontal = RectangularConnectionUtils.isSegmentHorizontal(connection, i);
            final List<Double> segmentIntersections = findSegmentIntersections(behind, i, isHorizontal);

            final boolean isDecreasing;
            if (isHorizontal) {
                isDecreasing = points.get(i + 1).getX() < points.get(i).getX();
            } else {
                isDecreasing = points.get(i + 1).getY() < points.get(i).getY();
            }

            if (!segmentIntersections.isEmpty()) {
                Collections.sort(segmentIntersections);
                if (isDecreasing) {
                    Collections.reverse(segmentIntersections);
                }
                intersections.put(i, segmentIntersections);
            }
        }

        return intersections;
    }

    /**
     * Finds the intersection points of other connections with a particular connection segment.
     * 
     * @param behind {@code true} to find intersections with the connections that are behind
     * @param index the index of the connection segment
     * @param isHorizontal {@code true} if the connection segment is horizontal
     * @return a list of positions along the segment where intersections occur
     */
    private List<Double> findSegmentIntersections(final boolean behind, final int index, final boolean isHorizontal) {

        final List<Double> segmentIntersections = new ArrayList<>();

        filterConnections(behind).forEach(otherConnection -> {

            final List<Point2D> otherPoints = allPoints.get(otherConnection);

            for (int j = 0; j < otherPoints.size() - 1; j++) {

                if (connection.equals(otherConnection) && (index > j ^ behind)) {
                    continue;
                }

                if (isHorizontal) {

                    final Point2D a = points.get(index);
                    final Point2D b = points.get(index + 1);
                    final Point2D c = otherPoints.get(j);
                    final Point2D d = otherPoints.get(j + 1);

                    if (GeometryUtils.checkIntersection(a, b, c, d)) {
                        segmentIntersections.add(c.getX());
                    }

                } else {

                    final Point2D a = otherPoints.get(j);
                    final Point2D b = otherPoints.get(j + 1);
                    final Point2D c = points.get(index);
                    final Point2D d = points.get(index + 1);

                    if (GeometryUtils.checkIntersection(a, b, c, d)) {
                        segmentIntersections.add(a.getY());
                    }
                }
            }
        });

        return segmentIntersections;
    }

    /**
     * Filters out some connections, because we want to either ignore connections in front or behind. We only want one
     * of the connections at an intersection to draw a detour graphic.
     * 
     * @param behind {@code true} to leave in connections behind this one and filter out those in front
     * @return a stream of connections with some filtered out
     */
    private Stream<GConnection> filterConnections(final boolean behind) {

        return allPoints.keySet().stream().filter(otherConnection -> {
            if (connection.equals(otherConnection)) {
                return true;
            } else if (behind) {
                return checkIfBehind(otherConnection);
            } else {
                return !checkIfBehind(otherConnection);
            }
        });
    }

    /**
     * Checks if the given connection is behind this one.
     *
     * @param other another {@link GConnection} instance
     * @return {@code true} if the given connection is behind this one
     */
    private boolean checkIfBehind(final GConnection other) {

        final Node node = skinLookup.lookupConnection(connection).getRoot();
        final Node otherNode = skinLookup.lookupConnection(other).getRoot();

        if (node.getParent() == null) {
            return false;
        } else {
            final int connectionIndex = node.getParent().getChildrenUnmodifiable().indexOf(node);
            final int otherIndex = node.getParent().getChildrenUnmodifiable().indexOf(otherNode);

            return otherIndex < connectionIndex;
        }
    }
}
