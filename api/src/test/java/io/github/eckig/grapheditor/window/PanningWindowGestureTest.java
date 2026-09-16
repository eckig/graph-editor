package io.github.eckig.grapheditor.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.utils.JavaFXThreadingRule;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Tests the input gestures of the {@link PanningWindow}:
 * <ul>
 * <li>middle-drag pans</li>
 * <li>{@code SPACE} + primary-drag pans</li>
 * <li>a plain primary-drag does not pan (it is reserved for rubber band selection)</li>
 * <li>the secondary button is neither used nor consumed</li>
 * </ul>
 */
public class PanningWindowGestureTest {

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double WINDOW_SIZE = 200;
    private static final double CONTENT_SIZE = 2000;

    /** the drag has to exceed PAN_THRESHOLD to have any effect */
    private static final double DRAG_DISTANCE = 100;

    /** {@link PanningWindow#setContent(Region)} is protected */
    private static class TestPanningWindow extends PanningWindow {

        void setTestContent(final Region content) {
            setContent(content);
        }
    }

    private TestPanningWindow window;
    private Region content;
    private TextField textField;
    private javafx.scene.control.Button button;
    private Stage stage;

    @Before
    public void setUp() {

        window = new TestPanningWindow();
        window.resize(WINDOW_SIZE, WINDOW_SIZE);

        content = new Pane();
        content.setPrefSize(CONTENT_SIZE, CONTENT_SIZE);
        content.setMinSize(CONTENT_SIZE, CONTENT_SIZE);
        content.resize(CONTENT_SIZE, CONTENT_SIZE);
        window.setTestContent(content);

        textField = new TextField();
        button = new javafx.scene.control.Button("unrelated");

        final Pane root = new Pane(window, textField, button);

        stage = new Stage();
        stage.setScene(new Scene(root, WINDOW_SIZE, WINDOW_SIZE + 30));
        stage.show();

        layout();
    }

    private void layout() {
        window.resize(WINDOW_SIZE, WINDOW_SIZE);
        window.applyCss();
        window.layout();
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    // ---------------------------------------------------------------- helpers

    private static MouseEvent mouse(final javafx.event.EventType<MouseEvent> type, final double x, final double y,
            final MouseButton button, final boolean primaryDown, final boolean middleDown,
            final boolean secondaryDown) {

        return new MouseEvent(type, x, y, x, y, button, 1, false, false, false, false, primaryDown, middleDown,
                secondaryDown, false, false, false, null);
    }

    /**
     * Performs a full press-drag-release gesture on the window content.
     *
     * @return the scroll position delta caused by the gesture
     */
    private Point2D drag(final MouseButton button) {

        final boolean primary = button == MouseButton.PRIMARY;
        final boolean middle = button == MouseButton.MIDDLE;
        final boolean secondary = button == MouseButton.SECONDARY;

        final Point2D before = window.getScrollPosition();

        Event.fireEvent(content, mouse(MouseEvent.MOUSE_PRESSED, 0, 0, button, primary, middle, secondary));
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_DRAGGED, -DRAG_DISTANCE, -DRAG_DISTANCE, button, primary,
                middle, secondary));
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_RELEASED, -DRAG_DISTANCE, -DRAG_DISTANCE, button, false, false,
                false));

        return window.getScrollPosition().subtract(before);
    }

    private void pressSpace() {
        Event.fireEvent(stage.getScene(),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));
    }

    private void releaseSpace() {
        Event.fireEvent(stage.getScene(),
                new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.SPACE, false, false, false, false));
    }

    // ------------------------------------------------------- keyboard focus

    @Test
    public void pressingTheCanvasClaimsTheKeyboardFocus() {

        // without a focus owner the scene never receives key events at all,
        // so SPACE could never arm pan mode
        assertEquals("precondition: nothing is focused", null, stage.getScene().getFocusOwner());

        Event.fireEvent(content, mouse(MouseEvent.MOUSE_PRESSED, 10, 10, MouseButton.PRIMARY, true, false, false));

        assertEquals("pressing the canvas must give the graph editor the keyboard focus", window,
                stage.getScene().getFocusOwner());
    }

    @Test
    public void pressingTheCanvasDoesNotStealFocusFromAChildOfTheWindow() {

        content.setFocusTraversable(true);
        content.requestFocus();
        assertEquals(content, stage.getScene().getFocusOwner());

        Event.fireEvent(content, mouse(MouseEvent.MOUSE_PRESSED, 10, 10, MouseButton.PRIMARY, true, false, false));

        assertEquals("focus inside the window must be left alone", content, stage.getScene().getFocusOwner());
    }

    @Test
    public void spaceArmsPanModeAfterTheCanvasWasPressed() {

        Event.fireEvent(content, mouse(MouseEvent.MOUSE_PRESSED, 10, 10, MouseButton.PRIMARY, true, false, false));
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_RELEASED, 10, 10, MouseButton.PRIMARY, false, false, false));

        // deliver the key event the way the JavaFX runtime does: to the focus owner
        Event.fireEvent(stage.getScene().getFocusOwner(),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));

        assertTrue("SPACE must arm pan mode after the user interacted with the canvas",
                window.isPanModeArmed());
    }

    // ------------------------------------------------------------- pan mode

    @Test
    public void spaceArmsAndDisarmsPanMode() {

        assertFalse(window.isPanModeArmed());

        pressSpace();
        assertTrue("SPACE should arm pan mode", window.isPanModeArmed());

        releaseSpace();
        assertFalse("releasing SPACE should disarm pan mode", window.isPanModeArmed());
    }

    @Test
    public void spaceArmsPanModeWhenFocusIsOnAnUnrelatedControl() {

        // this is the normal situation in a real application: some toolbar control
        // outside the graph editor owns the focus
        button.requestFocus();

        pressSpace();
        assertTrue("SPACE must arm pan mode even when the graph editor does not own the focus",
                window.isPanModeArmed());
    }

    @Test
    public void spaceDoesNotClickAFocusedButtonWhileItArmsPanMode() {

        final int[] fired = new int[1];
        button.setOnAction(_ -> fired[0]++);
        button.requestFocus();

        pressSpace();

        assertTrue(window.isPanModeArmed());
        assertEquals("SPACE is the pan modifier, it must not also press the focused button", 0, fired[0]);
    }

    @Test
    public void spaceArmsPanModeWhenNothingIsFocused() {

        assertEquals("precondition: no focus owner", null, stage.getScene().getFocusOwner());

        pressSpace();
        assertTrue("SPACE must arm pan mode when nothing owns the focus", window.isPanModeArmed());
    }

    @Test
    public void spaceIsIgnoredWhileTextFieldIsFocused() {

        textField.requestFocus();
        assertEquals("precondition: the text field must own the focus", textField, stage.getScene().getFocusOwner());

        pressSpace();
        assertFalse("SPACE must keep its regular meaning while typing", window.isPanModeArmed());
    }

    @Test
    public void panModeIsDisarmedWhenTheWindowLosesFocus() {

        pressSpace();
        assertTrue(window.isPanModeArmed());

        // the KEY_RELEASED event is lost when the window loses focus (e.g. alt-tab)
        stage.hide();

        assertFalse("pan mode must not stay armed after the window lost focus", window.isPanModeArmed());
    }

    // ------------------------------------------------------------- panning

    @Test
    public void middleDragPans() {
        final Point2D delta = drag(MouseButton.MIDDLE);
        assertNotEquals("middle-drag should pan horizontally", 0.0, delta.getX(), 1e-9);
        assertNotEquals("middle-drag should pan vertically", 0.0, delta.getY(), 1e-9);
    }

    @Test
    public void primaryDragDoesNotPan() {
        final Point2D delta = drag(MouseButton.PRIMARY);
        assertEquals("a plain primary-drag is reserved for rubber band selection", 0.0, delta.getX(), 1e-9);
        assertEquals("a plain primary-drag is reserved for rubber band selection", 0.0, delta.getY(), 1e-9);
    }

    @Test
    public void spacePlusPrimaryDragPans() {

        pressSpace();

        final Point2D delta = drag(MouseButton.PRIMARY);
        assertNotEquals("SPACE + primary-drag should pan horizontally", 0.0, delta.getX(), 1e-9);
        assertNotEquals("SPACE + primary-drag should pan vertically", 0.0, delta.getY(), 1e-9);
    }

    @Test
    public void secondaryDragDoesNotPan() {
        final Point2D delta = drag(MouseButton.SECONDARY);
        assertEquals("the secondary button must not pan", 0.0, delta.getX(), 1e-9);
        assertEquals("the secondary button must not pan", 0.0, delta.getY(), 1e-9);
    }

    // ---------------------------------------------- secondary button freedom

    @Test
    public void secondaryEventsAreNotConsumedByTheLibrary() {

        final int[] received = new int[3];
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, _ -> received[0]++);
        content.addEventHandler(MouseEvent.MOUSE_DRAGGED, _ -> received[1]++);
        content.addEventHandler(MouseEvent.MOUSE_RELEASED, _ -> received[2]++);

        drag(MouseButton.SECONDARY);

        assertEquals("a third party must still receive the secondary press", 1, received[0]);
        assertEquals("a third party must still receive the secondary drag", 1, received[1]);
        assertEquals("a third party must still receive the secondary release", 1, received[2]);
    }

    @Test
    public void spacePlusPrimaryPressIsConsumedSoNoSelectionStarts() {

        final int[] pressesSeenByContent = new int[1];
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, _ -> pressesSeenByContent[0]++);

        pressSpace();
        Event.fireEvent(content,
                mouse(MouseEvent.MOUSE_PRESSED, 0, 0, MouseButton.PRIMARY, true, false, false));

        assertEquals("the press must be consumed before it can start a rubber band selection", 0,
                pressesSeenByContent[0]);
    }

    @Test
    public void plainPrimaryPressReachesTheContentSoSelectionCanStart() {

        final int[] pressesSeenByContent = new int[1];
        content.addEventHandler(MouseEvent.MOUSE_PRESSED, _ -> pressesSeenByContent[0]++);

        Event.fireEvent(content,
                mouse(MouseEvent.MOUSE_PRESSED, 0, 0, MouseButton.PRIMARY, true, false, false));

        assertEquals("a plain primary press must reach the content to start a rubber band selection", 1,
                pressesSeenByContent[0]);
    }
}
