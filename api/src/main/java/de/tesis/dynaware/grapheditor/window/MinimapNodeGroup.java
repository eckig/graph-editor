/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * The minimap representation of all nodes in the graph editor.
 *
 * <p>
 * This is responsible for drawing mini versions of all nodes in a
 * {@link GModel}. This group of mini-nodes is then displayed inside the
 * {@link GraphEditorMinimap}.
 * </p>
 */
public class MinimapNodeGroup extends Group {

    private static final String STYLE_CLASS_NODE = "minimap-node";
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    private final InvalidationListener checkSelectionListener = obs -> checkSelection();
    private final InvalidationListener checkSelectionWeakListener = new WeakInvalidationListener(
            checkSelectionListener);

    private SelectionManager selectionManager;
    private GModel model;

    private final Map<GNode, Rectangle> nodes = new HashMap<>();

    private Predicate<GConnection> connectionFilter = c -> true;
    
    private double width = -1;
    private double height = -1;
    private double scaleFactor = -1;
    private Canvas canvas = null;

    private final StyleableObjectProperty<Color> connectionColor = new StyleableObjectProperty<Color>(Color.GRAY) {

        @Override
        public String getName() {
            return "connectionColor";
        }

        @Override
        public Object getBean() {
            return "GraphEditorMinimap";
        }

        @Override
        public CssMetaData<? extends Styleable, Color> getCssMetaData() {
            return StyleableProperties.CONNECTION_COLOR;
        }
    };

    /**
     * Sets the selection manager instance currently in use by this graph
     * editor.
     *
     * <p>
     * This will be used to show what nodes are currently selected.
     * <p>
     *
     * @param selectionManager
     *            a {@link SelectionManager} instance
     */
    public void setSelectionManager(final SelectionManager selectionManager) {

        if (this.selectionManager != null) {
            this.selectionManager.getSelectedNodes().removeListener(checkSelectionWeakListener);
        }

        this.selectionManager = selectionManager;

        if (this.selectionManager != null) {
            this.selectionManager.getSelectedNodes().addListener(checkSelectionWeakListener);
        }
        checkSelection();
    }

    /**
     * Sets the model whose nodes will be drawn in the minimap.
     *
     * @param model
     *            athe {@link GModel} whose nodes are to be drawn
     */
    public void setModel(final GModel model) {
        this.model = model;
    }

    private void checkSelection() {
        for (final Map.Entry<GNode, Rectangle> entry : nodes.entrySet()) {
            entry.getValue().pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, isSelected(entry.getKey()));
        }
    }

    private boolean isSelected(final GNode node) {
        return selectionManager == null ? false : selectionManager.getSelectedNodes().contains(node);
    }

    private static double scaleSharp(final double value, final double scale) {
        return Math.round(value * scale) + 0.5;
    }

    /**
     * Set a filter {@link Predicate} to only draw the desired connections onto
     * the minimap
     * 
     * @see #setConnectionColor(Color)
     * @param connectionFilter
     *            connection filter {@link Predicate}
     */
    public void setConnectionFilter(final Predicate<GConnection> connectionFilter) {
        this.connectionFilter = connectionFilter;
    }

    /**
     * Set a {@link Color} to paint the connections onto the minimap
     * 
     * @see #setConnectionFilter(Predicate)
     * @param connectionColor
     *            connection {@link Color}
     */
    public void setConnectionColor(final Color connectionColor) {
        this.connectionColor.set(connectionColor);
    }
    
    /**
     * @return current {@link Color} to paint the connections onto the minimap
     */
    public Color getConnectionColor() {
        return connectionColor.get();
    }

    /**
     * @return {@link ObjectProperty} controlling the {@link Color} to paint the
     *         connections onto the minimap
     */
    public ObjectProperty<Color> connectionColorProperty() {
        return connectionColor;
    }
    
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        
        if(this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            redraw();
        }
    }
    
    /**
     * @param scaleFactor
     *            the ratio between the size of the content and the size of the
     *            minimap (between 0 and 1)
     */
    public void setScaleFactor(final double scaleFactor) {
        if(this.scaleFactor != scaleFactor) {
            this.scaleFactor = scaleFactor;
            redraw();
        }
    }
    
    private void redraw() {
        if(nodes.isEmpty()) {
            draw();
        }
        else {
            scaleMinimapNodes();
        }
    }
    
    /**
     * @return current width
     */
    public double getWidth() {
        return width;
    }
    
    /**
     * @return current height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Draws the model's nodes at a scaled-down size to be displayed in the
     * minimap.
     */
    public void draw() {

        nodes.clear();
        getChildren().clear();
        
        if(width < 1 || height < 1) {
            return;
        }
        
        if (model != null) {
            for (int i = 0; i < model.getNodes().size(); i++) {
                final GNode node = model.getNodes().get(i);
                final Rectangle minimapNode = new Rectangle();
                minimapNode.getStyleClass().addAll(STYLE_CLASS_NODE, node.getType());
                getChildren().add(minimapNode);
                nodes.put(node, minimapNode);
            }
            checkSelection();
        }
        
        scaleMinimapNodes();
    }
    
    private void scaleMinimapNodes() {

        if(canvas != null) {
            getChildren().remove(canvas);
            canvas = null;
        }
        
        if(width < 1 || height < 1) {
            return;
        }
        
        canvas = new Canvas(width, height);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setStroke(connectionColor.get());
        gc.setLineWidth(1);
        getChildren().add(0, canvas);

        if (model != null) {

            for (int i = 0; i < model.getConnections().size(); i++) {

                final GConnection conn = model.getConnections().get(i);
                if (connectionFilter != null && !connectionFilter.test(conn)) {
                    continue;
                }
                
                final GConnector source = conn.getSource();
                final GNode parentSource = source.getParent();
                double x = scaleSharp(source.getX() + parentSource.getX() - 10, scaleFactor),
                        y = scaleSharp(source.getY() + parentSource.getY(), scaleFactor);
                
                gc.moveTo(x, y);

                for (int j = 0; j <= conn.getJoints().size(); j++) {
                    
                    final double newX;
                    final double newY;
                    if (j < conn.getJoints().size()) {
                        final GJoint joint = conn.getJoints().get(j);
                        newX = scaleSharp(joint.getX(), scaleFactor);
                        newY = scaleSharp(joint.getY(), scaleFactor);
                    } else {
                        final GConnector target = conn.getTarget();
                        final GNode parentTarget = target.getParent();
                        newX = scaleSharp(target.getX() + parentTarget.getX(), scaleFactor);
                        newY = scaleSharp(target.getY() + parentTarget.getY(), scaleFactor);
                    }
                    
                    // only draw direct rectangular lines:
                    if(Math.abs(newX - x) < Math.abs(newY - y)) {
                        gc.lineTo(x, newY);
                    }
                    else {
                        gc.lineTo(newX, y);
                    }
                    
                    x = newX;
                    y = newY;
                }

                gc.stroke();
            }

            for (int i = 0; i < model.getNodes().size(); i++) {

                final GNode node = model.getNodes().get(i);
                final Rectangle minimapNode = nodes.get(node);
                if(minimapNode == null) {
                    continue;
                }
                minimapNode.setX(Math.round(node.getX() * scaleFactor));
                minimapNode.setY(Math.round(node.getY() * scaleFactor));
                minimapNode.setWidth(Math.round(node.getWidth() * scaleFactor));
                minimapNode.setHeight(Math.round(node.getHeight() * scaleFactor));
            }
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     *         CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    private static class StyleableProperties {

        static final CssMetaData<MinimapNodeGroup, Color> CONNECTION_COLOR = new CssMetaData<MinimapNodeGroup, Color>(
                "-connection-color", StyleConverter.getColorConverter(), Color.GRAY) {

            @Override
            public boolean isSettable(final MinimapNodeGroup node) {
                return !node.connectionColor.isBound();
            }

            @Override
            public StyleableProperty<Color> getStyleableProperty(MinimapNodeGroup node) {
                return node.connectionColor;
            }
        };

        static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {

            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Node.getClassCssMetaData());
            styleables.add(CONNECTION_COLOR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
