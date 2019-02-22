/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
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
public abstract class GConnectionSkin extends GSkin<GConnection> {

    /**
     * Cache the index of this connection skin inside the list of children of the
     * connection layer. As the graph editor grows the indexOf() lookup calls take
     * up a considerable amount of time.
     */
    private int mConnectionIndex;

    /**
     * Creates a new {@link GConnectionSkin}.
     *
     * @param connection the {@link GConnection} represented by the skin
     */
    public GConnectionSkin(final GConnection connection) {
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
     * @param jointSkins the list of all {@link GJointSkin} instances associated to the connection
     */
    public abstract void setJointSkins(final List<GJointSkin> jointSkins);

    /**
     * Draws the connection skin. This is called every time the connection's
     * position could change, for example if one of its connectors is moved,
     * after {@link #update()}.
     *
     * <p>
     * A simple connection skin may ignore the given parameter. This parameter
     * can for example be used to display a 'rerouting' effect when the
     * connection passes over another connection.
     * </p>
     *
     * @param allConnections
     *            the lists of points for all connections (can be ignored in a
     *            simple skin)
     */
    public void draw(@SuppressWarnings("unused") final Map<GConnectionSkin, Point2D[]> allConnections)
    {
        if (getRoot() != null && getRoot().getParent() != null)
        {
            mConnectionIndex = getRoot().getParent().getChildrenUnmodifiable().indexOf(getRoot());
        }
        else
        {
            mConnectionIndex = -1;
        }
    }

    /**
     * Update and return the points of this connection. This is called every
     * time the connection's position could change, for example if one of its
     * connectors is moved before {@link #draw(Map)}.
     * <p>
     * The order of the points is as follows:
     *
     * <ol>
     * <li>Source position.
     * <li>Joint positions in same order the joints appear in their
     * {@link GConnection}.
     * <li>Target position.
     * </ol>
     *
     * </p>
     *
     * <p>
     * This method is called on <b>all</b> connection skins <b>before</b> the
     * draw method is called on any connection skin. It can safely be ignored by
     * simple skin implementations.
     * </p>
     *
     * <p>
     * Overriding this method allows the connection skin to apply constraints to
     * its set of points, and these constraints will be taken into account
     * during the draw methods of other connections, even if they are drawn
     * before this connection.
     * </p>
     *
     * @return points
     */
    public Point2D[] update()
    {
        final GConnection item = getItem();
        final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
        if (item == null || skinLookup == null)
        {
            return null;
        }
        else if (item.getJoints().isEmpty())
        {
            final Point2D[] points = new Point2D[2];

            // Start: Source position
            points[0] = GeometryUtils.getConnectorPosition(item.getSource(), skinLookup);

            // End: Target position
            points[1] = GeometryUtils.getConnectorPosition(item.getTarget(), skinLookup);

            return points;
        }
        else
        {
            final int len = item.getJoints().size() + 2;
            final Point2D[] points = new Point2D[len];

            // Middle: joint positions
            GeometryUtils.fillJointPositions(item, skinLookup, points);

            // Start: Source position
            points[0] = GeometryUtils.getConnectorPosition(item.getSource(), skinLookup);

            // End: Target position
            points[len - 1] = GeometryUtils.getConnectorPosition(item.getTarget(), skinLookup);

            return points;
        }
    }

    /**
     * @return cached position (index) of this connection skin inside the
     *         child-list of the parent connection layer.
     */
    public int getParentIndex()
    {
        return mConnectionIndex;
    }
}
