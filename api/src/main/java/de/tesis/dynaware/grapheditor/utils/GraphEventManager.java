package de.tesis.dynaware.grapheditor.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;


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
 * <p>
 * For touch enabled devices we additionally have to check the
 * {@link GraphInputMode} because moving elements around and moving the viewport
 * around are performed with the same touch gesture.
 * </p>
 */
public class GraphEventManager
{

    private final ObjectProperty<GraphInputMode> inputMode = new ObjectPropertyBase<GraphInputMode>(GraphInputMode.SELECTION)
    {

        @Override
        public Object getBean()
        {
            return GraphEventManager.this;
        }

        @Override
        public String getName()
        {
            return "inputMode"; //$NON-NLS-1$
        }

        @Override
        public void set(GraphInputMode newValue)
        {
            super.set(newValue == null ? GraphInputMode.SELECTION : newValue);
        }

        @Override
        public void setValue(GraphInputMode newValue)
        {
            set(newValue);
        }
    };

    private final ObjectProperty<GraphInputGesture> gesture = new ObjectPropertyBase<GraphInputGesture>()
    {

        @Override
        public Object getBean()
        {
            return GraphEventManager.this;
        }

        @Override
        public String getName()
        {
            return "inputGesture"; //$NON-NLS-1$
        }
    };

    /**
     * @return currently active {@link GraphInputMode}
     */
    public GraphInputMode getInputMode()
    {
        return inputMode.get();
    }

    /**
     * @param pInputMode
     *            new {@link GraphInputMode}
     */
    public void setInputMode(final GraphInputMode pInputMode)
    {
        inputMode.set(pInputMode);
    }

    /**
     * @return {@link ObjectProperty} controlling the current
     *         {@link GraphInputMode}
     */
    public ObjectProperty<GraphInputMode> inputModeProperty()
    {
        return inputMode;
    }

    /**
     * @return currently active {@link GraphInputGesture}
     */
    public GraphInputGesture getInputGesture()
    {
        return gesture.get();
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     *
     * @param pInputMode
     *            new {@link GraphInputGesture}
     */
    public void activateInputGesture(final GraphInputGesture pInputMode)
    {
        gesture.set(pInputMode);
    }

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
    public boolean finishInputGesture(final GraphInputGesture pExpected)
    {
        if (getInputGesture() == pExpected)
        {
            gesture.set(null);
            return true;
        }
        return false;
    }

    /**
     * @return {@link ObjectProperty} controlling the current
     *         {@link GraphInputGesture}
     */
    public ObjectProperty<GraphInputGesture> inputGestureProperty()
    {
        return gesture;
    }

    /**
     * @param pGesture
     *            {@link GraphInputGesture} to check
     * @param pEvent
     *            {@link Event}
     * @return {@code true} if the given gesture can be activated or
     *         {@code false}
     * @since 28.11.2018
     */
    public boolean canActivate(final GraphInputGesture pGesture, final Event pEvent)
    {
        final GraphInputGesture current = getInputGesture();
        final GraphInputMode mode = getInputMode();
        if (current == pGesture)
        {
            return true;
        }
        else if (current == null)
        {
            final boolean isTouch = pEvent instanceof MouseEvent && ((MouseEvent) pEvent).isSynthesized()
                    || pEvent instanceof ScrollEvent && ((ScrollEvent) pEvent).getTouchCount() > 0;
            if (!isTouch)
            {
                switch (pGesture)
                {
                    case PAN:
                        return pEvent instanceof MouseEvent && ((MouseEvent) pEvent).isSecondaryButtonDown();

                    case ZOOM:
                        return pEvent instanceof ScrollEvent;

                    case SELECT:
                    case CONNECT:
                    case MOVE:
                    case RESIZE:
                        return pEvent instanceof MouseEvent && ((MouseEvent) pEvent).isPrimaryButtonDown();
                }
            }
            else
            {
                switch (pGesture)
                {
                    case PAN:
                    case ZOOM:
                        return mode == null || mode == GraphInputMode.NAVIGATION;

                    case SELECT:
                        return mode == null || mode == GraphInputMode.SELECTION;

                    case CONNECT:
                    case MOVE:
                    case RESIZE:
                        return true;
                }
            }
        }
        return false;
    }
}
