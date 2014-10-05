/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.ResizableBox;

/**
 * Responsible for backing up what nodes and joints are currently selected.
 *
 * <p>
 * The backup and restore methods can be used to keep the selection undisturbed when a model is reloaded, for instance
 * after an undo or redo operation.
 * </p>
 */
public class SelectionBackup {

    private static final String NODE_LAYER_SELECTOR = "#nodeLayer";
    private static final String CONNECTION_LAYER_SELECTOR = "#connectionLayer";

    final private List<ResizableBox> nodeRoots = new ArrayList<>();
    final private List<DraggableBox> jointRoots = new ArrayList<>();

    final private Set<GNode> selectedNodes = new HashSet<>();
    final private Set<GJoint> selectedJoints = new HashSet<>();

    private final SkinLookup skinLookup;
    private final Region view;

    private GModel model;

    /**
     * Creates a new {@link SelectionBackup} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to lookup skins
     * @param view the graph editor view
     */
    public SelectionBackup(final SkinLookup skinLookup, final Region view) {
        this.skinLookup = skinLookup;
        this.view = view;
    }

    /**
     * Initializes the selection backup for the given model instance.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {
        this.model = model;
    }

    /**
     * Backs up the selection state, i.e. what nodes and joints are currently selected.
     *
     * <p>
     * The z-ordering of the root nodes of node and joint skins is also backed up.
     * </p>
     */
    public void backup() {

        nodeRoots.clear();
        jointRoots.clear();

        selectedNodes.clear();
        selectedJoints.clear();

        final Parent nodeLayer = (Parent) view.lookup(NODE_LAYER_SELECTOR);
        final Parent connectionLayer = (Parent) view.lookup(CONNECTION_LAYER_SELECTOR);

        for (final Node nodeRoot : nodeLayer.getChildrenUnmodifiable()) {
            if (nodeRoot instanceof ResizableBox) {

                nodeRoots.add((ResizableBox) nodeRoot);

                final GNodeSkin nodeSkin = getNodeSkin(nodeRoot, model, skinLookup);

                if (nodeSkin.isSelected()) {
                    selectedNodes.add(nodeSkin.getNode());
                }
            }
        }

        for (final Node jointRoot : connectionLayer.getChildrenUnmodifiable()) {
            if (jointRoot instanceof DraggableBox) {

                jointRoots.add((DraggableBox) jointRoot);

                final GJointSkin jointSkin = getJointSkin(jointRoot, model, skinLookup);

                if (jointSkin.isSelected()) {
                    selectedJoints.add(jointSkin.getJoint());
                }
            }
        }
    }

    /**
     * Restores the selection state from backup, i.e. reselects nodes and joints that were selected at the last backup.
     *
     * <p>
     * The z-ordering of the root nodes and joints is also restored.
     * </p>
     */
    public void restore() {

        for (final GNode node : selectedNodes) {
            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
            if (nodeSkin != null) {
                nodeSkin.setSelected(true);
            }
        }

        for (final GJoint joint : selectedJoints) {
            final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
            if (jointSkin != null) {
                jointSkin.setSelected(true);
            }
        }

        for (final ResizableBox nodeRoot : nodeRoots) {
            nodeRoot.toFront();
        }

        for (final DraggableBox jointRoot : jointRoots) {
            jointRoot.toFront();
        }
    }

    /**
     * Gets the node skin instance for a given root JavaFX node.
     *
     * @param root the root JavaFX {@link Node}
     * @param model the {@link GModel} currently being edited
     * @param skinLookup the {@link SkinLookup} for the graph editor
     *
     * @return the {@link GNodeSkin} instance containing the given root JavaFX node, or {@code null} if nothing was
     * found
     */
    private GNodeSkin getNodeSkin(final Node root, final GModel model, final SkinLookup skinLookup) {

        for (final GNode node : model.getNodes()) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin.getRoot().equals(root)) {
                return nodeSkin;
            }
        }

        return null;
    }

    /**
     * Gets the joint skin instance for a given root JavaFX node.
     *
     * @param root the root JavaFX {@link Node}
     * @param model the {@link GModel} currently being edited
     * @param skinLookup the {@link SkinLookup} for the graph editor
     *
     * @return the {@link GJointSkin} containing the given root JavaFX node, or {@code null} if nothing was found
     */
    private GJointSkin getJointSkin(final Node root, final GModel model, final SkinLookup skinLookup) {

        for (final GConnection connection : model.getConnections()) {
            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

                if (jointSkin.getRoot().equals(root)) {
                    return jointSkin;
                }
            }
        }

        return null;
    }
}
