/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.utils;

/**
 * All messages that are logged by the graph editor.
 */
public final class LogMessages {

    public static final String MODEL_SIZES_INVALID = "Model contains negative width / height values.";
    public static final String CANNOT_INSTANTIATE_SKIN = "Cannot instantiate custom skin: {}";
    public static final String CANNOT_INSTANTIATE_VALIDATOR = "Cannot instantiate custom validator: {}";
    public static final String UNSUPPORTED_CONNECTOR = "Connector type '{}' not recognized, setting to 'left-input'.";
    public static final String UNSUPPORTED_JOINT_COUNT = "Joint count not compatible with source and target connector types.";
    public static final String CONNECTOR_MISSING = "Connection must have non-null source and target connectors.";
    public static final String CONNECTION_REFERENCE_MISSING = "A connector is missing a reference to its connection.";
}