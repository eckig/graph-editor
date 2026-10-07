package io.github.eckig.grapheditor.routing;

import java.util.List;
import java.util.Map;

import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import javafx.geometry.Point2D;

/**
 * Computes the routes of connections. The graph editor's connection layout calls each router once per layout pass with
 * all connections assigned to it and hands the resulting routes to the {@link GVisualConnectionSkin connection skins}.
 *
 * <p>
 * Routes are in the coordinate system of the graph editor view and ordered from source to target (inclusive).
 * </p>
 *
 * <p>
 * Added nodes and connections are not announced separately: they are part of the next {@link #route(RoutingScene,
 * List)} call, when their positions are known. Removals are announced via {@link #nodeRemoved(GNode)} and
 * {@link #connectionRemoved(GConnection)}, so stateful routers can release their resources.
 * </p>
 *
 * @since 25.1.0
 */
public interface ConnectionRouter
{

    /**
     * Routes the given connections.
     *
     * @param pScene
     *         geometry of the graph, see {@link RoutingScene}
     * @param pConnections
     *         the connections assigned to this router
     * @return new routes for (a subset of) the given connections, connections not contained keep their previous route
     */
    Map<GConnection, List<Point2D>> route(RoutingScene pScene, List<GConnection> pConnections);

    /**
     * Whether this router may run on every layout pass.
     *
     * @return {@code true} if this router is cheap enough to run on every layout pass (e.g. while the user drags a
     *         node), {@code false} if it should only run after a model change, in which case a preview router is used
     *         while dragging
     */
    default boolean isInteractive()
    {
        return true;
    }

    /**
     * Called when the graph editor starts editing a (new) model. Stateful routers should reset their state.
     *
     * @param pModel
     *         the {@link GModel} now being edited, may be {@code null}
     */
    default void initialize(final GModel pModel)
    {
        // stateless by default
    }

    /**
     * Called when a node was removed from the model.
     *
     * @param pNode
     *         {@link GNode} removed from the model
     */
    default void nodeRemoved(final GNode pNode)
    {
        // stateless by default
    }

    /**
     * Called when a connection was removed from the model.
     *
     * @param pConnection
     *         {@link GConnection} removed from the model
     */
    default void connectionRemoved(final GConnection pConnection)
    {
        // stateless by default
    }
}
