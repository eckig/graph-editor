package de.tesis.dynaware.grapheditor.utils;

import javafx.beans.property.ObjectProperty;
import javafx.event.Event;


/**
 * <p>
 * Helper class managing the various different gestures in the graph editor to
 * prevent overlapping of different gestures (e.g. a user should not be able to
 * resize and zoom at the same time).
 * </p>
 *
 * <p>
 * For non touch devices this is straightforward by checking if any other
 * gesture is currently active before activating a new one.
 * </p>
 */
public interface GraphEventManager
{

    /**
     * @return currently active {@link GraphInputGesture}
     */
    GraphInputGesture getInputGesture();

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     *
     * @param pInputMode
     *            new {@link GraphInputGesture}
     */
    void activateInputGesture(final GraphInputGesture pInputMode);

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     *
     * @param pExpected
     *            the expected gesture that should be finished
     * @return {@code true} if the state changed as a result of this operation
     *         or {@code false}
     */
    boolean finishInputGesture(final GraphInputGesture pExpected);

    /**
     * @return {@link ObjectProperty} controlling the current
     *         {@link GraphInputGesture}
     */
    ObjectProperty<GraphInputGesture> inputGestureProperty();

    /**
     * @param pGesture
     *            {@link GraphInputGesture} to check
     * @param pEvent
     *            {@link Event}
     * @return {@code true} if the given gesture can be activated or
     *         {@code false}
     * @since 28.11.2018
     */
    boolean canActivate(final GraphInputGesture pGesture, final Event pEvent);
}
