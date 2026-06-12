package io.github.eckig.grapheditor.core.view.impl;

import java.util.HashMap;
import java.util.Map;

import io.github.eckig.grapheditor.SkinLookup;

import io.github.eckig.grapheditor.core.skins.defaults.connection.SimpleConnectionSkin;
import io.github.eckig.grapheditor.core.view.ConnectionLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.model.GModel;
import javafx.geometry.Point2D;


/**
 * Default implementation of {@link ConnectionLayout} that can layout variants of {@link SimpleConnectionSkin}
 */
public class DefaultConnectionLayout implements ConnectionLayout
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayout.class);

    private final SkinLookup mSkinLookup;
    private GModel mModel;

    /**
     * Creates a new {@link DefaultConnectionLayout} instance. Only one
     * instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param pSkinLookup
     *            the {@link SkinLookup} used to look up skins
     */
    public DefaultConnectionLayout(final SkinLookup pSkinLookup)
    {
        mSkinLookup = pSkinLookup;
    }

    @Override
    public void initialize(final GModel pModel)
    {
        mModel = pModel;
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
        final Map<SimpleConnectionSkin, Point2D[]> connectionPoints = new HashMap<>();
        for (final var connection : mModel.getConnections())
        {
            final var connectionSkin = mSkinLookup.lookupConnection(connection);
            if (connectionSkin instanceof SimpleConnectionSkin s)
            {
                final var points = s.update();
                if (points != null)
                {
                    connectionPoints.put(s, points);
                }
            }
        }

        for (final var skin : connectionPoints.keySet())
        {
            skin.draw(connectionPoints);
        }
    }
}
