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
 * The auto-scrolling occurs when the mouse is dragged to the edge of the window. The scrolling rate increases the
 * longer the cursor is outside the window.
 * </p>
 */
public class AutoScrollingWindow extends PanningWindow {

    private final AutoScrollingParameters parameters = new AutoScrollingParameters();

    private Timeline timeline;
    private MouseEvent currentDragEvent;
    private Node dragEventTarget;
    private boolean isScrolling;
    private Point2D jumpDistance;

    private boolean autoScrollingEnabled = true;
    private int jumpsTaken;

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
     * Gets the parameters that control the auto-scrolling rate.
     * 
     * @return the parameters that control the auto-scrolling rate
     */
    public AutoScrollingParameters getAutoScrollingParameters() {
        return parameters;
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

            if (jumpDistance == null) {
                jumpsTaken = 0;
            } else if (!isScrolling && isAutoScrollingEnabled()) {
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

        final double baseAmount = parameters.getBaseJumpAmount();
        final double additionalAmount = jumpsTaken + parameters.getJumpAmountIncreasePerJump();
        final double distance = Math.min(baseAmount + additionalAmount, parameters.getMaxJumpAmount());

        if (cursorX <= parameters.getInsetToBeginScroll()) {
            jumpX = -distance;
        } else if (cursorX >= getWidth() - parameters.getInsetToBeginScroll()) {
            jumpX = distance;
        }

        if (cursorY <= parameters.getInsetToBeginScroll()) {
            jumpY = -distance;
        } else if (cursorY >= getHeight() - parameters.getInsetToBeginScroll()) {
            jumpY = distance;
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
        jumpsTaken = 0;

        final KeyFrame frame = new KeyFrame(Duration.millis(parameters.getJumpPeriod()), event -> {
            if (dragEventTarget != null && isScrolling && jumpDistance != null) {
                panBy(jumpDistance.getX(), jumpDistance.getY());
                dragEventTarget.fireEvent(currentDragEvent);
                jumpsTaken++;
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
