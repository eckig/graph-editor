/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;

/**
 * The skin class for a {@link GJoint}. Responsible for visualizing joints in the graph editor.
 *
 * <p>
 * A custom joint skin must extend this class. It <b>must</b> also provide a constructor taking exactly one
 * {@link GJoint} parameter.
 * </p>
 *
 * <p>
 * The root JavaFX node of this skin is a {@link DraggableBox}.
 * </p>
 */
public abstract class GJointSkin {

    private final GJoint joint;

    private final DraggableBox root = new DraggableBox();

    private GraphEditor graphEditor;

    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

    /**
     * Creates a new {@link GJointSkin}.
     *
     * @param joint the {@link GJoint} represented by the skin
     */
    public GJointSkin(final GJoint joint) {
        this.joint = joint;
    }

    /**
     * Gets the joint model element represented by the skin.
     *
     * @return the {@link GJoint} represented by the skin
     */
    public GJoint getJoint() {
        return joint;
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return a {@link DraggableBox} containing the skin's root JavaFX node
     */
    public DraggableBox getRoot() {
        return root;
    }

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
     * Gets whether the joint is selected or not.
     *
     * @return {@code true} if the joint is selected, {@code false} if not
     */
    public boolean isSelected() {
        return selectedProperty.get();
    }

    /**
     * Sets whether the joint is selected or not.
     *
     * @param isSelected {@code true} if the joint is selected, {@code false} if not
     */
    public void setSelected(final boolean isSelected) {
        selectedProperty.set(isSelected);
    }

    /**
     * The property storing whether the joint is selected or not.
     *
     * @return a {@link BooleanProperty} containing {@code true} if the joint is selected, {@code false} if not
     */
    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    /**
     * Initializes the joint skin.
     *
     * <p>
     * The skin's layout values are loaded from the {@link GJoint} at this point.
     * </p>
     */
    public void initialize() {

        getRoot().setLayoutX(getJoint().getX() - getWidth() / 2);
        getRoot().setLayoutY(getJoint().getY() - getHeight() / 2);
    }

    /**
     * Gets the width of the joint.
     *
     * <p>
     * For robust behaviour this should return the correct width at all times. Note that getRoot().getWidth() does not
     * meet this condition as it will return 0 until the joint's node is added to the scene graph and layed out.
     * </p>
     *
     * @return the width of the joint
     */
    public abstract double getWidth();

    /**
     * Gets the height of the joint.
     *
     * <p>
     * For robust behaviour this should return the correct height at all times. Note that getRoot().getHeight() does not
     * meet this condition as it will return 0 until the joint's node is added to the scene graph and layed out.
     * </p>
     *
     * @return the height of the joint
     */
    public abstract double getHeight();
}
