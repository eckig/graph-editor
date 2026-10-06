/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.window;

import java.util.function.Predicate;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import io.github.eckig.grapheditor.SelectionManager;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import javafx.geometry.Orientation;
import javafx.scene.Node;


/**
 * A minimap for the graph editor.
 *
 * <p>
 * This extends {@link PanningWindowMinimap}, additionally displaying a small
 * rectangle for each of the nodes in the currently edited model.
 * </p>
 */
public class GraphEditorMinimap extends PanningWindowMinimap
{

    // Until the content is set, we don't know the aspect ratio of the minimap. Use this value until then.
    private static final double INITIAL_ASPECT_RATIO = 0.75;

    // Minimap height is not specified here, the minimap's aspect ratio is fixed by the aspect ratio of the content.
    private static final double MINIMAP_WIDTH = 250;

    private final MinimapNodeGroup minimapNodeGroup = new MinimapNodeGroup();

    private GModel model;
    private final CommandStackListener modelChangeListener = _ -> modelChanged();

    /**
     * Set when the model changed, so the connection routes must be re-read after the next connection layout pass.
     */
    private boolean routesOutdated = true;

    /**
     * Creates a new {@link GraphEditorMinimap} instance.
     */
    public GraphEditorMinimap()
    {
        setContentRepresentation(minimapNodeGroup);
        // updates are skipped while hidden, so rebuild as soon as the minimap is shown again
        visibleProperty().addListener((_, _, visible) ->
        {
            if (visible)
            {
                // the skins already drew their final routes while hidden, the rebuild reads them
                routesOutdated = false;
                minimapNodeGroup.draw();
            }
        });
    }

    private void modelChanged()
    {
        routesOutdated = true;
        if (isVisible())
        {
            minimapNodeGroup.draw();
        }
    }

    @Override
    public Orientation getContentBias()
    {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected double computePrefWidth(double height)
    {
        return MINIMAP_WIDTH;
    }

    @Override
    protected double computeMinWidth(double height)
    {
        return MINIMAP_WIDTH;
    }

    @Override
    protected double computePrefHeight(double width)
    {
        if (width == -1)
        {
            return super.computePrefHeight(width);
        }

        final double contentRatio = getContent() == null ? INITIAL_ASPECT_RATIO : getContent().getHeight() / getContent().getWidth();
        final double widthBeforePadding = width - 2 * MINIMAP_PADDING;
        final double heightBeforePadding = widthBeforePadding * contentRatio;
        // This effectively rounds the height down to an integer.
        return Math.floor(heightBeforePadding) + 2 * MINIMAP_PADDING;
    }

    @Override
    protected double computeMinHeight(double width)
    {
        return computePrefHeight(width);
    }

    /**
     * Set a filter {@link Predicate} to only draw the desired connections onto
     * the minimap. The default is to show all connections.
     *
     * <p>
     * The filter is applied before the route of a connection is resolved. A connection whose route (see
     * {@link #setConnectionRouter(IMinimapConnectionRouter)}) is empty or {@code null} is skipped as well.
     * </p>
     *
     * @param connectionFilter
     *            connection filter {@link Predicate}
     */
    public void setConnectionFilter(final Predicate<GConnection> connectionFilter)
    {
        minimapNodeGroup.setConnectionFilter(connectionFilter);
    }

    /**
     * Sets a custom {@link IMinimapConnectionRouter} supplying the route of each connection drawn onto the minimap.
     *
     * <p>
     * If {@code null} (default), the route displayed by the connection skin ({@link
     * io.github.eckig.grapheditor.GConnectionSkin#getRoutePoints()}) is used if a {@link SkinLookup} is set, falling
     * back to {@link IMinimapConnectionRouter#MODEL}.
     * </p>
     *
     * <p>
     * Use {@link IMinimapConnectionRouter#MODEL} to restore the routing of previous versions.
     * </p>
     *
     * @param pConnectionRouter
     *         {@link IMinimapConnectionRouter} or {@code null}
     */
    public void setConnectionRouter(final IMinimapConnectionRouter pConnectionRouter)
    {
        minimapNodeGroup.setConnectionRouter(pConnectionRouter);
    }

    /**
     * Sets the {@link SkinLookup} used to query the routes of the connection skins.
     *
     * @param pSkinLookup
     *         {@link SkinLookup} or {@code null}
     */
    public void setSkinLookup(final SkinLookup pSkinLookup)
    {
        minimapNodeGroup.setSkinLookup(pSkinLookup);
    }

    /**
     * Notifies the minimap that the connections of the graph editor were laid out (drawn) again.
     *
     * <p>
     * For performance reasons this only refreshes the minimap connections if the model changed since the last refresh
     * (e.g. when a drag gesture is committed), not on every intermediate layout pass while dragging. Called by the
     * graph editor container; use {@link #redrawConnections()} to force a refresh.
     * </p>
     */
    public void onConnectionsLaidOut()
    {
        if (routesOutdated && isVisible())
        {
            routesOutdated = false;
            minimapNodeGroup.requestLayout();
        }
    }

    /**
     * Requests a redraw of the minimap connections, e.g. after connections were re-routed outside of a model change.
     * Cheap to call repeatedly: requests are coalesced into the next layout pass, and the connections are only
     * repainted if their routes actually changed.
     */
    public void redrawConnections()
    {
        minimapNodeGroup.requestLayout();
    }

    /**
     * @param pMinimapRenderer
     *         {@link IMinimapRenderer}
     */
    public <N extends Node> void setMinimapRenderer(final IMinimapRenderer<N> pMinimapRenderer)
    {
        minimapNodeGroup.setMinimapRenderer(pMinimapRenderer);
    }

    /**
     * Sets the selection manager instance currently in use by this graph
     * editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param selectionManager
     *            a {@link SelectionManager} instance
     */
    public void setSelectionManager(final SelectionManager selectionManager)
    {
        minimapNodeGroup.setSelectionManager(selectionManager);
    }

    /**
     * Sets the model to be displayed in this minimap.
     *
     * @param pModel
     *            a {@link GModel} to be displayed
     */
    public void setModel(final GModel pModel)
    {
        // First remove the listener from old model's command stack, if it exists.
        if (model != null)
        {
            final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
            if (domain != null)
            {
                domain.getCommandStack().removeCommandStackListener(modelChangeListener);
            }
        }

        model = pModel;
        routesOutdated = true;
        minimapNodeGroup.setModel(pModel);
        minimapNodeGroup.draw();

        // Now add the listener to the new model's command stack.
        if (pModel != null)
        {
            final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(pModel);
            if (domain != null)
            {
                domain.getCommandStack().addCommandStackListener(modelChangeListener);
            }
        }
    }
}
