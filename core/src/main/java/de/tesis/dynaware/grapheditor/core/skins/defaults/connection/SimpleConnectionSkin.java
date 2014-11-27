/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.ConnectionSegment;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.DetouredConnectionSegment;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.GappedConnectionSegment;
import de.tesis.dynaware.grapheditor.core.skins.defaults.utils.RectangularConnectionUtils;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * A simple rectangular connection skin.
 * 
 * <p>
 * Shows a rectangular connection shape based on the positions of its joints. Shows a graphical effect at points where
 * the connection intersects other connections.
 * </p>
 */
public class SimpleConnectionSkin extends GConnectionSkin {

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

    private final IntersectionFinder intersectionFinder;

    private List<GJointSkin> jointSkins;
    private List<Point2D> points;
    private Map<Integer, List<Double>> intersections;

    /**
     * Creates a new simple connection skin instance.
     *
     * @param connection the {@link GConnection} the skin is being created for
     */
    public SimpleConnectionSkin(final GConnection connection) {

        super(connection);

        root.setManaged(false);

        intersectionFinder = new IntersectionFinder(connection);

        // Background path is invisible and used only to capture hover events.
        root.getChildren().add(backgroundPath);
        root.getChildren().add(path);

        path.setMouseTransparent(true);

        backgroundPath.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
        path.getStyleClass().setAll(STYLE_CLASS);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void setGraphEditor(final GraphEditor graphEditor) {

        super.setGraphEditor(graphEditor);
        intersectionFinder.setSkinLookup(graphEditor.getSkinLookup());
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {

        if (this.jointSkins != null) {
            removeOldRectangularConstraints();
        }

        this.jointSkins = jointSkins;

        addRectangularConstraints();
    }

    @Override
    public void applyConstraints(final List<Point2D> points) {
        if (!points.equals(this.points)) {
            checkFirstAndLastJoints(points);
        }
    }

    @Override
    public void draw(final List<Point2D> points, final Map<GConnection, List<Point2D>> allPoints) {

        final boolean pointsRequireRedraw = !points.equals(this.points);

        // If we are showing detours, get all intersections with connections *behind* this one. Otherwise in front.
        final Map<Integer, List<Double>> intersections = intersectionFinder.find(allPoints, checkShowDetours());

        final boolean intersectionsStayedNull = this.intersections == null && intersections == null;
        final boolean intersectionsSame = intersections != null && intersections.equals(this.intersections);
        final boolean intersectionsRequireRedraw = !(intersectionsStayedNull || intersectionsSame);

        if (pointsRequireRedraw || intersectionsRequireRedraw) {
            drawAllSegments(points, intersections);
        }

        this.points = points;
        this.intersections = intersections;
    }

    /**
     * Removes the old rectangular constraints on the connection's list of joint skins.
     */
    private void removeOldRectangularConstraints() {

        for (int i = 0; i < jointSkins.size() - 1; i++) {

            final Region thisJoint = jointSkins.get(i).getRoot();
            final Region nextJoint = jointSkins.get(i + 1).getRoot();

            if (RectangularConnectionUtils.isSegmentHorizontal(getConnection(), i)) {
                thisJoint.layoutXProperty().unbindBidirectional(nextJoint.layoutXProperty());
            } else {
                thisJoint.layoutYProperty().unbindBidirectional(nextJoint.layoutYProperty());
            }
        }

        for (final GJointSkin jointSkin : jointSkins) {

            jointSkin.getRoot().dragEnabledXProperty().unbind();
            jointSkin.getRoot().dragEnabledYProperty().unbind();
            jointSkin.getRoot().setDragEnabledX(true);
            jointSkin.getRoot().setDragEnabledY(true);
        }
    }

    /**
     * Adds constraints to the connection's joints in order to keep the connection rectangular in shape.
     */
    private void addRectangularConstraints() {

        // Our rectangular connection logic assumes an even number of joints.
        for (int i = 0; i < jointSkins.size() - 1; i++) {

            final Region thisJoint = jointSkins.get(i).getRoot();
            final Region nextJoint = jointSkins.get(i + 1).getRoot();

            if (RectangularConnectionUtils.isSegmentHorizontal(getConnection(), i)) {
                thisJoint.layoutXProperty().bindBidirectional(nextJoint.layoutXProperty());
            } else {
                thisJoint.layoutYProperty().bindBidirectional(nextJoint.layoutYProperty());
            }
        }

        restrictFirstAndLastJoints();
    }

    /**
     * Restricts the first and last joints to only be draggable in certain directions if the adjacent node is selected.
     */
    private void restrictFirstAndLastJoints() {

        final GNode sourceNode = (GNode) getConnection().getSource().getParent();
        final GNodeSkin sourceNodeSkin = getGraphEditor().getSkinLookup().lookupNode(sourceNode);

        if (RectangularConnectionUtils.isSegmentHorizontal(getConnection(), 0)) {
            jointSkins.get(0).getRoot().dragEnabledYProperty().bind(sourceNodeSkin.selectedProperty());
        } else {
            jointSkins.get(0).getRoot().dragEnabledXProperty().bind(sourceNodeSkin.selectedProperty());
        }

        final GNode targetNode = (GNode) getConnection().getTarget().getParent();
        final GNodeSkin targetNodeSkin = getGraphEditor().getSkinLookup().lookupNode(targetNode);
        final int lastIndex = jointSkins.size() - 1;

        if (RectangularConnectionUtils.isSegmentHorizontal(getConnection(), jointSkins.size())) {
            jointSkins.get(lastIndex).getRoot().dragEnabledYProperty().bind(targetNodeSkin.selectedProperty());
        } else {
            jointSkins.get(lastIndex).getRoot().dragEnabledXProperty().bind(targetNodeSkin.selectedProperty());
        }
    }

    /**
     * Checks the position of the first and last joints and makes sure they are aligned with their adjacent connectors.
     *
     * @param points all points that the connection should pass through (both connector and joint positions)
     */
    private void checkFirstAndLastJoints(final List<Point2D> points) {

        alignJoint(points, RectangularConnectionUtils.isSegmentHorizontal(getConnection(), 0), true);
        alignJoint(points, RectangularConnectionUtils.isSegmentHorizontal(getConnection(), points.size() - 2), false);
    }

    /**
     * Aligns the first or last joint to have the same vertical or horizontal position as the start or end point.
     *
     * @param points the list of points in this connection
     * @param vertical {@code true} to align in the vertical (y) direction, {@code false} for horizontal (x)
     * @param start {@code true} to align the first joint to the start, {@code false} for the last joint to the end
     */
    private void alignJoint(final List<Point2D> points, final boolean vertical, final boolean start) {

        final int targetPositionIndex = start ? 0 : points.size() - 1;
        final int jointPositionIndex = start ? 1 : points.size() - 2;

        final GJointSkin jointSkin = jointSkins.get(start ? 0 : jointSkins.size() - 1);

        if (vertical) {

            final double newJointY = points.get(targetPositionIndex).getY();
            final double newJointLayoutY = GeometryUtils.moveOnPixel(newJointY - jointSkin.getHeight() / 2);
            jointSkin.getRoot().setLayoutY(newJointLayoutY);

            final double currentX = points.get(jointPositionIndex).getX();
            points.set(jointPositionIndex, new Point2D(currentX, newJointY));

        } else {

            final double newJointX = points.get(targetPositionIndex).getX();
            final double newJointLayoutX = GeometryUtils.moveOnPixel(newJointX - jointSkin.getWidth() / 2);
            jointSkin.getRoot().setLayoutX(newJointLayoutX);

            final double currentY = points.get(jointPositionIndex).getY();
            points.set(jointPositionIndex, new Point2D(newJointX, currentY));
        }
    }

    /**
     * Draws all segments of the connection.
     *
     * @param points all points that the connection should pass through (both connector and joint positions)
     * @param intersections all intersection-points of this connection with other connections
     */
    private void drawAllSegments(final List<Point2D> points, final Map<Integer, List<Double>> intersections) {

        final double startX = points.get(0).getX();
        final double startY = points.get(0).getY();

        final MoveTo moveTo = new MoveTo(GeometryUtils.moveOffPixel(startX), GeometryUtils.moveOffPixel(startY));

        connectionSegments.clear();
        path.getElements().clear();
        path.getElements().add(moveTo);

        for (int i = 0; i < points.size() - 1; i++) {

            final Point2D start = points.get(i);
            final Point2D end = points.get(i + 1);

            List<Double> segmentIntersections;

            if (intersections != null && intersections.get(i) != null) {
                segmentIntersections = intersections.get(i);
            } else {
                segmentIntersections = new ArrayList<>();
            }

            final ConnectionSegment segment;

            if (checkShowDetours()) {
                segment = new DetouredConnectionSegment(start, end, segmentIntersections);
            } else {
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
    private boolean checkShowDetours() {

        boolean showDetours = false;

        final String value = getGraphEditor().getProperties().getCustomProperties().get(SHOW_DETOURS_KEY);
        if (Boolean.toString(true).equals(value)) {
            showDetours = true;
        }

        return showDetours;
    }
}
