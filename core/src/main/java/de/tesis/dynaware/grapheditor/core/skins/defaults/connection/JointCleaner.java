/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins.defaults.connection;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.utils.EventUtils;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.utils.GeometryUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * Monitors joint positions and cleans up / removes unnecessary joints.
 */
public class JointCleaner {

    private final GConnection mConnection;
    private final Map<Region, EventHandler<MouseEvent>> mCleaningHandlers = new HashMap<>();

    private GraphEditor mGraphEditor;

    /**
     * Creates a new joint cleaner.
     *
     * @param pConnection the connection whose joints should be cleaned up / removed
     */
    public JointCleaner(final GConnection pConnection) {
        mConnection = pConnection;
    }

    /**
     * Sets the graph editor instance currently in use.
     *
     * @param pGraphEditor the {@link GraphEditor} instance currently in use
     */
    public void setGraphEditor(final GraphEditor pGraphEditor) {
        mGraphEditor = pGraphEditor;
    }

    /**
     * Adds handlers to remove all joints that are on top of each other after a mouse-released gesture.
     *
     * @param pJointSkins the connection's joint skins
     */
    public void addCleaningHandlers(final List<GJointSkin> pJointSkins)
    {
        EventUtils.removeEventHandlers(mCleaningHandlers, MouseEvent.MOUSE_RELEASED);

        for (final GJointSkin jointSkin : pJointSkins)
        {
            final Region jointRegion = jointSkin.getRoot();

            final EventHandler<MouseEvent> newHandler = event ->
            {
                final Parent parent = jointRegion.getParent();

                if (pJointSkins.size() == 2 || !event.getButton().equals(MouseButton.PRIMARY))
                {
                    return;
                }

                final List<Point2D> jointPositions = GeometryUtils.getJointPositions(pJointSkins);
                final BitSet jointsToCleanUp = findJointsToCleanUp(jointPositions);

                if (!jointsToCleanUp.isEmpty())
                {
                    final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(mConnection);
                    final CompoundCommand command = new CompoundCommand();

                    final GModel model = mGraphEditor.getModel();
                    final SkinLookup skinLookup = mGraphEditor.getSkinLookup();

                    JointCommands.removeJoints(command, jointsToCleanUp, mConnection);
                    Commands.updateLayoutValues(command, model, skinLookup);

                    if (command.canExecute())
                    {
                        editingDomain.getCommandStack().execute(command);
                    }
                }

                parent.layout();
            };

            jointRegion.addEventHandler(MouseEvent.MOUSE_RELEASED, newHandler);
            mCleaningHandlers.put(jointRegion, newHandler);
        }
    }

    /**
     * Finds the joints that should be cleaned up / removed from a list of joint positions.
     *
     * @param jointPositions a list of {@link Point2D} instances containing the x and y values of the joints
     * @return a set of integers specifying the indices of the joints to be removed
     */
    public static BitSet findJointsToCleanUp(final List<Point2D> jointPositions)
    {
        final BitSet jointsToCleanUp = new BitSet(jointPositions.size());
        final List<Point2D> remainingJointPositions = new ArrayList<>(jointPositions);

        Point2D removed = removeJointPair(remainingJointPositions);

        while (removed != null) {

            int jointsCleaned = 0;
            for (int i = 0; i < jointPositions.size(); i++) {

                final boolean positionMatch = removed.equals(jointPositions.get(i));
                final boolean alreadyCounted = jointsToCleanUp.get(i);

                if (positionMatch && !alreadyCounted) {
                    jointsToCleanUp.set(i);
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
