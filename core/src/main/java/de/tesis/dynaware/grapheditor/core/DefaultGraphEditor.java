/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.function.BiConsumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.connections.ConnectionEventManager;
import de.tesis.dynaware.grapheditor.core.skins.SkinManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.util.Callback;

/**
 * Default implementation of the {@link GraphEditor}.
 */
public class DefaultGraphEditor implements GraphEditor {

    private final SkinManager skinManager;
    private final ConnectionEventManager connectionEventManager = new ConnectionEventManager();
    private final GraphEditorController controller;
    private final ObjectProperty<GModel> modelProperty = new SimpleObjectProperty<GModel>() {

        @Override
        protected void invalidated() {
            super.invalidated();
            controller.setModel(get());
        }
        
    };

    /**
     * Creates a new default implementation of the {@link GraphEditor}.
     */
    public DefaultGraphEditor() {

        // Skin manager needs 'this' reference so users can access GraphEditor inside their custom skins.
        skinManager = new SkinManager(this);

        controller = new GraphEditorController(skinManager, connectionEventManager);

        // Create some default layout properties in case the user never sets any.
        setProperties(new GraphEditorProperties());
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> skinFactory) {
        skinManager.setNodeSkinFactory(skinFactory);
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> connectorSkinFactory) {
        skinManager.setConnectorSkinFactory(connectorSkinFactory);
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> connectionSkinFactory) {
        skinManager.setConnectionSkinFactory(connectionSkinFactory);
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> jointSkinFactory) {
        skinManager.setJointSkinFactory(jointSkinFactory);
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> tailSkinFactory) {
        skinManager.setTailSkinFactory(tailSkinFactory);
    }

    @Override
    public void setConnectorValidator(final GConnectorValidator validator) {
        controller.setConnectorValidator(validator);
    }

    @Override
    public void setModel(final GModel model) {
        modelProperty.set(model);
    }

    @Override
    public GModel getModel() {
        return modelProperty.get();
    }

    @Override
    public void reload() {
        controller.initializeAll();
    }

    @Override
    public ObjectProperty<GModel> modelProperty() {
        return modelProperty;
    }

    @Override
    public Region getView() {
        return controller.getView();
    }

    @Override
    public GraphEditorProperties getProperties() {
        return controller.getEditorProperties();
    }

    @Override
    public void setProperties(final GraphEditorProperties editorProperties) {
        controller.setEditorProperties(editorProperties);
    }

    @Override
    public SkinLookup getSkinLookup() {
        return skinManager;
    }

    @Override
    public SelectionManager getSelectionManager() {
        return controller.getSelectionManager();
    }

    @Override
    public void setOnConnectionCreated(final BiConsumer<GConnection, CompoundCommand> consumer) {
        connectionEventManager.setOnConnectionCreated(consumer);
    }

    @Override
    public void setOnConnectionRemoved(final BiConsumer<GConnection, CompoundCommand> consumer) {
        connectionEventManager.setOnConnectionRemoved(consumer);
    }
}
