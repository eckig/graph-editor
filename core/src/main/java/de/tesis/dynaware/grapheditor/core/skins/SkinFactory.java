/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.skins;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectionSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultJointSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultNodeSkin;
import de.tesis.dynaware.grapheditor.core.skins.defaults.DefaultTailSkin;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Resposible for instantiating skins.
 *
 * <p>
 * Stores any custom skin classes that have been set. Uses these when creating new skins, if a custom skin class is
 * present for the given type. Otherwise uses the default skin class.
 * </p>
 */
public class SkinFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkinFactory.class);

    private final Map<String, Class<? extends GNodeSkin>> nodeSkins = new HashMap<>();
    private final Map<String, Class<? extends GConnectorSkin>> connectorSkins = new HashMap<>();
    private final Map<String, Class<? extends GConnectionSkin>> connectionSkins = new HashMap<>();
    private final Map<String, Class<? extends GJointSkin>> jointSkins = new HashMap<>();
    private final Map<String, Class<? extends GTailSkin>> tailSkins = new HashMap<>();

    /**
     * Sets the custom node skin class for the given type.
     *
     * @param type the {@link GNode} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GNodeSkin}
     */
    public void setNodeSkin(final String type, final Class<? extends GNodeSkin> skin) {
        nodeSkins.put(type, skin);
    }

    /**
     * Sets the custom connector skin class for the given type.
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectorSkin}
     */
    public void setConnectorSkin(final String type, final Class<? extends GConnectorSkin> skin) {
        connectorSkins.put(type, skin);
    }

    /**
     * Sets the custom connection skin class for the given type.
     *
     * @param type the {@link GConnection} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectionSkin}
     */
    public void setConnectionSkin(final String type, final Class<? extends GConnectionSkin> skin) {
        connectionSkins.put(type, skin);
    }

    /**
     * Sets the custom joint skin class for the given type.
     *
     * @param type the {@link GJoint} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GJointSkin}
     */
    public void setJointSkin(final String type, final Class<? extends GJointSkin> skin) {
        jointSkins.put(type, skin);
    }

    /**
     * Sets the custom tail skin class for the given type.
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GTailSkin}
     */
    public void setTailSkin(final String type, final Class<? extends GTailSkin> skin) {
        tailSkins.put(type, skin);
    }

    /**
     * Creates a new skin instance for the given node.
     *
     * @param node the {@link GNode} for which a skin should be created
     *
     * @return a new {@link GNodeSkin} instance
     */
    public GNodeSkin createNodeSkin(final GNode node) {

        if (node == null) {
            return null;
        } else if (node.getType() == null) {
            return new DefaultNodeSkin(node);
        }

        final Class<? extends GNodeSkin> skinClass = nodeSkins.get(node.getType());

        if (skinClass == null) {
            return new DefaultNodeSkin(node);
        } else {
            try {
                final Constructor<? extends GNodeSkin> constructor = skinClass.getConstructor(GNode.class);
                return constructor.newInstance(node);
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_SKIN, skinClass.getName());
                return new DefaultNodeSkin(node);
            }
        }
    }

    /**
     * Creates a new skin instance for the given connector.
     *
     * @param connector the {@link GConnector} for which a skin should be created
     *
     * @return a new {@link GConnectorSkin} instance
     */
    public GConnectorSkin createConnectorSkin(final GConnector connector) {

        if (connector == null) {
            return null;
        } else if (connector.getType() == null) {
            return new DefaultConnectorSkin(connector);
        }

        final Class<? extends GConnectorSkin> skinClass = connectorSkins.get(connector.getType());

        if (skinClass == null) {
            return new DefaultConnectorSkin(connector);
        } else {
            try {
                final Constructor<? extends GConnectorSkin> constructor = skinClass.getConstructor(GConnector.class);
                return constructor.newInstance(connector);
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_SKIN, skinClass.getName());
                return new DefaultConnectorSkin(connector);
            }
        }
    }

    /**
     * Creates a new skin instance for the given connection.
     *
     * @param connection the {@link GConnection} for which a skin should be created
     *
     * @return a new {@link GConnectionSkin} instance
     */
    public GConnectionSkin createConnectionSkin(final GConnection connection) {

        if (connection == null) {
            return null;
        } else if (connection.getType() == null) {
            return new DefaultConnectionSkin(connection);
        }

        final Class<? extends GConnectionSkin> skinClass = connectionSkins.get(connection.getType());

        if (skinClass == null) {
            return new DefaultConnectionSkin(connection);
        } else {
            try {
                final Constructor<? extends GConnectionSkin> constructor = skinClass.getConstructor(GConnection.class);
                return constructor.newInstance(connection);
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_SKIN, skinClass.getName());
                return new DefaultConnectionSkin(connection);
            }
        }
    }

    /**
     * Creates a new skin instance for the given joint.
     *
     * @param joint the {@link GJoint} for which a skin should be created
     *
     * @return a new {@link GJointSkin} instance
     */
    public GJointSkin createJointSkin(final GJoint joint) {

        if (joint == null) {
            return null;
        } else if (joint.getType() == null) {
            return new DefaultJointSkin(joint);
        }

        final Class<? extends GJointSkin> skinClass = jointSkins.get(joint.getType());

        if (skinClass == null) {
            return new DefaultJointSkin(joint);
        } else {
            try {
                final Constructor<? extends GJointSkin> constructor = skinClass.getConstructor(GJoint.class);
                return constructor.newInstance(joint);
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_SKIN, skinClass.getName());
                return new DefaultJointSkin(joint);
            }
        }
    }

    /**
     * Creates a new tail skin instance for the given connector.
     *
     * @param connector the {@link GConnector} for which a skin should be created
     *
     * @return a new {@link GTailSkin} instance
     */
    public GTailSkin createTailSkin(final GConnector connector) {

        if (connector == null) {
            return null;
        } else if (connector.getType() == null) {
            return new DefaultTailSkin(connector);
        }

        final Class<? extends GTailSkin> skinClass = tailSkins.get(connector.getType());

        if (skinClass == null) {
            return new DefaultTailSkin(connector);
        } else {
            try {
                final Constructor<? extends GTailSkin> constructor = skinClass.getConstructor(GConnector.class);
                return constructor.newInstance(connector);
            } catch (final ReflectiveOperationException e) {
                LOGGER.error(LogMessages.CANNOT_INSTANTIATE_SKIN, skinClass.getName());
                return new DefaultTailSkin(connector);
            }
        }
    }
}
