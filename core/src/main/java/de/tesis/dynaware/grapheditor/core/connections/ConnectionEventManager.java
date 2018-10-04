package de.tesis.dynaware.grapheditor.core.connections;

import java.util.function.Function;

import org.eclipse.emf.common.command.Command;

import de.tesis.dynaware.grapheditor.model.GConnection;

/**
 * Stores any connection created / removed handlers that are set.
 */
public class ConnectionEventManager {

    private Function<GConnection, Command> connectionCreatedHandler;
    private Function<GConnection, Command> connectionRemovedHandler;

    /**
     * Sets the handler to be called when connections are created.
     *
     * @param connectionCreatedHandler the handler to be called when connections are created
     */
    public void setOnConnectionCreated(final Function<GConnection, Command> connectionCreatedHandler)
    {
        this.connectionCreatedHandler = connectionCreatedHandler;
    }

    /**
     * Sets the handler to be called when connections are removed.
     *
     * @param connectionRemovedHandler the handler to be called when connections are removed
     */
    public void setOnConnectionRemoved(final Function<GConnection, Command> connectionRemovedHandler)
    {
        this.connectionRemovedHandler = connectionRemovedHandler;
    }

    /**
     * Calls the connection-created handler (if it exists) after a connection was created.
     *
     * @param connection the connection that was created
     * @param command the compound command that created it
     */
    public Command notifyConnectionAdded(final GConnection connection)
    {
        return connectionCreatedHandler == null ? null : connectionCreatedHandler.apply(connection);
    }

    /**
     * Calls the connection-removed handler (if it exists) after a connection was removed.
     *
     * @param connection the connection that was removed
     * @param command the compound command that removed it
     */
    public Command notifyConnectionRemoved(final GConnection connection)
    {
        return connectionRemovedHandler == null ? null : connectionRemovedHandler.apply(connection);
    }
}
