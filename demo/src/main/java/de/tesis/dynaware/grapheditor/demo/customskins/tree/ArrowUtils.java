package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import javafx.geometry.Point2D;
import de.tesis.dynaware.grapheditor.utils.Arrow;

/**
 * Utils for drawing arrows. Used by connection and tail skins.
 */
public class ArrowUtils {

    /**
     * Draws the given arrow from the start to end points with the given offset from either end.
     *
     * @param arrow an {@link Arrow} to be drawn
     * @param start the start position
     * @param end the end position
     * @param offset an offset from start and end positions
     */
    public static void draw(final Arrow arrow, final Point2D start, final Point2D end, final double offset) {

        final double deltaX = end.getX() - start.getX();
        final double deltaY = end.getY() - start.getY();

        final double angle = Math.atan2(deltaX, deltaY);

        final double startX = start.getX() + offset * Math.sin(angle);
        final double startY = start.getY() + offset * Math.cos(angle);

        final double endX = end.getX() - offset * Math.sin(angle);
        final double endY = end.getY() - offset * Math.cos(angle);

        arrow.setStart(startX, startY);
        arrow.setEnd(endX, endY);
        arrow.draw();

        if (Math.hypot(deltaX, deltaY) < 2 * offset) {
            arrow.setVisible(false);
        } else {
            arrow.setVisible(true);
        }
    }
}
