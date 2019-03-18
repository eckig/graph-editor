/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.connections;

import de.tesis.dynaware.grapheditor.core.connectors.DefaultConnectorTypes;
import de.tesis.dynaware.grapheditor.model.GConnection;
import javafx.geometry.Side;


/**
 * Miscellaneous helper methods for rectangular-shaped connections.
 */
public final class RectangularConnections
{

    private RectangularConnections()
    {
        // Auto-generated constructor stub
    }

    /**
     * Returns true if the segment beginning at index i is horizontal.
     *
     * <p>
     * This calculates using the index of the segment and the connector type it
     * starts from, and <b>not</b> the current position of the segment. The
     * latter may be unreliable in the case that 2 joints are on top of each
     * other.
     * </p>
     *
     * @param connection
     *            a {@link GConnection} instance with a non-null source
     *            connector
     * @param i
     *            an index in the list of the connection's points
     * @return {@code true} if the segment beginning at this index is horizontal
     */
    public static boolean isSegmentHorizontal(final GConnection connection, final int i)
    {
        final String sourceType = connection.getSource().getType();
        final boolean sourceIsLeft = DefaultConnectorTypes.isLeft(sourceType);
        final boolean sourceIsRight = DefaultConnectorTypes.isRight(sourceType);
        final boolean firstSegmentHorizontal = sourceIsLeft || sourceIsRight;

        return firstSegmentHorizontal == ((i & 1) == 0);
    }

    /**
     * Checks that the given connection has a workable number of joints.
     *
     * @param connection
     *            a {@link GConnection} that should be rectangular
     * @return {@code true} if the joint count is correct
     */
    public static boolean checkJointCount(final GConnection connection)
    {
        final Side sourceSide = DefaultConnectorTypes.getSide(connection.getSource().getType());
        final Side targetSide = DefaultConnectorTypes.getSide(connection.getTarget().getType());

        final boolean bothHorizontal = sourceSide.isHorizontal() && targetSide.isHorizontal();
        final boolean bothVertical = sourceSide.isVertical() && targetSide.isVertical();

        if (bothHorizontal || bothVertical)
        {
            return (connection.getJoints().size() & 1) == 0;
        }
        else
        {
            return (connection.getJoints().size() & 1) == 1;
        }
    }
}
