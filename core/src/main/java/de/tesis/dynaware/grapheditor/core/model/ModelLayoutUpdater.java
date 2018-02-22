/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
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
public class ModelLayoutUpdater {

    private final SkinLookup skinLookup;
    private final ModelEditingManager modelEditingManager;
    private final List<Node> registeredElements = new ArrayList<>();
    private final Supplier<GraphEditorProperties> properties;
    private final EventHandler<MouseEvent> mouseReleasedHandler = event -> elementMouseReleased();

    /**
     * Creates a new model layout updater. Only one instance should exist per
     * graph editor instance.
     *
     * @param skinLookup
     *            the {@link SkinLookup} used to lookup skins
     * @param modelEditingManager
     *            the {@link ModelEditingManager} used to update the model
     *            values
     */
    public ModelLayoutUpdater(final SkinLookup skinLookup, final ModelEditingManager modelEditingManager,
            final Supplier<GraphEditorProperties> pProperties) {

        this.skinLookup = skinLookup;
        this.modelEditingManager = modelEditingManager;
        this.properties  = pProperties;
    }

    /**
     * Initializes the model layout updater for the given model.
     *
     * <p>
     * The model layout updater works by updating the model whenever a
     * <b>mouse-released</b> event occurs on a node or joint. This means the
     * model will be updated at the end of every drag gesture. Before an update
     * is made we check to see if a layout value actually changed. A simple
     * click will therefore not trigger an update.
     * </p>
     *
     * <p>
     * A change to a single node or joint will cause the entire model to be
     * updated. This is because layout of multiple elements may be coupled, for
     * example moving a node may cause the joints inside its connections to
     * move.
     * </p>
     *
     * @param model
     *            the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {
        
        // remove previous event handlers:
        for(final Node root : registeredElements) {
            root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        }
        registeredElements.clear();
        
        // add new event handlers:
        model.getNodes().forEach(this::addNode);
        model.getConnections().stream().flatMap(c -> c.getJoints().stream()).forEach(this::addJoint);
    }

    /**
     * Adds a handler to update the model when a node's layout properties
     * change.
     *
     * @param node
     *            the {@link GNode} whose values should be updated
     */
    public void addNode(final GNode node) {

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null) {
            final Node root = nodeSkin.getRoot();
            if (root != null) {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
                registeredElements.add(root);
            }
        }
    }

    public void removeNode(final GNode node) {

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null) {
            final Node root = nodeSkin.getRoot();
            if (root != null) {
                registeredElements.remove(root);
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
    public void addJoint(final GJoint joint) {

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {
            final Node root = jointSkin.getRoot();
            if (root != null) {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
                registeredElements.add(root);
            }
        }
    }
    
    public void removeJoint(final GJoint joint) {

        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {
            final Node root = jointSkin.getRoot();
            if (root != null) {
                registeredElements.remove(root);
                root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
            }
        }
    }

    private void elementMouseReleased() {
        if (canEdit()) {
            modelEditingManager.updateLayoutValues(skinLookup);
        }
    }

    private boolean canEdit() {
        final GraphEditorProperties props = properties == null ? null : properties.get();
        return props != null && !props.isReadOnly();
    }
}
