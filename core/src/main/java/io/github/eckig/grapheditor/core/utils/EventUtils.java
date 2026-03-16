package io.github.eckig.grapheditor.core.utils;

import java.util.Map;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;


/**
 * Helper methods for JavaFX event handling
 */
public class EventUtils
{

    public static <N extends Node, T extends Event> void removeEventHandlers(final Map<N, EventHandler<T>> pEventHandlers,
            final EventType<T> pType)
    {
        for (final var iter = pEventHandlers.entrySet().iterator(); iter.hasNext();)
        {
            final var next = iter.next();
            final var node = next.getKey();
            final var eventHandler = next.getValue();
            if (node != null && eventHandler != null)
            {
                node.removeEventHandler(pType, eventHandler);
            }
            iter.remove();
        }
    }

}
