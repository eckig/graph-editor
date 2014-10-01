/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Provides a static validation method to validate a {@link GModel}.
 */
public class ModelValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGraphEditor.class);

    /**
     * Static class, private constructor.
     */
    private ModelValidator() {
    }

    /**
     * Validates the given {@link GModel}.
     *
     * <p>
     * Currently only performs a basic sanity check that width and height parameters are non-negative.
     * </p>
     *
     * @param model the {@link GModel} to be validated
     * @return {@code true} if the model is valid
     */
    public static boolean validate(final GModel model) {

        if (model.getContentWidth() < 0 || model.getContentHeight() < 0) {
            logError(model);
            return false;
        }

        for (final GNode node : model.getNodes()) {
            if (node.getWidth() < 0 || node.getHeight() < 0) {
                logError(model);
                return false;
            }
        }

        return true;
    }

    /**
     * Logs an error that the model is invalid.
     *
     * @param model the {@link GModel} that is invalid
     */
    private static void logError(final GModel model) {
        LOGGER.error(LogMessages.MODEL_INVALID, model.hashCode());
    }
}
