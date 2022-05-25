package io.github.eckig.grapheditor.core.selections;

import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import io.github.eckig.grapheditor.GSkin;
import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 * Provides observable lists of selected nodes and joints for convenience.
 */
public class SelectionTracker
{

    private final ObservableSet<EObject> selectedElements = FXCollections.observableSet(new HashSet<>());
    private final SkinLookup skinLookup;

    /**
     * Creates a new {@link SelectionTracker} instance.
     *
     * @param skinLookup
     *         the {@link SkinLookup}
     */
    public SelectionTracker(final SkinLookup skinLookup)
    {
        this.skinLookup = skinLookup;
        selectedElements.addListener(this::selectedElementsChanged);
    }

    private void selectedElementsChanged(final SetChangeListener.Change<? extends EObject> change)
    {
        if (change.wasRemoved())
        {
            update(change.getElementRemoved());
        }
        if (change.wasAdded())
        {
            update(change.getElementAdded());
        }
    }

    private void update(final EObject obj)
    {
        GSkin<?> skin = null;
        if (obj instanceof GNode n)
        {
            skin = skinLookup.lookupNode(n);
        }
        else if (obj instanceof GJoint j)
        {
            skin = skinLookup.lookupJoint(j);
        }
        else if (obj instanceof GConnection c)
        {
            skin = skinLookup.lookupConnection(c);
        }
        else if (obj instanceof GConnector c)
        {
            skin = skinLookup.lookupConnector(c);
        }

        if (skin != null)
        {
            skin.updateSelection();
        }
    }

    /**
     * Initializes the selection tracker for the given model.
     */
    public void initialize()
    {
        selectedElements.clear();
    }

    /**
     * @return the list of currently selected nodes
     */
    public List<GNode> getSelectedNodes()
    {
        return selectedElements.stream().filter(e -> e instanceof GNode).map(e -> (GNode) e).toList();
    }

    /**
     * @return the list of currently selected connections
     */
    public List<GConnection> getSelectedConnections()
    {
        return selectedElements.stream().filter(e -> e instanceof GConnection).map(e -> (GConnection) e).toList();
    }

    /**
     * @return the list of currently selected joints
     */
    public List<GJoint> getSelectedJoints()
    {
        return selectedElements.stream().filter(e -> e instanceof GJoint).map(e -> (GJoint) e).toList();
    }

    public ObservableSet<EObject> getSelectedItems()
    {
        return selectedElements;
    }
}
