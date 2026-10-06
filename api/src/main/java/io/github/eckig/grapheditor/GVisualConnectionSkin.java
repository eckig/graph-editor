package io.github.eckig.grapheditor;

import java.util.List;

import io.github.eckig.grapheditor.model.GConnection;
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
 * Skins of this type do not use joints, no joint skins are created for their connection. Skins displaying or
 * constraining joints must extend {@link GJointConnectionSkin} instead.
 * </p>
 *
 * @since 25.1.0
 */
public abstract class GVisualConnectionSkin extends GConnectionSkin
{

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
     * Returns the route this skin currently displays, e.g. for the minimap.
     *
     * <p>
     * The points are in the coordinate system of the graph editor view and ordered from source to target (inclusive).
     * Skins computing their own routing should override this and return the points they last drew. The default
     * returns an empty list, meaning "unknown".
     * </p>
     *
     * <p>
     * The minimap picks up the routes drawn in the first layout pass of the graph editor view after a model change
     * (command stack). Intermediate routes, e.g. while dragging, are not shown. If routes change at any other time
     * (e.g. asynchronous routing), the application should call {@code GraphEditorMinimap.redrawConnections()}.
     * </p>
     *
     * @return unmodifiable list of route points, never {@code null}
     */
    public List<Point2D> getRoutePoints()
    {
        return List.of();
    }
}
