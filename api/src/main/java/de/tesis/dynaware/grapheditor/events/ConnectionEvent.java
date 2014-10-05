package de.tesis.dynaware.grapheditor.events;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.model.GConnection;

/**
 * Contains information about a connection that was added or removed in the graph editor.
 */
public class ConnectionEvent {

    final GConnection connection;
    final CompoundCommand command;

    /**
     * Creates a new {@link ConnectionEvent} instance.
     * 
     * @param connection the {@link GConnection} that was created / removed
     * @param command the {@link CompoundCommand} that did the creation / removal
     */
    public ConnectionEvent(final GConnection connection, final CompoundCommand command) {

        this.connection = connection;
        this.command = command;
    }

    /**
     * Gets the connection that was created or removed.
     * 
     * @return the connection that was created / removed
     */
    public GConnection getConnection() {
        return connection;
    }

    /**
     * Gets the compound command responsible for the creation or removal of a connection.
     * 
     * <p>
     * Append any additional commands to this one if you would like them to be part of the same undo-redo step.
     * </p>
     * 
     * @return the compound command responsible for the connection creation / removal
     */
    public CompoundCommand getCommand() {
        return command;
    }
}
