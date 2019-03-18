package de.tesis.dynaware.grapheditor.demo.customskins;

import java.util.List;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectionSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeNodeSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeTailSkin;
import de.tesis.dynaware.grapheditor.demo.selections.SelectionCopier;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
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
