/*
 * Copyright (C) 2005 - 2015 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import java.util.function.BiPredicate;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import de.tesis.dynaware.grapheditor.GConnectionSkin;

/**
 * A predicate that determines whether a tree connection skin is within the selection-box.
 */
public class TreeConnectionSelectionPredicate implements BiPredicate<GConnectionSkin, Rectangle2D> {

    private static final String ARROW_LINE_SELECTOR = ".tree-connection .arrow-line";

    @Override
    public boolean test(final GConnectionSkin connectionSkin, final Rectangle2D selectionBox) {

        final Node arrowLineNode = connectionSkin.getRoot().lookup(ARROW_LINE_SELECTOR);

        if (arrowLineNode != null) {

            final double x = arrowLineNode.getLayoutBounds().getMinX();
            final double y = arrowLineNode.getLayoutBounds().getMinY();
            final double width = arrowLineNode.getLayoutBounds().getWidth();
            final double height = arrowLineNode.getLayoutBounds().getHeight();

            return selectionBox.contains(x, y, width, height);

        } else {
            return false;
        }
    }
}
