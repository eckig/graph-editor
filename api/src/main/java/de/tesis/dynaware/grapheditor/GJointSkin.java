/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

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
public abstract class GJointSkin extends GSkin<GJoint> {

    private final DraggableBox root = new DraggableBox()
    {

        @Override
        public final void positionMoved()
        {
            super.positionMoved();
            GJointSkin.this.impl_positionMoved();
        }
    };

    /**
     * Creates a new {@link GJointSkin}.
     *
     * @param joint the {@link GJoint} represented by the skin
     */
    public GJointSkin(final GJoint joint) {
        super(joint);
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return a {@link DraggableBox} containing the skin's root JavaFX node
     */
    @Override
    public DraggableBox getRoot() {
        return root;
    }

    /**
     * Initializes the joint skin.
     *
     * <p>
     * The skin's layout values are loaded from the {@link GJoint} at this point.
     * </p>
     */
    public void initialize() {
        getRoot().setLayoutX(getItem().getX() - getWidth() / 2);
        getRoot().setLayoutY(getItem().getY() - getHeight() / 2);
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
