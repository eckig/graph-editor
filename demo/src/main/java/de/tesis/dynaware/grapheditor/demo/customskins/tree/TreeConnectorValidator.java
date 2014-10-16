/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.demo.customskins.tree;

import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.model.GConnector;

/**
 * Validation rules for how connectors can be connected for the 'tree-like' graph.
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
        } else if (source.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)
                && !source.getConnections().isEmpty()) {
            return false;
        } else if (target.getType().equals(TreeSkinConstants.TREE_INPUT_CONNECTOR)
                && !target.getConnections().isEmpty()) {
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
