/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor.core.data;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import de.tesis.dynaware.grapheditor.model.GConnector;
import de.tesis.dynaware.grapheditor.model.GModel;
import de.tesis.dynaware.grapheditor.model.GNode;
import de.tesis.dynaware.grapheditor.model.GraphFactory;
import de.tesis.dynaware.grapheditor.model.GraphPackage;

public class DummyDataFactory {

    private static final String TEST_FILE = "test.graph";
    private static final String INPUT_TYPE = "input";
    private static final String OUTPUT_TYPE = "output";

    /**
     * Creates a new dummy model instance from a test file.
     *
     * @return a new dummy {@link GModel} instance from a test file
     */
    public static GModel createModel() {

        // Need to instantiate this to make metamodel available in unit tests.
        @SuppressWarnings("unused")
        final GraphPackage packageInstance = GraphPackage.eINSTANCE;

        final String testFilePath = DummyDataFactory.class.getResource(TEST_FILE).toExternalForm();

        final URI fileUri = URI.createURI(testFilePath);
        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);

        try {
            resource.load(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof GModel) {
            return (GModel) resource.getContents().get(0);
        } else {
            return null;
        }
    }

    public static GNode createNode() {

        final GNode node = GraphFactory.eINSTANCE.createGNode();

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        input.setType(INPUT_TYPE);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        output.setType(OUTPUT_TYPE);

        node.getConnectors().add(input);
        node.getConnectors().add(output);

        return node;
    }
}
