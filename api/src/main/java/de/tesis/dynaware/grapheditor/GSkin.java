/*
 * Copyright (C) 2005 - 2015 by TESIS DYNAware GmbH
 */
package de.tesis.dynaware.grapheditor;

import java.util.function.Consumer;

import org.eclipse.emf.ecore.EObject;

import de.tesis.dynaware.grapheditor.utils.DraggableBox;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;


/**
 * Abstract class that all skins inherit from. Contains logic common to all
 * skins.
 *
 * @param <T>
 *            A subtype of {@link EObject} that the Skin represents.
 */
public abstract class GSkin<T extends EObject>
{

    private final BooleanProperty selectedProperty = new BooleanPropertyBase(false)
    {

        @Override
        protected void invalidated()
        {
            selectionChanged(get());
        }

        @Override
        public Object getBean()
        {
            return GSkin.this;
        }

        @Override
        public String getName()
        {
            return "selected"; //$NON-NLS-1$
        }

    };

    private GraphEditor graphEditor;
    private final T item;
    private Consumer<GSkin<?>> onPositionMoved;

    /**
     * Constructor
     *
     * @param pItem
     *            item represented by this skin
     */
    protected GSkin(T pItem)
    {
        this.item = pItem;
    }

    /**
     * Sets the graph editor instance that this skin is a part of.
     *
     * @param pGraphEditor
     *            a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor pGraphEditor)
    {
        this.graphEditor = pGraphEditor;
        updateSelection();
    }

    /**
     * Gets the graph editor instance that this skin is a part of.
     *
     * <p>
     * This is provided for advanced skin customization purposes only. Use at
     * your own risk.
     * </p>
     *
     * @return the {@link GraphEditor} instance that this skin is a part of
     */
    protected GraphEditor getGraphEditor()
    {
        return graphEditor;
    }

    /**
     * Gets whether the skin is selected or not.
     *
     * @return {@code true} if the skin is selected, {@code false} if not
     */
    public boolean isSelected()
    {
        return selectedProperty.get();
    }

    /**
     * Sets whether the skin is selected or not.
     * <p>
     * <b>Should not</b> be called directly, the selection state is managed by
     * the selection manager of the graph editor!
     * </p>
     *
     * @param isSelected
     *            {@code true} if the skin is selected, {@code false} if not
     */
    protected void setSelected(final boolean isSelected)
    {
        selectedProperty.set(isSelected);
    }

    /**
     * Updates whether this skin is in a selected state or not.
     * <p>
     * This method will be automatically called by the SelectionTracker when
     * needed.
     * </p>
     */
    public void updateSelection()
    {
        setSelected(graphEditor == null ? false : graphEditor.getSelectionManager().isSelected(item));
    }

    /**
     * The property that determines whether the skin is selected or not.
     *
     * @return a {@link BooleanProperty} containing {@code true} if the skin is
     *         selected, {@code false} if not
     */
    public ReadOnlyBooleanProperty selectedProperty()
    {
        return selectedProperty;
    }

    /**
     * Is called whenever the selection state has changed.
     *
     * @param isSelected
     *            {@code true} if the skin is selected, {@code false} if not
     */
    protected abstract void selectionChanged(final boolean isSelected);

    /**
     * Called after the skin is removed. Can be overridden for cleanup.
     */
    public void dispose()
    {
        final Node root = getRoot();
        if (root instanceof DraggableBox)
        {
            ((DraggableBox) root).dispose();
        }
        onPositionMoved = null;
        graphEditor = null;
    }

    /**
     * Gets the root JavaFX node of the skin.
     *
     * @return a the skin's root JavaFX {@link Node}
     */
    public abstract Node getRoot();

    /**
     * @return item represented by this skin
     */
    public final T getItem()
    {
        return item;
    }

    /**
     * <p>
     * INTERNAL API
     * </p>
     *
     * @param pOnPositionMoved
     *            internal update hook to be informed when the position has been
     *            changed
     */
    public final void impl_setOnPositionMoved(final Consumer<GSkin<?>> pOnPositionMoved)
    {
        onPositionMoved = pOnPositionMoved;
    }

    /**
     * <p>
     * INTERNAL API
     * </p>
     * will be called when the position of this skin has been moved
     *
     * @since 16.01.2019
     */
    public final void impl_positionMoved()
    {
        final Consumer<GSkin<?>> inform = onPositionMoved;
        if (inform != null)
        {
            inform.accept(this);
        }
    }
}
