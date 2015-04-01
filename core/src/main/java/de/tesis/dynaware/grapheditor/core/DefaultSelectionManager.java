/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.model.ModelEditingManager;
import de.tesis.dynaware.grapheditor.core.selections.SelectionCopier;
import de.tesis.dynaware.grapheditor.core.selections.SelectionCreator;
import de.tesis.dynaware.grapheditor.core.selections.SelectionDeleter;
import de.tesis.dynaware.grapheditor.core.selections.SelectionDragManager;
import de.tesis.dynaware.grapheditor.core.selections.SelectionTracker;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Manages all graph editor logic relating to selections of one or more nodes and/or joints.
 *
 * <p>
 * Delegates certain jobs to the following classes.
 *
 * <ol>
 * <li>SelectionCreator - creates selections of objects via clicking or dragging
 * <li>SelectionDragManager - ensures selected objects move together when one is dragged
 * <li>SelectionDeleter - deletes selected nodes and dependent connectors, connections, etc
 * <li>SelectionBackup - used to backup the current selection and restore it later
 * <li>SelectionTracker - keeps track of the current selection
 * <li>SelectionCopier - copies and stores the current selection to be pasted later
 * </ol>
 *
 * </p>
 */
public class DefaultSelectionManager implements SelectionManager {

    private final SelectionCreator selectionCreator;
    private final SelectionDragManager selectionDragManager;
    private final SelectionDeleter selectionDeleter;
    private final SelectionTracker selectionTracker;
    private final SelectionCopier selectionCopier;

    private GModel model;

    /**
     * Creates a new default selection manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} instance in use
     * @param view the {@link GraphEditorView} instance in use
     * @param modelEditingManager the {@link ModelEditingManager} in use
     */
    public DefaultSelectionManager(final SkinLookup skinLookup, final GraphEditorView view,
            final ModelEditingManager modelEditingManager) {

        selectionDragManager = new SelectionDragManager(skinLookup, view);
        selectionDeleter = new SelectionDeleter(skinLookup, modelEditingManager);
        selectionCreator = new SelectionCreator(skinLookup, view, selectionDragManager);
        selectionTracker = new SelectionTracker(skinLookup);
        selectionCopier = new SelectionCopier(skinLookup, selectionTracker, selectionCreator, selectionDeleter);
    }

    /**
     * Initializes the selection manager for the given model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {

        this.model = model;

        selectionCreator.initialize(model);
        selectionTracker.initialize(model);
        selectionCopier.initialize(model);
    }

    @Override
    public ObservableList<GNode> getSelectedNodes() {
        return selectionTracker.getSelectedNodes();
    }

    @Override
    public ObservableList<GConnection> getSelectedConnections() {
        return selectionTracker.getSelectedConnections();
    }

    @Override
    public ObservableList<GJoint> getSelectedJoints() {
        return selectionTracker.getSelectedJoints();
    }

    @Override
    public void selectAll() {
        selectionCreator.selectAllNodes(true);
        selectionCreator.selectAllJoints(true);
        selectionCreator.selectAllConnections(true);
    }

    @Override
    public void selectAllNodes() {
        selectionCreator.selectAllNodes(true);
    }

    @Override
    public void selectAllJoints() {
        selectionCreator.selectAllJoints(true);
    }

    @Override
    public void selectAllConnections() {
        selectionCreator.selectAllConnections(true);
    }

    @Override
    public void clearSelection() {
        selectionCreator.deselectAll();
    }

    @Override
    public void deleteSelection() {
        selectionDeleter.deleteSelection(model, null);
    }

    @Override
    public void deleteSelection(final BiConsumer<List<GNode>, CompoundCommand> consumer) {
        selectionDeleter.deleteSelection(model, consumer);
    }

    @Override
    public void cut() {
        selectionCopier.cut(null);
    }

    @Override
    public void cut(final BiConsumer<List<GNode>, CompoundCommand> consumer) {
        selectionCopier.cut(consumer);
    }

    @Override
    public void copy() {
        selectionCopier.copy();
    }

    @Override
    public void paste() {
        selectionCopier.paste(null);
    }

    @Override
    public void paste(final BiConsumer<List<GNode>, CompoundCommand> consumer) {
        selectionCopier.paste(consumer);
    }

    @Override
    public void clearMemory() {
        selectionCopier.clearMemory();
    }

    @Override
    public void setConnectionSelectionPredicate(final BiPredicate<GConnectionSkin, Rectangle2D> connectionPredicate) {
        selectionCreator.setConnectionSelectionPredicate(connectionPredicate);
    }
}
