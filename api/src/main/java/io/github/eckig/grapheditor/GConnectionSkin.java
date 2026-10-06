/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor;

import java.util.List;

import io.github.eckig.grapheditor.model.GConnection;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;

/**
 * The skin class for a {@link GConnection}. Responsible for visualizing connections in the graph editor.
 *
 * <p>
 * A custom connection skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GConnection} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node must be created by the skin implementation and returned in the {@link #getRoot()} method. For
 * example, a very simple connection skin could use a {@link Line} whose start and end positions are set to those of the
 * source and target connectors.
 * </p>
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

    /**
     * Sets the skin objects for all joints inside the connection.
     *
     * <p>
     * This will be called as the connection skin is created. The connection skin can manipulate its joint skins if it
     * chooses. For example a 'rectangular' connection skin may restrict the movement of the first and last joints to
     * the x direction only.
     * </p>
     *
     * @param jointSkins
     *         the list of all {@link GJointSkin} instances associated to the connection
     */
    public abstract void setJointSkins(final List<GJointSkin> jointSkins);

    /**
     * Returns the route this skin currently displays, e.g. for the minimap.
     *
     * <p>
     * The points are in the coordinate system of the graph editor view and ordered from source to target (inclusive).
     * Skins computing their own routing should override this and return the points they last drew. The default
     * returns an empty list, meaning "unknown".
     * </p>
     *
     * <p>
     * The minimap picks up the routes drawn in the first layout pass of the graph editor view after a model change
     * (command stack). Intermediate routes, e.g. while dragging, are not shown. If routes change at any other time
     * (e.g. asynchronous routing), the application should call {@code GraphEditorMinimap.redrawConnections()}.
     * </p>
     *
     * @return unmodifiable list of route points, never {@code null}
     */
    public List<Point2D> getRoutePoints()
    {
        return List.of();
    }
}
