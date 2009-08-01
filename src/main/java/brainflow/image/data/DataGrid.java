package brainflow.image.data;

import brainflow.image.iterators.ImageIterator;
import brainflow.image.iterators.ValueIterator;
import brainflow.utils.IDimension;

/**
 * Created by IntelliJ IDEA.
 * User: Brad Buchsbaum
 * Date: Mar 17, 2008
 * Time: 10:04:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DataGrid {


    public double value(int i);

    public int numElements();

    public ValueIterator iterator();

    public IDimension<Integer> getDimensions();

}
