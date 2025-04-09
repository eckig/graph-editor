package io.github.eckig.grapheditor.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import io.github.eckig.grapheditor.core.connections.ConnectionEventManager;
import io.github.eckig.grapheditor.core.connections.ConnectorDragManager;
import io.github.eckig.grapheditor.core.model.DefaultModelEditingManager;
import io.github.eckig.grapheditor.core.model.ModelLayoutUpdater;
import io.github.eckig.grapheditor.core.model.ModelSanityChecker;
import io.github.eckig.grapheditor.core.selections.DefaultSelectionManager;
import io.github.eckig.grapheditor.core.skins.GraphEditorSkinManager;
import io.github.eckig.grapheditor.core.skins.SkinManager;
import io.github.eckig.grapheditor.core.view.ConnectionLayouter;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import io.github.eckig.grapheditor.core.view.impl.DefaultConnectionLayouter;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;

import javafx.scene.Scene;

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

import io.github.eckig.grapheditor.Commands;
import io.github.eckig.grapheditor.GConnectorValidator;
import io.github.eckig.grapheditor.GJointSkin;
import io.github.eckig.grapheditor.GNodeSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.SelectionManager;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GConnector;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GNode;
import io.github.eckig.grapheditor.model.GraphPackage;
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
 * <li>{@link #process() process queue} before scene pulse</li>
 * </ol>
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

    private final CommandStackListener mCommandStackListener = event -> process();

    private final ModelEditingManager mModelEditingManager = new DefaultModelEditingManager(mCommandStackListener);
    private final ModelLayoutUpdater mModelLayoutUpdater;
    private final ConnectionLayouter mConnectionLayouter;
    private final ConnectorDragManager mConnectorDragManager;
    private final DefaultSelectionManager mSelectionManager;
    private final GraphEditorSkinManager mSkinManager;

    private final E mEditor;
    private final ChangeListener<GModel> mModelChangeListener = (w, o, n) -> modelChanged(o, n);
    private final Runnable mOnScenePulse = this::process;

    /**
     * Creates a new controller instance. Only one instance should exist per
     * {@link GraphEditor} instance.
     *
     * @param pEditor
     *            {@link GraphEditor} instance
     * @param pView
     *            {@link GraphEditorView}
     * @param pConnectionEventManager
     *            the {@link ConnectionEventManager} instance
     */
    public GraphEditorController(final E pEditor, final GraphEditorView pView,
            final ConnectionEventManager pConnectionEventManager, final GraphEditorProperties pProperties)
    {
        mEditor = Objects.requireNonNull(pEditor, "GraphEditor instance may not be null!");
        mSkinManager = new GraphEditorSkinManager(pEditor, pView);
        mConnectionLayouter = new DefaultConnectionLayouter(mSkinManager);
        mModelLayoutUpdater = new ModelLayoutUpdater(mSkinManager, mModelEditingManager, pProperties);
        mConnectorDragManager = new ConnectorDragManager(mSkinManager, pConnectionEventManager, pView);
        mSelectionManager = new DefaultSelectionManager(mSkinManager, pView);

        initDefaultListeners();

        pEditor.modelProperty().addListener(new WeakChangeListener<>(mModelChangeListener));
        modelChanged(null, pEditor.getModel());
        pEditor.getView().sceneProperty().addListener((obs,oldScene,newScene)->sceneChanged(oldScene,newScene));
        sceneChanged(null,pEditor.getView().getScene());

        mSkinManager.setOnNodeCreated(this::onNodeCreated);
        mSkinManager.setOnConnectorCreated(this::onConnectorCreated);
        mSkinManager.setOnConnectionCreated(this::onConnectionCreated);
        mSkinManager.setOnJointCreated(this::onJointCreated);
    }

    private void sceneChanged(final Scene pOldScene, final Scene pNewScene)
    {
        if (pOldScene != null)
        {
            pOldScene.removePreLayoutPulseListener(mOnScenePulse);
        }
        if (pNewScene != null)
        {
            pNewScene.addPreLayoutPulseListener(mOnScenePulse);
        }
    }

    private void initDefaultListeners()
    {
        registerChangeListener(GraphPackage.Literals.GMODEL__NODES, e -> processNotification(e, this::addNode, this::removeNode));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS,
                e -> processNotification(e, this::addConnector, this::removeConnector));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS, e ->
        {
            if (e.getNotifier() instanceof GNode n)
            {
                // if the connector is removed, the parent element is null..
                // luckily getNotifier() still returns the GNode where the connector was removed from:
                markConnectorsDirty(n);
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

            if(pNewModel instanceof InternalEObject ieo)
            {
                // add existing nodes through the registered change handlers:
                processFeatureChanged(new ENotificationImpl(ieo, Notification.ADD_MANY,
                        GraphPackage.Literals.GMODEL__NODES, List.of(), List.copyOf(pNewModel.getNodes())));

                // add existing connections through the registered change handlers:
                processFeatureChanged(new ENotificationImpl(ieo, Notification.ADD_MANY,
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
            Commands.updateLayoutValues(cmd, pModel, getSkinManager());
            if (!cmd.getCommandList().isEmpty() && cmd.canExecute())
            {
                cmd.execute();
                process();
            }
        }
    }

    /**
     * flush all queued changes
     */
    public void flush()
    {
        if (Platform.isFxApplicationThread())
        {
            process();
        }
    }


    /**
     * process the currently queued commands and process them by delegating to the registered handlers
     */
    private void process()
    {
        Notification n;
        boolean changes = false;
        while ((n = mContentAdapter.getQueue().poll()) != null)
        {
            changes = true;
            try
            {
                processFeatureChanged(n);
            }
            catch (Exception e)
            {
                LOGGER.error("Could not process update notification '{}': ", n, e); //$NON-NLS-1$
            }
        }

        if (changes)
        {
            processingDone();
        }
    }

    private void onNodeCreated(final GNode pNode)
    {
        mModelLayoutUpdater.addNode(pNode);
        mSelectionManager.addNode(pNode);
        markConnectorsDirty(pNode);
    }

    private void onConnectorCreated(final GConnector pConnector)
    {
        mConnectorDragManager.addConnector(pConnector);
        mSelectionManager.addConnector(pConnector);
        markConnectorsDirty(pConnector.getParent());
    }

    private void onConnectionCreated(final GConnection pConnection)
    {
        mSelectionManager.addConnection(pConnection);
        mSkinManager.updateJoints(pConnection);
    }

    private void onJointCreated(final GJoint pJoint)
    {
        mModelLayoutUpdater.addJoint(pJoint);
        mSelectionManager.addJoint(pJoint);
        mSkinManager.updateJoints(pJoint.getConnection());
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
        mConnectionLayouter.draw();
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
        mSkinManager.lookupOrCreateJoint(pJoint); // implicit create
    }

    private void addJoint(final GJoint pJoint, final Object pNotifier)
    {
        addJoint(pJoint);
        updateConnectionAfterJointChange(pJoint, pNotifier);
    }

    private void removeJoint(final GJoint pJoint)
    {
        mSelectionManager.removeJoint(pJoint);
        mSelectionManager.clearSelection(pJoint);
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
            mSkinManager.updateJoints(pJoint.getConnection());
        }
        else if(pNotifier instanceof GConnection c)
        {
            mSkinManager.updateJoints(c);
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
        mSkinManager.updateConnectors(pNode);
    }

    private void addConnection(final GConnection pConnection)
    {
        mSkinManager.lookupOrCreateConnection(pConnection); // implicit create
        for (final GJoint joint : pConnection.getJoints())
        {
            addJoint(joint);
        }
    }

    private void removeConnection(final GConnection pConnection)
    {
        mSelectionManager.removeConnection(pConnection);
        mSelectionManager.clearSelection(pConnection);
        mSkinManager.removeConnection(pConnection);

        for (final GJoint joint : pConnection.getJoints())
        {
            removeJoint(joint);
        }
    }

    private void addNode(final GNode pNode)
    {
        mSkinManager.lookupOrCreateNode(pNode); // implicit create

        for (int i = 0; i < pNode.getConnectors().size(); i++)
        {
            addConnector(pNode.getConnectors().get(i));
        }
    }

    private void removeNode(final GNode pNode)
    {
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
        mSkinManager.lookupOrCreateConnector(pConnector); // implicit create
    }

    private void removeConnector(final GConnector pConnector)
    {
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
     * @return {@link SkinManager} instance
     */
    public final SkinManager getSkinManager()
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
