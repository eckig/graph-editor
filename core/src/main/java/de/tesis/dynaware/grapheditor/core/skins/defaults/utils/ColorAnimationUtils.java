package de.tesis.dynaware.grapheditor.core.skins.defaults.utils;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Utility class for creating animated color properties that can be accessed in CSS.
 */
public class ColorAnimationUtils {

    private static final String TIMELINE_KEY = "color-animation-utils-timeline";

    private static final String COLOR_FORMAT = "#%02x%02x%02x";

    /**
     * Adds animated color properties to the given node that can be accessed from CSS.
     * 
     * @param node the node to be styled with animated colors
     * @param data a {@link AnimatedColor} object storing the animation parameters
     */
    public static void animateColor(final Node node, final AnimatedColor data) {

        removeAnimation(node);

        final ObjectProperty<Color> baseColor = new SimpleObjectProperty<>();

        final KeyValue firstkeyValue = new KeyValue(baseColor, data.getFirstColor());
        final KeyValue secondKeyValue = new KeyValue(baseColor, data.getSecondColor());
        final KeyFrame firstKeyFrame = new KeyFrame(Duration.ZERO, firstkeyValue);
        final KeyFrame secondKeyFrame = new KeyFrame(data.getInterval(), secondKeyValue);
        final Timeline timeline = new Timeline(firstKeyFrame, secondKeyFrame);

        baseColor.addListener((v, o, n) -> {

            final int redValue = (int) (n.getRed() * 255);
            final int greenValue = (int) (n.getGreen() * 255);
            final int blueValue = (int) (n.getBlue() * 255);

            final String format = data.getProperty() + ": " + COLOR_FORMAT + ";";
            node.setStyle(String.format(format, redValue, greenValue, blueValue));
        });

        node.getProperties().put(TIMELINE_KEY, timeline);

        timeline.setAutoReverse(true);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Removes an animated color from this node, if one has been set on it.
     */
    public static void removeAnimation(final Node node) {

        // Stopping the timeline should allow object properties that depend on it to be garbage collected.
        if (node.getProperties().get(TIMELINE_KEY) instanceof Timeline) {
            final Timeline timeline = (Timeline) node.getProperties().get(TIMELINE_KEY);
            timeline.stop();
        }
    }
}
