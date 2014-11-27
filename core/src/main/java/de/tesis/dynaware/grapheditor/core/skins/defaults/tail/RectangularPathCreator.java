package de.tesis.dynaware.grapheditor.core.skins.defaults.tail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Side;

/**
 * Creates a rectangular connection path based on what sides of the nodes the connection starts and ends at.
 */
public class RectangularPathCreator {

    private static final double MINIMUM_EXTENSION = 30;

    /**
     * Creates a rectangular path from the start to the end positions.
     * 
     * <p>
     * Tries to travel outwards from the start and end nodes by at least a minimum amount.
     * </p>
     * 
     * @param startPosition the start position
     * @param endPosition the end position
     * @param startSide the side of the node the path starts from
     * @param endSide the side of the node the path travels to
     * @return a list of points specifying the path
     */
    public static List<Point2D> createPath(final Point2D startPosition, final Point2D endPosition,
            final Side startSide, final Side endSide) {

        if (startSide.equals(Side.LEFT) && endSide.equals(Side.LEFT)) {
            return connectLeftToLeft(startPosition, endPosition);

        } else if (startSide.equals(Side.LEFT) && endSide.equals(Side.RIGHT)) {
            return connectLeftToRight(startPosition, endPosition);

        } else if (startSide.equals(Side.LEFT) && endSide.equals(Side.TOP)) {
            return connectLeftToTop(startPosition, endPosition);

        } else if (startSide.equals(Side.LEFT) && endSide.equals(Side.BOTTOM)) {
            return connectLeftToBottom(startPosition, endPosition);

        } else if (startSide.equals(Side.RIGHT) && endSide.equals(Side.LEFT)) {
            return reverse(connectLeftToRight(endPosition, startPosition));

        } else if (startSide.equals(Side.RIGHT) && endSide.equals(Side.RIGHT)) {
            return connectRightToRight(startPosition, endPosition);

        } else if (startSide.equals(Side.RIGHT) && endSide.equals(Side.TOP)) {
            return connectRightToTop(startPosition, endPosition);

        } else if (startSide.equals(Side.RIGHT) && endSide.equals(Side.BOTTOM)) {
            return connectRightToBottom(startPosition, endPosition);

        } else if (startSide.equals(Side.TOP) && endSide.equals(Side.LEFT)) {
            return reverse(connectLeftToTop(endPosition, startPosition));

        } else if (startSide.equals(Side.TOP) && endSide.equals(Side.RIGHT)) {
            return reverse(connectRightToTop(endPosition, startPosition));

        } else if (startSide.equals(Side.TOP) && endSide.equals(Side.TOP)) {
            return connectTopToTop(startPosition, endPosition);

        } else if (startSide.equals(Side.TOP) && endSide.equals(Side.BOTTOM)) {
            return connectTopToBottom(startPosition, endPosition);

        } else if (startSide.equals(Side.BOTTOM) && endSide.equals(Side.LEFT)) {
            return reverse(connectLeftToBottom(endPosition, startPosition));

        } else if (startSide.equals(Side.BOTTOM) && endSide.equals(Side.RIGHT)) {
            return reverse(connectRightToBottom(endPosition, startPosition));

        } else if (startSide.equals(Side.BOTTOM) && endSide.equals(Side.TOP)) {
            return reverse(connectTopToBottom(endPosition, startPosition));

        } else {
            return connectBottomToBottom(startPosition, endPosition);
        }
    }

    /**
     * Connects the start point on the left side of a node to an end point on the left side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectLeftToLeft(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        final double minX = Math.min(start.getX(), end.getX());
        addPoint(path, minX - MINIMUM_EXTENSION, start.getY());
        addPoint(path, minX - MINIMUM_EXTENSION, end.getY());

        return path;
    }

    /**
     * Connects the start point on the left side of a node to an end point on the right side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectLeftToRight(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getX() >= end.getX() + 2 * MINIMUM_EXTENSION) {
            final double averageX = (start.getX() + end.getX()) / 2;
            addPoint(path, averageX, start.getY());
            addPoint(path, averageX, end.getY());
        } else {
            final double averageY = (start.getY() + end.getY()) / 2;
            addPoint(path, start.getX() - MINIMUM_EXTENSION, start.getY());
            addPoint(path, start.getX() - MINIMUM_EXTENSION, averageY);
            addPoint(path, end.getX() + MINIMUM_EXTENSION, averageY);
            addPoint(path, end.getX() + MINIMUM_EXTENSION, end.getY());
        }

        return path;
    }

    /**
     * Connects the start point on the left side of a node to an end point on the top side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectLeftToTop(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getX() > end.getX() + MINIMUM_EXTENSION) {
            if (start.getY() < end.getY() - MINIMUM_EXTENSION) {
                addPoint(path, end.getX(), start.getY());
            } else {
                final double averageX = (start.getX() + end.getX()) / 2;
                addPoint(path, averageX, start.getY());
                addPoint(path, averageX, end.getY() - MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() - MINIMUM_EXTENSION);
            }
        } else {
            if (start.getY() < end.getY() - MINIMUM_EXTENSION) {
                final double averageY = (start.getY() + end.getY()) / 2;
                addPoint(path, start.getX() - MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() - MINIMUM_EXTENSION, averageY);
                addPoint(path, end.getX(), averageY);
            } else {
                addPoint(path, start.getX() - MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() - MINIMUM_EXTENSION, end.getY() - MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() - MINIMUM_EXTENSION);
            }
        }

        return path;
    }

    /**
     * Connects the start point on the left side of a node to an end point on the bottom side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectLeftToBottom(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getX() > end.getX() + MINIMUM_EXTENSION) {
            if (start.getY() > end.getY() + MINIMUM_EXTENSION) {
                addPoint(path, end.getX(), start.getY());
            } else {
                final double averageX = (start.getX() + end.getX()) / 2;
                addPoint(path, averageX, start.getY());
                addPoint(path, averageX, end.getY() + MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() + MINIMUM_EXTENSION);
            }
        } else {
            if (start.getY() > end.getY() + MINIMUM_EXTENSION) {
                final double averageY = (start.getY() + end.getY()) / 2;
                addPoint(path, start.getX() - MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() - MINIMUM_EXTENSION, averageY);
                addPoint(path, end.getX(), averageY);
            } else {
                addPoint(path, start.getX() - MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() - MINIMUM_EXTENSION, end.getY() + MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() + MINIMUM_EXTENSION);
            }
        }

        return path;
    }

    /**
     * Connects the start point on the lerightft side of a node to an end point on the right side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectRightToRight(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        final double maxX = Math.max(start.getX(), end.getX());
        addPoint(path, maxX + MINIMUM_EXTENSION, start.getY());
        addPoint(path, maxX + MINIMUM_EXTENSION, end.getY());

        return path;
    }

    /**
     * Connects the start point on the right side of a node to an end point on the top side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectRightToTop(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getX() < end.getX() - MINIMUM_EXTENSION) {
            if (start.getY() < end.getY() - MINIMUM_EXTENSION) {
                addPoint(path, end.getX(), start.getY());
            } else {
                final double averageX = (start.getX() + end.getX()) / 2;
                addPoint(path, averageX, start.getY());
                addPoint(path, averageX, end.getY() - MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() - MINIMUM_EXTENSION);
            }
        } else {
            if (start.getY() < end.getY() - MINIMUM_EXTENSION) {
                final double averageY = (start.getY() + end.getY()) / 2;
                addPoint(path, start.getX() + MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() + MINIMUM_EXTENSION, averageY);
                addPoint(path, end.getX(), averageY);
            } else {
                addPoint(path, start.getX() + MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() + MINIMUM_EXTENSION, end.getY() - MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() - MINIMUM_EXTENSION);
            }
        }

        return path;
    }

    /**
     * Connects the start point on the right side of a node to an end point on the bottom side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectRightToBottom(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getX() < end.getX() - MINIMUM_EXTENSION) {
            if (start.getY() > end.getY() + MINIMUM_EXTENSION) {
                addPoint(path, end.getX(), start.getY());
            } else {
                final double averageX = (start.getX() + end.getX()) / 2;
                addPoint(path, averageX, start.getY());
                addPoint(path, averageX, end.getY() + MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() + MINIMUM_EXTENSION);
            }
        } else {
            if (start.getY() > end.getY() + MINIMUM_EXTENSION) {
                final double averageY = (start.getY() + end.getY()) / 2;
                addPoint(path, start.getX() + MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() + MINIMUM_EXTENSION, averageY);
                addPoint(path, end.getX(), averageY);
            } else {
                addPoint(path, start.getX() + MINIMUM_EXTENSION, start.getY());
                addPoint(path, start.getX() + MINIMUM_EXTENSION, end.getY() + MINIMUM_EXTENSION);
                addPoint(path, end.getX(), end.getY() + MINIMUM_EXTENSION);
            }
        }

        return path;
    }

    /**
     * Connects the start point on the top side of a node to an end point on the top side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectTopToTop(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        final double minY = Math.min(start.getY(), end.getY());
        addPoint(path, start.getX(), minY - MINIMUM_EXTENSION);
        addPoint(path, end.getX(), minY - MINIMUM_EXTENSION);

        return path;
    }

    /**
     * Connects the start point on the top side of a node to an end point on the bottom side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectTopToBottom(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        if (start.getY() >= end.getY() + 2 * MINIMUM_EXTENSION) {
            final double averageY = (start.getY() + end.getY()) / 2;
            addPoint(path, start.getX(), averageY);
            addPoint(path, end.getX(), averageY);
        } else {
            final double averageX = (start.getX() + end.getX()) / 2;
            addPoint(path, start.getX(), start.getY() - MINIMUM_EXTENSION);
            addPoint(path, averageX, start.getY() - MINIMUM_EXTENSION);
            addPoint(path, averageX, end.getY() + MINIMUM_EXTENSION);
            addPoint(path, end.getX(), end.getY() + MINIMUM_EXTENSION);
        }

        return path;
    }

    /**
     * Connects the start point on the bottom side of a node to an end point on the bottom side of a node.
     * 
     * @param start the start position
     * @param end the end position
     * @return a list of points connecting the start and end
     */
    private static List<Point2D> connectBottomToBottom(final Point2D start, final Point2D end) {

        final List<Point2D> path = new ArrayList<>();

        final double maxY = Math.max(start.getY(), end.getY());
        addPoint(path, start.getX(), maxY + MINIMUM_EXTENSION);
        addPoint(path, end.getX(), maxY + MINIMUM_EXTENSION);

        return path;
    }

    /**
     * Adds a point with the given x and y values to the path.
     * 
     * @param path a list of points
     * @param x the x coordinate for the new point
     * @param y the y coordinate for the new point
     */
    private static void addPoint(final List<Point2D> path, final double x, final double y) {
        path.add(new Point2D(x, y));
    }

    /**
     * Reverses a list of points and returns it.
     * 
     * @param points the list of points to be reversed
     * @return the reversed list of points
     */
    private static List<Point2D> reverse(final List<Point2D> points) {
        Collections.reverse(points);
        return points;
    }
}
