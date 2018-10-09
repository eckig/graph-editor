/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.HashMap;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
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
        mSkinLookup = pSkinLookup;
    }

    @Override
    public void initialize(final GModel pModel)
    {
        mModel = pModel;
    }

    @Override
    public void redraw()
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return;
        }

        final Map<GConnectionSkin, Point2D[]> allPoints = new HashMap<>();

        for (int i = 0; i < mModel.getConnections().size(); i++)
        {
            final GConnection connection = mModel.getConnections().get(i);
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
            final Point2D[] points = connectionSkin.update();
            if (points != null)
            {
                allPoints.put(connectionSkin, points);
            }
        }

        for (final GConnectionSkin skin : allPoints.keySet())
        {
        	skin.draw(allPoints);
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
}