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

import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.demo.customskins.DefaultSkinManager;
import de.tesis.dynaware.grapheditor.demo.customskins.GreySkinManager;
import de.tesis.dynaware.grapheditor.demo.customskins.TreeSkinManager;
import de.tesis.dynaware.grapheditor.demo.customskins.grey.GreySkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectorValidator;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.window.WindowPosition;

/**
 * Controller for the {@link GraphEditorDemo} application.
 */
public class GraphEditorDemoController {

    private static final String STYLE_CLASS_GREY_SKINS = "grey-skins";

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
    private RadioMenuItem greySkinButton;
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

    private DefaultSkinManager defaultSkinManager;
    private TreeSkinManager treeSkinManager;
    private GreySkinManager greySkinManager;

    private SkinType activeSkinType = SkinType.DEFAULT;

    /**
     * Called by JavaFX when FXML is loaded.
     */
    public void initialize() {

        final GModel model = GraphFactory.eINSTANCE.createGModel();

        graphEditor.setModel(model);
        graphEditorContainer.setGraphEditor(graphEditor);

        initializeMenuBar();

        defaultSkinManager = new DefaultSkinManager(graphEditor, graphEditorContainer);
        treeSkinManager = new TreeSkinManager(graphEditor, graphEditorContainer);
        greySkinManager = new GreySkinManager(graphEditor, graphEditorContainer);
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
    public void loadGrey() {
        greySkinButton.setSelected(true);
        setGreySkin();
        graphEditorPersistence.loadGrey(graphEditor);
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

        if (activeSkinType.equals(SkinType.GREY)) {
            greySkinManager.handlePaste();
        } else {
            graphEditor.getSelectionManager().paste();
        }
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

        switch (activeSkinType) {

        case DEFAULT:
            defaultSkinManager.addNode(currentZoomFactor);
            break;

        case TREE:
            treeSkinManager.addNode(currentZoomFactor);
            break;

        case GREY:
            greySkinManager.addNode(currentZoomFactor);
            break;
        }
    }

    @FXML
    public void addInputConnector() {

        switch (activeSkinType) {

        case DEFAULT:
            defaultSkinManager.addInputConnector();
            break;

        case GREY:
            greySkinManager.addInputConnector();
            break;

        case TREE:
            break;
        }
    }

    @FXML
    public void addOutputConnector() {

        switch (activeSkinType) {

        case DEFAULT:
            defaultSkinManager.addOutputConnector();
            break;

        case GREY:
            greySkinManager.addOutputConnector();
            break;

        case TREE:
            break;
        }
    }

    @FXML
    public void setDefaultSkin() {

        if (!activeSkinType.equals(SkinType.DEFAULT)) {
            switchToSkinType(SkinType.DEFAULT);
        }
    }

    @FXML
    public void setTreeSkin() {

        if (!activeSkinType.equals(SkinType.TREE)) {
            switchToSkinType(SkinType.TREE);
        }
    }

    @FXML
    public void setGreySkin() {

        if (!activeSkinType.equals(SkinType.GREY)) {
            switchToSkinType(SkinType.GREY);
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
     * Initializes the menu bar.
     */
    private void initializeMenuBar() {

        scaleTransform = new Scale(currentZoomFactor, currentZoomFactor, 0, 0);
        scaleTransform.yProperty().bind(scaleTransform.xProperty());

        graphEditor.getView().getTransforms().add(scaleTransform);

        final ToggleGroup skinToggleGroup = new ToggleGroup();
        skinToggleGroup.getToggles().addAll(defaultSkinButton, treeSkinButton, greySkinButton);

        final ToggleGroup connectionStyleToggleGroup = new ToggleGroup();
        connectionStyleToggleGroup.getToggles().addAll(gappedStyleButton, detouredStyleButton);

        graphEditor.getProperties().gridVisibleProperty().bind(showGridButton.selectedProperty());
        graphEditor.getProperties().snapToGridProperty().bind(snapToGridButton.selectedProperty());

        minimapButton.setGraphic(AwesomeIcon.MAP.node());

        initializeZoomOptions();
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
     * Crudely inspects the model's first node and sets the skin type accordingly.
     */
    private void checkSkinType() {

        if (!graphEditor.getModel().getNodes().isEmpty()) {

            final GNode firstNode = graphEditor.getModel().getNodes().get(0);
            final String type = firstNode.getType();
            switchToSkinType(getSkinType(type));
        }
    }

    private SkinType getSkinType(final String type) {

        if (TreeSkinConstants.TREE_NODE.equals(type)) {
            return SkinType.TREE;
        } else if (GreySkinConstants.GREY_NODE.equals(type)) {
            return SkinType.GREY;
        } else {
            return SkinType.DEFAULT;
        }
    }

    private void switchToSkinType(final SkinType skinType) {

        switch (skinType) {

        case DEFAULT:

            graphEditor.setConnectorValidator(null);
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_GREY_SKINS);
            disableDefaultSkinSpecificOptions(false);
            defaultSkinButton.setSelected(true);

            break;

        case TREE:

            graphEditor.setConnectorValidator(TreeConnectorValidator.class);
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_GREY_SKINS);
            disableDefaultSkinSpecificOptions(true);
            treeSkinButton.setSelected(true);

            break;

        case GREY:

            graphEditor.setConnectorValidator(null);
            if (!graphEditor.getView().getStyleClass().contains(STYLE_CLASS_GREY_SKINS)) {
                graphEditor.getView().getStyleClass().add(STYLE_CLASS_GREY_SKINS);
            }
            disableDefaultSkinSpecificOptions(false);
            greySkinButton.setSelected(true);

            break;
        }

        activeSkinType = skinType;

        clearAll();
        flushCommandStack();
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

    private enum SkinType {
        DEFAULT, TREE, GREY;
    }
}
