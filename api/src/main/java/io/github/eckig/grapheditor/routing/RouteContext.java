package io.github.eckig.grapheditor.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.eckig.grapheditor.GVisualConnectionSkin;
import javafx.geometry.Point2D;

/**
 * Context of a layout pass handed to {@link GVisualConnectionSkin} when drawing its route, e.g. to draw crossings
 * with other connections.
 *
 * @since 25.1.0
 */
public interface RouteContext
{

    /**
     * The routes of all visual connection skins.
     *
     * @return the current routes of all visual connection skins (unmodifiable)
     */
    Map<GVisualConnectionSkin, List<Point2D>> getRoutes();

    /**
     * Computes a value once per layout pass and shares it between all skins, e.g. a derived lookup structure.
     *
     * @param <T>
     *         type of the value
     * @param pKey
     *         key identifying the value
     * @param pFactory
     *         computes the value if not yet present in this pass
     * @return the shared value
     */
    <T> T getShared(Object pKey, Function<RouteContext, T> pFactory);

    /**
     * Creates a route context.
     *
     * @param pRoutes
     *         routes of all visual connection skins
     * @return a new {@link RouteContext}
     */
    static RouteContext of(final Map<GVisualConnectionSkin, List<Point2D>> pRoutes)
    {
        final Map<GVisualConnectionSkin, List<Point2D>> routes = Map.copyOf(pRoutes);
        final Map<Object, Object> shared = new HashMap<>();
        return new RouteContext()
        {

            @Override
            public Map<GVisualConnectionSkin, List<Point2D>> getRoutes()
            {
                return routes;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T getShared(final Object pKey, final Function<RouteContext, T> pFactory)
            {
                final Object value = shared.get(pKey);
                if (value != null)
                {
                    return (T) value;
                }
                final T computed = pFactory.apply(this);
                shared.put(pKey, computed);
                return computed;
            }
        };
    }
}
