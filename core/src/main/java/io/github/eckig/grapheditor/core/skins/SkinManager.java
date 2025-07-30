package io.github.eckig.grapheditor.core.skins;

import java.util.List;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GraphEditorSkins;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GNode;


/**
 * Graph Editor Skin Manager
 *
 * @since 09.02.2016
 */
public interface SkinManager extends SkinLookup, GraphEditorSkins
{

    /**
     * remove all cached skins and clear the graph editor view
     */
    void clear();

    /**
     * Removes the given {@link GNode} skin from the view
     *
     * @param pNodeToRemove
     *            node to remove
     * @since 10.02.2016
     */
    void removeNode(final GNode pNodeToRemove);

    /**
     * Removes the given {@link GConnector} skin from the view
     *
     * @param pConnectorToRemove
     *            connector to remove
     * @since 10.02.2016
     */
    void removeConnector(final GConnector pConnectorToRemove);

    /**
     * Removes the given {@link GConnection} skin from the view
     *
     * @param pConnectionToRemove
     *            connection to remove
     * @since 10.02.2016
     */
    void removeConnection(final GConnection pConnectionToRemove);

    /**
     * Removes the given {@link GJoint} skin from the view
     *
     * @param pJointToRemove
     *            joint to remove
     * @since 10.02.2016
     */
    void removeJoint(final GJoint pJointToRemove);
    /**
     * Calls {@link GNodeSkin#setConnectorSkins(List)} to update a nodes list of
     * connectors.
     *
     * @param pNode
     *            node to update
     * @since 10.02.2016
     */
    void updateConnectors(final GNode pNode);

    /**
     * Calls {@link GConnectionSkin#setJointSkins(List)} to update a connections
     * list of joints.
     *
     * @param pConnection
     *            connection to update
     * @since 17.02.2016
     */
    void updateJoints(final GConnection pConnection);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pNode
     * @return skin
     * @since 21.01.2019
     */
    GNodeSkin lookupOrCreateNode(final GNode pNode);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pConnector
     * @return skin
     * @since 21.01.2019
     */
    GConnectorSkin lookupOrCreateConnector(final GConnector pConnector);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pConnection
     * @return skin
     * @since 21.01.2019
     */
    GConnectionSkin lookupOrCreateConnection(final GConnection pConnection);
}
