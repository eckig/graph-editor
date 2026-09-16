package io.github.eckig.grapheditor.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

import org.junit.Test;

/**
 * Tests the localizable strings of {@link GraphEditorProperties}.
 */
public class GraphEditorPropertiesResourceTest {

    private static final String TEXT = "graphEditor.accessibleText";
    private static final String ROLE = "graphEditor.accessibleRoleDescription";

    private static ResourceBundle bundleOf(final Map<String, String> entries) {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(final String key) { return entries.get(key); }
            @Override
            public Enumeration<String> getKeys() { return Collections.enumeration(entries.keySet()); }
        };
    }

    @Test
    public void shippedDefaultsAreUsedWhenNothingIsConfigured() {
        final String text = new GraphEditorProperties().getString(TEXT, 3, 2);
        assertTrue("expected the counts in: " + text, text.contains("3") && text.contains("2"));
    }

    @Test
    public void customBundleOverridesTheDefaults() {
        final GraphEditorProperties properties = new GraphEditorProperties();
        properties.setResourceBundle(bundleOf(Map.of(TEXT, "Knoten {0}, Kanten {1}")));

        assertEquals("Knoten 3, Kanten 2", properties.getString(TEXT, 3, 2));
    }

    @Test
    public void incompleteCustomBundleFallsBackToTheDefaults() {
        final GraphEditorProperties properties = new GraphEditorProperties();
        properties.setResourceBundle(bundleOf(Map.of(TEXT, "Knoten {0}, Kanten {1}")));

        assertEquals("graph editor canvas", properties.getString(ROLE));
    }

    @Test
    public void unknownKeyReturnsTheKeyInsteadOfThrowing() {
        assertEquals("no.such.key", new GraphEditorProperties().getString("no.such.key"));
    }

    @Test
    public void bundleIsPerInstanceAndNotGlobal() {
        final GraphEditorProperties translated = new GraphEditorProperties();
        translated.setResourceBundle(bundleOf(Map.of(ROLE, "Leinwand")));

        final GraphEditorProperties untouched = new GraphEditorProperties();

        assertEquals("Leinwand", translated.getString(ROLE));
        assertEquals("a second editor must be unaffected", "graph editor canvas", untouched.getString(ROLE));
    }

    @Test
    public void copyConstructorKeepsTheBundle() {
        final GraphEditorProperties original = new GraphEditorProperties();
        original.setResourceBundle(bundleOf(Map.of(ROLE, "Leinwand")));

        assertEquals("Leinwand", new GraphEditorProperties(original).getString(ROLE));
    }

    @Test
    public void bundleDefaultsToNull() {
        assertNull(new GraphEditorProperties().getResourceBundle());
    }
}
