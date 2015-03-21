package de.tesis.dynaware.grapheditor.window;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * An extension of {@link PanningWindow} that adds an auto-scrolling mechanism.
 * 
 * <p>
 * The auto-scrolling occurs when the mouse is dragged to the edge of the window. The scrolling rate increases as the
 * cursor moves further outside the window.
 * </p>
 * 
 * <p>
 * The parameters controlling the scroll-rate can all be tweaked. Do so at your own risk.
 * </p>
 */
public class AutoScrollingWindow extends PanningWindow {

    private Timeline timeline;
    private MouseEvent currentDragEvent;
    private Node dragEventTarget;
    private boolean isScrolling;
    private Point2D jumpDistance;

    private boolean autoScrollingEnabled = true;
    private double jumpPeriod = 25;
    private double baseJumpAmount = 10;
    private double maxJumpAmount = 50;
    private double jumpAmountIncreasePerPixel = 0.1;
    private double insetToBeginScroll = 1;

    /**
     * Creates a new {@link AutoScrollingWindow}.
     */
    public AutoScrollingWindow() {
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    /**
     * Gets whether auto-scrolling is enabled.
     * 
     * @return {@code true} if auto-scrolling is enabled
     */
    public boolean isAutoScrollingEnabled() {
        return autoScrollingEnabled;
    }

    /**
     * Sets whether auto-scrolling is enabled
     * 
     * @param autoScrollingEnabled {@code true} to enable auto-scrolling, {@code false} to disable it
     */
    public void setAutoScrollingEnabled(final boolean autoScrollingEnabled) {
        this.autoScrollingEnabled = autoScrollingEnabled;
    }

    /**
     * Gets the interval at which auto-scroll 'jumps' occur when the cursor is dragged outside the window.
     * 
     * @return the jump period in milliseconds
     */
    public double getJumpPeriod() {
        return jumpPeriod;
    }

    /**
     * Sets the interval at which auto-scroll 'jumps' occur when the cursor is dragged outside the window.
     * 
     * <p>
     * Defaults to <b>25ms</b>. It shouldn't be necessary to change this unless performance is slow.
     * </p>
     * 
     * @param jumpPeriod a value in milliseconds
     */
    public void setJumpPeriod(final double jumpPeriod) {
        this.jumpPeriod = jumpPeriod;
    }

    /**
     * Gets the amount by which the window jumps when the cursor is dragged to the window-edge.
     * 
     * @return the jump amount in pixels
     */
    public double getBaseJumpAmount() {
        return baseJumpAmount;
    }

    /**
     * Sets the amount by which the window will jump when the cursor is dragged to the window-edge.
     * 
     * <p>
     * Defaults to <b>10 pixels</b>.
     * </p>
     * 
     * @param baseJumpAmount a value in pixels
     */
    public void setBaseJumpAmount(final double baseJumpAmount) {
        this.baseJumpAmount = baseJumpAmount;
    }

    /**
     * Gets the maximum amount by which the window will jump when the cursor is dragged far outside the window.
     * 
     * @return maxJumpAmount the maximum jump-amount in pixels
     */
    public double getMaxJumpAmount() {
        return maxJumpAmount;
    }

    /**
     * Gets the maximum amount by which the window will jump when the cursor is dragged far outside the window.
     * 
     * @return maxJumpAmount the maximum jump-amount in pixels
     */
    public void setMaxJumpAmount(final double maxJumpAmount) {
        this.maxJumpAmount = maxJumpAmount;
    }

    /**
     * Gets how much the jump-amount increases for each pixel that the cursor is outside the window.
     * 
     * @return the jump-amount increase per pixel
     */
    public double getJumpAmountIncreasePerPixel() {
        return jumpAmountIncreasePerPixel;
    }

    /**
     * Sets how much the jump-amount increases for each pixel that the cursor is outside the window.
     * 
     * <p>
     * Defaults to <b>0.1</b>.
     * </p>
     * 
     * @param a value in pixels or a fraction of pixels (should be small)
     */
    public void setJumpAmountIncreasePerPixel(final double jumpAmountIncreasePerPixel) {
        this.jumpAmountIncreasePerPixel = jumpAmountIncreasePerPixel;
    }

    /**
     * Gets the inset from the window-edge where auto-scrolling will begin.
     * 
     * @return the inset from the window-edge where auto-scrolling begins
     */
    public double getInsetToBeginScroll() {
        return insetToBeginScroll;
    }

    /**
     * Sets the inset from the window-edge where auto-scrolling will begin.
     * 
     * <p>
     * Defaults to <b>1 pixel</b>. Should not be 0 if the graph-editor can be full-screen.
     * </p>
     * 
     * @param insetToBeginScroll the inset from the window-edge where auto-scrolling begins
     */
    public void setInsetToBeginScroll(final double insetToBeginScroll) {
        this.insetToBeginScroll = insetToBeginScroll;
    }

    /**
     * Handles mouse-dragged events.
     * 
     * <p>
     * The event object is stored to be re-fired later. When we pan the window, we re-fire the previous drag event on
     * its target so that even if the cursor is no longer moving, the dragged-object will continue to move smoothly
     * along as the window auto-scrolls.
     * </p>
     * 
     * @param event the mouse-dragged event object
     */
    private void handleMouseDragged(final MouseEvent event) {

        if (event.isPrimaryButtonDown() && event.getTarget() instanceof Node) {

            currentDragEvent = event;
            dragEventTarget = (Node) event.getTarget();

            jumpDistance = getDistanceToJump(event.getX(), event.getY());
            if (jumpDistance != null && !isScrolling) {
                startScrolling();
            }
        }
    }

    /**
     * Handles mouse released events.
     * 
     * @param event the mouse-released event object
     */
    private void handleMouseReleased(final MouseEvent event) {
        dragEventTarget = null;
        endScrolling();
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
    private Point2D getDistanceToJump(final double cursorX, final double cursorY) {

        double jumpX = 0;
        double jumpY = 0;

        if (cursorX <= insetToBeginScroll) {
            final double distanceOutside = insetToBeginScroll - cursorX;
            jumpX = -baseJumpAmount - distanceOutside * jumpAmountIncreasePerPixel;
            jumpX = Math.max(jumpX, -maxJumpAmount);
        } else if (cursorX >= getWidth() - insetToBeginScroll) {
            final double distanceOutside = cursorX + insetToBeginScroll - getWidth();
            jumpX = baseJumpAmount + distanceOutside * jumpAmountIncreasePerPixel;
            jumpX = Math.min(jumpX, maxJumpAmount);
        }

        if (cursorY <= insetToBeginScroll) {
            final double distanceOutside = insetToBeginScroll - cursorY;
            jumpY = -baseJumpAmount - distanceOutside * jumpAmountIncreasePerPixel;
            jumpY = Math.max(jumpY, -maxJumpAmount);
        } else if (cursorY >= getHeight() - insetToBeginScroll) {
            final double distanceOutside = cursorY + insetToBeginScroll - getHeight();
            jumpY = baseJumpAmount + distanceOutside * jumpAmountIncreasePerPixel;
            jumpY = Math.min(jumpY, maxJumpAmount);
        }

        if (jumpX == 0 && jumpY == 0) {
            return null;
        } else {
            return new Point2D(Math.round(jumpX), Math.round(jumpY));
        }
    }

    /**
     * Starts the auto-scrolling.
     */
    private void startScrolling() {

        isScrolling = true;

        final KeyFrame frame = new KeyFrame(Duration.millis(jumpPeriod), event -> {
            if (dragEventTarget != null && isScrolling && jumpDistance != null) {
                panBy(jumpDistance.getX(), jumpDistance.getY());
                dragEventTarget.fireEvent(currentDragEvent);
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    /**
     * Stops the auto-scrolling.
     */
    private void endScrolling() {

        isScrolling = false;

        if (timeline != null) {
            timeline.stop();
        }
    }
}
