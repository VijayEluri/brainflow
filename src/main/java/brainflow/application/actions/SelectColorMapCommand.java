package brainflow.application.actions;

import brainflow.colormap.IColorMap;
import brainflow.colormap.LinearColorMap2;
import brainflow.core.ImageView;
import brainflow.core.layer.ImageLayer;


import javax.swing.*;
import java.awt.image.IndexColorModel;

/**
 * Created by IntelliJ IDEA.
 * User: Brad Buchsbaum
 * Date: Jul 6, 2006
 * Time: 1:01:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class SelectColorMapCommand extends BrainFlowCommand  {

    private IndexColorModel icm;

    public SelectColorMapCommand(String string, Icon icon, IndexColorModel icm) {
        this.icm = icm;
        getDefaultFace(true).setText(string);
        getDefaultFace(true).setIcon(icon);
    }

    protected void handleExecute() {
        ImageView view = getSelectedView();

        if (view != null) {

            ImageLayer layer = view.getSelectedLayer();

            IColorMap map = layer.getImageLayerProperties().getColorMap();

            LinearColorMap2 lmap = new LinearColorMap2(map.getMinimumValue(), map.getMaximumValue(), map.getLowClip(), map.getHighClip(), icm);
            layer.getImageLayerProperties().colorMap.set(lmap);


        }

    }

   


}