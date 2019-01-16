/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;


/**
 * Responsible for telling connection skins to draw themselves.
 */
public interface ConnectionLayouter
{

    /**
     * Initializes the connection layout manager for the given model.
     *
     * @param model
     *            the {@link GModel} currently being edited
     */
    void initialize(final GModel pModel);

    /**
     * Redraws all connections according to the latest layout values.
     */
    void redrawAll();

    /**
     * Redraws all dirty connections according to the latest layout values.
     */
    void redrawDirty();

    /**
     * Mark a connection as dirty
     * 
     * @param pConnection
     *            {@link GConnection} to flag
     */
    void markDirty(final GConnection pConnection);
}
