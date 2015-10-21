package de.tesis.dynaware.grapheditor.core.utils;

import java.util.Iterator;
import java.util.Map;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

/**
 * 
 * Helper methods for JavaFX event handling
 *
 */
public class EventUtils {

	public static <T extends Event> void removeEventHandlers(final Map<Node, EventHandler<T>> eventHandlers, final EventType<T> type) {
        for(final Iterator<Map.Entry<Node, EventHandler<T>>> iter = eventHandlers.entrySet().iterator(); iter.hasNext();) {
            final Map.Entry<Node, EventHandler<T>> next = iter.next();
            next.getKey().removeEventHandler(type, next.getValue());
            iter.remove();
        }
    }
	
}
