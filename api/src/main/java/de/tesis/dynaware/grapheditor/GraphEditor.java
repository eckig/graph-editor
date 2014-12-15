/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.function.BiConsumer;

import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Region;

import org.eclipse.emf.common.command.CompoundCommand;

import de.tesis.dynaware.grapheditor.model.GConnection;
import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GJoint;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.utils.GraphEditorProperties;

/**
 * Provides functionality for displaying and editing graph-like diagrams in JavaFX.
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>GModel model = GraphFactory.eINSTANCE.createGModel();
 * 
 * GraphEditor graphEditor = new DefaultGraphEditor();
 * graphEditor.setModel(model);
 * 
 * Region view = graphEditor.getView();</code>
 * </pre>
 *
 * The view is a {@link Region} and can be added to the JavaFX scene graph in the usual way. For large graphs, the
 * editor can be put inside a {@link GraphEditorContainer} instead.
 * </p>
 *
 * <p>
 * The editor updates its underlying model via EMF commands. This means any user action should be undoable. Helper
 * methods for common operations are provided in the {@link Commands} class, such as:
 *
 * <ul>
 * <li>Add Node</li>
 * <li>Clear All</li>
 * <li>Undo</li>
 * <li>Redo</li>
 * </ul>
 *
 * </p>
 *
 * <p>
 * Look & feel can be customised by setting custom skin classes. The default skins can also be customised to some extent
 * via CSS. See <b>defaults.css</b> in the core module for more information.
 * </p>
 */
public interface GraphEditor {

    /**
     * Sets the custom node skin for a particular node type.
     *
     * <p>
     * All {@link GNode} model elements that have a '<em><b>Type</b></em>' attribute equal to type set here will be
     * rendered using this skin.
     * </p>
     *
     * @param type the {@link GNode} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GNodeSkin}
     */
    void setNodeSkin(final String type, final Class<? extends GNodeSkin> skin);

    /**
     * Sets the custom connector skin for a particular connector type.
     *
     * <p>
     * All {@link GConnector} model elements that have a '<em><b>Type</b></em>' attribute equal to type set here will be
     * rendered using this skin.
     * </p>
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectorSkin}
     */
    void setConnectorSkin(final String type, final Class<? extends GConnectorSkin> skin);

    /**
     * Sets the custom connection skin for a particular connection type.
     *
     * <p>
     * All {@link GConnection} model elements that have a '<em><b>Type</b></em>' attribute equal to type set here will
     * be rendered using this skin.
     * </p>
     *
     * @param type the {@link GConnection} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GConnectionSkin}
     */
    void setConnectionSkin(final String type, final Class<? extends GConnectionSkin> skin);

    /**
     * Sets the custom joint skin for a particular joint type.
     *
     * <p>
     * All {@link GJoint} model elements that have a '<em><b>Type</b></em>' attribute equal to type set here will be
     * rendered using this skin.
     * </p>
     *
     * @param type the {@link GJoint} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GJointSkin}
     */
    void setJointSkin(final String type, final Class<? extends GJointSkin> skin);

    /**
     * Sets the custom tail skin for a particular connector type.
     *
     * <p>
     * All {@link GConnector} model elements that have a '<em><b>Type</b></em>' attribute equal to type set here will
     * have their tails rendered using this skin.
     * </p>
     *
     * @param type the {@link GConnector} type for which this skin should be used
     * @param skin a custom skin class that extends {@link GTailSkin}
     */
    void setTailSkin(final String type, final Class<? extends GTailSkin> skin);

    /**
     * Sets the custom connector validator class.
     *
     * <p>
     * This will be used to decide which connections are allowed / forbidden during drag & drop events in the editor.
     * </p>
     *
     * @param validator a custom validator class that implements {@link GConnectorValidator}
     */
    void setConnectorValidator(final Class<? extends GConnectorValidator> validator);

    /**
     * Sets the graph model to be edited.
     *
     * @param model the {@link GModel} to be edited
     */
    void setModel(final GModel model);

    /**
     * Gets the graph model that is currently being edited.
     *
     * @return the {@link GModel} being edited, or {@code null} if no model was ever set
     */
    GModel getModel();

    /**
     * Reloads the graph model currently being edited.
     *
     * <p>
     * <b>Note: </b><br>
     * If the model is updated via EMF commands, as is recommended, it should rarely be necessary to call this method.
     * The model will be reloaded automatically via a command-stack listener.
     * </p>
     */
    void reload();

    /**
     * The property containing the graph model being edited.
     *
     * @return a property containing the {@link GModel} being edited
     */
    ObjectProperty<GModel> modelProperty();

    /**
     * Gets the view where the graph is displayed and edited.
     *
     * <p>
     * The view is a JavaFX {@link Region}. It should be added to the scene graph in the usual way.
     * </p>
     *
     * @return the {@link Region} where the graph is displayed and edited
     */
    Region getView();

    /**
     * Gets the properties of the editor.
     *
     * <p>
     * This provides access to global properties such as:
     *
     * <ul>
     * <li>Show/hide alignment grid.</li>
     * <li>Toggle snap-to-grid on/off.</li>
     * <li>Toggle editor bounds on/off.</li>
     * </ul>
     *
     * </p>
     *
     * @return an {@link GraphEditorProperties} instance containing the properties of the editor
     */
    GraphEditorProperties getProperties();

    /**
     * Sets the properties of the editor.
     *
     * @param properties an {@link GraphEditorProperties} instance specifying a set of properties for the editor
     */
    void setProperties(GraphEditorProperties properties);

    /**
     * Gets the skin lookup.
     *
     * <p>
     * The skin lookup is used to get any skin instance associated to a model element instance.
     * </p>
     *
     * @return a {@link SkinLookup} used to lookup skins
     */
    SkinLookup getSkinLookup();

    /**
     * Gets the selection manager.
     *
     * <p>
     * This provides access to actions like 'select all', 'delete selection', cut, copy, paste, and so on.
     * </p>
     *
     * @return the {@link SelectionManager}
     */
    SelectionManager getSelectionManager();

    /**
     * Sets a method to be called when a connection is created in the editor.
     *
     * <p>
     * This can be used to append additional commands to the one that created the connection.
     * </p>
     *
     * @param consumer a consumer to append additional commands
     */
    void setOnConnectionCreated(BiConsumer<GConnection, CompoundCommand> consumer);

    /**
     * Sets a method to be called when a connection is removed in the editor.
     *
     * <p>
     * This can be used to append additional commands to the one that removed the connection.
     * </p>
     *
     * @param consumer a consumer to append additional commands
     */
    void setOnConnectionRemoved(BiConsumer<GConnection, CompoundCommand> consumer);
}
