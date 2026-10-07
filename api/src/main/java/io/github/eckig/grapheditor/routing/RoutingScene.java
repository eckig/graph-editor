package io.github.eckig.grapheditor.routing;

import java.util.List;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

/**
 * Geometry of the graph as seen by a {@link ConnectionRouter} during one layout pass.
 *
 * @since 25.1.0
 */
public interface RoutingScene
{

    /**
     * The model being edited.
     *
     * @return the {@link GModel} being edited
     */
    GModel getModel();

    /**
     * The bounds of a node.
     *
     * @param pNode
     *         {@link GNode}
     * @return the bounds of the node according to the model values
     */
    Bounds getNodeBounds(GNode pNode);

    /**
     * The end point of connections at a connector.
     *
     * @param pConnector
     *         {@link GConnector}
     * @return the current end point of connections at the given connector (live position from the skins), or
     *         {@code null} if not available yet
     */
    ConnectionEndpoint getEndpoint(GConnector pConnector);

    /**
     * The joint positions of a connection.
     *
     * @param pConnection
     *         {@link GConnection}
     * @return the current joint positions (live position from the joint skins, model values if there is no skin)
     */
    List<Point2D> getJointPositions(GConnection pConnection);

    /**
     * Requests another routing pass later, e.g. because some positions are not available yet.
     */
    void retryLater();
}
