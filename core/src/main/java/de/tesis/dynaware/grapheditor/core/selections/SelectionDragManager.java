/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * Handles how a selection of multiple objects is dragged.
 */
public class SelectionDragManager {

    private final SkinLookup skinLookup;
    private final GraphEditorView view;

    private final Map<GNode, Double> nodeLayoutXOffsets = new HashMap<>();
    private final Map<GNode, Double> nodeLayoutYOffsets = new HashMap<>();

    private final Map<GJoint, Double> jointLayoutXOffsets = new HashMap<>();
    private final Map<GJoint, Double> jointLayoutYOffsets = new HashMap<>();

    private ChangeListener<Number> currentLayoutXListener;
    private ChangeListener<Number> currentLayoutYListener;

    /**
     * Creates a new selection drag manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up skins
     * @param view the {@link GraphEditorView} instance
     */
    public SelectionDragManager(final SkinLookup skinLookup, final GraphEditorView view) {
        this.skinLookup = skinLookup;
        this.view = view;
    }

    /**
     * Binds the positions of all selected objects to have a fixed position relative to a given node.
     *
     * @param node the master {@link GNode} that all selected objects should keep a fixed position relative to
     * @param model the {@link GModel} currently being edited
     */
    public void bindPositions(final GNode node, final GModel model) {
        bindPositions(skinLookup.lookupNode(node).getRoot(), model);
    }

    /**
     * Binds the positions of all selected objects to have a fixed position relative to a given joint.
     *
     * @param joint the master {@link GJoint} that all selected objects should keep a fixed position relative to
     * @param model the {@link GModel} currently being edited
     */
    public void bindPositions(final GJoint joint, final GModel model) {
        bindPositions(skinLookup.lookupJoint(joint).getRoot(), model);
    }

    /**
     * Unbinds the positions of all selected objects.
     *
     * @param node the master {@link GNode} that all selected objects were previously bound to
     */
    public void unbindPositions(final GNode node) {
        if (skinLookup.lookupNode(node) != null) {
            unbindPositions(skinLookup.lookupNode(node).getRoot());
        }
    }

    /**
     * Unbinds the positions of all selected objects.
     *
     * @param joint the master {@link GJoint} that all selected objects were previously bound to
     */
    public void unbindPositions(final GJoint joint) {
        if (skinLookup.lookupJoint(joint) != null) {
            unbindPositions(skinLookup.lookupJoint(joint).getRoot());
        }
    }

    /**
     * Binds the positions of all selected objects to have a fixed position relative to the given draggable box.
     *
     * @param master the master {@link DraggableBox} that all selected objects should keep a fixed position relative to
     * @param model the {@link GModel} currently being edited
     */
    private void bindPositions(final DraggableBox master, final GModel model) {

        storeCurrentOffsets(master, model);
        setEditorBoundsForDrag(master, model);
        addPositionListeners(master, model);
    }

    /**
     * Unbinds the positions of all selected objects.
     *
     * @param node the master {@link DraggableBox} that all selected objects were previously bound to
     */
    private void unbindPositions(final DraggableBox master) {

        removePositionListeners(master);
        restoreEditorProperties(master);
    }

    /**
     * Stores the current offset position of all selected objects with respect to the given master region.
     *
     * @param master the master {@link Region} that all selected objects should keep a fixed position relative to
     * @param model the {@link GModel} currently being edited
     */
    private void storeCurrentOffsets(final Region master, final GModel model) {

        nodeLayoutXOffsets.clear();
        nodeLayoutYOffsets.clear();

        for (final GNode node : model.getNodes()) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin.isSelected() && !nodeSkin.getRoot().equals(master)) {

                final Region slave = nodeSkin.getRoot();

                nodeLayoutXOffsets.put(node, slave.getLayoutX() - master.getLayoutX());
                nodeLayoutYOffsets.put(node, slave.getLayoutY() - master.getLayoutY());
            }
        }

        jointLayoutXOffsets.clear();
        jointLayoutYOffsets.clear();

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

                if (jointSkin.isSelected() && !jointSkin.getRoot().equals(master)) {

                    final Region slave = jointSkin.getRoot();

                    jointLayoutXOffsets.put(joint, slave.getLayoutX() - master.getLayoutX());
                    jointLayoutYOffsets.put(joint, slave.getLayoutY() - master.getLayoutY());
                }
            }
        }
    }

    /**
     * Re-evaluates the current editor bound values based on what objects are selected.
     *
     * <p>
     * Essentially it 'shrinks the walls' of the editor view temporarily during the drag operation, so that it is not
     * possible to drag any object outside the view if multiple objects are selected.
     * </p>
     *
     * @param master the master {@link DraggableBox} that all selected objects should keep a fixed position relative to
     * @param model the {@link GModel} currently being edited
     */
    private void setEditorBoundsForDrag(final DraggableBox master, final GModel model) {

        final GraphEditorProperties propertiesForDrag = new GraphEditorProperties(view.getEditorProperties());

        final BoundOffsets maxOffsets = new BoundOffsets();

        for (final GNode node : model.getNodes()) {

            if (skinLookup.lookupNode(node).isSelected()) {

                final DraggableBox slave = skinLookup.lookupNode(node).getRoot();
                addOffsets(master, slave, maxOffsets);
            }
        }

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                if (skinLookup.lookupJoint(joint).isSelected()) {

                    final DraggableBox slave = skinLookup.lookupJoint(joint).getRoot();
                    addOffsets(master, slave, maxOffsets);
                }
            }
        }

        propertiesForDrag.setNorthBoundValue(propertiesForDrag.getNorthBoundValue() + maxOffsets.northOffset);
        propertiesForDrag.setSouthBoundValue(propertiesForDrag.getSouthBoundValue() + maxOffsets.southOffset);
        propertiesForDrag.setEastBoundValue(propertiesForDrag.getEastBoundValue() + maxOffsets.eastOffset);
        propertiesForDrag.setWestBoundValue(propertiesForDrag.getWestBoundValue() + maxOffsets.westOffset);

        master.setEditorProperties(propertiesForDrag);
    }

    /**
     * Calculates the offset between the given master and slave boxes and adds it to the maxOffsets instance if it is
     * larger than the current maximum value.
     *
     * @param master the master {@link DraggableBox} being dragged
     * @param slave the slave {@link DraggableBox} that is also selected and whose position is bound to the master
     * @param maxOffsets the {@link BoundOffsets} instance storing the current max offsets in all 4 directions
     */
    private void addOffsets(final DraggableBox master, final DraggableBox slave, final BoundOffsets maxOffsets) {

        if (slave.isDragEnabledX()) {

            final double masterX = master.getLayoutX();
            final double masterWidth = master.getWidth();

            final double slaveX = slave.getLayoutX();
            final double slaveWidth = slave.getWidth();

            final double westOffset = masterX - slaveX;
            maxOffsets.westOffset = Math.max(maxOffsets.westOffset, westOffset);

            final double eastOffset = slaveX + slaveWidth - (masterX + masterWidth);
            maxOffsets.eastOffset = Math.max(maxOffsets.eastOffset, eastOffset);
        }

        if (slave.isDragEnabledY()) {

            final double masterY = master.getLayoutY();
            final double masterHeight = master.getHeight();

            final double slaveY = slave.getLayoutY();
            final double slaveHeight = slave.getHeight();

            final double northOffset = master.getLayoutY() - slave.getLayoutY();
            maxOffsets.northOffset = Math.max(maxOffsets.northOffset, northOffset);

            final double southOffset = slaveY + slaveHeight - (masterY + masterHeight);
            maxOffsets.southOffset = Math.max(maxOffsets.southOffset, southOffset);
        }
    }

    /**
     * Resets the editor properties instance in the master draggable box to its original value.
     *
     * @param the master {@link DraggableBox} that was just dragged
     */
    private void restoreEditorProperties(final DraggableBox master) {
        master.setEditorProperties(view.getEditorProperties());
    }

    /**
     * Adds listeners to the master region to update all slave regions accordingly when the master's position changes.
     *
     * @param the master {@link DraggableBox} that is about to be dragged
     * @param model the {@link GModel} currently being edited
     */
    private void addPositionListeners(final Region master, final GModel model) {

        // Remove old listeners just in case there are any.
        removePositionListeners(master);

        currentLayoutXListener = (v, o, n) -> {

            for (final GNode node : model.getNodes()) {

                final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

                if (nodeSkin.isSelected() && !nodeSkin.getRoot().equals(master)) {

                    final Region slave1 = nodeSkin.getRoot();
                    slave1.setLayoutX((Double) n + nodeLayoutXOffsets.get(node));
                }
            }

            for (final GConnection connection : model.getConnections()) {

                for (final GJoint joint : connection.getJoints()) {

                    final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

                    if (jointSkin.isSelected() && !jointSkin.getRoot().equals(master)) {

                        final Region slave2 = jointSkin.getRoot();
                        slave2.setLayoutX((Double) n + jointLayoutXOffsets.get(joint));
                    }
                }
            }
        };

        currentLayoutYListener = (v, o, n) -> {

            for (final GNode node : model.getNodes()) {

                final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

                if (nodeSkin.isSelected() && !nodeSkin.getRoot().equals(master)) {

                    final Region slave1 = nodeSkin.getRoot();
                    slave1.setLayoutY((Double) n + nodeLayoutYOffsets.get(node));
                }
            }

            for (final GConnection connection : model.getConnections()) {

                for (final GJoint joint : connection.getJoints()) {

                    final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

                    if (jointSkin.isSelected() && !jointSkin.getRoot().equals(master)) {

                        final Region slave2 = jointSkin.getRoot();
                        slave2.setLayoutY((Double) n + jointLayoutYOffsets.get(joint));
                    }
                }
            }
        };

        master.layoutXProperty().addListener(currentLayoutXListener);
        master.layoutYProperty().addListener(currentLayoutYListener);
    }

    /**
     * Removes the position listeners from the given master region.
     *
     * @param master the master {@link DraggableBox} that was just dragged
     */
    private void removePositionListeners(final Region master) {

        if (currentLayoutXListener != null) {
            master.layoutXProperty().removeListener(currentLayoutXListener);
        }
        if (currentLayoutYListener != null) {
            master.layoutYProperty().removeListener(currentLayoutYListener);
        }
    }

    /**
     * A class to store the bound offsets for each direction (north, south, east, west).
     */
    private class BoundOffsets {

        public double northOffset;
        public double southOffset;
        public double eastOffset;
        public double westOffset;
    }
}
