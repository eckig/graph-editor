package de.tesis.dynaware.grapheditor.core.selections;

import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.GSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker {

    private final ObservableList<EObject> selectedElements = FXCollections.observableArrayList();
    
    private ObservableList<GNode> selectedNodes;
    private ObservableList<GConnection> selectedConnections;
    private ObservableList<GJoint> selectedJoints;

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
    
	private void selectedElementsChanged(final ListChangeListener.Change<? extends EObject> change) {
		while (change.next()) {
			if (change.wasRemoved()) {
				for (final EObject removed : change.getRemoved()) {
					update(removed);
				}
			}
			if (change.wasAdded()) {
	            for (int i = change.getFrom(); i < change.getTo(); i++) {
	                update(selectedElements.get(i));
	            }
			}
		}
	}
	
	private void update(final EObject obj) {
        
        GSkin<?> skin = null;
        if (obj instanceof GNode) {
            skin = skinLookup.lookupNode((GNode) obj);
        }
        else if (obj instanceof GJoint) {
            skin = skinLookup.lookupJoint((GJoint) obj);
        }
        else if (obj instanceof GConnection) {
            skin = skinLookup.lookupConnection((GConnection) obj);
        }
        
        if(skin != null) {
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
     * Gets the observable list of selected nodes.
     *
     * @return the list of selected nodes
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ObservableList<GNode> getSelectedNodes() {
    	if(selectedNodes == null) {
    		selectedNodes = new FilteredList(selectedElements, e -> e instanceof GNode);
    	}
        return selectedNodes;
    }

    /**
     * Gets the observable list of selected connections.
     *
     * @return the list of selected connections
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ObservableList<GConnection> getSelectedConnections() {
    	if(selectedConnections == null) {
    		selectedConnections = new FilteredList(selectedElements, e -> e instanceof GConnection);
    	}
        return selectedConnections;
    }

    /**
     * Gets the observable list of selected joints.
     *
     * @return the list of selected joints
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ObservableList<GJoint> getSelectedJoints() {
    	if(selectedJoints == null) {
    		selectedJoints = new FilteredList(selectedElements, e -> e instanceof GJoint);
    	}
        return selectedJoints;
    }
    
    public ObservableList<EObject> getSelectedItems() {
    	return selectedElements;
    }
}
