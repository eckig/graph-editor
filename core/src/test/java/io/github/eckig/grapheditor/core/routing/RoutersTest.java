package io.github.eckig.grapheditor.core.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphFactory;
import io.github.eckig.grapheditor.routing.ConnectionEndpoint;
import io.github.eckig.grapheditor.routing.RoutingScene;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;

/**
 * Tests the built-in {@link io.github.eckig.grapheditor.routing.ConnectionRouter routers}.
 */
public class RoutersTest
{

    private final Map<GConnector, ConnectionEndpoint> endpoints = new HashMap<>();
    private final RoutingScene scene = new RoutingScene()
    {

        @Override
        public GModel getModel()
        {
            return null;
        }

        @Override
        public Bounds getNodeBounds(final GNode pNode)
        {
            return new BoundingBox(pNode.getX(), pNode.getY(), pNode.getWidth(), pNode.getHeight());
        }

        @Override
        public ConnectionEndpoint getEndpoint(final GConnector pConnector)
        {
            return endpoints.get(pConnector);
        }

        @Override
        public List<Point2D> getJointPositions(final GConnection pConnection)
        {
            return pConnection.getJoints().stream().map(j -> new Point2D(j.getX(), j.getY())).toList();
        }

        @Override
        public void retryLater()
        {
            // not needed
        }
    };

    private GConnection connection;

    @Before
    public void setUp()
    {
        final GConnector source = GraphFactory.eINSTANCE.createGConnector();
        final GConnector target = GraphFactory.eINSTANCE.createGConnector();
        connection = GraphFactory.eINSTANCE.createGConnection();
        connection.setSource(source);
        connection.setTarget(target);
        endpoints.put(source, new ConnectionEndpoint(source, new Point2D(0, 0), Side.RIGHT));
        endpoints.put(target, new ConnectionEndpoint(target, new Point2D(200, 100), Side.LEFT));
    }

    private static GJoint joint(final double x, final double y)
    {
        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
        joint.setX(x);
        joint.setY(y);
        return joint;
    }

    @Test
    public void jointRouterRoutesThroughJoints()
    {
        connection.getJoints().add(joint(100, 0));
        connection.getJoints().add(joint(100, 100));

        assertEquals(List.of(new Point2D(0, 0), new Point2D(100, 0), new Point2D(100, 100), new Point2D(200, 100)),
                new JointRouter().route(scene, List.of(connection)).get(connection));
    }

    @Test
    public void straightRouterIgnoresJoints()
    {
        connection.getJoints().add(joint(100, 0));

        assertEquals(List.of(new Point2D(0, 0), new Point2D(200, 100)),
                new StraightRouter().route(scene, List.of(connection)).get(connection));
    }

    @Test
    public void orthogonalRouterCreatesRectangularRoute()
    {
        final List<Point2D> route = new OrthogonalRouter().route(scene, List.of(connection)).get(connection);

        assertEquals(new Point2D(0, 0), route.getFirst());
        assertEquals(new Point2D(200, 100), route.getLast());
        for (int i = 1; i < route.size(); i++)
        {
            final Point2D a = route.get(i - 1);
            final Point2D b = route.get(i);
            assertTrue("segment " + i + " must be horizontal or vertical: " + route,
                    a.getX() == b.getX() || a.getY() == b.getY());
        }
    }

    @Test
    public void orthogonalRouterAllSideCombinationsStayRectangular()
    {
        for (final Side s : Side.values())
        {
            for (final Side t : Side.values())
            {
                final List<Point2D> route = OrthogonalRouter.route(
                        new ConnectionEndpoint(connection.getSource(), new Point2D(10, 20), s),
                        new ConnectionEndpoint(connection.getTarget(), new Point2D(300, 250), t));
                assertEquals(new Point2D(10, 20), route.getFirst());
                assertEquals(new Point2D(300, 250), route.getLast());
                for (int i = 1; i < route.size(); i++)
                {
                    final Point2D a = route.get(i - 1);
                    final Point2D b = route.get(i);
                    assertTrue(s + "->" + t + " segment " + i + ": " + route, a.getX() == b.getX() || a.getY() == b.getY());
                }
            }
        }
    }

    @Test
    public void orthogonalRouterWithoutSideIsStraight()
    {
        endpoints.put(connection.getSource(), new ConnectionEndpoint(connection.getSource(), new Point2D(0, 0), null));

        assertEquals(List.of(new Point2D(0, 0), new Point2D(200, 100)),
                new OrthogonalRouter().route(scene, List.of(connection)).get(connection));
    }

    @Test
    public void connectionsWithoutEndpointAreNotRouted()
    {
        endpoints.remove(connection.getTarget());

        assertFalse(new JointRouter().route(scene, List.of(connection)).containsKey(connection));
        assertFalse(new StraightRouter().route(scene, List.of(connection)).containsKey(connection));
        assertFalse(new OrthogonalRouter().route(scene, List.of(connection)).containsKey(connection));
    }
}
