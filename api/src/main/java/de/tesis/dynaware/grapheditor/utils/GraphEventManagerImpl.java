package de.tesis.dynaware.grapheditor.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.Event;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;


/**
 * Default implementation of {@link GraphEventManager}
 */
class GraphEventManagerImpl implements GraphEventManager
{

    private final ObjectProperty<GraphInputGesture> gesture = new ObjectPropertyBase<GraphInputGesture>()
    {

        @Override
        public Object getBean()
        {
            return GraphEventManagerImpl.this;
        }

        @Override
        public String getName()
        {
            return "inputGesture"; //$NON-NLS-1$
        }
    };

    @Override
    public GraphInputGesture getInputGesture()
    {
        return gesture.get();
    }

    @Override
    public void activateInputGesture(final GraphInputGesture pInputMode)
    {
        gesture.set(pInputMode);
    }

    @Override
    public boolean finishInputGesture(final GraphInputGesture pExpected)
    {
        if (getInputGesture() == pExpected)
        {
            gesture.set(null);
            return true;
        }
        return false;
    }

    @Override
    public ObjectProperty<GraphInputGesture> inputGestureProperty()
    {
        return gesture;
    }

    @Override
    public boolean canActivate(final GraphInputGesture pGesture, final Event pEvent)
    {
        final GraphInputGesture current = getInputGesture();
        if (current == pGesture)
        {
            return true;
        }
        else if (current == null)
        {
            final boolean isTouch = pEvent instanceof TouchEvent
                    || pEvent instanceof MouseEvent && ((MouseEvent) pEvent).isSynthesized()
                    || pEvent instanceof ScrollEvent && ((ScrollEvent) pEvent).getTouchCount() > 0;
            if (!isTouch)
            {
                switch (pGesture)
                {
                    case PAN:
                        return pEvent instanceof ScrollEvent && !((ScrollEvent) pEvent).isControlDown()
                                || pEvent instanceof MouseEvent && ((MouseEvent) pEvent).isSecondaryButtonDown();

                    case ZOOM:
                        return pEvent instanceof ScrollEvent && ((ScrollEvent) pEvent).isControlDown();

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
                    case ZOOM:
                        return pEvent instanceof ZoomEvent;

                    case PAN:
                        return pEvent instanceof TouchEvent && ((TouchEvent) pEvent).getTouchCount() > 1;

                    case SELECT:
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
