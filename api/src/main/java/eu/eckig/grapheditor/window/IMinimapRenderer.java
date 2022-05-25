package eu.eckig.grapheditor.window;

import eu.eckig.grapheditor.model.GNode;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;

/**
 * Interface for rendering {@link GNode} objects as {@link Node}.
 *
 * @param <N>
 *         type of {@link Node} to render a {@link GNode} in the minimap
 */
public interface IMinimapRenderer<N extends Node>
{
    /**
     * create a minimap representation for the given {@link GNode}
     *
     * @param pNode
     *         {@link GNode}
     * @return node
     */
    N createMinimapNode(final GNode pNode);

    /**
     * @return type
     */
    Class<N> getType();

    /**
     * set the layout bounds of a minimap node to the specified pWidth and pHeight.
     *
     * @param pNode
     *         node
     * @param pX
     *         the target pX coordinate location
     * @param pY
     *         the target pY coordinate location
     * @param pWidth
     *         the target layout bounds pWidth
     * @param pHeight
     *         the target layout bounds pHeight
     */
    void resizeRelocate(N pNode, double pX, double pY, double pWidth, double pHeight);

    class DefaultMinimapRenderer implements IMinimapRenderer<Rectangle>
    {
        /**
         * style class
         */
        public static final String STYLE_CLASS_NODE = "minimap-node"; //$NON-NLS-1$

        @Override
        public Rectangle createMinimapNode(final GNode pNode)
        {
            final Rectangle minimapNode = new Rectangle();
            minimapNode.getStyleClass().addAll(STYLE_CLASS_NODE, pNode.getType());
            return minimapNode;
        }

        @Override
        public Class<Rectangle> getType()
        {
            return Rectangle.class;
        }

        @Override
        public void resizeRelocate(final Rectangle pNode, final double pX, final double pY, final double pWidth,
                final double pHeight)
        {
            pNode.setX(pX);
            pNode.setY(pY);
            pNode.setWidth(pWidth);
            pNode.setHeight(pHeight);
        }
    }
}
