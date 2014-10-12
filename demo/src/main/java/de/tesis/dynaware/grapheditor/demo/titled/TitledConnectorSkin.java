/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.titled;

import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GConnectorStyle;
import de.tesis.dynaware.grapheditor.model.GConnector;

public class TitledConnectorSkin extends GConnectorSkin {

    private static final String STYLE_CLASS = "titled-connector";
    private static final String STYLE_CLASS_FORBIDDEN_GRAPHIC = "titled-connector-forbidden-graphic";

    private static final double SIZE = 16;

    private static final PseudoClass PSEUDO_CLASS_ALLOWED = PseudoClass.getPseudoClass("allowed");
    private static final PseudoClass PSEUDO_CLASS_FORBIDDEN = PseudoClass.getPseudoClass("forbidden");

    private final Pane root = new Pane();
    private final Rectangle square = new Rectangle(SIZE, SIZE);

    private final Group forbiddenGraphic;

    /**
     * Creates a new {@link TitledConnectorSkin} instance.
     *
     * @param connector the {@link GConnector} that this skin is representing
     */
    public TitledConnectorSkin(final GConnector connector) {

        super(connector);

        root.setMinSize(SIZE, SIZE);
        root.setPrefSize(SIZE, SIZE);
        root.setMaxSize(SIZE, SIZE);
        root.setPickOnBounds(false);

        square.setManaged(false);
        square.resizeRelocate(0, 0, SIZE, SIZE);
        square.getStyleClass().setAll(STYLE_CLASS);

        forbiddenGraphic = createForbiddenGraphic();

        root.getChildren().addAll(square, forbiddenGraphic);
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
            square.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            square.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            forbiddenGraphic.setVisible(false);
            break;

        case DRAG_OVER_ALLOWED:
            square.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            square.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, true);
            forbiddenGraphic.setVisible(false);
            break;

        case DRAG_OVER_FORBIDDEN:
            square.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, true);
            square.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            forbiddenGraphic.setVisible(true);
            break;
        }
    }

    private Group createForbiddenGraphic() {

        final Group group = new Group();
        final Line firstLine = new Line(1, 1, SIZE - 1, SIZE - 1);
        final Line secondLine = new Line(1, SIZE - 1, SIZE - 1, 1);

        firstLine.getStyleClass().add(STYLE_CLASS_FORBIDDEN_GRAPHIC);
        secondLine.getStyleClass().add(STYLE_CLASS_FORBIDDEN_GRAPHIC);

        group.getChildren().addAll(firstLine, secondLine);
        group.setVisible(false);

        return group;
    }
}
