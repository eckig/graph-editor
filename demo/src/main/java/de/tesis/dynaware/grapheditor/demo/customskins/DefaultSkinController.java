package de.tesis.dynaware.grapheditor.demo.customskins;

import javafx.geometry.Side;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.DefaultConnectorTypes;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

/**
 * Responsible for default-skin specific logic in the graph editor demo.
 */
public class DefaultSkinController implements SkinController {

    protected static final int NODE_INITIAL_X = 19;
    protected static final int NODE_INITIAL_Y = 19;

    protected final GraphEditor graphEditor;
    protected final GraphEditorContainer graphEditorContainer;

    private static final int MAX_CONNECTOR_COUNT = 5;

    /**
     * Creates a new {@link DefaultSkinController} instance.
     * 
     * @param graphEditor the graph editor on display in this demo
     * @param graphEditorContainer the graph editor container on display in this demo
     */
    public DefaultSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {

        this.graphEditor = graphEditor;
        this.graphEditorContainer = graphEditorContainer;
    }

    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        final GConnector rightOutput = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(rightOutput);

        final GConnector leftInput = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(leftInput);

        node.setX(NODE_INITIAL_X + windowXOffset);

        rightOutput.setType(DefaultConnectorTypes.RIGHT_OUTPUT);
        leftInput.setType(DefaultConnectorTypes.LEFT_INPUT);

        Commands.addNode(graphEditor.getModel(), node);
    }

    /**
     * Adds a connector of the given type to all nodes that are currently selected.
     *
     * @param position the position of the new connector
     * @param input {@code true} for input, {@code false} for output
     */
    @Override
    public void addConnector(final Side position, final boolean input) {

        final String type = getType(position, input);

        final GModel model = graphEditor.getModel();
        final SkinLookup skinLookup = graphEditor.getSkinLookup();
        final CompoundCommand command = new CompoundCommand();
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        for (final GNode node : model.getNodes()) {

            if (skinLookup.lookupNode(node).isSelected()) {
                if (countConnectors(node, position) < MAX_CONNECTOR_COUNT) {

                    final GConnector connector = GraphFactory.eINSTANCE.createGConnector();
                    connector.setType(type);

                    final EReference connectors = GraphPackage.Literals.GCONNECTABLE__CONNECTORS;
                    command.append(AddCommand.create(editingDomain, node, connectors, connector));
                }
            }
        }

        if (command.canExecute()) {
            editingDomain.getCommandStack().execute(command);
        }
    }

    @Override
    public void clearConnectors() {
        Commands.clearConnectors(graphEditor.getModel(), graphEditor.getSelectionManager().getSelectedNodes());
    }

    @Override
    public void handlePaste() {
        graphEditor.getSelectionManager().paste();
    }

    /**
     * Counts the number of connectors the given node currently has of the given type.
     *
     * @param node a {@link GNode} instance
     * @param side the {@link Side} the connector is on
     * @return the number of connectors this node has on the given side
     */
    private int countConnectors(final GNode node, final Side side) {

        int count = 0;

        for (final GConnector connector : node.getConnectors()) {
            if (side.equals(DefaultConnectorTypes.getSide(connector.getType()))) {
                count++;
            }
        }

        return count;
    }

    /**
     * Gets the connector type string corresponding to the given position and input values.
     * 
     * @param position a {@link Side} value
     * @param input {@code true} for input, {@code false} for output
     * @return the connector type corresponding to these values
     */
    private String getType(final Side position, final boolean input) {

        switch (position) {
        case TOP:
            if (input) {
                return DefaultConnectorTypes.TOP_INPUT;
            } else {
                return DefaultConnectorTypes.TOP_OUTPUT;
            }
        case RIGHT:
            if (input) {
                return DefaultConnectorTypes.RIGHT_INPUT;
            } else {
                return DefaultConnectorTypes.RIGHT_OUTPUT;
            }
        case BOTTOM:
            if (input) {
                return DefaultConnectorTypes.BOTTOM_INPUT;
            } else {
                return DefaultConnectorTypes.BOTTOM_OUTPUT;
            }
        case LEFT:
            if (input) {
                return DefaultConnectorTypes.LEFT_INPUT;
            } else {
                return DefaultConnectorTypes.LEFT_OUTPUT;
            }
        }

        return null;
    }
}
