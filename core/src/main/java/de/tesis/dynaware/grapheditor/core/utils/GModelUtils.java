package de.tesis.dynaware.grapheditor.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Helper methods to manipulate a {@link GModel}.
 */
public class GModelUtils {

    /**
     * Static class.
     */
    private GModelUtils() {
    }

    /**
     * Copies connection information from one set of nodes to another.
     *
     * <p>
     * Connections between nodes in the <b>keys</b> of the input map are copied (including their joint information) and
     * the new connections are set inside the corresponding nodes in the <b>values</b> of the input map.
     * </p>
     *
     * <p>
     * The new connection information is set <em>directly</em>. EMF commands are not used
     * </p>
     *
     * @param copies a map that links source nodes to their copies in its key-value pairs
     * @return the list of created connections
     */
    public static List<GConnection> copyConnections(final Map<GNode, GNode> copies) {

        final Map<GConnection, GConnection> copiedConnections = new HashMap<>();

        for (final GNode node : copies.keySet()) {

            final GNode copy = copies.get(node);

            for (final GConnector connector : node.getConnectors()) {

                final int connectorIndex = node.getConnectors().indexOf(connector);
                final GConnector copiedConnector = copy.getConnectors().get(connectorIndex);

                copiedConnector.getConnections().clear();

                for (final GConnection connection : connector.getConnections()) {

                    final GNode opposingNode = getOpposingNode(connector, connection);

                    final boolean opposingNodePresent = copies.containsKey(opposingNode);

                    if (opposingNodePresent) {

                        final GConnection copiedConnection;
                        if (!copiedConnections.containsKey(connection)) {
                            copiedConnection = EcoreUtil.copy(connection);
                            copiedConnections.put(connection, copiedConnection);
                        } else {
                            copiedConnection = copiedConnections.get(connection);
                        }

                        if (connection.getSource().equals(connector)) {
                            copiedConnection.setSource(copiedConnector);
                        } else {
                            copiedConnection.setTarget(copiedConnector);
                        }

                        copiedConnector.getConnections().add(copiedConnection);
                    }
                }
            }
        }

        return new ArrayList<GConnection>(copiedConnections.values());
    }

    /**
     * Gets the node on the other side of the connection to the given connector.
     *
     * @param connector a {@link GConnector} instance
     * @param connection a {@link GConnection} attached to this connector
     * @return the {@link GNode} on the other side of the connection, or {@code null} if none exists
     */
    public static GNode getOpposingNode(final GConnector connector, final GConnection connection) {

        GConnector opposingConnector;
        if (connection.getSource().equals(connector)) {
            opposingConnector = connection.getTarget();
        } else {
            opposingConnector = connection.getSource();
        }

        if (opposingConnector != null && opposingConnector.getParent() instanceof GNode) {
            return (GNode) opposingConnector.getParent();
        } else {
            return null;
        }
    }

    /**
     * Puts all joints in the given model into a new list.
     *
     * @param model a {@link GModel} instance
     * @return a new list containing all joints in this model
     */
    public static List<GJoint> getAllJoints(final GModel model) {

        final List<GJoint> allJoints = new ArrayList<>();

        for (final GConnection connection : model.getConnections()) {
            allJoints.addAll(connection.getJoints());
        }

        return allJoints;
    }
}
