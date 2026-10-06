/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import io.github.eckig.grapheditor.model.GConnection;

/**
 * The skin class for a {@link GConnection}.
 *
 * <p>
 * A custom connection skin must extend this class (or one of its subclasses). It <b>must</b> also provide a
 * constructor taking exactly one {@link GConnection} parameter.
 * </p>
 *
 * <p>
 * Skins extending this class directly are <b>not visual</b>: they are not added to the graph editor view, cannot be
 * clicked, are not shown in the minimap and stay untouched when one of their connectors is dragged off. They still
 * take part in selection via {@link #selectionChanged(boolean)}. The hierarchy is:
 * </p>
 * <ul>
 * <li>{@link GConnectionSkin}: no visual representation</li>
 * <li>{@link GVisualConnectionSkin}: adds a root JavaFX node and the displayed route</li>
 * <li>{@link GJointConnectionSkin}: additionally displays and/or constrains the joints of the connection</li>
 * </ul>
 */
public abstract class GConnectionSkin extends GSkin<GConnection>
{

    /**
     * Creates a new {@link GConnectionSkin}.
     *
     * @param connection
     *         the {@link GConnection} represented by the skin
     */
    public GConnectionSkin(final GConnection connection)
    {
        super(connection);
    }
}
