package de.tesis.dynaware.grapheditor.demo.customskins;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.demo.customskins.titled.TitledConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.titled.TitledNodeSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.titled.TitledSkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.titled.TitledTailSkin;
import de.tesis.dynaware.grapheditor.demo.selections.SelectionCopier;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

/**
 * Responsible for grey-skin specific logic in the graph editor demo.
 */
public class TitledSkinController extends DefaultSkinController {

    /**
     * Creates a new {@link TitledSkinController} instance.
     *
     * @param graphEditor the graph editor on display in this demo
     * @param graphEditorContainer the graph editor container on display in this demo
     */
    public TitledSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {
        super(graphEditor, graphEditorContainer);
    }

    @Override
    public void activate() {
        super.activate();
        graphEditor.setNodeSkinFactory(this::createSkin);
        graphEditor.setConnectorSkinFactory(this::createSkin);
        graphEditor.setTailSkinFactory(this::createTailSkin);
    }

    private GNodeSkin createSkin(final GNode node) {
        return TitledSkinConstants.TITLED_NODE.equals(node.getType()) ? new TitledNodeSkin(node) : new DefaultNodeSkin(node);
    }

    private GConnectorSkin createSkin(final GConnector connector) {
        return TitledSkinConstants.TITLED_INPUT_CONNECTOR.equals(connector.getType()) || TitledSkinConstants.TITLED_OUTPUT_CONNECTOR.equals(connector.getType()) ?
                new TitledConnectorSkin(connector) : new DefaultConnectorSkin(connector);
    }

    private GTailSkin createTailSkin(final GConnector connector) {
        return TitledSkinConstants.TITLED_INPUT_CONNECTOR.equals(connector.getType()) || TitledSkinConstants.TITLED_INPUT_CONNECTOR.equals(connector.getType()) ?
                new TitledTailSkin(connector) : new DefaultTailSkin(connector);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.getContentX() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.getContentY() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        node.setType(TitledSkinConstants.TITLED_NODE);
        node.setX(NODE_INITIAL_X + windowXOffset);
        node.setId(allocateNewId());

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(input);
        input.setType(TitledSkinConstants.TITLED_INPUT_CONNECTOR);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);
        output.setType(TitledSkinConstants.TITLED_OUTPUT_CONNECTOR);

        Commands.addNode(graphEditor.getModel(), node);
    }

    @Override
    public void handlePaste(final SelectionCopier selectionCopier) {
        selectionCopier.paste((nodes, command) -> allocateIds(nodes, command));
    }

    /**
     * Allocates ID's to recently pasted nodes.
     *
     * @param nodes the recently pasted nodes
     * @param command the command responsible for adding the nodes
     */
    private void allocateIds(final List<GNode> nodes, final CompoundCommand command) {

        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(graphEditor.getModel());
        final EAttribute feature = GraphPackage.Literals.GNODE__ID;

        for (final GNode node : nodes) {

            if (checkNeedsNewId(node, nodes)) {

                final String id = allocateNewId();
                final Command setCommand = SetCommand.create(domain, node, feature, id);

                if (setCommand.canExecute()) {
                    command.appendAndExecute(setCommand);
                }

                graphEditor.getSkinLookup().lookupNode(node).initialize();
            }
        }
    }

    /**
     * Check the given node needs a new ID, i.e. that it's not already in use.
     *
     * @param node the nodes to check
     * @param pastedNodes the recently-pasted nodes
     */
    private boolean checkNeedsNewId(final GNode node, final List<GNode> pastedNodes) {

        final List<GNode> nodes = new ArrayList<>(graphEditor.getModel().getNodes());
        nodes.removeAll(pastedNodes);

        return nodes.stream().anyMatch(other -> other.getId().equals(node.getId()));
    }

    /**
     * Allocates a new ID corresponding to the largest existing ID + 1.
     *
     * @return the new ID
     */
    private String allocateNewId() {

        final List<GNode> nodes = graphEditor.getModel().getNodes();
        final OptionalInt max = nodes.stream().mapToInt(node -> Integer.parseInt(node.getId())).max();

        if (max.isPresent()) {
            return Integer.toString(max.getAsInt() + 1);
        }
        // ELSE:
        return "1"; //$NON-NLS-1$
    }
}
