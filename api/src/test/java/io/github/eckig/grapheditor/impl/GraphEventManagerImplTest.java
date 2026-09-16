package io.github.eckig.grapheditor.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import io.github.eckig.grapheditor.utils.GraphInputGesture;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Tests the gesture activation matrix of {@link GraphEventManagerImpl}.
 */
public class GraphEventManagerImplTest {

    private GraphEventManagerImpl manager;
    private Object owner;

    @Before
    public void setUp() {
        manager = new GraphEventManagerImpl();
        owner = new Object();
    }

    private static MouseEvent mouseEvent(final MouseButton button, final boolean primaryDown,
            final boolean middleDown, final boolean secondaryDown) {

        return new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, button, 1, false, false, false, false,
                primaryDown, middleDown, secondaryDown, false, false, false, null);
    }

    private static MouseEvent primaryDown() {
        return mouseEvent(MouseButton.PRIMARY, true, false, false);
    }

    private static MouseEvent middleDown() {
        return mouseEvent(MouseButton.MIDDLE, false, true, false);
    }

    private static MouseEvent secondaryDown() {
        return mouseEvent(MouseButton.SECONDARY, false, false, true);
    }

    @Test
    public void primaryButtonActivatesEveryGesture() {
        for (final GraphInputGesture gesture : GraphInputGesture.values()) {
            setUp();
            assertTrue(gesture.name(), manager.activateGesture(gesture, primaryDown(), owner));
        }
    }

    @Test
    public void secondaryButtonActivatesNoGesture() {
        for (final GraphInputGesture gesture : GraphInputGesture.values()) {
            setUp();
            assertFalse("the secondary button must stay free for third party handlers: " + gesture,
                    manager.activateGesture(gesture, secondaryDown(), owner));
        }
    }

    @Test
    public void middleButtonActivatesPan() {
        assertTrue(manager.activateGesture(GraphInputGesture.PAN, middleDown(), owner));
    }

    @Test
    public void middleButtonActivatesNothingButPan() {
        for (final GraphInputGesture gesture : GraphInputGesture.values()) {
            if (gesture == GraphInputGesture.PAN) {
                continue;
            }
            setUp();
            assertFalse(gesture.name(), manager.activateGesture(gesture, middleDown(), owner));
        }
    }

    @Test
    public void panCannotStartWhileAnotherGestureIsActive() {
        assertTrue(manager.activateGesture(GraphInputGesture.MOVE, primaryDown(), owner));
        assertFalse(manager.activateGesture(GraphInputGesture.PAN, middleDown(), new Object()));
    }

    @Test
    public void selectCannotStartWhilePanIsActive() {
        assertTrue(manager.activateGesture(GraphInputGesture.PAN, middleDown(), owner));
        assertFalse(manager.activateGesture(GraphInputGesture.SELECT, primaryDown(), new Object()));
    }

    @Test
    public void panCanBeFinishedAndRestarted() {
        assertTrue(manager.activateGesture(GraphInputGesture.PAN, middleDown(), owner));
        assertTrue(manager.finishGesture(GraphInputGesture.PAN, owner));
        assertTrue(manager.activateGesture(GraphInputGesture.SELECT, primaryDown(), new Object()));
    }

    @Test
    public void ongoingPanGestureKeepsBeingActivatable() {
        assertTrue(manager.activateGesture(GraphInputGesture.PAN, middleDown(), owner));
        // subsequent drag events of the same gesture must keep succeeding
        assertTrue(manager.activateGesture(GraphInputGesture.PAN, middleDown(), owner));
    }
}
