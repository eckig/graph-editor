package io.github.eckig.grapheditor.window;


import io.github.eckig.grapheditor.utils.GraphEventManager;
import io.github.eckig.grapheditor.window.skin.PanningWindowScrollPaneSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleRole;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Window;

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
    private static final float SCALE_MIN = 0.25f;
    private static final float SCALE_MAX = 2.0f;
    private final DoubleProperty zoom = new SimpleDoubleProperty(1);
    private final Scale scale = new Scale();

    /**
     * {@code true} while the user holds {@code SPACE} to temporarily turn the
     * primary mouse button into a panning gesture.
     */
    private final ReadOnlyBooleanWrapper panModeArmed = new ReadOnlyBooleanWrapper(false);

    private GraphEventManager eventManager;

    /** the cursor set by the application, saved while a gesture cursor is shown */
    private Cursor applicationCursor;
    private boolean gestureCursorActive;

    private final EventHandler<KeyEvent> keyPressedFilter = this::handleKeyPressed;
    private final EventHandler<KeyEvent> keyReleasedFilter = this::handleKeyReleased;
    private final ChangeListener<Boolean> windowFocusListener = (_, _, focused) ->
    {
        if (!Boolean.TRUE.equals(focused))
        {
            // the KEY_RELEASED event is lost when the window loses focus (e.g. alt-tab)
            setPanModeArmed(false);
        }
    };
    private final ChangeListener<Boolean> windowShowingListener = (_, _, showing) ->
    {
        if (!Boolean.TRUE.equals(showing))
        {
            setPanModeArmed(false);
        }
    };
    private final ChangeListener<Window> sceneWindowListener = (_, oldWindow, newWindow) ->
    {
        if (oldWindow != null)
        {
            oldWindow.focusedProperty().removeListener(windowFocusListener);
            oldWindow.showingProperty().removeListener(windowShowingListener);
        }
        if (newWindow != null)
        {
            newWindow.focusedProperty().addListener(windowFocusListener);
            newWindow.showingProperty().addListener(windowShowingListener);
        }
        setPanModeArmed(false);
    };

    /**
     * Creates a new {@link PanningWindow}.
     */
    public PanningWindow()
    {
        // the PanningWindow itself is the focusable element, the inner ScrollPane
        // must not be a second tab stop
        scrollPane.setFocusTraversable(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setSkin(new PanningWindowScrollPaneSkin(this, scrollPane));

        getChildren().add(scrollPane);

        scale.xProperty().bind(zoom);
        scale.yProperty().bind(zoom);

        addEventHandler(ZoomEvent.ZOOM, this::handleZoom);

        // the graph editor has to be able to own the keyboard focus, otherwise it
        // never receives key events (e.g. SPACE to pan)
        setFocusTraversable(true);
        // ... and a focusable element has to tell assistive technology what it is
        setAccessibleRole(AccessibleRole.SCROLL_PANE);
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressedForFocus);

        sceneProperty().addListener((_, oldScene, newScene) -> sceneChanged(oldScene, newScene));

        panModeArmed.addListener((_, _, armed) -> setGestureCursor(Boolean.TRUE.equals(armed) ? Cursor.OPEN_HAND : null));
    }

    private void sceneChanged(final Scene pOldScene, final Scene pNewScene)
    {
        setPanModeArmed(false);

        if (pOldScene != null)
        {
            pOldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedFilter);
            pOldScene.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleasedFilter);
            pOldScene.windowProperty().removeListener(sceneWindowListener);
            final Window oldWindow = pOldScene.getWindow();
            if (oldWindow != null)
            {
                oldWindow.focusedProperty().removeListener(windowFocusListener);
                oldWindow.showingProperty().removeListener(windowShowingListener);
            }
        }

        if (pNewScene != null)
        {
            pNewScene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedFilter);
            pNewScene.addEventFilter(KeyEvent.KEY_RELEASED, keyReleasedFilter);
            pNewScene.windowProperty().addListener(sceneWindowListener);
            final Window newWindow = pNewScene.getWindow();
            if (newWindow != null)
            {
                newWindow.focusedProperty().addListener(windowFocusListener);
                newWindow.showingProperty().addListener(windowShowingListener);
            }
        }
    }

    /**
     * Claims the keyboard focus when the user interacts with the graph editor, so
     * that key events (e.g. {@code SPACE} to pan) are delivered at all.
     *
     * <p>
     * This runs as an event <i>filter</i>, so a child that wants the focus for
     * itself (a text field inside a node skin, ...) can still request it afterwards.
     * The focus is never stolen from a node that already lives inside this window.
     * </p>
     *
     * @param pEvent
     *            a mouse-pressed {@link MouseEvent}
     */
    private void handleMousePressedForFocus(final MouseEvent pEvent)
    {
        final Scene scene = getScene();
        if (scene != null && !isInsideThisWindow(scene.getFocusOwner()))
        {
            requestFocus();
        }
    }

    private boolean isInsideThisWindow(final Node pNode)
    {
        for (Node current = pNode; current != null; current = current.getParent())
        {
            if (current == this)
            {
                return true;
            }
        }
        return false;
    }

    private void handleKeyPressed(final KeyEvent pEvent)
    {
        if (pEvent.getCode() != KeyCode.SPACE || isTextInputFocused())
        {
            return;
        }

        // note: key auto repeat is harmless, the armed state is idempotent
        setPanModeArmed(true);

        final Scene scene = getScene();
        final Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner instanceof ButtonBase || focusOwner instanceof ComboBoxBase)
        {
            // SPACE is the pan modifier here, so do not also "click" a focused button
            // or open the popup of a focused combo box. Everything else (application
            // accelerators, ...) is deliberately left alone.
            pEvent.consume();
        }
    }

    private void handleKeyReleased(final KeyEvent pEvent)
    {
        if (pEvent.getCode() == KeyCode.SPACE)
        {
            setPanModeArmed(false);
        }
    }

    /**
     * {@code SPACE} must keep its regular meaning while the user is typing.
     *
     * <p>
     * Note that the focus is deliberately <b>not</b> required to be inside this
     * window: in a real application some unrelated control (a toolbar button, ...)
     * usually owns the focus, and {@code SPACE} still has to pan.
     * </p>
     *
     * @return {@code true} if the current focus owner needs the {@code SPACE} key
     */
    private boolean isTextInputFocused()
    {
        final Scene scene = getScene();
        final Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner instanceof TextInputControl)
        {
            return true;
        }
        return focusOwner instanceof ComboBoxBase<?> combo && combo.isEditable();
    }

    /**
     * @return {@code true} while {@code SPACE} is held down and the primary mouse
     *         button therefore pans the view instead of starting a rubber-band
     *         selection
     */
    public boolean isPanModeArmed()
    {
        return panModeArmed.get();
    }

    private void setPanModeArmed(final boolean pArmed)
    {
        panModeArmed.set(pArmed);
    }

    /**
     * @return read-only view on {@link #isPanModeArmed()}
     */
    public ReadOnlyBooleanProperty panModeArmedProperty()
    {
        // note: getReadOnlyProperty() caches, so callers cannot write to the state
        // and repeated calls do not pile up wrappers
        return panModeArmed.getReadOnlyProperty();
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b> call it.
     * </p>
     *
     * <p>
     * Temporarily overrides the cursor of this window for the duration of a gesture,
     * remembering the cursor set by the application so that it can be restored.
     * </p>
     *
     * @param pCursor
     *            the gesture {@link Cursor}, or {@code null} to restore the cursor
     *            previously set by the application
     */
    public void setGestureCursor(final Cursor pCursor)
    {
        if (pCursor == null)
        {
            if (gestureCursorActive)
            {
                gestureCursorActive = false;
                setCursor(applicationCursor);
                applicationCursor = null;
            }
            return;
        }

        if (!gestureCursorActive)
        {
            gestureCursorActive = true;
            applicationCursor = getCursor();
        }
        setCursor(pCursor);
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b> call it.
     * </p>
     *
     * @return the {@link GraphEventManager} arbitrating the gestures of this window, may be {@code null}
     */
    public GraphEventManager getEventManager()
    {
        return eventManager;
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b> call it.
     * </p>
     *
     * @param pEventManager
     *            the {@link GraphEventManager} arbitrating the gestures of this window
     */
    public void setEventManager(final GraphEventManager pEventManager)
    {
        eventManager = pEventManager;
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
            scrollPane.setContent(pContent);
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
