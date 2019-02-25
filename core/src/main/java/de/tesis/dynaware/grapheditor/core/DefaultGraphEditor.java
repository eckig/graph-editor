/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

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
import de.tesis.dynaware.grapheditor.core.model.ModelEditingManager;
import de.tesis.dynaware.grapheditor.core.skins.SkinManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import de.tesis.dynaware.grapheditor.utils.RemoveContext;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.layout.Region;
import javafx.util.Callback;


/**
 * Default implementation of the {@link GraphEditor}.
 */
public class DefaultGraphEditor implements GraphEditor
{

    private final SkinManager mSkinManager;
    private final ConnectionEventManager mConnectionEventManager = new ConnectionEventManager();
    private final GraphEditorController mController;

    private final ObjectProperty<GModel> mModelProperty = new ObjectPropertyBase<GModel>()
    {

        @Override
        protected void invalidated()
        {
            mController.setModel(get());
        }

        @Override
        public Object getBean()
        {
            return DefaultGraphEditor.this;
        }

        @Override
        public String getName()
        {
            return "model";
        }

    };

    /**
     * Creates a new default implementation of the {@link GraphEditor}.
     */
    public DefaultGraphEditor()
    {
        // Skin manager needs 'this' reference so users can access GraphEditor inside their custom skins.
        mSkinManager = new SkinManager(this);

        mController = new GraphEditorController(mSkinManager, mConnectionEventManager);

        // Create some default layout properties in case the user never sets any.
        setProperties(new GraphEditorProperties());
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> pSkinFactory)
    {
        mSkinManager.setNodeSkinFactory(pSkinFactory);
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> pConnectorSkinFactory)
    {
        mSkinManager.setConnectorSkinFactory(pConnectorSkinFactory);
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> pConnectionSkinFactory)
    {
        mSkinManager.setConnectionSkinFactory(pConnectionSkinFactory);
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> pJointSkinFactory)
    {
        mSkinManager.setJointSkinFactory(pJointSkinFactory);
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> pTailSkinFactory)
    {
        mSkinManager.setTailSkinFactory(pTailSkinFactory);
    }

    @Override
    public void setConnectorValidator(final GConnectorValidator pValidator)
    {
        mController.setConnectorValidator(pValidator);
    }

    @Override
    public void setModel(final GModel pModel)
    {
        mModelProperty.set(pModel);
    }

    @Override
    public GModel getModel()
    {
        return mModelProperty.get();
    }

    @Override
    public void reload()
    {
        mController.initializeAll();
    }

    @Override
    public ObjectProperty<GModel> modelProperty()
    {
        return mModelProperty;
    }

    @Override
    public Region getView()
    {
        return mController.getView();
    }

    @Override
    public GraphEditorProperties getProperties()
    {
        return mController.getEditorProperties();
    }

    @Override
    public void setProperties(final GraphEditorProperties pEditorProperties)
    {
        mController.setEditorProperties(pEditorProperties);
    }

    @Override
    public SkinLookup getSkinLookup()
    {
        return mSkinManager;
    }

    @Override
    public SelectionManager getSelectionManager()
    {
        return mController.getSelectionManager();
    }

    @Override
    public void setOnConnectionCreated(final Function<GConnection, Command> pConsumer)
    {
        mConnectionEventManager.setOnConnectionCreated(pConsumer);
    }

    @Override
    public void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved)
    {
        mConnectionEventManager.setOnConnectionRemoved(pOnConnectionRemoved);
        getModelEditingManager().setOnConnectionRemoved(pOnConnectionRemoved);
    }

    @Override
    public void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved)
    {
        getModelEditingManager().setOnNodeRemoved(pOnNodeRemoved);
    }

    @Override
    public void delete(Collection<EObject> pItems)
    {
        getModelEditingManager().remove(pItems);
    }

    private ModelEditingManager getModelEditingManager()
    {
        return mController.getModelEditingManager();
    }
}
