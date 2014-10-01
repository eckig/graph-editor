/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import javafx.beans.binding.DoubleBinding;

/**
 * An extension of double binding that floors the double.
 */
public class FloorBinding extends DoubleBinding {

    private final DoubleBinding binding;

    /**
     * Creates a new {@link FloorBinding} for the given double binding.
     *
     * @param binding a {@link DoubleBinding} to floor
     */
    public FloorBinding(final DoubleBinding binding) {
        this.binding = binding;
        bind(binding);
    }

    @Override
    protected double computeValue() {
        return Math.floor(binding.getValue());
    }
}