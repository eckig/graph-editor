package io.github.eckig.grapheditor.core.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.utils.JavaFXThreadingRule;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphPackage;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Verifies that the minimap of the {@link GraphEditorContainer} is fed with the routes drawn by the connection skins.
 */
public class GraphEditorContainerMinimapTest
{

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private static final double WINDOW_SIZE = 400;

    private GraphEditor graphEditor;
    private GraphEditorContainer container;
    private GModel model;
    private Stage stage;

    @Before
    public void setUp()
    {
        model = DummyDataFactory.createModel();

        graphEditor = new DefaultGraphEditor();
        graphEditor.setModel(model);

        container = new GraphEditorContainer();
        container.setGraphEditor(graphEditor);
        container.getMinimap().setVisible(true);

        stage = new Stage();
        stage.setScene(new Scene(new Pane(container), WINDOW_SIZE, WINDOW_SIZE));
        stage.show();

        container.resize(WINDOW_SIZE, WINDOW_SIZE);
        layout();
    }

    @After
    public void tearDown()
    {
        stage.hide();
    }

    private void layout()
    {
        stage.getScene().getRoot().applyCss();
        stage.getScene().getRoot().layout();
        graphEditor.getView().applyCss();
        graphEditor.getView().layout();
        container.layout();
        container.getMinimap().layout();
    }

    @Test
    public void defaultConnectionSkinExposesDrawnRoute()
    {
        final SkinLookup skinLookup = graphEditor.getSkinLookup();
        assertFalse(model.getConnections().isEmpty());

        for (final GConnection connection : model.getConnections())
        {
            final GVisualConnectionSkin skin = (GVisualConnectionSkin) skinLookup.lookupConnection(connection);
            final List<Point2D> route = skin.getRoutePoints();

            assertEquals("route = source + joints + target", connection.getJoints().size() + 2, route.size());
            assertEquals(GeometryUtils.getConnectorPosition(connection.getSource(), skinLookup), route.getFirst());
            assertEquals(GeometryUtils.getConnectorPosition(connection.getTarget(), skinLookup), route.getLast());
        }
    }

    private AtomicInteger countRouting()
    {
        final AtomicInteger routed = new AtomicInteger();
        container.getMinimap().setConnectionRouter(c ->
        {
            routed.incrementAndGet();
            return List.of();
        });
        layout();
        routed.set(0);
        return routed;
    }

    private void moveFirstNodeByCommand()
    {
        final GNode node = model.getNodes().get(0);
        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        domain.getCommandStack().execute(SetCommand.create(domain, node, GraphPackage.Literals.GNODE__X, node.getX() + 20));
    }

    @Test
    public void minimapIsRedrawnAfterModelChange()
    {
        final AtomicInteger routed = countRouting();

        // e.g. a drag gesture committed on mouse release
        moveFirstNodeByCommand();
        graphEditor.getView().requestLayout();
        layout();

        assertTrue("minimap should have re-queried the routes", routed.get() > 0);
    }

    @Test
    public void intermediateLayoutPassesDoNotRedrawMinimap()
    {
        final AtomicInteger routed = countRouting();

        // e.g. connections re-routed on every pixel while dragging, without a model change
        for (int i = 0; i < 5; i++)
        {
            graphEditor.getView().requestLayout();
            layout();
        }

        assertEquals("no minimap update without model change", 0, routed.get());
    }

    @Test
    public void explicitRedrawRequestIsHonored()
    {
        final AtomicInteger routed = countRouting();

        container.getMinimap().redrawConnections();
        layout();

        assertTrue(routed.get() > 0);
    }

    @Test
    public void hiddenMinimapIsNotRedrawn()
    {
        final AtomicInteger routed = new AtomicInteger();
        container.getMinimap().setConnectionRouter(c ->
        {
            routed.incrementAndGet();
            return List.of();
        });
        container.getMinimap().setVisible(false);
        layout();
        routed.set(0);

        moveFirstNodeByCommand();
        graphEditor.getView().requestLayout();
        layout();

        assertEquals(0, routed.get());
    }

    @Test
    public void minimapCatchesUpWhenShownAgain()
    {
        final AtomicInteger routed = new AtomicInteger();
        container.getMinimap().setConnectionRouter(c ->
        {
            routed.incrementAndGet();
            return List.of();
        });
        container.getMinimap().setVisible(false);
        layout();

        // re-routing while hidden is skipped ...
        moveFirstNodeByCommand();
        graphEditor.getView().requestLayout();
        layout();
        routed.set(0);

        // ... and caught up once the minimap is shown again
        container.getMinimap().setVisible(true);
        layout();

        assertTrue("minimap should re-query the routes when shown", routed.get() > 0);
    }
}
