/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;


/**
 * The minimap-representation of the currently-visible region of the graph
 * editor.
 *
 * <p>
 * This looks like a rectangle in the minimap. It's position 'locates' the
 * currently-visible region relative to the entire content.
 * </p>
 */
class MinimapLocator extends DraggableBox
{

    private static final String STYLE_CLASS_LOCATOR = "minimap-locator"; //$NON-NLS-1$

    /**
     * Creates a new {@link MinimapLocator}.
     *
     * @param minimapPadding
     *            the padding value used by the minimap
     */
    public MinimapLocator(final double minimapPadding)
    {
        getStyleClass().add(STYLE_CLASS_LOCATOR);

        final GraphEditorProperties locatorProperties = new GraphEditorProperties();

        locatorProperties.setNorthBoundValue(minimapPadding);
        locatorProperties.setSouthBoundValue(minimapPadding);
        locatorProperties.setEastBoundValue(minimapPadding);
        locatorProperties.setWestBoundValue(minimapPadding);

        setEditorProperties(locatorProperties);
    }

    @Override
    protected boolean isEditable()
    {
        // we want the minimap to be movable at all times because it is not really editing
        return true;
    }
}
