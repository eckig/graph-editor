/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;

/**
 * Monitors joint positions and cleans up / removes unnecessary joints.
 */
public class JointCleaner {

    private final GConnection connection;
    private final Map<GJoint, EventHandler<MouseEvent>> cleaningHandlers = new HashMap<>();

    private GraphEditor graphEditor;

    /**
     * Creates a new joint cleaner.
     *
     * @param connection the connection whose joints should be cleaned up / removed
     */
    public JointCleaner(final GConnection connection) {
        this.connection = connection;
    }

    /**
     * Sets the graph editor instance currently in use.
     *
     * @param graphEditor the {@link GraphEditor} instance currently in use
     */
    public void setGraphEditor(final GraphEditor graphEditor) {
        this.graphEditor = graphEditor;
    }

    /**
     * Adds handlers to remove all joints that are on top of each other after a mouse-released gesture.
     *
     * @param jointSkins the connection's joint skins
     */
    public void addCleaningHandlers(final List<GJointSkin> jointSkins) {

        final Map<GJoint, EventHandler<MouseEvent>> oldCleaningHandlers = new HashMap<>(cleaningHandlers);

        cleaningHandlers.clear();

        for (final GJointSkin jointSkin : jointSkins) {

            final GJoint joint = jointSkin.getJoint();
            final Region jointRegion = jointSkin.getRoot();
            final EventHandler<MouseEvent> oldHandler = oldCleaningHandlers.get(joint);

            if (oldHandler != null) {
                jointRegion.removeEventHandler(MouseEvent.MOUSE_RELEASED, oldHandler);
            }

            final EventHandler<MouseEvent> newHandler = event -> {

                final Parent parent = jointRegion.getParent();

                if (jointSkins.size() == 2 || !event.getButton().equals(MouseButton.PRIMARY)) {
                    return;
                }

                final List<Point2D> jointPositions = GeometryUtils.getJointPositions(jointSkins);
                final Set<Integer> jointsToCleanUp = findJointsToCleanUp(jointPositions);

                if (!jointsToCleanUp.isEmpty()) {

                    final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(connection);
                    final CompoundCommand command = new CompoundCommand();

                    final GModel model = graphEditor.getModel();
                    final SkinLookup skinLookup = graphEditor.getSkinLookup();

                    JointCommands.removeJoints(command, jointsToCleanUp, connection);
                    Commands.updateLayoutValues(command, model, skinLookup);

                    final SelectionManager selectionManager = graphEditor.getSelectionManager();

                    selectionManager.backup();

                    if (command.canExecute()) {
                        editingDomain.getCommandStack().execute(command);
                    }

                    selectionManager.restore();
                }

                parent.layout();
            };

            jointRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, newHandler);
            cleaningHandlers.put(joint, newHandler);
        }
    }

    /**
     * Finds the joints that should be cleaned up / removed from a list of joint positions.
     *
     * @param jointPositions a list of {@link Point2D} instances containing the x and y values of the joints
     * @return a set of integers specifying the indices of the joints to be removed
     */
    public static Set<Integer> findJointsToCleanUp(final List<Point2D> jointPositions) {

        final Set<Integer> jointsToCleanUp = new HashSet<>();
        final List<Point2D> remainingJointPositions = new ArrayList<>(jointPositions);

        Point2D removed = removeJointPair(remainingJointPositions);

        while (removed != null) {

            int jointsCleaned = 0;
            for (int i = 0; i < jointPositions.size(); i++) {

                final boolean positionMatch = removed.equals(jointPositions.get(i));
                final boolean alreadyCounted = jointsToCleanUp.contains(i);

                if (positionMatch && !alreadyCounted) {
                    jointsToCleanUp.add(i);
                    jointsCleaned++;
                }

                if (jointsCleaned == 2) {
                    break;
                }
            }

            removed = removeJointPair(remainingJointPositions);
        }

        return jointsToCleanUp;
    }

    /**
     * Finds the first pair of identical joint positions and removes them from the list.
     *
     * @param jointPositions a list of {@link Point2D} instances containing the x and y values of the joints
     * @return the position of the removed joints, or {@code null} if nothing was found to remove
     */
    private static Point2D removeJointPair(final List<Point2D> jointPositions) {

        int foundIndex = -1;
        Point2D foundPosition = null;

        if (jointPositions.size() > 2) {
            for (int i = 0; i < jointPositions.size() - 1; i++) {

                final Point2D currentPosition = jointPositions.get(i);
                final Point2D nextPosition = jointPositions.get(i + 1);

                if (currentPosition.getX() == nextPosition.getX() && currentPosition.getY() == nextPosition.getY()) {
                    foundIndex = i;
                    foundPosition = currentPosition;
                }
            }
        }

        if (foundIndex >= 0) {
            jointPositions.remove(foundIndex + 1);
            jointPositions.remove(foundIndex);
        }

        return foundPosition;
    }
}
