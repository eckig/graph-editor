/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.utils.FloorBinding;

/**
 * A minimap for the graph editor.
 *
 * <p>
 * This extends {@link PanningWindowMinimap}, additionally displaying a small rectangle for each of the nodes in the
 * currently edited model.
 * </p>
 */
public class GraphEditorMinimap extends PanningWindowMinimap {

    // Until the content is set, we don't know the aspect ratio of the minimap. Use this value until then.
    private static final double INITIAL_ASPECT_RATIO = 0.75;

    private final MinimapNodeGroup minimapNodeGroup = new MinimapNodeGroup();

    private GModel model;
    private CommandStackListener modelChangeListener;

    private DoubleBinding contentRatio;
    private DoubleBinding widthBeforePadding;
    private DoubleBinding heightBeforePadding;

    /**
     * Creates a new {@link GraphEditorMinimap} instance.
     *
     * @param width the width to be set for this minimap
     */
    public GraphEditorMinimap(final double width) {

        setMinWidth(width);
        setPrefWidth(width);
        setMaxWidth(width);

        setMinHeight(width * INITIAL_ASPECT_RATIO);
        setPrefHeight(width * INITIAL_ASPECT_RATIO);
        setMaxHeight(width * INITIAL_ASPECT_RATIO);

        autosize();

        setContentRepresentation(minimapNodeGroup);
        createModelChangeListener();
    }

    /**
     * Sets the skin lookup instance currently in use by this graph editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param skinLookup a {@link SkinLookup} instance
     */
    public void setSkinLookup(final SkinLookup skinLookup) {
        minimapNodeGroup.setSkinLookup(skinLookup);
    }

    @Override
    public void setContent(final Region content) {
        super.setContent(content);
        bindAspectRatioToContent(content);
    }

    /**
     * Sets the model to be displayed in this minimap.
     *
     * @param model a {@link GModel} to be displayed
     */
    public void setModel(final GModel model) {

        // First remove the listener from old model's command stack, if it exists.
        if (this.model != null) {
            final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(this.model);
            if (domain != null) {
                domain.getCommandStack().removeCommandStackListener(modelChangeListener);
            }
        }

        this.model = model;
        minimapNodeGroup.setModel(model);
        minimapNodeGroup.draw(calculateScaleFactor());

        // Now add the listener to the new model's command stack.
        if (model != null) {
            final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
            if (domain != null) {
                domain.getCommandStack().addCommandStackListener(modelChangeListener);
            }
        }
    }

    /**
     * Binds the aspect ratio of the minimap to the aspect ratio of the content that the minimap is representing.
     */
    private void bindAspectRatioToContent(final Region content) {

        contentRatio = content.heightProperty().divide(content.widthProperty());
        widthBeforePadding = widthProperty().subtract(2 * MINIMAP_PADDING);
        heightBeforePadding = widthBeforePadding.multiply(contentRatio);

        // This effectively rounds the height down to an integer.
        final FloorBinding flooredHeight = new FloorBinding(heightBeforePadding);

        minHeightProperty().bind(flooredHeight.add(2 * MINIMAP_PADDING));
        prefHeightProperty().bind(flooredHeight.add(2 * MINIMAP_PADDING));
        maxHeightProperty().bind(flooredHeight.add(2 * MINIMAP_PADDING));
    }

    /**
     * Creates a change listener that will listen to changes in the model and redraw things when necessary.
     */
    private void createModelChangeListener() {
        modelChangeListener = event -> {
            if (isVisible()) {
                minimapNodeGroup.draw(calculateScaleFactor());
            }
        };
    }
}
