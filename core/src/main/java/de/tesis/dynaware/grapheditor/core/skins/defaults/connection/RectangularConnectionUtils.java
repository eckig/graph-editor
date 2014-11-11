/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import de.tesis.dynaware.grapheditor.model.GConnection;

/**
 * Helper methods for rectangular-shaped connections.
 */
public class RectangularConnectionUtils {

    private static final String LEFT_SIDE = "left";
    private static final String RIGHT_SIDE = "right";

    /**
     * Returns true if the segment beginning at index i is horizontal.
     * 
     * <p>
     * This calculates using the index of the segment and the connector type it starts from, and <b>not</b> the current
     * position of the segment. The latter may be unreliable in the case that 2 joints are on top of each other.
     * </p>
     *
     * @param connection a {@link GConnection} instance with a non-null source connector
     * @param i an index in the list of the connection's points
     * @return {@code true} if the segment beginning at this index is horizontal
     */
    public static boolean isSegmentHorizontal(final GConnection connection, final int i) {

        final String sourceType = connection.getSource().getType();
        final boolean sourceIsLeft = sourceType.contains(LEFT_SIDE);
        final boolean sourceIsRight = sourceType.contains(RIGHT_SIDE);
        final boolean firstSegmentHorizontal = sourceIsLeft || sourceIsRight;

        return firstSegmentHorizontal == ((i & 1) == 0);
    }

    /**
     * Calculates the minimum number of joints allowed by a rectangular connection.
     * 
     * @param connection a {@link GConnection} that should be rectangular
     * @return the minimum nzmber of joints allowed by this connection
     */
    public static int calculateMinJointNumber(final GConnection connection) {

        final boolean sourceOnLeft = connection.getSource().getType().contains(LEFT_SIDE);
        final boolean sourceOnRight = connection.getSource().getType().equals(RIGHT_SIDE);
        final boolean targetOnLeft = connection.getTarget().getType().equals(LEFT_SIDE);
        final boolean targetOnRight = connection.getTarget().getType().equals(RIGHT_SIDE);

        if ((sourceOnLeft || sourceOnRight) && (targetOnLeft || targetOnRight)) {
            return 2;
        } else {
            return 1;
        }
    }
}
