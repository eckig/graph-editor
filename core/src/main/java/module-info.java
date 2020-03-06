module fx.graph.editor.core
{
    requires transitive javafx.controls;
    requires transitive fx.graph.editor.model;
    requires transitive fx.graph.editor.api;
    requires org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires org.eclipse.emf.ecore;
    requires transitive org.eclipse.emf.common;
    requires org.slf4j;

    exports de.tesis.dynaware.grapheditor.core;
    exports de.tesis.dynaware.grapheditor.core.connections;
    exports de.tesis.dynaware.grapheditor.core.connectors;
    exports de.tesis.dynaware.grapheditor.core.skins;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.connection;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.connection.segment;
    exports de.tesis.dynaware.grapheditor.core.skins.defaults.tail;
    exports de.tesis.dynaware.grapheditor.core.view;
}
