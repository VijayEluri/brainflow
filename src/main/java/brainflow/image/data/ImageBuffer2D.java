package brainflow.image.data;

import org.boxwood.array.IDataGrid2D;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Nov 29, 2008
 * Time: 12:37:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ImageBuffer2D extends ImageBuffer, IDataGrid2D {


    public void setValue(int x, int y, double val);

    public IImageData2D asImageData();




}
