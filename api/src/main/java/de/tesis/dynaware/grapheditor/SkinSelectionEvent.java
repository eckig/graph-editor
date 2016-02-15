package de.tesis.dynaware.grapheditor;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Event that is fired whenever the selected state of a {@link GSkin} has
 * changed.
 * 
 * @author eckig
 */
public class SkinSelectionEvent extends Event {

    private static final long serialVersionUID = 6363597170378541487L;

    public static final EventType<SkinSelectionEvent> SKIN_SELECTION_ANY = new EventType<>("SKIN_SELECTION_ANY");
    public static final EventType<SkinSelectionEvent> SKIN_SELECTED = new EventType<>(SKIN_SELECTION_ANY, "GSKIN_SELECTED");
    public static final EventType<SkinSelectionEvent> SKIN_DESELECTED = new EventType<>(SKIN_SELECTION_ANY, "SKIN_DESELECTED");

    private final GSkin skin;

    /**
     * Constructor
     * 
     * @param eventType
     *            {@link EventType}
     * @param skin
     *            {@link GSkin}
     */
    public SkinSelectionEvent(EventType<SkinSelectionEvent> eventType, final GSkin skin) {
        super(eventType);
        this.skin = skin;
    }

    /**
     * @return the changed {@link GSkin}
     */
    public GSkin getSkin() {
        return skin;
    }
}
