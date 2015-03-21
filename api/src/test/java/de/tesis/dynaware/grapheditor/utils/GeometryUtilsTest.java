/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import static org.junit.Assert.assertEquals;
import javafx.geometry.Point2D;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.data.MockSkinLookupFactory;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

@RunWith(MockitoJUnitRunner.class)
public class GeometryUtilsTest {

    private static final double NODE_X = 55;
    private static final double NODE_Y = 32;
    private static final double CONNECTOR_CENTER_X = 13;
    private static final double CONNECTOR_CENTER_Y = 29;
    private static final double CONNECTOR_WIDTH = 18;
    private static final double CONNECTOR_HEIGHT = 12;

    private final GModel model = GraphFactory.eINSTANCE.createGModel();
    private final GNode node = GraphFactory.eINSTANCE.createGNode();
    private final GConnector connector = GraphFactory.eINSTANCE.createGConnector();

    private SkinLookup skinLookup;

    @Before
    public void setUp() {

        // Assign some arbitrary position to the node.
        node.setX(NODE_X);
        node.setY(NODE_Y);

        model.getNodes().add(node);
        node.getConnectors().add(connector);

        skinLookup = MockSkinLookupFactory.createSkinLookup(model);

        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);

        // Assign some arbitrary position to the center of the connector relative to the node.
        final Point2D position = new Point2D(CONNECTOR_CENTER_X, CONNECTOR_CENTER_Y);
        Mockito.when(nodeSkin.getConnectorPosition(connectorSkin)).thenReturn(position);

        // Assign some arbitrary connector width and height.
        Mockito.when(connectorSkin.getWidth()).thenReturn(CONNECTOR_WIDTH);
        Mockito.when(connectorSkin.getHeight()).thenReturn(CONNECTOR_HEIGHT);
    }

    @Test
    public void testGetConnectorPosition() {
        // Should return the absolute position of the center of the connector.
        final Point2D target = new Point2D(NODE_X + CONNECTOR_CENTER_X, NODE_Y + CONNECTOR_CENTER_Y);
        assertEquals(GeometryUtils.getConnectorPosition(connector, skinLookup), target);
    }
}
