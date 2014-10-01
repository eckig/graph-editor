/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.scene.Group;

/**
 * The representation of the panning-window content that will be displayed in the minimap.
 */
public abstract class MinimapContentRepresentation extends Group {

    /**
     * Draws the minimap content representation for the given scale factor.
     *
     * @param scaleFactor ratio between the sizes of the panning window and minimap (between 0 and 1)
     */
    public abstract void draw(final double scaleFactor);
}
