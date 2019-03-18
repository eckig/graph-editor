/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.connections.RectangularConnections;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Responsible for how joints align to adjacent joints when dragged close enough to them in the default connection skin.
 */
public class JointAlignmentManager {

    private final Map<GJointSkin, EventHandler<MouseEvent>> alignmentHandlers = new HashMap<>();
    private final GConnection connection;
    private SkinLookup skinLookup;

    /**
     * Creates a new {@link JointAlignmentManager} instance.
     *
     * @param connection the connection whose joints are being aligned
     */
    public JointAlignmentManager(final GConnection connection) {
        this.connection = connection;
    }

    /**
     * Sets the skin directory instance for this graph editor.
     *
     * @param skinLookup the {@link SkinLookup} instance for this graph editor
     */
    public void setSkinLookup(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
    }

    /**
     * Adds an alignment handler to each of the given joint skins.
     *
     * <p>
     * This is a mouse-pressed handler which checks for appropriate adjacent joints and sets alignment targets in the
     * joint's {@link DraggableBox} root node, so that the joint will align to adjacent joints when dragged near them.
     * </p>
     *
     * @param jointSkins all joint skin instances belonging to a connection
     */
    public void addAlignmentHandlers(final List<GJointSkin> jointSkins) {

        final Map<GJointSkin, EventHandler<MouseEvent>> oldAlignmentHandlers = new HashMap<>(alignmentHandlers);
        alignmentHandlers.clear();

        for (final GJointSkin jointSkin : jointSkins) {

            final EventHandler<MouseEvent> oldHandler = oldAlignmentHandlers.get(jointSkin);

            final DraggableBox root = jointSkin.getRoot();

            if (oldHandler != null) {
                root.removeEventHandler(MouseEvent.MOUSE_PRESSED, oldHandler);
            }

            final EventHandler<MouseEvent> newHandler = event -> {
                addHorizontalAlignmentTargets(jointSkin, jointSkins);
                addVerticalAlignmentTargets(jointSkin, jointSkins);
            };

            root.addEventHandler(MouseEvent.MOUSE_PRESSED, newHandler);
            alignmentHandlers.put(jointSkin, newHandler);
        }
    }

    /**
     * Adds horizontal alignment targets to the given joint skin based on the positions of neighbouring joints.
     *
     * @param jointSkin the current joint skin
     * @param jointSkins all joint skin instances belonging to a connection
     */
    private void addHorizontalAlignmentTargets(final GJointSkin jointSkin, final List<GJointSkin> jointSkins)
    {

        final int index = jointSkins.indexOf(jointSkin);
        final int count = jointSkins.size();

        final List<GJointSkin> alignmentValuesX = new ArrayList<>();

        // First check for existence of previous vertical segment that will not move when this joint is dragged.
        if (isPreviousVerticalSegmentStationary(index, jointSkins))
        {
            if (index == 1)
            {
                alignmentValuesX.add(jointSkins.get(index - 1));
            }
            else if (index > 1)
            {
                alignmentValuesX.add(jointSkins.get(index - 2));
            }
        }

        // Then check for existence of next vertical segment that will not move when this joint is dragged.
        if (isNextVerticalSegmentStationary(index, jointSkins))
        {
            if (index == count - 2)
            {
                alignmentValuesX.add(jointSkins.get(index + 1));
            }
            else if (index < count - 2)
            {
                alignmentValuesX.add(jointSkins.get(index + 2));
            }
        }

        if (!alignmentValuesX.isEmpty())
        {
            final double[] alignmentValues = new double[alignmentValuesX.size()];
            for (int i = 0; i < alignmentValuesX.size(); i++)
            {
                alignmentValues[i] = alignmentValuesX.get(i).getRoot().getLayoutX();
            }
            jointSkin.getRoot().setAlignmentTargetsX(alignmentValues);
        }
        else
        {
            jointSkin.getRoot().setAlignmentTargetsX(null);
        }
    }

    /**
     * Adds vertical alignment targets to the given joint skin based on the positions of neighbouring joints.
     *
     * @param jointSkin the current joint skin
     * @param jointSkins all joint skin instances belonging to a connection
     */
    private void addVerticalAlignmentTargets(final GJointSkin jointSkin, final List<GJointSkin> jointSkins)
    {
        final int index = jointSkins.indexOf(jointSkin);
        final int count = jointSkins.size();

        final List<GJointSkin> alignmentValuesY = new ArrayList<>();

        // First check for existence of previous vertical segment that will not move when this joint is dragged.
        if (isPreviousHorizontalSegmentStationary(index, jointSkins))
        {
            if (index == 1)
            {
                alignmentValuesY.add(jointSkins.get(index - 1));
            }
            else if (index > 1)
            {
                alignmentValuesY.add(jointSkins.get(index - 2));
            }
        }

        // Then check for existence of next vertical segment that will not move when this joint is dragged.
        if (isNextHorizontalSegmentStationary(index, jointSkins))
        {
            if (index == count - 2)
            {
                alignmentValuesY.add(jointSkins.get(index + 1));
            }
            else if (index < count - 2)
            {
                alignmentValuesY.add(jointSkins.get(index + 2));
            }
        }

        if (!alignmentValuesY.isEmpty())
        {
            final double[] alignmentValues = new double[alignmentValuesY.size()];
            for (int i = 0; i < alignmentValuesY.size(); i++)
            {
                alignmentValues[i] = alignmentValuesY.get(i).getRoot().getLayoutY();
            }
            jointSkin.getRoot().setAlignmentTargetsY(alignmentValues);
        }
        else
        {
            jointSkin.getRoot().setAlignmentTargetsY(null);
        }
    }

    /**
     * Checks whether the previous vertical horizontal segment will remain stationary when the current joint is dragged.
     *
     * @param index the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the previous vertical segment of the connection will remain stationary
     */
    private boolean isPreviousVerticalSegmentStationary(final int index, final List<GJointSkin> jointSkins) {

        final boolean firstSegmentHorizontal = RectangularConnections.isSegmentHorizontal(connection, 0);

        if (!firstSegmentHorizontal && (index == 1 || index == 2)) {
            return isNodeStationary(jointSkins.get(index), true);
        } else {
            return isJointPairStationary(index, false, false, jointSkins);
        }
    }

    /**
     * Checks whether the next vertical segment will remain stationary when the current joint is dragged.
     *
     * @param currentIndex the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the next vertical segment of the connection will remain stationary
     */
    private boolean isNextVerticalSegmentStationary(final int index, final List<GJointSkin> jointSkins) {

        final int count = jointSkins.size();
        final boolean lastSegmentHorizontal = RectangularConnections.isSegmentHorizontal(connection, count);

        if (!lastSegmentHorizontal && index >= 0 && (index == count - 2 || index == count - 3)) {
            return isNodeStationary(jointSkins.get(index), false);
        } else {
            return isJointPairStationary(index, false, true, jointSkins);
        }
    }

    /**
     * Checks whether the previous horizontal segment will remain stationary when the current joint is dragged.
     *
     * @param index the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the previous horizontal segment of the connection will remain stationary
     */
    private boolean isPreviousHorizontalSegmentStationary(final int index, final List<GJointSkin> jointSkins) {

        final boolean firstSegmentHorizontal = RectangularConnections.isSegmentHorizontal(connection, 0);

        if (firstSegmentHorizontal && (index == 1 || index == 2)) {
            return isNodeStationary(jointSkins.get(index), true);
        } else {
            return isJointPairStationary(index, true, false, jointSkins);
        }
    }

    /**
     * Checks whether the next horizontal segment will remain stationary when the current joint is dragged.
     *
     * @param index the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the next horizontal segment of the connection will remain stationary
     */
    private boolean isNextHorizontalSegmentStationary(final int index, final List<GJointSkin> jointSkins) {

        final int count = jointSkins.size();
        final boolean lastSegmentHorizontal = RectangularConnections.isSegmentHorizontal(connection, count);

        if (lastSegmentHorizontal && index >= 0 && (index == count - 2 || index == count - 3)) {
            return isNodeStationary(jointSkins.get(index), false);
        } else {
            return isJointPairStationary(index, true, true, jointSkins);
        }
    }

    /**
     * Checks whether a node attached to the connection's source or target connector will move when a joint is dragged.
     *
     * <p>
     * This is evaluated based on whether the node and/or joint are selected.
     * </p>
     *
     * @param jointSkin a {@link GJointSkin} that is dragged
     * @param source {@code true} to query source node, {@code false} to query target node
     * @return {@code true} if the node will move when the given joint is dragged
     */
    private boolean isNodeStationary(final GJointSkin jointSkin, final boolean source) {

        final GConnector connector = source ? connection.getSource() : connection.getTarget();
        final GNode parent = connector.getParent();

        final GNodeSkin nodeSkin = skinLookup.lookupNode(parent);
        return !nodeSkin.isSelected() || !jointSkin.isSelected();
    }

    /**
     * Checks if the previous or next horizontal or vertical joint pair will remain stationary.
     *
     * @param index the dragged joint index in the list of joint skins
     * @param horizontal {@code true} for horizontal segment, {@code false} for vertical
     * @param next {@code true} for next segment, {@code false} for previous
     * @param jointSkins the list of joint skins
     * @return {@code true} if the joint pair will remain stationary when the joint is dragged
     */
    private boolean isJointPairStationary(final int index, final boolean horizontal, final boolean next,
            final List<GJointSkin> jointSkins) {

        final boolean segmentHorizontal = RectangularConnections.isSegmentHorizontal(connection, index + 1);

        final int jump;
        if (segmentHorizontal == (horizontal == next)) {
            jump = 2;
        } else {
            jump = 1;
        }

        final int firstIndex = next ? index + jump : index - jump;
        final int secondIndex = next ? index + jump + 1 : index - jump - 1;

        if (secondIndex >= 0 && secondIndex < jointSkins.size()) {

            final boolean firstNotSelected = !jointSkins.get(firstIndex).isSelected();
            final boolean secondNotSelected = !jointSkins.get(secondIndex).isSelected();
            final boolean draggedNotSelected = !jointSkins.get(index).isSelected();

            return firstNotSelected && secondNotSelected || draggedNotSelected;
        } else {
            return false;
        }
    }
}
