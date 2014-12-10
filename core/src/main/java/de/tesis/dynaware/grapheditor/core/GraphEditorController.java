/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.List;

import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.CommandStackListener;

import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.core.connections.ConnectionEventManager;
import de.tesis.dynaware.grapheditor.core.connections.ConnectorDragManager;
import de.tesis.dynaware.grapheditor.core.model.ModelEditingManager;
import de.tesis.dynaware.grapheditor.core.model.ModelLayoutUpdater;
import de.tesis.dynaware.grapheditor.core.model.ModelMemory;
import de.tesis.dynaware.grapheditor.core.model.ModelSanityChecker;
import de.tesis.dynaware.grapheditor.core.skins.SkinManager;
import de.tesis.dynaware.grapheditor.core.validators.ValidatorManager;
import de.tesis.dynaware.grapheditor.core.view.ConnectionLayouter;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * The central controller class for the default graph editor implementation.
 *
 * <p>
 * Responsible for using the {@link SkinManager} to create all skin instances for the current {@link GModel}, and adding
 * them to the {@link GraphEditorView}.
 * </p>
 *
 * <p>
 * Also responsible for creating all secondary managers like the {@link ConnectorDragManager} and reinitializing them
 * when the model changes.
 * </p>
 */
public class GraphEditorController {

    private final SkinManager skinManager;
    private final GraphEditorView view;

    private final CommandStackListener commandStackListener;
    private final ModelEditingManager modelEditingManager;
    private final ModelLayoutUpdater modelLayoutUpdater;
    private final ModelMemory modelMemory;

    private final ConnectionLayouter connectionLayouter;
    private final ConnectorDragManager connectorDragManager;

    private final DefaultSelectionManager selectionManager;

    private GModel model;

    /**
     * Creates a new controller instance. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param skinManager the {@link SkinManager} instance
     * @param validatorManager the {@link ValidatorManager} instance
     * @param connectionEventManager the {@link ConnectionEventManager} instance
     */
    public GraphEditorController(final SkinManager skinManager, final ValidatorManager validatorManager,
            final ConnectionEventManager connectionEventManager) {

        this.skinManager = skinManager;

        view = new GraphEditorView();

        commandStackListener = createCommandStackListener();
        modelEditingManager = new ModelEditingManager(commandStackListener);
        modelLayoutUpdater = new ModelLayoutUpdater(skinManager, modelEditingManager);
        modelMemory = new ModelMemory();
        connectionLayouter = new ConnectionLayouter(skinManager);
        connectorDragManager = new ConnectorDragManager(skinManager, validatorManager, connectionEventManager, view);
        selectionManager = new DefaultSelectionManager(skinManager, view, modelEditingManager);

        view.setConnectionLayouter(connectionLayouter);
    }

    /**
     * Gets the {@link GraphEditorView} instance. Returned as a {@link Region} so that it's read-only.
     *
     * @return a {@link Region} containing the entire graph editor
     */
    public Region getView() {
        return view;
    }

    /**
     * Sets the graph model to be edited.
     *
     * @param model the {@link GModel} to be edited
     */
    public void setModel(final GModel model) {

        this.model = model;

        modelMemory.wipe();
        view.clear();

        // Perform single null check here. All secondary managers can assume that the model is not null.
        if (model != null) {
            initializeAll();
        }
    }

    /**
     * Gets the graph model currently being edited.
     *
     * @return the {@link GModel} being edited, or {@code null} if no model was ever set
     */
    public GModel getModel() {
        return model;
    }

    /**
     * Gets the editor properties instance used by the graph editor.
     *
     * @return the {@link GraphEditorProperties} instance in use
     */
    public GraphEditorProperties getEditorProperties() {
        return view.getEditorProperties();
    }

    /**
     * Sets the editor properties instance for the graph editor.
     *
     * @param editorProperties a {@link GraphEditorProperties} instance to be used
     */
    public void setEditorProperties(final GraphEditorProperties editorProperties) {
        view.setEditorProperties(editorProperties);
    }

    /**
     * Gets the selection manager currently being used.
     */
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    /**
     * Initializes everything for the current model.
     */
    public void initializeAll() {

        ModelSanityChecker.validate(model);

        modelMemory.setNewModelState(model);
        reloadView();

        modelEditingManager.initialize(model);
        modelLayoutUpdater.initialize(model);
        connectionLayouter.initialize(model);
        connectorDragManager.initialize(model);
        selectionManager.initialize(model);
    }

    /**
     * Creates the listener implementation for when a command is executed on the command stack.
     *
     * <p>
     * The current approach is brute force - the entire view and all secondary managers are reinitialized.
     * </p>
     *
     * @return the created command stack listener
     */
    private CommandStackListener createCommandStackListener() {
        return event -> initializeAll();
    }

    /**
     * Reloads the view according to the new model values.
     *
     * <p>
     * Uses the skin manager to create skins for the current model, and adds the skin instances to the view.
     * </p>
     */
    private void reloadView() {

        cleanUpView();
        updateSkinManager();

        for (final GNode node : modelMemory.getNodesToAdd()) {
            view.add(skinManager.lookupNode(node));
        }

        for (final GConnection connection : modelMemory.getConnectionsToAdd()) {
            view.add(skinManager.lookupConnection(connection));
        }

        for (final List<GJoint> joints : modelMemory.getJointsToAdd().values()) {
            for (final GJoint joint : joints) {
                view.add(skinManager.lookupJoint(joint));
            }
        }
    }

    /**
     * Cleans up the view, removing all elements that the {@link ModelMemory} tells us to remove.
     */
    private void cleanUpView() {

        for (final GNode node : modelMemory.getNodesToRemove()) {
            view.remove(skinManager.lookupNode(node));
        }

        for (final GConnection connection : modelMemory.getConnectionsToRemove()) {
            view.remove(skinManager.lookupConnection(connection));
        }

        for (final List<GJoint> joints : modelMemory.getJointsToRemove().values()) {
            for (final GJoint joint : joints) {
                view.remove(skinManager.lookupJoint(joint));
            }
        }
    }

    /**
     * Updates the skin manager, adding and removing skin instances according to what the {@link ModelMemory} specifies.
     */
    private void updateSkinManager() {

        skinManager.addNodes(modelMemory.getNodesToAdd());
        skinManager.removeNodes(modelMemory.getNodesToRemove());
        skinManager.updateNodes(modelMemory.getNodesToUpdate());
        skinManager.removeConnectors(modelMemory.getConnectorsToRemove());

        skinManager.addConnections(modelMemory.getConnectionsToAdd());
        skinManager.removeConnections(modelMemory.getConnectionsToRemove());

        for (final List<GJoint> joints : modelMemory.getJointsToRemove().values()) {
            skinManager.removeJoints(joints);
        }

        for (final GConnection connection : modelMemory.getJointsToAdd().keySet()) {
            skinManager.addJoints(connection, modelMemory.getJointsToAdd().get(connection));
        }

        skinManager.initializeAll();
    }
}
