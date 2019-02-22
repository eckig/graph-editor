package de.tesis.dynaware.grapheditor.utils;

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
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     *
     * @param pGesture
     *            {@link GraphInputGesture} to check
     * @param pEvent
     *            {@link Event}
     * @param pOwner
     *            owner
     * @return {@code true} if the given gesture was activated otherwise
     *         {@code false}
     */
    boolean activateGesture(final GraphInputGesture pGesture, final Event pEvent, final Object pOwner);

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     *
     * @param pExpected
     *            the expected gesture that should be finished
     * @param pOwner
     *            owner
     * @return {@code true} if the state changed as a result of this operation
     *         or {@code false}
     */
    boolean finishGesture(final GraphInputGesture pExpected, final Object pOwner);
}
