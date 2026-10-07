package io.github.eckig.grapheditor.core.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.routing.ConnectionEndpoint;
import io.github.eckig.grapheditor.routing.ConnectionRouter;
import io.github.eckig.grapheditor.routing.RoutingScene;
import javafx.geometry.Point2D;

/**
 * Routes connections as a straight line from source to target, ignoring joints. Default router for
 * {@link io.github.eckig.grapheditor.GVisualConnectionSkin visual connection skins} not using joints.
 *
 * @since 25.1.0
 */
public class StraightRouter implements ConnectionRouter
{

    @Override
    public Map<GConnection, List<Point2D>> route(final RoutingScene pScene, final List<GConnection> pConnections)
    {
        final Map<GConnection, List<Point2D>> routes = new HashMap<>();
        for (final GConnection connection : pConnections)
        {
            final ConnectionEndpoint source = pScene.getEndpoint(connection.getSource());
            final ConnectionEndpoint target = pScene.getEndpoint(connection.getTarget());
            if (source != null && target != null)
            {
                routes.put(connection, List.of(source.position(), target.position()));
            }
        }
        return routes;
    }
}
