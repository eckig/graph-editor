/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.ModelEditingManager;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;


/**
 * Responsible for updating the {@link GModel}'s layout values at the end of
 * each mouse gesture.
 */
public class ModelLayoutUpdater
{

    private final SkinLookup skinLookup;
    private final ModelEditingManager modelEditingManager;
    private final GraphEditorProperties properties;
    private final EventHandler<MouseEvent> mouseReleasedHandler = event -> elementMouseReleased();

    /**
     * Creates a new model layout updater. Only one instance should exist per
     * graph editor instance.
     *
     * @param pSkinLookup
     *            the {@link SkinLookup} used to lookup skins
     * @param pModelEditingManager
     *            the {@link ModelEditingManager} used to update the model
     *            values
     */
    public ModelLayoutUpdater(final SkinLookup pSkinLookup, final ModelEditingManager pModelEditingManager,
            final GraphEditorProperties pProperties)
    {
        skinLookup = pSkinLookup;
        modelEditingManager = pModelEditingManager;
        properties = pProperties;
    }

    /**
     * Adds a handler to update the model when a node's layout properties
     * change.
     *
     * @param node
     *            the {@link GNode} whose values should be updated
     */
    public void addNode(final GNode node)
    {
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null)
        {
            final Node root = nodeSkin.getRoot();
            if (root != null)
            {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }
    }

    public void removeNode(final GNode node)
    {
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null)
        {
            final Node root = nodeSkin.getRoot();
            if (root != null)
            {
                root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }
    }

    /**
     * Adds a handler to update the model when a joint's layout properties
     * change.
     *
     * @param joint
     *            the {@link GJoint} whose values should be updated
     */
    public void addJoint(final GJoint joint)
    {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null)
        {
            final Node root = jointSkin.getRoot();
            if (root != null)
            {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }
    }

    public void removeJoint(final GJoint joint)
    {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null)
        {
            final Node root = jointSkin.getRoot();
            if (root != null)
            {
                root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }
    }

    private void elementMouseReleased()
    {
        if (canEdit())
        {
            modelEditingManager.updateLayoutValues(skinLookup);
        }
    }

    private boolean canEdit()
    {
        final GraphEditorProperties props = properties == null ? null : properties;
        return props != null && !props.isReadOnly();
    }
}
