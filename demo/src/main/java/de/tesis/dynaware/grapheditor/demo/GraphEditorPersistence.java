package de.tesis.dynaware.grapheditor.demo;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;

import de.tesis.dynaware.grapheditor.GraphEditor;
import de.tesis.dynaware.grapheditor.model.GModel;

/**
 * Helper class for crudely loading {@link GModel} states to and from XML.
 *
 * <p>
 * Not part of the graph editor library, only used in the {@link GraphEditorDemo} application.
 * </p>
 */
public class GraphEditorPersistence {

    private static final String FILE_EXTENSION = ".graph";
    private static final String CHOOSER_TEXT = "Graph Model Files (*" + FILE_EXTENSION + ")";

    private static final String SAMPLE_FILE = "sample" + FILE_EXTENSION;
    private static final String TREE_FILE = "tree" + FILE_EXTENSION;
    private static final String GREY_FILE = "grey" + FILE_EXTENSION;

    private File initialDirectory = null;

    /**
     * Saves the graph editor's {@link GModel} state to an XML file via the {@link FileChooser}.
     *
     * @param graphEditor the graph editor whose model state is to be saved
     */
    public void saveToFile(final GraphEditor graphEditor) {

        final Scene scene = graphEditor.getView().getScene();

        if (scene != null) {

            final File file = showFileChooser(scene.getWindow(), true);

            if (file != null && graphEditor.getModel() != null) {
                saveModel(file, graphEditor.getModel());
            }
        }
    }

    /**
     * Loads an XML .graph file into the given graph editor.
     *
     * @param graphEditor the graph editor in which the loaded model will be set
     */
    public void loadFromFile(final GraphEditor graphEditor) {

        final Scene scene = graphEditor.getView().getScene();

        if (scene != null) {

            final File file = showFileChooser(scene.getWindow(), false);

            if (file != null) {
                loadModel(file, graphEditor);
            }
        }
    }

    /**
     * Loads the sample saved in the <b>sample.graph</b> file.
     *
     * @param graphEditor the graph editor in which the loaded model will be set
     */
    public void loadSample(final GraphEditor graphEditor) {
        loadSample(SAMPLE_FILE, graphEditor);
    }

    /**
     * Loads the sample saved in the <b>tree.graph</b> file.
     *
     * @param graphEditor the graph editor in which the loaded model will be set
     */
    public void loadTree(final GraphEditor graphEditor) {
        loadSample(TREE_FILE, graphEditor);
    }

    /**
     * Loads the sample saved in the <b>tree.graph</b> file.
     *
     * @param graphEditor the graph editor in which the loaded model will be set
     */
    public void loadGrey(final GraphEditor graphEditor) {
        loadSample(GREY_FILE, graphEditor);
    }

    /**
     * Loads the sample saved in the given file.
     *
     * @param graphEditor the graph editor in which the loaded model will be set
     */
    private void loadSample(final String file, final GraphEditor graphEditor) {

        final String samplePath = getClass().getResource(file).toExternalForm();

        final URI fileUri = URI.createURI(samplePath);
        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);

        try {
            resource.load(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof GModel) {

            final GModel model = (GModel) resource.getContents().get(0);
            graphEditor.setModel(model);
        }
    }

    /**
     * Opens the file chooser and returns the selected {@link File}.
     *
     * @param graphEditor a graph editor instance with a not-null scene
     * @param save {@code true} to open a save dialog, {@code false} to open a load dialog
     * @return the chosen file
     */
    private File showFileChooser(final Window window, final boolean save) {

        final FileChooser fileChooser = new FileChooser();

        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(CHOOSER_TEXT, "*" + FILE_EXTENSION);
        fileChooser.getExtensionFilters().add(filter);

        if (initialDirectory != null && initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        if (save) {
            return fileChooser.showSaveDialog(window);
        } else {
            return fileChooser.showOpenDialog(window);
        }
    }

    /**
     * Saves the graph editor's model state in the given file.
     *
     * @param file the {@link File} the model state will be saved in
     * @param model the {@link GModel} to be saved
     */
    private void saveModel(final File file, final GModel model) {

        String absolutePath = file.getAbsolutePath();
        if (!absolutePath.endsWith(FILE_EXTENSION)) {
            absolutePath += FILE_EXTENSION;
        }

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        final URI fileUri = URI.createFileURI(absolutePath);
        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);
        resource.getContents().add(model);

        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        editingDomain.getResourceSet().getResources().clear();
        editingDomain.getResourceSet().getResources().add(resource);

        initialDirectory = file.getParentFile();
    }

    /**
     * Loads the model from the given file and sets it in the given graph editor.
     *
     * @param file the {@link File} to be loaded
     * @param graphEditor the {@link GraphEditor} in which the loaded model will be set
     */
    private void loadModel(final File file, final GraphEditor graphEditor) {

        final URI fileUri = URI.createFileURI(file.getAbsolutePath());

        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);

        try {
            resource.load(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof GModel) {

            final GModel model = (GModel) resource.getContents().get(0);
            graphEditor.setModel(model);
        }

        initialDirectory = file.getParentFile();
    }
}
