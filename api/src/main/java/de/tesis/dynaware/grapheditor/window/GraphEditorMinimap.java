/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import java.util.function.Predicate;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import javafx.geometry.Orientation;


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
    private final CommandStackListener modelChangeListener = event -> minimapNodeGroup.draw();

    /**
     * Creates a new {@link GraphEditorMinimap} instance.
     */
    public GraphEditorMinimap()
    {
        setContentRepresentation(minimapNodeGroup);
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
     * @param connectionFilter
     *            connection filter {@link Predicate}
     */
    public void setConnectionFilter(final Predicate<GConnection> connectionFilter)
    {
        minimapNodeGroup.setConnectionFilter(connectionFilter);
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
