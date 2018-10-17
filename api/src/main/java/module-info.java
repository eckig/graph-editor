module graph.editor.api
{
    requires javafx.controls;
    requires javafx.swing;

    requires graph.editor.model;

    requires org.eclipse.emf.common;
    requires org.eclipse.emf.ecore;
    requires edit;

    requires slf4j.api;

    exports de.tesis.dynaware.grapheditor;
    exports de.tesis.dynaware.grapheditor.utils;
    exports de.tesis.dynaware.grapheditor.window;
}