package de.tesis.dynaware.grapheditor;

import org.eclipse.emf.common.command.CompoundCommand;

/**
 * Used to append additional actions to EMF compound-commands that are executed by the graph editor.
 */
@FunctionalInterface
public interface CommandAppender<T> {

    /**
     * Appends commands to a recently-executed compound command.
     * 
     * @param object the graph object that was modified as a result of the command
     * @param command the {@link CompoundCommand} that was executed
     */
    public void append(T object, CompoundCommand command);
}
