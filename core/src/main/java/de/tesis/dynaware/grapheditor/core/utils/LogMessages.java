/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.utils;

import de.tesis.dynaware.grapheditor.model.GModel;

/**
 * All messages that are logged by the graph editor.
 */
public final class LogMessages {

    /**
     * The message to be shown when the {@link GModel} instance contains negative width / height values.
     */
    public static final String MODEL_INVALID = "Model #{} contains negative width / height values.";

    /**
     * The message to be shown when the skin factory fails to instantiate a custom skin via reflection.
     */
    public static final String CANNOT_INSTANTIATE_SKIN = "Cannot instantiate custom skin: {}";

    /**
     * The message to be shown when the validator factory fails to instantiate a custom validator via reflection.
     */
    public static final String CANNOT_INSTANTIATE_VALIDATOR = "Cannot instantiate custom validator: {}";

    /**
     * Shown when a default node or connector skin encounters a connector with a type it does not support.
     */
    public static final String UNSUPPORTED_CONNECTOR = "Connector type '{}' not recognized, setting to 'left-input'.";

    /**
     * Shown when the default connection skin is told to show a connection with the wrong number of joints.
     */
    public static final String UNSUPPORTED_JOINT_COUNT = "Joint count not compatible with source and target connector types.";

    /**
     * Shown when the default connection skin is told to show a connection without a source and target connector.
     */
    public static final String CONNECTOR_MISSING = "Connection must have non-null source and target connectors.";
}