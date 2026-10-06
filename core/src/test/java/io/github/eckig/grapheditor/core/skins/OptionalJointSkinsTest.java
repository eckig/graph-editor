package io.github.eckig.grapheditor.core.skins;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import io.github.eckig.grapheditor.GVisualConnectionSkin;
import io.github.eckig.grapheditor.GraphEditor;
import io.github.eckig.grapheditor.core.DefaultGraphEditor;
import io.github.eckig.grapheditor.core.data.DummyDataFactory;
import io.github.eckig.grapheditor.core.utils.JavaFXThreadingRule;
import io.github.eckig.grapheditor.model.GConnection;
import io.github.eckig.grapheditor.model.GJoint;
import io.github.eckig.grapheditor.model.GModel;
import io.github.eckig.grapheditor.model.GraphFactory;
import javafx.scene.Group;
import javafx.scene.Node;

/**
 * Verifies that joint skins are only created for connections whose skin is a
 * {@link io.github.eckig.grapheditor.GJointConnectionSkin}.
 */
public class OptionalJointSkinsTest
{

    @ClassRule
    public static JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private GraphEditor graphEditor;
    private GModel model;

    /** Connection skin routing on its own, not using joints. */
    private static final class JointlessConnectionSkin extends GVisualConnectionSkin
    {

        private final Group root = new Group();

        JointlessConnectionSkin(final GConnection pConnection)
        {
            super(pConnection);
        }

        @Override
        protected void selectionChanged(final boolean pIsSelected)
        {
            // not needed
        }

        @Override
        public Node getRoot()
        {
            return root;
        }
    }

    @Before
    public void setUp()
    {
        model = DummyDataFactory.createModel();
        graphEditor = new DefaultGraphEditor();
    }

    private List<GJoint> allJoints()
    {
        return model.getConnections().stream().flatMap(c -> c.getJoints().stream()).toList();
    }

    @Test
    public void defaultConnectionSkinGetsJointSkins()
    {
        graphEditor.setModel(model);

        assertFalse("dummy model should contain joints", allJoints().isEmpty());
        for (final GJoint joint : allJoints())
        {
            assertNotNull(graphEditor.getSkinLookup().lookupJoint(joint));
        }
    }

    @Test
    public void connectionSkinWithoutJointsGetsNoJointSkins()
    {
        graphEditor.setConnectionSkinFactory(JointlessConnectionSkin::new);
        graphEditor.setModel(model);

        assertFalse(allJoints().isEmpty());
        for (final GJoint joint : allJoints())
        {
            assertNull(graphEditor.getSkinLookup().lookupJoint(joint));
        }
    }

    @Test
    public void jointAddedLaterGetsNoJointSkin()
    {
        graphEditor.setConnectionSkinFactory(JointlessConnectionSkin::new);
        graphEditor.setModel(model);

        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
        model.getConnections().getFirst().getJoints().add(joint);

        assertNull(graphEditor.getSkinLookup().lookupJoint(joint));
    }

    @Test
    public void selectAllSkipsJointsWithoutSkin()
    {
        graphEditor.setConnectionSkinFactory(JointlessConnectionSkin::new);
        graphEditor.setModel(model);

        graphEditor.getSelectionManager().selectAll();

        final var selected = graphEditor.getSelectionManager().getSelectedItems();
        assertTrue(selected.containsAll(model.getConnections()));
        for (final GJoint joint : allJoints())
        {
            assertFalse("joint without skin must not be selected", selected.contains(joint));
        }
    }

    @Test
    public void jointWithoutSkinCannotBeSelected()
    {
        // e.g. rubber band selection or paste, which select joints individually
        graphEditor.setConnectionSkinFactory(JointlessConnectionSkin::new);
        graphEditor.setModel(model);

        final GJoint joint = allJoints().getFirst();
        graphEditor.getSelectionManager().select(joint);

        assertFalse(graphEditor.getSelectionManager().isSelected(joint));
    }

    @Test
    public void jointWithSkinCanBeSelected()
    {
        graphEditor.setModel(model);

        final GJoint joint = allJoints().getFirst();
        graphEditor.getSelectionManager().select(joint);

        assertTrue(graphEditor.getSelectionManager().isSelected(joint));
    }

    @Test
    public void newJointOfJointSkinCanBeSelectedImmediately()
    {
        // e.g. paste: joints are added and selected before the skins are created
        graphEditor.setModel(model);

        final GJoint joint = GraphFactory.eINSTANCE.createGJoint();
        model.getConnections().getFirst().getJoints().add(joint);
        graphEditor.getSelectionManager().select(joint);

        assertTrue(graphEditor.getSelectionManager().isSelected(joint));
    }

    @Test
    public void selectAllIncludesJointsWithSkin()
    {
        graphEditor.setModel(model);

        graphEditor.getSelectionManager().selectAll();

        assertTrue(graphEditor.getSelectionManager().getSelectedItems().containsAll(allJoints()));
    }
}
