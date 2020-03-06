/**
 * Sample graph editor implementation
 */
module fx.graph.editor.demo
{
    requires javafx.fxml;
    requires fx.graph.editor.api;
    requires fx.graph.editor.core;
    requires org.eclipse.emf.ecore;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires org.slf4j;

    exports de.tesis.dynaware.grapheditor.demo to javafx.graphics, javafx.fxml;
    opens de.tesis.dynaware.grapheditor.demo to javafx.fxml;
}
