/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.RemoveContext;


/**
 * Provides utility methods to edit the graph model via EMF commands.
 */
public interface ModelEditingManager
{

    /**
     * Initializes the model editing manager for the given model instance.
     *
     * @param model
     *            the new {@link GModel} to be edited
     */
    void initialize(final GModel model);

    /**
     * Sets a method to be called when a connection is removed in the editor.
     *
     * <p>
     * This can be used to create additional commands to the one that removed
     * the connection.
     * </p>
     *
     * @param pOnConnectionRemoved
     *            a {@link BiFunction} creating the additional command
     */
    void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved);

    /**
     * Sets a method to be called when a node is removed in the editor.
     *
     * <p>
     * This can be used to create additional commands to the one that removed
     * the node.
     * </p>
     *
     * @param pOnNodeRemoved
     *            a {@link Function} creating the additional command
     */
    void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved);

    /**
     * Silently updates the model's layout values to match those in the skin
     * instances.
     *
     * @param skinLookup
     *            the {@link SkinLookup} used to lookup skin instances
     */
    void updateLayoutValues(final SkinLookup skinLookup);


    /**
     * Removes the given elements from the graph editor
     * 
     * @param pToRemove
     *            elements to remove
     */
    void remove(final Collection<EObject> pToRemove);
}
