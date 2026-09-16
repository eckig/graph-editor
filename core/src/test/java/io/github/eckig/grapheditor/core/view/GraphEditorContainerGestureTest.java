package io.github.eckig.grapheditor.core.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.utils.JavaFXThreadingRule;
import io.github.eckig.grapheditor.model.GModel;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Verifies that the pan gestures of the {@link GraphEditorContainer} and the
 * rubber band selection of the graph editor do not interfere with each other:
 * <ul>
 * <li>a plain primary-drag on the empty canvas selects</li>
 * <li>{@code SPACE} + primary-drag pans instead of selecting</li>
 * <li>a middle-drag pans and never selects</li>
 * </ul>
 */
public class GraphEditorContainerGestureTest {

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double WINDOW_SIZE = 400;

    private GraphEditor graphEditor;
    private GraphEditorContainer container;
    private Region view;
    private javafx.scene.control.Button toolbarButton;
    private Stage stage;

    @Before
    public void setUp() {

        final GModel model = DummyDataFactory.createModel();

        graphEditor = new DefaultGraphEditor();
        graphEditor.setModel(model);

        container = new GraphEditorContainer();
        container.setGraphEditor(graphEditor);
        container.resize(WINDOW_SIZE, WINDOW_SIZE);

        view = graphEditor.getView();

        toolbarButton = new javafx.scene.control.Button("toolbar");

        stage = new Stage();
        stage.setScene(new Scene(new Pane(container, toolbarButton), WINDOW_SIZE, WINDOW_SIZE));
        stage.show();

        // mirror a real application: an unrelated toolbar control owns the focus
        toolbarButton.requestFocus();

        container.resize(WINDOW_SIZE, WINDOW_SIZE);
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        view.applyCss();
        view.layout();
    }

    // ---------------------------------------------------------------- helpers

    private static MouseEvent mouse(final EventType<MouseEvent> type, final double x, final double y,
            final MouseButton button, final boolean primaryDown, final boolean middleDown) {

        return new MouseEvent(type, x, y, x, y, button, 1, false, false, false, false, primaryDown, middleDown, false,
                false, false, false, null);
    }

    /**
     * Drags across the canvas, covering the area where the dummy model has its nodes.
     *
     * @return the scroll position delta caused by the gesture
     */
    private Point2D dragAcrossCanvas(final MouseButton button) {

        final boolean primary = button == MouseButton.PRIMARY;
        final boolean middle = button == MouseButton.MIDDLE;

        final Point2D before = container.getScrollPosition();

        Event.fireEvent(view, mouse(MouseEvent.MOUSE_PRESSED, 800, 700, button, primary, middle));
        Event.fireEvent(view, mouse(MouseEvent.MOUSE_DRAGGED, 400, 350, button, primary, middle));
        Event.fireEvent(view, mouse(MouseEvent.MOUSE_DRAGGED, 10, 10, button, primary, middle));
        Event.fireEvent(view, mouse(MouseEvent.MOUSE_RELEASED, 10, 10, button, false, false));

        return container.getScrollPosition().subtract(before);
    }

    private void pressSpace() {
        Event.fireEvent(stage.getScene(),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.SPACE, false, false, false, false));
    }

    private int selectionSize() {
        return graphEditor.getSelectionManager().getSelectedItems().size();
    }

    // ----------------------------------------------------------------- tests

    @Test
    public void primaryDragOnEmptyCanvasSelects() {

        assertEquals("precondition: nothing selected", 0, selectionSize());

        final Point2D delta = dragAcrossCanvas(MouseButton.PRIMARY);

        assertTrue("a primary-drag on the empty canvas must rubber band select", selectionSize() > 0);
        assertEquals("a rubber band selection must not pan", 0.0, delta.getX(), 1e-9);
        assertEquals("a rubber band selection must not pan", 0.0, delta.getY(), 1e-9);
    }

    @Test
    public void spacePlusPrimaryDragPansInsteadOfSelecting() {

        assertEquals("precondition: an unrelated control owns the focus", toolbarButton,
                stage.getScene().getFocusOwner());

        pressSpace();
        assertTrue("precondition: pan mode armed", container.isPanModeArmed());

        final Point2D delta = dragAcrossCanvas(MouseButton.PRIMARY);

        assertEquals("SPACE + primary-drag must not select", 0, selectionSize());
        assertNotEquals("SPACE + primary-drag must pan", 0.0, delta.getX(), 1e-9);
        assertNotEquals("SPACE + primary-drag must pan", 0.0, delta.getY(), 1e-9);
    }

    @Test
    public void middleDragPansInsteadOfSelecting() {

        assertFalse("precondition: pan mode not armed", container.isPanModeArmed());

        final Point2D delta = dragAcrossCanvas(MouseButton.MIDDLE);

        assertEquals("middle-drag must not select", 0, selectionSize());
        assertNotEquals("middle-drag must pan", 0.0, delta.getX(), 1e-9);
        assertNotEquals("middle-drag must pan", 0.0, delta.getY(), 1e-9);
    }

    // --------------------------------------------------------- accessibility

    @Test
    public void reportsAnAccessibleRoleAndDescription() {

        assertEquals(javafx.scene.AccessibleRole.SCROLL_PANE, container.getAccessibleRole());
        assertEquals("graph editor canvas",
                container.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.ROLE_DESCRIPTION));
    }

    @Test
    public void reportsTheCurrentModelContentsAsAccessibleText() {

        final Object text = container.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.TEXT);
        final int nodes = graphEditor.getModel().getNodes().size();
        final int connections = graphEditor.getModel().getConnections().size();

        assertTrue("precondition: the dummy model is not empty", nodes > 0);
        assertTrue("expected the node count in " + text, String.valueOf(text).contains(String.valueOf(nodes)));
        assertTrue("expected the connection count in " + text,
                String.valueOf(text).contains(String.valueOf(connections)));
    }

    @Test
    public void accessibleTextIsLocalizable() {

        // the bundle is per editor instance, not global state
        graphEditor.getProperties().setResourceBundle(new java.util.ResourceBundle() {
            @Override
            protected Object handleGetObject(final String key) {
                return "graphEditor.accessibleText".equals(key) ? "Knoten {0}, Kanten {1}" : null;
            }
            @Override
            public java.util.Enumeration<String> getKeys() {
                return java.util.Collections.enumeration(java.util.Set.of("graphEditor.accessibleText"));
            }
        });

        final Object text = container.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.TEXT);
        assertTrue("expected the translated pattern, was " + text, String.valueOf(text).startsWith("Knoten "));

        // a key the custom bundle does not define must fall back to the defaults
        assertEquals("graph editor canvas",
                container.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.ROLE_DESCRIPTION));
    }

    @Test
    public void accessibleTextReportsAnEmptyGraph() {

        graphEditor.setModel(null);
        assertEquals("empty graph",
                container.queryAccessibleAttribute(javafx.scene.AccessibleAttribute.TEXT));
    }

    @Test
    public void secondaryDragNeitherSelectsNorPans() {

        final Point2D before = container.getScrollPosition();

        Event.fireEvent(view, mouse(MouseEvent.MOUSE_PRESSED, 800, 700, MouseButton.SECONDARY, false, false));
        Event.fireEvent(view, mouse(MouseEvent.MOUSE_DRAGGED, 10, 10, MouseButton.SECONDARY, false, false));
        Event.fireEvent(view, mouse(MouseEvent.MOUSE_RELEASED, 10, 10, MouseButton.SECONDARY, false, false));

        final Point2D delta = container.getScrollPosition().subtract(before);

        assertEquals("the secondary button must not select", 0, selectionSize());
        assertEquals("the secondary button must not pan", 0.0, delta.getX(), 1e-9);
        assertEquals("the secondary button must not pan", 0.0, delta.getY(), 1e-9);
    }
}
