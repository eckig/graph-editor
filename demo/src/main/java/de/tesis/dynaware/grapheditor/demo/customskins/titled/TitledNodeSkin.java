/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.titled;

import java.util.ArrayList;
import java.util.List;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

/**
 * A grey node with a navy title-bar for the 'titled-skins' theme.
 */
public class TitledNodeSkin extends GNodeSkin {

    private static final String TITLE_TEXT = "Node "; //$NON-NLS-1$

    private static final String STYLE_CLASS_BORDER = "titled-node-border"; //$NON-NLS-1$
    private static final String STYLE_CLASS_BACKGROUND = "titled-node-background"; //$NON-NLS-1$
    private static final String STYLE_CLASS_SELECTION_HALO = "titled-node-selection-halo"; //$NON-NLS-1$
    private static final String STYLE_CLASS_HEADER = "titled-node-header"; //$NON-NLS-1$
    private static final String STYLE_CLASS_TITLE = "titled-node-title"; //$NON-NLS-1$
    private static final String STYLE_CLASS_BUTTON = "titled-node-close-button"; //$NON-NLS-1$

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected"); //$NON-NLS-1$

    private static final double HALO_OFFSET = 5;
    private static final double HALO_CORNER_SIZE = 10;

    private static final double MIN_WIDTH = 81;
    private static final double MIN_HEIGHT = 81;

    private static final int BORDER_WIDTH = 1;
    private static final int HEADER_HEIGHT = 20;

    private final Rectangle selectionHalo = new Rectangle();

    private VBox contentRoot = new VBox();
    private HBox header = new HBox();
    private Label title = new Label();

    private final List<GConnectorSkin> inputConnectorSkins = new ArrayList<>();
    private final List<GConnectorSkin> outputConnectorSkins = new ArrayList<>();

    private final Rectangle border = new Rectangle();

    /**
     * Creates a new {@link TitledNodeSkin} instance.
     *
     * @param node the {link GNode} this skin is representing
     */
    public TitledNodeSkin(final GNode node) {

        super(node);

        border.getStyleClass().setAll(STYLE_CLASS_BORDER);
        border.widthProperty().bind(getRoot().widthProperty());
        border.heightProperty().bind(getRoot().heightProperty());

        getRoot().getChildren().add(border);
        getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

        addSelectionHalo();

        createContent();

        contentRoot.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::filterMouseDragged);
    }

    @Override
    public void initialize() {
        super.initialize();
        title.setText(TITLE_TEXT + getItem().getId());
    }

    @Override
    public void setConnectorSkins(final List<GConnectorSkin> connectorSkins) {

        removeAllConnectors();

        inputConnectorSkins.clear();
        outputConnectorSkins.clear();

        if (connectorSkins != null) {
            for (final GConnectorSkin connectorSkin : connectorSkins) {

                final boolean isInput = connectorSkin.getItem().getType().contains("input"); //$NON-NLS-1$
                final boolean isOutput = connectorSkin.getItem().getType().contains("output"); //$NON-NLS-1$

                if (isInput) {
                    inputConnectorSkins.add(connectorSkin);
                } else if (isOutput) {
                    outputConnectorSkins.add(connectorSkin);
                }

                if (isInput || isOutput) {
                    getRoot().getChildren().add(connectorSkin.getRoot());
                }
            }
        }

        setConnectorsSelected();
    }

    @Override
    public void layoutConnectors() {
        layoutLeftAndRightConnectors();
        layoutSelectionHalo();
    }

    @Override
    public Point2D getConnectorPosition(final GConnectorSkin connectorSkin) {

        final Node connectorRoot = connectorSkin.getRoot();

        final double x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
        final double y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;

        if (inputConnectorSkins.contains(connectorSkin)) {
            return new Point2D(x, y);
        }
        // ELSE:
        // Subtract 1 to align start-of-connection correctly. Compensation for rounding errors?
        return new Point2D(x - 1, y);
    }

    /**
     * Creates the content of the node skin - header, title, close button, etc.
     */
    private void createContent() {

        header.getStyleClass().setAll(STYLE_CLASS_HEADER);
        header.setAlignment(Pos.CENTER);

        title.getStyleClass().setAll(STYLE_CLASS_TITLE);

        final Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        final Button closeButton = new Button();
        closeButton.getStyleClass().setAll(STYLE_CLASS_BUTTON);

        header.getChildren().addAll(title, filler, closeButton);
        contentRoot.getChildren().add(header);
        getRoot().getChildren().add(contentRoot);

        closeButton.setGraphic(AwesomeIcon.TIMES.node());
        closeButton.setCursor(Cursor.DEFAULT);
        closeButton.setOnAction(event -> Commands.removeNode(getGraphEditor().getModel(), getItem()));

        contentRoot.minWidthProperty().bind(getRoot().widthProperty());
        contentRoot.prefWidthProperty().bind(getRoot().widthProperty());
        contentRoot.maxWidthProperty().bind(getRoot().widthProperty());
        contentRoot.minHeightProperty().bind(getRoot().heightProperty());
        contentRoot.prefHeightProperty().bind(getRoot().heightProperty());
        contentRoot.maxHeightProperty().bind(getRoot().heightProperty());

        contentRoot.setLayoutX(BORDER_WIDTH);
        contentRoot.setLayoutY(BORDER_WIDTH);

        contentRoot.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
    }

    /**
     * Lays out all connectors.
     */
    private void layoutLeftAndRightConnectors() {

        final int inputCount = inputConnectorSkins.size();
        final double inputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (inputCount + 1);

        for (int i = 0; i < inputCount; i++) {

            final GConnectorSkin inputSkin = inputConnectorSkins.get(i);
            final Node connectorRoot = inputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(0 - inputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * inputOffsetY - inputSkin.getHeight() / 2);

            connectorRoot.setLayoutX(layoutX);
            connectorRoot.setLayoutY(layoutY + HEADER_HEIGHT);
        }

        final int outputCount = outputConnectorSkins.size();
        final double outputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (outputCount + 1);

        for (int i = 0; i < outputCount; i++) {

            final GConnectorSkin outputSkin = outputConnectorSkins.get(i);
            final Node connectorRoot = outputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(getRoot().getWidth() - outputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * outputOffsetY - outputSkin.getHeight() / 2);

            connectorRoot.setLayoutX(layoutX);
            connectorRoot.setLayoutY(layoutY + HEADER_HEIGHT);
        }
    }

    /**
     * Adds the selection halo and initializes some of its values.
     */
    private void addSelectionHalo() {

        getRoot().getChildren().add(selectionHalo);

        selectionHalo.setManaged(false);
        selectionHalo.setMouseTransparent(false);
        selectionHalo.setVisible(false);

        selectionHalo.setLayoutX(-HALO_OFFSET);
        selectionHalo.setLayoutY(-HALO_OFFSET);

        selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
    }

    /**
     * Lays out the selection halo based on the current width and height of the node skin region.
     */
    private void layoutSelectionHalo() {

        if (selectionHalo.isVisible()) {

            selectionHalo.setWidth(getRoot().getWidth() + 2 * HALO_OFFSET);
            selectionHalo.setHeight(getRoot().getHeight() + 2 * HALO_OFFSET);

            final double cornerLength = 2 * HALO_CORNER_SIZE;
            final double xGap = getRoot().getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
            final double yGap = getRoot().getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

            selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
            selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
        }
    }


    @Override
    protected void selectionChanged(final boolean isSelected) {
        if (isSelected) {
            selectionHalo.setVisible(true);
            layoutSelectionHalo();
            contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            getRoot().toFront();
        } else {
            selectionHalo.setVisible(false);
            contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
        }
        setConnectorsSelected();
    }

    /**
     * Removes any input and output connectors from the list of children, if they exist.
     */
    private void removeAllConnectors() {

        for (final GConnectorSkin connectorSkin : inputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }

        for (final GConnectorSkin connectorSkin : outputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }
    }

    /**
     * Adds or removes the 'selected' pseudo-class from all connectors belonging to this node.
     */
    private void setConnectorsSelected()
    {

    	final GraphEditor editor = getGraphEditor();
    	if(editor == null) {
    		return;
    	}

        for (final GConnectorSkin skin : inputConnectorSkins) {
            if (skin instanceof TitledConnectorSkin) {
            	editor.getSelectionManager().select(skin.getItem());
            }
        }

        for (final GConnectorSkin skin : outputConnectorSkins) {
            if (skin instanceof TitledConnectorSkin) {
            	editor.getSelectionManager().select(skin.getItem());
            }
        }
    }

    /**
     * Stops the node being dragged if it isn't selected.
     *
     * @param event a mouse-dragged event on the node
     */
    private void filterMouseDragged(final MouseEvent event) {
        if (event.isPrimaryButtonDown() && !isSelected()) {
            event.consume();
        }
    }
}
