/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * A draggable, resizable box that can display children.
 *
 * <p>
 * See {@link DraggableBox} for more information.
 * </p>
 */
public class ResizableBox extends DraggableBox
{

    private static final int DEFAULT_RESIZE_BORDER_TOLERANCE = 8;

    private double lastWidth;
    private double lastHeight;

    private RectangleMouseRegion lastMouseRegion;

    private boolean mouseInPositionForResize;

    /**
     * Creates an empty resizable box.
     */
    public ResizableBox()
    {

        addEventHandler(MouseEvent.MOUSE_ENTERED, this::processMousePosition);
        addEventHandler(MouseEvent.MOUSE_MOVED, this::processMousePosition);
        addEventHandler(MouseEvent.MOUSE_EXITED, this::handleMouseExited);
    }

    @Override
    public void dispose()
    {
        finishGesture(GraphInputGesture.RESIZE);

        super.dispose();
    }


    @Override
    public boolean isMouseInPositionForResize()
    {
        return mouseInPositionForResize;
    }

    @Override
    protected void handleMousePressed(final MouseEvent event)
    {
        super.handleMousePressed(event);

        if (!(getParent() instanceof Region))
        {
            return;
        }
        else if (!event.isPrimaryButtonDown() || !isEditable())
        {
            setCursor(null);
            return;
        }

        storeClickValuesForResize(event.getX(), event.getY());
    }

    @Override
    protected void handleMouseDragged(final MouseEvent pEvent)
    {
        if (lastMouseRegion == null || !(getParent() instanceof Region) || !pEvent.isPrimaryButtonDown() || !isEditable())
        {
            setCursor(null);
            return;
        }

        final Point2D cursorPosition = GeometryUtils.getCursorPosition(pEvent, getContainer(this));
        if (lastMouseRegion == RectangleMouseRegion.INSIDE)
        {
            super.handleMouseDragged(pEvent);
        }
        else if (lastMouseRegion != RectangleMouseRegion.OUTSIDE && isResizeCursor(getCursor()) &&
                activateGesture(GraphInputGesture.RESIZE, pEvent))
        {
            handleResize(cursorPosition.getX(), cursorPosition.getY());
            pEvent.consume();
        }
    }

    private static boolean isResizeCursor(final Cursor pCursor)
    {
        return pCursor == Cursor.NE_RESIZE ||
                pCursor == Cursor.NW_RESIZE ||
                pCursor == Cursor.SE_RESIZE ||
                pCursor == Cursor.SW_RESIZE ||
                pCursor == Cursor.N_RESIZE ||
                pCursor == Cursor.S_RESIZE ||
                pCursor == Cursor.E_RESIZE ||
                pCursor == Cursor.W_RESIZE;
    }

    @Override
    protected void handleMouseReleased(final MouseEvent pEvent)
    {
        super.handleMouseReleased(pEvent);
        processMousePosition(pEvent);
        if (finishGesture(GraphInputGesture.RESIZE))
        {
            pEvent.consume();
        }
    }

    private void handleMouseExited(final MouseEvent pEvent)
    {
        if (!pEvent.isPrimaryButtonDown())
        {
            setCursor(null);
        }
    }

    /**
     * Processes the current mouse position, updating the cursor accordingly.
     *
     * @param pEvent
     *            the latest {@link MouseEvent} for the mouse entering or moving
     *            inside the rectangle
     */
    private void processMousePosition(final MouseEvent pEvent)
    {
        if (pEvent.isPrimaryButtonDown() || !isEditable())
        {
            return;
        }

        final RectangleMouseRegion mouseRegion = getMouseRegion(pEvent.getX(), pEvent.getY());
        mouseInPositionForResize = mouseRegion != RectangleMouseRegion.INSIDE;
        updateCursor(mouseRegion);
    }

    /**
     * Stores relevant layout values at the time of the last mouse click
     * (mouse-pressed event).
     *
     * @param x
     *            the x position of the click event
     * @param y
     *            the y position of the click event
     */
    private void storeClickValuesForResize(final double x, final double y)
    {
        lastWidth = getWidth();
        lastHeight = getHeight();

        lastMouseRegion = getMouseRegion(x, y);
    }

    /**
     * Handles a resize event to the given cursor position.
     *
     * @param x
     *            the cursor container-x position
     * @param y
     *            the cursor container-y position
     */
    private void handleResize(final double x, final double y)
    {
        switch (lastMouseRegion)
        {
            case NORTHEAST:
                handleResizeNorth(y);
                handleResizeEast(x);
                positionMoved();
                break;
            case NORTHWEST:
                handleResizeNorth(y);
                handleResizeWest(x);
                positionMoved();
                break;
            case SOUTHEAST:
                handleResizeSouth(y);
                handleResizeEast(x);
                positionMoved();
                break;
            case SOUTHWEST:
                handleResizeSouth(y);
                handleResizeWest(x);
                positionMoved();
                break;
            case NORTH:
                handleResizeNorth(y);
                positionMoved();
                break;
            case SOUTH:
                handleResizeSouth(y);
                positionMoved();
                break;
            case EAST:
                handleResizeEast(x);
                positionMoved();
                break;
            case WEST:
                handleResizeWest(x);
                positionMoved();
                break;

            case INSIDE:
            case OUTSIDE:
                break;
        }
    }

    /**
     * Handles a resize event in the north (top) direction to the given cursor y
     * position.
     *
     * @param y the cursor scene-y position
     */
    private void handleResizeNorth(final double y) {

        final double scaleFactor = getLocalToSceneTransform().getMyy();

        final double yDragDistance = (y - lastMouseY) / scaleFactor;
        final double minResizeHeight = Math.max(getMinHeight(), 0);

        double newLayoutY = lastLayoutY + yDragDistance;
        double newHeight = lastHeight - yDragDistance;

        // Snap-to-grid logic here.
        if (isSnapToGrid())
        {
            // The -1 here is to put the rectangle border exactly on top of a grid line.
            final double roundedLayoutY = roundToGridSpacing(newLayoutY) - 1;
            newHeight = newHeight - roundedLayoutY + newLayoutY;
            newLayoutY = roundedLayoutY;
        }
        else
        {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            final double roundedLayoutY = Math.round(newLayoutY);
            newHeight = Math.round(newHeight - roundedLayoutY + newLayoutY);
            newLayoutY = roundedLayoutY;
        }

        // Min & max resize logic here.
        if (newLayoutY < getNorthBoundValue())
        {
            newLayoutY = getNorthBoundValue();
            newHeight = lastLayoutY + lastHeight - getNorthBoundValue();
        }
        else if (newHeight < minResizeHeight)
        {
            newLayoutY = lastLayoutY + lastHeight - minResizeHeight;
            newHeight = minResizeHeight;
        }

        setLayoutY(newLayoutY);
        setHeight(newHeight);
    }

    /**
     * Handles a resize event in the south (bottom) direction to the given
     * cursor y position.
     *
     * @param y the cursor scene-y position
     */
    private void handleResizeSouth(final double y) {

        final double scaleFactor = getLocalToSceneTransform().getMyy();

        final double yDragDistance = (y - lastMouseY) / scaleFactor;
        final double maxParentHeight = getParent().getLayoutBounds().getHeight();

        final double minResizeHeight = Math.max(getMinHeight(), 0);
        final double maxAvailableHeight = maxParentHeight - getLayoutY() - getSouthBoundValue();

        double newHeight = lastHeight + yDragDistance;

        // Snap-to-grid logic here.
        if (isSnapToGrid())
        {
            newHeight = roundToGridSpacing(newHeight + lastLayoutY) - lastLayoutY;
        }
        else
        {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newHeight = Math.round(newHeight);
        }

        // Min & max resize logic here.
        if (newHeight > maxAvailableHeight) {
            newHeight = maxAvailableHeight;
        } else if (newHeight < minResizeHeight) {
            newHeight = minResizeHeight;
        }

        setHeight(newHeight);
    }

    /**
     * Handles a resize event in the east (right) direction to the given cursor
     * x position.
     *
     * @param x the cursor scene-x position
     */
    private void handleResizeEast(final double x) {

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        final double xDragDistance = (x - lastMouseX) / scaleFactor;
        final double maxParentWidth = getParent().getLayoutBounds().getWidth();

        final double minResizeWidth = Math.max(getMinWidth(), 0);
        final double maxAvailableWidth = maxParentWidth - getLayoutX() - getEastBoundValue();

        double newWidth = lastWidth + xDragDistance;

        // Snap-to-grid logic here.
        if (isSnapToGrid())
        {
            newWidth = roundToGridSpacing(newWidth + lastLayoutX) - lastLayoutX;
        }
        else
        {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newWidth = Math.round(newWidth);
        }

        // Min & max resize logic here.
        if (newWidth > maxAvailableWidth) {
            newWidth = maxAvailableWidth;
        } else if (newWidth < minResizeWidth) {
            newWidth = minResizeWidth;
        }

        setWidth(newWidth);
    }

    /**
     * Handles a resize event in the west (left) direction to the given cursor x
     * position.
     *
     * @param x the cursor scene-x position
     */
    private void handleResizeWest(final double x) {

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        final double xDragDistance = (x - lastMouseX) / scaleFactor;
        final double minResizeWidth = Math.max(getMinWidth(), 0);

        double newLayoutX = lastLayoutX + xDragDistance;
        double newWidth = lastWidth - xDragDistance;

        // Snap-to-grid logic here.
        if (isSnapToGrid())
        {
            // The -1 here is to put the rectangle border exactly on top of a grid line.
            final double roundedLayoutX = roundToGridSpacing(newLayoutX) - 1;
            newWidth = newWidth - roundedLayoutX + newLayoutX;
            newLayoutX = roundedLayoutX;
        }
        else
        {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            final double roundedLayoutX = Math.round(newLayoutX);
            newWidth = Math.round(newWidth - roundedLayoutX + newLayoutX);
            newLayoutX = roundedLayoutX;
        }

        // Min & max resize logic here.
        if (newLayoutX < getWestBoundValue())
        {
            newLayoutX = getWestBoundValue();
            newWidth = lastLayoutX + lastWidth - getWestBoundValue();
        }
        else if (newWidth < minResizeWidth)
        {
            newLayoutX = lastLayoutX + lastWidth - minResizeWidth;
            newWidth = minResizeWidth;
        }

        setLayoutX(newLayoutX);
        setWidth(newWidth);
    }

    /**
     * Gets the particular sub-region of the rectangle that the given cursor
     * position is in.
     *
     * @param x the x cursor position
     * @param y the y cursor position
     *
     * @return the {@link RectangleMouseRegion} that the cursor is located in
     */
    private RectangleMouseRegion getMouseRegion(final double x, final double y) {

        final double width = getWidth();
        final double height = getHeight();

        if (x < 0 || y < 0 || x > width || y > height) {
            return RectangleMouseRegion.OUTSIDE;
        }

        final boolean isNorth = y < DEFAULT_RESIZE_BORDER_TOLERANCE;
        final boolean isSouth = y > height - DEFAULT_RESIZE_BORDER_TOLERANCE;
        final boolean isEast = x > width - DEFAULT_RESIZE_BORDER_TOLERANCE;
        final boolean isWest = x < DEFAULT_RESIZE_BORDER_TOLERANCE;

        if (isNorth && isEast) {
            return RectangleMouseRegion.NORTHEAST;
        } else if (isNorth && isWest) {
            return RectangleMouseRegion.NORTHWEST;
        } else if (isSouth && isEast) {
            return RectangleMouseRegion.SOUTHEAST;
        } else if (isSouth && isWest) {
            return RectangleMouseRegion.SOUTHWEST;
        } else if (isNorth) {
            return RectangleMouseRegion.NORTH;
        } else if (isSouth) {
            return RectangleMouseRegion.SOUTH;
        } else if (isEast) {
            return RectangleMouseRegion.EAST;
        } else if (isWest) {
            return RectangleMouseRegion.WEST;
        } else {
            return RectangleMouseRegion.INSIDE;
        }
    }

    /**
     * Updates the cursor style.
     *
     * <p>
     * This should occur for example when the cursor is near the border of the
     * rectangle, to indicate that resizing is allowed.
     * </p>
     *
     * @param mouseRegion the {@link RectangleMouseRegion} where the cursor is
     * located
     */
    private void updateCursor(final RectangleMouseRegion mouseRegion)
    {
        switch (mouseRegion)
        {
            case NORTHEAST:
                setCursor(Cursor.NE_RESIZE);
                break;
            case NORTHWEST:
                setCursor(Cursor.NW_RESIZE);
                break;
            case SOUTHEAST:
                setCursor(Cursor.SE_RESIZE);
                break;
            case SOUTHWEST:
                setCursor(Cursor.SW_RESIZE);
                break;
            case NORTH:
                setCursor(Cursor.N_RESIZE);
                break;
            case SOUTH:
                setCursor(Cursor.S_RESIZE);
                break;
            case EAST:
                setCursor(Cursor.E_RESIZE);
                break;
            case WEST:
                setCursor(Cursor.W_RESIZE);
                break;

            case INSIDE:
            case OUTSIDE:
                // Set to null instead of Cursor.DEFAULT so it doesn't overwrite cursor settings of parent.
                setCursor(null);
                break;
        }
    }

    /**
     * The set of possible regions around the border of a rectangle.
     *
     * <p>
     * Used during mouse hover and drag events on a {@link ResizableBox}.
     * </p>
     */
    private enum RectangleMouseRegion {

        NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, INSIDE, OUTSIDE;
    }
}
