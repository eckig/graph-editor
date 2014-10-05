package de.tesis.dynaware.grapheditor.core.selections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker {

    ObservableList<GNode> selectedNodes = FXCollections.observableArrayList();
    ObservableList<GJoint> selectedJoints = FXCollections.observableArrayList();

    private final SkinLookup skinLookup;

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

        selectedNodes.clear();
        selectedJoints.clear();

        for (final GNode node : model.getNodes()) {

            final GNodeSkin nodeSkin = skinLookup.lookupNode(node);

            if (nodeSkin.isSelected()) {
                selectedNodes.add(node);
            }

            nodeSkin.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue && !selectedNodes.contains(node)) {
                    selectedNodes.add(node);
                } else if (!newValue && selectedNodes.contains(node)) {
                    selectedNodes.remove(node);
                }
            });
        }

        for (final GConnection connection : model.getConnections()) {

            for (final GJoint joint : connection.getJoints()) {

                final GJointSkin jointSkin = skinLookup.lookupJoint(joint);

                if (jointSkin.isSelected()) {
                    selectedJoints.add(joint);
                }

                jointSkin.selectedProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue && !selectedJoints.contains(joint)) {
                        selectedJoints.add(joint);
                    } else if (!newValue && selectedJoints.contains(joint)) {
                        selectedJoints.remove(joint);
                    }
                });
            }
        }
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
     * Gets the observable list of selected joints.
     *
     * @return the list of selected joints
     */
    public ObservableList<GJoint> getSelectedJoints() {
        return selectedJoints;
    }
}
