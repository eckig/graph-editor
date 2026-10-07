package io.github.eckig.grapheditor.core.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.eckig.grapheditor.core.skins.defaults.tail.RectangularPathCreator;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.routing.ConnectionEndpoint;
import io.github.eckig.grapheditor.routing.ConnectionRouter;
import io.github.eckig.grapheditor.routing.RoutingScene;
import javafx.geometry.Point2D;

/**
 * Routes connections as simple rectangular (orthogonal) lines based on the sides of their end points, without avoiding
 * obstacles. Used as the default preview router while dragging, for routers that are not
 * {@link ConnectionRouter#isInteractive() interactive}. Connections with an end point without
 * {@link ConnectionEndpoint#side() side} are routed as a straight line.
 *
 * @since 25.1.0
 */
public class OrthogonalRouter implements ConnectionRouter
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
                routes.put(connection, route(source, target));
            }
        }
        return routes;
    }

    /**
     * @param pSource
     *         source {@link ConnectionEndpoint}
     * @param pTarget
     *         target {@link ConnectionEndpoint}
     * @return route from source to target (inclusive)
     */
    public static List<Point2D> route(final ConnectionEndpoint pSource, final ConnectionEndpoint pTarget)
    {
        final List<Point2D> route = new ArrayList<>();
        route.add(pSource.position());
        if (pSource.side() != null && pTarget.side() != null)
        {
            route.addAll(RectangularPathCreator.createPath(pSource.position(), pTarget.position(), pSource.side(),
                    pTarget.side()));
        }
        route.add(pTarget.position());
        return route;
    }
}
