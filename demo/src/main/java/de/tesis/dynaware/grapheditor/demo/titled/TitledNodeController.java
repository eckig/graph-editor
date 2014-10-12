/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.titled;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.demo.utils.AwesomeIcon;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

public class TitledNodeController {

    private static final String TITLE_TEXT_PREFIX = "Node ";
    public static int counter = 0;

    @FXML
    private VBox root;
    @FXML
    private Label titleLabel;
    @FXML
    private Button closeButton;

    private GNode node;

    public void initialize() {

        closeButton.setGraphic(AwesomeIcon.TIMES.node());
        closeButton.setCursor(Cursor.DEFAULT);
        closeButton.setOnAction(event -> removeNode());

        titleLabel.setText(TITLE_TEXT_PREFIX + counter);
        counter++;
    }

    public void setNode(final GNode node) {
        this.node = node;
    }

    private void removeNode() {
        Commands.removeNode((GModel) node.eContainer(), node);
    }
}
