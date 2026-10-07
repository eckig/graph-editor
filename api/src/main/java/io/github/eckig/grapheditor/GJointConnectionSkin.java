package io.github.eckig.grapheditor;

import java.util.List;

import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GJoint;

/**
 * A {@link GVisualConnectionSkin} that displays and/or constrains the {@link GJoint joints} of its connection.
 *
 * <p>
 * Joint skins are only created for connections whose skin extends this class. Connection skins computing their own
 * routing (and thus not using joints) should extend {@link GVisualConnectionSkin} directly: no joint skins are
 * created for them and {@link SkinLookup#lookupJoint(GJoint)} returns {@code null} for their joints.
 * </p>
 *
 * @since 25.1.0
 */
public abstract class GJointConnectionSkin extends GVisualConnectionSkin
{

    /**
     * Creates a new {@link GJointConnectionSkin}.
     *
     * @param pConnection
     *         the {@link GConnection} represented by the skin
     */
    public GJointConnectionSkin(final GConnection pConnection)
    {
        super(pConnection);
    }

    /**
     * Sets the skin objects for all joints inside the connection.
     *
     * <p>
     * This will be called as the connection skin is created and whenever the joints of the connection change. The
     * connection skin can manipulate its joint skins if it chooses. For example a 'rectangular' connection skin may
     * restrict the movement of the first and last joints to the x direction only.
     * </p>
     *
     * @param pJointSkins
     *         the list of all {@link GJointSkin} instances associated to the connection
     */
    public abstract void setJointSkins(final List<GJointSkin> pJointSkins);

    /**
     * Called on every layout pass before the connections are routed. Can be used to constrain the joint skins, e.g.
     * to align the first and last joint with the connectors. The default does nothing.
     */
    public void prepareRoute()
    {
        // no constraints by default
    }
}
