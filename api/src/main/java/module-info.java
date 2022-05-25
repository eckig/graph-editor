module io.github.eckig.grapheditor.api
{
    requires transitive javafx.controls;
    requires transitive io.github.eckig.grapheditor.model;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.edit;
    requires org.slf4j;

    exports io.github.eckig.grapheditor;
    exports io.github.eckig.grapheditor.utils;
    exports io.github.eckig.grapheditor.window;
}
