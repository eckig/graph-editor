/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.tree.validators;

import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.demo.GraphEditorDemo;
import de.tesis.dynaware.grapheditor.demo.tree.skins.TreeSkinConstants;
import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * Validation rules for how connectors can be connected for the 'tree-like' graph.
 *
 * <p>
 * Not part of the graph editor library, only used in the {@link GraphEditorDemo} application.
 * </p>
 */
public class TreeConnectorValidator implements GConnectorValidator {

    @Override
    public boolean prevalidate(final GConnector source, final GConnector target) {

        if (source == null || target == null) {
            return false;
        } else if (source.equals(target)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(final GConnector source, final GConnector target) {

        if (source.getType() == null || target.getType() == null) {
            return false;
        } else if (source.getParent().equals(target.getParent())) {
            return false;
        } else if (source.getType().equals(target.getType())) {
            return false;
        } else if (source.getType().equals(TreeSkinConstants.TREE_INPUT) && !source.getConnections().isEmpty()) {
            return false;
        } else if (target.getType().equals(TreeSkinConstants.TREE_INPUT) && !target.getConnections().isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public String createConnectionType(final GConnector source, final GConnector target) {
        return TreeSkinConstants.TREE_CONNECTION;
    }

    @Override
    public String createJointType(final GConnector source, final GConnector target) {
        return null;
    }
}
