/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * The skin class for a {@link GConnector}. Responsible for visualizing connectors in the graph editor.
 *
 * <p>
 * A custom connector skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GConnector} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node must be created by the skin implementation and returned in the {@link #getRoot()} method.
 * </p>
 */
public abstract class GConnectorSkin extends GSkin<GConnector> {

    /**
     * Creates a new {@link GConnectorSkin}.
     *
     * @param connector the {@link GConnector} represented by the skin
     */
    public GConnectorSkin(final GConnector connector) {
        super(connector);
    }

    /**
     * Gets the width of the connector skin.
     *
     * @return the width of the connector skin
     */
    public abstract double getWidth();

    /**
     * Gets the height of the connector skin.
     *
     * @return the height of the connector skin
     */
    public abstract double getHeight();

    /**
     * Applys the specified style to the connector.
     *
     * <p>
     * This is called by the library during various mouse events. For example when a connector is dragged over another
     * connector in an attempt to create a new connection.
     * </p>
     *
     * @param style the {@link GConnectorStyle} to apply
     */
    public abstract void applyStyle(GConnectorStyle style);
}
