package de.tesis.dynaware.grapheditor.core.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
        for (final Iterator<Entry<N, EventHandler<T>>> iter = pEventHandlers.entrySet().iterator(); iter.hasNext();)
        {
            final Entry<N, EventHandler<T>> next = iter.next();
            final N node = next.getKey();
            final EventHandler<T> eventHandler = next.getValue();
            if (node != null && eventHandler != null)
            {
                node.removeEventHandler(pType, eventHandler);
            }
            iter.remove();
        }
    }

}
