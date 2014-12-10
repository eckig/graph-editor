/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.utils;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * General properties for the graph editor.
 *
 * <p>
 * For example, should the editor have 'bounds', or should objects be draggable outside the editor area?
 * </p>
 *
 * <p>
 * If a bound is <b>active</b>, objects that are dragged or resized in the editor should stop when they hit the edge,
 * and the editor region will not try to grow in size. Otherwise it will grow up to its max size.
 * </p>
 *
 * <p>
 * Also stores properties for whether the grid is visible and/or snap-to-grid is on.
 * </p>
 */
public class GraphEditorProperties {

    // The default max size of the editor region, set on startup.
    public static final double DEFAULT_MAX_WIDTH = Double.MAX_VALUE;
    public static final double DEFAULT_MAX_HEIGHT = Double.MAX_VALUE;

    private static final double DEFAULT_BOUND_VALUE = 15;
    private static final double DEFAULT_GRID_SPACING = 12;

    // Not currently configurable.
    private final boolean northBoundActive = true;
    private final boolean westBoundActive = true;

    // On by default.
    private boolean southBoundActive = true;
    private boolean eastBoundActive = true;

    // The distance from the editor edge at which the objects should stop when dragged / resized.
    private double northBoundValue = DEFAULT_BOUND_VALUE;
    private double southBoundValue = DEFAULT_BOUND_VALUE;
    private double eastBoundValue = DEFAULT_BOUND_VALUE;
    private double westBoundValue = DEFAULT_BOUND_VALUE;

    // Off by default.
    private final BooleanProperty gridVisibleProperty = new SimpleBooleanProperty();
    private final BooleanProperty snapToGridProperty = new SimpleBooleanProperty();
    private final DoubleProperty gridSpacingProperty = new SimpleDoubleProperty(DEFAULT_GRID_SPACING);

    private final Map<String, String> customProperties = new HashMap<>();

    /**
     * Creates a new editor properties instance containing a set of default properties.
     */
    public GraphEditorProperties() {
    }

    /**
     * Copy constructor.
     *
     * <p>
     * Creates a new editor properties instance with all values copied over from an existing instance.
     * </p>
     *
     * @param editorProperties an existing {@link GraphEditorProperties} instance
     */
    public GraphEditorProperties(final GraphEditorProperties editorProperties) {

        southBoundActive = editorProperties.isSouthBoundActive();
        eastBoundActive = editorProperties.isSouthBoundActive();

        northBoundValue = editorProperties.getNorthBoundValue();
        southBoundValue = editorProperties.getSouthBoundValue();
        eastBoundValue = editorProperties.getEastBoundValue();
        westBoundValue = editorProperties.getWestBoundValue();

        gridVisibleProperty.set(editorProperties.isGridVisible());
        snapToGridProperty.set(editorProperties.isSnapToGridOn());
        gridSpacingProperty.set(editorProperties.getGridSpacing());
    }

    /**
     * Gets whether the north bound is active.
     *
     * @return {@code true} if the north bound is active, false if not
     */
    public boolean isNorthBoundActive() {
        return northBoundActive;
    }

    /**
     * Gets whether the south bound is active.
     *
     * @return {@code true} if the south bound is active, false if not
     */
    public boolean isSouthBoundActive() {
        return southBoundActive;
    }

    /**
     * Sets whether the south bound is active.
     *
     * @param southBound {@code true} if the south bound is active, false if not
     */
    public void setSouthBound(final boolean southBound) {
        southBoundActive = southBound;
    }

    /**
     * Gets whether the east bound is active.
     *
     * @return {@code true} if the east bound is active, false if not
     */
    public boolean isEastBoundActive() {
        return eastBoundActive;
    }

    /**
     * Sets whether the east bound is active.
     *
     * @param eastBound {@code true} if the east bound is active, false if not
     */
    public void setEastBound(final boolean eastBound) {
        eastBoundActive = eastBound;
    }

    /**
     * Gets whether the west bound is active.
     *
     * @return {@code true} if the west bound is active, false if not
     */
    public boolean isWestBoundActive() {
        return westBoundActive;
    }

    /**
     * Gets the value of the north bound.
     *
     * @return the value of the north bound
     */
    public double getNorthBoundValue() {
        return northBoundValue;
    }

    /**
     * Sets the value of the north bound.
     *
     * @param northBoundValue the value of the north bound
     */
    public void setNorthBoundValue(final double northBoundValue) {
        this.northBoundValue = northBoundValue;
    }

    /**
     * Gets the value of the south bound.
     *
     * @return the value of the south bound
     */
    public double getSouthBoundValue() {
        return southBoundValue;
    }

    /**
     * Sets the value of the south bound.
     *
     * @param southBoundValue the value of the south bound
     */
    public void setSouthBoundValue(final double southBoundValue) {
        this.southBoundValue = southBoundValue;
    }

    /**
     * Gets the value of the east bound.
     *
     * @return the value of the east bound
     */
    public double getEastBoundValue() {
        return eastBoundValue;
    }

    /**
     * Sets the value of the east bound.
     *
     * @param eastBoundValue the value of the east bound
     */
    public void setEastBoundValue(final double eastBoundValue) {
        this.eastBoundValue = eastBoundValue;
    }

    /**
     * Gets the value of the west bound.
     *
     * @return the value of the west bound
     */
    public double getWestBoundValue() {
        return westBoundValue;
    }

    /**
     * Sets the value of the west bound.
     *
     * @param westBoundValue the value of the west bound
     */
    public void setWestBoundValue(final double westBoundValue) {
        this.westBoundValue = westBoundValue;
    }

    /**
     * Checks if the background grid is visible.
     *
     * @return {@code true} if the background grid is visible, {@code false} if not
     */
    public boolean isGridVisible() {
        return gridVisibleProperty.get();
    }

    /**
     * Sets whether the background grid should be visible or not.
     *
     * @param gridVisible {@code true} if the background grid should be visible, {@code false} if not
     */
    public void setGridVisible(final boolean gridVisible) {
        gridVisibleProperty.set(gridVisible);
    }

    /**
     * Gets the grid-visible property.
     *
     * @return a {@link BooleanProperty} tracking whether the grid is visible or not
     */
    public BooleanProperty gridVisibleProperty() {
        return gridVisibleProperty;
    }

    /**
     * Checks if snap-to-grid is on.
     *
     * @return {@code true} if snap-to-grid is on, {@code false} if not
     */
    public boolean isSnapToGridOn() {
        return snapToGridProperty.get();
    }

    /**
     * Sets whether snap-to-grid should be on.
     *
     * @param snapToGrid {@code true} if snap-to-grid should be on, {@code false} if not
     */
    public void setSnapToGrid(final boolean snapToGrid) {
        snapToGridProperty.set(snapToGrid);
    }

    /**
     * Gets the snap-to-grid property.
     *
     * @return a {@link BooleanProperty} tracking whether snap-to-grid is on or off
     */
    public BooleanProperty snapToGridProperty() {
        return snapToGridProperty;
    }

    /**
     * Gets the current grid spacing in pixels.
     *
     * @return the current grid spacing
     */
    public double getGridSpacing() {
        return gridSpacingProperty.get();
    }

    /**
     * Sets the grid spacing to be used if the grid is visible and/or snap-to-grid is enabled.
     *
     * <p>
     * Integer values are recommended to avoid sub-pixel positioning effects.
     * </p>
     *
     * @param gridSpacing the grid spacing to be used
     */
    public void setGridSpacing(final double gridSpacing) {
        gridSpacingProperty.set(gridSpacing);
    }

    /**
     * Gets the grid spacing property.
     *
     * @return the grid spacing {@link DoubleProperty}.
     */
    public DoubleProperty gridSpacingProperty() {
        return gridSpacingProperty;
    }

    /**
     * Additional properties that may be added and referred to in custom skin implementations.
     *
     * @return a map of custom properties
     */
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }
}
