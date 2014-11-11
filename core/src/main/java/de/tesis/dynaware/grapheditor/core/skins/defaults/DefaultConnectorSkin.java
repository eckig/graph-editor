/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.DefaultConnectorTypes;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * The default connector skin.
 *
 * <p>
 * A connector that uses this skin must have one of the 8 types defined in {@link DefaultConnectorTypes}. If the
 * connector does not have one of these types, it will be set to <b>left-input</b>.
 * </p>
 */
public class DefaultConnectorSkin extends GConnectorSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectorSkin.class);

    private static final String STYLE_CLASS_BASE = "default-connector";

    private static final PseudoClass PSEUDO_CLASS_ALLOWED = PseudoClass.getPseudoClass("allowed");
    private static final PseudoClass PSEUDO_CLASS_FORBIDDEN = PseudoClass.getPseudoClass("forbidden");

    private static final double SIZE = 25;

    private final Pane root = new Pane();
    private final Polygon polygon = new Polygon();

    /**
     * Creates a new default connector skin instance.
     *
     * @param connector the {@link GConnector} the skin is being created for
     */
    public DefaultConnectorSkin(final GConnector connector) {

        super(connector);

        performChecks();

        root.setManaged(false);
        root.resize(SIZE, SIZE);
        root.setPickOnBounds(false);

        polygon.setManaged(false);
        polygon.getStyleClass().addAll(STYLE_CLASS_BASE, connector.getType());

        switch (connector.getType()) {

        case DefaultConnectorTypes.TOP_INPUT:
            drawVertical(false);
            break;

        case DefaultConnectorTypes.TOP_OUTPUT:
            drawVertical(true);
            break;

        case DefaultConnectorTypes.RIGHT_INPUT:
            drawHorizontal(false);
            break;

        case DefaultConnectorTypes.RIGHT_OUTPUT:
            drawHorizontal(true);
            break;

        case DefaultConnectorTypes.BOTTOM_INPUT:
            drawVertical(true);
            break;

        case DefaultConnectorTypes.BOTTOM_OUTPUT:
            drawVertical(false);
            break;

        case DefaultConnectorTypes.LEFT_INPUT:
            drawHorizontal(true);
            break;

        case DefaultConnectorTypes.LEFT_OUTPUT:
            drawHorizontal(false);
            break;
        }

        root.getChildren().add(polygon);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public double getWidth() {
        return SIZE;
    }

    @Override
    public double getHeight() {
        return SIZE;
    }

    @Override
    public void applyStyle(final GConnectorStyle style) {

        switch (style) {

        case DEFAULT:
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;

        case DRAG_OVER_ALLOWED:
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, true);
            break;

        case DRAG_OVER_FORBIDDEN:
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, true);
            polygon.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            break;
        }
    }

    /**
     * Checks that the connector has the correct values to be displayed using this skin.
     */
    private void performChecks() {
        if (!DefaultConnectorTypes.isValid(getConnector().getType())) {
            LOGGER.error(LogMessages.UNSUPPORTED_CONNECTOR, getConnector().getType());
            getConnector().setType(DefaultConnectorTypes.LEFT_INPUT);
        }
    }

    /**
     * Draws the polygon for a horizontal orientation, pointing right or left.
     * 
     * @param pointingRight {@code true} to point right, {@code false} to point left
     */
    private void drawHorizontal(final boolean pointingRight) {

        if (pointingRight) {
            polygon.getPoints().addAll(new Double[] { 0D, 0D, SIZE, SIZE / 2, 0D, SIZE });
        } else {
            polygon.getPoints().addAll(new Double[] { SIZE, 0D, SIZE, SIZE, 0D, SIZE / 2 });
        }
    }

    /**
     * Draws the polygon for a vertical orientation, pointing up or down.
     * 
     * @param pointingUp {@code true} to point up, {@code false} to point down
     */
    private void drawVertical(final boolean pointingUp) {

        if (pointingUp) {
            polygon.getPoints().addAll(new Double[] { SIZE / 2, 0D, SIZE, SIZE, 0D, SIZE });
        } else {
            polygon.getPoints().addAll(new Double[] { 0D, 0D, SIZE, 0D, SIZE / 2, SIZE });
        }
    }
}
