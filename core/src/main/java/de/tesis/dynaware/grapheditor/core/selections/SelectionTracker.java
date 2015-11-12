package de.tesis.dynaware.grapheditor.core.selections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker {

    private final ObservableList<GNode> selectedNodes = FXCollections.observableArrayList();
    private final ObservableList<GConnection> selectedConnections = FXCollections.observableArrayList();
    private final ObservableList<GJoint> selectedJoints = FXCollections.observableArrayList();

    private final SkinLookup skinLookup;
    
    private final List<GSkin> observedSkins = new ArrayList<>();
    private final ChangeListener<Boolean> selectionChangeListener = this::selectionChanged;

    /**
     * Creates a new {@link SelectionTracker} instance.
     *
     * @param skinLookup the {@link SkinLookup} for this graph editor instance
     */
    public SelectionTracker(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
    }

    /**
     * Initializes the selection tracker for the given model.
     *
     * @param model the {@link GModel} instance being edited
     */
    public void initialize(final GModel model) {
        for(final Iterator<GSkin> iter = observedSkins.iterator(); iter.hasNext();) {
            final GSkin next = iter.next();
            next.selectedProperty().removeListener(selectionChangeListener);
            iter.remove();
        }
        trackNodes(model);
        trackConnectionsAndJoints(model);
    }

    /**
     * Gets the observable list of selected nodes.
     *
     * @return the list of selected nodes
     */
    public ObservableList<GNode> getSelectedNodes() {
        return selectedNodes;
    }

    /**
     * Gets the observable list of selected connections.
     *
     * @return the list of selected connections
     */
    public ObservableList<GConnection> getSelectedConnections() {
        return selectedConnections;
    }

    /**
     * Gets the observable list of selected joints.
     *
     * @return the list of selected joints
     */
    public ObservableList<GJoint> getSelectedJoints() {
        return selectedJoints;
    }

    /**
     * Creates listeners to keep track of selected nodes.
     *
     * @param model the {@link GModel} instance being edited
     */
    private void trackNodes(final GModel model) {
        selectedNodes.clear();

        for (final GNode node : model.getNodes()) {
            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
            if (nodeSkin != null) {
                addRemove(selectedNodes, node, nodeSkin.isSelected());
                observedSkins.add(nodeSkin);
                nodeSkin.selectedProperty().addListener(selectionChangeListener);
            }
        }
    }
    
    private void selectionChanged(final Observable observable, final boolean oldValue, final boolean newValue) {
        if (observable instanceof BooleanProperty) {
            final BooleanProperty property = (BooleanProperty) observable;
            if (property.getBean() instanceof GNodeSkin) {
                final GNode node = ((GNodeSkin) property.getBean()).getNode();
                addRemove(selectedNodes, node, newValue);
            }
            else if (property.getBean() instanceof GJointSkin) {
                final GJoint joint = ((GJointSkin) property.getBean()).getJoint();
                addRemove(selectedJoints, joint, newValue);
            }
            else if (property.getBean() instanceof GConnectionSkin) {
                final GConnection connection = ((GConnectionSkin) property.getBean()).getConnection();
                addRemove(selectedConnections, connection, newValue);
            }
        }
    }
    
    private static <T> void addRemove(final ObservableList<T> list, final T element, final boolean add) {
        if(add) {
            if(!list.contains(element)) {
                list.add(element);
            }
        }
        else {
            list.remove(element);
        }
    }

    /**
     * Creates listeners to keep track of selected connections and joints.
     *
     * @param model the {@link GModel} instance being edited
     */
    private void trackConnectionsAndJoints(final GModel model) {

        selectedConnections.clear();
        selectedJoints.clear();

        for (final GConnection connection : model.getConnections()) {

            final GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
            if (connectionSkin != null) {
                addRemove(selectedConnections, connection, connectionSkin.isSelected());

                observedSkins.add(connectionSkin);
                connectionSkin.selectedProperty().addListener(selectionChangeListener);
            }

            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
                if (jointSkin != null) {
                    addRemove(selectedJoints, joint, jointSkin.isSelected());

                    observedSkins.add(jointSkin);
                    jointSkin.selectedProperty().addListener(selectionChangeListener);
                }
            }
        }
    }
}
