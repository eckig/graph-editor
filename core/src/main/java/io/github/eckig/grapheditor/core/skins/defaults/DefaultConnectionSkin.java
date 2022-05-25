/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package io.github.eckig.grapheditor.core.skins.defaults;

import java.util.List;

import io.github.eckig.grapheditor.core.connections.RectangularConnections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.skins.defaults.connection.CursorOffsetCalculator;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointAlignmentManager;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointCleaner;
import io.github.eckig.grapheditor.core.skins.defaults.connection.JointCreator;
import io.github.eckig.grapheditor.core.skins.defaults.connection.SimpleConnectionSkin;
import io.github.eckig.grapheditor.model.GConnection;

/**
 * The default connection skin.
 *
 * <p>
 * Extension of {@link SimpleConnectionSkin} that provides a mechanism for creating and removing joints.
 * </p>
 */
public class DefaultConnectionSkin extends SimpleConnectionSkin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionSkin.class);

    private final JointCreator jointCreator;
    private final JointCleaner jointCleaner;
    private final JointAlignmentManager jointAlignmentManager;
    private final CursorOffsetCalculator cursorOffsetCalculator;

    /**
     * Creates a new default connection skin instance.
     *
     * @param connection the {@link GConnection} the skin is being created for
     */
    public DefaultConnectionSkin(final GConnection connection) {

        super(connection);

        performChecks();

        cursorOffsetCalculator = new CursorOffsetCalculator(connection, path, backgroundPath, connectionSegments);
        jointCreator = new JointCreator(connection, cursorOffsetCalculator);
        jointCleaner = new JointCleaner(connection);
        jointAlignmentManager = new JointAlignmentManager(connection);

        jointCreator.addJointCreationHandler(root);
    }

    @Override
    public void setGraphEditor(final GraphEditor graphEditor) {

        super.setGraphEditor(graphEditor);

        jointCreator.setGraphEditor(graphEditor);
        jointCleaner.setGraphEditor(graphEditor);
        jointAlignmentManager.setSkinLookup(graphEditor.getSkinLookup());
    }

    @Override
    public void setJointSkins(final List<GJointSkin> jointSkins) {

        super.setJointSkins(jointSkins);

        jointCleaner.addCleaningHandlers(jointSkins);
        jointAlignmentManager.addAlignmentHandlers(jointSkins);
    }

    /**
     * Checks that the connection has the correct values to be displayed using
     * this skin.
     */
    private void performChecks()
    {
        if (!RectangularConnections.checkJointCount(getItem()))
        {
            LOGGER.error("Joint count not compatible with source and target connector types.");
        }
    }
}
