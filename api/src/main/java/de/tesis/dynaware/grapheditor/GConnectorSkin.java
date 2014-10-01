/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import javafx.scene.Node;
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
public abstract class GConnectorSkin {

    private final GConnector connector;

    private GraphEditor graphEditor;

    /**
     * Creates a new {@link GConnectorSkin}.
     *
     * @param connector the {@link GConnector} represented by the skin
     */
    public GConnectorSkin(final GConnector connector) {
        this.connector = connector;
    }

    /**
     * Gets the connector model element represented by the skin.
     *
     * @return the {@link GConnector} represented by the skin
     */
    public GConnector getConnector() {
        return connector;
    }

    /**
     * Sets the graph editor instance that this skin is a part of.
     *
     * @param graphEditor a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    /**
     * Gets the graph editor instance that this skin is a part of.
     *
     * <p>
     * This is provided for advanced skin customisation purposes only. Use at your own risk.
     * </p>
     *
     * @return the {@link GraphEditor} instance that this skin is a part of
     */
    public GraphEditor getGraphEditor() {
        return graphEditor;
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return the connector's root {@link Node}
     */
    public abstract Node getRoot();

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
