package io.github.eckig.grapheditor.demo.customskins;

import java.util.List;

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GTailSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultNodeSkin;
import io.github.eckig.grapheditor.core.skins.defaults.DefaultTailSkin;
import io.github.eckig.grapheditor.core.view.GraphEditorContainer;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeConnectionSkin;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeConnectorSkin;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeNodeSkin;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeSkinConstants;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeTailSkin;
import io.github.eckig.grapheditor.demo.selections.SelectionCopier;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphFactory;
import javafx.geometry.Side;

/**
 * Responsible for tree-skin specific logic in the graph editor demo.
 */
public class TreeSkinController implements SkinController {

    protected static final int TREE_NODE_INITIAL_Y = 19;

    private final GraphEditor graphEditor;
    private final GraphEditorContainer graphEditorContainer;

    /**
     * Creates a new {@link TreeSkinController} instance.
     *
     * @param graphEditor the graph editor on display in this demo
     * @param graphEditorContainer the graph editor container on display in this demo
     */
    public TreeSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        this.graphEditor = graphEditor;
        this.graphEditorContainer = graphEditorContainer;
    }

    @Override
    public void activate()
    {
        graphEditor.setNodeSkinFactory(this::createSkin);
        graphEditor.setConnectorSkinFactory(this::createSkin);
        graphEditor.setConnectionSkinFactory(this::createSkin);
        graphEditor.setTailSkinFactory(this::createTailSkin);
        graphEditorContainer.getMinimap().setConnectionFilter(c -> false);
    }

    private GNodeSkin createSkin(final GNode node) {
        return TreeSkinConstants.TREE_NODE.equals(node.getType()) ? new TreeNodeSkin(node) : new DefaultNodeSkin(node);
    }

    private GConnectionSkin createSkin(final GConnection connection) {
        return TreeSkinConstants.TREE_CONNECTION.equals(connection.getType()) ? new TreeConnectionSkin(connection) : new DefaultConnectionSkin(connection);
    }

    private GConnectorSkin createSkin(final GConnector connector) {
        return TreeSkinConstants.TREE_INPUT_CONNECTOR.equals(connector.getType()) || TreeSkinConstants.TREE_OUTPUT_CONNECTOR.equals(connector.getType()) ?
                new TreeConnectorSkin(connector) : new DefaultConnectorSkin(connector);
    }

    private GTailSkin createTailSkin(final GConnector connector) {
        return TreeSkinConstants.TREE_INPUT_CONNECTOR.equals(connector.getType()) || TreeSkinConstants.TREE_OUTPUT_CONNECTOR.equals(connector.getType()) ?
                new TreeTailSkin(connector) : new DefaultTailSkin(connector);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.getContentX() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.getContentY() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(TREE_NODE_INITIAL_Y + windowYOffset);

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

    @Override
    public void addConnector(final Side position, final boolean input) {
        // Not implemented for tree nodes.
    }

    @Override
    public void clearConnectors() {
        // Not implemented for tree nodes.
    }

    @Override
    public void handlePaste(final SelectionCopier selectionCopier) {
        selectionCopier.paste((nodes, command) -> selectReferencedConnections(nodes));
    }

    @Override
    public void handleSelectAll() {
    	graphEditor.getSelectionManager().selectAll();
    }

    /**
     * Selects all connections that are referenced (i.e. connected to) the given nodes.
     *
     * @param nodes a list of graph nodes
     */
	private void selectReferencedConnections(final List<GNode> nodes) {

		nodes.stream()
			.flatMap(node -> node.getConnectors().stream())
			.flatMap(connector -> connector.getConnections().stream())
			.forEach(graphEditor.getSelectionManager()::select);
	}
}
