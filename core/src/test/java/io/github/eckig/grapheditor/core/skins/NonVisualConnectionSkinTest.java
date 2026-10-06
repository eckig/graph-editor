package io.github.eckig.grapheditor.core.skins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GConnectionSkin;
import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.utils.JavaFXThreadingRule;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GModel;
import javafx.scene.Node;
import javafx.scene.Parent;

/**
 * Verifies that connection skins without a visual representation (plain {@link GConnectionSkin}) are handled by the
 * graph editor: registered in the skin lookup and selectable, but not part of the view.
 */
public class NonVisualConnectionSkinTest
{

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private GraphEditor graphEditor;
    private GModel model;

    /** Connection skin without visual representation, tracking its selection state. */
    private static final class NonVisualConnectionSkin extends GConnectionSkin
    {

        private boolean selected;

        NonVisualConnectionSkin(final GConnection pConnection)
        {
            super(pConnection);
        }

        @Override
        protected void selectionChanged(final boolean pIsSelected)
        {
            selected = pIsSelected;
        }
    }

    @Before
    public void setUp()
    {
        model = DummyDataFactory.createModel();
        graphEditor = new DefaultGraphEditor();
        graphEditor.setConnectionSkinFactory(NonVisualConnectionSkin::new);
        graphEditor.setModel(model);
        graphEditor.getView().layout();
    }

    @Test
    public void skinIsInLookupButNotInView()
    {
        assertFalse(model.getConnections().isEmpty());
        for (final GConnection connection : model.getConnections())
        {
            final var skin = graphEditor.getSkinLookup().lookupConnection(connection);
            assertNotNull(skin);
            assertFalse(skin instanceof GVisualConnectionSkin);
        }
        assertFalse("no connection skin may end up in the view", containsConnectionNode(graphEditor.getView()));
    }

    @Test
    public void visualSkinIsInView()
    {
        final GraphEditor defaultEditor = new DefaultGraphEditor();
        defaultEditor.setModel(DummyDataFactory.createModel());

        assertTrue("sanity check of containsConnectionNode", containsConnectionNode(defaultEditor.getView()));
    }

    private static boolean containsConnectionNode(final Parent pParent)
    {
        for (final Node child : pParent.getChildrenUnmodifiable())
        {
            if (child.getStyleClass().contains("default-connection")
                    || child instanceof Parent p && containsConnectionNode(p))
            {
                return true;
            }
        }
        return false;
    }

    @Test
    public void skinCanBeSelectedAndDeselected()
    {
        final GConnection connection = model.getConnections().getFirst();
        final var skin = (NonVisualConnectionSkin) graphEditor.getSkinLookup().lookupConnection(connection);

        graphEditor.getSelectionManager().select(connection);
        assertTrue(skin.selected);

        graphEditor.getSelectionManager().clearSelection();
        assertFalse(skin.selected);
    }

    @Test
    public void connectionCanBeRemoved()
    {
        final GConnection connection = model.getConnections().getFirst();
        graphEditor.delete(List.of(connection));

        assertFalse(model.getConnections().contains(connection));
    }
}
