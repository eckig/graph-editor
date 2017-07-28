/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javafx.collections.ObservableSet;
import javafx.geometry.Rectangle2D;
import javafx.util.Pair;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * Provides actions related to selections in the graph editor.
 */
public interface SelectionManager {

    /**
     * Gets the list of currently selected nodes.
     *
     * <p>
     * This list is read-only. Nodes should be selected via {@link #select(EObject)}.
     * </p>
     *
     * @return the list of currently selected nodes
     */
    List<GNode> getSelectedNodes();
    
    /**
     * Gets the list of currently selected connections.
     *
     * <p>
     * This list is read-only. Connections should be selected via {@link #select(EObject)}.
     * </p>
     *
     * @return the list of currently selected connections
     */
    List<GConnection> getSelectedConnections();

    /**
     * Gets the list of currently selected joints.
     *
     * <p>
     * This list is read-only. Joints should be selected via {@link #select(EObject)}.
     * </p>
     *
     * @return the list of currently selected joints
     */
    List<GJoint> getSelectedJoints();
    
    /**
     * Convenience method to inform if the given object is currently selected. Is
     * functionally equivalent to calling
     * <code>getSelectedItems().contains(object)</code>.
     * 
     * @param object
     * @return {@code true} if the given index is selected, {@code false} otherwise.
     */
    boolean isSelected(EObject object);
    
    /**
     * Gets the {@link ObservableSet} of currently-selected items.
     *
     * <p>
     * This set is read-only. Items should be selected via {@link #select(EObject)}.
     * </p>
     *
     * @return the set of selected items
     */
    ObservableSet<EObject> getSelectedItems();
    
    /**
     * This method will attempt to select the given object.
     *
     * @param object The object to attempt to select in the underlying data model.
     */
    void select(EObject object);
    
    /**
     * Selects all selectable elements (nodes, joints, and connections) in the graph editor.
     */
    void selectAll();
    
    /**
     * This method will clear the selection of the given object.
     * If the given object is not selected, nothing will happen.
     *
     * @param object The selected item to deselect.
     */
    void clearSelection(EObject object);

    /**
     * Clears the selection, i.e. de-selects all elements.
     */
    void clearSelection();

    /**
     * Deletes all elements that are currently selected.
     */
    void deleteSelection();

    /**
     * Deletes all nodes and connections that are currently selected.
     *
     * <p>
     * Additionally calls the given method for the compound command that did the deletion.
     * </p>
     *
     * @param consumer a consumer to append additional commands to this one
     */
    void deleteSelection(BiConsumer<Pair<List<GNode>, List<GConnection>>, CompoundCommand> consumer);

    /**
     * Sets an optional predicate to be called when the selection-box changes to see if connections should be selected.
     *
     * <p>
     * The predicate should return true if the connection is inside the selection box. Setting a null predicate means no
     * connections will be selected by the selection-box. This is the default behaviour.
     * </p>
     *
     * @param connectionPredicate a predicate that checks if a connection is inside the selection-box
     */
    void setConnectionSelectionPredicate(BiPredicate<GConnectionSkin, Rectangle2D> connectionPredicate);
    
    /**
     * Sets the editor properties instance for the graph editor.
     *
     * @param editorProperties a {@link GraphEditorProperties} instance to be used
     */
    void setEditorProperties(final GraphEditorProperties editorProperties);
}
