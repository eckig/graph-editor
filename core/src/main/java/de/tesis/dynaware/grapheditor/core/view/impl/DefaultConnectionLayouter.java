package de.tesis.dynaware.grapheditor.core.view.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.VirtualSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.view.ConnectionLayouter;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import javafx.geometry.Point2D;


/**
 * Default implementation of {@link ConnectionLayouter}
 */
public class DefaultConnectionLayouter implements ConnectionLayouter
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayouter.class);

    private final Set<GConnection> mDirty = new HashSet<>();
    private boolean mRedrawAll = false;
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
        mDirty.addAll(pConnections);
    }

    @Override
    public void redraw(final GConnection pConnection)
    {
        mDirty.add(pConnection);
    }

    @Override
    public void redrawAll()
    {
        mRedrawAll = true;
    }

    @Override
    public void draw()
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return;
        }

        try
        {
            if (mRedrawAll)
            {
                mConnectionPoints.clear();
                if (!mModel.getConnections().isEmpty())
                {
                    redrawAllConnections();
                }
                mRedrawAll = false;
            }
            else if (!mDirty.isEmpty())
            {
                final List<GConnectionSkin> repaint = new ArrayList<>(mDirty.size());
                for (final GConnection conn : mDirty)
                {
                    final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(conn);
                    if (connectionSkin != null && !(connectionSkin instanceof VirtualSkin))
                    {
                        final Point2D[] points = connectionSkin.update();
                        if (points != null)
                        {
                            mConnectionPoints.put(connectionSkin, points);
                        }

                        repaint.add(connectionSkin);
                    }
                }

                for (final GConnectionSkin skin : repaint)
                {
                    skin.draw(mConnectionPoints);
                }
                mDirty.clear();
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
