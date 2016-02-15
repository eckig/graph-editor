package de.tesis.dynaware.grapheditor.core.selections;

import de.tesis.dynaware.grapheditor.GConnectionSkin;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.SkinSelectionEvent;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker {

    private final ObservableList<GNode> selectedNodes = FXCollections.observableArrayList();
    private final ObservableList<GConnection> selectedConnections = FXCollections.observableArrayList();
    private final ObservableList<GJoint> selectedJoints = FXCollections.observableArrayList();

    private final SkinLookup skinLookup;
    private final GraphEditorView view;
    
    private final EventHandler<SkinSelectionEvent> skinSelectionChangeHandler = this::handleSelectionChangedEvent;

    /**
     * Creates a new {@link SelectionTracker} instance.
     *
     * @param graphEditor the {@link GraphEditor}
     */
    public SelectionTracker(final SkinLookup skinLookup, final GraphEditorView view) {

        this.view = view;
        this.skinLookup = skinLookup;
        this.view.addEventHandler(SkinSelectionEvent.SKIN_SELECTION_ANY,
                new WeakEventHandler<>(skinSelectionChangeHandler));
    }
    
    private void handleSelectionChangedEvent(final SkinSelectionEvent event) {
        
        final boolean selected = event.getEventType() == SkinSelectionEvent.SKIN_SELECTED;
        final GSkin skin = event.getSkin();
        
        if (skin instanceof GNodeSkin) {
            final GNode node = ((GNodeSkin) skin).getNode();
            addRemove(selectedNodes, node, selected);
        }
        else if (skin instanceof GJointSkin) {
            final GJoint joint = ((GJointSkin) skin).getJoint();
            addRemove(selectedJoints, joint, selected);
        }
        else if (skin instanceof GConnectionSkin) {
            final GConnection connection = ((GConnectionSkin) skin).getConnection();
            addRemove(selectedConnections, connection, selected);
        }
    }

    /**
     * Initializes the selection tracker for the given model.
     *
     * @param model the {@link GModel} instance being edited
     */
    public void initialize(final GModel model) {
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

    private void trackNodes(final GModel model) {
        selectedNodes.clear();

        for (final GNode node : model.getNodes()) {
            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
            if (nodeSkin != null) {
                addRemove(selectedNodes, node, nodeSkin.isSelected());
            }
        }
    }
    
    private void trackConnectionsAndJoints(final GModel model) {

        selectedConnections.clear();
        selectedJoints.clear();

        for (final GConnection connection : model.getConnections()) {

            final GConnectionSkin connectionSkin = skinLookup.lookupConnection(connection);
            if (connectionSkin != null) {
                addRemove(selectedConnections, connection, connectionSkin.isSelected());
            }

            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
                if (jointSkin != null) {
                    addRemove(selectedJoints, joint, jointSkin.isSelected());
                }
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
}
