package io.github.eckig.grapheditor.core.view.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.routing.StraightRouter;
import io.github.eckig.grapheditor.core.utils.JavaFXThreadingRule;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphPackage;
import io.github.eckig.grapheditor.routing.ConnectionRouter;
import io.github.eckig.grapheditor.routing.RoutingScene;
import javafx.geometry.Point2D;

/**
 * Tests the generic {@link DefaultConnectionLayout}: how routers are called and routes are applied.
 */
public class DefaultConnectionLayoutTest
{

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private DefaultGraphEditor graphEditor;
    private GraphEditorView view;
    private GModel model;

    /** Router counting its calls, routing straight lines. */
    private static class CountingRouter implements ConnectionRouter
    {

        private final boolean interactive;
        private final List<List<GConnection>> calls = new ArrayList<>();
        private final List<GConnection> removedConnections = new ArrayList<>();
        private final List<GNode> removedNodes = new ArrayList<>();
        private boolean returnNothing;
        private int retries;

        CountingRouter(final boolean pInteractive)
        {
            interactive = pInteractive;
        }

        @Override
        public Map<GConnection, List<Point2D>> route(final RoutingScene pScene, final List<GConnection> pConnections)
        {
            calls.add(List.copyOf(pConnections));
            if (retries > 0)
            {
                retries--;
                pScene.retryLater();
            }
            return returnNothing ? Map.of() : new StraightRouter().route(pScene, pConnections);
        }

        @Override
        public boolean isInteractive()
        {
            return interactive;
        }

        @Override
        public void connectionRemoved(final GConnection pConnection)
        {
            removedConnections.add(pConnection);
        }

        @Override
        public void nodeRemoved(final GNode pNode)
        {
            removedNodes.add(pNode);
        }
    }

    @Before
    public void setUp()
    {
        model = DummyDataFactory.createModel();
        graphEditor = new DefaultGraphEditor();
        view = (GraphEditorView) graphEditor.getView();
    }

    private void start(final ConnectionRouter pRouter)
    {
        graphEditor.setConnectionRouter(_ -> pRouter);
        graphEditor.setModel(model);
        layout();
    }

    private void layout()
    {
        view.requestLayout();
        view.layout();
    }

    private void moveFirstNodeByCommand()
    {
        final GNode node = model.getNodes().getFirst();
        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        domain.getCommandStack().execute(SetCommand.create(domain, node, GraphPackage.Literals.GNODE__X, node.getX() + 40));
    }

    private GVisualConnectionSkin skin(final GConnection pConnection)
    {
        return (GVisualConnectionSkin) graphEditor.getSkinLookup().lookupConnection(pConnection);
    }

    @Test
    public void routerIsCalledOncePerPassWithAllItsConnections()
    {
        final CountingRouter router = new CountingRouter(true);
        start(router);
        router.calls.clear();

        layout();

        assertEquals(1, router.calls.size());
        assertEquals(model.getConnections(), router.calls.getFirst());
    }

    @Test
    public void routingRunsOncePerPassWhenConnectionLayerChanges()
    {
        final CountingRouter router = new CountingRouter(true);
        start(router);

        // adding a connection changes the connection layer (new skin), which must not cause a second draw
        final GConnection connection = io.github.eckig.grapheditor.model.GraphFactory.eINSTANCE.createGConnection();
        connection.setSource(model.getConnections().getFirst().getSource());
        connection.setTarget(model.getConnections().getFirst().getTarget());
        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        domain.getCommandStack().execute(org.eclipse.emf.edit.command.AddCommand.create(domain, model,
                GraphPackage.Literals.GMODEL__CONNECTIONS, connection));
        router.calls.clear();
        view.layout();

        assertEquals(1, router.calls.size());
    }

    @Test
    public void connectionsAreGroupedByRouter()
    {
        final CountingRouter first = new CountingRouter(true);
        final CountingRouter second = new CountingRouter(true);
        final GConnection firstConnection = model.getConnections().getFirst();
        graphEditor.setConnectionRouter(c -> c == firstConnection ? first : second);
        graphEditor.setModel(model);
        layout();

        assertEquals(List.of(firstConnection), first.calls.getLast());
        assertFalse(second.calls.getLast().contains(firstConnection));
        assertEquals(model.getConnections().size() - 1, second.calls.getLast().size());
    }

    @Test
    public void routesAreAppliedToSkins()
    {
        start(new CountingRouter(true));

        for (final GConnection connection : model.getConnections())
        {
            assertEquals(2, skin(connection).getRoutePoints().size());
        }
    }

    @Test
    public void connectionsNotInResultKeepTheirRoute()
    {
        final CountingRouter router = new CountingRouter(true);
        start(router);
        final GConnection connection = model.getConnections().getFirst();
        final List<Point2D> before = skin(connection).getRoutePoints();

        router.returnNothing = true;
        layout();

        assertFalse(before.isEmpty());
        assertEquals(before, skin(connection).getRoutePoints());
    }

    @Test
    public void nonInteractiveRouterOnlyRunsAfterModelChange()
    {
        final CountingRouter router = new CountingRouter(false);
        start(router);
        assertEquals("initial routing", 1, router.calls.size());

        layout();
        layout();
        assertEquals("no routing without model change", 1, router.calls.size());

        moveFirstNodeByCommand();
        layout();
        assertEquals("routing after model change", 2, router.calls.size());
    }

    @Test
    public void previewRouterIsUsedWhileDragging()
    {
        final CountingRouter router = new CountingRouter(false);
        final CountingRouter preview = new CountingRouter(true);
        graphEditor.setPreviewRouter(preview);
        start(router);
        router.calls.clear();

        // drag: the skin moves, the model is not yet updated
        final GNode node = model.getNodes().getFirst();
        graphEditor.getSkinLookup().lookupNode(node).getRoot().setLayoutX(node.getX() + 40);
        layout();

        assertTrue("final router must not run while dragging", router.calls.isEmpty());
        assertFalse("preview expected", preview.calls.isEmpty());
        for (final GConnection connection : preview.calls.getLast())
        {
            assertTrue("only connections of the dragged node",
                    connection.getSource().getParent() == node || connection.getTarget().getParent() == node);
        }
    }

    @Test
    public void finalRouteIsRestoredWhenDragEndsWithoutModelChange()
    {
        final CountingRouter router = new CountingRouter(false);
        start(router);
        final GNode node = model.getNodes().getFirst();
        final GConnection connection = model.getConnections().stream()
                .filter(c -> c.getSource().getParent() == node || c.getTarget().getParent() == node).findFirst()
                .orElseThrow();
        final List<Point2D> finalRoute = skin(connection).getRoutePoints();

        // drag: preview route
        final var root = graphEditor.getSkinLookup().lookupNode(node).getRoot();
        root.setLayoutX(node.getX() + 40);
        layout();
        assertFalse("preview expected", finalRoute.equals(skin(connection).getRoutePoints()));

        // released at the start position: no command, no model change
        root.setLayoutX(node.getX());
        layout();
        assertEquals(finalRoute, skin(connection).getRoutePoints());
    }

    @Test
    public void persistentRetryIsCapped()
    {
        final CountingRouter router = new CountingRouter(false);
        router.retries = Integer.MAX_VALUE;
        start(router);
        for (int i = 0; i < 30; i++)
        {
            layout();
        }

        assertTrue("retries must be capped, but was " + router.calls.size(), router.calls.size() <= 12);

        // a model change routes again
        final int before = router.calls.size();
        moveFirstNodeByCommand();
        layout();
        assertTrue(router.calls.size() > before);
    }

    @Test
    public void replacedRouterNoLongerReceivesCallbacks()
    {
        final CountingRouter old = new CountingRouter(true);
        start(old);
        graphEditor.setConnectionRouter(_ -> new StraightRouter());
        layout();

        graphEditor.delete(List.of(model.getConnections().getFirst()));
        graphEditor.flush();

        assertTrue(old.removedConnections.isEmpty());
    }

    @Test
    public void retryLaterTriggersAnotherRouting()
    {
        final CountingRouter router = new CountingRouter(false);
        router.retries = 1;
        start(router);
        layout();
        assertEquals("initial routing + retry", 2, router.calls.size());

        layout();
        assertEquals("no further retry", 2, router.calls.size());
    }

    @Test
    public void removalsAreForwardedToRouter()
    {
        final CountingRouter router = new CountingRouter(true);
        start(router);
        final GConnection connection = model.getConnections().getFirst();
        final GNode node = model.getNodes().getLast();

        graphEditor.delete(List.of(connection, node));
        graphEditor.flush();

        assertTrue(router.removedConnections.contains(connection));
        assertTrue(router.removedNodes.contains(node));
    }

    @Test
    public void nonVisualSkinsAreNotRouted()
    {
        graphEditor.setConnectionSkinFactory(c -> new GConnectionSkin(c)
        {

            @Override
            protected void selectionChanged(final boolean pIsSelected)
            {
                // not needed
            }
        });
        final CountingRouter router = new CountingRouter(true);
        start(router);

        assertTrue(router.calls.isEmpty());
    }

    @Test
    public void routesChangedOnlyForFinalRoutes()
    {
        final AtomicInteger changes = new AtomicInteger();
        view.setOnRoutesChanged(changes::incrementAndGet);
        start(new CountingRouter(true));
        assertEquals("initial routes", 1, changes.get());

        layout();
        layout();
        assertEquals("intermediate passes", 1, changes.get());

        moveFirstNodeByCommand();
        layout();
        assertEquals("changed after model change", 2, changes.get());
    }

    @Test
    public void defaultRouterRoutesThroughJoints()
    {
        graphEditor.setModel(model);
        layout();

        final Map<GConnection, Integer> expected = new HashMap<>();
        for (final GConnection connection : model.getConnections())
        {
            expected.put(connection, connection.getJoints().size() + 2);
        }
        for (final GConnection connection : model.getConnections())
        {
            assertEquals((int) expected.get(connection), skin(connection).getRoutePoints().size());
        }
    }
}
