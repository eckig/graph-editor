/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.selections;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.model.ModelEditingManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Responsible for deleting a selection of one or more elements in the graph editor.
 */
public class SelectionDeleter {

    private final SkinLookup skinLookup;
    private final ModelEditingManager modelEditingManager;

    /**
     * Creates a new selection deleter. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup the {@link SkinLookup} used to look up skins
     * @param modelEditingManager the {@link ModelEditingManager} used to make changes to the model
     */
    public SelectionDeleter(final SkinLookup skinLookup, final ModelEditingManager modelEditingManager) {
        this.skinLookup = skinLookup;
        this.modelEditingManager = modelEditingManager;
    }

    /**
     * Deletes all nodes in the current selection and all attached connections.
     *
     * @param model the {@link GModel} currently being edited
     * @param consumer a consumer to allow custom commands to be appended to the delete command
     */
    public void deleteSelection(final GModel model, final BiConsumer<List<GNode>, CompoundCommand> consumer) {

        final List<GNode> nodesToDelete = new ArrayList<>();
        final List<GConnection> connectionsToDelete = new ArrayList<>();

        for (final GNode node : model.getNodes()) {
            if (skinLookup.lookupNode(node).isSelected()) {

                nodesToDelete.add(node);

                for (final GConnector connector : node.getConnectors()) {
                    for (final GConnection connection : connector.getConnections()) {

                        if (connection != null && !connectionsToDelete.contains(connection)) {
                            connectionsToDelete.add(connection);
                        }
                    }
                }
            }
        }

        if (!nodesToDelete.isEmpty() || !connectionsToDelete.isEmpty()) {

            final CompoundCommand command = modelEditingManager.remove(nodesToDelete, connectionsToDelete);

            if (consumer != null) {
                consumer.accept(nodesToDelete, command);
            }
        }
    }
}
