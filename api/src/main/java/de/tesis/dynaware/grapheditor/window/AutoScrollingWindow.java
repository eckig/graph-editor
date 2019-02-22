package de.tesis.dynaware.grapheditor.window;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * An extension of {@link PanningWindow} that adds an auto-scrolling mechanism.
 *
 * <p>
 * The auto-scrolling occurs when the mouse is dragged to the edge of the window. The scrolling rate increases the
 * longer the cursor is outside the window.
 * </p>
 */
public class AutoScrollingWindow extends PanningWindow
{
    private static final Duration JUMP_PERIOD = Duration.millis(25);

    private double baseJumpAmount = 1;
    private double maxJumpAmount = 50;
    private double jumpAmountIncreasePerJump = 0.5;
    private double insetToBeginScroll = 1;

    private Timeline timeline;
    private boolean isScrolling;
    private Point2D jumpDistance;

    private boolean autoScrollingEnabled = true;
    private int jumpsTaken;

    /**
     * Creates a new {@link AutoScrollingWindow}.
     */
    public AutoScrollingWindow()
    {
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
    }

    @Override
    protected void handlePanningMouseReleased(MouseEvent pEvent)
    {
        super.handlePanningMouseReleased(pEvent);
        endScrolling();
    }

    /**
     * Gets whether auto-scrolling is enabled.
     *
     * @return {@code true} if auto-scrolling is enabled
     */
    public boolean isAutoScrollingEnabled()
    {
        return autoScrollingEnabled;
    }

    /**
     * Sets whether auto-scrolling is enabled
     *
     * @param pAutoScrollingEnabled
     *            {@code true} to enable auto-scrolling, {@code false} to
     *            disable it
     */
    public void setAutoScrollingEnabled(final boolean pAutoScrollingEnabled)
    {
        autoScrollingEnabled = pAutoScrollingEnabled;
    }

    /**
     * Handles mouse-dragged events.
     *
     * @param event the mouse-dragged event object
     */
    private void handleMouseDragged(final MouseEvent event)
    {
        if (event.isPrimaryButtonDown() && event.getTarget() instanceof Node && !isScrollBar(event))
        {
            jumpDistance = getDistanceToJump(event.getX(), event.getY());

            if (jumpDistance == null)
            {
                jumpsTaken = 0;
            }
            else if (!isScrolling && isAutoScrollingEnabled())
            {
                startScrolling();
            }
        }
    }

    private boolean isScrollBar(final MouseEvent pEvent)
    {
        if (pEvent.getTarget() instanceof Node)
        {
            Node n = (Node) pEvent.getTarget();
            while (n != null)
            {
                if (n instanceof ScrollBar)
                {
                    return true;
                }
                n = n.getParent();
            }
        }
        return false;
    }

    /**
     * Gets the distance to jump based on the current cursor position.
     *
     * <p>
     * Returns null if the cursor is inside the window and no auto-scrolling should occur.
     * </p>
     *
     * @param cursorX the cursor-x position in this {@link PanningWindow}
     * @param cursorY the cursor-y position in this {@link PanningWindow}
     * @return the distance to jump, or null if no jump should occur
     */
    private Point2D getDistanceToJump(final double cursorX, final double cursorY)
    {
        double jumpX = 0;
        double jumpY = 0;

        final double baseAmount = baseJumpAmount;
        final double additionalAmount = jumpsTaken * jumpAmountIncreasePerJump;
        final double distance = Math.min(baseAmount + additionalAmount, maxJumpAmount);

        if (cursorX <= insetToBeginScroll)
        {
            jumpX = -distance;
        }
        else if (cursorX >= getWidth() - insetToBeginScroll)
        {
            jumpX = distance;
        }

        if (cursorY <= insetToBeginScroll)
        {
            jumpY = -distance;
        }
        else if (cursorY >= getHeight() - insetToBeginScroll)
        {
            jumpY = distance;
        }

        if (jumpX == 0 && jumpY == 0)
        {
            return null;
        }
        return new Point2D(Math.round(jumpX), Math.round(jumpY));
    }

    /**
     * Pans the window by the specified x and y values.
     *
     * <p>
     * The window cannot be panned outside the content. When the window 'hits
     * the edge' of the content it will stop.
     * </p>
     *
     * @param x
     *            the horizontal distance to move the window by
     * @param y
     *            the vertical distance to move the window by
     */
    private void panBy(final double x, final double y)
    {
        if (x != 0 && y != 0)
        {
            panTo(getContentX() + x, getContentY() + y);
        }
        else if (x != 0)
        {
            panToX(getContentX() + x);
        }
        else if (y != 0)
        {
            panToY(getContentY() + y);
        }
    }

    /**
     * Starts the auto-scrolling.
     */
    private void startScrolling()
    {
        isScrolling = true;
        jumpsTaken = 0;

        final KeyFrame frame = new KeyFrame(JUMP_PERIOD, event ->
        {
            if (isScrolling && jumpDistance != null)
            {
                panBy(jumpDistance.getX(), jumpDistance.getY());
                jumpsTaken++;
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    /**
     * Stops the auto-scrolling.
     */
    private void endScrolling()
    {
        isScrolling = false;

        if (timeline != null)
        {
            timeline.stop();
        }
    }
}
