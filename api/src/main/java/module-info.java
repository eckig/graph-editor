module eu.eckig.grapheditor.api
{
    requires transitive javafx.controls;
    requires transitive eu.eckig.grapheditor.model;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.edit;
    requires org.slf4j;

    exports eu.eckig.grapheditor;
    exports eu.eckig.grapheditor.utils;
    exports eu.eckig.grapheditor.window;
}
