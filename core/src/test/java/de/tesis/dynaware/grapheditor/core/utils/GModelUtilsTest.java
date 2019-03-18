package de.tesis.dynaware.grapheditor.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.tesis.dynaware.grapheditor.core.connections.ConnectionCopier;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

public class GModelUtilsTest {

    @Test
    public void copyConnections() {

        final List<GNode> nodes = createNodes();
        final List<GNode> copies = createNodes();

        connect(nodes.get(0).getConnectors().get(1), nodes.get(1).getConnectors().get(0));

        final Map<GNode, GNode> map = new HashMap<>();

        for (int i = 0; i < 3; i++) {
            map.put(nodes.get(i), copies.get(i));
        }

        final List<GConnection> connections = ConnectionCopier.copyConnections(map);

        assertTrue(connections.size() == 1);
        assertTrue(copies.get(0).getConnectors().get(1).getConnections().size() == 1);

        final GConnection newConnection = copies.get(0).getConnectors().get(1).getConnections().get(0);

        assertTrue(copies.get(1).getConnectors().get(0).getConnections().size() == 1);
        assertEquals(copies.get(1).getConnectors().get(0).getConnections().get(0), newConnection);

        assertEquals(copies.get(0).getConnectors().get(1), newConnection.getSource());
        assertEquals(copies.get(1).getConnectors().get(0), newConnection.getTarget());

        // Check no other connections have appeared.
        assertTrue(copies.get(0).getConnectors().get(0).getConnections().isEmpty());
        assertTrue(copies.get(1).getConnectors().get(1).getConnections().isEmpty());
        assertTrue(copies.get(2).getConnectors().get(0).getConnections().isEmpty());
        assertTrue(copies.get(2).getConnectors().get(1).getConnections().isEmpty());
    }

    private static List<GNode> createNodes() {

        final List<GNode> nodes = new ArrayList<>();

        final GNode firstNode = createNode();
        final GNode secondNode = createNode();
        final GNode thirdNode = createNode();

        nodes.add(firstNode);
        nodes.add(secondNode);
        nodes.add(thirdNode);

        return nodes;
    }

    private static final GNode createNode() {

        final GNode node = GraphFactory.eINSTANCE.createGNode();

        final GConnector firstConnector = GraphFactory.eINSTANCE.createGConnector();
        final GConnector secondConnector = GraphFactory.eINSTANCE.createGConnector();

        node.getConnectors().add(firstConnector);
        node.getConnectors().add(secondConnector);

        return node;
    }

    private static final void connect(final GConnector source, final GConnector target) {

        final GConnection connection = GraphFactory.eINSTANCE.createGConnection();

        source.getConnections().add(connection);
        target.getConnections().add(connection);

        connection.setSource(source);
        connection.setTarget(target);
    }
}
