package io.github.eckig.grapheditor.window;

import java.util.ArrayList;
import java.util.List;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import javafx.geometry.Point2D;

/**
 * Supplies the route of a {@link GConnection} to the {@link GraphEditorMinimap}.
 *
 * <p>
 * The returned points are in <b>content coordinates</b> (the unscaled coordinate system of the graph editor view) and
 * are ordered from source to target. The minimap scales them and joins consecutive points with straight lines, so
 * orthogonal, diagonal or any other routing can be represented.
 * </p>
 *
 * @see GraphEditorMinimap#setConnectionRouter(IMinimapConnectionRouter)
 */
@FunctionalInterface
public interface IMinimapConnectionRouter
{
    /**
     * Legacy router computing an approximated rectangular route purely from the model (connector and joint
     * positions). Used as a fallback when no other route is available.
     */
    IMinimapConnectionRouter MODEL = IMinimapConnectionRouter::modelRoute;

    /**
     * Only called for connections accepted by the connection filter, see
     * {@link GraphEditorMinimap#setConnectionFilter(java.util.function.Predicate)}.
     *
     * @param pConnection
     *         {@link GConnection}
     * @return ordered route points from source to target (inclusive), or an empty list / {@code null} to not draw the
     *         connection
     */
    List<Point2D> getRoute(GConnection pConnection);

    /**
     * Creates a router that uses the route last drawn by the {@link GVisualConnectionSkin} of each connection (see
     * {@link GVisualConnectionSkin#getRoutePoints()}).
     *
     * @param pSkinLookup
     *         {@link SkinLookup}
     * @return router returning the route of the connection skin, or an empty list if not available or the connection
     *         skin is not visual
     */
    static IMinimapConnectionRouter fromSkins(final SkinLookup pSkinLookup)
    {
        return c ->
        {
            final GConnectionSkin skin = pSkinLookup == null ? null : pSkinLookup.lookupConnection(c);
            final List<Point2D> route = skin instanceof GVisualConnectionSkin v ? v.getRoutePoints() : null;
            return route == null ? List.of() : route;
        };
    }

    private static List<Point2D> modelRoute(final GConnection pConnection)
    {
        final GConnector source = pConnection.getSource();
        final GConnector target = pConnection.getTarget();
        if (source == null || target == null || source.getParent() == null || target.getParent() == null)
        {
            return List.of();
        }

        final List<Point2D> route = new ArrayList<>(pConnection.getJoints().size() + 2);
        double x = source.getX() + source.getParent().getX() - 10;
        double y = source.getY() + source.getParent().getY();
        route.add(new Point2D(x, y));

        for (int j = 0; j <= pConnection.getJoints().size(); j++)
        {
            final double newX;
            final double newY;
            if (j < pConnection.getJoints().size())
            {
                final GJoint joint = pConnection.getJoints().get(j);
                newX = joint.getX();
                newY = joint.getY();
            }
            else
            {
                newX = target.getX() + target.getParent().getX();
                newY = target.getY() + target.getParent().getY();
            }

            // only draw direct rectangular and sharp lines:
            if (Math.abs(newX - x) < Math.abs(newY - y))
            {
                route.add(new Point2D(x, newY));
            }
            else
            {
                route.add(new Point2D(newX, y));
            }

            x = newX;
            y = newY;
        }
        return route;
    }
}
