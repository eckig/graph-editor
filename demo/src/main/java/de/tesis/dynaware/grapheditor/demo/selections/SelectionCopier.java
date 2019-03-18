package de.tesis.dynaware.grapheditor.demo.selections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.connections.ConnectionCopier;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Region;

/**
 * Manages cut, copy, & paste actions on the current selection.
 *
 * <p>
 * The rules for what is copied are as follows:
 * <ol>
 * <li>All selected nodes and their connectors (containment references) are copied.</li>
 * <li>If a connection's source and target nodes are <b>both</b> copied, the connection and its joints are also copied.</li>
 * </ol>
 * </p>
 */
public class SelectionCopier {

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;

    private static final double BASE_PASTE_OFFSET = 20;

    private final SkinLookup skinLookup;
    private final SelectionManager selectionManager;

    private final List<GNode> copiedNodes = new ArrayList<>();
    private final List<GConnection> copiedConnections = new ArrayList<>();

    private Parent parentAtTimeOfCopy;
    private double parentSceneXAtTimeOfCopy;
    private double parentSceneYAtTimeOfCopy;

    private GModel model;

    /**
     * Creates a new {@link SelectionCopier} instance.
     *
     * @param skinLookup
     *            the {@link SkinLookup} instance for the graph editor
     * @param selectionManager
     *            the {@link SelectionManager} instance for the graph editor
     */
	public SelectionCopier(final SkinLookup skinLookup, final SelectionManager selectionManager) {

        this.skinLookup = skinLookup;
        this.selectionManager = selectionManager;
    }

    /**
     * Initializes the selection copier for the current model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {
        this.model = model;
    }

    /**
     * Copies the current selection and stores it in memory.
     */
    public void copy() {

        if (selectionManager.getSelectedItems().isEmpty()) {
            return;
        }

        copiedNodes.clear();
        copiedConnections.clear();

        final Map<GNode, GNode> copyStorage = new HashMap<>();

        // Don't iterate directly over selectionTracker.getSelectedNodes() because that will not preserve ordering.
        for (final GNode node : model.getNodes()) {
            if (selectionManager.isSelected(node)) {

                final GNode copiedNode = EcoreUtil.copy(node);
                copiedNodes.add(copiedNode);
                copyStorage.put(node, copiedNode);
            }
        }

        copiedConnections.addAll(ConnectionCopier.copyConnections(copyStorage));
        saveParentPositionInScene();
    }

    /**
     * Pastes the most-recently-copied selection.
     *
     * <p>
     * After the paste operation, the newly-pasted elements will be selected.
     * </p>
     *
     * @param consumer a consumer to allow custom commands to be appended to the paste command
     * @return the list of new {@link GNode} instances created by the paste operation
     */
    public List<GNode> paste(final BiConsumer<List<GNode>, CompoundCommand> consumer) {

    	selectionManager.clearSelection();

        final List<GNode> pastedNodes = new ArrayList<>();
        final List<GConnection> pastedConnections = new ArrayList<>();

        preparePastedElements(pastedNodes, pastedConnections);
        addPasteOffset(pastedNodes, pastedConnections);
        checkWithinBounds(pastedNodes, pastedConnections);
        addPastedElements(pastedNodes, pastedConnections, consumer);

        for (final GNode pastedNode : pastedNodes) {
        	selectionManager.select(pastedNode);
        }

        for (final GConnection pastedConnection : pastedConnections) {
            for (final GJoint pastedJoint : pastedConnection.getJoints()) {
            	selectionManager.select(pastedJoint);
            }
        }

        return pastedNodes;
    }

    /**
     * Clears the memory of what was cut / copied. Future paste operations will do nothing.
     */
    public void clearMemory() {
        copiedNodes.clear();
        copiedConnections.clear();
    }

    /**
     * Prepares the lists of pasted nodes and connections.
     *
     * @param pastedNodes an empty list to be filled with pasted nodes
     * @param pastedConnections an empty list to be filled with pasted connections
     */
    private void preparePastedElements(final List<GNode> pastedNodes, final List<GConnection> pastedConnections) {

        final Map<GNode, GNode> pasteStorage = new HashMap<>();

        for (final GNode copiedNode : copiedNodes) {

            final GNode pastedNode = EcoreUtil.copy(copiedNode);
            pastedNodes.add(pastedNode);

            pasteStorage.put(copiedNode, pastedNode);
        }

        pastedConnections.addAll(ConnectionCopier.copyConnections(pasteStorage));
    }

    /**
     * Adds an x and y offset to all nodes and connections that are about to be pasted.
     *
     * @param pastedNodes the nodes that are going to be pasted
     * @param pastedConnections the connections that are going to be pasted
     */
    private void addPasteOffset(final List<GNode> pastedNodes, final List<GConnection> pastedConnections) {

        final Point2D pasteOffset = determinePasteOffset();

        for (final GNode node : pastedNodes) {
            node.setX(node.getX() + pasteOffset.getX());
            node.setY(node.getY() + pasteOffset.getY());
        }

        for (final GConnection connection : pastedConnections) {
            for (final GJoint joint : connection.getJoints()) {
                joint.setX(joint.getX() + pasteOffset.getX());
                joint.setY(joint.getY() + pasteOffset.getY());
            }
        }
    }

    /**
     * Checks that the pasted node and joints will be inside the bounds of their parent.
     *
     * <p>
     * Corrects the x and y positions accordingly if they will be outside the bounds.
     * </p>
     *
     * @param pastedNodes the nodes that are going to be pasted
     * @param pastedConnections the connections that are going to be pasted
     */
    private void checkWithinBounds(final List<GNode> pastedNodes, final List<GConnection> pastedConnections) {

        if (parentAtTimeOfCopy instanceof Region) {

            final Region parentRegion = (Region) parentAtTimeOfCopy;

            final Bounds parentBounds = getBounds(parentRegion);
            final Bounds contentBounds = getContentBounds(pastedNodes, pastedConnections);

            double xCorrection = 0;
            double yCorrection = 0;

            if (contentBounds.startX < parentBounds.startX) {
                xCorrection = parentBounds.startX - contentBounds.startX;
            } else if (contentBounds.endX > parentBounds.endX) {
                xCorrection = parentBounds.endX - contentBounds.endX;
            }

            if (contentBounds.startY < parentBounds.startY) {
                yCorrection = parentBounds.startY - contentBounds.startY;
            } else if (contentBounds.endY > parentBounds.endY) {
                yCorrection = parentBounds.endY - contentBounds.endY;
            }

            if (xCorrection != 0 || yCorrection != 0) {

                for (final GNode node : pastedNodes) {
                    node.setX(node.getX() + xCorrection);
                    node.setY(node.getY() + yCorrection);
                }

                for (final GConnection connection : pastedConnections) {
                    for (final GJoint joint : connection.getJoints()) {
                        joint.setX(joint.getX() + xCorrection);
                        joint.setY(joint.getY() + yCorrection);
                    }
                }
            }
        }
    }

    /**
     * Adds the pasted elements to the graph editor via a single EMF command.
     *
     * @param pastedNodes the pasted nodes to be added
     * @param pastedConnections the pasted connections to be added
     * @param consumer a consumer to allow custom commands to be appended to the paste command
     */
    private void addPastedElements(final List<GNode> pastedNodes, final List<GConnection> pastedConnections,
            final BiConsumer<List<GNode>, CompoundCommand> consumer) {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);
        final CompoundCommand command = new CompoundCommand();

        for (final GNode pastedNode : pastedNodes) {
            command.append(AddCommand.create(editingDomain, model, NODES, pastedNode));
        }

        for (final GConnection pastedConnection : pastedConnections) {
            command.append(AddCommand.create(editingDomain, model, CONNECTIONS, pastedConnection));
        }

        if (command.canExecute()) {
            editingDomain.getCommandStack().execute(command);
        }

        if (consumer != null) {
            consumer.accept(pastedNodes, command);
        }

    }

    /**
     * Saves the position in the scene of the JavaFX {@link Parent} of the node skins.
     */
    private void saveParentPositionInScene() {

        if (!selectionManager.getSelectedItems().isEmpty()) {

            final GNode firstSelectedNode = selectionManager.getSelectedNodes().get(0);
            final GNodeSkin firstSelectedNodeSkin = skinLookup.lookupNode(firstSelectedNode);

            final Node root = firstSelectedNodeSkin.getRoot();
            final Parent parent = root.getParent();

            if (parent != null) {

                parentAtTimeOfCopy = parent;

                final Point2D localToScene = parent.localToScene(0, 0);

                parentSceneXAtTimeOfCopy = localToScene.getX();
                parentSceneYAtTimeOfCopy = localToScene.getY();
            }
        }
    }

    /**
     * Determines the offset by which the new nodes and joints should be positioned relative to the nodes and joints
     * they were copied from.
     *
     * <p>
     * The aim here is to paste the new nodes and joints into a <b>visible</b> area on the screen, even if the user has
     * panned around in the graph editor container since the copy-action took place.
     * </p>
     *
     * @return a {@link Point2D} containing the x and y offsets
     */
    private Point2D determinePasteOffset() {

        double offsetX = BASE_PASTE_OFFSET;
        double offsetY = BASE_PASTE_OFFSET;

        if (parentAtTimeOfCopy != null) {

            final Point2D localToScene = parentAtTimeOfCopy.localToScene(0, 0);

            final double parentSceneXAtTimeOfPaste = localToScene.getX();
            final double parentSceneYAtTimeOfPaste = localToScene.getY();

            offsetX += parentSceneXAtTimeOfCopy - parentSceneXAtTimeOfPaste;
            offsetY += parentSceneYAtTimeOfCopy - parentSceneYAtTimeOfPaste;
        }

        return new Point2D(offsetX, offsetY);
    }

    /**
     * Gets the start and end x- and y-positions of the given region (including insets).
     *
     * @param region a {@link Region}
     * @return the bounds of the given region
     */
    private Bounds getBounds(final Region region) {

        final Bounds bounds = new Bounds();

        bounds.startX = region.getInsets().getLeft();
        bounds.startY = region.getInsets().getTop();

        bounds.endX = region.getWidth() - region.getInsets().getRight();
        bounds.endY = region.getHeight() - region.getInsets().getBottom();

        return bounds;
    }

    /**
     * Gets the start and end x- and y-positions of the given group of nodes and joints.
     *
     * @param nodes a list of nodes
     * @param connections a list of connections
     * @return the start and end x- and y-positions of the given nodes and joints.
     */
    private Bounds getContentBounds(final List<GNode> nodes, final List<GConnection> connections) {

        final Bounds contentBounds = new Bounds();

        contentBounds.startX = Double.MAX_VALUE;
        contentBounds.startY = Double.MAX_VALUE;

        contentBounds.endX = 0;
        contentBounds.endY = 0;

        for (final GNode node : nodes) {

            if (node.getX() < contentBounds.startX) {
                contentBounds.startX = node.getX();
            }
            if (node.getY() < contentBounds.startY) {
                contentBounds.startY = node.getY();
            }
            if (node.getX() + node.getWidth() > contentBounds.endX) {
                contentBounds.endX = node.getX() + node.getWidth();
            }
            if (node.getY() + node.getHeight() > contentBounds.endY) {
                contentBounds.endY = node.getY() + node.getHeight();
            }
        }

        for (final GConnection connection : connections) {
            for (final GJoint joint : connection.getJoints()) {

                if (joint.getX() < contentBounds.startX) {
                    contentBounds.startX = joint.getX();
                }
                if (joint.getY() < contentBounds.startY) {
                    contentBounds.startY = joint.getY();
                }
                if (joint.getX() > contentBounds.endX) {
                    contentBounds.endX = joint.getX();
                }
                if (joint.getY() > contentBounds.endY) {
                    contentBounds.endY = joint.getY();
                }
            }
        }

        return contentBounds;
    }

    /**
     * Stores start and end x- and y-positions of a rectangular object.
     */
    private class Bounds {

        public double startX;
        public double startY;
        public double endX;
        public double endY;
    }
}
