/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * An interface for customising connector validation.
 *
 * <p>
 * A custom connector validator must implement this interface. It <b>must</b> also provide a no-argument constructor.
 * </p>
 */
public interface GConnectorValidator {

    /**
     * Pre-validate check made during drag-over events.
     *
     * <p>
     * If the pre-validate check fails, the dragged connector will not interact with the dragged-over connector at all.
     * </p>
     *
     * @param source the {@link GConnector} that was dragged
     * @param target the {@link GConnector} that was dragged-over
     *
     * @return {@code true} if a validate check should be made, {@code false} if not
     */
    boolean prevalidate(GConnector source, GConnector target);

    /**
     * Validate check made during drag-over events. Only made if the pre-validate check passes.
     *
     * @param source the {@link GConnector} that was dragged
     * @param target the {@link GConnector} that was dragged-over
     *
     * @return {@code true} if connection is allowed, {@code false} if it is forbidden
     */
    boolean validate(GConnector source, GConnector target);

    /**
     * Creates the 'type' string to be used in a new connection.
     *
     * <p>
     * If both prevalidate and validate checks pass, a new connection will be created of the type returned by this
     * method.
     * </p>
     *
     * @param source the {@link GConnector} that was dragged
     * @param target the {@link GConnector} that was dragged-over
     *
     * @return a {@link String} specifying the type for the new connection
     */
    String createConnectionType(GConnector source, GConnector target);

    /**
     * Creates the 'type' string to be used in the joints inside a new connection.
     *
     * @param source the {@link GConnector} that was dragged
     * @param target the {@link GConnector} that was dragged-over
     *
     * @return a {@link String} specifying the type for the new connection
     */
    String createJointType(GConnector source, GConnector target);
}
