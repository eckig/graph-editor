/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import javafx.geometry.Point2D;


/**
 * Default implementation of {@link ConnectionLayouter}
 */
public class DefaultConnectionLayouter implements ConnectionLayouter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayouter.class);

    private final Map<GConnectionSkin, Point2D[]> mConnectionPoints = new HashMap<>();
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
    public void redraw(final Collection<GConnection> pConnections)
    {
        if (mConnectionPoints.isEmpty())
        {
            // we need all points of all connection first:
            redrawAll();
        }

        try
        {
            for (final GConnection connection : pConnections)
            {
                if (connection == null)
                {
                    break;
                }
                redrawSingleConnection(connection);
            }
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not redraw dirty Connections: ", e); //$NON-NLS-1$
        }
    }

    @Override
    public void redraw(final GConnection pConnection)
    {
        if (mConnectionPoints.isEmpty())
        {
            // we need all points of all connection first:
            redrawAll();
        }

        try
        {
            redrawSingleConnection(pConnection);
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not redraw dirty Connections: ", e); //$NON-NLS-1$
        }
    }

    private void redrawSingleConnection(final GConnection pConnection)
    {
        final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(pConnection);
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

    @Override
    public void redrawAll()
    {
        if (mModel == null)
        {
            // not yet initialized:
            return;
        }

        try
        {
            mConnectionPoints.clear();
            if (!mModel.getConnections().isEmpty())
            {
                redrawAllConnections();
            }
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not redraw Connections: ", e); //$NON-NLS-1$
        }
    }

    private void redrawAllConnections()
    {
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

        for (final GConnectionSkin skin : mConnectionPoints.keySet())
        {
            skin.draw(mConnectionPoints);
        }
    }
}
