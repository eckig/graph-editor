module io.github.eckig.grapheditor.core
{
    requires transitive javafx.controls;
    requires transitive io.github.eckig.grapheditor.model;
    requires transitive io.github.eckig.grapheditor.api;
    requires org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires org.eclipse.emf.ecore;
    requires transitive org.eclipse.emf.common;
    requires org.slf4j;

    exports io.github.eckig.grapheditor.core;
    exports io.github.eckig.grapheditor.core.connections;
    exports io.github.eckig.grapheditor.core.connectors;
    exports io.github.eckig.grapheditor.core.skins;
    exports io.github.eckig.grapheditor.core.skins.defaults;
    exports io.github.eckig.grapheditor.core.skins.defaults.connection;
    exports io.github.eckig.grapheditor.core.skins.defaults.connection.segment;
    exports io.github.eckig.grapheditor.core.skins.defaults.tail;
    exports io.github.eckig.grapheditor.core.view;
}
