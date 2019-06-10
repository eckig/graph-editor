/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo;

import java.util.*;

import de.tesis.dynaware.grapheditor.*;
import de.tesis.dynaware.grapheditor.core.connectors.DefaultConnectorTypes;
import de.tesis.dynaware.grapheditor.model.*;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.eclipse.elk.alg.disco.DisCoLayoutProvider;
import org.eclipse.elk.alg.layered.LayeredLayoutProvider;
import org.eclipse.elk.alg.mrtree.TreeLayoutProvider;
import org.eclipse.elk.alg.radial.RadialLayoutProvider;
import org.eclipse.elk.alg.spore.OverlapRemovalLayoutProvider;
import org.eclipse.elk.alg.spore.ShrinkTreeLayoutProvider;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.IGraphLayoutEngine;
import org.eclipse.elk.core.data.*;
import org.eclipse.elk.core.math.ElkMargin;
import org.eclipse.elk.core.math.ElkPadding;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.math.KVectorChain;
import org.eclipse.elk.core.options.*;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.core.util.BoxLayoutProvider;
import org.eclipse.elk.core.util.IElkProgressMonitor;
import org.eclipse.elk.graph.*;
import org.eclipse.elk.graph.properties.IPropertyHolder;
import org.eclipse.elk.graph.util.ElkReflect;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.SimpleConnectionSkin;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorContainer;
import de.tesis.dynaware.grapheditor.demo.customskins.DefaultSkinController;
import de.tesis.dynaware.grapheditor.demo.customskins.SkinController;
import de.tesis.dynaware.grapheditor.demo.customskins.TitledSkinController;
import de.tesis.dynaware.grapheditor.demo.customskins.TreeSkinController;
import de.tesis.dynaware.grapheditor.demo.customskins.titled.TitledSkinConstants;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeConnectorValidator;
import de.tesis.dynaware.grapheditor.demo.customskins.tree.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.demo.selections.SelectionCopier;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.layout.AnchorPane;

/**
 * Controller for the {@link GraphEditorDemo} application.
 */
public class GraphEditorDemoController {

    private static final String STYLE_CLASS_TITLED_SKINS = "titled-skins"; //$NON-NLS-1$

    @FXML
    private AnchorPane root;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem addConnectorButton;
    @FXML
    private MenuItem clearConnectorsButton;
    @FXML
    private Menu connectorTypeMenu;
    @FXML
    private Menu connectorPositionMenu;
    @FXML
    private RadioMenuItem inputConnectorTypeButton;
    @FXML
    private RadioMenuItem outputConnectorTypeButton;
    @FXML
    private RadioMenuItem leftConnectorPositionButton;
    @FXML
    private RadioMenuItem rightConnectorPositionButton;
    @FXML
    private RadioMenuItem topConnectorPositionButton;
    @FXML
    private RadioMenuItem bottomConnectorPositionButton;
    @FXML
    private RadioMenuItem showGridButton;
    @FXML
    private RadioMenuItem snapToGridButton;
    @FXML
    private RadioMenuItem readOnlyButton;
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
    private ToggleButton minimapButton;
    @FXML
    private GraphEditorContainer graphEditorContainer;
    @FXML
    private ToolBar bottomToolbar;

    private final GraphEditor graphEditor = new DefaultGraphEditor();
	private final SelectionCopier selectionCopier = new SelectionCopier(graphEditor.getSkinLookup(),
			graphEditor.getSelectionManager());
    private final GraphEditorPersistence graphEditorPersistence = new GraphEditorPersistence();

    private DefaultSkinController defaultSkinController;
    private TreeSkinController treeSkinController;
    private TitledSkinController titledSkinController;

    private final ObjectProperty<SkinController> activeSkinController = new SimpleObjectProperty<>()
    {

        @Override
        protected void invalidated() {
            super.invalidated();
            if(get() != null) {
                get().activate();
            }
        }

    };

    /**
     * Called by JavaFX when FXML is loaded.
     */
    public void initialize() {

        final GModel model = GraphFactory.eINSTANCE.createGModel();

        graphEditor.setModel(model);
        graphEditorContainer.setGraphEditor(graphEditor);

        setDetouredStyle();

        defaultSkinController = new DefaultSkinController(graphEditor, graphEditorContainer);
        treeSkinController = new TreeSkinController(graphEditor, graphEditorContainer);
        titledSkinController = new TitledSkinController(graphEditor, graphEditorContainer);

        activeSkinController.set(defaultSkinController);

        graphEditor.modelProperty().addListener((w, o, n) -> selectionCopier.initialize(n));
        selectionCopier.initialize(model);

        initializeMenuBar();
        addActiveSkinControllerListener();

        ElkReflect.register(LinkedList.class, LinkedList::new, p -> ((LinkedList) p).clone());
        ElkReflect.register(ElkPadding.class, ElkPadding::new, p -> ((ElkPadding) p).clone());
        ElkReflect.register(ElkMargin.class, ElkMargin::new, p -> ((ElkMargin) p).clone());
        ElkReflect.register(KVector.class, KVector::new, p -> ((KVector) p).clone());
        ElkReflect.register(KVectorChain.class, KVectorChain::new, p -> ((KVectorChain) p).clone());

        final CoreOptions options = new CoreOptions();
        final LayoutMetaDataProviderRegistry registry = new LayoutMetaDataProviderRegistry();
        options.apply(registry);
        final ComboBox<IGraphLayoutEngine> layoutEngines = new ComboBox<>(FXCollections.observableArrayList(
                new LayeredLayoutProvider(), new BoxLayoutProvider(), new TreeLayoutProvider(),
                new DisCoLayoutProvider(), new RadialLayoutProvider(),
                new OverlapRemovalLayoutProvider(), new ShrinkTreeLayoutProvider()));
        layoutEngines.valueProperty().addListener((w, o, n) -> autoLayout(registry, n));
        bottomToolbar.getItems().add(layoutEngines);
        registry.addTo(obs -> autoLayout(registry, layoutEngines.getValue()), bottomToolbar.getItems());
    }

    private void autoLayout(final LayoutMetaDataProviderRegistry pRegistry, final IGraphLayoutEngine pEngine)
    {
        if(pEngine==null)
        {
            return;
        }

        final IElkProgressMonitor progress = new BasicProgressMonitor();
        final Mapping mapping = new Mapping();
        final ElkNode elkNodeRepresentation = createElkNodeRepresentation(pRegistry, mapping);
        pEngine.layout(elkNodeRepresentation, progress);

        mapping.apply(graphEditor.getModel());
        graphEditor.reload();
    }

    private void setCommonProperties(final LayoutMetaDataProviderRegistry pRegistry, final LayoutOptionData.Target pTarget, final IPropertyHolder pHolder)
    {
        pHolder.setProperty(CoreOptions.PORT_CONSTRAINTS, PortConstraints.FIXED_POS);
        pHolder.setProperty(CoreOptions.ANIMATE, false);
        pHolder.setProperty(CoreOptions.NODE_SIZE_CONSTRAINTS, SizeConstraint.fixed());
        pHolder.setProperty(CoreOptions.EDGE_ROUTING, EdgeRouting.ORTHOGONAL);
        pRegistry.applyTo(pTarget, pHolder);
    }

    private ElkNode createElkNodeRepresentation(final LayoutMetaDataProviderRegistry pRegistry, final Mapping pMapping)
    {
        final ElkNode model = ElkGraphFactory.eINSTANCE.createElkNode();
        GModel gModel = graphEditor.getModel();
        if (gModel != null)
        {
            if(!gModel.getNodes().isEmpty())
            {
                model.setWidth(gModel.getContentWidth());
                model.setHeight(gModel.getContentHeight());
                for (final GNode node : gModel.getNodes())
                {
                    model.getChildren().add(nodeToElk(pRegistry, node, pMapping));
                }
            }
            if(!gModel.getConnections().isEmpty())
            {
                for(final GConnection connection : gModel.getConnections())
                {
                    final GConnectionSkin connectionSkin = graphEditor.getSkinLookup().lookupConnection(connection);
                    if(connectionSkin instanceof VirtualSkin)
                    {
                        continue;
                    }
                    final ElkEdge edge = ElkGraphFactory.eINSTANCE.createElkEdge();
                    setCommonProperties(pRegistry, LayoutOptionData.Target.EDGES, edge);

                    final ElkPort portSource = pMapping.connectors.get(connection.getSource());
                    final ElkPort portTarget = pMapping.connectors.get(connection.getTarget());
                    if (portSource != null && portTarget != null)
                    {
                        edge.getTargets().add(portTarget);
                        edge.getSources().add(portSource);
                        pMapping.connections.put(connection, edge);
                    }
                }
            }
        }

        setCommonProperties(pRegistry, LayoutOptionData.Target.PARENTS, model);

        return model;
    }

    private static class Mapping
    {
        // GConnector  -> ElkPort
        // GConnection -> ElkEdge
        // GNode       -> ElkNode
        // GJoint      -> ElkEdgeSection
        final Map<GConnection, ElkEdge> connections = new IdentityHashMap<>();
        final Map<GConnector, ElkPort> connectors = new IdentityHashMap<>();
        final Map<GNode, ElkNode> nodes = new IdentityHashMap<>();

        public void apply(final GModel pModel)
        {
            final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pModel);
            final CompoundCommand command = new CompoundCommand();

            for(final Map.Entry<GNode, ElkNode> entry : nodes.entrySet())
            {
                final GNode node = entry.getKey();
                final ElkNode eNode = entry.getValue();
                command.append(SetCommand.create(editingDomain, node, GraphPackage.Literals.GNODE__X, eNode.getX()));
                command.append(SetCommand.create(editingDomain, node, GraphPackage.Literals.GNODE__Y, eNode.getY()));
            }

            for(final Map.Entry<GConnection, ElkEdge> entry : connections.entrySet())
            {
                final GConnection connection = entry.getKey();
                final List<GJoint> newJoints = new ArrayList<>();
                for(final ElkEdgeSection section : entry.getValue().getSections())
                {
                    for(final ElkBendPoint point : section.getBendPoints())
                    {
                        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
                        joint.setX(point.getX());
                        joint.setY(point.getY());
                        newJoints.add(joint);
                    }
                }
                command.append(SetCommand.create(editingDomain, connection, GraphPackage.Literals.GCONNECTION__JOINTS, newJoints));
            }

            if (command.canExecute())
            {
                command.execute();
            }
        }
    }

    private ElkNode nodeToElk(final LayoutMetaDataProviderRegistry pRegistry, final GNode pNode, final Mapping pMapping)
    {
        final ElkNode model = ElkGraphFactory.eINSTANCE.createElkNode();
        model.setWidth(pNode.getWidth());
        model.setHeight(pNode.getHeight());
        model.setX(pNode.getX());
        model.setY(pNode.getY());
        model.setIdentifier(pNode.getId());
        setCommonProperties(pRegistry, LayoutOptionData.Target.NODES, model);

        final GNodeSkin skin = graphEditor.getSkinLookup().lookupNode(pNode);
        skin.layoutConnectors();

        for(final GConnector connector : pNode.getConnectors())
        {
            final ElkPort port = ElkGraphFactory.eINSTANCE.createElkPort();
            final GConnectorSkin connectorSkin = graphEditor.getSkinLookup().lookupConnector(connector);
            port.setX(connectorSkin.getRoot().getLayoutX());
            port.setY(connectorSkin.getRoot().getLayoutY());
            port.setWidth(connectorSkin.getWidth());
            port.setHeight(connectorSkin.getHeight());
            port.setIdentifier(connector.getId());
            setCommonProperties(pRegistry, LayoutOptionData.Target.PORTS, port);

            final Side side = DefaultConnectorTypes.getSide(connector.getType());
            if(side!=null)
            {
                switch (side)
                {
                    case TOP:
                        port.setProperty(CoreOptions.PORT_SIDE, PortSide.NORTH);
                        break;
                    case LEFT:
                        port.setProperty(CoreOptions.PORT_SIDE, PortSide.WEST);
                        break;
                    case RIGHT:
                        port.setProperty(CoreOptions.PORT_SIDE, PortSide.EAST);
                        break;
                    case BOTTOM:
                        port.setProperty(CoreOptions.PORT_SIDE, PortSide.SOUTH);
                        break;
                }
            }

            model.getPorts().add(port);
            pMapping.connectors.put(connector, port);
        }

        pMapping.nodes.put(pNode, model);
        return model;
    }

    private static class LayoutMetaDataProviderRegistry implements ILayoutMetaDataProvider.Registry {

        private final Map<LayoutOptionData, ComboBox<String>> imLayoutOptionData = new HashMap<>();
        private final Map<String, LayoutAlgorithmData> imLayouts = new HashMap<>();
        private final ComboBox<String> imCbxLayouts = new ComboBox<>();

        @Override
        public void register(LayoutAlgorithmData layoutAlgorithmData) {
            imLayouts.put(layoutAlgorithmData.getName(), layoutAlgorithmData);
            imCbxLayouts.getItems().add(layoutAlgorithmData.getName());
        }

        @Override
        public void register(LayoutOptionData layoutOptionData) {
            final String[] opts = layoutOptionData.getChoices();
            if (opts != null && opts.length > 0) {
                final ComboBox<String> cbx = new ComboBox<>(FXCollections.observableArrayList(layoutOptionData.getChoices()));
                imLayoutOptionData.put(layoutOptionData, cbx);
            }
        }

        @Override
        public void register(LayoutCategoryData layoutCategoryData) {

        }

        @Override
        public void addDependency(String s, String s1, Object o) {

        }

        @Override
        public void addOptionSupport(String s, String s1, Object o) {

        }

        public void addTo(final InvalidationListener pListener, final ObservableList<Node> pParent) {
            imLayoutOptionData.forEach((k, v) -> pParent.add(new HBox(new Label(k.getName()), v)));
            imLayoutOptionData.values().forEach(n -> n.valueProperty().addListener(pListener));
        }

        public void applyTo(final LayoutOptionData.Target pTarget, final IPropertyHolder pHolder) {
            for (final Map.Entry<LayoutOptionData, ComboBox<String>> entry : imLayoutOptionData.entrySet()) {
                final LayoutOptionData key = entry.getKey();
                final ComboBox<String> value = entry.getValue();
//                if (key.getTargets().contains(pTarget) && value.getValue() != null) {
                    pHolder.setProperty(key, key.parseValue(value.getValue()));
//                }
            }
        }

        public AbstractLayoutProvider fetchLayout() {
            final String selected = imCbxLayouts.getValue();
            return selected != null ? imLayouts.get(selected).getInstancePool().fetch() : null;
        }
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
    public void loadSampleLarge() {
        defaultSkinButton.setSelected(true);
        setDefaultSkin();
        graphEditorPersistence.loadSampleLarge(graphEditor);
    }

    @FXML
    public void loadTree() {
        treeSkinButton.setSelected(true);
        setTreeSkin();
        graphEditorPersistence.loadTree(graphEditor);
    }

    @FXML
    public void loadTitled() {
        titledSkinButton.setSelected(true);
        setTitledSkin();
        graphEditorPersistence.loadTitled(graphEditor);
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
        Commands.undo(graphEditor.getModel());
    }

    @FXML
    public void redo() {
        Commands.redo(graphEditor.getModel());
    }

    @FXML
    public void copy() {
        selectionCopier.copy();
    }

    @FXML
    public void paste() {
        activeSkinController.get().handlePaste(selectionCopier);
    }

    @FXML
    public void selectAll() {
        activeSkinController.get().handleSelectAll();
    }

    @FXML
    public void deleteSelection() {
        final List<EObject> selection = new ArrayList<>(graphEditor.getSelectionManager().getSelectedItems());
        graphEditor.delete(selection);
    }

    @FXML
    public void addNode() {
        activeSkinController.get().addNode(graphEditor.getView().getLocalToSceneTransform().getMxx());
    }

    @FXML
    public void addConnector() {
        activeSkinController.get().addConnector(getSelectedConnectorPosition(), inputConnectorTypeButton.isSelected());
    }

    @FXML
    public void clearConnectors() {
        activeSkinController.get().clearConnectors();
    }

    @FXML
    public void setDefaultSkin() {
        activeSkinController.set(defaultSkinController);
    }

    @FXML
    public void setTreeSkin() {
        activeSkinController.set(treeSkinController);
    }

    @FXML
    public void setTitledSkin() {
        activeSkinController.set(titledSkinController);
    }

    @FXML
    public void setGappedStyle() {

        graphEditor.getProperties().getCustomProperties().remove(SimpleConnectionSkin.SHOW_DETOURS_KEY);
        graphEditor.reload();
    }

    @FXML
    public void setDetouredStyle() {

        final Map<String, String> customProperties = graphEditor.getProperties().getCustomProperties();
        customProperties.put(SimpleConnectionSkin.SHOW_DETOURS_KEY, Boolean.toString(true));
        graphEditor.reload();
    }

    @FXML
    public void toggleMinimap() {
        graphEditorContainer.getMinimap().visibleProperty().bind(minimapButton.selectedProperty());
    }

    /**
     * Initializes the menu bar.
     */
    private void initializeMenuBar() {

        final ToggleGroup skinGroup = new ToggleGroup();
        skinGroup.getToggles().addAll(defaultSkinButton, treeSkinButton, titledSkinButton);

        final ToggleGroup connectionStyleGroup = new ToggleGroup();
        connectionStyleGroup.getToggles().addAll(gappedStyleButton, detouredStyleButton);

        final ToggleGroup connectorTypeGroup = new ToggleGroup();
        connectorTypeGroup.getToggles().addAll(inputConnectorTypeButton, outputConnectorTypeButton);

        final ToggleGroup positionGroup = new ToggleGroup();
        positionGroup.getToggles().addAll(leftConnectorPositionButton, rightConnectorPositionButton);
        positionGroup.getToggles().addAll(topConnectorPositionButton, bottomConnectorPositionButton);

        graphEditor.getProperties().gridVisibleProperty().bind(showGridButton.selectedProperty());
        graphEditor.getProperties().snapToGridProperty().bind(snapToGridButton.selectedProperty());
        graphEditor.getProperties().readOnlyProperty().bind(readOnlyButton.selectedProperty());

        minimapButton.setGraphic(AwesomeIcon.MAP.node());

        final SetChangeListener<? super EObject> selectedNodesListener = change -> checkConnectorButtonsToDisable();
        graphEditor.getSelectionManager().getSelectedItems().addListener(selectedNodesListener);
        checkConnectorButtonsToDisable();
    }

    /**
     * Adds a listener to make changes to available menu options when the skin type changes.
     */
    private void addActiveSkinControllerListener() {

        activeSkinController.addListener((observable, oldValue, newValue) -> {
            handleActiveSkinControllerChange();
        });
    }

    /**
     * Enables & disables certain menu options and sets CSS classes based on the new skin type that was set active.
     */
    private void handleActiveSkinControllerChange() {

        if (treeSkinController.equals(activeSkinController.get())) {

            graphEditor.setConnectorValidator(new TreeConnectorValidator());
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED_SKINS);
            treeSkinButton.setSelected(true);

        } else if (titledSkinController.equals(activeSkinController.get())) {

            graphEditor.setConnectorValidator(null);
            if (!graphEditor.getView().getStyleClass().contains(STYLE_CLASS_TITLED_SKINS)) {
                graphEditor.getView().getStyleClass().add(STYLE_CLASS_TITLED_SKINS);
            }
            titledSkinButton.setSelected(true);

        } else {

            graphEditor.setConnectorValidator(null);
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED_SKINS);
            defaultSkinButton.setSelected(true);
        }

        // Demo does not currently support mixing of skin types. Skins don't know how to cope with it.
        clearAll();
        flushCommandStack();
        checkConnectorButtonsToDisable();
        selectionCopier.clearMemory();
    }

    /**
     * Crudely inspects the model's first node and sets the new skin type accordingly.
     */
    private void checkSkinType() {

        if (!graphEditor.getModel().getNodes().isEmpty()) {

            final GNode firstNode = graphEditor.getModel().getNodes().get(0);
            final String type = firstNode.getType();

            if (TreeSkinConstants.TREE_NODE.equals(type)) {
                activeSkinController.set(treeSkinController);
            } else if (TitledSkinConstants.TITLED_NODE.equals(type)) {
                activeSkinController.set(titledSkinController);
            } else {
                activeSkinController.set(defaultSkinController);
            }
        }
    }

    /**
     * Checks if the connector buttons need disabling (e.g. because no nodes are selected).
     */
    private void checkConnectorButtonsToDisable() {

		final boolean nothingSelected = graphEditor.getSelectionManager().getSelectedItems().stream()
				.noneMatch(e -> e instanceof GNode);

        final boolean treeSkinActive = treeSkinController.equals(activeSkinController.get());
        final boolean titledSkinActive = titledSkinController.equals(activeSkinController.get());

        if (titledSkinActive || treeSkinActive) {
            addConnectorButton.setDisable(true);
            clearConnectorsButton.setDisable(true);
            connectorTypeMenu.setDisable(true);
            connectorPositionMenu.setDisable(true);
        } else if (nothingSelected) {
            addConnectorButton.setDisable(true);
            clearConnectorsButton.setDisable(true);
            connectorTypeMenu.setDisable(false);
            connectorPositionMenu.setDisable(false);
        } else {
            addConnectorButton.setDisable(false);
            clearConnectorsButton.setDisable(false);
            connectorTypeMenu.setDisable(false);
            connectorPositionMenu.setDisable(false);
        }

        intersectionStyle.setDisable(treeSkinActive);
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
     * Gets the side corresponding to the currently selected connector position in the menu.
     *
     * @return the {@link Side} corresponding to the currently selected connector position
     */
    private Side getSelectedConnectorPosition() {

        if (leftConnectorPositionButton.isSelected()) {
            return Side.LEFT;
        } else if (rightConnectorPositionButton.isSelected()) {
            return Side.RIGHT;
        } else if (topConnectorPositionButton.isSelected()) {
            return Side.TOP;
        } else {
            return Side.BOTTOM;
        }
    }
}
