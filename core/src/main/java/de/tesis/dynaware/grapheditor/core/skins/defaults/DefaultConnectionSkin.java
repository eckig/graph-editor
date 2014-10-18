/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.CursorOffsetCalculator;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.IntersectionFinder;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.JointAlignmentManager;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.JointCleaner;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.JointCreator;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.ConnectionSegment;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.DetouredConnectionSegment;
import de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment.GappedConnectionSegment;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * The default connection skin.
 *
 * <p>
 * Connections drawn by this skin have a rectangular shape. They are intended for connections with
 *
 * <ol>
 * <li>An even number of joints.
 * <li>At least two joints.
 * <li>Source and target connectors on the <b>sides</b> of their nodes.
 * </ol>
 */
public class DefaultConnectionSkin extends GConnectionSkin {

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

    // The following are protected to allow subclasses to set their own CSS styleclasses on them.
    protected final Path connectionPath = new Path();
    protected final Path backgroundPath = new Path();
    protected final JointCreator jointCreator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSkin.class);

    private static final String STYLE_CLASS = "default-connection";
    private static final String STYLE_CLASS_BACKGROUND = "default-connection-background";

    private static final int MIN_NUMBER_OF_JOINTS_SUPPORTED = 2;

    private final List<ConnectionSegment> connectionSegments = new ArrayList<>();

    private final IntersectionFinder intersectionFinder;
    private final CursorOffsetCalculator cursorOffsetCalculator;
    private final JointCleaner jointCleaner;
    private final JointAlignmentManager jointAlignmentManager;

    private final Group root = new Group();

    private List<GJointSkin> jointSkins;

    private List<Point2D> points;
    private Map<Integer, List<Double>> intersections;

    /**
     * Creates a new default connection skin instance.
     *
     * @param connection the {@link GConnection} the skin is being created for
     */
    public DefaultConnectionSkin(final GConnection connection) {

        super(connection);

        root.setManaged(false);

        intersectionFinder = new IntersectionFinder(connection);
        cursorOffsetCalculator = new CursorOffsetCalculator(connectionPath, backgroundPath, connectionSegments);
        jointCreator = new JointCreator(connection, cursorOffsetCalculator);
        jointCleaner = new JointCleaner(connection);
        jointAlignmentManager = new JointAlignmentManager();

        // Background path is invisible and used only to capture hover events.
        root.getChildren().add(backgroundPath);
        root.getChildren().add(connectionPath);

        connectionPath.setMouseTransparent(true);

        backgroundPath.getStyleClass().setAll(STYLE_CLASS_BACKGROUND);
        connectionPath.getStyleClass().setAll(STYLE_CLASS);

        jointCreator.addJointCreationHandler(root);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void setGraphEditor(final GraphEditor graphEditor) {

        super.setGraphEditor(graphEditor);

        intersectionFinder.setSkinLookup(graphEditor.getSkinLookup());
        jointCreator.setGraphEditor(graphEditor);
        jointCleaner.setGraphEditor(graphEditor);
        jointAlignmentManager.setSkinLookup(graphEditor.getSkinLookup());
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {

        if (this.jointSkins != null) {
            removeOldRectangularConstraints();
        }

        this.jointSkins = jointSkins;

        addRectangularConstraints();
        checkJointCount();

        jointCleaner.addCleaningHandlers(jointSkins);
        jointAlignmentManager.addAlignmentHandlers(jointSkins);
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

            if (i % 2 == 0) {

                final Region even = jointSkins.get(i).getRoot();
                final Region odd = jointSkins.get(i + 1).getRoot();

                even.layoutXProperty().unbindBidirectional(odd.layoutXProperty());

            } else {

                final Region odd = jointSkins.get(i).getRoot();
                final Region even = jointSkins.get(i + 1).getRoot();

                odd.layoutYProperty().unbindBidirectional(even.layoutYProperty());
            }
        }
    }

    /**
     * Adds constraints to the connection's joints in order to keep the connection rectangular in shape.
     */
    private void addRectangularConstraints() {

        // Our rectangular connection logic assumes an even number of joints.
        for (int i = 0; i < jointSkins.size() - 1; i++) {

            if (i % 2 == 0) {

                final Region even = jointSkins.get(i).getRoot();
                final Region odd = jointSkins.get(i + 1).getRoot();

                even.layoutXProperty().bindBidirectional(odd.layoutXProperty());

            } else {

                final Region odd = jointSkins.get(i).getRoot();
                final Region even = jointSkins.get(i + 1).getRoot();

                odd.layoutYProperty().bindBidirectional(even.layoutYProperty());
            }
        }

        // The first and last skins are only draggable if their nearest node is selected.
        if (jointSkins.size() >= MIN_NUMBER_OF_JOINTS_SUPPORTED) {

            final int firstIndex = 0;
            final int lastIndex = jointSkins.size() - 1;

            final GNode sourceNode = (GNode) getConnection().getSource().getParent();
            final GNodeSkin sourceNodeSkin = getGraphEditor().getSkinLookup().lookupNode(sourceNode);

            jointSkins.get(firstIndex).getRoot().dragEnabledYProperty().bind(sourceNodeSkin.selectedProperty());

            final GNode targetNode = (GNode) getConnection().getTarget().getParent();
            final GNodeSkin targetNodeSkin = getGraphEditor().getSkinLookup().lookupNode(targetNode);

            jointSkins.get(lastIndex).getRoot().dragEnabledYProperty().bind(targetNodeSkin.selectedProperty());
        }
    }

    /**
     * Checks the position of the first and last joints and makes sure they are vertically aligned with their adjacent
     * connectors.
     *
     * @param points all points that the connection should pass through (both connector and joint positions)
     */
    private void checkFirstAndLastJoints(final List<Point2D> points) {

        final double startY = points.get(0).getY();
        final double endY = points.get(points.size() - 1).getY();

        if (jointSkins.size() >= MIN_NUMBER_OF_JOINTS_SUPPORTED) {

            final GJointSkin firstJointSkin = jointSkins.get(0);
            final GJointSkin lastJointSkin = jointSkins.get(jointSkins.size() - 1);

            final double firstJointY = GeometryUtils.moveOnPixel(startY - firstJointSkin.getHeight() / 2);
            final double lastJointY = GeometryUtils.moveOnPixel(endY - lastJointSkin.getHeight() / 2);

            firstJointSkin.getRoot().setLayoutY(firstJointY);
            lastJointSkin.getRoot().setLayoutY(lastJointY);

            final double firstJointX = points.get(1).getX();
            final double lastJointX = points.get(points.size() - 2).getX();

            points.set(1, new Point2D(firstJointX, startY));
            points.set(points.size() - 2, new Point2D(lastJointX, endY));
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
        connectionPath.getElements().clear();
        connectionPath.getElements().add(moveTo);

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
            connectionPath.getElements().addAll(segment.getPathElements());
        }

        backgroundPath.getElements().clear();
        backgroundPath.getElements().addAll(connectionPath.getElements());
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

    /**
     * Checks the number of joints is even and that there are at least two joints.
     */
    private boolean checkJointCount() {

        final int count = getConnection().getJoints().size();

        if (count < 2 || count % 2 != 0) {
            LOGGER.error(LogMessages.UNSUPPORTED_JOINT_COUNT);
            return false;
        }

        return true;
    }
}
