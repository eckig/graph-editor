package io.github.eckig.grapheditor.demo.customskins;

import io.github.eckig.grapheditor.SkinLookup;
import io.github.eckig.grapheditor.core.view.impl.DefaultConnectionLayout;
import io.github.eckig.grapheditor.demo.customskins.tree.TreeConnectionSkin;
import io.github.eckig.grapheditor.model.GModel;

public class DemoConnectionLayout extends DefaultConnectionLayout
{
    public DemoConnectionLayout(SkinLookup pSkinLookup)
    {
        super(pSkinLookup);
    }

    @Override
    protected void redrawAllConnections(GModel pModel)
    {
        super.redrawAllConnections(pModel);

        for (final var connection : pModel.getConnections())
        {
            final var connectionSkin = getSkinLookup().lookupConnection(connection);
            if (connectionSkin instanceof TreeConnectionSkin t)
            {
                final var points = t.update();
                if (points != null)
                {
                    t.draw(points);
                }
            }
        }
    }
}
