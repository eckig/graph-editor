module fx.graph.editor.api
{
    requires transitive javafx.controls;
    requires transitive fx.graph.editor.model;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.edit;
    requires org.slf4j;

    exports de.tesis.dynaware.grapheditor;
    exports de.tesis.dynaware.grapheditor.utils;
    exports de.tesis.dynaware.grapheditor.window;
}
