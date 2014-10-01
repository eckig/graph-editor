/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.data;

import org.mockito.Mockito;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GConnectorSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GTailSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import de.tesis.dynaware.grapheditor.utils.ResizableBox;

public class MockSkinLookupFactory {

    /**
     * Creates a mock skin-lookup for the given model.
     *
     * <p>
     * The lookup methods will return mocked skin instances with root nodes whose positions match those in the model
     * provided. The mock skin-lookup is therefore 'synced' with this model.
     * </p>
     *
     * <p>
     * Widths and heights of the root nodes will still unfortunately be zero, because these values are not settable, and
     * the get methods are final and cannot be stubbed.
     * </p>
     *
     * @param model the {@link GModel} that the skin layout values should be synced with
     * @return a mock {@link SkinLookup} synced with the given model
     */
    public static SkinLookup createSkinLookup(final GModel model) {

        final SkinLookup skinLookup = Mockito.mock(SkinLookup.class);

        for (final GNode node : model.getNodes()) {

            final GNodeSkin nodeSkin = Mockito.mock(GNodeSkin.class);
            final ResizableBox root = Mockito.spy(new ResizableBox());

            Mockito.when(skinLookup.lookupNode(node)).thenReturn(nodeSkin);
            Mockito.when(nodeSkin.getRoot()).thenReturn(root);

            root.setLayoutX(node.getX());
            root.setLayoutY(node.getY());

            for (final GConnector connector : node.getConnectors()) {

                final GConnectorSkin connectorSkin = Mockito.mock(GConnectorSkin.class);
                Mockito.when(skinLookup.lookupConnector(connector)).thenReturn(connectorSkin);

                final GTailSkin tailSkin = Mockito.mock(GTailSkin.class);
                Mockito.when(skinLookup.lookupTail(connector)).thenReturn(tailSkin);
            }
        }

        for (final GConnection connection : model.getConnections()) {

            final GConnectionSkin connectionSkin = Mockito.mock(GConnectionSkin.class);
            Mockito.when(skinLookup.lookupConnection(connection)).thenReturn(connectionSkin);

            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = Mockito.mock(GJointSkin.class);
                final DraggableBox root = Mockito.spy(new DraggableBox());

                Mockito.when(skinLookup.lookupJoint(joint)).thenReturn(jointSkin);
                Mockito.when(jointSkin.getRoot()).thenReturn(root);

                root.setLayoutX(joint.getX());
                root.setLayoutY(joint.getY());
            }
        }

        return skinLookup;
    }
}
