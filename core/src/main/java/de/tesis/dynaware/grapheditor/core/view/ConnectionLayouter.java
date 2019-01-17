/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.view;

import java.util.Collection;

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
     * Redraw the given connection according to the latest layout values.
     *
     * @param pConnection
     *            connection to redraw
     */
    void redraw(final GConnection pConnection);

    /**
     * Redraw the given connections according to the latest layout values.
     *
     * @param pConnections
     *            connections to redraw
     */
    void redraw(final Collection<GConnection> pConnections);
}
