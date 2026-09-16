package io.github.eckig.grapheditor.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.utils.JavaFXThreadingRule;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Tests for defects in {@link io.github.eckig.grapheditor.window.skin.PanningWindowScrollPaneSkin}.
 */
public class PanningWindowScrollPaneSkinTest {

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double VIEWPORT = 200;

    /** deliberately NOT square, so that a horizontal/vertical mix-up is visible */
    private static final double CONTENT_WIDTH = 1000;
    private static final double CONTENT_HEIGHT = 4000;

    private static class TestPanningWindow extends PanningWindow {
        void setTestContent(final Region content) { setContent(content); }
    }

    private TestPanningWindow window;
    private Region content;
    private Stage stage;

    @Before
    public void setUp() {

        window = new TestPanningWindow();
        // pin the size, otherwise the window grows to the size of its content
        window.setMinSize(VIEWPORT, VIEWPORT);
        window.setPrefSize(VIEWPORT, VIEWPORT);
        window.setMaxSize(VIEWPORT, VIEWPORT);
        window.resize(VIEWPORT, VIEWPORT);

        content = new Pane();
        content.setPrefSize(CONTENT_WIDTH, CONTENT_HEIGHT);
        content.setMinSize(CONTENT_WIDTH, CONTENT_HEIGHT);
        content.resize(CONTENT_WIDTH, CONTENT_HEIGHT);
        window.setTestContent(content);

        stage = new Stage();
        stage.setScene(new Scene(new Pane(window), VIEWPORT, VIEWPORT));
        stage.show();

        window.resize(VIEWPORT, VIEWPORT);
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
    }

    private static MouseEvent mouse(final javafx.event.EventType<MouseEvent> type, final double x, final double y,
            final boolean middleDown) {
        return new MouseEvent(type, x, y, x, y, MouseButton.MIDDLE, 1, false, false, false, false, false, middleDown,
                false, false, false, false, null);
    }

    private Point2D panBy(final double dx, final double dy) {
        window.scrollTo(new Point2D(0.5, 0.5));
        final Point2D before = window.getScrollPosition();
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_PRESSED, 0, 0, true));
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_DRAGGED, -dx, -dy, true));
        Event.fireEvent(content, mouse(MouseEvent.MOUSE_RELEASED, -dx, -dy, false));
        return window.getScrollPosition().subtract(before);
    }

    private ScrollPane scrollPane() {
        for (final javafx.scene.Node n : window.getChildrenUnmodifiable()) {
            if (n instanceof ScrollPane sp) { return sp; }
        }
        throw new IllegalStateException("no ScrollPane");
    }

    // ------------------------------------------------- 1) axis-correct panning

    @Test
    public void verticalPanningUsesTheVerticalOverflow() {

        final double drag = 50;
        final double viewport = scrollPane().getViewportBounds().getWidth();

        final Point2D horizontal = panBy(drag, 0);
        final Point2D vertical = panBy(0, drag);

        // expected scrollbar delta == drag / (contentSize - viewport)
        final double expectedX = drag / (CONTENT_WIDTH - viewport);
        final double expectedY = drag / (CONTENT_HEIGHT - viewport);

        assertEquals("horizontal panning must scale with the content width", expectedX, horizontal.getX(), 1e-6);
        assertEquals("vertical panning must scale with the content HEIGHT, not the width", expectedY,
                vertical.getY(), 1e-6);
        assertTrue("taller content must pan slower per pixel than wider content",
                Math.abs(vertical.getY()) < Math.abs(horizontal.getX()));
    }

    // ------------------------------------------------------- 2) key handling

    /**
     * @return the key codes that still reach the parent of the ScrollPane, i.e. the
     *         events the skin did NOT consume
     */
    private boolean reachesParent(final KeyEvent pEvent) {
        final boolean[] seen = new boolean[1];
        final javafx.event.EventHandler<KeyEvent> spy = _ -> seen[0] = true;
        final javafx.scene.Parent root = stage.getScene().getRoot();
        root.addEventHandler(KeyEvent.KEY_PRESSED, spy);
        try {
            // the runtime delivers key events to the focus owner; the canvas is the
            // focusable element, so that is where they arrive
            Event.fireEvent(window, pEvent);
        } finally {
            root.removeEventHandler(KeyEvent.KEY_PRESSED, spy);
        }
        return seen[0];
    }

    private static KeyEvent key(final KeyCode code, final boolean control) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, control, false, false);
    }

    @Test
    public void unhandledKeysAreNotConsumed() {
        assertTrue("the skin must not swallow keys it does not handle",
                reachesParent(key(KeyCode.A, false)));
    }

    @Test
    public void modifiedNavigationKeysAreLeftToTheApplication() {

        scrollPane().setVvalue(0);

        assertTrue("Ctrl+DOWN belongs to the application", reachesParent(key(KeyCode.DOWN, true)));
        assertEquals("Ctrl+DOWN must not scroll", 0.0, scrollPane().getVvalue(), 1e-9);
    }

    @Test
    public void plainNavigationKeysStillScrollAndAreConsumed() {

        scrollPane().setVvalue(0);

        assertFalse("a handled key must be consumed", reachesParent(key(KeyCode.DOWN, false)));
        assertTrue("DOWN must still scroll the viewport", scrollPane().getVvalue() > 0);
    }

    // ------------------------------------------------------------- 3) zoom

    @Test
    public void fractionalScrollDeltaZoomsIn() {

        final double before = window.getZoom();

        // trackpads report fractional deltas - this used to zoom OUT
        Event.fireEvent(content, new ScrollEvent(ScrollEvent.SCROLL, 0, 0, 0, 0, false, true, false, false, true,
                false, 0, 0.5, 0, 0.5, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null));

        assertTrue("a positive scroll delta must zoom in, got " + window.getZoom(), window.getZoom() > before);
    }

    // ------------------------------------------------- keyboard reachability

    @Test
    public void arrowKeysPanWhenTheCanvasHasFocus() {

        scrollPane().setVvalue(0);

        // this is the real situation: the user clicked the canvas, so the
        // PanningWindow owns the focus
        Event.fireEvent(content, new MouseEvent(MouseEvent.MOUSE_PRESSED, 10, 10, 10, 10, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
        assertEquals("precondition: the canvas owns the focus", window, stage.getScene().getFocusOwner());

        // and the runtime delivers key events to the focus owner
        Event.fireEvent(stage.getScene().getFocusOwner(), key(KeyCode.DOWN, false));

        assertTrue("DOWN must pan the view when the canvas has focus", scrollPane().getVvalue() > 0);
    }

    @Test
    public void arrowKeysAreMirroredInRightToLeftOrientation() {

        scrollPane().setHvalue(0.5);
        final double ltrAfterLeft = pressAndGetHvalue(KeyCode.LEFT);

        window.setNodeOrientation(javafx.geometry.NodeOrientation.RIGHT_TO_LEFT);
        stage.getScene().getRoot().layout();

        scrollPane().setHvalue(0.5);
        final double rtlAfterLeft = pressAndGetHvalue(KeyCode.LEFT);

        assertTrue("LEFT must decrease hvalue in LTR, was " + ltrAfterLeft, ltrAfterLeft < 0.5);
        assertTrue("LEFT must increase hvalue in RTL, was " + rtlAfterLeft, rtlAfterLeft > 0.5);
    }

    private double pressAndGetHvalue(final KeyCode code) {
        Event.fireEvent(window, key(code, false));
        return scrollPane().getHvalue();
    }

    // ------------------------------------------- 5) degenerate resize maths

    /** resizes the content and returns the resulting vertical scrollbar value */
    private double resizeContentHeightTo(final double pHeight) {
        content.setMinSize(CONTENT_WIDTH, pHeight);
        content.setPrefSize(CONTENT_WIDTH, pHeight);
        content.setMaxSize(CONTENT_WIDTH, pHeight);
        content.resize(CONTENT_WIDTH, pHeight);
        content.autosize();
        stage.getScene().getRoot().layout();
        return scrollPane().getVvalue();
    }

    @Test
    public void contentGrowingToViewportSizeDoesNotJumpToTheEnd() {

        // the degenerate division only happens when the inset is 0
        scrollPane().setStyle("-fx-padding: 0; -fx-border-width: 0; -fx-background-insets: 0;");
        scrollPane().applyCss();
        stage.getScene().getRoot().layout();

        // content SMALLER than the viewport, but scrolled away from the start
        resizeContentHeightTo(100);
        window.scrollToY(0.5);
        stage.getScene().getRoot().layout();

        // grow it to exactly the viewport size -> projected new position becomes 0
        final double viewportHeight = scrollPane().getViewportBounds().getHeight();
        final double value = resizeContentHeightTo(viewportHeight);

        assertTrue("scrollbar value must stay finite, was " + value, Double.isFinite(value));
        assertTrue("the viewport must not jump to the very end, was " + value, value < 1.0);
    }

    @Test
    public void contentShrinkingToViewportSizeDoesNotJumpToTheEnd() {

        window.scrollTo(new Point2D(0.5, 0.5));
        stage.getScene().getRoot().layout();

        // the content ends up exactly as large as the viewport, which makes the
        // projected new position 0 -> used to divide by zero and jump to the end
        final double viewportHeight = scrollPane().getViewportBounds().getHeight();
        final double value = resizeContentHeightTo(viewportHeight);

        assertTrue("scrollbar value must stay a finite number, was " + value, Double.isFinite(value));
        assertTrue("value must stay within range, was " + value, value >= 0.0 && value <= 1.0);
        assertTrue("the viewport must not jump to the very end, was " + value, value < 1.0);
    }

    @Test
    public void repeatedContentResizesNeverProduceNaNOrInfinity() {

        window.scrollTo(new Point2D(0.5, 0.5));
        stage.getScene().getRoot().layout();

        for (final double height : new double[] { 4000, 200, 183.5, 1, 4000, 0.5, 2500 }) {
            final double value = resizeContentHeightTo(height);
            assertTrue("height " + height + " produced " + value, Double.isFinite(value));
            assertTrue("height " + height + " produced " + value, value >= 0.0 && value <= 1.0);
        }
    }

    // ----------------------------------------------------------- 4) dispose

    @Test
    public void disposeUnregistersTheKeyHandler() {

        final ScrollPane sp = scrollPane();
        sp.setVvalue(0);

        // precondition: the skin handles DOWN
        assertFalse(reachesParent(key(KeyCode.DOWN, false)));

        sp.getSkin().dispose();

        assertTrue("a disposed skin must no longer handle or consume key events",
                reachesParent(key(KeyCode.DOWN, false)));
    }

    // ------------------------------------------------- cursor anchored zoom

    private static ScrollEvent ctrlScroll(final double sceneX, final double sceneY, final double deltaY) {
        return new ScrollEvent(ScrollEvent.SCROLL, sceneX, sceneY, sceneX, sceneY, false, true, false, false, true,
                false, 0, deltaY, 0, deltaY, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null);
    }

    @Test
    public void zoomKeepsThePointUnderTheCursorInPlace() {

        window.scrollTo(new Point2D(0.4, 0.4));
        stage.getScene().getRoot().layout();

        // a point well inside the viewport, not the origin
        final Point2D cursorInScene = content.localToScene(0, 0).add(60, 70);

        final Point2D contentPointBefore = content.sceneToLocal(cursorInScene);
        final double zoomBefore = window.getZoom();

        Event.fireEvent(content, ctrlScroll(cursorInScene.getX(), cursorInScene.getY(), 1));
        stage.getScene().getRoot().layout();

        assertTrue("precondition: the zoom must actually have changed", window.getZoom() != zoomBefore);

        final Point2D contentPointAfter = content.sceneToLocal(cursorInScene);

        assertEquals("the content must not drift horizontally under the cursor",
                contentPointBefore.getX(), contentPointAfter.getX(), 2.0);
        assertEquals("the content must not drift vertically under the cursor",
                contentPointBefore.getY(), contentPointAfter.getY(), 2.0);
    }

    private void scrollZoom(final int notches, final double deltaY) {
        final Point2D cursor = content.localToScene(0, 0).add(60, 70);
        for (int i = 0; i < notches; i++) {
            Event.fireEvent(content, ctrlScroll(cursor.getX(), cursor.getY(), deltaY));
        }
        stage.getScene().getRoot().layout();
    }

    @Test
    public void zoomStepsAreMultiplicativeAndReversible() {

        assertEquals("precondition", 1.0, window.getZoom(), 1e-9);

        scrollZoom(3, 1);
        final double zoomedIn = window.getZoom();
        assertTrue("zooming in must increase the zoom", zoomedIn > 1.0);

        scrollZoom(3, -1);
        assertEquals("zooming in and out again must land back on the original level", 1.0, window.getZoom(), 1e-9);
    }

    @Test
    public void zoomIsReversibleAwayFromTheSnapToOneBand() {

        // the snap-to-100% band would mask rounding drift near 1.0, so move well
        // below it first
        scrollZoom(6, -1);
        final double base = window.getZoom();
        assertTrue("precondition: outside the snap band, was " + base, base < 0.9);

        scrollZoom(4, 1);
        scrollZoom(4, -1);

        assertEquals("zoom must be reversible at every level", base, window.getZoom(), 1e-9);
    }

    @Test
    public void zoomStepsHaveAConstantRatio() {

        scrollZoom(1, 1);
        final double first = window.getZoom();
        scrollZoom(1, 1);
        final double second = window.getZoom();

        // an additive step would give a shrinking ratio as the zoom grows
        assertEquals("every notch must scale by the same factor", first / 1.0, second / first, 1e-3);
    }

    @Test
    public void zoomIsClampedToTheConfiguredBand() {

        scrollZoom(40, 1);
        assertEquals("must not zoom in beyond the maximum", 2.0, window.getZoom(), 1e-9);

        scrollZoom(60, -1);
        assertEquals("must not zoom out beyond the minimum", 0.25, window.getZoom(), 1e-9);
    }

    // --------------------------------------------------- 6) property exposure

    @Test
    public void panModeArmedPropertyIsStableAndReadOnly() {
        assertEquals("repeated calls must return the same cached instance", window.panModeArmedProperty(),
                window.panModeArmedProperty());
        assertFalse(window.panModeArmedProperty() instanceof javafx.beans.property.BooleanProperty);
    }
}
