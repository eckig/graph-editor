/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.view;

import java.util.Collection;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;


/**
 * Responsible for telling connection skins to draw themselves.
 */
public interface ConnectionLayouter
{

    /**
     * Initializes the connection layout manager for the given model.
     *
     * @param pModel
     *            the {@link GModel} currently being edited
     */
    void initialize(final GModel pModel);

    /**
     * Draws all connections according to the latest layout values.
     */
    void draw();
}
