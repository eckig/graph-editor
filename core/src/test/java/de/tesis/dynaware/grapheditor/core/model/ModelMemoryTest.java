/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.tesis.dynaware.grapheditor.core.data.DummyDataFactory;
import de.tesis.dynaware.grapheditor.core.model.ModelMemory;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

public class ModelMemoryTest {

    private final ModelMemory modelMemory = new ModelMemory();
    private GModel model;

    @Before
    public void setUp() {
        model = DummyDataFactory.createModel();
        modelMemory.setNewModelState(model);
    }

    @Test
    public void testUnchangedModel() {

        // Compare the initial model state with itself.
        modelMemory.setNewModelState(model);

        // There should be nothing to add / remove / update.
        assertTrue(modelMemory.getNodesToAdd().isEmpty());
        assertTrue(modelMemory.getNodesToRemove().isEmpty());
        assertTrue(modelMemory.getNodesToUpdate().isEmpty());
        assertTrue(modelMemory.getConnectorsToRemove().isEmpty());
        assertTrue(modelMemory.getConnectionsToAdd().isEmpty());
        assertTrue(modelMemory.getConnectionsToRemove().isEmpty());

        for (final GConnection connection : model.getConnections()) {
            assertTrue(modelMemory.getJointsToAdd().get(connection).isEmpty());
            assertTrue(modelMemory.getJointsToRemove().get(connection).isEmpty());
        }
    }

    @Test
    public void testAddNode() {

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        model.getNodes().add(node);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getNodesToAdd().size() == 1);
        assertTrue(modelMemory.getNodesToAdd().contains(node));
        assertTrue(modelMemory.getNodesToRemove().isEmpty());
        assertTrue(modelMemory.getNodesToUpdate().isEmpty());
        assertTrue(modelMemory.getConnectorsToRemove().isEmpty());
    }

    @Test
    public void testRemoveNode() {

        final GNode node = model.getNodes().get(0);
        model.getNodes().remove(node);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getNodesToAdd().isEmpty());
        assertTrue(modelMemory.getNodesToRemove().size() == 1);
        assertTrue(modelMemory.getNodesToRemove().contains(node));
        assertTrue(modelMemory.getNodesToUpdate().isEmpty());
        assertTrue(modelMemory.getConnectorsToRemove().isEmpty());
    }

    @Test
    public void testAddConnector() {

        final GNode node = model.getNodes().get(0);
        node.getConnectors().add(GraphFactory.eINSTANCE.createGConnector());

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getNodesToAdd().isEmpty());
        assertTrue(modelMemory.getNodesToRemove().isEmpty());
        assertTrue(modelMemory.getNodesToUpdate().size() == 1);
        assertTrue(modelMemory.getNodesToUpdate().contains(node));
        assertTrue(modelMemory.getConnectorsToRemove().isEmpty());
    }

    @Test
    public void testRemoveConnector() {

        final GNode node = model.getNodes().get(0);
        final GConnector connector = node.getConnectors().get(0);
        node.getConnectors().remove(connector);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getNodesToAdd().isEmpty());
        assertTrue(modelMemory.getNodesToRemove().isEmpty());
        assertTrue(modelMemory.getNodesToUpdate().size() == 1);
        assertTrue(modelMemory.getNodesToUpdate().contains(node));
        assertTrue(modelMemory.getConnectorsToRemove().size() == 1);
        assertTrue(modelMemory.getConnectorsToRemove().contains(connector));
    }

    @Test
    public void testAddConnection() {

        final GConnection connection = GraphFactory.eINSTANCE.createGConnection();
        model.getConnections().add(connection);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getConnectionsToAdd().size() == 1);
        assertTrue(modelMemory.getConnectionsToAdd().contains(connection));
        assertTrue(modelMemory.getConnectionsToRemove().isEmpty());
    }

    @Test
    public void testRemoveConnection() {

        final GConnection connection = model.getConnections().get(0);
        model.getConnections().remove(connection);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getConnectionsToAdd().isEmpty());
        assertTrue(modelMemory.getConnectionsToRemove().size() == 1);
        assertTrue(modelMemory.getConnectionsToRemove().contains(connection));
    }

    @Test
    public void testAddJoint() {

        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
        model.getConnections().get(0).getJoints().add(joint);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getConnectionsToAdd().isEmpty());
        assertTrue(modelMemory.getConnectionsToRemove().isEmpty());

        for (int i = 0; i < model.getConnections().size(); i++) {

            final GConnection connection = model.getConnections().get(i);

            if (i == 0) {
                assertTrue(modelMemory.getJointsToAdd().get(connection).size() == 1);
                assertTrue(modelMemory.getJointsToAdd().get(connection).contains(joint));
            } else {
                assertTrue(modelMemory.getJointsToAdd().get(connection).isEmpty());
            }

            assertTrue(modelMemory.getJointsToRemove().get(connection).isEmpty());
        }
    }

    @Test
    public void testRemoveJoint() {

        final GJoint joint = model.getConnections().get(0).getJoints().get(0);
        model.getConnections().get(0).getJoints().remove(joint);

        modelMemory.setNewModelState(model);

        assertTrue(modelMemory.getConnectionsToAdd().isEmpty());
        assertTrue(modelMemory.getConnectionsToRemove().isEmpty());

        for (int i = 0; i < model.getConnections().size(); i++) {

            final GConnection connection = model.getConnections().get(i);

            if (i == 0) {
                assertTrue(modelMemory.getJointsToRemove().get(connection).size() == 1);
                assertTrue(modelMemory.getJointsToRemove().get(connection).contains(joint));
            } else {
                assertTrue(modelMemory.getJointsToRemove().get(connection).isEmpty());
            }

            assertTrue(modelMemory.getJointsToAdd().get(connection).isEmpty());
        }
    }
}
