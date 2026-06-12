package io.github.eckig.grapheditor.window.skin;

import io.github.eckig.grapheditor.window.PanningWindow;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventDispatcher;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VerticalDirection;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
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

    private static final double DEFAULT_PREF_SIZE = 100.0;

    private static final double DEFAULT_MIN_SIZE = 36.0;

    private static final double PAN_THRESHOLD = 0.5;

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
    private Cursor saveCursor = null;
    private boolean dragDetected = false;

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
                double oldPositionY = (snapPositionY(
                        snappedTopInset() - posY / (vsb.getMax() - vsb.getMin()) * (oldHeight - contentHeight)));
                double newPositionY = (snapPositionY(
                        snappedTopInset() - posY / (vsb.getMax() - vsb.getMin()) * (newHeight - contentHeight)));

                double newValueY = (oldPositionY / newPositionY) * vsb.getValue();
                if (newValueY < 0.0)
                {
                    vsb.setValue(0.0);
                }
                else if (newValueY < 1.0)
                {
                    vsb.setValue(newValueY);
                }
                else if (newValueY > 1.0)
                {
                    vsb.setValue(1.0);
                }
            }

            /*
             ** For a width change then we want to reduce viewport horizontal jumping as much as possible.
             ** We set a new hsb value to try to keep the same content position to the left of the viewport
             */
            double oldWidth = oldBounds.getWidth();
            double newWidth = newBounds.getWidth();
            if (oldWidth > 0 && oldWidth != newWidth)
            {
                double oldPositionX = (snapPositionX(
                        snappedLeftInset() - posX / (hsb.getMax() - hsb.getMin()) * (oldWidth - contentWidth)));
                double newPositionX = (snapPositionX(
                        snappedLeftInset() - posX / (hsb.getMax() - hsb.getMin()) * (newWidth - contentWidth)));

                double newValueX = (oldPositionX / newPositionX) * hsb.getValue();
                if (newValueX < 0.0)
                {
                    hsb.setValue(0.0);
                }
                else if (newValueX < 1.0)
                {
                    hsb.setValue(newValueX);
                }
                else if (newValueX > 1.0)
                {
                    hsb.setValue(1.0);
                }
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
        control.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyEvent);

        scrollNode = control.getContent();

        viewRect = new StackPane()
        {
            @Override
            protected void layoutChildren()
            {
                viewContent.resize(getWidth(), getHeight());
            }
        };
        // prevent requestLayout requests from within scrollNode from percolating up
        viewRect.setManaged(false);
        viewRect.setCache(true);
        viewRect.getStyleClass().add("viewport");

        viewRect.setClip(clipRect);

        vsb.setOrientation(Orientation.VERTICAL);

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
                if (scrollNode != null && scrollNode.isResizable())
                {
                    scrollNode.resize(snapSizeX(nodeWidth), snapSizeY(nodeHeight));
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

        viewRect.setOnMousePressed(e ->
        {
            pressX = e.getX();
            pressY = e.getY();
            ohvalue = hsb.getValue();
            ovvalue = vsb.getValue();
        });

        viewRect.setOnDragDetected(_ ->
        {
            dragDetected = true;
            if (saveCursor == null)
            {
                saveCursor = getSkinnable().getCursor();
                if (saveCursor == null)
                {
                    saveCursor = Cursor.DEFAULT;
                }
                getSkinnable().setCursor(Cursor.MOVE);
                getSkinnable().requestLayout();
            }
        });

        viewRect.addEventFilter(MouseEvent.MOUSE_RELEASED, _ ->
        {
            endScrolling();
            if (dragDetected)
            {
                if (saveCursor != null)
                {
                    getSkinnable().setCursor(saveCursor);
                    saveCursor = null;
                    getSkinnable().requestLayout();
                }
                dragDetected = false;
            }

            if ((posY > getSkinnable().getVmax() || posY < getSkinnable().getVmin() ||
                    posX > getSkinnable().getHmax() || posX < getSkinnable().getHmin()))
            {
                startContentsToViewport();
            }
        });

        viewRect.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDraggedForAutoScroll);
        viewRect.setOnMouseDragged(this::handleMouseDraggedForPanning);

        /*
         ** don't allow the ScrollBar to handle the ScrollEvent,
         ** In a ScrollPane a vertical scroll should scroll on the vertical only,
         ** whereas in a horizontal ScrollBar it can scroll horizontally.
         */
        // block the event from being passed down to children
        final EventDispatcher blockEventDispatcher = (event, _) -> event;
        // block ScrollEvent from being passed down to scrollbar's skin
        final EventDispatcher oldHsbEventDispatcher = hsb.getEventDispatcher();
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
                final double modifier = event.getDeltaY() > 1 ? 0.06 : -0.06;
                panningWindow.setZoom(panningWindow.getZoom() + modifier);
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
                    nodeWidth = snapSizeX(scrollNode.getLayoutBounds().getWidth());
                    nodeHeight = snapSizeY(scrollNode.getLayoutBounds().getHeight());
                    viewContent.getChildren().setAll(scrollNode);
                    scrollNode.layoutBoundsProperty().addListener(weakNodeListener);
                    scrollNode.layoutBoundsProperty().addListener(weakBoundsChangeListener);
                }
            }
            getSkinnable().requestLayout();
        });

        registerChangeListener(control.hvalueProperty(), _ -> hsb.setValue(getSkinnable().getHvalue()));
        registerChangeListener(control.hmaxProperty(), _ -> hsb.setMax(getSkinnable().getHmax()));
        registerChangeListener(control.hminProperty(), _ -> hsb.setMin(getSkinnable().getHmin()));
        registerChangeListener(control.vvalueProperty(), _ -> vsb.setValue(getSkinnable().getVvalue()));
        registerChangeListener(control.vmaxProperty(), _ -> vsb.setMax(getSkinnable().getVmax()));
        registerChangeListener(control.vminProperty(), _ -> vsb.setMin(getSkinnable().getVmin()));

        registerChangeListener(control.prefViewportWidthProperty(), _ -> getSkinnable().requestLayout());
        registerChangeListener(control.prefViewportHeightProperty(), _ -> getSkinnable().requestLayout());
        registerChangeListener(control.minViewportWidthProperty(), _ -> getSkinnable().requestLayout());
        registerChangeListener(control.minViewportHeightProperty(), _ -> getSkinnable().requestLayout());
    }

    private final DoubleProperty contentPosX = new DoublePropertyBase()
    {
        @Override
        protected void invalidated()
        {
            hsb.setValue(getContentPosX());
            getSkinnable().requestLayout();
        }

        @Override
        public Object getBean()
        {
            return PanningWindowScrollPaneSkin.this;
        }

        @Override
        public String getName()
        {
            return "contentPosX";
        }
    };

    private void setContentPosX(double value)
    {
        contentPosXProperty().set(value);
    }

    private double getContentPosX()
    {
        return contentPosX.get();
    }

    private DoubleProperty contentPosXProperty()
    {
        return contentPosX;
    }

    private final DoubleProperty contentPosY = new DoublePropertyBase()
    {
        @Override
        protected void invalidated()
        {
            vsb.setValue(getContentPosY());
            getSkinnable().requestLayout();
        }

        @Override
        public Object getBean()
        {
            return PanningWindowScrollPaneSkin.this;
        }

        @Override
        public String getName()
        {
            return "contentPosY";
        }
    };

    private void setContentPosY(double value)
    {
        contentPosYProperty().set(value);
    }

    private double getContentPosY()
    {
        return contentPosY.get();
    }

    private DoubleProperty contentPosYProperty()
    {
        return contentPosY;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        final ScrollPane sp = getSkinnable();

        double vsbWidth = computeVsbSizeHint();
        double minWidth = vsbWidth + snappedLeftInset() + snappedRightInset();

        if (sp.getPrefViewportWidth() > 0)
        {
            return snapSpaceX(sp.getPrefViewportWidth() + minWidth);
        }
        else if (sp.getContent() != null)
        {
            return snapSpaceX(sp.getContent().prefWidth(height) + minWidth);
        }
        else
        {
            return Math.max(minWidth, DEFAULT_PREF_SIZE);
        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        final ScrollPane sp = getSkinnable();

        double hsbHeight = computeHsbSizeHint();
        double minHeight = hsbHeight + snappedTopInset() + snappedBottomInset();

        if (sp.getPrefViewportHeight() > 0)
        {
            return snapSpaceY(sp.getPrefViewportHeight() + minHeight);
        }
        else if (sp.getContent() != null)
        {
            return snapSpaceY(sp.getContent().prefHeight(width) + minHeight);
        }
        else
        {
            return Math.max(minHeight, DEFAULT_PREF_SIZE);
        }
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        final ScrollPane sp = getSkinnable();

        double vsbWidth = computeVsbSizeHint();
        double minWidth = vsbWidth + snappedLeftInset() + snappedRightInset();

        if (sp.getMinViewportWidth() > 0)
        {
            return snapSpaceX(sp.getMinViewportWidth() + minWidth);
        }
        else
        {
            double w = corner.minWidth(-1);
            return (w > 0) ? (3 * w) : (DEFAULT_MIN_SIZE);
        }

    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        final ScrollPane sp = getSkinnable();

        double hsbHeight = computeHsbSizeHint();
        double minHeight = hsbHeight + snappedTopInset() + snappedBottomInset();

        if (sp.getMinViewportHeight() > 0)
        {
            return snapSpaceY(sp.getMinViewportHeight() + minHeight);
        }
        else
        {
            double h = corner.minHeight(-1);
            return (h > 0) ? (3 * h) : (DEFAULT_MIN_SIZE);
        }
    }

    @Override
    protected void layoutChildren(final double x, final double y, final double w, final double h)
    {
        final ScrollPane control = getSkinnable();
        final Insets padding = control.getPadding();
        final double rightPadding = snapSizeX(padding.getRight());
        final double leftPadding = snapSizeX(padding.getLeft());
        final double topPadding = snapSizeY(padding.getTop());
        final double bottomPadding = snapSizeY(padding.getBottom());

        vsb.setMin(control.getVmin());
        vsb.setMax(control.getVmax());

        hsb.setMin(control.getHmin());
        hsb.setMax(control.getHmax());

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
        switch (attribute)
        {
            case VERTICAL_SCROLLBAR:
                return vsb;
            case HORIZONTAL_SCROLLBAR:
                return hsb;
            default:
                return super.queryAccessibleAttribute(attribute, parameters);
        }
    }

    private double computeHsbSizeHint()
    {
        return hsb.prefHeight(ScrollBar.USE_COMPUTED_SIZE);
    }

    private double computeVsbSizeHint()
    {
        return vsb.prefWidth(ScrollBar.USE_COMPUTED_SIZE);
    }

    private void computeScrollNodeSize()
    {
        if (scrollNode != null)
        {
            nodeWidth = snapSizeX(scrollNode.getLayoutBounds().getWidth());
            nodeHeight = snapSizeY(scrollNode.getLayoutBounds().getHeight());
            nodeSizeInvalid = false;
        }
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
        final ScrollPane sp = getSkinnable();
        double x = posX;
        double hsbRange = hsb.getMax() - hsb.getMin();
        double minX = hsbRange > 0 ? -x / hsbRange * (nodeWidth - contentWidth) : 0;
        minX = Math.min(minX, 0);
        viewContent.setLayoutX(snapPositionX(minX));
        if (!sp.hvalueProperty().isBound())
        {
            sp.setHvalue(clampScrollValue(posX));
        }
    }

    private void updatePosY()
    {
        final ScrollPane sp = getSkinnable();
        double vsbRange = vsb.getMax() - vsb.getMin();
        double minY = vsbRange > 0 ? -posY / vsbRange * (nodeHeight - contentHeight) : 0;
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

        setContentPosX(posX);
        setContentPosY(posY);

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
        contentPosX.set(newPosX);
        contentPosY.set(newPosY);
    }

    private void handleKeyEvent(KeyEvent e)
    {
        switch (e.getCode())
        {
            case LEFT:
                hsb.decrement();
                break;

            case RIGHT:
                hsb.increment();
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
        }
        e.consume();
    }

    private static double clampScrollValue(double value)
    {
        return Math.max(0.0, Math.min(value, 1.0));
    }

    private void handleMouseDraggedForPanning(final MouseEvent e)
    {
        if (e.getButton() != MouseButton.PRIMARY || IS_TOUCH_SUPPORTED)
        {
            final var deltaX = pressX - e.getX();
            final var deltaY = pressY - e.getY();
            handleMousePressedForPanning(hsb, deltaX, ohvalue);
            handleMousePressedForPanning(vsb, deltaY, ovvalue);
        }
        // we need to consume drag events, as we don't want the scrollpane itself to be dragged on every mouse click
        e.consume();
    }

    private void handleMousePressedForPanning(final ScrollBar pBar, final double pDelta, final double pOValue)
    {
        if (pBar.getVisibleAmount() > 0.0 && pBar.getVisibleAmount() < pBar.getMax() &&
                Math.abs(pDelta) > PAN_THRESHOLD)
        {
            var newHVal =
                    (pOValue + pDelta / (nodeWidth - viewRect.getWidth()) * (pBar.getMax() - pBar.getMin()));
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
        if (e.isPrimaryButtonDown())
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
