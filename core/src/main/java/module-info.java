module graph.editor.core
{
    requires javafx.controls;
    requires javafx.swing;

    requires graph.editor.model;
    requires graph.editor.api;

    requires org.eclipse.emf.common;
    requires org.eclipse.emf.ecore;
    requires org.eclipse.emf.ecore.xmi;
    requires edit;

    requires slf4j.api;

    exports de.tesis.dynaware.grapheditor.core;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.utils;
    exports de.tesis.dynaware.grapheditor.core.utils;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.connection;
    exports de.tesis.dynaware.grapheditor.core.view;
}