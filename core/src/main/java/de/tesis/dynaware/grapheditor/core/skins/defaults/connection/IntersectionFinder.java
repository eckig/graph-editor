/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.RectangularConnectionUtils;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;


/**
 * Responsible for finding the intersection points between a connection and
 * other connections.
 */
public class IntersectionFinder
{

    /**
     * Finds the intersection points of the connection with all other
     * connections that are in front of / behind it.
     *
     * @param allPoints
     *            the map of all current points of all connections in the model
     * @param behind
     *            {@code true} to find intersections with the connections that
     *            are behind
     * @return array of intersection points for each segment of the connection
     */
    public static double[][] find(final GConnectionSkin pSkin, final Map<GConnectionSkin, List<Point2D>> allPoints,
            final boolean behind)
    {
        final List<Point2D> points = allPoints.get(pSkin);
        final double[][] intersections = new double[points.size()][];

        for (int i = 0; i < points.size() - 1; i++)
        {
            final boolean isHorizontal = RectangularConnectionUtils.isSegmentHorizontal(pSkin.getItem(), i);
            final double[] segmentIntersections = findSegmentIntersections(pSkin, allPoints, behind, i, isHorizontal);

            final boolean isDecreasing;
            if (isHorizontal)
            {
                isDecreasing = points.get(i + 1).getX() < points.get(i).getX();
            }
            else
            {
                isDecreasing = points.get(i + 1).getY() < points.get(i).getY();
            }

            if (segmentIntersections != null && segmentIntersections.length > 0)
            {
                Arrays.sort(segmentIntersections);
                if (isDecreasing)
                {
                    reverse(segmentIntersections);
                }
                intersections[i] = segmentIntersections;
            }
        }
        return intersections;
    }

    private static void reverse(final double[] array)
    {
        for (int i = 0; i < array.length / 2; i++)
        {
            double temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    /**
     * Finds the intersection points of other connections with a particular
     * connection segment.
     *
     * @param behind
     *            {@code true} to find intersections with the connections that
     *            are behind
     * @param index
     *            the index of the connection segment
     * @param isHorizontal
     *            {@code true} if the connection segment is horizontal
     * @return a list of positions along the segment where intersections occur
     */
    private static double[] findSegmentIntersections(final GConnectionSkin connection, final Map<GConnectionSkin, List<Point2D>> allPoints,
            final boolean behind, final int index, final boolean isHorizontal)
    {
        double[] segmentIntersections = null;
        int resultLen = 0;
        final List<Point2D> points = allPoints.get(connection);

        for (final Map.Entry<GConnectionSkin, List<Point2D>> entry : allPoints.entrySet())
        {
            if (!filterConnection(connection, behind, entry.getKey()))
            {
                continue;
            }

            final List<Point2D> otherPoints = entry.getValue();

            for (int j = 0; j < otherPoints.size() - 1; j++)
            {
                if (connection.equals(entry.getKey()) && index > j ^ behind)
                {
                    continue;
                }

                if (isHorizontal)
                {
                    final Point2D a = points.get(index);
                    final Point2D b = points.get(index + 1);
                    final Point2D c = otherPoints.get(j);
                    final Point2D d = otherPoints.get(j + 1);

                    if (GeometryUtils.checkIntersection(a, b, c, d))
                    {
                        if (segmentIntersections == null)
                        {
                            segmentIntersections = new double[5];
                        }
                        if (resultLen >= segmentIntersections.length)
                        {
                            segmentIntersections = Arrays.copyOf(segmentIntersections, segmentIntersections.length * 2);
                        }
                        segmentIntersections[resultLen++] = a.getY();
                    }
                }
                else
                {
                    final Point2D a = otherPoints.get(j);
                    final Point2D b = otherPoints.get(j + 1);
                    final Point2D c = points.get(index);
                    final Point2D d = points.get(index + 1);

                    if (GeometryUtils.checkIntersection(a, b, c, d))
                    {
                        if (segmentIntersections == null)
                        {
                            segmentIntersections = new double[5];
                        }
                        if (resultLen >= segmentIntersections.length)
                        {
                            segmentIntersections = Arrays.copyOf(segmentIntersections, segmentIntersections.length * 2);
                        }
                        segmentIntersections[resultLen++] = a.getY();
                    }
                }
            }
        }
        return segmentIntersections;
    }

    /**
     * Filters out some connections, because we want to either ignore
     * connections in front or behind. We only want one of the connections at an
     * intersection to draw a detour graphic.
     *
     * @param behind
     *            {@code true} to leave in connections behind this one and
     *            filter out those in front
     * @return a stream of connections with some filtered out
     */
    private static boolean filterConnection(final GConnectionSkin connection, final boolean behind, final GConnectionSkin otherConnection)
    {
        if (connection.equals(otherConnection))
        {
            return true;
        }
        else if (behind)
        {
            return checkIfBehind(connection, otherConnection);
        }
        else
        {
            return !checkIfBehind(connection, otherConnection);
        }
    }

    /**
     * Checks if the given connection is behind this one.
     *
     * @param other
     *            another {@link GConnection} instance
     * @return {@code true} if the given connection is behind this one
     */
    private static boolean checkIfBehind(final GConnectionSkin skin, final GConnectionSkin otherSkin)
    {
        if (skin == null || skin.getParentIndex() == -1)
        {
            return false;
        }
        else
        {
            final int otherIndex = otherSkin == null ? -1 : otherSkin.getParentIndex();
            return otherIndex < skin.getParentIndex();
        }
    }
}
