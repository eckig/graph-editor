/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Provides a static validation method to check a {@link GModel} instance for errors.
 */
public class ModelSanityChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGraphEditor.class);

    /**
     * Static class, private constructor.
     */
    private ModelSanityChecker() {
    }

    /**
     * Validates the given {@link GModel}.
     *
     * @param model the {@link GModel} to be validated
     * @return {@code true} if the model is valid
     */
    public static boolean validate(final GModel model) {

        boolean valid = true;

        valid = validateSizes(model);
        valid = valid && validateReferences(model);

        return valid;
    }

    /**
     * Performs a basic sanity check that width and height parameters are non-negative.
     * 
     * @param model the {@link GModel} to be validated
     * @return {@code true} if the model width and height parameters are valid
     */
    private static boolean validateSizes(final GModel model) {

        if (model.getContentWidth() < 0 || model.getContentHeight() < 0) {
            LOGGER.error(LogMessages.MODEL_SIZES_INVALID);
            return false;
        }

        for (final GNode node : model.getNodes()) {
            if (node.getWidth() < 0 || node.getHeight() < 0) {
                LOGGER.error(LogMessages.MODEL_SIZES_INVALID);
                return false;
            }
        }

        return true;
    }

    /**
     * Validates that the references between connectors and their connections make sense.
     * 
     * @param model the {@link GModel} to be validated
     * @return {@code true} if the model references are valid
     */
    private static boolean validateReferences(final GModel model) {

        boolean valid = true;

        for (final GConnection connection : model.getConnections()) {

            final GConnector source = connection.getSource();
            final GConnector target = connection.getTarget();

            if (source == null || target == null) {
                LOGGER.error(LogMessages.CONNECTOR_MISSING);
                valid = false;
            } else if (source != null && !source.getConnections().contains(connection)) {
                LOGGER.error(LogMessages.CONNECTION_REFERENCE_MISSING);
                valid = false;
            } else if (target != null && !target.getConnections().contains(connection)) {
                LOGGER.error(LogMessages.CONNECTION_REFERENCE_MISSING);
                valid = false;
            }
        }

        return valid;
    }
}
