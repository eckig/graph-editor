/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.utils;

import javafx.geometry.Side;

/**
 * This class defines 8 connector types. The connectors can be:
 * 
 * <ol>
 * <li>Either <b>input</b> or <b>output</b></li>
 * <li>Either <b>top</b>, <b>right</b>, <b>bottom</b>, or <b>left</b></li>
 * </ol>
 * 
 * For example <b>left-input</b> defines a connector on the <em>left</em> side of a node, with a triangle point facing
 * <em>into</em> the node.
 */
public class DefaultConnectorTypes {

    /**
     * Type string for an input connector positioned at the top of a node.
     */
    public static final String TOP_INPUT = "top-input";

    /**
     * Type string for an output connector positioned at the top of a node.
     */
    public static final String TOP_OUTPUT = "top-output";

    /**
     * Type string for an input connector positioned on the right side of a node.
     */
    public static final String RIGHT_INPUT = "right-input";

    /**
     * Type string for an output connector positioned on the right side of a node.
     */
    public static final String RIGHT_OUTPUT = "right-output";

    /**
     * Type string for an input connector positioned at the bottom of a node.
     */
    public static final String BOTTOM_INPUT = "bottom-input";

    /**
     * Type string for an output connector positioned at the bottom of a node.
     */
    public static final String BOTTOM_OUTPUT = "bottom-output";

    /**
     * Type string for an input connector positioned on the left side of a node.
     */
    public static final String LEFT_INPUT = "left-input";

    /**
     * Type string for an output connector positioned on the left side of a node.
     */
    public static final String LEFT_OUTPUT = "left-output";

    /**
     * Returns true if the type is one of the 8 types defined in this class.
     * 
     * @param type a connector's type string
     * @return {@code true} if the type is one of the 8 types defined in this class
     */
    public static boolean isValid(final String type) {
        return type != null && (isTop(type) || isRight(type) || isBottom(type) || isLeft(type));
    }

    /**
     * Gets the side corresponding to the given connector type.
     * 
     * @param type one of the 8 valid connector types
     * @return the {@link Side} the connector type is on
     */
    public static Side getSide(final String type) {

        if (isTop(type)) {
            return Side.TOP;
        } else if (isRight(type)) {
            return Side.RIGHT;
        } else if (isBottom(type)) {
            return Side.BOTTOM;
        } else {
            return Side.LEFT;
        }
    }

    /**
     * Returns true if the type corresponds to a connector positioned at the top of a node.
     * 
     * @param type a connector's type string
     * @return {@code true} if the connector will be positioned at the top of a node
     */
    public static boolean isTop(final String type) {
        return type.equals(TOP_INPUT) || type.equals(TOP_OUTPUT);
    }

    /**
     * Returns true if the type corresponds to a connector positioned on the right side of a node.
     * 
     * @param type a connector's type string
     * @return {@code true} if the connector will be positioned on the right side of a node
     */
    public static boolean isRight(final String type) {
        return type.equals(RIGHT_INPUT) || type.equals(RIGHT_OUTPUT);
    }

    /**
     * Returns true if the type corresponds to a connector positioned at the bottom of a node.
     * 
     * @param type a connector's type string
     * @return {@code true} if the connector will be positioned at the bottom of a node
     */
    public static boolean isBottom(final String type) {
        return type.equals(BOTTOM_INPUT) || type.equals(BOTTOM_OUTPUT);
    }

    /**
     * Returns true if the type corresponds to a connector positioned on the left side of a node.
     * 
     * @param type a connector's type string
     * @return {@code true} if the connector will be positioned on the left side of a node
     */
    public static boolean isLeft(final String type) {
        return type.equals(LEFT_INPUT) || type.equals(LEFT_OUTPUT);
    }

    /**
     * Returns true if the type corresponds to an input connector.
     * 
     * @param type a connector's type string
     * @return {@code true} if the connector is any kind of input
     */
    public static boolean isInput(final String type) {

        final boolean leftOrRight = type.equals(LEFT_INPUT) || type.equals(RIGHT_INPUT);
        final boolean topOrBottom = type.equals(TOP_INPUT) || type.equals(BOTTOM_INPUT);

        return leftOrRight || topOrBottom;
    }
}
