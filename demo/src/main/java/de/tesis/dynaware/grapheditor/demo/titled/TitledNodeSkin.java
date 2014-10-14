/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.titled;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

public class TitledNodeSkin extends GNodeSkin {

    private static final String TITLE_TEXT = "Node ";

    private static final String STYLE_CLASS_BORDER = "titled-node-border";
    private static final String STYLE_CLASS_BACKGROUND = "titled-node-background";
    private static final String STYLE_CLASS_SELECTION_HALO = "titled-node-selection-halo";
    private static final String STYLE_CLASS_HEADER = "titled-node-header";
    private static final String STYLE_CLASS_TITLE = "titled-node-title";
    private static final String STYLE_CLASS_BUTTON = "titled-node-close-button";

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final double HALO_OFFSET = 5;
    private static final double HALO_CORNER_SIZE = 10;

    private static final double MIN_WIDTH = 81;
    private static final double MIN_HEIGHT = 81;

    private static final int BORDER_WIDTH = 1;
    private static final int HEADER_HEIGHT = 10;

    private final Rectangle selectionHalo = new Rectangle();

    private VBox contentRoot = new VBox();
    private Label title = new Label();

    private final List<GConnectorSkin> inputConnectorSkins = new ArrayList<>();
    private final List<GConnectorSkin> outputConnectorSkins = new ArrayList<>();

    public TitledNodeSkin(final GNode node) {

        super(node);

        getRoot().getBorderRectangle().getStyleClass().add(STYLE_CLASS_BORDER);
        getRoot().getBackgroundRectangle().setVisible(false);
        getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

        addSelectionHalo();
        addSelectionListener();

        createContent();
    }

    @Override
    public void initialize() {
        super.initialize();
        title.setText(TITLE_TEXT + getNode().getId());
    }

    @Override
    public void setConnectorSkins(final List<GConnectorSkin> connectorSkins) {

        removeAllConnectors();

        inputConnectorSkins.clear();
        outputConnectorSkins.clear();

        if (connectorSkins != null) {
            for (final GConnectorSkin connectorSkin : connectorSkins) {

                final boolean isInput = connectorSkin.getConnector().getType().contains("input");
                final boolean isOutput = connectorSkin.getConnector().getType().contains("output");

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

        return new Point2D(x, y);
    }

    private void createContent() {

        final HBox header = new HBox();
        header.getStyleClass().add(STYLE_CLASS_HEADER);
        header.setAlignment(Pos.CENTER);

        title.getStyleClass().add(STYLE_CLASS_TITLE);

        final Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        final Button closeButton = new Button();
        closeButton.getStyleClass().add(STYLE_CLASS_BUTTON);

        header.getChildren().addAll(title, filler, closeButton);
        contentRoot.getChildren().add(header);
        getRoot().getChildren().add(contentRoot);

        closeButton.setGraphic(AwesomeIcon.TIMES.node());
        closeButton.setCursor(Cursor.DEFAULT);
        closeButton.setOnAction(event -> Commands.removeNode(getGraphEditor().getModel(), getNode()));

        final DoubleProperty width = getRoot().getBorderRectangle().widthProperty();
        final DoubleProperty height = getRoot().getBorderRectangle().heightProperty();

        contentRoot.minWidthProperty().bind(width);
        contentRoot.prefWidthProperty().bind(width);
        contentRoot.maxWidthProperty().bind(width);
        contentRoot.minHeightProperty().bind(height);
        contentRoot.prefHeightProperty().bind(height);
        contentRoot.maxHeightProperty().bind(height);

        contentRoot.setLayoutX(BORDER_WIDTH);
        contentRoot.setLayoutY(BORDER_WIDTH);

        contentRoot.getStyleClass().add(STYLE_CLASS_BACKGROUND);
    }

    private void layoutLeftAndRightConnectors() {

        final int inputCount = inputConnectorSkins.size();
        final double inputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (inputCount + 1);

        for (int i = 0; i < inputCount; i++) {

            final GConnectorSkin inputSkin = inputConnectorSkins.get(i);
            final Node connectorRoot = inputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(0 - inputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * inputOffsetY - inputSkin.getHeight() / 2)
                    + HEADER_HEIGHT;

            if (inputSkin.getConnector().getType().contains("triangle")) {
                connectorRoot.setLayoutX(layoutX + 1);
            } else {
                connectorRoot.setLayoutX(layoutX);
            }
            connectorRoot.setLayoutY(layoutY);
        }

        final int outputCount = outputConnectorSkins.size();
        final double outputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (outputCount + 1);

        for (int i = 0; i < outputCount; i++) {

            final GConnectorSkin outputSkin = outputConnectorSkins.get(i);
            final Node connectorRoot = outputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(getRoot().getWidth() - outputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * outputOffsetY - outputSkin.getHeight() / 2)
                    + HEADER_HEIGHT;

            if (outputSkin.getConnector().getType().contains("triangle")) {
                connectorRoot.setLayoutX(layoutX + 1);
            } else {
                connectorRoot.setLayoutX(layoutX);
            }
            connectorRoot.setLayoutY(layoutY);
        }
    }

    private void addSelectionHalo() {

        getRoot().getChildren().add(selectionHalo);

        selectionHalo.setManaged(false);
        selectionHalo.setMouseTransparent(false);
        selectionHalo.setVisible(false);

        selectionHalo.setLayoutX(-HALO_OFFSET);
        selectionHalo.setLayoutY(-HALO_OFFSET);

        selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
    }

    private void layoutSelectionHalo() {

        if (selectionHalo.isVisible()) {

            final Rectangle border = getRoot().getBorderRectangle();

            selectionHalo.setWidth(border.getWidth() + 2 * HALO_OFFSET);
            selectionHalo.setHeight(border.getHeight() + 2 * HALO_OFFSET);

            final double cornerLength = 2 * HALO_CORNER_SIZE;
            final double xGap = border.getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
            final double yGap = border.getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

            selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
            selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
        }
    }

    private void addSelectionListener() {

        selectedProperty().addListener((v, o, n) -> {

            if (n) {
                selectionHalo.setVisible(true);
                layoutSelectionHalo();
                contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
                getRoot().toFront();
            } else {
                selectionHalo.setVisible(false);
                contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
            }
        });
    }

    private void removeAllConnectors() {

        for (final GConnectorSkin connectorSkin : inputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }

        for (final GConnectorSkin connectorSkin : outputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }
    }
}
