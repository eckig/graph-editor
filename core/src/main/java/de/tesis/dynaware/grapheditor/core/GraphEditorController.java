package de.tesis.dynaware.grapheditor.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.GConnectorValidator;
import de.tesis.dynaware.grapheditor.GJointSkin;
import de.tesis.dynaware.grapheditor.GNodeSkin;
import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.SelectionManager;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.connections.ConnectionEventManager;
import de.tesis.dynaware.grapheditor.core.connections.ConnectorDragManager;
import de.tesis.dynaware.grapheditor.core.model.DefaultModelEditingManager;
import de.tesis.dynaware.grapheditor.core.model.ModelLayoutUpdater;
import de.tesis.dynaware.grapheditor.core.model.ModelSanityChecker;
import de.tesis.dynaware.grapheditor.core.selections.DefaultSelectionManager;
import de.tesis.dynaware.grapheditor.core.skins.SkinManager;
import de.tesis.dynaware.grapheditor.core.view.ConnectionLayouter;
import de.tesis.dynaware.grapheditor.core.view.GraphEditorView;
import de.tesis.dynaware.grapheditor.core.view.impl.DefaultConnectionLayouter;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;


/**
 * The central controller class for the default graph editor implementation.
 *
 * <p>
 * Responsible for using the {@link SkinManager} to create all skin instances
 * for the current {@link GModel}, and adding them to the {@link GraphEditorView
 * view}.
 * </p>
 *
 * <p>
 * Also responsible for creating all secondary managers like the
 * {@link ConnectorDragManager} and reinitializing them when the model changes.
 * </p>
 *
 * <p>
 * The process of synchronizing is rather complicated in case more than one
 * model is part of the resource set:
 * <ol>
 * <li>register listener on every model in the resource set (with an
 * {@link EContentAdapter}</li>
 * <li>receive notifications</li>
 * <li>put notification into queue</li>
 * <li>{@link #process() process queue} on every reload and/or command stack
 * change</li>
 * </ol>
 * This procedure (processing a chunk of notifications on command stack change
 * or {@link GraphEditor#reload()} is a very safe way to determine a valid
 * package of changes.
 * </p>
 *
 * <p>
 * This implementation is thread safe: It is able to process notifications in
 * parallel and processes them in chunks on the FX Thread.
 * </p>
 */
public class GraphEditorController<E extends GraphEditor>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphEditorController.class);

    private final GraphEditorEContentAdapter mContentAdapter = new GraphEditorEContentAdapter();

    private final Map<EStructuralFeature, Consumer<Notification>> mHandlersByFeature = new HashMap<>();
    private final Map<Integer, Consumer<Notification>> mHandlersByType = new HashMap<>();

    private final Collection<GNode> mNodeConnectorsDirty = new HashSet<>();
    private final Collection<GConnection> mConnectionsDirty = new HashSet<>();

    private final Collection<GConnection> mConnectionsToAdd = new HashSet<>();
    private final Collection<GNode> mNodesToAdd = new HashSet<>();
    private final Collection<GJoint> mJointsToAdd = new HashSet<>();
    private final Collection<GConnector> mConnectorsToAdd = new HashSet<>();

    private final CommandStackListener mCommandStackListener = event -> process();

    private final ModelEditingManager mModelEditingManager = new DefaultModelEditingManager(mCommandStackListener);
    private final ModelLayoutUpdater mModelLayoutUpdater;
    private final ConnectionLayouter mConnectionLayouter;
    private final ConnectorDragManager mConnectorDragManager;
    private final DefaultSelectionManager mSelectionManager;
    private final SkinManager mSkinManager;

    private final E mEditor;
    private final ChangeListener<GModel> mModelChangeListener = (w, o, n) -> modelChanged(o, n);

    /**
     * While processing a {@link Notification} from EMF it might trigger other
     * changes which will eventually lead to an infinite update cycle.<br>
     * When this flag is {@code true} we are currently processing a batch of
     * updates. All new notifications will be put in the queue but are processed
     * later on.
     */
    private boolean mProcessing = false;

    /**
     * Creates a new controller instance. Only one instance should exist per
     * {@link GraphEditor} instance.
     *
     * @param pEditor
     *            {@link GraphEditor} instance
     * @param pSkinManager
     *            the {@link SkinManager} instance
     * @param pView
     *            {@link GraphEditorView}
     * @param pConnectionEventManager
     *            the {@link ConnectionEventManager} instance
     */
    public GraphEditorController(final E pEditor, final SkinManager pSkinManager,
            final GraphEditorView pView, final ConnectionEventManager pConnectionEventManager, final GraphEditorProperties pProperties)
    {
        mEditor = Objects.requireNonNull(pEditor, "GraphEditor instance may not be null!");
        mConnectionLayouter = new DefaultConnectionLayouter(pSkinManager);

        mSkinManager = Objects.requireNonNull(pSkinManager, "SkinManager may not be null!");

        mModelLayoutUpdater = new ModelLayoutUpdater(pSkinManager, mModelEditingManager, pProperties);
        mConnectorDragManager = new ConnectorDragManager(pSkinManager, pConnectionEventManager, pView);
        mSelectionManager = new DefaultSelectionManager(pSkinManager, pView);

        initDefaultListeners();

        pEditor.modelProperty().addListener(new WeakChangeListener<>(mModelChangeListener));
        modelChanged(null, pEditor.getModel());
    }

    private void initDefaultListeners()
    {
        registerChangeListener(GraphPackage.Literals.GMODEL__NODES, e -> processNotification(e, this::addNode, this::removeNode));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS,
                e -> processNotification(e, this::addConnector, this::removeConnector));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS, e ->
        {
            if (e.getNotifier() instanceof GNode)
            {
                // if the connector is removed, the parent element is null..
                // luckily getNotifier() still returns the GNode where the connector was removed from:
                markConnectorsDirty((GNode) e.getNotifier());
            }
        });
        registerChangeListener(GraphPackage.Literals.GMODEL__CONNECTIONS,
                e -> processNotification(e, this::addConnection, this::removeConnection));

        registerChangeListener(GraphPackage.Literals.GCONNECTION__JOINTS,
                e -> processNotification(e, (GJoint j) -> addJoint(j, e.getNotifier()),
                        (GJoint j) -> removeJoint(j, e.getNotifier())));

        registerChangeListener(GraphPackage.Literals.GJOINT__X, this::jointPositionChanged);
        registerChangeListener(GraphPackage.Literals.GJOINT__Y, this::jointPositionChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__Y, this::nodePositionChanged);
        registerChangeListener(GraphPackage.Literals.GNODE__X, this::nodePositionChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__HEIGHT, this::nodeSizeChanged);
        registerChangeListener(GraphPackage.Literals.GNODE__WIDTH, this::nodeSizeChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__TYPE, e ->
        {
            final GNode node = (GNode) e.getNotifier();
            removeNode(node);
            addNode(node);
        });
    }

    /**
     * Registers a change listener
     *
     * @param pFeature
     *            {@link EStructuralFeature feature to watch}
     * @param pHandler
     *            {@link Consumer} handling the {@link Notification}
     * @since 15.03.2019
     */
    public final void registerChangeListener(final EStructuralFeature pFeature, final Consumer<Notification> pHandler)
    {
        Objects.requireNonNull(pFeature, "EStructuralFeature may not be null!");
        Objects.requireNonNull(pHandler, "Notification Consumer may not be null!");
        mHandlersByFeature.merge(pFeature, pHandler, Consumer::andThen);
    }

    /**
     * Registers a change listener
     *
     * @param pNotificationType
     *            {@link Notification notification type}
     * @param pHandler
     *            {@link Consumer} handling the {@link Notification}
     * @since 15.03.2019
     */
    public final void registerChangeListener(final int pNotificationType, final Consumer<Notification> pHandler)
    {
        Objects.requireNonNull(pHandler, "Notification Consumer may not be null!");
        mHandlersByType.merge(pNotificationType, pHandler, Consumer::andThen);
    }

    private void modelChanged(final GModel pOldModel, final GModel pNewModel)
    {
        if (pOldModel != null)
        {
            final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pOldModel);
            editingDomain.getResourceSet().eAdapters().remove(mContentAdapter);

            for (int i = 0; i < pOldModel.getNodes().size(); i++)
            {
                removeNode(pOldModel.getNodes().get(i));
            }
            for (int i = 0; i < pOldModel.getConnections().size(); i++)
            {
                removeConnection(pOldModel.getConnections().get(i));
            }
        }

        // remove any remaining skins that might have been left over:
        mSkinManager.clear();

        if (pNewModel != null)
        {
            ModelSanityChecker.validate(pNewModel);

            mModelEditingManager.initialize(pNewModel);

            final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pNewModel);
            editingDomain.getResourceSet().eAdapters().add(mContentAdapter);

            if(pNewModel instanceof InternalEObject)
            {
                // add existing nodes through the registered change handlers:
                processFeatureChanged(new ENotificationImpl((InternalEObject) pNewModel, Notification.ADD_MANY,
                        GraphPackage.Literals.GMODEL__NODES, List.of(), List.copyOf(pNewModel.getNodes())));

                // add existing connections through the registered change handlers:
                processFeatureChanged(new ENotificationImpl((InternalEObject) pNewModel, Notification.ADD_MANY,
                        GraphPackage.Literals.GMODEL__CONNECTIONS, List.of(), List.copyOf(pNewModel.getConnections())));
            }
            else
            {
                for(final GNode node : pNewModel.getNodes())
                {
                    addNode(node);
                }

                for(final GConnection connection : pNewModel.getConnections())
                {
                    addConnection(connection);
                }
            }

            process();

            mSelectionManager.initialize(pNewModel);
            mConnectionLayouter.initialize(pNewModel);
            mConnectorDragManager.initialize(pNewModel);

            // 1) wait until the graph editor is registered in a visible view (scene != null)
            // 2) wait a little bit with Platform.runLater() so the UI has a chance to "settle down"
            // 3) update layout values
            executeOnceWhenPropertyIsNonNull(mEditor.getView().sceneProperty(),
                    scene -> Platform.runLater(() -> updateLayoutValues(pNewModel)));
        }
    }

    private void updateLayoutValues(final GModel pModel)
    {
        // because we defer execution with Platform.runLater()
        //   we have to check if the given model is still valid:
        if (mEditor.getModel() != pModel)
        {
            return;
        }

        // When the model is loaded from the database and painted to the UI sometimes
        //   the rendering process calculates different sizes than the ones stored in the model
        //   which triggers a change..
        // for this case we wait until the rendering is done and update the layout values by hand
        final CompoundCommand cmd = new CompoundCommand();
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pModel);
        if (editingDomain != null)
        {
            Commands.updateLayoutValues(cmd, pModel, getSkinLookup());
            if (!cmd.getCommandList().isEmpty() && cmd.canExecute())
            {
                cmd.execute();
                process();
            }
        }
    }

    /**
     * flush the currently queued commands and process them immediately.<br>
     * Only has an effect if the current Thread is the
     * {@link Platform#isFxApplicationThread() FX Application Thread}
     *
     * @since 09.02.2016
     */
    public final void process()
    {
        if (mProcessing || !Platform.isFxApplicationThread()) // prevent GUI updates outside the FX Application Thread
        {
            return;
        }

        mProcessing = true;
        try
        {
            Notification n;
            while ((n = mContentAdapter.getQueue().poll()) != null)
            {
                try
                {
                    processFeatureChanged(n);
                }
                catch (Exception e)
                {
                    LOGGER.error("Could not process update notification '{}': ", n, e); //$NON-NLS-1$
                }
            }

            if (!mNodesToAdd.isEmpty())
            {
                for (final Iterator<GNode> iter = mNodesToAdd.iterator(); iter.hasNext();)
                {
                    final GNode next = iter.next();
                    mSkinManager.lookupOrCreateNode(next); // implicit create
                    mModelLayoutUpdater.addNode(next);
                    mSelectionManager.addNode(next);
                    markConnectorsDirty(next);
                    iter.remove();
                }
            }

            if (!mConnectorsToAdd.isEmpty())
            {
                for (final Iterator<GConnector> iter = mConnectorsToAdd.iterator(); iter.hasNext();)
                {
                    final GConnector next = iter.next();
                    mSkinManager.lookupOrCreateConnector(next); // implicit create
                    mConnectorDragManager.addConnector(next);
                    mSelectionManager.addConnector(next);
                    markConnectorsDirty(next.getParent());
                    iter.remove();
                }
            }

            if (!mConnectionsToAdd.isEmpty())
            {
                for (final Iterator<GConnection> iter = mConnectionsToAdd.iterator(); iter.hasNext();)
                {
                    final GConnection next = iter.next();
                    mSkinManager.lookupOrCreateConnection(next); // implicit create
                    mSelectionManager.addConnection(next);
                    mConnectionsDirty.add(next);
                    iter.remove();
                }
            }

            if (!mJointsToAdd.isEmpty())
            {
                for (final Iterator<GJoint> iter = mJointsToAdd.iterator(); iter.hasNext();)
                {
                    final GJoint next = iter.next();
                    mSkinManager.lookupOrCreateJoint(next); // implicit create
                    mModelLayoutUpdater.addJoint(next);
                    mSelectionManager.addJoint(next);
                    mConnectionsDirty.add(next.getConnection());
                    iter.remove();
                }
            }

            if (!mNodeConnectorsDirty.isEmpty())
            {
                for (final Iterator<GNode> iter = mNodeConnectorsDirty.iterator(); iter.hasNext();)
                {
                    mSkinManager.updateConnectors(iter.next());
                    iter.remove();
                }
            }

            if (!mConnectionsDirty.isEmpty())
            {
                for (final Iterator<GConnection> iter = mConnectionsDirty.iterator(); iter.hasNext();)
                {
                    final GConnection conn = iter.next();
                    mSkinManager.updateJoints(conn);
                    iter.remove();
                }
            }

            processingDone();
        }
        finally
        {
            mProcessing = false;
        }
    }

    private void processFeatureChanged(final Notification pNotification)
    {
        // call every registered consumer, registered for the feature
        final Consumer<Notification> consumerForFeature;
        if ((consumerForFeature = mHandlersByFeature.get(pNotification.getFeature())) != null)
        {
            consumerForFeature.accept(pNotification);
        }

        // call every registered consumer, registered for the feature
        final Consumer<Notification> consumerForType;
        if ((consumerForType = mHandlersByType.get(pNotification.getEventType())) != null)
        {
            consumerForType.accept(pNotification);
        }
    }

    /**
     * Called when all queued commands have been processed
     *
     * @since 15.03.2019
     */
    protected void processingDone()
    {
        mConnectionLayouter.redrawAll();
    }

    private void nodePositionChanged(final Notification pChange)
    {
        final GNode node = (GNode) pChange.getNotifier();
        if (node != null)
        {
            final GNodeSkin skin = mSkinManager.lookupNode(node);
            if (skin != null)
            {
                skin.getRoot().relocate(node.getX(), node.getY());
            }
        }
    }

    private void nodeSizeChanged(final Notification pChange)
    {
        final GNode node = (GNode) pChange.getNotifier();
        if (node != null)
        {
            final GNodeSkin skin = mSkinManager.lookupNode(node);
            if (skin != null)
            {
                skin.getRoot().resize(node.getWidth(), node.getHeight());
            }
        }
    }

    private void jointPositionChanged(final Notification pChange)
    {
        final GJoint joint = (GJoint) pChange.getNotifier();
        if (joint != null)
        {
            final GJointSkin skin = mSkinManager.lookupJoint(joint);
            if (skin != null)
            {
                skin.initialize();
            }
        }
    }

    private void addJoint(final GJoint pJoint)
    {
        mJointsToAdd.add(pJoint);
    }

    private void addJoint(final GJoint pJoint, final Object pNotifier)
    {
        addJoint(pJoint);
        updateConnectionAfterJointChange(pJoint, pNotifier);
    }

    private void removeJoint(final GJoint pJoint)
    {
        mJointsToAdd.remove(pJoint);

        mSelectionManager.removeJoint(pJoint);
        mSelectionManager.getSelectedJoints().remove(pJoint);
        mModelLayoutUpdater.removeJoint(pJoint);
        mSkinManager.removeJoint(pJoint);
    }

    private void removeJoint(final GJoint pJoint, final Object pNotifier)
    {
        removeJoint(pJoint);
        updateConnectionAfterJointChange(pJoint, pNotifier);
    }

    private void updateConnectionAfterJointChange(final GJoint pJoint, final Object pNotifier)
    {
        if (pJoint.getConnection() != null)
        {
            mConnectionsDirty.add(pJoint.getConnection());
        }
        else if(pNotifier instanceof GConnection)
        {
            mConnectionsDirty.add((GConnection) pNotifier);
        }
    }

    /**
     * @param pNode
     *            {@link GNode} of which the {@link GConnector connectors} have
     *            been changed
     * @since 15.03.2019
     */
    protected final void markConnectorsDirty(final GNode pNode)
    {
        mNodeConnectorsDirty.add(pNode);
    }

    private void addConnection(final GConnection pConnection)
    {
        mConnectionsToAdd.add(pConnection);
        for (final GJoint joint : pConnection.getJoints())
        {
            addJoint(joint);
        }
    }

    private void removeConnection(final GConnection pConnection)
    {
        mConnectionsToAdd.remove(pConnection);

        mSelectionManager.removeConnection(pConnection);
        mSelectionManager.getSelectedConnections().remove(pConnection);
        mSkinManager.removeConnection(pConnection);

        for (final GJoint joint : pConnection.getJoints())
        {
            removeJoint(joint);
        }
    }

    private void addNode(final GNode pNode)
    {
        mNodesToAdd.add(pNode);

        for (int i = 0; i < pNode.getConnectors().size(); i++)
        {
            addConnector(pNode.getConnectors().get(i));
        }
    }

    private void removeNode(final GNode pNode)
    {
        mNodesToAdd.remove(pNode);

        for (int i = 0; i < pNode.getConnectors().size(); i++)
        {
            removeConnector(pNode.getConnectors().get(i));
        }

        mSelectionManager.removeNode(pNode);
        mSelectionManager.clearSelection(pNode);
        mModelLayoutUpdater.removeNode(pNode);
        mSkinManager.removeNode(pNode);
    }

    private void addConnector(final GConnector pConnector)
    {
        mConnectorsToAdd.add(pConnector);
    }

    private void removeConnector(final GConnector pConnector)
    {
        mConnectorsToAdd.remove(pConnector);

        mSelectionManager.removeConnector(pConnector);
        mConnectorDragManager.removeConnector(pConnector);
        mSkinManager.removeConnector(pConnector);
    }

    /**
     * @return {@link ConnectionLayouter}
     */
    public final ConnectionLayouter getConnectionLayouter()
    {
        return mConnectionLayouter;
    }

    /**
     * @return {@link GraphEditor} instance
     */
    public final E getEditor()
    {
        return mEditor;
    }

    /**
     * @return {@link SkinLookup} instance
     */
    public final SkinLookup getSkinLookup()
    {
        return mSkinManager;
    }

    /**
     * Gets the selection manager currently being used.
     *
     * @return selection manager currently being used.
     */
    public final SelectionManager getSelectionManager()
    {
        return mSelectionManager;
    }

    /**
     * Sets the validator that determines what connections can be created.
     *
     * @param validator
     *            a {@link GConnectorValidator} implementation, or null to use the
     *            default
     */
    public final void setConnectorValidator(final GConnectorValidator validator)
    {
        mConnectorDragManager.setValidator(validator);
    }

    /**
     * @return {@link ModelEditingManager}
     */
    public final ModelEditingManager getModelEditingManager()
    {
        return mModelEditingManager;
    }

    /**
     * Delegates the contents of the given {@link Notification} to the given
     * {@link Consumer consumers}:
     * <ul>
     * <li>In case of {@link Notification#ADD} or {@link Notification#ADD_MANY},
     * the add consumer will be called with each added element</li>
     * <li>In case of {@link Notification#REMOVE} or
     * {@link Notification#REMOVE_MANY}, the remove consumer will be called with
     * each added element</li>
     * </ul>
     * IMPORTANT: The added/removed values are casted without any checks, any
     * one calling this method should take extra care to not mix wrong types!
     *
     * @param pNotification the Notification to examine
     * @param pAdd Consumer to invoke with the new element(s) (if any)
     * @param pRemove Consumer to invoke with the deleted element(s) (if any)
     * @since 15.03.2019
     */
    protected static <T> void processNotification(final Notification pNotification, final Consumer<T> pAdd, final Consumer<T> pRemove)
    {
        Objects.requireNonNull(pNotification);
        Objects.requireNonNull(pAdd);
        Objects.requireNonNull(pRemove);
        switch (pNotification.getEventType())
        {
            case Notification.ADD:
                @SuppressWarnings("unchecked")
                final T newValue = (T) pNotification.getNewValue();
                pAdd.accept(newValue);
                break;

            case Notification.ADD_MANY:
                @SuppressWarnings("unchecked")
                final List<T> newValues = (List<T>) pNotification.getNewValue();
                newValues.forEach(pAdd);
                break;

            case Notification.REMOVE:
                @SuppressWarnings("unchecked")
                final T oldValue = (T) pNotification.getOldValue();
                pRemove.accept(oldValue);
                break;

            case Notification.REMOVE_MANY:
                @SuppressWarnings("unchecked")
                final List<T> oldValues = (List<T>) pNotification.getOldValue();
                oldValues.forEach(pRemove);
                break;
        }
    }

    /**
     * <p>
     * Attaches a value listener to the given {@link ObservableValue} and when
     * the value changes to a non-{@code null} value the given {@link Consumer}
     * will be invoked with the new value and the listener will be removed.
     * </p>
     * <p>
     * NOTE: If the {@link ObservableValue} already has a non-{@code null} value
     * the {@link Consumer} will be invoked directly.
     * </p>
     * <p>
     * This proves useful for linking things together before a property is
     * necessarily set.
     * </p>
     *
     * @param pProperty
     *            {@link ObservableValue} to observe
     * @param pConsumer
     *            {@link Consumer} to call with the first non-{@code null} value
     * @since 12.05.2017
     */
    private static <T> void executeOnceWhenPropertyIsNonNull(final ObservableValue<T> pProperty, final Consumer<T> pConsumer)
    {
        if (pProperty == null)
        {
            return;
        }

        final T value = pProperty.getValue();
        if (value != null)
        {
            pConsumer.accept(value);
        }
        else
        {
            final InvalidationListener listener = new InvalidationListener()
            {

                @Override
                public void invalidated(final Observable observable)
                {
                    final T newValue = pProperty.getValue();
                    if (newValue != null)
                    {
                        pProperty.removeListener(this);
                        pConsumer.accept(newValue);
                    }
                }
            };
            pProperty.addListener(listener);
        }
    }

    private static class GraphEditorEContentAdapter extends EContentAdapter
    {

        private final Queue<Notification> imQueue = new ConcurrentLinkedQueue<>();

        @Override
        public final void notifyChanged(final Notification pNotification)
        {
            super.notifyChanged(pNotification);
            if (pNotification.getEventType() != Notification.REMOVING_ADAPTER)
            {
                imQueue.add(pNotification);
            }
        }

        Queue<Notification> getQueue()
        {
            return imQueue;
        }
    }
}
