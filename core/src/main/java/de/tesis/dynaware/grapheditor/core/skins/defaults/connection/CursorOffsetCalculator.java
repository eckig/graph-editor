/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.VLineTo;
import de.tesis.dynaware.grapheditor.core.connections.RectangularConnections;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.ConnectionSegment;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * Helper class for calculating the offset of the cursor to a default connection skin.
 */
public class CursorOffsetCalculator {

    private final GConnection connection;
    private final Path path;
    private final Path backgroundPath;
    private final List<ConnectionSegment> connectionSegments;

    // Temporary variables used during calculation.
    private double minOffsetX;
    private double minOffsetY;
    private double currentX;
    private double currentY;

    /**
     * Creates a new cursor offset calculator instance for a default connection skin.
     *
     * @param path the connection's path
     * @param backgroundPath the connection's background path
     * @param connectionSegments the connection's list of segments
     */
    public CursorOffsetCalculator(final GConnection connection, final Path path, final Path backgroundPath,
            final List<ConnectionSegment> connectionSegments) {

        this.connection = connection;
        this.path = path;
        this.backgroundPath = backgroundPath;
        this.connectionSegments = connectionSegments;
    }

    /**
     * Gets the horizontal or vertical offset to the connection for the given cursor position.
     *
     * @param cursorSceneX the cursor x-position in the scene
     * @param cursorSceneY the cursor y-position in the scene
     * @return an offset to the nearest connection, or {@code null} if the cursor is too far away
     */
    public Point2D getOffset(final double cursorSceneX, final double cursorSceneY) {

        // Scale factor only relevant if we are zoomed in.
        final double scaleFactor = backgroundPath.getLocalToSceneTransform().getMxx();

        // This will be used as the largest acceptable offset value.
        final double offsetBound = Math.ceil(backgroundPath.getStrokeWidth() / 2) * scaleFactor;

        minOffsetX = offsetBound + 1;
        minOffsetY = offsetBound + 1;

        currentX = ((MoveTo) path.getElements().get(0)).getX();
        currentY = ((MoveTo) path.getElements().get(0)).getY();

        for (int i = 1; i < path.getElements().size(); i++) {

            final PathElement pathElement = path.getElements().get(i);

            calculateOffset(pathElement, cursorSceneX, cursorSceneY, offsetBound);
        }

        if (minOffsetX > offsetBound && minOffsetY > offsetBound) {
            return null;
        } else if (Math.abs(minOffsetX) <= Math.abs(minOffsetY)) {
            return new Point2D(minOffsetX, 0);
        } else {
            return new Point2D(0, minOffsetY);
        }
    }

    /**
     * Gets the index i of the connection segment that is closest to the given cursor position.
     *
     * @param cursorX the cursor X position
     * @param cursorY the cursor Y position
     * @return the index of the nearest connection segment
     */
    public int getNearestSegment(final double cursorX, final double cursorY) {

        int nearestIndex = -1;
        double nearestDistance = -1;

        for (int i = 0; i < connectionSegments.size(); i++) {

            final Point2D start = path.localToScene(connectionSegments.get(i).getStart());
            final Point2D end = path.localToScene(connectionSegments.get(i).getEnd());

            if (RectangularConnections.isSegmentHorizontal(connection, i)) {

                final boolean inRangeX = GeometryUtils.checkInRange(start.getX(), end.getX(), cursorX);
                final double distanceY = Math.abs(start.getY() - cursorY);

                if (inRangeX && (nearestDistance < 0 || distanceY < nearestDistance)) {
                    nearestIndex = i;
                    nearestDistance = distanceY;
                }
            } else {

                final boolean inRangeY = GeometryUtils.checkInRange(start.getY(), end.getY(), cursorY);
                final double distanceX = Math.abs(start.getX() - cursorX);

                if (inRangeY && (nearestDistance < 0 || distanceX < nearestDistance)) {
                    nearestIndex = i;
                    nearestDistance = distanceX;
                }
            }
        }

        return nearestIndex;
    }

    /**
     * Calculates the offset of the cursor to the given path element.
     *
     * <p>
     * If the offset is smaller than the current minimum variable, its value will be updated.
     * </p>
     *
     * @param pathElement a {@link PathElement} inside the connection path
     * @param cursorSceneX the x position of the cursor
     * @param cursorSceneY the y position of the cursor
     * @param offsetBound the maximum allowed offset value
     */
    private void calculateOffset(final PathElement pathElement, final double cursorSceneX, final double cursorSceneY,
            final double offsetBound) {

        final double currentSceneX = path.localToScene(currentX, currentY).getX();
        final double currentSceneY = path.localToScene(currentX, currentY).getY();

        if (pathElement instanceof HLineTo) {

            final HLineTo hLineTo = (HLineTo) pathElement;

            final double nextSceneX = path.localToScene(hLineTo.getX(), currentY).getX();
            final double possibleMinOffsetY = currentSceneY - cursorSceneY;

            final boolean inRangeX = GeometryUtils.checkInRange(currentSceneX, nextSceneX, cursorSceneX);
            final boolean cursorInRangeY = Math.abs(possibleMinOffsetY) < offsetBound;
            final boolean foundCloser = Math.abs(possibleMinOffsetY) < Math.abs(minOffsetY);

            if (inRangeX && cursorInRangeY && foundCloser) {
                minOffsetY = possibleMinOffsetY;
            }

            currentX = hLineTo.getX();

        } else if (pathElement instanceof ArcTo) {

            final ArcTo arcTo = (ArcTo) pathElement;

            currentX = arcTo.getX();
            currentY = arcTo.getY();

        } else if (pathElement instanceof VLineTo) {

            final VLineTo vLineTo = (VLineTo) pathElement;

            final double nextSceneY = path.localToScene(currentX, vLineTo.getY()).getY();
            final double possibleMinOffsetX = currentSceneX - cursorSceneX;

            final boolean cursorInRangeY = GeometryUtils.checkInRange(currentSceneY, nextSceneY, cursorSceneY);
            final boolean cursorInRangeX = Math.abs(possibleMinOffsetX) < offsetBound;
            final boolean foundCloser = Math.abs(possibleMinOffsetX) < Math.abs(minOffsetX);

            if (cursorInRangeY && cursorInRangeX && foundCloser) {
                minOffsetX = possibleMinOffsetX;
            }
            currentY = vLineTo.getY();
        }
    }
}
