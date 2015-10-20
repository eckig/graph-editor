/*
 * Copyright (C) 2005 - 2015 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;

/**
 * Abstract class that all skins inherit from. Contains logic common to all skins.
 */
public abstract class GSkin {

    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false){

        @Override
        protected void invalidated() {
            super.invalidated();
            selectionChanged(get());
        }
        
    };
    
    private GraphEditor graphEditor;

    /**
     * Sets the graph editor instance that this skin is a part of.
     *
     * @param graphEditor a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    /**
     * Gets the graph editor instance that this skin is a part of.
     *
     * <p>
     * This is provided for advanced skin customisation purposes only. Use at your own risk.
     * </p>
     *
     * @return the {@link GraphEditor} instance that this skin is a part of
     */
    public GraphEditor getGraphEditor() {
        return graphEditor;
    }

    /**
     * Gets whether the skin is selected or not.
     *
     * @return {@code true} if the skin is selected, {@code false} if not
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    /**
     * Sets whether the skin is selected or not.
     *
     * @param isSelected {@code true} if the skin is selected, {@code false} if not
     */
    public void setSelected(final boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * The property that determines whether the skin is selected or not.
     *
     * @return a {@link BooleanProperty} containing {@code true} if the skin is selected, {@code false} if not
     */
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }
    
    /**
     * Is called whenever the selection state has changed.
     *
     * @param isSelected {@code true} if the skin is selected, {@code false} if not
     */
    protected abstract void selectionChanged(final boolean isSelected);

    /**
     * Called after the skin is removed. Can be overridden for cleanup.
     */
    public void dispose() {
        // No default implementation.
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return a the skin's root JavaFX {@link Node}
     */
    public abstract Node getRoot();
}
