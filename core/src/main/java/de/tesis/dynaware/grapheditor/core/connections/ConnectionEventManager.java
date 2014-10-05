package de.tesis.dynaware.grapheditor.core.connections;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.events.ConnectionEvent;
import de.tesis.dynaware.grapheditor.events.ConnectionEventHandler;
import de.tesis.dynaware.grapheditor.model.GConnection;

/**
 * Stores any connection created / removed handlers that are set.
 */
public class ConnectionEventManager {

    private ConnectionEventHandler connectionCreatedHandler;
    private ConnectionEventHandler connectionRemovedHandler;

    /**
     * Sets the handler to be notified when connections are created.
     * 
     * @param connectionCreatedHandler the handler to be notified when connections are created
     */
    public void setOnConnectionCreated(final ConnectionEventHandler connectionCreatedHandler) {
        this.connectionCreatedHandler = connectionCreatedHandler;
    }

    /**
     * Sets the handler to be notified when connections are removed.
     * 
     * @param connectionRemovedHandler the handler to be notified when connections are removed
     */
    public void setOnConnectionRemoved(final ConnectionEventHandler connectionRemovedHandler) {
        this.connectionRemovedHandler = connectionRemovedHandler;
    }

    /**
     * Notifies the connection-created handler (if it exists) that a connection was created.
     * 
     * @param connection the connection that was created
     * @param command the compound command that created it
     */
    public void notifyConnectionAdded(final GConnection connection, final CompoundCommand command) {

        if (connectionCreatedHandler != null) {
            final ConnectionEvent event = new ConnectionEvent(connection, command);
            connectionCreatedHandler.handle(event);
        }
    }

    /**
     * Notifies the connection-removed handler (if it exists) that a connection was removed.
     * 
     * @param connection the connection that was removed
     * @param command the compound command that removed it
     */
    public void notifyConnectionRemoved(final GConnection connection, final CompoundCommand command) {

        if (connectionRemovedHandler != null) {
            final ConnectionEvent event = new ConnectionEvent(connection, command);
            connectionRemovedHandler.handle(event);
        }
    }
}
