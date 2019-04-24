package de.tesis.dynaware.grapheditor.core.skins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.VirtualSkin;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultJointSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.core.view.ConnectionLayouter;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.util.Callback;


/**
 * Default {@link SkinManager} implementation
 *
 * @since 09.02.2016
 */
public class GraphEditorSkinManager implements SkinManager
{

    private final GraphEditor mGraphEditor;
    private final GraphEditorView mView;

    private Callback<GNode, GNodeSkin> mNodeSkinFactory;
    private Callback<GConnector, GConnectorSkin> mConnectorSkinFactory;
    private Callback<GConnection, GConnectionSkin> mConnectionSkinFactory;
    private Callback<GJoint, GJointSkin> mJointSkinFactory;
    private Callback<GConnector, GTailSkin> mTailSkinFactory;

    private final Map<GNode, GNodeSkin> mNodeSkins = new HashMap<>();
    private final Map<GConnector, GConnectorSkin> mConnectorSkins = new HashMap<>();
    private final Map<GConnection, GConnectionSkin> mConnectionSkins = new HashMap<>();
    private final Map<GJoint, GJointSkin> mJointSkins = new HashMap<>();
    private final Map<GConnector, GTailSkin> mTailSkins = new HashMap<>();

    private ConnectionLayouter mConnectionLayouter;
    private final Consumer<GSkin<?>> mOnPositionMoved = this::positionMoved;

    /**
     * Creates a new skin manager instance. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     *
     * @param pGraphEditor
     *            {@link GraphEditor}
     * @param pView
     *            {@link GraphEditorView}
     */
    public GraphEditorSkinManager(final GraphEditor pGraphEditor, final GraphEditorView pView)
    {
        mView = pView;
        mGraphEditor = pGraphEditor;
    }

    @Override
    public void setConnectionLayouter(final ConnectionLayouter pConnectionLayouter)
    {
        mConnectionLayouter = pConnectionLayouter;
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> pSkinFactory)
    {
        mNodeSkinFactory = pSkinFactory;
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> pConnectorSkinFactory)
    {
        mConnectorSkinFactory = pConnectorSkinFactory;
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> pConnectionSkinFactory)
    {
        mConnectionSkinFactory = pConnectionSkinFactory;
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> pJointSkinFactory)
    {
        mJointSkinFactory = pJointSkinFactory;
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> pTailSkinFactory)
    {
        mTailSkinFactory = pTailSkinFactory;
    }

    @Override
    public void clear()
    {
        if (!mNodeSkins.isEmpty())
        {
            final GNode[] nodes = mNodeSkins.keySet().toArray(new GNode[0]);
            for (final GNode n : nodes)
            {
                removeNode(n);
            }
        }

        if (!mConnectorSkins.isEmpty())
        {
            final GConnector[] connectors = mConnectorSkins.keySet().toArray(new GConnector[0]);
            for (final GConnector c : connectors)
            {
                removeConnector(c);
            }
        }

        if (!mConnectionSkins.isEmpty())
        {
            final GConnection[] connections = mConnectionSkins.keySet().toArray(new GConnection[0]);
            for (final GConnection c : connections)
            {
                removeConnection(c);
            }
        }

        if (!mJointSkins.isEmpty())
        {
            final GJoint[] joints = mJointSkins.keySet().toArray(new GJoint[0]);
            for (final GJoint c : joints)
            {
                removeJoint(c);
            }
        }

        if (!mTailSkins.isEmpty())
        {
            final GTailSkin[] tails = mTailSkins.values().toArray(new GTailSkin[0]);
            for (final GTailSkin tail : tails)
            {
                mView.remove(tail);
                tail.dispose();
            }
        }

        // remove any remainders that might have been left over:
        mView.clear();
    }

    @Override
    public void removeNode(final GNode pNodeToRemove)
    {
        if (pNodeToRemove != null)
        {
            final GNodeSkin removedSkin = mNodeSkins.remove(pNodeToRemove);
            if (removedSkin != null)
            {
                mView.remove(removedSkin);
                removedSkin.dispose();
            }

            for (int i = 0; i < pNodeToRemove.getConnectors().size(); i++)
            {
                removeConnector(pNodeToRemove.getConnectors().get(i));
            }
        }
    }

    @Override
    public void removeConnector(final GConnector pConnectorToRemove)
    {
        if (pConnectorToRemove != null)
        {
            final GConnectorSkin removedSkin = mConnectorSkins.remove(pConnectorToRemove);
            if (removedSkin != null)
            {
                removedSkin.dispose();
            }
            final GTailSkin removedTailSkin = mTailSkins.remove(pConnectorToRemove);
            if (removedTailSkin != null)
            {
                removedTailSkin.dispose();
            }
        }
    }

    @Override
    public void removeConnection(final GConnection pConnectionToRemove)
    {
        if (pConnectionToRemove != null)
        {
            final GConnectionSkin removedSkin = mConnectionSkins.remove(pConnectionToRemove);
            if (removedSkin != null)
            {
                mView.remove(removedSkin);
                removedSkin.dispose();
            }
        }
    }

    @Override
    public void removeJoint(final GJoint pJointToRemove)
    {
        if (pJointToRemove != null)
        {
            final GJointSkin removedSkin = mJointSkins.remove(pJointToRemove);
            if (removedSkin != null)
            {
                mView.remove(removedSkin);
                removedSkin.dispose();
            }
        }
    }

    @Override
    public void updateConnectors(final GNode pNode)
    {
        final GNodeSkin nodeSkin = mNodeSkins.get(pNode);
        if (nodeSkin != null)
        {
            final List<GConnectorSkin> nodeConnectorSkins = pNode.getConnectors().stream().map(this::lookupConnector)
                    .collect(Collectors.toList());
            nodeSkin.setConnectorSkins(nodeConnectorSkins);
        }
    }

    @Override
    public void updateJoints(final GConnection pConnection)
    {
        final GConnectionSkin connectionSkin = lookupConnection(pConnection);
        if (connectionSkin != null)
        {
            final List<GJointSkin> connectionJointSkins = pConnection.getJoints().stream().map(this::lookupJoint)
                    .collect(Collectors.toList());
            connectionSkin.setJointSkins(connectionJointSkins);
        }
    }

    @Override
    public GNodeSkin lookupOrCreateNode(final GNode pNode)
    {
        return mNodeSkins.computeIfAbsent(pNode, this::createNodeSkin);
    }

    @Override
    public GConnectorSkin lookupOrCreateConnector(final GConnector pConnector)
    {
        return mConnectorSkins.computeIfAbsent(pConnector, this::createConnectorSkin);
    }

    @Override
    public GConnectionSkin lookupOrCreateConnection(final GConnection pConnection)
    {
        return mConnectionSkins.computeIfAbsent(pConnection, this::createConnectionSkin);
    }

    @Override
    public GJointSkin lookupOrCreateJoint(final GJoint pJoint)
    {
        return mJointSkins.computeIfAbsent(pJoint, this::createJointSkin);
    }

    @Override
    public GNodeSkin lookupNode(final GNode pNode)
    {
        return mNodeSkins.get(pNode);
    }

    @Override
    public GConnectorSkin lookupConnector(final GConnector pConnector)
    {
        return mConnectorSkins.get(pConnector);
    }

    @Override
    public GConnectionSkin lookupConnection(final GConnection pConnection)
    {
        return mConnectionSkins.get(pConnection);
    }

    @Override
    public GJointSkin lookupJoint(final GJoint pJoint)
    {
        return mJointSkins.get(pJoint);
    }

    @Override
    public GTailSkin lookupTail(final GConnector pConnector)
    {
        // GTailSkin is always/only created on demand
        return mTailSkins.computeIfAbsent(pConnector, this::createTailSkin);
    }

    private GConnectorSkin createConnectorSkin(final GConnector pConnector)
    {
        GConnectorSkin skin = mConnectorSkinFactory == null ? null : mConnectorSkinFactory.call(pConnector);
        if (skin == null)
        {
            skin = new DefaultConnectorSkin(pConnector);
        }
        skin.setGraphEditor(mGraphEditor);
        return skin;
    }

    private GTailSkin createTailSkin(final GConnector pConnector)
    {
        GTailSkin skin = mTailSkinFactory == null ? null : mTailSkinFactory.call(pConnector);
        if (skin == null)
        {
            skin = new DefaultTailSkin(pConnector);
        }
        skin.setGraphEditor(mGraphEditor);
        return skin;
    }

    private GConnectionSkin createConnectionSkin(final GConnection pConnection)
    {
        GConnectionSkin skin = mConnectionSkinFactory == null ? null : mConnectionSkinFactory.call(pConnection);
        if (skin == null)
        {
            skin = new DefaultConnectionSkin(pConnection);
        }
        skin.setGraphEditor(mGraphEditor);
        if (!(skin instanceof VirtualSkin))
        {
            mView.add(skin);
        }
        return skin;
    }

    private GJointSkin createJointSkin(final GJoint pJoint)
    {
        GJointSkin skin = mJointSkinFactory == null ? null : mJointSkinFactory.call(pJoint);
        if (skin == null)
        {
            skin = new DefaultJointSkin(pJoint);
        }
        skin.setGraphEditor(mGraphEditor);
        skin.getRoot().setEditorProperties(mGraphEditor.getProperties());
        skin.impl_setOnPositionMoved(mOnPositionMoved);
        skin.initialize();
        if (!(skin instanceof VirtualSkin))
        {
            mView.add(skin);
        }
        return skin;
    }

    private GNodeSkin createNodeSkin(final GNode pNode)
    {
        GNodeSkin skin = mNodeSkinFactory == null ? null : mNodeSkinFactory.call(pNode);
        if (skin == null)
        {
            skin = new DefaultNodeSkin(pNode);
        }
        skin.setGraphEditor(mGraphEditor);
        skin.getRoot().setEditorProperties(mGraphEditor.getProperties());
        skin.impl_setOnPositionMoved(mOnPositionMoved);
        skin.initialize();
        if (!(skin instanceof VirtualSkin))
        {
            mView.add(skin);
        }
        return skin;
    }

    private void positionMoved(final GSkin<?> pMovedSkin)
    {
        final ConnectionLayouter layouter = mConnectionLayouter;
        if (layouter == null)
        {
            return;
        }
        if (pMovedSkin instanceof GNodeSkin)
        {
            // redraw all connections attached to each connector of the GNode:
            for (final GConnector connector : ((GNodeSkin) pMovedSkin).getItem().getConnectors())
            {
                layouter.redraw(connector.getConnections());
            }
        }
        else if (pMovedSkin instanceof GJointSkin)
        {
            // redraw the GConnection of the GJoint:
            layouter.redraw(((GJointSkin) pMovedSkin).getItem().getConnection());
        }
    }
}
