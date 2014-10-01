/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.utils;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.model.GModel;

/**
 * All messages that are logged by the graph editor.
 */
public final class LogMessages {

    /**
     * The message to be shown when a new {@link GModel} instance is set in the graph editor.
     */
    public static final String SETTING_NEW_MODEL = "Setting new model instance #{}.";

    /**
     * The message to be shown when the {@link GModel} instance contains negative width / height values.
     */
    public static final String MODEL_INVALID = "Model #{} contains negative width / height values.";

    /**
     * The message to be shown when the controller is reinitializing a new {@link GModel} state.
     */
    public static final String REINITIALIZING_MODEL = "Reinitializing for new model state.";

    /**
     * The message to be shown when a new {@link Resource} is created for the model being edited.
     */
    public static final String CREATING_RESOURCE = "Resource for model #{} not found, creating one.";

    /**
     * The message to be shown when a new {@link EditingDomain} is created for the model being edited.
     */
    public static final String CREATING_EDITING_DOMAIN = "Editing domain for #{} not found, creating one.";

    /**
     * The message to be shown when the skin factory fails to instantiate a custom skin via reflection.
     */
    public static final String CANNOT_INSTANTIATE_SKIN = "Cannot instantiate custom skin: {}";

    /**
     * The message to be shown when the validator factory fails to instantiate a custom validator via reflection.
     */
    public static final String CANNOT_INSTANTIATE_VALIDATOR = "Cannot instantiate custom validator: {}";

    /**
     * The message to be shown when a new connection is created.
     */
    public static final String CREATING_CONNECTION = "Creating connection between connectors #{} and #{}.";

    /**
     * The message to be shown when a new connection is created.
     */
    public static final String REMOVING_CONNECTION = "Removing connection between connectors #{} and #{}.";

    /**
     * The message to be shown when any layout values in the model are updated.
     */
    public static final String UPDATING_LAYOUT = "Updating model layout values.";

    /**
     * The message to be shown when a node is removed.
     */
    public static final String REMOVING_NODE = "Removing node #{}.";

    /**
     * The message to be shown when multiple nodes are removed.
     */
    public static final String REMOVING_NODES = "Removing {} nodes.";

    /**
     * The message to be shown when a connection's joint list is updated.
     */
    public static final String UPDATING_JOINTS = "Updating joint list in connection #{}.";

    /**
     * The message to be shown when the joint cleaner removes joints.
     */
    public static final String REMOVING_JOINTS = "Removing {} joints from connection #{}.";

    /**
     * Shown when the default node skin is told to add a connector with a type it does not support.
     */
    public static final String UNSUPPORTED_CONNECTOR = "Type {} not recognized, default node skin supports 'input' and 'output'.";

    /**
     * Shown when the default connection skin is told to show a connection with the wrong number of joints.
     */
    public static final String UNSUPPORTED_JOINT_COUNT = "Default connection skin requires an even number of joints, with a minimum of 2.";
}