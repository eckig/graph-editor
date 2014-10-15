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
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.demo.customskins.grey.GreyConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.grey.GreyNodeSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.grey.GreySkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.grey.GreyTailSkin;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

public class GreySkinManager extends DefaultSkinManager {

    public GreySkinManager(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        super(graphEditor, graphEditorContainer);

        graphEditor.setNodeSkin(GreySkinConstants.GREY_NODE, GreyNodeSkin.class);
        graphEditor.setConnectorSkin(GreySkinConstants.GREY_INPUT_CONNECTOR, GreyConnectorSkin.class);
        graphEditor.setConnectorSkin(GreySkinConstants.GREY_OUTPUT_CONNECTOR, GreyConnectorSkin.class);
        graphEditor.setTailSkin(GreySkinConstants.GREY_INPUT_CONNECTOR, GreyTailSkin.class);
        graphEditor.setTailSkin(GreySkinConstants.GREY_OUTPUT_CONNECTOR, GreyTailSkin.class);
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        node.setType(GreySkinConstants.GREY_NODE);
        node.setX(NODE_INITIAL_X + windowXOffset);
        node.setId(allocateNewId());

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(input);
        input.setType(GreySkinConstants.GREY_INPUT_CONNECTOR);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);
        output.setType(GreySkinConstants.GREY_OUTPUT_CONNECTOR);

        Commands.addNode(graphEditor.getModel(), node);
    }

    @Override
    public void addInputConnector() {
        addConnector(GreySkinConstants.GREY_INPUT_CONNECTOR);
    }

    @Override
    public void addOutputConnector() {
        addConnector(GreySkinConstants.GREY_OUTPUT_CONNECTOR);
    }

    public void handlePaste() {
        graphEditor.getSelectionManager().paste((nodes, command) -> allocateIds(nodes, command));
    }

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

    private boolean checkNeedsNewId(final GNode node, final List<GNode> pastedNodes) {

        final List<GNode> nodes = new ArrayList<>(graphEditor.getModel().getNodes());
        nodes.removeAll(pastedNodes);

        return nodes.stream().anyMatch(other -> other.getId().equals(node.getId()));
    }

    private String allocateNewId() {

        final List<GNode> nodes = graphEditor.getModel().getNodes();
        final OptionalInt max = nodes.stream().mapToInt(node -> Integer.parseInt(node.getId())).max();

        if (max.isPresent()) {
            return Integer.toString(max.getAsInt() + 1);
        } else {
            return "1";
        }
    }
}
