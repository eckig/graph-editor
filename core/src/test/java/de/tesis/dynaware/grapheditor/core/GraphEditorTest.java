/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.Before;
import org.junit.Test;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.connections.ConnectionCommands;
import de.tesis.dynaware.grapheditor.core.data.DummyDataFactory;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

/**
 * This test treats the graph editor as a single unit.
 *
 * <p>
 * A fully-functional {@link DefaultGraphEditor} instance is created and populated with a dummy {@link GModel}. We make
 * some changes to the model and check that the graph editor is reinitialized correctly.
 * </p>
 */
public class GraphEditorTest {

    private GraphEditor graphEditor;
    private GModel model;
    private SkinLookup skinLookup;

    private EditingDomain editingDomain;
    private CommandStack commandStack;

    @Before
    public void setUp() {

        graphEditor = new DefaultGraphEditor();
        model = DummyDataFactory.createModel();
        skinLookup = graphEditor.getSkinLookup();

        graphEditor.setModel(model);

        editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        if (editingDomain != null) {
            commandStack = editingDomain.getCommandStack();
        }
    }

    @Test
    public void checkInitializedCorrectly() {

        assertNotNull("Editing domain should exist.", editingDomain);
        assertNotNull("Command stack should exist.", commandStack);
        assertNotNull("Skin lookup should exist.", skinLookup);
    }

    @Test
    public void undoRedoNode() {

        final GNode node = addNodeToModel();

        assertNotNull("Node skin instance should exist.", skinLookup.lookupNode(node));
        assertTrue("Undo should be possible.", commandStack.canUndo());

        editingDomain.getCommandStack().undo();

        assertFalse("Node should have been removed.", model.getNodes().contains(node));
        assertNull("Node skin instance should no longer exist.", skinLookup.lookupNode(node));
        assertTrue("Redo should be possible.", commandStack.canRedo());

        editingDomain.getCommandStack().redo();

        assertTrue("Node should be back in again.", model.getNodes().contains(node));
        assertNotNull("Node skin instance should exist again.", skinLookup.lookupNode(node));
    }

    @Test
    public void undoRedoConnection() {

        Commands.clear(model);

        final GNode firstNode = addNodeToModel();
        final GNode secondNode = addNodeToModel();

        final GConnector firstNodeOutput = firstNode.getConnectors().get(1);
        final GConnector secondNodeInput = secondNode.getConnectors().get(0);

        final List<GJoint> joints = new ArrayList<>();
        joints.add(GraphFactory.eINSTANCE.createGJoint());
        joints.add(GraphFactory.eINSTANCE.createGJoint());

        ConnectionCommands.addConnection(model, firstNodeOutput, secondNodeInput, null, joints);

        assertFalse("A connection should be present.", model.getConnections().isEmpty());

        final GConnection connection = model.getConnections().get(0);

        assertNotNull("Connection skin instance should exist.", skinLookup.lookupConnection(connection));
        assertTrue("Undo should be possible.", commandStack.canUndo());

        editingDomain.getCommandStack().undo();

        assertFalse("Connection should have been removed.", model.getConnections().contains(connection));
        assertNull("Connection skin instance should no longer exist.", skinLookup.lookupConnection(connection));
        assertTrue("Redo should be possible.", commandStack.canRedo());

        editingDomain.getCommandStack().redo();

        assertTrue("Connection should be back in again.", model.getConnections().contains(connection));
        assertNotNull("Connection skin instance should exist again.", skinLookup.lookupConnection(connection));
    }

    @Test
    public void selectAllAndDelete() {

        graphEditor.getSelectionManager().selectAll();
        graphEditor.getSelectionManager().deleteSelection();

        assertTrue("All nodes should have gone.", model.getNodes().isEmpty());
        assertTrue("All connections should have gone.", model.getConnections().isEmpty());
    }

    @Test
    public void backupAndRestoreSelection() {

        final GNode firstNode = model.getNodes().get(0);
        final GJoint firstJoint = model.getConnections().get(0).getJoints().get(0);

        skinLookup.lookupNode(firstNode).setSelected(true);
        skinLookup.lookupJoint(firstJoint).setSelected(true);

        graphEditor.getSelectionManager().backup();

        // Reloading the model will cause selection information to be lost.
        graphEditor.reload();

        graphEditor.getSelectionManager().restore();

        assertTrue("First node should be selected again.", skinLookup.lookupNode(firstNode).isSelected());
        assertTrue("First joint should be selected again.", skinLookup.lookupJoint(firstJoint).isSelected());
    }

    @Test
    public void moveJointAndUpdateLayout() {

        final GJoint firstJoint = model.getConnections().get(0).getJoints().get(0);
        final GJoint secondJoint = model.getConnections().get(0).getJoints().get(1);

        final double firstJointInitialX = skinLookup.lookupJoint(firstJoint).getRoot().getLayoutX();
        final double secondJointInitialX = skinLookup.lookupJoint(secondJoint).getRoot().getLayoutX();

        skinLookup.lookupJoint(firstJoint).getRoot().setLayoutX(firstJointInitialX + 17);

        // This will call layoutChildren method of view and trigger connection redraw.
        graphEditor.getView().layout();

        final double secondJointFinalX = skinLookup.lookupJoint(secondJoint).getRoot().getLayoutX();

        assertTrue("Second joint should have moved right by 17 pixels.", secondJointFinalX == secondJointInitialX + 17);
    }

    /**
     * Adds a node to the model that has an input and output connector.
     *
     * @return the newly-added node
     */
    private GNode addNodeToModel() {
        final GNode node = DummyDataFactory.createNode();
        Commands.addNode(model, node);
        return node;
    }
}
