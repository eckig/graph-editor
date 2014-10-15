package de.tesis.dynaware.grapheditor.demo.customskins;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectionSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeNodeSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;

public class TreeSkinManager extends DefaultSkinManager {

    public TreeSkinManager(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        super(graphEditor, graphEditorContainer);

        graphEditor.setNodeSkin(TreeSkinConstants.TREE_NODE, TreeNodeSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_INPUT_CONNECTOR, TreeConnectorSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_OUTPUT_CONNECTOR, TreeConnectorSkin.class);
        graphEditor.setConnectionSkin(TreeSkinConstants.TREE_CONNECTION, TreeConnectionSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_INPUT_CONNECTOR, TreeTailSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_OUTPUT_CONNECTOR, TreeTailSkin.class);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);

        final double initialX = graphEditorContainer.getWidth() / (2 * currentZoomFactor) - node.getWidth() / 2;
        node.setX(Math.floor(initialX) + windowXOffset);

        node.setType(TreeSkinConstants.TREE_NODE);
        output.setType(TreeSkinConstants.TREE_OUTPUT_CONNECTOR);

        // This allows multiple connections to be created from the output.
        output.setConnectionDetachedOnDrag(false);

        Commands.addNode(graphEditor.getModel(), node);
    }
}
