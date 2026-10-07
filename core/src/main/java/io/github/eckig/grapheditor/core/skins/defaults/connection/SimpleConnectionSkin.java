/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.eckig.grapheditor.GJointConnectionSkin;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.connections.RectangularConnections;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.ConnectionSegment;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.DetouredConnectionSegment;
import io.github.eckig.grapheditor.core.skins.defaults.connection.segment.GappedConnectionSegment;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.routing.RouteContext;
import io.github.eckig.grapheditor.utils.DraggableBox;
import io.github.eckig.grapheditor.utils.GeometryUtils;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * A simple rectangular connection skin.
 *
 * <p>
 * Shows a rectangular connection shape based on the positions of its joints. Shows a graphical effect at points where
 * the connection intersects other connections.
 * </p>
 */
public class SimpleConnectionSkin extends GJointConnectionSkin implements IntersectionFinder.IIntersectionConnection
{

    /**
     * Property key to show detours at intersections.
     *
     * <p>
     * By default small gaps are drawn points where the connection passes <b>under</b> other connections. However it is
     * also possible to draw detours (small semicircles) at points where the connection passes <b>over</b> others.
     * </p>
     *
     * <p>
     * To activate this functionality, add this key to the graph editor's custom properties with the value "true". Do
     * <b>NOT</b> mix 'detoured' and 'gapped' connection skins in the same graph, it will look bad.
     * </p>
     */
    public static final String SHOW_DETOURS_KEY = "default-connection-skin-show-detours";

    protected final Group root = new Group();
    protected final Path path = new Path();
    protected final Path backgroundPath = new Path();

    protected final List<ConnectionSegment> connectionSegments = new ArrayList<>();

    private static final String STYLE_CLASS = "default-connection";
    private static final String STYLE_CLASS_BACKGROUND = "default-connection-background";

    /**
     * Cache the index of this connection skin inside the list of children of the connection layer. As the graph editor
     * grows the indexOf() lookup calls take up a considerable amount of time.
     */
    private int mConnectionIndex;

    private List<GJointSkin> jointSkins;


    /**
     * Creates a new simple connection skin instance.
     *
     * @param connection
     *         the {@link GConnection} the skin is being created for
     */
    public SimpleConnectionSkin(final GConnection connection)
    {
        super(connection);

        root.setManaged(false);

        // Background path is invisible and used only to capture hover events.
        root.getChildren().add(backgroundPath);
        root.getChildren().add(path);

        path.setMouseTransparent(true);

        backgroundPath.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
        path.getStyleClass().setAll(STYLE_CLASS);
    }

    @Override
    public Node getRoot()
    {
        return root;
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins)
    {
        if (this.jointSkins != null)
        {
            removeOldRectangularConstraints();
        }

        this.jointSkins = jointSkins;

        addRectangularConstraints();
    }

    /**
     * Aligns the first and last joint skins with their adjacent connectors, so the connection stays rectangular when a
     * node is moved.
     */
    @Override
    public void prepareRoute()
    {
        final Point2D[] points = doUpdate();
        if (points != null && hasJointSkins(points))
        {
            alignJoint(points, RectangularConnections.isSegmentHorizontal(getItem(), 0), true, true);
            alignJoint(points, RectangularConnections.isSegmentHorizontal(getItem(), points.length - 2), false, true);
        }
    }

    private boolean hasJointSkins(final Point2D[] pPoints)
    {
        return jointSkins != null && !jointSkins.isEmpty() && jointSkins.size() == pPoints.length - 2;
    }

    /**
     * @param pRoute
     *         route of this connection
     * @return the route as array with the first and last joint exactly aligned with their adjacent connectors
     */
    private Point2D[] alignedPoints(final List<Point2D> pRoute)
    {
        final Point2D[] points = pRoute.toArray(new Point2D[0]);
        if (hasJointSkins(points))
        {
            alignJoint(points, RectangularConnections.isSegmentHorizontal(getItem(), 0), true, false);
            alignJoint(points, RectangularConnections.isSegmentHorizontal(getItem(), points.length - 2), false, false);
        }
        return points;
    }

    /**
     * Removes the old rectangular constraints on the connection's list of joint skins.
     */
    private void removeOldRectangularConstraints()
    {
        for (int i = 0; i < jointSkins.size() - 1; i++)
        {
            final DraggableBox thisJoint = jointSkins.get(i).getRoot();
            final DraggableBox nextJoint = jointSkins.get(i + 1).getRoot();

            if (RectangularConnections.isSegmentHorizontal(getItem(), i))
            {
                thisJoint.bindLayoutX(null);
                nextJoint.bindLayoutX(null);
            }
            else
            {
                thisJoint.bindLayoutY(null);
                nextJoint.bindLayoutY(null);
            }
        }
    }

    /**
     * Adds constraints to the connection's joints in order to keep the connection rectangular in shape.
     */
    private void addRectangularConstraints()
    {
        // Our rectangular connection logic assumes an even number of joints.
        for (int i = 0; i < jointSkins.size() - 1; i++)
        {
            final DraggableBox thisJoint = jointSkins.get(i).getRoot();
            final DraggableBox nextJoint = jointSkins.get(i + 1).getRoot();

            if (RectangularConnections.isSegmentHorizontal(getItem(), i))
            {
                thisJoint.bindLayoutX(nextJoint);
                nextJoint.bindLayoutX(thisJoint);
            }
            else
            {
                thisJoint.bindLayoutY(nextJoint);
                nextJoint.bindLayoutY(thisJoint);
            }
        }
    }

    /**
     * Aligns the first or last joint to have the same vertical or horizontal position as the start or end point.
     *
     * @param points
     *         the list of points in this connection
     * @param vertical
     *         {@code true} to align in the vertical (y) direction, {@code false} for horizontal (x)
     * @param start
     *         {@code true} to align the first joint to the start, {@code false} for the last joint to the end
     * @param moveSkin
     *         {@code true} to also move the joint skin, {@code false} to only align the point
     */
    private void alignJoint(final Point2D[] points, final boolean vertical, final boolean start, final boolean moveSkin)
    {
        final int targetPositionIndex = start ? 0 : points.length - 1;
        final int jointPositionIndex = start ? 1 : points.length - 2;
        final GJointSkin jointSkin = jointSkins.get(start ? 0 : jointSkins.size() - 1);

        if (vertical)
        {
            final double newJointY = points[targetPositionIndex].getY();
            if (moveSkin)
            {
                jointSkin.getRoot().setLayoutY(GeometryUtils.moveOnPixel(newJointY - jointSkin.getHeight() / 2));
            }

            final double currentX = points[jointPositionIndex].getX();
            points[jointPositionIndex] = new Point2D(currentX, newJointY);
        }
        else
        {
            final double newJointX = points[targetPositionIndex].getX();
            if (moveSkin)
            {
                jointSkin.getRoot().setLayoutX(GeometryUtils.moveOnPixel(newJointX - jointSkin.getWidth() / 2));
            }

            final double currentY = points[jointPositionIndex].getY();
            points[jointPositionIndex] = new Point2D(newJointX, currentY);
        }
    }

    /**
     * Draws all segments of the connection.
     *
     * @param points
     *         all points that the connection should pass through (both connector and joint positions)
     * @param intersections
     *         all intersection-points of this connection with other connections
     */
    private void drawAllSegments(final Point2D[] points, final double[][] intersections)
    {
        final double startX = points[0].getX();
        final double startY = points[0].getY();

        final MoveTo moveTo = new MoveTo(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

        connectionSegments.clear();
        path.getElements().clear();
        path.getElements().add(moveTo);

        for (int i = 0; i < points.length - 1; i++)
        {
            final Point2D start = points[i];
            final Point2D end = points[i + 1];

            final double[] segmentIntersections = intersections != null ? intersections[i] : null;
            final ConnectionSegment segment;

            if (checkShowDetours())
            {
                segment = new DetouredConnectionSegment(start, end, segmentIntersections);
            }
            else
            {
                segment = new GappedConnectionSegment(start, end, segmentIntersections);
            }

            segment.draw();

            connectionSegments.add(segment);
            path.getElements().addAll(segment.getPathElements());
        }

        backgroundPath.getElements().clear();
        backgroundPath.getElements().addAll(path.getElements());
    }

    /**
     * Checks whether the custom property has been set to show detours instead of gaps when connections intersect.
     *
     * @return {@code true} if the custom property to show detours has been set
     */
    private boolean checkShowDetours()
    {
        boolean showDetours = false;

        final String value = getGraphEditor().getProperties().getCustomProperties().get(SHOW_DETOURS_KEY);
        if (Boolean.toString(true).equals(value))
        {
            showDetours = true;
        }

        return showDetours;
    }

    @Override
    protected void selectionChanged(boolean isSelected)
    {
        // Not implemented
    }

    @Override
    protected void drawRoute(final List<Point2D> pRoute, final RouteContext pContext)
    {
        if (getRoot() != null && getRoot().getParent() != null)
        {
            mConnectionIndex = getRoot().getParent().getChildrenUnmodifiable().indexOf(getRoot());
        }
        else
        {
            mConnectionIndex = -1;
        }

        if (pRoute.size() < 2)
        {
            connectionSegments.clear();
            path.getElements().clear();
            backgroundPath.getElements().clear();
            return;
        }

        final Map<SimpleConnectionSkin, Point2D[]> allPoints = pContext == null ? Map.of(this, alignedPoints(pRoute))
                : pContext.getShared(SimpleConnectionSkin.class, SimpleConnectionSkin::collectPoints);
        final Point2D[] points = allPoints.containsKey(this) ? allPoints.get(this) : alignedPoints(pRoute);

        // If we are showing detours, get all intersections with connections *behind* this one. Otherwise in front.
        final double[][] intersections = IntersectionFinder.find(this, allPoints, checkShowDetours());
        drawAllSegments(points, intersections);
    }

    /**
     * @return the (aligned) points of all {@link SimpleConnectionSkin simple connection skins} of the layout pass,
     *         used to find intersections
     */
    private static Map<SimpleConnectionSkin, Point2D[]> collectPoints(final RouteContext pContext)
    {
        final Map<SimpleConnectionSkin, Point2D[]> points = new HashMap<>();
        for (final var entry : pContext.getRoutes().entrySet())
        {
            if (entry.getKey() instanceof SimpleConnectionSkin skin && entry.getValue().size() >= 2)
            {
                points.put(skin, skin.alignedPoints(entry.getValue()));
            }
        }
        return points;
    }

    private Point2D[] doUpdate()
    {
        final GConnection item = getItem();
        final SkinLookup skinLookup = getGraphEditor() == null ? null : getGraphEditor().getSkinLookup();
        if (item == null || skinLookup == null)
        {
            return null;
        }
        else if (item.getJoints().isEmpty())
        {
            final Point2D[] points = new Point2D[2];

            // Start: Source position
            points[0] = GeometryUtils.getConnectorPosition(item.getSource(), skinLookup);

            // End: Target position
            points[1] = GeometryUtils.getConnectorPosition(item.getTarget(), skinLookup);

            return points;
        }
        else
        {
            final int len = item.getJoints().size() + 2;
            final Point2D[] points = new Point2D[len];

            // Middle: joint positions
            GeometryUtils.fillJointPositions(item, skinLookup, points);

            // Start: Source position
            points[0] = GeometryUtils.getConnectorPosition(item.getSource(), skinLookup);

            // End: Target position
            points[len - 1] = GeometryUtils.getConnectorPosition(item.getTarget(), skinLookup);

            return points;
        }
    }

    @Override
    public int getParentIndex()
    {
        return mConnectionIndex;
    }
}
