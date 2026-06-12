package io.github.eckig.grapheditor.demo.customskins;

import io.github.eckig.grapheditor.core.GraphEditorController;
import io.github.eckig.grapheditor.core.connections.ConnectionEventManager;
import io.github.eckig.grapheditor.core.view.ConnectionLayout;
import io.github.eckig.grapheditor.core.view.GraphEditorView;
import io.github.eckig.grapheditor.utils.GraphEditorProperties;

public class DemoGraphEditorController extends GraphEditorController<DemoGraphEditor>
{
    public DemoGraphEditorController(DemoGraphEditor pEditor, GraphEditorView pView, ConnectionEventManager pConnectionEventManager, GraphEditorProperties pProperties)
    {
        super(pEditor, pView, pConnectionEventManager, pProperties);
    }

    @Override
    protected ConnectionLayout createConnectionLayout()
    {
        return new DemoConnectionLayout(getSkinManager());
    }
}
