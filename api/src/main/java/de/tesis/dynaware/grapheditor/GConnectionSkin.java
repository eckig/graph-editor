/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import de.tesis.dynaware.grapheditor.model.GConnection;

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
public abstract class GConnectionSkin {

    private final GConnection connection;

    private GraphEditor graphEditor;

    /**
     * Creates a new {@link GConnectionSkin}.
     *
     * @param connection the {@link GConnection} represented by the skin
     */
    public GConnectionSkin(final GConnection connection) {
        this.connection = connection;
    }

    /**
     * Gets the connection model element represented by the skin.
     *
     * @return the {@link GConnection} represented by the skin
     */
    public GConnection getConnection() {
        return connection;
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
     * @return a the connection's root {@link Node}
     */
    public abstract Node getRoot();

    /**
     * Sets the skin objects for all joints inside the connection.
     *
     * <p>
     * This will be called as the connection skin is created. The connection skin can manipulate its joint skins if it
     * chooses. For example a 'rectangular' connection skin may restrict the movement of the first and last joints to
     * the x direction only.
     * </p>
     *
     * @param jointSkins the list of all {@link GJointSkin} instances associated to the connection
     */
    public abstract void setJointSkins(final List<GJointSkin> jointSkins);

    /**
     * Draws the connection skin based on the given points. This is called every time the connection's position could
     * change, for example if one of its connectors is moved.
     *
     * <p>
     * The order of the points is as follows:
     *
     * <ol>
     * <li>Source position.
     * <li>Joint positions in same order the joints appear in their {@link GConnection}.
     * <li>Target position.
     * </ol>
     *
     * </p>
     *
     * <p>
     * A simple connection skin may ignore the second parameter. This parameter can for example be used to display a
     * 'rerouting' effect when the connection passes over another connection.
     * </p>
     *
     * @param points all the {@link Point2D} instances that specify the connection position
     * @param allConnections the lists of points for all connections (can be ignored in a simple skin)
     */
    public abstract void draw(final List<Point2D> points, Map<GConnection, List<Point2D>> allConnections);

    /**
     * Applies constraints to the given set of points before any connections are drawn.
     *
     * <p>
     * This method is called on <b>all</b> connection skins <b>before</b> the draw method is called on any connection
     * skin. It can safely be ignored by simple skin implementations.
     * </p>
     *
     * <p>
     * Overriding this method allows the connection skin to apply constraints to its set of points, and these
     * constraints will be taken into account during the draw methods of other connections, even if they are drawn
     * before this connection.
     * </p>
     */
    public void applyConstraints(final List<Point2D> points) {
        // Base implementation does nothing.
    }
}
