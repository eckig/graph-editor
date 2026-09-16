package io.github.eckig.grapheditor.window.skin;

import io.github.eckig.grapheditor.utils.GraphEventManager;
import io.github.eckig.grapheditor.utils.GraphInputGesture;
import io.github.eckig.grapheditor.window.PanningWindow;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Orientation;
import javafx.geometry.VerticalDirection;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Cursor;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import java.util.function.DoubleUnaryOperator;
import javafx.util.Duration;


/**
 * ScrollPane skin for usage within PanningWindow.
 * This is a modified copy of the default {@code javafx.scene.control.skin.ScrollPaneSkin}
 */
public class PanningWindowScrollPaneSkin extends SkinBase<ScrollPane>
{

    private final static boolean IS_TOUCH_SUPPORTED = Platform.isSupported(ConditionalFeature.INPUT_TOUCH);

    private static final Duration JUMP_PERIOD = Duration.millis(25);
    private static final double INSET_TO_BEGIN_SCROLL = 1;

    private static final double PAN_THRESHOLD = 0.5;

    /**
     * Zoom is multiplied by this per notch instead of being incremented.
     *
     * <p>
     * Perceived zoom is logarithmic, so a fixed summand feels far too coarse when
     * zoomed out and far too fine when zoomed in. A fixed factor gives every notch
     * the same perceived size and makes zooming in and out again land exactly back
     * on the original level.
     * </p>
     */
    private static final double ZOOM_STEP = 1.1;

    private final PanningWindow panningWindow;

    // state from the control
    private Node scrollNode;

    private double nodeWidth;
    private double nodeHeight;
    private boolean nodeSizeInvalid = true;

    private double posX;
    private double posY;

    // working state
    private double hsbHeight;
    private double vsbWidth;

    // substructure
    private final StackPane viewRect;
    private final StackPane viewContent;
    private double contentWidth;
    private double contentHeight;
    private final StackPane corner = new StackPane();
    private final ScrollBar hsb = new ScrollBar();
    private final ScrollBar vsb = new ScrollBar();

    private double pressX;
    private double pressY;
    private double ohvalue;
    private double ovvalue;
    private boolean dragDetected = false;

    /** {@code true} while this skin owns the active {@link GraphInputGesture#PAN} gesture */
    private boolean panning = false;

    /** kept so that {@link #dispose()} can unregister it again */
    private final EventHandler<KeyEvent> keyEventHandler = this::handleKeyEvent;

    /** kept so that {@link #dispose()} can restore the original scrollbar dispatchers */
    private EventDispatcher originalHsbEventDispatcher;
    private EventDispatcher originalVsbEventDispatcher;

    // auto scroll
    private Timeline timeline;
    private boolean isScrolling;
    private HorizontalDirection scrollX;
    private VerticalDirection scrollY;

    private final Rectangle clipRect = new Rectangle();

    private final InvalidationListener nodeListener = new InvalidationListener()
    {
        @Override
        public void invalidated(Observable valueModel)
        {
            if (!nodeSizeInvalid)
            {
                final Bounds scrollNodeBounds = scrollNode.getLayoutBounds();
                final double scrollNodeWidth = scrollNodeBounds.getWidth();
                final double scrollNodeHeight = scrollNodeBounds.getHeight();

                if ((scrollNodeWidth != 0.0 && nodeWidth != scrollNodeWidth) ||
                        (scrollNodeHeight != 0.0 && nodeHeight != scrollNodeHeight))
                {
                    getSkinnable().requestLayout();
                }
                else
                {
                    if (!dragDetected)
                    {
                        updateVerticalSB();
                        updateHorizontalSB();
                    }
                }
            }
        }
    };

    private final WeakInvalidationListener weakNodeListener = new WeakInvalidationListener(nodeListener);

    /*
     ** The content of the ScrollPane has just changed bounds, check scrollBar positions.
     */
    private final ChangeListener<Bounds> boundsChangeListener = new ChangeListener<>()
    {
        @Override
        public void changed(ObservableValue<? extends Bounds> observable, Bounds oldBounds, Bounds newBounds)
        {
            /*
             ** For a height change then we want to reduce viewport vertical jumping as much as possible.
             ** We set a new vsb value to try to keep the same content position at the top of the viewport
             */
            double oldHeight = oldBounds.getHeight();
            double newHeight = newBounds.getHeight();
            if (oldHeight > 0 && oldHeight != newHeight)
            {
                adjustScrollBarForResize(vsb, posY, snappedTopInset(), oldHeight, newHeight, contentHeight,
                        PanningWindowScrollPaneSkin.this::snapPositionY);
            }

            /*
             ** For a width change then we want to reduce viewport horizontal jumping as much as possible.
             ** We set a new hsb value to try to keep the same content position to the left of the viewport
             */
            double oldWidth = oldBounds.getWidth();
            double newWidth = newBounds.getWidth();
            if (oldWidth > 0 && oldWidth != newWidth)
            {
                adjustScrollBarForResize(hsb, posX, snappedLeftInset(), oldWidth, newWidth, contentWidth,
                        PanningWindowScrollPaneSkin.this::snapPositionX);
            }
        }
    };

    private final WeakChangeListener<Bounds> weakBoundsChangeListener = new WeakChangeListener<>(boundsChangeListener);

    /**
     * Creates a new ScrollPaneSkin instance
     *
     * @param window
     *         PanningWindow
     * @param control
     *         The control that this skin should be installed onto.
     */
    public PanningWindowScrollPaneSkin(final PanningWindow window, final ScrollPane control)
    {
        super(control);
        panningWindow = window;

        // install default input map for the ScrollPane control
        // NOTE: the handler has to sit on the PanningWindow, not on the ScrollPane.
        // The window is the focusable element, so the ScrollPane is never on the
        // event dispatch path and would never see a key event.
        window.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);

        scrollNode = control.getContent();

        viewRect = new StackPane()
        {
            @Override
            protected void layoutChildren()
            {
                viewContent.resize(getWidth(), getHeight());
            }
        };
        viewRect.getStyleClass().add("viewport");

        viewRect.setClip(clipRect);

        vsb.setMin(control.getVmin());
        vsb.setMax(control.getVmax());
        vsb.setOrientation(Orientation.VERTICAL);

        hsb.setMin(control.getHmin());
        hsb.setMax(control.getHmax());

        corner.getStyleClass().setAll("corner");

        viewContent = new StackPane()
        {
            @Override
            public void requestLayout()
            {
                // if scrollNode requested layout, will want to recompute
                nodeSizeInvalid = true;

                super.requestLayout(); // add as layout root for next layout pass

                PanningWindowScrollPaneSkin.this.getSkinnable().requestLayout();
            }

            @Override
            protected void layoutChildren()
            {
                if (nodeSizeInvalid)
                {
                    computeScrollNodeSize();
                }
                if (scrollNode != null)
                {
                    scrollNode.relocate(0, 0);
                }
            }
        };
        viewRect.getChildren().add(viewContent);

        getChildren().addAll(viewRect, vsb, hsb, corner);

        registerInvalidationListener(vsb.valueProperty(), (_) ->
        {
            posY = clampScrollValue(vsb.getValue());
            updatePosY();
        });

        registerInvalidationListener(hsb.valueProperty(), (_) ->
        {
            posX = clampScrollValue(hsb.getValue());
            updatePosX();
        });

        viewRect.addEventFilter(MouseEvent.MOUSE_PRESSED, e ->
        {
            pressX = e.getX();
            pressY = e.getY();
            ohvalue = hsb.getValue();
            ovvalue = vsb.getValue();

            if (isPanTrigger(e) && activatePanGesture(e))
            {
                panningWindow.setGestureCursor(Cursor.CLOSED_HAND);
                // consume so that no other gesture (e.g. rubber band selection) is started
                e.consume();
            }
        });

        viewRect.setOnDragDetected(_ -> dragDetected = true);

        viewRect.addEventFilter(MouseEvent.MOUSE_RELEASED, e ->
        {
            endScrolling();
            dragDetected = false;

            if (finishPanGesture())
            {
                panningWindow.setGestureCursor(panningWindow.isPanModeArmed() ? Cursor.OPEN_HAND : null);
                e.consume();
            }

            if (posY > getSkinnable().getVmax() || posY < getSkinnable().getVmin() ||
                    posX > getSkinnable().getHmax() || posX < getSkinnable().getHmin())
            {
                startContentsToViewport();
            }
        });

        viewRect.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDraggedForPanning);
        viewRect.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDraggedForAutoScroll);

        /*
         ** don't allow the ScrollBar to handle the ScrollEvent,
         ** In a ScrollPane a vertical scroll should scroll on the vertical only,
         ** whereas in a horizontal ScrollBar it can scroll horizontally.
         */
        // block the event from being passed down to children
        final EventDispatcher blockEventDispatcher = (event, _) -> event;
        // block ScrollEvent from being passed down to scrollbar's skin
        final EventDispatcher oldHsbEventDispatcher = hsb.getEventDispatcher();
        originalHsbEventDispatcher = oldHsbEventDispatcher;
        hsb.setEventDispatcher((event, tail) ->
        {
            if (event.getEventType() == ScrollEvent.SCROLL &&
                    !((ScrollEvent) event).isDirect())
            {
                tail = tail.prepend(blockEventDispatcher);
                tail = tail.prepend(oldHsbEventDispatcher);
                return tail.dispatchEvent(event);
            }
            return oldHsbEventDispatcher.dispatchEvent(event, tail);
        });
        // block ScrollEvent from being passed down to scrollbar's skin
        final EventDispatcher oldVsbEventDispatcher = vsb.getEventDispatcher();
        originalVsbEventDispatcher = oldVsbEventDispatcher;
        vsb.setEventDispatcher((event, tail) ->
        {
            if (event.getEventType() == ScrollEvent.SCROLL &&
                    !((ScrollEvent) event).isDirect())
            {
                tail = tail.prepend(blockEventDispatcher);
                tail = tail.prepend(oldVsbEventDispatcher);
                return tail.dispatchEvent(event);
            }
            return oldVsbEventDispatcher.dispatchEvent(event, tail);
        });

        viewRect.addEventHandler(ScrollEvent.SCROLL, event ->
        {
            if (event.isControlDown())
            {
                // note: trackpads and hi-res wheels report fractional deltas, so this
                // has to test against 0 and not against 1
                final double factor = event.getDeltaY() > 0 ? ZOOM_STEP : 1 / ZOOM_STEP;
                zoomAt(factor, event.getSceneX(), event.getSceneY());
                event.consume();
                return;
            }

            // if we're completely visible then do nothing.... we only consume an event that we've used.
            if (vsb.getVisibleAmount() < vsb.getMax())
            {
                double vRange = getSkinnable().getVmax() - getSkinnable().getVmin();
                double hDelta = nodeHeight - contentHeight;
                double vPixelValue = hDelta > 0.0 ? vRange / hDelta : 0.0;
                double newValue = vsb.getValue() + (-event.getDeltaY()) * vPixelValue;
                if (!IS_TOUCH_SUPPORTED)
                {
                    if ((event.getDeltaY() > 0.0 && vsb.getValue() > vsb.getMin()) ||
                            (event.getDeltaY() < 0.0 && vsb.getValue() < vsb.getMax()))
                    {
                        vsb.setValue(newValue);
                        event.consume();
                    }
                }
                else
                {
                    vsb.setValue(newValue);
                    if ((newValue > vsb.getMax() || newValue < vsb.getMin()))
                    {
                        startContentsToViewport();
                    }
                    event.consume();
                }
            }

            if (hsb.getVisibleAmount() < hsb.getMax())
            {
                double hRange = getSkinnable().getHmax() - getSkinnable().getHmin();
                double wDelta = nodeWidth - contentWidth;
                double hPixelValue = wDelta > 0.0 ? hRange / wDelta : 0.0;
                double newValue = hsb.getValue() + (-event.getDeltaX()) * hPixelValue;
                if (!IS_TOUCH_SUPPORTED)
                {
                    if ((event.getDeltaX() > 0.0 && hsb.getValue() > hsb.getMin()) ||
                            (event.getDeltaX() < 0.0 && hsb.getValue() < hsb.getMax()))
                    {
                        hsb.setValue(newValue);
                        event.consume();
                    }
                }
                else
                {
                    hsb.setValue(newValue);

                    if ((newValue > hsb.getMax() || newValue < hsb.getMin()))
                    {
                        startContentsToViewport();
                    }
                    event.consume();
                }
            }
        });

        hsb.setValue(control.getHvalue());
        vsb.setValue(control.getVvalue());

        registerChangeListener(control.contentProperty(), _ ->
        {
            if (scrollNode != getSkinnable().getContent())
            {
                if (scrollNode != null)
                {
                    scrollNode.layoutBoundsProperty().removeListener(weakNodeListener);
                    scrollNode.layoutBoundsProperty().removeListener(weakBoundsChangeListener);
                    viewContent.getChildren().remove(scrollNode);
                }
                scrollNode = getSkinnable().getContent();
                if (scrollNode != null)
                {
                    doComputeScrollNodeSize();
                    viewContent.getChildren().setAll(scrollNode);
                    scrollNode.layoutBoundsProperty().addListener(weakNodeListener);
                    scrollNode.layoutBoundsProperty().addListener(weakBoundsChangeListener);
                }
            }
            getSkinnable().requestLayout();
        });

        registerChangeListener(control.hvalueProperty(), _ -> hsb.setValue(getSkinnable().getHvalue()));
        registerChangeListener(control.vvalueProperty(), _ -> vsb.setValue(getSkinnable().getVvalue()));
    }

    private void setContentPosX(final double pValue)
    {
        hsb.setValue(pValue);
        getSkinnable().requestLayout();
    }

    private void setContentPosY(final double pValue)
    {
        vsb.setValue(pValue);
        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h)
    {
        final var control = getSkinnable();
        if (control == null)
        {
            // already disposed, but a layout pulse was still queued
            return;
        }
        final var padding = control.getPadding();
        final var rightPadding = snapSizeX(padding.getRight());
        final var leftPadding = snapSizeX(padding.getLeft());
        final var topPadding = snapSizeY(padding.getTop());
        final var bottomPadding = snapSizeY(padding.getBottom());

        contentWidth = w;
        contentHeight = h;

        // we want the scrollbars to go right to the border
        double hsbWidth;
        double vsbHeight;

        computeScrollNodeSize();
        computeScrollBarSize();

        contentWidth = w - vsbWidth;
        hsbWidth = w + leftPadding + rightPadding - vsbWidth;
        contentHeight = h - hsbHeight;
        vsbHeight = h + topPadding + bottomPadding - hsbHeight;

        // figure out the content area that is to be filled
        double cx = snappedLeftInset() - leftPadding;
        double cy = snappedTopInset() - topPadding;

        vsb.resizeRelocate(snappedLeftInset() + w - vsbWidth + (rightPadding < 1 ? 0 : rightPadding - 1),
                cy, vsbWidth, vsbHeight);
        updateVerticalSB();

        hsb.resizeRelocate(cx, snappedTopInset() + h - hsbHeight + (bottomPadding < 1 ? 0 : bottomPadding - 1),
                hsbWidth, hsbHeight);
        updateHorizontalSB();

        viewRect.resizeRelocate(snappedLeftInset(), snappedTopInset(), snapSizeX(contentWidth),
                snapSizeY(contentHeight));
        resetClip();

        corner.setVisible(true);
        double cornerWidth = vsbWidth;
        double cornerHeight = hsbHeight;
        corner.resizeRelocate(snapPositionX(vsb.getLayoutX()), snapPositionY(hsb.getLayoutY()),
                snapSizeX(cornerWidth), snapSizeY(cornerHeight));
        control.setViewportBounds(
                new BoundingBox(snapPositionX(viewContent.getLayoutX()), snapPositionY(viewContent.getLayoutY()),
                        snapSizeX(contentWidth), snapSizeY(contentHeight)));
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters)
    {
        return switch (attribute)
        {
            case VERTICAL_SCROLLBAR-> vsb;
            case HORIZONTAL_SCROLLBAR-> hsb;
            default-> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    private void computeScrollNodeSize()
    {
        if (scrollNode != null)
        {
            doComputeScrollNodeSize();
            nodeSizeInvalid = false;
        }
    }

    private void doComputeScrollNodeSize()
    {
        nodeWidth = snapSizeX(scrollNode.getLayoutBounds().getWidth() * panningWindow.getZoom());
        nodeHeight = snapSizeY(scrollNode.getLayoutBounds().getHeight() * panningWindow.getZoom());
    }

    private void computeScrollBarSize()
    {
        vsbWidth = snapSizeX(vsb.prefWidth(-1));
        hsbHeight = snapSizeY(hsb.prefHeight(-1));
    }

    private void updateHorizontalSB()
    {
        double contentRatio = nodeWidth * (hsb.getMax() - hsb.getMin());
        if (contentRatio > 0.0)
        {
            hsb.setVisibleAmount(contentWidth / contentRatio);
            hsb.setBlockIncrement(0.9 * hsb.getVisibleAmount());
            hsb.setUnitIncrement(0.1 * hsb.getVisibleAmount());
        }
        else
        {
            hsb.setVisibleAmount(0.0);
            hsb.setBlockIncrement(0.0);
            hsb.setUnitIncrement(0.0);
        }

        updatePosX();
    }

    private void updateVerticalSB()
    {
        double contentRatio = nodeHeight * (vsb.getMax() - vsb.getMin());
        if (contentRatio > 0.0)
        {
            vsb.setVisibleAmount(contentHeight / contentRatio);
            vsb.setBlockIncrement(0.9 * vsb.getVisibleAmount());
            vsb.setUnitIncrement(0.1 * vsb.getVisibleAmount());
        }
        else
        {
            vsb.setVisibleAmount(0.0);
            vsb.setBlockIncrement(0.0);
            vsb.setUnitIncrement(0.0);
        }

        updatePosY();
    }

    private void updatePosX()
    {
        final var sp = getSkinnable();
        var x = posX;
        var hsbRange = hsb.getMax() - hsb.getMin();
        var minX = hsbRange > 0 ? -x / hsbRange * (nodeWidth - contentWidth) : 0;
        minX = Math.min(minX, 0);
        viewContent.setLayoutX(snapPositionX(minX));
        if (!sp.hvalueProperty().isBound())
        {
            sp.setHvalue(clampScrollValue(posX));
        }
    }

    private void updatePosY()
    {
        final var sp = getSkinnable();
        var vsbRange = vsb.getMax() - vsb.getMin();
        var minY = vsbRange > 0 ? -posY / vsbRange * (nodeHeight - contentHeight) : 0;
        minY = Math.min(minY, 0);
        viewContent.setLayoutY(snapPositionY(minY));
        if (!sp.vvalueProperty().isBound())
        {
            sp.setVvalue(clampScrollValue(posY));
        }
    }

    private void resetClip()
    {
        clipRect.setWidth(snapSizeX(contentWidth));
        clipRect.setHeight(snapSizeY(contentHeight));
    }

    private void startContentsToViewport()
    {
        double newPosX = posX;
        double newPosY = posY;

        if (posY > getSkinnable().getVmax())
        {
            newPosY = getSkinnable().getVmax();
        }
        else if (posY < getSkinnable().getVmin())
        {
            newPosY = getSkinnable().getVmin();
        }

        if (posX > getSkinnable().getHmax())
        {
            newPosX = getSkinnable().getHmax();
        }
        else if (posX < getSkinnable().getHmin())
        {
            newPosX = getSkinnable().getHmin();
        }
        setContentPosX(newPosX);
        setContentPosY(newPosY);
    }

    /**
     * Scrolls the viewport with the keyboard.
     *
     * <p>
     * Only unmodified navigation keys are handled, and only those are consumed.
     * Every other key press is left untouched so that the application (and third
     * party code) can use the keyboard for its own shortcuts.
     * </p>
     *
     * @param e
     *         a {@code KEY_PRESSED} {@link KeyEvent}
     */
    private void handleKeyEvent(final KeyEvent e)
    {
        if (e.isShortcutDown() || e.isControlDown() || e.isAltDown() || e.isMetaDown() || e.isShiftDown())
        {
            // modified key strokes belong to the application
            return;
        }

        // under RIGHT_TO_LEFT the horizontal axis is mirrored, so the arrow keys
        // have to be swapped to keep moving the viewport in the direction the
        // user actually points at
        final boolean rightToLeft =
                panningWindow.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;

        switch (e.getCode())
        {
            case LEFT:
                if (rightToLeft)
                {
                    hsb.increment();
                }
                else
                {
                    hsb.decrement();
                }
                break;

            case RIGHT:
                if (rightToLeft)
                {
                    hsb.decrement();
                }
                else
                {
                    hsb.increment();
                }
                break;

            case UP, PAGE_UP:
                vsb.decrement();
                break;

            case DOWN, PAGE_DOWN:
                vsb.increment();
                break;

            case HOME:
                getSkinnable().setHvalue(getSkinnable().getHmin());
                getSkinnable().setVvalue(getSkinnable().getVmin());
                break;

            case END:
                getSkinnable().setHvalue(getSkinnable().getHmax());
                getSkinnable().setVvalue(getSkinnable().getVmax());
                break;

            default:
                // not ours - do NOT consume it
                return;
        }
        e.consume();
    }

    /**
     * Keeps the content position as stable as possible while the content is resized.
     *
     * <p>
     * Every division in here can degenerate: the scrollbar range can be {@code 0},
     * and the projected new position can be {@code 0} - for example while the
     * viewport sits at the very start, or when the content ends up exactly as large
     * as the viewport. Unguarded this produced {@code NaN} (silently swallowed by
     * the comparisons) or {@code Infinity}, which made the viewport jump to the very
     * end. In those cases the current value is simply kept.
     * </p>
     *
     * @param pBar
     *         the {@link ScrollBar} of the axis being adjusted
     * @param pPos
     *         the current position of that axis
     * @param pInset
     *         the snapped inset of that axis
     * @param pOldExtent
     *         the content size of that axis before the resize
     * @param pNewExtent
     *         the content size of that axis after the resize
     * @param pViewportExtent
     *         the viewport size of that axis
     * @param pSnap
     *         the snapping function of that axis
     */
    private void adjustScrollBarForResize(final ScrollBar pBar, final double pPos, final double pInset,
            final double pOldExtent, final double pNewExtent, final double pViewportExtent,
            final DoubleUnaryOperator pSnap)
    {
        final double range = pBar.getMax() - pBar.getMin();
        if (range <= 0.0)
        {
            return;
        }

        final double oldPosition = pSnap.applyAsDouble(pInset - pPos / range * (pOldExtent - pViewportExtent));
        final double newPosition = pSnap.applyAsDouble(pInset - pPos / range * (pNewExtent - pViewportExtent));
        if (newPosition == 0.0)
        {
            return;
        }

        final double newValue = oldPosition / newPosition * pBar.getValue();
        if (Double.isFinite(newValue))
        {
            pBar.setValue(clampScrollValue(newValue));
        }
    }

    /**
     * Changes the zoom level while keeping the point under the cursor in place.
     *
     * <p>
     * Without this the viewport zooms towards the content origin, which makes the
     * area the user is actually looking at drift away.
     * </p>
     *
     * @param pFactor
     *         the factor to multiply the current zoom with
     * @param pSceneX
     *         the scene x coordinate to keep fixed
     * @param pSceneY
     *         the scene y coordinate to keep fixed
     */
    private void zoomAt(final double pFactor, final double pSceneX, final double pSceneY)
    {
        final double oldZoom = panningWindow.getZoom();
        if (scrollNode == null)
        {
            panningWindow.setZoom(oldZoom * pFactor);
            return;
        }

        // remember which point of the content sits under the cursor
        final Point2D anchor = scrollNode.sceneToLocal(pSceneX, pSceneY);

        panningWindow.setZoom(oldZoom * pFactor);
        if (panningWindow.getZoom() == oldZoom)
        {
            // clamped, nothing moved
            return;
        }

        // the scaled content size feeds into the layout, so it has to be redone
        // before we can measure where the anchor ended up
        nodeSizeInvalid = true;
        getSkinnable().requestLayout();
        getSkinnable().layout();

        final Point2D moved = scrollNode.localToScene(anchor);
        scrollBackBy(hsb, moved.getX() - pSceneX, nodeWidth - contentWidth);
        scrollBackBy(vsb, moved.getY() - pSceneY, nodeHeight - contentHeight);
    }

    private static void scrollBackBy(final ScrollBar pBar, final double pDrift, final double pOverflow)
    {
        if (pOverflow > 0.0 && Double.isFinite(pDrift))
        {
            pBar.setValue(clampScrollValue(pBar.getValue() + pDrift / pOverflow * (pBar.getMax() - pBar.getMin())));
        }
    }

    private static double clampScrollValue(double value)
    {
        return Math.max(0.0, Math.min(value, 1.0));
    }

    /**
     * Determines whether the given event should start a panning gesture.
     *
     * <p>
     * Panning is triggered by the middle mouse button (the universal convention)
     * or by the primary mouse button while {@code SPACE} is held down. The
     * secondary mouse button deliberately does <b>not</b> pan so that it stays
     * available for third party context menus.
     * </p>
     *
     * @param e
     *         a {@link MouseEvent}
     * @return {@code true} if the event should pan the view
     */
    private boolean isPanTrigger(final MouseEvent e)
    {
        if (e.isMiddleButtonDown())
        {
            return true;
        }
        if (e.isPrimaryButtonDown())
        {
            // Note: IS_TOUCH_SUPPORTED alone is not sufficient, it is also true for plain
            // mice on touch capable hardware - which would break rubber band selection there.
            return panningWindow.isPanModeArmed() || IS_TOUCH_SUPPORTED && e.isSynthesized();
        }
        return false;
    }

    private boolean activatePanGesture(final MouseEvent e)
    {
        final GraphEventManager manager = panningWindow.getEventManager();
        if (manager == null)
        {
            // no arbitration available (plain PanningWindow usage): pan unconditionally
            panning = true;
            return true;
        }
        panning = manager.activateGesture(GraphInputGesture.PAN, e, this);
        return panning;
    }

    private boolean finishPanGesture()
    {
        if (!panning)
        {
            return false;
        }
        panning = false;
        final GraphEventManager manager = panningWindow.getEventManager();
        return manager == null || manager.finishGesture(GraphInputGesture.PAN, this);
    }

    @Override
    public void dispose()
    {
        // never leave a dangling PAN gesture behind, it would block every other gesture
        finishPanGesture();

        // an INDEFINITE Timeline keeps this skin alive and keeps firing forever
        endScrolling();
        timeline = null;

        panningWindow.removeEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        if (originalHsbEventDispatcher != null)
        {
            hsb.setEventDispatcher(originalHsbEventDispatcher);
            originalHsbEventDispatcher = null;
        }
        if (originalVsbEventDispatcher != null)
        {
            vsb.setEventDispatcher(originalVsbEventDispatcher);
            originalVsbEventDispatcher = null;
        }

        super.dispose();
    }

    private void handleMouseDraggedForPanning(final MouseEvent e)
    {
        if (!panning)
        {
            // not our gesture - leave the event alone so that e.g. rubber band
            // selection and third party handlers still receive it
            return;
        }

        final var deltaX = pressX - e.getX();
        final var deltaY = pressY - e.getY();
        // each axis has to be scaled by its OWN overflow, otherwise panning is
        // too fast or too slow as soon as the content is not square
        handleMousePressedForPanning(hsb, deltaX, ohvalue, nodeWidth - viewRect.getWidth());
        handleMousePressedForPanning(vsb, deltaY, ovvalue, nodeHeight - viewRect.getHeight());

        e.consume();
    }

    /**
     * @param pBar
     *         the {@link ScrollBar} of the axis being panned
     * @param pDelta
     *         the cursor movement along that axis
     * @param pOValue
     *         the scrollbar value when the gesture started
     * @param pOverflow
     *         the amount of content of that axis that does not fit into the viewport
     */
    private void handleMousePressedForPanning(final ScrollBar pBar, final double pDelta, final double pOValue,
            final double pOverflow)
    {
        if (pOverflow > 0.0 && pBar.getVisibleAmount() > 0.0 && pBar.getVisibleAmount() < pBar.getMax() &&
                Math.abs(pDelta) > PAN_THRESHOLD)
        {
            var newHVal = (pOValue + pDelta / pOverflow * (pBar.getMax() - pBar.getMin()));
            if (!IS_TOUCH_SUPPORTED)
            {
                if (newHVal > pBar.getMax())
                {
                    newHVal = pBar.getMax();
                }
                else if (newHVal < pBar.getMin())
                {
                    newHVal = pBar.getMin();
                }
                pBar.setValue(newHVal);
            }
            else
            {
                pBar.setValue(newHVal);
            }
        }
    }

    private void handleMouseDraggedForAutoScroll(final MouseEvent e)
    {
        if (e.isPrimaryButtonDown() && !panning)
        {
            final var cursorX = e.getX();
            final var cursorY = e.getY();
            if (cursorX <= INSET_TO_BEGIN_SCROLL)
            {
                scrollX = HorizontalDirection.LEFT;
            }
            else if (cursorX >= viewRect.getWidth() - INSET_TO_BEGIN_SCROLL)
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
            else if (cursorY >= viewRect.getHeight() - INSET_TO_BEGIN_SCROLL)
            {
                scrollY = VerticalDirection.DOWN;
            }
            else
            {
                scrollY = null;
            }

            if ((scrollX != null || scrollY != null) && !isScrolling)
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
                if (scrollX == HorizontalDirection.LEFT)
                {
                    hsb.decrement();
                }
                else if (scrollX == HorizontalDirection.RIGHT)
                {
                    hsb.increment();
                }

                if (scrollY == VerticalDirection.UP)
                {
                    vsb.decrement();
                }
                else if (scrollY == VerticalDirection.DOWN)
                {
                    vsb.increment();
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

}
