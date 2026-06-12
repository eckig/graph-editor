package io.github.eckig.grapheditor.window;


import io.github.eckig.grapheditor.window.skin.PanningWindowScrollPaneSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;

/**
 * A window over a large {@link Region} of content.
 *
 * <p>
 * This window can be panned around relative to its content. Only the parts of
 * the content that are inside the window will be rendered. Everything outside
 * it is clipped.
 * </p>
 */
public class PanningWindow extends Region
{

    private final ScrollPane scrollPane = new ScrollPane();

    // zoom
    private static final float SCALE_MIN = 0.5f;
    private static final float SCALE_MAX = 1.5f;
    private final DoubleProperty zoom = new SimpleDoubleProperty(1);
    private final Scale scale = new Scale();

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow()
    {
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setSkin(new PanningWindowScrollPaneSkin(this, scrollPane));

        getChildren().add(scrollPane);

        scale.xProperty().bind(zoom);
        scale.yProperty().bind(zoom);

        addEventHandler(ZoomEvent.ZOOM, this::handleZoom);
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();
        scrollPane.resizeRelocate(0, 0, snapSizeX(getWidth()), snapSizeY(getHeight()));
    }

    /**
     * @return the current scrollbar values as {@link Point2D}
     */
    public Point2D getScrollPosition()
    {
        return new Point2D(scrollPane.getHvalue(), scrollPane.getVvalue());
    }

    /**
     * set the scrollbar values to the given coordinate
     * @param pPoint scrollbar position
     */
    public void scrollTo(final Point2D pPoint)
    {
        if (pPoint != null)
        {
            scrollPane.setHvalue(pPoint.getX());
            scrollPane.setVvalue(pPoint.getY());
        }
    }

    /**
     * set the horizontal scrollbar to the given value
     * @param pX scrollbar position
     */
    public void scrollToX(final double pX)
    {
        scrollPane.setHvalue(pX);
    }

    /**
     * set the vertical scrollbar to the given value
     * @param pY scrollbar position
     */
    public void scrollToY(final double pY)
    {
        scrollPane.setVvalue(pY);
    }

    /**
     * normalize the given {@link Point2D} (within the given {@link BoundingBox bounds} to a value between 0 and 1,
     * suitable for setting a scrollbar value
     *
     * @param pPoint
     *         point
     * @param pBounds
     *         bounds
     * @return normalized point
     */
    public static Point2D normalize(final Point2D pPoint, final BoundingBox pBounds)
    {
        if (pPoint == null || pBounds == null)
        {
            return Point2D.ZERO;
        }
        final var x = (pPoint.getX() - pBounds.getMinX()) / (pBounds.getMaxX() - pBounds.getMinX());
        final var y = (pPoint.getY() - pBounds.getMinY()) / (pBounds.getMaxY() - pBounds.getMinY());
        return new Point2D(x, y);
    }

    /**
     * denormalize the given {@link Point2D} from a value between 0 and 1 to a coordinate (within the given
     * {@link BoundingBox bounds}.
     *
     * @param pPoint
     *         point
     * @param pBounds
     *         bounds
     * @return normalized point
     */
    public static Point2D denormalize(final Point2D pPoint, final BoundingBox pBounds)
    {
        if (pPoint == null || pBounds == null)
        {
            return Point2D.ZERO;
        }
        final var x = pPoint.getX() * (pBounds.getMaxX() - pBounds.getMinX()) + pBounds.getMinX();
        final var y = pPoint.getY() * (pBounds.getMaxY() - pBounds.getMinY()) + pBounds.getMinY();
        return new Point2D(x, y);
    }

    /**
     * @return the x coordinate of the window relative to the top-left corner of the content.
     */
    public double getContentX()
    {
        return scrollPane.getViewportBounds().getMinX();
    }

    /**
     * @return the y coordinate of the window relative to the top-left corner of the content.
     */
    public double getContentY()
    {
        return scrollPane.getViewportBounds().getMinY();
    }

    /**
     * Set new zoom level
     *
     * @param pZoom
     *         new zoom factor
     */
    public void setZoom(final double pZoom)
    {
        final double oldZoomLevel = getZoom();
        final double newZoomLevel = constrainZoom(pZoom);

        if (newZoomLevel != oldZoomLevel)
        {
            final var x = scrollPane.getHvalue();
            final var y = scrollPane.getVvalue();
            zoom.set(newZoomLevel);
            scrollTo(new Point2D(x, y));
        }
    }

    /**
     * @return current zoom factor
     */
    public double getZoom()
    {
        return zoom.get();
    }

    private static double constrainZoom(final double pZoom)
    {
        final double zoom = Math.round(pZoom * 100.0) / 100.0;
        if (zoom <= 1.02 && zoom >= 0.98)
        {
            return 1.0;
        }
        return Math.min(Math.max(zoom, SCALE_MIN), SCALE_MAX);
    }

    @Override
    public ObservableList<Node> getChildren()
    {
        return super.getChildren();
    }

    /**
     * Sets the content of the panning window.
     *
     * <p>
     * Note that the content's {@code managed} attribute will be set to false. Its size must therefore be set manually
     * using the {@code resize()} method of the {@link Node} class.
     * </p>
     *
     * @param pContent
     *         the {@link Region} to be displayed inside the panning window
     */
    protected void setContent(final Region pContent)
    {
        final var oldContent = scrollPane.getContent();
        if (oldContent instanceof Group g)
        {
            g.getChildren().forEach(n -> n.getTransforms().remove(scale));
        }

        if (pContent != null)
        {
            pContent.getTransforms().add(scale);
            scrollPane.setContent((new Group(pContent)));
        }
        else
        {
            scrollPane.setContent(null);
        }
    }

    private void handleZoom(final ZoomEvent pEvent)
    {
        final double newZoomLevel = getZoom() * pEvent.getZoomFactor();
        setZoom(newZoomLevel);
        pEvent.consume();
    }

    /**
     * The actual Bounds of the ScrollPane Viewport.
     * This is the Bounds of the content node.
     */
    public ObjectProperty<Bounds> viewportBoundsProperty()
    {
        return scrollPane.viewportBoundsProperty();
    }

    /**
     * The actual Bounds of the ScrollPane Viewport.
     * This is the Bounds of the content node.
     */
    public Bounds getViewportBounds()
    {
        return viewportBoundsProperty().get();
    }
}
