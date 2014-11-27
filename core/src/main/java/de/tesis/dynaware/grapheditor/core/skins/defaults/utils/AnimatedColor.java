package de.tesis.dynaware.grapheditor.core.skins.defaults.utils;

import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A set of values describing a color animation.
 */
public class AnimatedColor {

    private final String property;
    private final Color first;
    private final Color second;
    private final Duration interval;

    /**
     * Creates a new {@link AnimatedColor} instance for the given parameters.
     * 
     * @param property the CSS property string to be accessed in CSS
     * @param first the color of the first keyframe
     * @param second the color of the second keyframe
     * @param interval the interval of the animation
     */
    public AnimatedColor(final String property, final Color first, final Color second, final Duration interval) {
        this.property = property;
        this.first = first;
        this.second = second;
        this.interval = interval;
    }

    /**
     * Gets the CSS property string of the animation.
     * 
     * @return the CSS property string of the animation
     */
    public String getProperty() {
        return property;
    }

    /**
     * Gets the color of the first animation keyframe.
     * 
     * @return the color of the first keyframe
     */
    public Color getFirstColor() {
        return first;
    }

    /**
     * Gets the color of the second animation keyframe.
     * 
     * @return the color of the second keyframe
     */
    public Color getSecondColor() {
        return second;
    }

    /**
     * Gets the interval of the animation.
     * 
     * @return the interval of the animation
     */
    public Duration getInterval() {
        return interval;
    }
}
