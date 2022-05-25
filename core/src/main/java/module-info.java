module eu.eckig.grapheditor.core
{
    requires transitive javafx.controls;
    requires transitive eu.eckig.grapheditor.model;
    requires transitive eu.eckig.grapheditor.api;
    requires org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires org.eclipse.emf.ecore;
    requires transitive org.eclipse.emf.common;
    requires org.slf4j;

    exports eu.eckig.grapheditor.core;
    exports eu.eckig.grapheditor.core.connections;
    exports eu.eckig.grapheditor.core.connectors;
    exports eu.eckig.grapheditor.core.skins;
    exports eu.eckig.grapheditor.core.skins.defaults;
    exports eu.eckig.grapheditor.core.skins.defaults.connection;
    exports eu.eckig.grapheditor.core.skins.defaults.connection.segment;
    exports eu.eckig.grapheditor.core.skins.defaults.tail;
    exports eu.eckig.grapheditor.core.view;
}
