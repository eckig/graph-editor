/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.view;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;


/**
 * Responsible for routing the connections and telling the connection skins to draw themselves.
 *
 * <p>
 * The default implementation {@link io.github.eckig.grapheditor.core.view.impl.DefaultConnectionLayout} delegates the
 * routing to pluggable {@link io.github.eckig.grapheditor.routing.ConnectionRouter routers}, a custom implementation
 * is usually not needed.
 * </p>
 */
public interface ConnectionLayout
{

    /**
     * Initializes the connection layout manager for the given model.
     *
     * @param pModel
     *            the {@link GModel} currently being edited
     */
    void initialize(final GModel pModel);

    /**
     * Draws all connections according to the latest layout values. Called on every layout pass of the graph editor
     * view, including the passes while the user drags elements.
     *
     * @return {@code true} if the final routes (i.e. the routes after a {@link #requestRouting() requested routing})
     *         changed
     */
    boolean draw();

    /**
     * Requests a full routing of all connections in the next {@link #draw()}, e.g. after the model changed.
     */
    void requestRouting();

    /**
     * @param pNode
     *            {@link GNode} removed from the model
     */
    default void nodeRemoved(final GNode pNode)
    {
        // nothing to do by default
    }

    /**
     * @param pConnection
     *            {@link GConnection} removed from the model
     */
    default void connectionRemoved(final GConnection pConnection)
    {
        // nothing to do by default
    }
}
