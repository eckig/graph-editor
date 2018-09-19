package de.tesis.dynaware.grapheditor.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;

public class GraphEventManager {

    private final ObjectProperty<GraphInputMode> inputMode = new ObjectPropertyBase<GraphInputMode>(GraphInputMode.SELECTION) {

        @Override
        public Object getBean() {
            return GraphEventManager.this;
        }

        @Override
        public String getName() {
            return "inputMode"; //$NON-NLS-1$
        }

        @Override
        public void set(GraphInputMode newValue) {
            super.set(newValue == null ? GraphInputMode.SELECTION : newValue);
        }

        @Override
        public void setValue(GraphInputMode newValue) {
            set(newValue);
        }
    };

    private final ObjectProperty<GraphInputGesture> gesture = new ObjectPropertyBase<GraphInputGesture>() {

        @Override
        public Object getBean() {
            return GraphEventManager.this;
        }

        @Override
        public String getName() {
            return "inputGesture"; //$NON-NLS-1$
        }
    };

    /**
     * @return currently active {@link GraphInputMode}
     */
    public GraphInputMode getInputMode() {
        return inputMode.get();
    }

    /**
     * @param pInputMode
     *            new {@link GraphInputMode}
     */
    public void setInputMode(final GraphInputMode pInputMode)
    {
        inputMode.set(pInputMode);
    }

    /**
     * @return {@link ObjectProperty} controlling the current {@link GraphInputMode}
     */
    public ObjectProperty<GraphInputMode> inputModeProperty() {
        return inputMode;
    }

    /**
     * @return currently active {@link GraphInputGesture}
     */
    public GraphInputGesture getInputGesture() {
        return gesture.get();
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     * 
     * @param pInputMode
     *            new {@link GraphInputGesture}
     */
    public void activateInputGesture(final GraphInputGesture pInputMode)
    {
        gesture.set(pInputMode);
    }

    /**
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it.
     * </p>
     * 
     * @param pExpected
     *            the expected gesture that should be finished
     */
    public void finishInputGesture(final GraphInputGesture pExpected)
    {
        if (getInputGesture() == pExpected)
        {
            gesture.set(null);
        }
    }

    /**
     * @return {@link ObjectProperty} controlling the current {@link GraphInputGesture}
     */
    public ObjectProperty<GraphInputGesture> inputGestureProperty() {
        return gesture;
    }

    /**
     * @param pGesture
     *            {@link GraphInputGesture}
     * @return {@code true} if {@link #getInputGesture() the currently active
     *         gesture} matches the given one or no gesture is active at all
     */
    public boolean isInputGestureActiveOrEmpty(final GraphInputGesture pGesture)
    {
        final GraphInputGesture current = getInputGesture();
        return current == null || current == pGesture;
    }
}
