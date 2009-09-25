package brainflow.image.data;

import brainflow.utils.IDimension;
import brainflow.utils.IBounds;
import org.boxwood.array.IDataGrid;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Jul 22, 2009
 * Time: 10:43:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SpatialDataGrid extends IDataGrid {

    public IDimension<Double> getSpacing();

    public IBounds<Double> getBounds();
}
