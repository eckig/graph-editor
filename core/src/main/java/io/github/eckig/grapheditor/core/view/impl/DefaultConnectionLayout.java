package io.github.eckig.grapheditor.core.view.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GJointConnectionSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.connectors.DefaultConnectorTypes;
import io.github.eckig.grapheditor.core.routing.JointRouter;
import io.github.eckig.grapheditor.core.routing.OrthogonalRouter;
import io.github.eckig.grapheditor.core.routing.StraightRouter;
import io.github.eckig.grapheditor.core.view.ConnectionLayout;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.routing.ConnectionEndpoint;
import io.github.eckig.grapheditor.routing.ConnectionRouter;
import io.github.eckig.grapheditor.routing.RouteContext;
import io.github.eckig.grapheditor.routing.RoutingScene;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Parent;


/**
 * Default implementation of {@link ConnectionLayout}.
 *
 * <p>
 * Every layout pass, the connections of all {@link GVisualConnectionSkin visual connection skins} are grouped by their
 * {@link ConnectionRouter} (see {@link #setConnectionRouter(Function)}) and each router is called once with all of its
 * connections. The resulting routes are handed to the skins via
 * {@link GVisualConnectionSkin#applyRoute(List, RouteContext)}.
 * </p>
 *
 * <ul>
 * <li>{@link ConnectionRouter#isInteractive() Interactive} routers run on every layout pass.</li>
 * <li>Other routers only run after a {@link #requestRouting() requested routing} (i.e. after a model change). While
 * the user drags a node, connections attached to it are routed by the preview router (see
 * {@link #setPreviewRouter(ConnectionRouter)}).</li>
 * </ul>
 */
public class DefaultConnectionLayout implements ConnectionLayout
{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayout.class);

    private static final ConnectionRouter JOINT_ROUTER = new JointRouter();
    private static final ConnectionRouter STRAIGHT_ROUTER = new StraightRouter();
    private static final int MAX_RETRIES = 10;

    private final SkinLookup mSkinLookup;
    private final Parent mView;
    private GModel mModel;

    private Function<GConnection, ConnectionRouter> mRouterFunction;
    private ConnectionRouter mPreviewRouter = new OrthogonalRouter();
    private Function<GConnector, Side> mSideFunction = DefaultConnectionLayout::defaultSide;

    private final Set<ConnectionRouter> mRouters = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<GConnection, List<Point2D>> mFinalRoutes = new IdentityHashMap<>();
    private final Set<GConnection> mPreviewed = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean mRoutingRequested = true;
    private int mRetries;

    /**
     * Creates a new {@link DefaultConnectionLayout} instance. Only one instance should exist per
     * {@link DefaultGraphEditor} instance.
     *
     * @param pSkinLookup
     *            the {@link SkinLookup} used to look up skins
     * @param pView
     *            the graph editor view, used to request another layout pass if a router
     *            {@link RoutingScene#retryLater() retries}, may be {@code null}
     */
    public DefaultConnectionLayout(final SkinLookup pSkinLookup, final Parent pView)
    {
        mSkinLookup = Objects.requireNonNull(pSkinLookup);
        mView = pView;
    }

    /**
     * @param pRouterFunction
     *            returns the {@link ConnectionRouter} for a connection, or {@code null} for the default: a
     *            {@link JointRouter} for {@link GJointConnectionSkin joint connection skins}, a
     *            {@link StraightRouter} for all other visual connection skins. Return the same router instance for
     *            connections that should be routed together.
     */
    public void setConnectionRouter(final Function<GConnection, ConnectionRouter> pRouterFunction)
    {
        mRouterFunction = pRouterFunction;
        mRouters.clear();
        requestRouting();
    }

    /**
     * @param pPreviewRouter
     *            router used while dragging for connections of non-interactive routers, {@code null} to not draw a
     *            preview. Default: {@link OrthogonalRouter}.
     */
    public void setPreviewRouter(final ConnectionRouter pPreviewRouter)
    {
        mPreviewRouter = pPreviewRouter;
    }

    /**
     * @param pSideFunction
     *            returns the {@link Side} of the node a connector is on (see {@link ConnectionEndpoint#side()}),
     *            {@code null} for the default based on {@link DefaultConnectorTypes#getSide(String)}
     */
    public void setConnectorSideFunction(final Function<GConnector, Side> pSideFunction)
    {
        mSideFunction = pSideFunction == null ? DefaultConnectionLayout::defaultSide : pSideFunction;
        requestRouting();
    }

    private static Side defaultSide(final GConnector pConnector)
    {
        return pConnector.getType() == null ? null : DefaultConnectorTypes.getSide(pConnector.getType());
    }

    @Override
    public void initialize(final GModel pModel)
    {
        mModel = pModel;
        mFinalRoutes.clear();
        mPreviewed.clear();
        for (final ConnectionRouter router : mRouters)
        {
            router.initialize(pModel);
        }
        requestRouting();
    }

    @Override
    public void requestRouting()
    {
        mRoutingRequested = true;
        mRetries = 0;
    }

    @Override
    public void nodeRemoved(final GNode pNode)
    {
        for (final ConnectionRouter router : mRouters)
        {
            router.nodeRemoved(pNode);
        }
    }

    @Override
    public void connectionRemoved(final GConnection pConnection)
    {
        mFinalRoutes.remove(pConnection);
        mPreviewed.remove(pConnection);
        for (final ConnectionRouter router : mRouters)
        {
            router.connectionRemoved(pConnection);
        }
    }

    /**
     * @return the {@link SkinLookup}
     */
    public SkinLookup getSkinLookup()
    {
        return mSkinLookup;
    }

    @Override
    public boolean draw()
    {
        if (mModel == null || mModel.getConnections().isEmpty())
        {
            return false;
        }

        final boolean routeAll = mRoutingRequested;
        mRoutingRequested = false;
        final Scene scene = new Scene();
        try
        {
            return route(scene, routeAll);
        }
        catch (Exception e)
        {
            LOGGER.debug("Could not route connections: ", e); //$NON-NLS-1$
            scene.retryLater();
            return false;
        }
        finally
        {
            if (scene.mRetry)
            {
                retry();
            }
            else if (routeAll)
            {
                mRetries = 0;
            }
        }
    }

    private void retry()
    {
        if (++mRetries > MAX_RETRIES)
        {
            // give up until the next model change, otherwise every pulse would route again
            LOGGER.warn("Could not route connections after {} attempts, retrying on the next model change", //$NON-NLS-1$
                    Integer.valueOf(MAX_RETRIES));
            return;
        }
        mRoutingRequested = true;
        if (mView != null)
        {
            mView.requestLayout();
        }
    }

    private boolean route(final Scene pScene, final boolean pRouteAll)
    {
        // group by router:
        final Map<ConnectionRouter, List<GConnection>> byRouter = new LinkedHashMap<>();
        final Map<GConnection, GVisualConnectionSkin> skins = new HashMap<>();
        for (final GConnection connection : mModel.getConnections())
        {
            if (mSkinLookup.lookupConnection(connection) instanceof GVisualConnectionSkin skin)
            {
                if (skin instanceof GJointConnectionSkin jointSkin)
                {
                    jointSkin.prepareRoute();
                }
                skins.put(connection, skin);
                byRouter.computeIfAbsent(getRouter(connection, skin), _ -> new ArrayList<>()).add(connection);
            }
        }

        // only routers still in use receive callbacks:
        mRouters.retainAll(byRouter.keySet());

        // route:
        final Map<GConnection, List<Point2D>> routes = new HashMap<>();
        for (final Map.Entry<ConnectionRouter, List<GConnection>> entry : byRouter.entrySet())
        {
            final ConnectionRouter router = entry.getKey();
            if (pRouteAll || router.isInteractive())
            {
                routes.putAll(router.route(pScene, entry.getValue()));
            }
            else if (mPreviewRouter != null)
            {
                final List<GConnection> changing = entry.getValue().stream().filter(this::isChanging).toList();
                if (!changing.isEmpty())
                {
                    routes.putAll(mPreviewRouter.route(pScene, changing));
                    mPreviewed.addAll(changing);
                }
            }
        }

        // connections showing a preview route get their final route back if the router did not route them again
        // (e.g. drag released without model change, or route unchanged):
        for (final var it = mPreviewed.iterator(); it.hasNext();)
        {
            final GConnection connection = it.next();
            if (routes.containsKey(connection) && !pRouteAll)
            {
                continue; // still previewing
            }
            if (!routes.containsKey(connection) && (pRouteAll || !isChanging(connection)))
            {
                final List<Point2D> finalRoute = mFinalRoutes.get(connection);
                if (finalRoute != null)
                {
                    routes.put(connection, finalRoute);
                }
            }
            if (pRouteAll || !isChanging(connection))
            {
                it.remove();
            }
        }

        // draw:
        if (!routes.isEmpty())
        {
            final Map<GVisualConnectionSkin, List<Point2D>> allRoutes = new IdentityHashMap<>();
            for (final Map.Entry<GConnection, GVisualConnectionSkin> entry : skins.entrySet())
            {
                final List<Point2D> route = routes.get(entry.getKey());
                allRoutes.put(entry.getValue(), route == null ? entry.getValue().getRoutePoints() : List.copyOf(route));
            }
            final RouteContext context = RouteContext.of(allRoutes);
            for (final Map.Entry<GConnection, List<Point2D>> entry : routes.entrySet())
            {
                final GVisualConnectionSkin skin = skins.get(entry.getKey());
                if (skin != null)
                {
                    try
                    {
                        skin.applyRoute(entry.getValue(), context);
                    }
                    catch (Exception e)
                    {
                        // one broken skin must not prevent drawing the others
                        LOGGER.warn("Could not draw connection skin {}: ", skin, e); //$NON-NLS-1$
                    }
                }
            }
        }

        // detect changes of the final routes:
        boolean changed = false;
        if (pRouteAll)
        {
            for (final Map.Entry<GConnection, GVisualConnectionSkin> entry : skins.entrySet())
            {
                final List<Point2D> route = entry.getValue().getRoutePoints();
                if (!route.equals(mFinalRoutes.put(entry.getKey(), route)))
                {
                    changed = true;
                }
            }
        }
        return changed;
    }

    private ConnectionRouter getRouter(final GConnection pConnection, final GConnectionSkin pSkin)
    {
        ConnectionRouter router = mRouterFunction == null ? null : mRouterFunction.apply(pConnection);
        if (router == null)
        {
            router = pSkin instanceof GJointConnectionSkin ? JOINT_ROUTER : STRAIGHT_ROUTER;
        }
        if (mRouters.add(router))
        {
            router.initialize(mModel);
        }
        return router;
    }

    /**
     * @return {@code true} if one of the nodes of the connection is currently being changed by the user, i.e. its
     *         skin differs from its model values
     */
    private boolean isChanging(final GConnection pConnection)
    {
        return isChanging(pConnection.getSource()) || isChanging(pConnection.getTarget());
    }

    private boolean isChanging(final GConnector pConnector)
    {
        final GNode node = pConnector == null ? null : pConnector.getParent();
        final GNodeSkin skin = node == null ? null : mSkinLookup.lookupNode(node);
        if (skin == null)
        {
            return false;
        }
        final var root = skin.getRoot();
        return root.getLayoutX() != node.getX() || root.getLayoutY() != node.getY() || root.getWidth() != node.getWidth()
                || root.getHeight() != node.getHeight();
    }

    private final class Scene implements RoutingScene
    {

        private boolean mRetry;

        @Override
        public GModel getModel()
        {
            return mModel;
        }

        @Override
        public Bounds getNodeBounds(final GNode pNode)
        {
            return new BoundingBox(pNode.getX(), pNode.getY(), pNode.getWidth(), pNode.getHeight());
        }

        @Override
        public ConnectionEndpoint getEndpoint(final GConnector pConnector)
        {
            if (pConnector == null || pConnector.getParent() == null
                    || mSkinLookup.lookupNode(pConnector.getParent()) == null
                    || mSkinLookup.lookupConnector(pConnector) == null)
            {
                return null;
            }
            final Point2D position = GeometryUtils.getConnectorPosition(pConnector, mSkinLookup);
            return new ConnectionEndpoint(pConnector, position, mSideFunction.apply(pConnector));
        }

        @Override
        public List<Point2D> getJointPositions(final GConnection pConnection)
        {
            return GeometryUtils.getJointPositions(pConnection, mSkinLookup);
        }

        @Override
        public void retryLater()
        {
            mRetry = true;
        }
    }
}
