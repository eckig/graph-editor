/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.model;

import java.util.List;
import java.util.Map;

import javafx.geometry.Point2D;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory.Descriptor.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tesis.dynaware.grapheditor.Commands;
import de.tesis.dynaware.grapheditor.SkinLookup;
import de.tesis.dynaware.grapheditor.core.DefaultGraphEditor;
import de.tesis.dynaware.grapheditor.core.utils.LogMessages;
import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

/**
 * Provides utility methods to edit the graph model via EMF commands.
 */
public class ModelEditingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEditingManager.class);

    private static final EAttribute JOINT_X = GraphPackage.Literals.GJOINT__X;
    private static final EAttribute JOINT_Y = GraphPackage.Literals.GJOINT__Y;

    private static final EReference NODES = GraphPackage.Literals.GMODEL__NODES;
    private static final EReference CONNECTIONS = GraphPackage.Literals.GMODEL__CONNECTIONS;
    private static final EReference JOINTS = GraphPackage.Literals.GCONNECTION__JOINTS;

    private static final EReference CONNECTOR_CONNECTIONS = GraphPackage.Literals.GCONNECTOR__CONNECTIONS;

    private static final URI DEFAULT_URI = URI.createFileURI("");

    private final CommandStackListener commandStackListener;

    private EditingDomain editingDomain;
    private GModel model;

    /**
     * Creates a new model editing manager. Only one instance should exist per {@link DefaultGraphEditor} instance.
     *
     * @param commandStackListener the {@link CommandStackListener} that listens for changes in the model
     */
    public ModelEditingManager(final CommandStackListener commandStackListener) {
        this.commandStackListener = commandStackListener;
    }

    /**
     * Initializes the model editing manager for the given model instance.
     *
     * @param model the new {@link GModel} to be edited
     */
    public void initialize(final GModel model) {

        // Only initialize the editing domain if the model object has actually changed.
        if (!model.equals(this.model)) {
            initializeEditingDomain(this.model, model);
        }
        this.model = model;
    }

    /**
     * Silently updates the model's layout values to match those in the skin instances.
     *
     * @param skinLookup the {@link SkinLookup} used to lookup skin instances
     */
    public void updateLayoutValues(final SkinLookup skinLookup) {

        final CompoundCommand command = new CompoundCommand();

        Commands.updateLayoutValues(command, model, skinLookup);

        editingDomain.getCommandStack().removeCommandStackListener(commandStackListener);

        if (command.canExecute()) {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(LogMessages.UPDATING_LAYOUT);
            }

            editingDomain.getCommandStack().execute(command);
        }

        editingDomain.getCommandStack().addCommandStackListener(commandStackListener);
    }

    /**
     * Removes all specified nodes, connections, and joints from the model in a single compound command.
     *
     * <p>
     * All references to the removed elements are also removed.
     * </p>
     *
     * <p>
     * Additionally, joints may be repositioned within the same compound command.
     * </p>
     *
     * @param nodesToRemove the nodes to be removed
     * @param connectionsToRemove the connections to be removed
     * @param jointsToRemove the joints to be removed
     * @param jointsToReposition the joints that are <b>not</b> removed but need to be repositioned
     */
    public void remove(final List<GNode> nodesToRemove, final List<GConnection> connectionsToRemove,
            final List<GJoint> jointsToRemove, final Map<GJoint, Point2D> jointsToReposition) {

        final CompoundCommand command = new CompoundCommand();

        for (final GNode node : nodesToRemove) {
            command.append(RemoveCommand.create(editingDomain, model, NODES, node));
        }

        for (final GConnection connection : connectionsToRemove) {
            command.append(RemoveCommand.create(editingDomain, model, CONNECTIONS, connection));

            final GConnector source = connection.getSource();
            final GConnector target = connection.getTarget();

            if (!nodesToRemove.contains(source.getParent())) {
                command.append(RemoveCommand.create(editingDomain, source, CONNECTOR_CONNECTIONS, connection));
            }

            if (!nodesToRemove.contains(target.getParent())) {
                command.append(RemoveCommand.create(editingDomain, target, CONNECTOR_CONNECTIONS, connection));
            }
        }

        for (final GJoint joint : jointsToRemove) {
            command.append(RemoveCommand.create(editingDomain, joint.getConnection(), JOINTS, joint));
        }

        for (final GJoint joint : jointsToReposition.keySet()) {

            final double x = jointsToReposition.get(joint).getX();
            final double y = jointsToReposition.get(joint).getY();

            command.append(SetCommand.create(editingDomain, joint, JOINT_X, x));
            command.append(SetCommand.create(editingDomain, joint, JOINT_Y, y));
        }

        if (command.canExecute()) {

            if (LOGGER.isTraceEnabled()) {

                if (nodesToRemove.size() == 1) {
                    LOGGER.trace(LogMessages.REMOVING_NODE, nodesToRemove.get(0).hashCode());
                } else {
                    LOGGER.trace(LogMessages.REMOVING_NODES, nodesToRemove.size());
                }
            }

            editingDomain.getCommandStack().execute(command);
        }
    }

    /**
     * Initializes the editing domain and resource for the new model.
     *
     * <p>
     * If a resource and/or editing domain are already associated to this model, these will be used. Otherwise they will
     * be created.
     * </p>
     */
    private void initializeEditingDomain(final GModel oldModel, final GModel newModel) {

        // First remove the listener from the old model, if it exists.
        if (oldModel != null) {
            final EditingDomain oldDomain = AdapterFactoryEditingDomain.getEditingDomainFor(oldModel);
            if (oldDomain != null) {
                oldDomain.getCommandStack().removeCommandStackListener(commandStackListener);
            }
        }

        if (newModel.eResource() == null) {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(LogMessages.CREATING_RESOURCE, newModel.hashCode());
            }

            final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
            final Resource resource = resourceFactory.createResource(DEFAULT_URI);
            resource.getContents().add(newModel);
        }

        editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(newModel);

        if (editingDomain == null) {

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(LogMessages.CREATING_EDITING_DOMAIN, newModel.hashCode());
            }

            final Registry registry = ComposedAdapterFactory.Descriptor.Registry.INSTANCE;
            final AdapterFactory adapterFactory = new ComposedAdapterFactory(registry);

            editingDomain = new AdapterFactoryEditingDomain(adapterFactory, new BasicCommandStack());
            editingDomain.getResourceSet().getResources().add(newModel.eResource());
        }

        editingDomain.getCommandStack().addCommandStackListener(commandStackListener);
    }
}
