/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * The default connector skin.
 *
 * <p>
 * Currently expects its {@link GConnector} to have a type of either <b>input</b> or <b>output</b>.
 * </p>
 */
public class DefaultConnectorSkin extends GConnectorSkin {

    private static final String STYLE_CLASS_INPUT = "default-input-connector";
    private static final String STYLE_CLASS_OUTPUT = "default-output-connector";

    private static final PseudoClass PSEUDO_CLASS_ALLOWED = PseudoClass.getPseudoClass("allowed");
    private static final PseudoClass PSEUDO_CLASS_FORBIDDEN = PseudoClass.getPseudoClass("forbidden");

    private static final double WIDTH = 25;
    private static final double HEIGHT = 25;

    private final Pane root = new Pane();
    private final Polygon triangle = new Polygon();

    /**
     * Creates a new default connector skin instance.
     *
     * @param connector the {@link GConnector} the skin is being created for
     */
    public DefaultConnectorSkin(final GConnector connector) {

        super(connector);

        root.setManaged(false);
        root.resize(WIDTH, HEIGHT);
        root.setPickOnBounds(false);

        triangle.setManaged(false);
        triangle.getPoints().addAll(new Double[] { 0D, 0D, WIDTH, HEIGHT / 2, 0D, HEIGHT });

        if (DefaultSkinConstants.INPUT_TYPE.equals(connector.getType())) {
            triangle.getStyleClass().setAll(STYLE_CLASS_INPUT);
        } else {
            triangle.getStyleClass().setAll(STYLE_CLASS_OUTPUT);
        }

        root.getChildren().add(triangle);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public double getWidth() {
        return WIDTH;
    }

    @Override
    public double getHeight() {
        return HEIGHT;
    }

    @Override
    public void applyStyle(final GConnectorStyle style) {

        switch (style) {

        case DEFAULT:
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;

        case DRAG_OVER_ALLOWED:
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, true);
            break;

        case DRAG_OVER_FORBIDDEN:
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, true);
            triangle.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;
        }
    }
}
