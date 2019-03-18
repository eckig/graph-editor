package de.tesis.dynaware.grapheditor.core.utils;

import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;


/**
 * The rectangle that is drawn when dragging on the view to create a selection.
 */
public class SelectionBox extends Rectangle
{

    private static final String STYLE_CLASS_SELECTION_BOX = "graph-editor-selection-box";

    private ChangeListener<Scene> sceneListener;
    private ChangeListener<Window> windowListener;
    private ChangeListener<Boolean> windowFocusListener;

    /**
     * Creates a new {@link SelectionBox} instance.
     */
    public SelectionBox()
    {
        getStyleClass().addAll(STYLE_CLASS_SELECTION_BOX);

        setVisible(false);
        setManaged(false);
        setMouseTransparent(true);

        addWindowFocusListener();
    }

    /**
     * Draws the selection box for the given position parameters.
     *
     * @param x
     *            the start x position of the box
     * @param y
     *            the start y position of the box
     * @param width
     *            the width of the box
     * @param height
     *            the height of the box
     */
    public void draw(final double x, final double y, final double width, final double height)
    {
        setVisible(true);

        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }

    /**
     * Adds a listener to the focused property of the selection box's window, so
     * that the selection box always disappears when the window loses focus.
     *
     * <p>
     * If this listener is not added, the selection box will not disappear if
     * the user alt-tabs before releasing the right-mouse button.
     * </p>
     */
    private void addWindowFocusListener()
    {
        sceneListener = (observable, oldValue, newValue) ->
        {
            if (oldValue != null)
            {
                oldValue.windowProperty().removeListener(windowListener);
            }

            if (newValue != null)
            {
                newValue.windowProperty().addListener(windowListener);
            }
        };

        windowListener = (observable, oldValue, newValue) ->
        {
            if (oldValue != null)
            {
                oldValue.focusedProperty().removeListener(windowFocusListener);
            }

            if (newValue != null)
            {
                newValue.focusedProperty().addListener(windowFocusListener);
            }
        };

        windowFocusListener = (observable, oldValue, newValue) ->
        {
            if (!newValue)
            {
                setVisible(false);
            }
        };

        sceneProperty().addListener(sceneListener);
    }
}
