module fx.graph.editor.core
{
    requires transitive javafx.controls;
    requires transitive fx.graph.editor.model;
    requires transitive fx.graph.editor.api;
    requires org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires slf4j.api;

    exports de.tesis.dynaware.grapheditor.core;
    exports de.tesis.dynaware.grapheditor.core.connections;
    exports de.tesis.dynaware.grapheditor.core.model;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.utils;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.connection;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.tail;
    exports de.tesis.dynaware.grapheditor.core.utils;
    exports de.tesis.dynaware.grapheditor.core.view;
}