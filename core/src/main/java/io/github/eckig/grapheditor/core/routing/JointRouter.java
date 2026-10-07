package io.github.eckig.grapheditor.core.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.routing.ConnectionEndpoint;
import io.github.eckig.grapheditor.routing.ConnectionRouter;
import io.github.eckig.grapheditor.routing.RoutingScene;
import javafx.geometry.Point2D;

/**
 * Routes connections through their joints: source, joints, target. Default router for
 * {@link io.github.eckig.grapheditor.GJointConnectionSkin joint connection skins}.
 *
 * @since 25.1.0
 */
public class JointRouter implements ConnectionRouter
{

    @Override
    public Map<GConnection, List<Point2D>> route(final RoutingScene pScene, final List<GConnection> pConnections)
    {
        final Map<GConnection, List<Point2D>> routes = new HashMap<>();
        for (final GConnection connection : pConnections)
        {
            final ConnectionEndpoint source = pScene.getEndpoint(connection.getSource());
            final ConnectionEndpoint target = pScene.getEndpoint(connection.getTarget());
            if (source == null || target == null)
            {
                continue;
            }
            final List<Point2D> joints = pScene.getJointPositions(connection);
            final List<Point2D> route = new ArrayList<>(joints.size() + 2);
            route.add(source.position());
            route.addAll(joints);
            route.add(target.position());
            routes.put(connection, route);
        }
        return routes;
    }
}
