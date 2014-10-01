/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.validators;

import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;

/**
 * Manages all validator instances.
 */
public class ValidatorManager {

    private final ValidatorFactory validatorFactory;
    private GConnectorValidator connectorValidator;

    /**
     * Creates a new validator manager instance. Only one instance should exist per {@link DefaultGraphEditor} instance.
     */
    public ValidatorManager() {
        this(new ValidatorFactory());
    }

    /**
     * Package-private constructor used only to inject mocks in unit tests.
     * 
     * @param skinFactory a mock {@link ValidatorFactory} instance
     */
    ValidatorManager(final ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
        connectorValidator = validatorFactory.createConnectorValidator();
    }

    /**
     * Sets the custom connector validator class
     * 
     * @param validatorClass a custom validator class that implements {@link GConnectorValidator}
     */
    public void setConnectorValidator(final Class<? extends GConnectorValidator> validatorClass) {
        validatorFactory.setConnectorValidator(validatorClass);
        connectorValidator = validatorFactory.createConnectorValidator();
    }

    /**
     * Gets the connector validator instance.
     * 
     * <p>
     * This will either be the default connector validator, or a custom connector validator if a custom validator class
     * has previously been set.
     * </p>
     * 
     * @return a {@link GConnectorValidator} used to specify what connectors can be connected to each other
     */
    public GConnectorValidator getConnectorValidator() {
        return connectorValidator;
    }
}
