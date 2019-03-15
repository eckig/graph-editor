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
import java.util.concurrent.CountDownLatch;

import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.Before;
import org.junit.Test;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.data.DummyDataFactory;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.ConnectionCommands;
import de.tesis.dynaware.grapheditor.core.utils.FXTestUtils;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import javafx.application.Platform;

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
    public void setUp() throws InterruptedException
    {
        graphEditor = new DefaultGraphEditor();
        model = DummyDataFactory.createModel();
        skinLookup = graphEditor.getSkinLookup();

        graphEditor.setModel(model);

        editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        if (editingDomain != null) {
            commandStack = editingDomain.getCommandStack();
        }

        final CountDownLatch waitInit = new CountDownLatch(1);
        try
        {
            Platform.startup(waitInit::countDown);
        }
        catch (Exception e)
        {
            waitInit.countDown();
        }
        waitInit.await();

        reloadEditor();

        graphEditor.getView().autosize();
        graphEditor.getView().layout();
    }

    private void reloadEditor() throws InterruptedException
    {
        final CountDownLatch wait = new CountDownLatch(1);
        Platform.runLater(() ->
        {
            graphEditor.reload();
            wait.countDown();
        });
        wait.await();
    }

    @Test
    public void checkInitializedCorrectly() {

        assertNotNull("Editing domain should exist.", editingDomain);
        assertNotNull("Command stack should exist.", commandStack);
        assertNotNull("Skin lookup should exist.", skinLookup);
    }

    @Test
    public void undoRedoNode() throws InterruptedException
    {

        final GNode node = addNodeToModel();
        reloadEditor();

        assertNotNull("Node skin instance should exist.", skinLookup.lookupNode(node));
        assertTrue("Undo should be possible.", commandStack.canUndo());

        editingDomain.getCommandStack().undo();
        reloadEditor();

        assertFalse("Node should have been removed.", model.getNodes().contains(node));
        assertNull("Node skin instance should no longer exist.", skinLookup.lookupNode(node));
        assertTrue("Redo should be possible.", commandStack.canRedo());

        editingDomain.getCommandStack().redo();
        reloadEditor();

        assertTrue("Node should be back in again.", model.getNodes().contains(node));
        assertNotNull("Node skin instance should exist again.", skinLookup.lookupNode(node));
    }

    @Test
    public void undoRedoConnection() throws InterruptedException
    {

        Commands.clear(model);

        final GNode firstNode = addNodeToModel();
        final GNode secondNode = addNodeToModel();

        final GConnector firstNodeOutput = firstNode.getConnectors().get(1);
        final GConnector secondNodeInput = secondNode.getConnectors().get(0);

        final List<GJoint> joints = new ArrayList<>();
        joints.add(GraphFactory.eINSTANCE.createGJoint());
        joints.add(GraphFactory.eINSTANCE.createGJoint());

        ConnectionCommands.addConnection(model, firstNodeOutput, secondNodeInput, null, joints, null);
        reloadEditor();

        assertFalse("A connection should be present.", model.getConnections().isEmpty());

        final GConnection connection = model.getConnections().get(0);

        assertNotNull("Connection skin instance should exist.", skinLookup.lookupConnection(connection));
        assertTrue("Undo should be possible.", commandStack.canUndo());

        editingDomain.getCommandStack().undo();
        reloadEditor();

        assertFalse("Connection should have been removed.", model.getConnections().contains(connection));
        assertNull("Connection skin instance should no longer exist.", skinLookup.lookupConnection(connection));
        assertTrue("Redo should be possible.", commandStack.canRedo());

        editingDomain.getCommandStack().redo();
        reloadEditor();

        assertTrue("Connection should be back in again.", model.getConnections().contains(connection));
        assertNotNull("Connection skin instance should exist again.", skinLookup.lookupConnection(connection));
    }

    @Test
    public void selectAllAndDelete() {

        graphEditor.getSelectionManager().selectAll();
        final List<EObject> selection = new ArrayList<>(graphEditor.getSelectionManager().getSelectedItems());
        graphEditor.delete(selection);

        assertTrue("All nodes should have gone.", model.getNodes().isEmpty());
        assertTrue("All connections should have gone.", model.getConnections().isEmpty());
    }

    @Test
    public void moveJointAndUpdateLayout() {

        final GJoint firstJoint = model.getConnections().get(0).getJoints().get(0);
        final GJoint secondJoint = model.getConnections().get(0).getJoints().get(1);

        final double secondJointInitialX = skinLookup.lookupJoint(secondJoint).getRoot().getLayoutX();

        FXTestUtils.dragBy(skinLookup.lookupJoint(firstJoint).getRoot(), 17, 0);

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
