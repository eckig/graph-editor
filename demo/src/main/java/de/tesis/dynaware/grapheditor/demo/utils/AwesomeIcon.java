package de.tesis.dynaware.grapheditor.demo.utils;

import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A few icons.
 *
 * <p>
 * Uses Font Awesome by Dave Gandy - http://fontawesome.io.
 * </p>
 */
public enum AwesomeIcon {

    /**
     * A plus icon.
     */
    PLUS(0xf067),

    /**
     * A times / cross icon.
     */
    TIMES(0xf00d),

    /**
     * A map icon.
     */
    MAP(0xf03e);

    private static final String STYLE_CLASS = "icon"; //$NON-NLS-1$
    private static final String FONT_AWESOME = "FontAwesome"; //$NON-NLS-1$
    private int unicode;

    /**
     * Creates a new awesome icon for the given unicode value.
     *
     * @param pUnicode the unicode value as an integer
     */
    private AwesomeIcon(final int pUnicode) {
        this.unicode = pUnicode;
    }

    /**
     * Returns a new {@link Node} containing the icon.
     *
     * @return a new node containing the icon
     */
    public Node node() {

        final Text text = new Text(String.valueOf((char) unicode));
        text.getStyleClass().setAll(STYLE_CLASS);
        text.setFont(Font.font(FONT_AWESOME));

        return text;
    }
}
