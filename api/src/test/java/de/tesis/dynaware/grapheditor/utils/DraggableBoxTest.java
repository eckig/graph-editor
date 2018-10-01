/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import static de.tesis.dynaware.grapheditor.utils.FXTestUtils.dragBy;
import static de.tesis.dynaware.grapheditor.utils.FXTestUtils.dragTo;
import static de.tesis.dynaware.grapheditor.utils.FXTestUtils.forceLayoutUpdate;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javafx.scene.layout.Pane;

public class DraggableBoxTest {

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double BOX_WIDTH = 80;
    private static final double BOX_HEIGHT = 50;
    private static final double BOX_INITIAL_X = 48;
    private static final double BOX_INITIAL_Y = 39;

    private static final double BOUNDARY_INDENT = 12;
    private static final double GRID_SPACING = 11;
    private static final double ALIGNMENT_THRESHOLD = 16;

    private final DraggableBox box = new DraggableBox();
    private final Pane container = new Pane();

    private final GraphEditorProperties properties = new GraphEditorProperties();

    @Before
    public void setUp() throws Exception {

        properties.setEastBoundValue(BOUNDARY_INDENT);
        properties.setWestBoundValue(BOUNDARY_INDENT);
        properties.setNorthBoundValue(BOUNDARY_INDENT);
        properties.setSouthBoundValue(BOUNDARY_INDENT);
        properties.setSnapToGrid(false);

        box.resize(BOX_WIDTH, BOX_HEIGHT);
        box.setLayoutX(BOX_INITIAL_X);
        box.setLayoutY(BOX_INITIAL_Y);
        box.setAlignmentTargetsX(null);
        box.setAlignmentTargetsY(null);
        box.setEditorProperties(properties);

        container.setPrefSize(400, 300);
        container.autosize();
        container.getChildren().add(box);
    }

    @Test
    public void testDrag_Simple() {

        // Try some simple drag operations.
        dragBy(box, 100, 100);
        assertAt(BOX_INITIAL_X + 100, BOX_INITIAL_Y + 100);

        dragBy(box, -50, 75);
        assertAt(BOX_INITIAL_X + 50, BOX_INITIAL_Y + 175);

        // Move the cursor to the top left of the scene - should stop at the boundary values.
        dragTo(box, 10, 10);
        assertAt(BOUNDARY_INDENT, BOUNDARY_INDENT);

        // Move the cursor to the bottom right - should stop at the boundary values.
        dragTo(box, 400, 300);
        assertAt(400 - BOUNDARY_INDENT - BOX_WIDTH, 300 - BOUNDARY_INDENT - BOX_HEIGHT);
    }

    @Test
    public void testDrag_SnapToGrid() {

        properties.setSnapToGrid(true);
        properties.setGridSpacing(GRID_SPACING);

        dragBy(box, 14, 17);

        final double x = box.getLayoutX();
        final double y = box.getLayoutY();

        assertTrue(x == Math.round(x / GRID_SPACING) * GRID_SPACING);
        assertTrue(y == Math.round(y / GRID_SPACING) * GRID_SPACING);
    }

    @Test
    public void testDrag_AlignmentTargets() {

        final double[] alignmentTargetsX = new double[] {67.0};
        final double[] alignmentTargetsY = new double[] {184.0};

        box.setAlignmentThreshold(ALIGNMENT_THRESHOLD);
        box.setAlignmentTargetsX(alignmentTargetsX);
        box.setAlignmentTargetsY(alignmentTargetsY);

        dragTo(box, 40 + BOX_WIDTH / 2, 0);
        assertTrue(box.getLayoutX() == 67.0);

        dragTo(box, 0, 170 + BOX_HEIGHT / 2);
        assertTrue(box.getLayoutY() == 184.0);
    }

    /**
     * Asserts that the box's layout X and Y values are at the given positions.
     *
     * @param x the x value that the box should have
     * @param y the y value that the box should have
     */
    private void assertAt(final double x, final double y) {
        forceLayoutUpdate(container);
        assertTrue(box.getLayoutX() == x && box.getLayoutY() == y);
    }
}