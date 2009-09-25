package brainflow.image.data;

import brainflow.image.space.IImageSpace;
import org.boxwood.array.IDataGrid;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Nov 29, 2008
 * Time: 12:35:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ImageBuffer extends IDataGrid {

    public void setValue(int index, double value);
    
    public IImageData asImageData();

    public IImageSpace getImageSpace();

}
