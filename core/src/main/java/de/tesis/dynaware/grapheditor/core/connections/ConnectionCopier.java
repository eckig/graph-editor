package de.tesis.dynaware.grapheditor.core.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.util.EcoreUtil;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GNode;


/**
 * Helper methods to copy {@link GConnection connections}
 */
public final class ConnectionCopier
{

    /**
     * Static class.
     */
    private ConnectionCopier()
    {
    }

    /**
     * Copies connection information from one set of nodes to another.
     *
     * <p>
     * Connections between nodes in the <b>keys</b> of the input map are copied
     * (including their joint information) and the new connections are set
     * inside the corresponding nodes in the <b>values</b> of the input map.
     * </p>
     *
     * <p>
     * The new connection information is set <em>directly</em>. EMF commands are
     * not used
     * </p>
     *
     * @param copies
     *            a map that links source nodes to their copies in its key-value
     *            pairs
     * @return the list of created connections
     */
    public static List<GConnection> copyConnections(final Map<GNode, GNode> copies)
    {
        final Map<GConnection, GConnection> copiedConnections = new HashMap<>();

        for (final GNode node : copies.keySet())
        {
            final GNode copy = copies.get(node);

            for (final GConnector connector : node.getConnectors())
            {
                final int connectorIndex = node.getConnectors().indexOf(connector);
                final GConnector copiedConnector = copy.getConnectors().get(connectorIndex);

                copiedConnector.getConnections().clear();

                for (final GConnection connection : connector.getConnections())
                {
                    final GNode opposingNode = getOpposingNode(connector, connection);

                    final boolean opposingNodePresent = copies.containsKey(opposingNode);

                    if (opposingNodePresent)
                    {
                        final GConnection copiedConnection;
                        if (!copiedConnections.containsKey(connection))
                        {
                            copiedConnection = EcoreUtil.copy(connection);
                            copiedConnections.put(connection, copiedConnection);
                        }
                        else
                        {
                            copiedConnection = copiedConnections.get(connection);
                        }

                        if (connection.getSource().equals(connector))
                        {
                            copiedConnection.setSource(copiedConnector);
                        }
                        else
                        {
                            copiedConnection.setTarget(copiedConnector);
                        }

                        copiedConnector.getConnections().add(copiedConnection);
                    }
                }
            }
        }

        return new ArrayList<>(copiedConnections.values());
    }

    /**
     * Gets the node on the other side of the connection to the given connector.
     *
     * @param connector
     *            a {@link GConnector} instance
     * @param connection
     *            a {@link GConnection} attached to this connector
     * @return the {@link GNode} on the other side of the connection, or
     *         {@code null} if none exists
     */
    private static GNode getOpposingNode(final GConnector connector, final GConnection connection)
    {
        GConnector opposingConnector;
        if (connection.getSource().equals(connector))
        {
            opposingConnector = connection.getTarget();
        }
        else
        {
            opposingConnector = connection.getSource();
        }

        if (opposingConnector != null && opposingConnector.getParent() instanceof GNode)
        {
            return opposingConnector.getParent();
        }
        else
        {
            return null;
        }
    }
}
