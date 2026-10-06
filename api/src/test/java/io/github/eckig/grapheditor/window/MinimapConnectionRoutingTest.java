package io.github.eckig.grapheditor.window;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GConnectorSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GTailSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphFactory;
import io.github.eckig.grapheditor.utils.JavaFXThreadingRule;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Tests how the {@link GraphEditorMinimap} resolves and draws the routes of connections.
 */
public class MinimapConnectionRoutingTest
{

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double SIZE = 100;

    private GModel model;
    private GConnection connection;
    private MinimapNodeGroup group;

    @Before
    public void setUp()
    {
        model = GraphFactory.eINSTANCE.createGModel();

        final GNode source = node(0, 0);
        final GNode target = node(60, 80);
        final GConnector out = connector(source, 20, 10);
        final GConnector in = connector(target, 0, 10);

        connection = GraphFactory.eINSTANCE.createGConnection();
        connection.setSource(out);
        connection.setTarget(in);
        model.getConnections().add(connection);

        group = new MinimapNodeGroup();
        group.setModel(model);
        group.setScaleFactor(1);
        group.resize(SIZE, SIZE);
    }

    private GNode node(final double x, final double y)
    {
        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setX(x);
        node.setY(y);
        node.setWidth(20);
        node.setHeight(20);
        model.getNodes().add(node);
        return node;
    }

    private static GConnector connector(final GNode parent, final double x, final double y)
    {
        final GConnector connector = GraphFactory.eINSTANCE.createGConnector();
        connector.setX(x);
        connector.setY(y);
        parent.getConnectors().add(connector);
        return connector;
    }

    private static GJoint joint(final double x, final double y)
    {
        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
        joint.setX(x);
        joint.setY(y);
        return joint;
    }

    @Test
    public void modelRouterKeepsLegacyRectangularRoute()
    {
        connection.getJoints().add(joint(40, 10));
        connection.getJoints().add(joint(40, 90));

        // legacy: source x shifted by -10, every step reduced to one horizontal or vertical line
        final List<Point2D> expected = List.of(new Point2D(10, 10), new Point2D(40, 10), new Point2D(40, 90),
                new Point2D(60, 90));
        assertEquals(expected, IMinimapConnectionRouter.MODEL.getRoute(connection));
    }

    @Test
    public void modelRouterSkipsIncompleteConnection()
    {
        connection.setTarget(null);
        assertTrue(IMinimapConnectionRouter.MODEL.getRoute(connection).isEmpty());
    }

    @Test
    public void fallsBackToModelRouteWithoutSkinLookup()
    {
        assertEquals(IMinimapConnectionRouter.MODEL.getRoute(connection), group.getRoute(connection));
    }

    @Test
    public void usesRouteOfConnectionSkin()
    {
        final List<Point2D> skinRoute = List.of(new Point2D(20, 10), new Point2D(30, 10), new Point2D(30, 90),
                new Point2D(60, 90));
        group.setSkinLookup(lookupReturning(new RoutedSkin(connection, skinRoute)));

        assertEquals(skinRoute, group.getRoute(connection));
    }

    @Test
    public void fallsBackToModelRouteIfSkinHasNoRoute()
    {
        group.setSkinLookup(lookupReturning(new RoutedSkin(connection, List.of())));

        assertEquals(IMinimapConnectionRouter.MODEL.getRoute(connection), group.getRoute(connection));
    }

    @Test
    public void customRouterTakesPrecedenceOverSkin()
    {
        final List<Point2D> custom = List.of(new Point2D(1, 2), new Point2D(3, 4));
        group.setSkinLookup(lookupReturning(new RoutedSkin(connection, List.of(new Point2D(0, 0), new Point2D(5, 5)))));
        group.setConnectionRouter(c -> custom);

        assertEquals(custom, group.getRoute(connection));
    }

    @Test
    public void customRouterReturningNullIsTreatedAsEmpty()
    {
        group.setConnectionRouter(c -> null);
        assertTrue(group.getRoute(connection).isEmpty());
    }

    @Test
    public void drawsCustomRouteAsStraightDiagonalLine()
    {
        group.setConnectionColor(Color.RED);
        group.setConnectionRouter(c -> List.of(new Point2D(10, 10), new Point2D(90, 90)));

        final PixelReader pixels = render();

        // the diagonal is drawn as-is (no rectangular approximation)
        assertTrue("diagonal should be drawn", isDrawn(pixels, 50, 50));
        assertTrue("diagonal should be drawn", isDrawn(pixels, 30, 30));
        // the corners of a rectangular approximation must stay empty
        assertTrue("no rectangular corner expected", !isDrawn(pixels, 90, 10));
        assertTrue("no rectangular corner expected", !isDrawn(pixels, 10, 90));
    }

    @Test
    public void drawsNothingForEmptyRouteOrFilteredConnection()
    {
        group.setConnectionColor(Color.RED);
        group.setConnectionRouter(c -> List.of(new Point2D(10, 10), new Point2D(90, 90)));
        group.setConnectionFilter(c -> false);
        assertTrue(!isDrawn(render(), 50, 50));

        group.setConnectionFilter(c -> true);
        group.setConnectionRouter(c -> List.of(new Point2D(50, 50)));
        assertTrue(!isDrawn(render(), 50, 50));
    }

    @Test
    public void routesAreScaled()
    {
        group.setConnectionColor(Color.RED);
        group.setScaleFactor(0.5);
        group.setConnectionRouter(c -> List.of(new Point2D(0, 90), new Point2D(180, 90)));

        final PixelReader pixels = render();
        assertTrue("scaled line expected at y = 45", isDrawn(pixels, 60, 45));
        assertTrue("nothing at unscaled position", !isDrawn(pixels, 60, 90));
    }

    @Test
    public void unchangedRoutesAreNotRepainted()
    {
        group.setConnectionRouter(c -> List.of(new Point2D(10, 10), new Point2D(90, 90)));
        group.layout();
        final long painted = group.getPaintCount();

        group.requestLayout();
        group.layout();
        assertEquals("same routes: no repaint", painted, group.getPaintCount());

        group.setConnectionRouter(c -> List.of(new Point2D(10, 10), new Point2D(90, 10)));
        group.layout();
        assertEquals("changed route: repaint", painted + 1, group.getPaintCount());

        group.setConnectionColor(Color.BLUE);
        group.layout();
        assertEquals("changed color: repaint", painted + 2, group.getPaintCount());

        group.setScaleFactor(0.5);
        group.layout();
        assertEquals("changed scale: repaint", painted + 3, group.getPaintCount());
    }

    @Test
    public void routeChangedInPlaceIsRepainted()
    {
        group.setConnectionColor(Color.RED);
        final List<Point2D> mutableRoute = new ArrayList<>(List.of(new Point2D(10, 10), new Point2D(90, 10)));
        group.setConnectionRouter(c -> mutableRoute);
        assertTrue(isDrawn(render(), 50, 10));

        mutableRoute.set(1, new Point2D(10, 90));
        group.requestLayout();
        final PixelReader pixels = render();
        assertTrue("new route drawn", isDrawn(pixels, 10, 50));
        assertTrue("old route erased", !isDrawn(pixels, 50, 10));
    }

    private PixelReader render()
    {
        group.layout();
        final Group root = new Group(group);
        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        final WritableImage image = root.snapshot(params, new WritableImage((int) SIZE, (int) SIZE));
        return image.getPixelReader();
    }

    private static boolean isDrawn(final PixelReader pixels, final int x, final int y)
    {
        // allow for anti aliasing: check the pixel and its direct neighbors
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dy = -1; dy <= 1; dy++)
            {
                if (pixels.getColor(x + dx, y + dy).getOpacity() > 0.2)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static SkinLookup lookupReturning(final GConnectionSkin pSkin)
    {
        return new SkinLookup()
        {

            @Override
            public GNodeSkin lookupNode(final GNode node)
            {
                return null;
            }

            @Override
            public GConnectorSkin lookupConnector(final GConnector connector)
            {
                return null;
            }

            @Override
            public GConnectionSkin lookupConnection(final GConnection connection)
            {
                return pSkin.getItem() == connection ? pSkin : null;
            }

            @Override
            public GJointSkin lookupJoint(final GJoint joint)
            {
                return null;
            }

            @Override
            public GTailSkin lookupTail(final GConnector connector)
            {
                return null;
            }
        };
    }

    private static class RoutedSkin extends GConnectionSkin
    {

        private final List<Point2D> route;

        RoutedSkin(final GConnection connection, final List<Point2D> route)
        {
            super(connection);
            this.route = new ArrayList<>(route);
        }

        @Override
        public List<Point2D> getRoutePoints()
        {
            return route;
        }

        @Override
        public void setJointSkins(final List<GJointSkin> jointSkins)
        {
            // not needed
        }

        @Override
        protected void selectionChanged(final boolean isSelected)
        {
            // not needed
        }

        @Override
        public Node getRoot()
        {
            return new Group();
        }
    }
}
