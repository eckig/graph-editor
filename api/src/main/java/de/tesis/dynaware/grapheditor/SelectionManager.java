/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Provides actions related to selections in the graph editor.
 */
public interface SelectionManager {

    /**
     * Gets the observable list of currently-selected nodes.
     *
     * <p>
     * This list is read-only. Nodes should be selected via their skin class.
     * </p>
     *
     * @return the list of selected nodes
     */
    ObservableList<GNode> getSelectedNodes();

    /**
     * Gets the observable list of currently-selected connections.
     *
     * <p>
     * This list is read-only. Connections should be selected via their skin class.
     * </p>
     *
     * @return the list of selected connections
     */
    ObservableList<GConnection> getSelectedConnections();

    /**
     * Gets the observable list of currently-selected joints.
     *
     * <p>
     * This list is read-only. Joints should be selected via their skin class.
     * </p>
     *
     * @return the list of selected joints
     */
    ObservableList<GJoint> getSelectedJoints();

    /**
     * Cuts the current selection. Saves cut nodes and the connections between them to memory to be pasted later.
     */
    void cut();

    /**
     * Cuts the current selection. Saves cut nodes and the connections between them to memory to be pasted later.
     *
     * <p>
     * Additionally calls the given method for the compound command that removed the nodes.
     * </p>
     *
     * @param consumer a consumer to append additional commands to this one
     */
    void cut(BiConsumer<List<GNode>, CompoundCommand> consumer);

    /**
     * Copies the current selection. Saves copied nodes and the connections between them to memory to be pasted later.
     */
    void copy();

    /**
     * Pastes the recently cut or copied selection.
     */
    void paste();

    /**
     * Pastes the recently cut or copied selection.
     *
     * <p>
     * Additionally calls the given method for the compound command that pasted the nodes.
     * </p>
     *
     * @param consumer a consumer to append additional commands to this one
     */
    void paste(BiConsumer<List<GNode>, CompoundCommand> consumer);

    /**
     * Clears the memory of what was cut / copied, so that future paste calls will do nothing.
     */
    void clearMemory();

    /**
     * Selects all selectable elements (nodes, joints, and connections) in the graph editor.
     */
    void selectAll();

    /**
     * Selects all nodes in the graph editor.
     */
    void selectAllNodes();

    /**
     * Selects all joints in the graph editor.
     */
    void selectAllJoints();

    /**
     * Selects all connections in the graph editor.
     */
    void selectAllConnections();

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
    void deleteSelection(BiConsumer<List<GNode>, CompoundCommand> consumer);

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
}
