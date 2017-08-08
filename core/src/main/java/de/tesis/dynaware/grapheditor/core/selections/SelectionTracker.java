package de.tesis.dynaware.grapheditor.core.selections;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker {

    private final ObservableSet<EObject> selectedElements = FXCollections.observableSet(new HashSet<>());
    private final SkinLookup skinLookup;

    /**
     * Creates a new {@link SelectionTracker} instance.
     *
     * @param graphEditor the {@link GraphEditor}
     */
    public SelectionTracker(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
        selectedElements.addListener(this::selectedElementsChanged);
    }

    private void selectedElementsChanged(final SetChangeListener.Change<? extends EObject> change) {
        if (change.wasRemoved()) {
            update(change.getElementRemoved());
        }
        if (change.wasAdded()) {
            update(change.getElementAdded());
        }
    }

    private void update(final EObject obj) {

        GSkin<?> skin = null;
        if (obj instanceof GNode) {
            skin = skinLookup.lookupNode((GNode) obj);
        } else if (obj instanceof GJoint) {
            skin = skinLookup.lookupJoint((GJoint) obj);
        } else if (obj instanceof GConnection) {
            skin = skinLookup.lookupConnection((GConnection) obj);
        } else if (obj instanceof GConnector) {
            skin = skinLookup.lookupConnector((GConnector) obj);
        }

        if (skin != null) {
            skin.updateSelection();
        }
    }

    /**
     * Initializes the selection tracker for the given model.
     *
     * @param model the {@link GModel} instance being edited
     */
    public void initialize(final GModel model) {
        selectedElements.clear();
    }

    /**
     * @return the list of currently selected nodes
     */
    public List<GNode> getSelectedNodes() {
        return selectedElements.stream().filter(e -> e instanceof GNode).map(e -> (GNode) e)
                .collect(Collectors.toList());
    }

    /**
     * @return the list of currently selected connections
     */
    public List<GConnection> getSelectedConnections() {
        return selectedElements.stream().filter(e -> e instanceof GConnection).map(e -> (GConnection) e)
                .collect(Collectors.toList());
    }

    /**
     * @return the list of currently selected joints
     */
    public List<GJoint> getSelectedJoints() {
        return selectedElements.stream().filter(e -> e instanceof GJoint).map(e -> (GJoint) e)
                .collect(Collectors.toList());
    }

    public ObservableSet<EObject> getSelectedItems() {
        return selectedElements;
    }
}
