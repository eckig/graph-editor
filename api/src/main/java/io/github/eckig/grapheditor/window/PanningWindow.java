package io.github.eckig.grapheditor.window;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Point2D;
import javafx.geometry.VerticalDirection;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.util.Optional;

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

    private ScrollBar hsb;
    private ScrollBar vsb;

    // auto scroll
    private static final Duration JUMP_PERIOD = Duration.millis(25);
    private static final double INSET_TO_BEGIN_SCROLL = 1;
    private Timeline timeline;
    private boolean isScrolling;
    private HorizontalDirection scrollX;
    private VerticalDirection scrollY;

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow()
    {
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        getChildren().add(scrollPane);

        scale.xProperty().bind(zoom);
        scale.yProperty().bind(zoom);

        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, _ -> endScrolling());
        addEventHandler(ZoomEvent.ZOOM, this::handleZoom);
        addEventFilter(ScrollEvent.SCROLL, event ->
        {
            if (event.isControlDown())
            {
                final double modifier = event.getDeltaY() > 1 ? 0.06 : -0.06;
                setZoom(getZoom() + modifier);
                event.consume();
            }
        });
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();
        scrollPane.resizeRelocate(0, 0, snapSizeX(getWidth()), snapSizeY(getHeight()));
    }

    private ScrollBar hsb()
    {
        if (hsb == null)
        {
            //filter out "VirtualScrollBar"
            hsb = (ScrollBar) scrollPane.lookupAll(".scroll-bar:horizontal")
                    .stream()
                    .filter(n -> n.getClass().equals(ScrollBar.class))
                    .findFirst()
                    .orElse(null);
        }
        return hsb;
    }

    private ScrollBar vsb()
    {
        if (vsb == null)
        {
            //filter out "VirtualScrollBar"
            vsb = (ScrollBar) scrollPane.lookupAll(".scroll-bar:vertical")
                    .stream()
                    .filter(n -> n.getClass().equals(ScrollBar.class))
                    .findFirst()
                    .orElse(null);
        }
        return vsb;
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

    private void handleMouseDragged(final MouseEvent event)
    {
        if (event.isPrimaryButtonDown())
        {
            final var cursorX = event.getX();
            final var cursorY = event.getY();
            if (cursorX <= INSET_TO_BEGIN_SCROLL)
            {
                scrollX = HorizontalDirection.LEFT;
            }
            else if (cursorX >= getWidth() - INSET_TO_BEGIN_SCROLL)
            {
                scrollX = HorizontalDirection.RIGHT;
            }
            else
            {
                scrollX = null;
            }

            if (cursorY <= INSET_TO_BEGIN_SCROLL)
            {
                scrollY = VerticalDirection.UP;
            }
            else if (cursorY >= getHeight() - INSET_TO_BEGIN_SCROLL)
            {
                scrollY = VerticalDirection.DOWN;
            }
            else
            {
                scrollY = null;
            }

            if (!isScrolling)
            {
                startScrolling();
            }
        }
    }

    private void startScrolling()
    {
        isScrolling = true;

        final KeyFrame frame = new KeyFrame(JUMP_PERIOD, _ ->
        {
            if (isScrolling)
            {
                final var hsb = hsb();
                final var vsb = vsb();
                final var unitIncrementX = hsb == null ? 0.01 : hsb.getUnitIncrement();
                final var unitIncrementY = vsb == null ? 0.01 : vsb.getUnitIncrement();
                if (scrollX == HorizontalDirection.LEFT)
                {
                    decrement(scrollPane.hvalueProperty(), unitIncrementX);
                }
                else if (scrollX == HorizontalDirection.RIGHT)
                {
                    increment(scrollPane.hvalueProperty(), unitIncrementX);
                }

                if (scrollY == VerticalDirection.UP)
                {
                    decrement(scrollPane.vvalueProperty(), unitIncrementY);
                }
                else if (scrollY == VerticalDirection.DOWN)
                {
                    increment(scrollPane.vvalueProperty(), unitIncrementY);
                }
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    /**
     * Stops the auto-scrolling.
     */
    private void endScrolling()
    {
        isScrolling = false;

        if (timeline != null)
        {
            timeline.stop();
        }
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

    private static double clampScrollValue(double value)
    {
        return Math.max(0.0, Math.min(value, 1.0));
    }

    private void increment(final DoubleProperty pPos, final double pUnitIncrement)
    {
        pPos.set(clampScrollValue(pPos.get() + pUnitIncrement));
    }

    private void decrement(final DoubleProperty pPos, final double pUnitIncrement)
    {
        pPos.set(clampScrollValue(pPos.get() - pUnitIncrement));
    }
}
