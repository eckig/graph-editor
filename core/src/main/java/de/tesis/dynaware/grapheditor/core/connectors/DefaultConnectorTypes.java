/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.connectors;

import javafx.geometry.Side;


/**
 * This class defines 8 connector types. The connectors can be:
 *
 * <ol>
 * <li>Either <b>input</b> or <b>output</b></li>
 * <li>Either <b>top</b>, <b>right</b>, <b>bottom</b>, or <b>left</b></li>
 * </ol>
 *
 * For example <b>left-input</b> defines a connector on the <em>left</em> side
 * of a node, with a triangle point facing <em>into</em> the node.
 */
public final class DefaultConnectorTypes
{

    private DefaultConnectorTypes()
    {
        // Auto-generated constructor stub
    }

    /**
     * Type string for an input connector positioned at the top of a node.
     */
    public static final String TOP_INPUT = "top-input";

    /**
     * Type string for an output connector positioned at the top of a node.
     */
    public static final String TOP_OUTPUT = "top-output";

    /**
     * Type string for an input connector positioned on the right side of a
     * node.
     */
    public static final String RIGHT_INPUT = "right-input";

    /**
     * Type string for an output connector positioned on the right side of a
     * node.
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
     * Type string for an output connector positioned on the left side of a
     * node.
     */
    public static final String LEFT_OUTPUT = "left-output";

    private static final String LEFT_SIDE = "left";
    private static final String RIGHT_SIDE = "right";
    private static final String TOP_SIDE = "top";
    private static final String BOTTOM_SIDE = "bottom";

    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    /**
     * Returns true if the type is supported by the default skins.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the type is supported by the default skins
     */
    public static boolean isValid(final String type)
    {
        final boolean hasSide = type != null && (isTop(type) || isRight(type) || isBottom(type) || isLeft(type));
        final boolean inputOrOutput = type != null && (isInput(type) || isOutput(type));
        return hasSide && inputOrOutput;
    }

    /**
     * Gets the side corresponding to the given connector type.
     *
     * @param type
     *            a non-null connector type
     * @return the {@link Side} the connector type is on
     */
    public static Side getSide(final String type)
    {
        if (isTop(type))
        {
            return Side.TOP;
        }
        else if (isRight(type))
        {
            return Side.RIGHT;
        }
        else if (isBottom(type))
        {
            return Side.BOTTOM;
        }
        else if (isLeft(type))
        {
            return Side.LEFT;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns true if the type corresponds to a connector positioned at the top
     * of a node.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector will be positioned at the top of a
     *         node
     */
    public static boolean isTop(final String type)
    {
        return type.contains(TOP_SIDE);
    }

    /**
     * Returns true if the type corresponds to a connector positioned on the
     * right side of a node.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector will be positioned on the right
     *         side of a node
     */
    public static boolean isRight(final String type)
    {
        return type.contains(RIGHT_SIDE);
    }

    /**
     * Returns true if the type corresponds to a connector positioned at the
     * bottom of a node.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector will be positioned at the bottom of
     *         a node
     */
    public static boolean isBottom(final String type)
    {
        return type.contains(BOTTOM_SIDE);
    }

    /**
     * Returns true if the type corresponds to a connector positioned on the
     * left side of a node.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector will be positioned on the left side
     *         of a node
     */
    public static boolean isLeft(final String type)
    {
        return type.contains(LEFT_SIDE);
    }

    /**
     * Returns true if the type corresponds to an input connector.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector is any kind of input
     */
    public static boolean isInput(final String type)
    {
        return type.contains(INPUT);
    }

    /**
     * Returns true if the type corresponds to an output connector.
     *
     * @param type
     *            a connector's type string
     * @return {@code true} if the connector is any kind of output
     */
    public static boolean isOutput(final String type)
    {
        return type.contains(OUTPUT);
    }

    /**
     * Returns true if the two given types are on the same side of a node.
     *
     * @param firstType
     *            the first connector type
     * @param secondType
     *            the second connector type
     * @return {@code true} if the connectors are on the same side of a node
     */
    public static boolean isSameSide(final String firstType, final String secondType)
    {
        return getSide(firstType) != null && getSide(firstType).equals(getSide(secondType));
    }
}
