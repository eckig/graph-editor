/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults;

import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;


/**
 * The default joint skin.
 *
 * <p>
 * Pretty much just a {@link DraggableBox} with some hover and pressed effects.
 * </p>
 */
public class DefaultJointSkin extends GJointSkin
{

    private static final String STYLE_CLASS = "default-joint";

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private static final double SIZE = 12;
    private static final Point2D SNAP_OFFSET = new Point2D(-5, -5);

    /**
     * Creates a new default join instance.
     *
     * @param joint
     *            the {@link GJoint} the skin is being created for
     */
    public DefaultJointSkin(final GJoint joint)
    {
        super(joint);

        getRoot().resize(SIZE, SIZE);
        getRoot().getStyleClass().setAll(STYLE_CLASS);

        getRoot().setPickOnBounds(false);
        getRoot().setSnapToGridOffset(SNAP_OFFSET);
    }

    @Override
    protected void selectionChanged(boolean isSelected)
    {
        getRoot().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, isSelected);
        if (isSelected)
        {
            getRoot().toFront();
        }
    }

    @Override
    public double getWidth()
    {
        return SIZE;
    }

    @Override
    public double getHeight()
    {
        return SIZE;
    }
}
