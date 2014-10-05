package de.tesis.dynaware.grapheditor.events;

/**
 * Handles the events that are fired during creation and removal of connections in the graph editor.
 */
@FunctionalInterface
public interface ConnectionEventHandler {

    /**
     * Handles the creation or removal of a connection.
     * 
     * @param event contains information about the connection that was created / removed
     */
    public void handle(ConnectionEvent event);
}
