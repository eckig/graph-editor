/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnectable;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;

/**
 * Responsible for how joints align to adjacent joints when dragged close enough to them in the default connection skin.
 */
public class JointAlignmentManager {

    private final Map<GJointSkin, EventHandler<MouseEvent>> alignmentHandlers = new HashMap<>();

    private SkinLookup skinLookup;

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

            final EventHandler<MouseEvent> newHandler = new EventHandler<MouseEvent>() {

                @Override
                public void handle(final MouseEvent event) {

                    if (jointSkins.size() <= 2) {
                        return;
                    }

                    addHorizontalAlignmentTargets(jointSkin, jointSkins);
                    addVerticalAlignmentTargets(jointSkin, jointSkins);
                }
            };

            root.addEventHandler(MouseEvent.MOUSE_PRESSED, newHandler);
        }
    }

    /**
     * Adds horizontal alignment targets to the given joint skin based on the positions of neighbouring joints.
     *
     * @param jointSkin the current joint skin
     * @param jointSkins all joint skin instances belonging to a connection
     */
    private void addHorizontalAlignmentTargets(final GJointSkin jointSkin, final List<GJointSkin> jointSkins) {

        final int currentIndex = jointSkins.indexOf(jointSkin);

        final List<Double> alignmentValuesX = new ArrayList<>();

        // First check for existence of previous vertical segment that will not move when this joint is dragged.
        if (isPreviousVerticalSegmentStationary(currentIndex, jointSkins)) {
            if (currentIndex % 2 == 1) {
                alignmentValuesX.add(jointSkins.get(currentIndex - 2).getRoot().getLayoutX());
            } else {
                alignmentValuesX.add(jointSkins.get(currentIndex - 1).getRoot().getLayoutX());
            }
        }

        // Then check for existence of next vertical segment that will not move when this joint is dragged.
        if (isNextVerticalSegmentStationary(currentIndex, jointSkins)) {
            if (currentIndex % 2 == 1) {
                alignmentValuesX.add(jointSkins.get(currentIndex + 1).getRoot().getLayoutX());
            } else {
                alignmentValuesX.add(jointSkins.get(currentIndex + 2).getRoot().getLayoutX());
            }
        }

        jointSkin.getRoot().setAlignmentTargetsX(alignmentValuesX);
    }

    /**
     * Adds vertical alignment targets to the given joint skin based on the positions of neighbouring joints.
     *
     * @param jointSkin the current joint skin
     * @param jointSkins all joint skin instances belonging to a connection
     */
    private void addVerticalAlignmentTargets(final GJointSkin jointSkin, final List<GJointSkin> jointSkins) {

        final int currentIndex = jointSkins.indexOf(jointSkin);

        final List<Double> alignmentValuesY = new ArrayList<>();

        // First check for existence of previous horizontal segment that will not move when this joint is dragged.
        if (isPreviousHorizontalSegmentStationary(currentIndex, jointSkins)) {
            if (currentIndex % 2 == 1) {
                alignmentValuesY.add(jointSkins.get(currentIndex - 1).getRoot().getLayoutY());
            } else {
                alignmentValuesY.add(jointSkins.get(currentIndex - 2).getRoot().getLayoutY());
            }
        }

        // Then check for existence of next horizontal segment that will not move when this joint is dragged.
        if (isNextHorizontalSegmentStationary(currentIndex, jointSkins)) {
            if (currentIndex % 2 == 1) {
                alignmentValuesY.add(jointSkins.get(currentIndex + 2).getRoot().getLayoutY());
            } else {
                alignmentValuesY.add(jointSkins.get(currentIndex + 1).getRoot().getLayoutY());
            }
        }

        jointSkin.getRoot().setAlignmentTargetsY(alignmentValuesY);
    }

    /**
     * Checks whether the vertical horizontal segment of the connection will remain stationary when the current joint is
     * dragged.
     *
     * @param currentIndex the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the previous vertical segment of the connection will remaing stationary when the current
     * joint is dragged
     */
    private boolean isPreviousVerticalSegmentStationary(final int currentIndex, final List<GJointSkin> jointSkins) {

        if (currentIndex % 2 == 1 && currentIndex >= 3) {
            if (!jointSkins.get(currentIndex - 2).isSelected() && !jointSkins.get(currentIndex - 3).isSelected()) {
                return true;
            }
        } else if (currentIndex % 2 == 0 && currentIndex >= 2) {
            if (!jointSkins.get(currentIndex - 1).isSelected() && !jointSkins.get(currentIndex - 2).isSelected()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the next vertical segment of the connection will remain stationary when the current joint is
     * dragged.
     *
     * @param currentIndex the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the next vertical segment of the connection will remaing stationary when the current
     * joint is dragged
     */
    private boolean isNextVerticalSegmentStationary(final int currentIndex, final List<GJointSkin> jointSkins) {

        if (currentIndex % 2 == 1 && currentIndex <= jointSkins.size() - 3) {
            if (!jointSkins.get(currentIndex + 1).isSelected() && !jointSkins.get(currentIndex + 2).isSelected()) {
                return true;
            }
        } else if (currentIndex % 2 == 0 && currentIndex <= jointSkins.size() - 4) {
            if (!jointSkins.get(currentIndex + 2).isSelected() && !jointSkins.get(currentIndex + 3).isSelected()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the previous horizontal segment of the connection will remain stationary when the current joint is
     * dragged.
     *
     * <p>
     * This is done by checking if the previous joints or source node are selected. If they are selected, they will move
     * too when the current joint is dragged.
     * </p>
     *
     * @param currentIndex the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the previous horizontal segment of the connection will remaing stationary when the
     * current joint is dragged
     */
    private boolean isPreviousHorizontalSegmentStationary(final int currentIndex, final List<GJointSkin> jointSkins) {

        if (currentIndex == 1) {

            final GJoint currentJoint = jointSkins.get(currentIndex).getJoint();
            final GConnectable sourceParent = currentJoint.getConnection().getSource().getParent();

            if (sourceParent instanceof GNode) {
                final GNodeSkin sourceNodeSkin = skinLookup.lookupNode((GNode) sourceParent);

                if (!sourceNodeSkin.isSelected()) {
                    return true;
                }
            }
        } else if (currentIndex > 1) {
            if (!jointSkins.get(currentIndex - 1).isSelected() && !jointSkins.get(currentIndex - 2).isSelected()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the next horizontal segment of the connection will remain stationary when the current joint is
     * dragged.
     *
     * @param currentIndex the index of the current joint
     * @param jointSkins the list of joint skins for the joint's connection
     *
     * @return {@code true} if the next horizontal segment of the connection will remaing stationary when the current
     * joint is dragged
     */
    private boolean isNextHorizontalSegmentStationary(final int currentIndex, final List<GJointSkin> jointSkins) {

        if (currentIndex == jointSkins.size() - 2) {

            final GJoint currentJoint = jointSkins.get(currentIndex).getJoint();
            final GConnectable targetParent = currentJoint.getConnection().getTarget().getParent();

            if (targetParent instanceof GNode) {
                final GNodeSkin targetNodeSkin = skinLookup.lookupNode((GNode) targetParent);

                if (!targetNodeSkin.isSelected()) {
                    return true;
                }
            }
        } else if (currentIndex < jointSkins.size() - 2) {
            if (!jointSkins.get(currentIndex + 1).isSelected() && !jointSkins.get(currentIndex + 2).isSelected()) {
                return true;
            }
        }

        return false;
    }
}
