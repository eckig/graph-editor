package de.tesis.dynaware.grapheditor.utils;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.Line;


/**
 * An arrow shape.
 *
 * <p>
 * This is a {@link Node} subclass and can be added to the JavaFX scene graph in
 * the usual way. Styling can be achieved via the CSS classes
 * <em>arrow-line</em> and <em>arrow-head</em>.
 * <p>
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>Arrow arrow = new Arrow();
 * arrow.setStart(10, 20);
 * arrow.setEnd(100, 150);
 * arrow.draw();</code>
 * </pre>
 *
 * </p>
 *
 */
public class Arrow extends Group
{

    private static final String STYLE_CLASS_LINE = "arrow-line"; //$NON-NLS-1$
    private static final String STYLE_CLASS_HEAD = "arrow-head"; //$NON-NLS-1$

    private final Line line = new Line();
    private final ArrowHead head = new ArrowHead();

    private double startX;
    private double startY;

    private double endX;
    private double endY;

    /**
     * Creates a new {@link Arrow}.
     */
    public Arrow()
    {
        line.getStyleClass().add(STYLE_CLASS_LINE);
        head.getStyleClass().add(STYLE_CLASS_HEAD);

        getChildren().addAll(line, head);
    }

    /**
     * Sets the width of the arrow-head.
     *
     * @param width
     *            the width of the arrow-head
     */
    public void setHeadWidth(final double width)
    {
        head.setWidth(width);
    }

    /**
     * Sets the length of the arrow-head.
     *
     * @param length
     *            the length of the arrow-head
     */
    public void setHeadLength(final double length)
    {
        head.setLength(length);
    }

    /**
     * Sets the radius of curvature of the {@link ArcTo} at the base of the
     * arrow-head.
     *
     * <p>
     * If this value is less than or equal to zero, a straight line will be
     * drawn instead. The default is -1.
     * </p>
     *
     * @param radius
     *            the radius of curvature of the arc at the base of the
     *            arrow-head
     */
    public void setHeadRadius(final double radius)
    {
        head.setRadiusOfCurvature(radius);
    }

    /**
     * Gets the start point of the arrow.
     *
     * @return the start {@link Point2D} of the arrow
     */
    public Point2D getStart()
    {
        return new Point2D(startX, startY);
    }

    /**
     * Sets the start position of the arrow.
     *
     * @param pStartX
     *            the x-coordinate of the start position of the arrow
     * @param pStartY
     *            the y-coordinate of the start position of the arrow
     */
    public void setStart(final double pStartX, final double pStartY)
    {
        startX = pStartX;
        startY = pStartY;
    }

    /**
     * Gets the start point of the arrow.
     *
     * @return the start {@link Point2D} of the arrow
     */
    public Point2D getEnd()
    {
        return new Point2D(endX, endY);
    }

    /**
     * Sets the end position of the arrow.
     *
     * @param pEndX
     *            the x-coordinate of the end position of the arrow
     * @param pEndY
     *            the y-coordinate of the end position of the arrow
     */
    public void setEnd(final double pEndX, final double pEndY)
    {
        endX = pEndX;
        endY = pEndY;
    }

    /**
     * Draws the arrow for its current size and position values.
     */
    public void draw()
    {
        final double deltaX = endX - startX;
        final double deltaY = endY - startY;

        final double angle = Math.atan2(deltaX, deltaY);

        final double headX = endX - head.getLength() / 2 * Math.sin(angle);
        final double headY = endY - head.getLength() / 2 * Math.cos(angle);

        line.setStartX(GeometryUtils.moveOffPixel(startX));
        line.setStartY(GeometryUtils.moveOffPixel(startY));
        line.setEndX(GeometryUtils.moveOffPixel(headX));
        line.setEndY(GeometryUtils.moveOffPixel(headY));

        head.setCenter(headX, headY);
        head.setAngle(Math.toDegrees(-angle));
        head.draw();
    }
}
