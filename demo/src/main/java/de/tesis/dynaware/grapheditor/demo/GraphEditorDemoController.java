/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo;

import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.demo.titled.TitledConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.titled.TitledTailSkin;
import de.tesis.dynaware.grapheditor.demo.titled.TitledNodeSkin;
import de.tesis.dynaware.grapheditor.demo.titled.TitledSkinConstants;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeConnectionSkin;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeConnectorSkin;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeNodeSkin;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeTailSkin;
import de.tesis.dynaware.grapheditor.demo.tree.validators.TreeConnectorValidator;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import de.tesis.dynaware.grapheditor.window.WindowPosition;

/**
 * Controller for the {@link GraphEditorDemo} application.
 */
public class GraphEditorDemoController {

    private static final String STYLE_CLASS_TITLED = "titled";

    private static final String DEFAULT_INPUT_CONNECTOR_TYPE = "input";
    private static final String DEFAULT_OUTPUT_CONNECTOR_TYPE = "output";
    private static final int MAX_CONNECTOR_COUNT = 5;

    private static final int NODE_INITIAL_X = 19;
    private static final int NODE_INITIAL_Y = 19;

    @FXML
    private AnchorPane root;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem addInputButton;
    @FXML
    private MenuItem addOutputButton;
    @FXML
    private RadioMenuItem showGridButton;
    @FXML
    private RadioMenuItem snapToGridButton;
    @FXML
    private RadioMenuItem defaultSkinButton;
    @FXML
    private RadioMenuItem treeSkinButton;
    @FXML
    private RadioMenuItem titledSkinButton;
    @FXML
    private Menu intersectionStyle;
    @FXML
    private RadioMenuItem gappedStyleButton;
    @FXML
    private RadioMenuItem detouredStyleButton;
    @FXML
    private Menu zoomOptions;
    @FXML
    private ToggleButton minimapButton;
    @FXML
    private GraphEditorContainer graphEditorContainer;

    private final GraphEditor graphEditor = new DefaultGraphEditor();
    private final GraphEditorPersistence graphEditorPersistence = new GraphEditorPersistence();

    private Scale scaleTransform;

    private double currentZoomFactor = 1;

    private boolean isMinimapVisible;

    private SkinType activeSkinType = SkinType.DEFAULT;

    /**
     * Called by JavaFX when FXML is loaded.
     */
    public void initialize() {

        final GModel model = GraphFactory.eINSTANCE.createGModel();

        graphEditor.setModel(model);
        graphEditorContainer.setGraphEditor(graphEditor);

        scaleTransform = new Scale(currentZoomFactor, currentZoomFactor, 0, 0);
        scaleTransform.yProperty().bind(scaleTransform.xProperty());

        graphEditor.getView().getTransforms().add(scaleTransform);

        final ToggleGroup skinToggleGroup = new ToggleGroup();
        skinToggleGroup.getToggles().addAll(defaultSkinButton, treeSkinButton, titledSkinButton);

        final ToggleGroup connectionStyleToggleGroup = new ToggleGroup();
        connectionStyleToggleGroup.getToggles().addAll(gappedStyleButton, detouredStyleButton);

        graphEditor.getProperties().gridVisibleProperty().bind(showGridButton.selectedProperty());
        graphEditor.getProperties().snapToGridProperty().bind(snapToGridButton.selectedProperty());

        minimapButton.setGraphic(AwesomeIcon.MAP.node());

        initializeZoomOptions();
        setCustomSkins();
    }

    @FXML
    public void load() {
        graphEditorPersistence.loadFromFile(graphEditor);
        checkSkinType();
    }

    @FXML
    public void loadSample() {
        defaultSkinButton.setSelected(true);
        setDefaultSkin();
        graphEditorPersistence.loadSample(graphEditor);
    }

    @FXML
    public void loadTree() {
        treeSkinButton.setSelected(true);
        setTreeSkin();
        graphEditorPersistence.loadTree(graphEditor);
    }

    @FXML
    public void save() {
        graphEditorPersistence.saveToFile(graphEditor);
    }

    @FXML
    public void clearAll() {
        Commands.clear(graphEditor.getModel());
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    @FXML
    public void undo() {
        graphEditor.getSelectionManager().backup();
        Commands.undo(graphEditor.getModel());
        graphEditor.getSelectionManager().restore();
    }

    @FXML
    public void redo() {
        graphEditor.getSelectionManager().backup();
        Commands.redo(graphEditor.getModel());
        graphEditor.getSelectionManager().restore();
    }

    @FXML
    public void cut() {
        graphEditor.getSelectionManager().cut();
    }

    @FXML
    public void copy() {
        graphEditor.getSelectionManager().copy();
    }

    @FXML
    public void paste() {
        graphEditor.getSelectionManager().paste().size();
    }

    @FXML
    public void selectAll() {
        graphEditor.getSelectionManager().selectAll();
    }

    @FXML
    public void deleteSelection() {
        graphEditor.getSelectionManager().deleteSelection();
    }

    @FXML
    public void addNode() {

        final double windowXOffset = graphEditorContainer.windowXProperty().get() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.windowYProperty().get() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        switch (activeSkinType) {
        case DEFAULT:

            // Default node.
            final GConnector output = GraphFactory.eINSTANCE.createGConnector();
            node.getConnectors().add(output);

            final GConnector input = GraphFactory.eINSTANCE.createGConnector();
            node.getConnectors().add(input);

            node.setX(NODE_INITIAL_X + windowXOffset);

            input.setType(DEFAULT_INPUT_CONNECTOR_TYPE);
            output.setType(DEFAULT_OUTPUT_CONNECTOR_TYPE);

            break;

        case TREE:

            // Tree root-node.
            final GConnector treeOutput = GraphFactory.eINSTANCE.createGConnector();
            node.getConnectors().add(treeOutput);

            final double initialX = graphEditorContainer.getWidth() / (2 * currentZoomFactor) - node.getWidth() / 2;
            node.setX(Math.floor(initialX) + windowXOffset);

            node.setType(TreeSkinConstants.TREE_NODE);
            treeOutput.setType(TreeSkinConstants.TREE_OUTPUT);

            // This allows multiple connections to be created from the output.
            treeOutput.setConnectionDetachedOnDrag(false);

            break;

        case TITLED:

            node.setType(TitledSkinConstants.TITLED_NODE);
            node.setX(NODE_INITIAL_X + windowXOffset);

            final GConnector squareInput = GraphFactory.eINSTANCE.createGConnector();
            node.getConnectors().add(squareInput);
            squareInput.setType(TitledSkinConstants.SQUARE_INPUT);

            final GConnector squareOutput = GraphFactory.eINSTANCE.createGConnector();
            node.getConnectors().add(squareOutput);
            squareOutput.setType(TitledSkinConstants.SQUARE_OUTPUT);

            break;
        }

        Commands.addNode(graphEditor.getModel(), node);
    }

    @FXML
    public void addInputConnector() {
        addConnector(DEFAULT_INPUT_CONNECTOR_TYPE);
    }

    @FXML
    public void addOutputConnector() {
        addConnector(DEFAULT_OUTPUT_CONNECTOR_TYPE);
    }

    @FXML
    public void setDefaultSkin() {

        if (!activeSkinType.equals(SkinType.DEFAULT)) {

            activeSkinType = SkinType.DEFAULT;

            // The custom skins are not intended to be mixed with other skins. Therefore clear everything.
            clearAll();
            flushCommandStack();

            // Set connector validator to null so that default will be used.
            graphEditor.setConnectorValidator(null);
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED);

            disableDefaultSkinSpecificOptions(false);
        }
    }

    @FXML
    public void setTreeSkin() {

        if (!activeSkinType.equals(SkinType.TREE)) {

            activeSkinType = SkinType.TREE;

            // The custom skins are not intended to be mixed with other skins. Therefore clear everything.
            clearAll();
            flushCommandStack();

            graphEditor.setConnectorValidator(TreeConnectorValidator.class);

            disableDefaultSkinSpecificOptions(true);
        }
    }

    @FXML
    public void setTitledSkin() {

        if (!activeSkinType.equals(SkinType.TITLED)) {

            activeSkinType = SkinType.TITLED;

            // The custom skins are not intended to be mixed with other skins. Therefore clear everything.
            clearAll();
            flushCommandStack();

            graphEditor.setConnectorValidator(null);
            graphEditor.getView().getStyleClass().add(STYLE_CLASS_TITLED);

            disableDefaultSkinSpecificOptions(true);
        }
    }

    @FXML
    public void setGappedStyle() {
        graphEditor.getProperties().getCustomProperties().remove(DefaultConnectionSkin.SHOW_DETOURS_KEY);
        graphEditor.reload();
    }

    @FXML
    public void setDetouredStyle() {
        final Map<String, String> customProperties = graphEditor.getProperties().getCustomProperties();
        customProperties.put(DefaultConnectionSkin.SHOW_DETOURS_KEY, Boolean.toString(true));
        graphEditor.reload();
    }

    @FXML
    public void toggleMinimap() {
        isMinimapVisible = !isMinimapVisible;
        graphEditorContainer.setMinimapVisible(isMinimapVisible);
    }

    /**
     * Pans the graph editor container to place the window over the center of the content.
     *
     * <p>
     * Only works after the scene has been drawn, when getWidth() & getHeight() return non-zero values.
     * </p>
     */
    public void panToCenter() {
        graphEditorContainer.panTo(WindowPosition.CENTER);
    }

    /**
     * Initializes the list of zoom options.
     */
    private void initializeZoomOptions() {

        final ToggleGroup toggleGroup = new ToggleGroup();

        for (int i = 1; i <= 5; i++) {

            final RadioMenuItem zoomOption = new RadioMenuItem();
            final double zoomFactor = i;

            zoomOption.setText(i + "00%");
            zoomOption.setOnAction(event -> setZoomFactor(zoomFactor));

            toggleGroup.getToggles().add(zoomOption);
            zoomOptions.getItems().add(zoomOption);

            if (i == 1) {
                zoomOption.setSelected(true);
            }
        }
    }

    /**
     * Sets some custom skins for a graph with a tree-structure.
     */
    private void setCustomSkins() {

        graphEditor.setNodeSkin(TreeSkinConstants.TREE_NODE, TreeNodeSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_INPUT, TreeConnectorSkin.class);
        graphEditor.setConnectorSkin(TreeSkinConstants.TREE_OUTPUT, TreeConnectorSkin.class);
        graphEditor.setConnectionSkin(TreeSkinConstants.TREE_CONNECTION, TreeConnectionSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_INPUT, TreeTailSkin.class);
        graphEditor.setTailSkin(TreeSkinConstants.TREE_OUTPUT, TreeTailSkin.class);

        graphEditor.setNodeSkin(TitledSkinConstants.TITLED_NODE, TitledNodeSkin.class);
        graphEditor.setConnectorSkin(TitledSkinConstants.SQUARE_INPUT, TitledConnectorSkin.class);
        graphEditor.setConnectorSkin(TitledSkinConstants.SQUARE_OUTPUT, TitledConnectorSkin.class);
        graphEditor.setTailSkin(TitledSkinConstants.SQUARE_INPUT, TitledTailSkin.class);
        graphEditor.setTailSkin(TitledSkinConstants.SQUARE_OUTPUT, TitledTailSkin.class);
    }

    /**
     * Sets a new zoom factor.
     *
     * <p>
     * Note that everything will look crap if the zoom factor is non-integer.
     * </p>
     *
     * @param zoomFactor the new zoom factor
     */
    private void setZoomFactor(final double zoomFactor) {

        final double zoomFactorRatio = zoomFactor / currentZoomFactor;

        final double currentCenterX = graphEditorContainer.windowXProperty().get();
        final double currentCenterY = graphEditorContainer.windowYProperty().get();

        scaleTransform.setX(zoomFactor);
        graphEditorContainer.panTo(currentCenterX * zoomFactorRatio, currentCenterY * zoomFactorRatio);
        currentZoomFactor = zoomFactor;
    }

    /**
     * Adds a connector of the given type to all nodes that are currently selected.
     *
     * @param type the type for the new connector
     */
    private void addConnector(final String type) {

        if (!DEFAULT_INPUT_CONNECTOR_TYPE.equals(type) && !DEFAULT_OUTPUT_CONNECTOR_TYPE.equals(type)) {
            return;
        }

        final GModel model = graphEditor.getModel();
        final SkinLookup skinLookup = graphEditor.getSkinLookup();
        final CompoundCommand command = new CompoundCommand();
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        for (final GNode node : model.getNodes()) {

            if (skinLookup.lookupNode(node).isSelected()) {
                if (countConnectors(node, type) < MAX_CONNECTOR_COUNT) {

                    final GConnector connector = GraphFactory.eINSTANCE.createGConnector();
                    connector.setType(type);

                    final EReference connectors = GraphPackage.Literals.GCONNECTABLE__CONNECTORS;

                    command.append(AddCommand.create(editingDomain, node, connectors, connector));
                }
            }
        }

        if (command.canExecute()) {
            graphEditor.getSelectionManager().backup();
            editingDomain.getCommandStack().execute(command);
            graphEditor.getSelectionManager().restore();
        }
    }

    /**
     * Counts the number of connectors the given node currently has of the given type.
     *
     * @param node a {@link GNode} instance
     * @param type a type String
     * @return the number of connectors this node has of the given type
     */
    private int countConnectors(final GNode node, final String type) {

        int count = 0;

        for (final GConnector connector : node.getConnectors()) {
            if (connector.getType() == null && type == null || connector.getType() != null
                    && connector.getType().equals(type)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Disables / enables the menu options that are specific to the default skins.
     *
     * @param disable {@code true} to disable, {@code false} to enable
     */
    private void disableDefaultSkinSpecificOptions(final boolean disable) {

        // Tree skins are not intended to have more than one output or input.
        addInputButton.setDisable(disable);
        addOutputButton.setDisable(disable);

        // Connection style options only apply to default connection skin.
        intersectionStyle.setDisable(disable);
    }

    /**
     * Flushes the command stack, so that the undo/redo history is cleared.
     */
    private void flushCommandStack() {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(graphEditor.getModel());
        if (editingDomain != null) {
            editingDomain.getCommandStack().flush();
        }
    }

    /**
     * Crudely inspects the model's first node and sets the skin type accordingly.
     */
    private void checkSkinType() {

        if (!graphEditor.getModel().getNodes().isEmpty()) {

            final GNode firstNode = graphEditor.getModel().getNodes().get(0);
            final String type = firstNode.getType();

            if (TreeSkinConstants.TREE_NODE.equals(type)) {

                activeSkinType = SkinType.TREE;
                graphEditor.setConnectorValidator(TreeConnectorValidator.class);
                disableDefaultSkinSpecificOptions(true);
                treeSkinButton.setSelected(true);

            } else if (TitledSkinConstants.TITLED_NODE.equals(type)) {

                activeSkinType = SkinType.TITLED;
                graphEditor.setConnectorValidator(null);
                disableDefaultSkinSpecificOptions(true);
                titledSkinButton.setSelected(true);
                graphEditor.getView().getStyleClass().add(STYLE_CLASS_TITLED);

            } else {

                activeSkinType = SkinType.DEFAULT;
                graphEditor.setConnectorValidator(null);
                disableDefaultSkinSpecificOptions(false);
                defaultSkinButton.setSelected(true);
                graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED);
            }
        }
    }

    private enum SkinType {
        DEFAULT, TREE, TITLED;
    }
}
