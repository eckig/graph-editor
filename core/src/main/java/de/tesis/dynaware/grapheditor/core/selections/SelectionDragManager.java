/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Handles how a selection of multiple objects is dragged.
 */
public class SelectionDragManager {

    private final SkinLookup skinLookup;
    private final GraphEditorView view;
    private final SelectionManager selectionManager;

    private final ChangeListener<Number> layoutXListener = (v, o, n) -> masterMovedX(n.doubleValue());
    private final ChangeListener<Number> layoutYListener = (v, o, n) -> masterMovedY(n.doubleValue());

    private final List<DraggableBox> currentSelectedElements = new ArrayList<>();
    private double[] elementLayoutXOffsets; // array index == List index
    private double[] elementLayoutYOffsets; // array index == List index

    private DraggableBox master;
    private EventHandler<MouseEvent> removeOnReleased;

    /**
     * Creates a new selection drag manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up skins
     * @param view the {@link GraphEditorView} instance
     * @param selectionManager the {@link SelectionManager} instance
     */
    public SelectionDragManager(final SkinLookup skinLookup, final GraphEditorView view,
            final SelectionManager selectionManager) {
        this.skinLookup = skinLookup;
        this.view = view;
        this.selectionManager = selectionManager;
    }

    private void masterMovedX(final double x)
    {
        if (master != null)
        {
            for (int i = 0; i < currentSelectedElements.size(); i++)
            {
                final DraggableBox node = currentSelectedElements.get(i);
                if (node != master)
                {
                    node.setLayoutX(x + elementLayoutXOffsets[i]);
                    node.positionMoved();
                }
            }
        }
    }

    private void masterMovedY(final double y)
    {
        if (master != null)
        {
            for (int i = 0; i < currentSelectedElements.size(); i++)
            {
                final DraggableBox node = currentSelectedElements.get(i);
                if (node != master)
                {
                    node.setLayoutY(y + elementLayoutYOffsets[i]);
                    node.positionMoved();
                }
            }
        }
    }

    /**
     * Binds the positions of all selected objects to have a fixed position relative to the given draggable box.
     *
     * @param pMaster the master {@link DraggableBox} that all selected objects should keep a fixed position relative to
     */
    public void bindPositions(final DraggableBox pMaster) {

        // clean up
        currentSelectedElements.clear();
        if (master != null) {
            removePositionListeners(master);
        }

        // store the currently selected elements of interest
        // (the ones we want to move alongside the master):
        for (final EObject selected : selectionManager.getSelectedItems()) {

            if (selected instanceof GNode) {

                final GNodeSkin skin = skinLookup.lookupNode((GNode) selected);
                if (skin != null) {
                    currentSelectedElements.add(skin.getRoot());
                }
            } else if (selected instanceof GJoint) {

                final GJointSkin skin = skinLookup.lookupJoint((GJoint) selected);
                if (skin != null) {
                    currentSelectedElements.add(skin.getRoot());
                }
            }
        }

        // shortcut: if no element is selected or
        // if only the master element is selected we do not need to attach any listeners
        if (currentSelectedElements.isEmpty()
                || currentSelectedElements.size() == 1 && currentSelectedElements.get(0) == pMaster) {
            return;
        }

        master = pMaster;

        storeCurrentOffsets(pMaster);
        setEditorBoundsForDrag(pMaster);
        addPositionListeners(pMaster);
    }

    /**
     * Unbinds the positions of all selected objects.
     *
     * @param node the master {@link DraggableBox} that all selected objects were previously bound to
     */
    private void unbindPositions(final DraggableBox master) {

        removePositionListeners(master);
        restoreEditorProperties(master);

        currentSelectedElements.clear();
        elementLayoutXOffsets = null;
        elementLayoutYOffsets = null;

        this.master = null;
    }

    /**
     * Stores the current offset position of all selected objects with respect to the given master region.
     *
     * @param master the master {@link Region} that all selected objects should keep a fixed position relative to
     */
    private void storeCurrentOffsets(final DraggableBox master) {

        elementLayoutYOffsets = new double[currentSelectedElements.size()];
        elementLayoutXOffsets = new double[currentSelectedElements.size()];

        for (int i = 0; i < currentSelectedElements.size(); i++) {

            final DraggableBox node = currentSelectedElements.get(i);
            if (node != master) {
                elementLayoutXOffsets[i] = node.getLayoutX() - master.getLayoutX();
                elementLayoutYOffsets[i] = node.getLayoutY() - master.getLayoutY();
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
     */
    private void setEditorBoundsForDrag(final DraggableBox master) {

        final GraphEditorProperties propertiesForDrag = new GraphEditorProperties(view.getEditorProperties());

        final BoundOffsets maxOffsets = new BoundOffsets();

        for (final DraggableBox node : currentSelectedElements) {
            addOffsets(master, node, maxOffsets);
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

        final double masterX = master.getLayoutX();
        final double masterWidth = master.getWidth();

        final double slaveX = slave.getLayoutX();
        final double slaveWidth = slave.getWidth();

        final double westOffset = masterX - slaveX;
        maxOffsets.westOffset = Math.max(maxOffsets.westOffset, westOffset);

        final double eastOffset = slaveX + slaveWidth - (masterX + masterWidth);
        maxOffsets.eastOffset = Math.max(maxOffsets.eastOffset, eastOffset);

        final double masterY = master.getLayoutY();
        final double masterHeight = master.getHeight();

        final double slaveY = slave.getLayoutY();
        final double slaveHeight = slave.getHeight();

        final double northOffset = master.getLayoutY() - slave.getLayoutY();
        maxOffsets.northOffset = Math.max(maxOffsets.northOffset, northOffset);

        final double southOffset = slaveY + slaveHeight - (masterY + masterHeight);
        maxOffsets.southOffset = Math.max(maxOffsets.southOffset, southOffset);
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
     */
    private void addPositionListeners(final DraggableBox master) {

        master.layoutXProperty().addListener(layoutXListener);
        master.layoutYProperty().addListener(layoutYListener);

        removeOnReleased = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                unbindPositions(master);
                master.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
            }
        };
        master.addEventHandler(MouseEvent.MOUSE_RELEASED, removeOnReleased);
    }

    /**
     * Removes the position listeners from the given master region.
     *
     * @param master the master {@link DraggableBox} that was just dragged
     */
    private void removePositionListeners(final DraggableBox master) {

        master.layoutXProperty().removeListener(layoutXListener);
        master.layoutYProperty().removeListener(layoutYListener);

        if (removeOnReleased != null) {
            master.removeEventHandler(MouseEvent.MOUSE_RELEASED, removeOnReleased);
            removeOnReleased = null;
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
