/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.core.data.skins.CustomConnectionSkin;
import de.tesis.dynaware.grapheditor.core.data.skins.CustomConnectorSkin;
import de.tesis.dynaware.grapheditor.core.data.skins.CustomJointSkin;
import de.tesis.dynaware.grapheditor.core.data.skins.CustomNodeSkin;
import de.tesis.dynaware.grapheditor.core.data.skins.CustomTailSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultJointSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

@RunWith(MockitoJUnitRunner.class)
public class SkinFactoryTest {

    private final SkinFactory skinFactory = new SkinFactory();

    private static final GNode NODE = GraphFactory.eINSTANCE.createGNode();
    private static final String NODE_TYPE = "peach";

    private static final GConnector CONNECTOR = GraphFactory.eINSTANCE.createGConnector();
    private static final String CONNECTOR_TYPE = "apple";

    private static final GConnection CONNECTION = GraphFactory.eINSTANCE.createGConnection();
    private static final String CONNECTION_TYPE = "pear";

    private static final GJoint JOINT = GraphFactory.eINSTANCE.createGJoint();
    private static final String JOINT_TYPE = "apricot";

    @Before
    public void initialize() {

        final GConnector source = GraphFactory.eINSTANCE.createGConnector();
        final GConnector target = GraphFactory.eINSTANCE.createGConnector();

        source.setType("right-output");
        target.setType("left-input");

        // Dummy source and target to prevent NPE's
        CONNECTION.setSource(source);
        CONNECTION.setTarget(target);
    }

    @Test
    public void testCreateNodeSkin() {

        NODE.setType(null);

        assertNull(skinFactory.createNodeSkin(null));
        assertTrue(skinFactory.createNodeSkin(NODE) instanceof DefaultNodeSkin);

        NODE.setType(NODE_TYPE);

        assertTrue(skinFactory.createNodeSkin(NODE) instanceof DefaultNodeSkin);

        skinFactory.setNodeSkin(NODE_TYPE, CustomNodeSkin.class);

        assertTrue(skinFactory.createNodeSkin(NODE) instanceof CustomNodeSkin);
    }

    @Test
    public void testCreateNodeSkin_badSkinClass() {

        NODE.setType(NODE_TYPE);

        skinFactory.setNodeSkin(NODE_TYPE, GNodeSkin.class);

        assertTrue(skinFactory.createNodeSkin(NODE) instanceof DefaultNodeSkin);
    }

    @Test
    public void testCreateConnectorSkin() {

        CONNECTOR.setType(null);

        assertNull(skinFactory.createConnectorSkin(null));
        assertTrue(skinFactory.createConnectorSkin(CONNECTOR) instanceof DefaultConnectorSkin);

        CONNECTOR.setType(CONNECTOR_TYPE);

        assertTrue(skinFactory.createConnectorSkin(CONNECTOR) instanceof DefaultConnectorSkin);

        CONNECTOR.setType(CONNECTOR_TYPE);

        skinFactory.setConnectorSkin(CONNECTOR_TYPE, CustomConnectorSkin.class);

        assertTrue(skinFactory.createConnectorSkin(CONNECTOR) instanceof CustomConnectorSkin);
    }

    @Test
    public void testCreateConnectorSkin_badSkinClass() {

        CONNECTOR.setType(CONNECTOR_TYPE);

        skinFactory.setConnectorSkin(CONNECTOR_TYPE, GConnectorSkin.class);

        assertTrue(skinFactory.createConnectorSkin(CONNECTOR) instanceof DefaultConnectorSkin);
    }

    @Test
    public void testCreateConnectionSkin() {

        CONNECTION.setType(null);

        assertNull(skinFactory.createConnectionSkin(null));
        assertTrue(skinFactory.createConnectionSkin(CONNECTION) instanceof DefaultConnectionSkin);

        CONNECTION.setType(CONNECTION_TYPE);

        assertTrue(skinFactory.createConnectionSkin(CONNECTION) instanceof DefaultConnectionSkin);

        skinFactory.setConnectionSkin(CONNECTION_TYPE, CustomConnectionSkin.class);

        assertTrue(skinFactory.createConnectionSkin(CONNECTION) instanceof CustomConnectionSkin);
    }

    @Test
    public void testCreateConnectionSkin_badSkinClass() {

        CONNECTION.setType(CONNECTION_TYPE);

        skinFactory.setConnectionSkin(CONNECTION_TYPE, GConnectionSkin.class);

        assertTrue(skinFactory.createConnectionSkin(CONNECTION) instanceof DefaultConnectionSkin);
    }

    @Test
    public void testCreateJointSkin() {

        JOINT.setType(null);

        assertNull(skinFactory.createJointSkin(null));
        assertTrue(skinFactory.createJointSkin(JOINT) instanceof DefaultJointSkin);

        JOINT.setType(JOINT_TYPE);

        assertTrue(skinFactory.createJointSkin(JOINT) instanceof DefaultJointSkin);

        skinFactory.setJointSkin(JOINT_TYPE, CustomJointSkin.class);

        assertTrue(skinFactory.createJointSkin(JOINT) instanceof CustomJointSkin);
    }

    @Test
    public void testCreateJointSkin_badSkinClass() {

        JOINT.setType(JOINT_TYPE);

        skinFactory.setJointSkin(JOINT_TYPE, GJointSkin.class);

        assertTrue(skinFactory.createJointSkin(JOINT) instanceof DefaultJointSkin);
    }

    @Test
    public void testCreateTailSkin() {

        CONNECTOR.setType(null);

        assertNull(skinFactory.createTailSkin(null));
        assertTrue(skinFactory.createTailSkin(CONNECTOR) instanceof DefaultTailSkin);

        CONNECTOR.setType(CONNECTOR_TYPE);

        assertTrue(skinFactory.createTailSkin(CONNECTOR) instanceof DefaultTailSkin);

        CONNECTOR.setType(CONNECTOR_TYPE);

        skinFactory.setTailSkin(CONNECTOR_TYPE, CustomTailSkin.class);

        assertTrue(skinFactory.createTailSkin(CONNECTOR) instanceof CustomTailSkin);
    }

    @Test
    public void testCreateTailSkin_badSkinClass() {

        CONNECTOR.setType(CONNECTOR_TYPE);

        skinFactory.setTailSkin(CONNECTOR_TYPE, GTailSkin.class);

        assertTrue(skinFactory.createTailSkin(CONNECTOR) instanceof DefaultTailSkin);
    }
}
