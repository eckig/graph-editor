package io.github.eckig.grapheditor.utils;

/**
 * Defines the various input gestures used by the graph editor
 */
public enum GraphInputGesture
{

    /**
     * Resizing graph editor elements
     */
    RESIZE,

    /**
     * Moving graph editor elements
     */
    MOVE,

    /**
     * Connecting graph editor elements
     */
    CONNECT,

    /**
     * Selecting graph editor elements
     */
    SELECT,

    /**
     * Panning the view port.
     *
     * <p>
     * Unlike all other gestures, this one is also activated by the
     * {@link javafx.scene.input.MouseButton#MIDDLE middle} mouse button.
     * </p>
     */
    PAN;
}
