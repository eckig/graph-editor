package io.github.eckig.grapheditor;

import java.util.List;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.routing.RouteContext;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;

/**
 * A {@link GConnectionSkin} with a visual representation in the graph editor view.
 *
 * <p>
 * The root JavaFX node must be created by the skin implementation and returned in the {@link #getRoot()} method. For
 * example, a very simple connection skin could use a {@link Line} whose start and end positions are set to those of the
 * source and target connectors.
 * </p>
 *
 * <p>
 * The route is computed by a {@link io.github.eckig.grapheditor.routing.ConnectionRouter} and handed to the skin via
 * {@link #applyRoute(List, RouteContext)}, the skin only draws it in {@link #drawRoute(List, RouteContext)}.
 * </p>
 *
 * <p>
 * Skins of this type do not use joints, no joint skins are created for their connection. Skins displaying or
 * constraining joints must extend {@link GJointConnectionSkin} instead.
 * </p>
 *
 * @since 25.1.0
 */
public abstract class GVisualConnectionSkin extends GConnectionSkin
{

    private List<Point2D> routePoints = List.of();

    /**
     * Creates a new {@link GVisualConnectionSkin}.
     *
     * @param pConnection
     *         the {@link GConnection} represented by the skin
     */
    public GVisualConnectionSkin(final GConnection pConnection)
    {
        super(pConnection);
    }

    /**
     * Gets the root JavaFX node of the skin, added to the graph editor view.
     *
     * @return the skin's root JavaFX {@link Node}, never {@code null}
     */
    public abstract Node getRoot();

    /**
     * Sets the route of this skin and draws it. Called by the graph editor's connection layout.
     *
     * @param pRoute
     *         route from source to target (inclusive) in the coordinate system of the graph editor view, an empty
     *         list or {@code null} if the connection cannot be routed
     * @param pContext
     *         {@link RouteContext} of the current layout pass
     */
    public final void applyRoute(final List<Point2D> pRoute, final RouteContext pContext)
    {
        routePoints = pRoute == null ? List.of() : List.copyOf(pRoute);
        drawRoute(routePoints, pContext);
    }

    /**
     * Draws the given route.
     *
     * @param pRoute
     *         route from source to target (inclusive), may be empty
     * @param pContext
     *         {@link RouteContext} of the current layout pass, e.g. to draw crossings with other connections
     */
    protected abstract void drawRoute(List<Point2D> pRoute, RouteContext pContext);

    /**
     * Returns the route this skin currently displays, e.g. for the minimap.
     *
     * <p>
     * The points are in the coordinate system of the graph editor view and ordered from source to target (inclusive).
     * </p>
     *
     * @return unmodifiable list of route points, empty if not routed yet, never {@code null}
     */
    public final List<Point2D> getRoutePoints()
    {
        return routePoints;
    }
}
