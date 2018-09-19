package de.tesis.dynaware.grapheditor.utils;

import static de.tesis.dynaware.grapheditor.utils.FXTestUtils.dragBy;
import static de.tesis.dynaware.grapheditor.utils.FXTestUtils.forceLayoutUpdate;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javafx.scene.layout.Pane;

public class ResizableBoxTest {

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private final static double BOX_WIDTH = 240;
    private final static double BOX_HEIGHT = 100;
    private final static double BOX_INITIAL_X = 60;
    private final static double BOX_INITIAL_Y = 60;
    private final static double BOX_RESIZE_BORDER_TOLERANCE = 1;

    private final ResizableBox box = new ResizableBox();
    private final Pane container = new Pane();

    private final GraphEditorProperties properties = new GraphEditorProperties();

    @Before
    public void setUp() throws Exception {

        box.setEditorProperties(properties);
        box.resize(BOX_WIDTH, BOX_HEIGHT);

        box.setLayoutX(BOX_INITIAL_X);
        box.setLayoutY(BOX_INITIAL_Y);

        container.setPrefSize(360, 300);
        container.autosize();
        container.getChildren().add(box);

        forceLayoutUpdate(container);
    }

    @Test
    public void testResize_UpperLeftCorner() {

        dragBy(box, 24, 33);
        assertSize(BOX_WIDTH - 24, BOX_HEIGHT - 33);
        assertPosition(BOX_INITIAL_X + 24, BOX_INITIAL_Y + 33);

        dragBy(box, 99, -10);
        assertSize(BOX_WIDTH - 123, BOX_HEIGHT - 23);
        assertPosition(BOX_INITIAL_X + 123, BOX_INITIAL_Y + 23);
    }

    @Test
    public void testResize_LowerRightCorner() {

        final double offsetX = BOX_WIDTH - BOX_RESIZE_BORDER_TOLERANCE;
        final double offsetY = BOX_HEIGHT - BOX_RESIZE_BORDER_TOLERANCE;

        dragBy(box, offsetX, offsetY, -24, -33);
        assertSize(BOX_WIDTH - 24, BOX_HEIGHT - 33);
        assertPosition(BOX_INITIAL_X, BOX_INITIAL_Y);

        dragBy(box, offsetX - 24, offsetY - 33, -99, 10);
        assertSize(BOX_WIDTH - 123, BOX_HEIGHT - 23);
        assertPosition(BOX_INITIAL_X, BOX_INITIAL_Y);
    }

    /**
     * Asserts that the box's size matches the given width and height parameter
     *
     * @param width the width that the box should have
     * @param height the height that the box should have
     */
    private void assertSize(final double width, final double height) {
        forceLayoutUpdate(container);
        assertTrue(box.getWidth() == width && box.getHeight() == height);
    }

    /**
     * Asserts that the box's coordinates matches the given x and y parameter
     *
     * @param x coordinate that the box should have
     * @param y coordinate that the box should have
     */
    private void assertPosition(final double x, final double y) {
        assertTrue(box.getLayoutX() == x && box.getLayoutY() == y);
    }
}
