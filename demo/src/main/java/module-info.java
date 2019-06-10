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
    requires org.eclipse.elk.graph;
    requires org.eclipse.elk.core;
    requires org.eclipse.elk.alg.mrtree;
    requires org.eclipse.elk.alg.layered;
    requires org.eclipse.elk.alg.disco;
    requires org.eclipse.elk.alg.radial;
    requires org.eclipse.elk.alg.spore;
    requires com.google.common;
    requires slf4j.api;

    exports de.tesis.dynaware.grapheditor.demo to javafx.graphics, javafx.fxml;
    opens de.tesis.dynaware.grapheditor.demo to javafx.fxml;
}