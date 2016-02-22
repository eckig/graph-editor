/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import de.tesis.dynaware.grapheditor.model.GModel;

/**
 * Responsible for telling connection skins to draw themselves.
 */
public interface ConnectionLayouter {

    /**
     * Initializes the connection layout manager for the given model.
     *
     * @param model
     *            the {@link GModel} currently being edited
     */
    void initialize(final GModel model);

    /**
     * Redraws all connections according to the latest layout values.
     */
    void redraw();
}