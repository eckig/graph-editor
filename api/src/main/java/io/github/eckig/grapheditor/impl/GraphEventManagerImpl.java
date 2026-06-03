package io.github.eckig.grapheditor.impl;

import io.github.eckig.grapheditor.utils.GraphEventManager;
import io.github.eckig.grapheditor.utils.GraphInputGesture;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;


/**
 * Default implementation of {@link GraphEventManager}
 */
public class GraphEventManagerImpl implements GraphEventManager
{

    private GraphInputGesture gesture;
    private Object owner;

    @Override
    public boolean activateGesture(final GraphInputGesture pGesture, final Event pEvent, final Object pOwner)
    {
        if (!canOverwrite(owner, pOwner))
        {
            return false;
        }
        if (canActivate(pGesture, pEvent))
        {
            gesture = pGesture;
            owner = pOwner;
            return true;
        }
        // ELSE:
        return false;
    }

    private boolean canActivate(final GraphInputGesture pGesture, final Event pEvent)
    {
        final GraphInputGesture current = gesture;
        if (current == pGesture)
        {
            return true;
        }
        else if (current == null)
        {
            final boolean isTouch = pEvent instanceof TouchEvent || pEvent instanceof MouseEvent me && me.isSynthesized()
                    || pEvent instanceof ScrollEvent se && se.getTouchCount() > 0;
            if (!isTouch)
            {
                return pEvent instanceof MouseEvent me && me.isPrimaryButtonDown();
            }
            else
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean finishGesture(final GraphInputGesture pExpected, final Object pOwner)
    {
        if (gesture == pExpected && (owner == pOwner || !isVisible(owner)))
        {
            gesture = null;
            owner = null;
            return true;
        }
        return false;
    }

    private static boolean canOverwrite(final Object pExisting, final Object pCandidate)
    {
        if (pExisting == pCandidate)
        {
            return true;
        }
        if (pCandidate == null)
        {
            return false;
        }
        return pExisting == null || !isVisible(pExisting);
    }

    private static boolean isVisible(final Object pNode)
    {
        if (pNode != null)
        {
            if (pNode instanceof Node n)
            {
                return n.isVisible() && n.getParent() != null && n.getScene() != null;
            }
            return true;
        }
        // ELSE: null
        return false;
    }
}
