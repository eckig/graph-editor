/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;

/**
 * Responsible for telling connection skins to draw themselves.
 */
public class DefaultConnectionLayouter implements ConnectionLayouter {

    private final SkinLookup mSkinLookup;
    private GModel mModel;

    /**
     * Creates a new {@link DefaultConnectionLayouter} instance. Only one
     * instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param pSkinLookup
     *            the {@link SkinLookup} used to look up skins
     */
    public DefaultConnectionLayouter(final SkinLookup pSkinLookup)
    {
        this.mSkinLookup = pSkinLookup;
    }

    @Override
    public void initialize(final GModel pModel)
    {
        this.mModel = pModel;
    }

    @Override
    public void redraw()
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return;
        }

        final Map<GConnection, List<Point2D>> allPoints = new HashMap<>();

        for (int i = 0; i < mModel.getConnections().size(); i++)
        {
            final GConnection connection = mModel.getConnections().get(i);
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
            final List<Point2D> points = createPoints(connection);
            connectionSkin.applyConstraints(points);
            allPoints.put(connection, points);
        }

        for (final Map.Entry<GConnection, List<Point2D>> entry : allPoints.entrySet())
        {
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(entry.getKey());
            connectionSkin.draw(entry.getValue(), allPoints);
        }
    }

    @Override
    public void redrawViewport()
    {
        // TODO implement as soon as the EMF model watcher is done..
        redraw();
    }

    @Override
    public void viewportMoved()
    {
        // TODO implement as soon as the EMF model watcher is done..
    }

    /**
     * Creates the list of points for the given connection.
     *
     * <p>
     * The order of the points should be as follows:
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
     * @param connection
     *            a {@link GConnection} instance
     *
     * @return a list of the given connection's points
     */
    private List<Point2D> createPoints(final GConnection connection)
    {
        // Middle: joint positions
        final List<Point2D> points = GeometryUtils.getJointPositions(connection, mSkinLookup);

        // Start: Source position
        points.add(0, GeometryUtils.getConnectorPosition(connection.getSource(), mSkinLookup));

        // End: Target position
        points.add(GeometryUtils.getConnectorPosition(connection.getTarget(), mSkinLookup));

        return points;
    }
}