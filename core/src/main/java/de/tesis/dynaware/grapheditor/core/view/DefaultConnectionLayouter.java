/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import javafx.geometry.Point2D;


/**
 * Responsible for telling connection skins to draw themselves.
 */
public class DefaultConnectionLayouter implements ConnectionLayouter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayouter.class);

    private final Map<GConnectionSkin, Point2D[]> mConnectionPoints = new HashMap<>();
    private final SkinLookup mSkinLookup;
    private final Set<GConnection> mDirty = new HashSet<>();
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
    public void markDirty(final GConnection pConnection)
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return;
        }

        mDirty.add(pConnection);
    }

    @Override
    public void redrawDirty()
    {
        if (mDirty.isEmpty())
        {
            return;
        }
        else if (mConnectionPoints.isEmpty())
        {
            redrawAll();
        }

        try
        {
            redrawDirtyConnections();
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not redraw dirty Connections: ", e); //$NON-NLS-1$
        }
    }

    private void redrawDirtyConnections()
    {
        final GConnection[] dirty = mDirty.toArray(new GConnection[mDirty.size()]);
        mDirty.clear();

        for (final GConnection connection : dirty)
        {
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
            if (connectionSkin != null)
            {
                final Point2D[] points = connectionSkin.update();
                if (points != null)
                {
                    mConnectionPoints.put(connectionSkin, points);
                }

                connectionSkin.draw(mConnectionPoints);
            }
        }
    }

    @Override
    public void redrawAll()
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return;
        }

        try
        {
            redrawAllConnections();
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not redraw Connections: ", e); //$NON-NLS-1$
        }
    }

    private void redrawAllConnections()
    {
        mConnectionPoints.clear();

        for (final GConnection connection : mModel.getConnections())
        {
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
            if (connectionSkin != null)
            {
                final Point2D[] points = connectionSkin.update();
                if (points != null)
                {
                    mConnectionPoints.put(connectionSkin, points);
                }
            }
        }

        for (final Entry<GConnectionSkin, Point2D[]> entry : mConnectionPoints.entrySet())
        {
            entry.getKey().draw(mConnectionPoints);
        }
    }
}
