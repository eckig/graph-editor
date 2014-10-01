/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.HashMap;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Responsible for updating the {@link GModel}'s layout values at the end of each mouse gesture.
 */
public class ModelLayoutUpdater {

    private final SkinLookup skinLookup;
    private final ModelEditingManager modelEditingManager;

    private final Map<GNode, EventHandler<MouseEvent>> nodeReleasedHandlers = new HashMap<>();
    private final Map<GJoint, EventHandler<MouseEvent>> jointReleasedHandlers = new HashMap<>();

    /**
     * Creates a new model layout updater. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to lookup skins
     * @param modelEditingManager the {@link ModelEditingManager} used to update the model values
     */
    public ModelLayoutUpdater(final SkinLookup skinLookup, final ModelEditingManager modelEditingManager) {

        this.skinLookup = skinLookup;
        this.modelEditingManager = modelEditingManager;
    }

    /**
     * Initializes the model layout updater for the given model.
     *
     * <p>
     * The model layout updater works by updating the model whenever a <b>mouse-released</b> event occurs on a node or
     * joint. This means the model will be updated at the end of every drag gesture. Before an update is made we check
     * to see if a layout value actually changed. A simple click will therefore not trigger an update.
     * </p>
     *
     * <p>
     * A change to a single node or joint will cause the entire model to be updated. This is because layout of multiple
     * elements may be coupled, for example moving a node may cause the joints inside its connections to move.
     * </p>
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        final Map<GNode, EventHandler<MouseEvent>> oldNodeReleasedHandlers = new HashMap<>(nodeReleasedHandlers);
        final Map<GJoint, EventHandler<MouseEvent>> oldJointReleasedHandlers = new HashMap<>(jointReleasedHandlers);

        for (final GNode node : model.getNodes()) {

            if (oldNodeReleasedHandlers.get(node) != null) {
                removeHandler(node, oldNodeReleasedHandlers.get(node));
            }
            addHandler(node);
        }

        for (final GConnection connection : model.getConnections()) {
            for (final GJoint joint : connection.getJoints()) {

                if (oldJointReleasedHandlers.get(joint) != null) {
                    removeHandler(joint, oldJointReleasedHandlers.get(joint));
                }
                addHandler(joint);
            }
        }
    }

    /**
     * Adds a handler to update the model when a node's layout properties change.
     *
     * @param node the {@link GNode} whose values should be updated
     */
    private void addHandler(final GNode node) {

        skinLookup.lookupNode(node).getRoot().setOnMouseReleased(event -> {
            if (checkNodeChanged(node)) {
                modelEditingManager.updateLayoutValues(skinLookup);
            }
        });
    }

    /**
     * Adds a handler to update the model when a joint's layout properties change.
     *
     * @param joint the {@link GJoint} whose values should be updated
     */
    private void addHandler(final GJoint joint) {

        skinLookup.lookupJoint(joint).getRoot().setOnMouseReleased(event -> {
            if (checkJointChanged(joint)) {
                modelEditingManager.updateLayoutValues(skinLookup);
            }
        });
    }

    /**
     * Removes a mouse-released handler from a node skin's root JavaFX node.
     *
     * @param node the {@link GNode} whose handler should be removed
     * @param handler the mouse-released handler to remove
     */
    private void removeHandler(final GNode node, final EventHandler<MouseEvent> handler) {
        skinLookup.lookupNode(node).getRoot().removeEventHandler(MouseEvent.MOUSE_RELEASED, handler);
    }

    /**
     * Removes a mouse-released handler from a joint skin's root JavaFX node.
     *
     * @param joint the {@link GJoint} whose handler should be removed
     * @param handler the mouse-released handler to remove
     */
    private void removeHandler(final GJoint joint, final EventHandler<MouseEvent> handler) {
        skinLookup.lookupJoint(joint).getRoot().removeEventHandler(MouseEvent.MOUSE_RELEASED, handler);
    }

    /**
     * Checks if a node's JavaFX region has different layout values than those currently stored in the model.
     *
     * @param node the model instance for the node
     *
     * @return {@code true} if any layout value has changed, {@code false if not}
     */
    private boolean checkNodeChanged(final GNode node) {

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

        if (nodeSkin == null) {
            return false;
        }

        final Region nodeRegion = nodeSkin.getRoot();

        if (nodeRegion.getLayoutX() != node.getX()) {
            return true;
        } else if (nodeRegion.getLayoutY() != node.getY()) {
            return true;
        } else if (nodeRegion.getWidth() != node.getWidth()) {
            return true;
        } else if (nodeRegion.getHeight() != node.getHeight()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a joint's JavaFX region has different layout values than those currently stored in the model.
     *
     * @param joint the model instance for the joint
     *
     * @return {@code true} if any layout value has changed, {@code false if not}
     */
    private boolean checkJointChanged(final GJoint joint) {

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

        if (jointSkin == null) {
            return false;
        }

        final Region jointRegion = jointSkin.getRoot();

        final double jointRegionX = jointRegion.getLayoutX() + jointSkin.getWidth() / 2;
        final double jointRegionY = jointRegion.getLayoutY() + jointSkin.getHeight() / 2;

        if (jointRegionX != joint.getX()) {
            return true;
        } else if (jointRegionY != joint.getY()) {
            return true;
        }
        return false;
    }
}
