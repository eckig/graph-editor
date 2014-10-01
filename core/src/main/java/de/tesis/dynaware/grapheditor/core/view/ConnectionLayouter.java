/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * Responsible for telling connection skins to draw themselves.
 */
public class ConnectionLayouter {

    private final SkinLookup skinLookup;

    private GModel model;

    /**
     * Creates a new {@link ConnectionLayouter} instance. Only one instance should exist per {@link DefaultGraphEditor}
     * instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up skins
     */
    public ConnectionLayouter(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
    }

    /**
     * Initializes the connection layout manager for the given model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        this.model = model;

        redraw();
    }

    /**
     * Redraws all connections according to the latest layout values.
     */
    public void redraw() {

        if (model == null) {
            return;
        }

        final Map<GConnection, List<Point2D>> allPoints = new HashMap<>();

        for (final GConnection connection : model.getConnections()) {
            final List<Point2D> points = createPoints(connection);
            skinLookup.lookupConnection(connection).applyConstraints(points);
            allPoints.put(connection, points);
        }

        for (final GConnection connection : model.getConnections()) {
            skinLookup.lookupConnection(connection).draw(allPoints.get(connection), allPoints);
        }
    }

    /**
     * Creates the list of points for the given connection.
     *
     * <p>
     * The order of the points should be as follows:
     *
     * <ol>
     * <li>Source position.
     * <li>Joint positions in same order the joints appear in their {@link GConnection}.
     * <li>Target position.
     * </ol>
     *
     * </p>
     *
     * @param connection a {@link GConnection} instance
     *
     * @return a list of the given connection's points
     */
    private List<Point2D> createPoints(final GConnection connection) {

        final List<Point2D> points = new ArrayList<>();

        points.add(GeometryUtils.getConnectorPosition(connection.getSource(), skinLookup));
        points.addAll(GeometryUtils.getJointPositions(connection, skinLookup));
        points.add(GeometryUtils.getConnectorPosition(connection.getTarget(), skinLookup));

        return points;
    }
}
