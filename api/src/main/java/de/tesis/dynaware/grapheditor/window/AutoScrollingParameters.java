package de.tesis.dynaware.grapheditor.window;

/**
 * The parameters controlling the auto-scrolling rate. Change at your own risk.
 */
public class AutoScrollingParameters {

    private double jumpPeriod = 25;
    private double baseJumpAmount = 5;
    private double maxJumpAmount = 50;
    private double jumpAmountIncreasePerJump = 0.5;
    private double insetToBeginScroll = 1;

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
     * Gets how much (in pixels) that the jump-amount increases with each jump.
     * 
     * @return the amount that the jump-amount increases with each jump
     */
    public double getJumpAmountIncreasePerJump() {
        return jumpAmountIncreasePerJump;
    }

    /**
     * Sets how much (in pixels) that the jump-amount increases with each jump.
     * 
     * <p>
     * This leads to an "acceleration" effect. Defaults to <b>0.5</b>.
     * </p>
     * 
     * @param jumpAmountIncreasePerJump the amount that the jump-amount increases with each jump
     */
    public void setJumpAmountIncreasePerJump(final double jumpAmountIncreasePerJump) {
        this.jumpAmountIncreasePerJump = jumpAmountIncreasePerJump;
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
}
