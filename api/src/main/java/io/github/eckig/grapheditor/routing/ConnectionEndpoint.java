package io.github.eckig.grapheditor.routing;

import io.github.eckig.grapheditor.model.GConnector;
import javafx.geometry.Point2D;
import javafx.geometry.Side;

/**
 * End point of a connection at a connector.
 *
 * @param connector
 *         the {@link GConnector}
 * @param position
 *         position of the connector in the coordinate system of the graph editor view
 * @param side
 *         the side of the node the connector is on, i.e. the direction a connection leaves the node, or {@code null}
 *         if unknown
 * @since 25.1.0
 */
public record ConnectionEndpoint(GConnector connector, Point2D position, Side side)
{
}
