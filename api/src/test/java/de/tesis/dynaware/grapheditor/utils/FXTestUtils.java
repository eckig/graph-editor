package de.tesis.dynaware.grapheditor.utils;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A set of utility methods for unit testing JavaFX classes.
 */
public class FXTestUtils {

    /**
     * Forces an update of the {@link Parent}'s layout.
     *
     * @param parent the {@link Parent} whose layout should be updated
     */
    public static void forceLayoutUpdate(final Parent parent) {
        parent.autosize();
        parent.snapshot(null, null);
        parent.layout();
    }

    /**
     * Drags the given {@link Node} by the given x and y displacements.
     *
     * @param node the {@link Node} which should be dragged
     * @param dx the x displacement to move the node by
     * @param dy the y displacement to move the node by
     */
    public static void dragBy(final Node node, final double dx, final double dy) {
        dragTo(node, node.getLayoutX() + dx, node.getLayoutY() + dy);
    }

    /**
     * Drags the given {@link Node} by the given x and y displacements.
     *
     * @param node the {@link Node} which should be dragged
     * @param offsetX the x offset on the node where the drag gesture starts from
     * @param offsetY the y offset on the node where the drag gesture starts from
     * @param dx the x displacement to move the {@link Node} by
     * @param dy the y displacement to move the {@link Node} by
     */
    public static void dragBy(final Node node, final double offsetX, final double offsetY, final double dx,
            final double dy) {

        dragTo(node, offsetX, offsetY, node.getLayoutX() + dx, node.getLayoutY() + dy);
    }

    /**
     * Drags the given {@link Node} from the start location to the end location.
     *
     * @param node the {@link Node} which should be dragged
     * @param endX the x coordinate of the targeted location
     * @param endY the y coordinate of the targeted location
     */
    public static void dragTo(final Node node, final double endX, final double endY) {
        dragTo(node, 0, 0, endX, endY);
    }

    /**
     * Drags the given {@link Node} from the start location to the end location.
     *
     * @param node the {@link Node} which should be dragged
     * @param offsetX the x offset on the node where the drag gesture starts from
     * @param offsetY the y offset on the node where the drag gesture starts from
     * @param endX the x coordinate of the targeted location
     * @param endY the y coordinate of the targeted location
     */
    public static void dragTo(final Node node, final double offsetX, final double offsetY, final double endX,
            final double endY) {

        final double startX = node.getLayoutX() + offsetX;
        final double startY = node.getLayoutY() + offsetY;

        final double dragX = startX + (endX - node.getLayoutX());
        final double dragY = startY + (endY - node.getLayoutY());

        final MouseEvent moved = createMouseEvent(MouseEvent.MOUSE_MOVED, startX, startY,false);
        final MouseEvent pressed = createMouseEvent(MouseEvent.MOUSE_PRESSED, startX, startY);
        final MouseEvent dragged = createMouseEvent(MouseEvent.MOUSE_DRAGGED, dragX, dragY);
        final MouseEvent released = createMouseEvent(MouseEvent.MOUSE_RELEASED, dragX, dragY);

        Event.fireEvent(node, moved);
        Event.fireEvent(node, pressed);
        Event.fireEvent(node, dragged);
        Event.fireEvent(node, released);
    }

    private static MouseEvent createMouseEvent(final EventType<MouseEvent> type, final double x, final double y)
    {
        return createMouseEvent(type, x, y, true);
    }

    /**
     * Creates a new {@link MouseEvent}.
     *
     * @param type
     *         the {@link EventType} of the event
     * @param x
     *         the x coordinate of the event
     * @param y
     *         the y coordinate of the event
     * @return the newly-created {@link MouseEvent}
     */
    private static MouseEvent createMouseEvent(final EventType<MouseEvent> type, final double x, final double y,
            final boolean pPrimaryButtonDown)
    {

        return new MouseEvent(type, // event type
                x, y, // x and y
                0, 0, MouseButton.PRIMARY, 0, // screen x, screen y, mouse button, click count
                false, false, false, false, pPrimaryButtonDown, false, false, false, false, false, null);
    }
}
