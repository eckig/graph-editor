/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import de.tesis.dynaware.grapheditor.GraphEditor;

/**
 * A demo application to show uses of the {@link GraphEditor} library.
 */
public class GraphEditorDemo extends Application {

    private static final String APPLICATION_TITLE = "Graph Editor Demo";
    private static final String DEMO_STYLESHEET = "demo.css";
    private static final String TREE_SKIN_STYLESHEET = "treeskins.css";
    private static final String TITLED_SKIN_STYLESHEET = "greyskins.css";
    private static final String FONT_AWESOME = "fontawesome.ttf";

    @Override
    public void start(final Stage stage) throws Exception {

        final URL location = getClass().getResource("GraphEditorDemo.fxml");
        final FXMLLoader loader = new FXMLLoader();
        final Parent root = loader.load(location.openStream());
        final GraphEditorDemoController controller = loader.getController();

        final Scene scene = new Scene(root, 830, 630);

        scene.getStylesheets().add(getClass().getResource(DEMO_STYLESHEET).toExternalForm());
        scene.getStylesheets().add(getClass().getResource(TREE_SKIN_STYLESHEET).toExternalForm());
        scene.getStylesheets().add(getClass().getResource(TITLED_SKIN_STYLESHEET).toExternalForm());
        Font.loadFont(getClass().getResource(FONT_AWESOME).toExternalForm(), 12);

        stage.setScene(scene);
        stage.setTitle(APPLICATION_TITLE);

        stage.show();

        controller.panToCenter();
    }

    public static void main(final String[] args) {
        launch(args);
    }
}