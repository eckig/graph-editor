/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import io.github.eckig.grapheditor.SelectionManager;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


/**
 * The minimap representation of all nodes in the graph editor.
 *
 * <p>
 * This is responsible for drawing mini versions of all nodes in a
 * {@link GModel}. This group of mini-nodes is then displayed inside the
 * {@link GraphEditorMinimap}.
 * </p>
 */
class MinimapNodeGroup extends Parent
{

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected"); //$NON-NLS-1$

    private final InvalidationListener checkSelectionListener = obs -> checkSelection();
    private final InvalidationListener checkSelectionWeakListener = new WeakInvalidationListener(checkSelectionListener);

    private SelectionManager selectionManager;
    private GModel model;

    private final Map<GNode, Node> nodes = new HashMap<>();

    private IMinimapRenderer<?> minimapRenderer = new IMinimapRenderer.DefaultMinimapRenderer();
    private Predicate<GConnection> connectionFilter = c -> true;
    private IMinimapConnectionRouter connectionRouter;
    private IMinimapConnectionRouter skinRouter = IMinimapConnectionRouter.fromSkins(null);

    private double width = -1;
    private double height = -1;
    private double scaleFactor = -1;
    private final Canvas canvas = new Canvas();

    // state of the last canvas paint, used to skip repainting unchanged connections:
    private List<List<Point2D>> paintedRoutes;
    private double paintedWidth = -1;
    private double paintedHeight = -1;
    private double paintedScaleFactor = -1;
    private Color paintedColor;
    private long paintCount;

    private final StyleableObjectProperty<Color> connectionColor = new StyleableObjectProperty<>(Color.GRAY)
    {

        @Override
        protected void invalidated()
        {
            requestLayout();
        }

        @Override
        public String getName()
        {
            return "connectionColor"; //$NON-NLS-1$
        }

        @Override
        public Object getBean()
        {
            return "GraphEditorMinimap"; //$NON-NLS-1$
        }

        @Override
        public CssMetaData<? extends Styleable, Color> getCssMetaData()
        {
            return StyleableProperties.CONNECTION_COLOR;
        }
    };

    /**
     * Default constructor
     */
    public MinimapNodeGroup()
    {
        getChildren().add(canvas);
    }

    /**
     * Sets the selection manager instance currently in use by this graph
     * editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param pSelectionManager
     *            a {@link SelectionManager} instance
     */
    public void setSelectionManager(final SelectionManager pSelectionManager)
    {
        if (selectionManager != null)
        {
            selectionManager.getSelectedItems().removeListener(checkSelectionWeakListener);
        }

        selectionManager = pSelectionManager;

        if (selectionManager != null)
        {
            selectionManager.getSelectedItems().addListener(checkSelectionWeakListener);
        }
        checkSelection();
    }

    /**
     * Sets the model whose nodes will be drawn in the minimap.
     *
     * @param pModel
     *            the {@link GModel} whose nodes are to be drawn
     */
    public void setModel(final GModel pModel)
    {
        model = pModel;
    }

    private void checkSelection()
    {
        for (final Map.Entry<GNode, Node> entry : nodes.entrySet())
        {
            entry.getValue().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, isSelected(entry.getKey()));
        }
    }

    private boolean isSelected(final GNode node)
    {
        return selectionManager != null && selectionManager.isSelected(node);
    }

    private static double scaleSharp(final double value, final double scale)
    {
        return Math.round(value * scale) + 0.5;
    }

    /**
     * Set a filter {@link Predicate} to only draw the desired connections onto
     * the minimap
     *
     * @see #setConnectionColor(Color)
     * @param pConnectionFilter
     *            connection filter {@link Predicate}
     */
    public void setConnectionFilter(final Predicate<GConnection> pConnectionFilter)
    {
        connectionFilter = pConnectionFilter;
    }

    /**
     * @param pConnectionRouter
     *         custom {@link IMinimapConnectionRouter} or {@code null} to use the default routing
     */
    public void setConnectionRouter(final IMinimapConnectionRouter pConnectionRouter)
    {
        connectionRouter = pConnectionRouter;
        requestLayout();
    }

    /**
     * @param pSkinLookup
     *         {@link SkinLookup} used by the default routing to query the connection skins
     */
    public void setSkinLookup(final SkinLookup pSkinLookup)
    {
        skinRouter = IMinimapConnectionRouter.fromSkins(pSkinLookup);
        requestLayout();
    }

    /**
     * Resolves the route of the given connection: the custom router if set, otherwise the route of the connection
     * skin, falling back to {@link IMinimapConnectionRouter#MODEL}.
     *
     * @param pConnection
     *         {@link GConnection}
     * @return route in content coordinates, never {@code null}
     */
    List<Point2D> getRoute(final GConnection pConnection)
    {
        final List<Point2D> route;
        if (connectionRouter != null)
        {
            route = connectionRouter.getRoute(pConnection);
        }
        else
        {
            final List<Point2D> skinRoute = skinRouter.getRoute(pConnection);
            route = skinRoute.size() >= 2 ? skinRoute : IMinimapConnectionRouter.MODEL.getRoute(pConnection);
        }
        return route == null ? List.of() : route;
    }

    /**
     * @param pMinimapRenderer
     *         {@link IMinimapRenderer}
     */
    public void setMinimapRenderer(final IMinimapRenderer<?> pMinimapRenderer)
    {
        minimapRenderer = pMinimapRenderer;
        draw();
    }

    /**
     * Set a {@link Color} to paint the connections onto the minimap
     *
     * @see #setConnectionFilter(Predicate)
     * @param pConnectionColor
     *            connection {@link Color}
     */
    public void setConnectionColor(final Color pConnectionColor)
    {
        connectionColor.set(pConnectionColor);
    }

    /**
     * @return current {@link Color} to paint the connections onto the minimap
     */
    public Color getConnectionColor()
    {
        return connectionColor.get();
    }

    /**
     * @return {@link ObjectProperty} controlling the {@link Color} to paint the
     *         connections onto the minimap
     */
    public ObjectProperty<Color> connectionColorProperty()
    {
        return connectionColor;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double pWidth, double pHeight)
    {
        if (width != pWidth || height != pHeight)
        {
            width = pWidth;
            height = pHeight;
            redraw();
        }
    }

    /**
     * @param pScaleFactor
     *            the ratio between the size of the content and the size of the
     *            minimap (between 0 and 1)
     */
    public void setScaleFactor(final double pScaleFactor)
    {
        if (scaleFactor != pScaleFactor)
        {
            scaleFactor = pScaleFactor;
            redraw();
        }
    }

    private void redraw()
    {
        if (nodes.isEmpty())
        {
            draw();
        }
        else
        {
            requestLayout();
        }
    }

    /**
     * Draws the model's nodes at a scaled-down size to be displayed in the
     * minimap.
     */
    public void draw()
    {
        nodes.clear();
        if (getChildren().size() > 1)
        {
            getChildren().remove(1, getChildren().size());
        }

        if (width == -1 || height == -1 || scaleFactor == -1 || minimapRenderer == null)
        {
            return;
        }

        if (model != null)
        {
            for (int i = 0; i < model.getNodes().size(); i++)
            {
                final GNode node = model.getNodes().get(i);
                final Node minimapNode = minimapRenderer == null ? null : minimapRenderer.createMinimapNode(node);
                if (minimapNode != null)
                {
                    getChildren().add(minimapNode);
                    nodes.put(node, minimapNode);
                }
            }
            checkSelection();
        }

        requestLayout();
    }

    @Override
    protected void layoutChildren()
    {
        if (width < 1 || height < 1 || minimapRenderer == null)
        {
            return;
        }

        drawConnections();

        if (model != null)
        {
            for (final Map.Entry<GNode, Node> entry : nodes.entrySet())
            {
                resizeRelocate(entry.getKey(), entry.getValue(), minimapRenderer);
            }
        }
    }

    /**
     * @return number of times the connections were painted onto the canvas (for tests)
     */
    long getPaintCount()
    {
        return paintCount;
    }

    /**
     * Paints all connection routes onto the canvas. The canvas is only repainted if anything affecting the result
     * (routes, size, scale or color) changed since the last paint, as this is called on every layout pass of the graph
     * editor view.
     */
    private void drawConnections()
    {
        final List<List<Point2D>> routes = new ArrayList<>();
        if (model != null)
        {
            for (int i = 0; i < model.getConnections().size(); i++)
            {
                final GConnection conn = model.getConnections().get(i);
                if (connectionFilter != null && !connectionFilter.test(conn))
                {
                    continue;
                }

                final List<Point2D> route = getRoute(conn);
                if (route.size() >= 2)
                {
                    // copy: a mutable route changed in place must not compare equal to the painted one
                    routes.add(List.copyOf(route));
                }
            }
        }

        final Color color = connectionColor.get();
        if (routes.equals(paintedRoutes) && width == paintedWidth && height == paintedHeight
                && scaleFactor == paintedScaleFactor && Objects.equals(color, paintedColor))
        {
            return;
        }
        paintedRoutes = routes;
        paintedWidth = width;
        paintedHeight = height;
        paintedScaleFactor = scaleFactor;
        paintedColor = color;
        paintCount++;

        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        canvas.setWidth(width);
        canvas.setHeight(height);

        gc.setStroke(color);
        gc.setLineWidth(1);

        for (final List<Point2D> route : routes)
        {
            gc.beginPath();
            gc.moveTo(scaleSharp(route.get(0).getX(), scaleFactor), scaleSharp(route.get(0).getY(), scaleFactor));
            for (int j = 1; j < route.size(); j++)
            {
                final Point2D p = route.get(j);
                gc.lineTo(scaleSharp(p.getX(), scaleFactor), scaleSharp(p.getY(), scaleFactor));
            }
            gc.stroke();
        }
    }

    private <N extends Node> void resizeRelocate(final GNode node, final Node rendered,
            final IMinimapRenderer<N> pRenderer)
    {
        final double x = (Math.round(node.getX() * scaleFactor));
        final double y = (Math.round(node.getY() * scaleFactor));
        final double width = (Math.round(node.getWidth() * scaleFactor));
        final double height = (Math.round(node.getHeight() * scaleFactor));
        if (rendered != null && pRenderer.getType().isAssignableFrom(rendered.getClass()))
        {
            pRenderer.resizeRelocate(pRenderer.getType().cast(rendered), x, y, width, height);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData()
    {
        return getClassCssMetaData();
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     *         CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData()
    {
        return StyleableProperties.STYLEABLES;
    }

    private static class StyleableProperties
    {

        static final CssMetaData<MinimapNodeGroup, Color> CONNECTION_COLOR = new CssMetaData<>("-connection-color", //$NON-NLS-1$
                StyleConverter.getColorConverter(), Color.GRAY)
        {

            @Override
            public boolean isSettable(final MinimapNodeGroup node)
            {
                return !node.connectionColor.isBound();
            }

            @Override
            public StyleableProperty<Color> getStyleableProperty(MinimapNodeGroup node)
            {
                return node.connectionColor;
            }
        };

        static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static
        {

            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Node.getClassCssMetaData());
            styleables.add(CONNECTION_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
