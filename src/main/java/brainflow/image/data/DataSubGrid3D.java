package brainflow.image.data;

import brainflow.image.interpolation.InterpolationFunction3D;
import brainflow.image.space.IImageSpace3D;
import brainflow.image.space.ImageSpace3D;
import brainflow.image.space.Axis;
import brainflow.image.iterators.Iterator3D;
import brainflow.image.iterators.ValueIterator;
import brainflow.image.axis.ImageAxis;
import brainflow.image.anatomy.Anatomy3D;
import brainflow.image.io.ImageInfo;
import brainflow.utils.DataType;
import brainflow.utils.IDimension;
import brainflow.utils.Dimension3D;
import brainflow.math.Index3D;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Jun 30, 2009
 * Time: 10:46:54 PM
 * To change this template use File | Settings | File Templates.
 */


public class DataSubGrid3D implements IImageData3D {

    private IImageData3D wrapped;

    private IImageSpace3D space;

    private int xoffset;

    private int xlen;

    private int yoffset;

    private int ylen;

    private int zoffset;

    private int zlen;

    private int indexOffset;

    public DataSubGrid3D(IImageData3D wrapped, int xoffset, int xlen, int yoffset, int ylen, int zoffset, int zlen) {
        this.wrapped = wrapped;
        this.xoffset = xoffset;
        this.xlen = xlen;
        this.yoffset = yoffset;
        this.ylen = ylen;
        this.zoffset = zoffset;
        this.zlen = zlen;

        int planeSize = wrapped.getDimensions().getDim(0) * wrapped.getDimensions().getDim(1);
        int dim0 = wrapped.getDimensions().getDim(0);

        indexOffset = zoffset * planeSize + dim0 * yoffset + xoffset;

        initSpace();
    }


    private void initSpace() {
        ImageAxis xaxis = wrapped.getImageSpace().getImageAxis(Axis.X_AXIS);
        ImageAxis yaxis = wrapped.getImageSpace().getImageAxis(Axis.Y_AXIS);
        ImageAxis zaxis = wrapped.getImageSpace().getImageAxis(Axis.Z_AXIS);

        double x1 = xaxis.gridToReal(xoffset);
        double x2 = x1 + xaxis.getSpacing() * xlen;

        double y1 = xaxis.gridToReal(yoffset);
        double y2 = y1 + yaxis.getSpacing() * ylen;

        double z1 = xaxis.gridToReal(zoffset);
        double z2 = z1 + zaxis.getSpacing() * zlen;


        ImageAxis newxaxis = new ImageAxis(x1, x2, xaxis.getAnatomicalAxis(), xlen);
        ImageAxis newyaxis = new ImageAxis(y1, y2, yaxis.getAnatomicalAxis(), ylen);
        ImageAxis newzaxis = new ImageAxis(z1, z2, yaxis.getAnatomicalAxis(), zlen);

        space = new ImageSpace3D(newxaxis, newyaxis, newzaxis);

    }

    @Override
    public double value(float x, float y, float z, InterpolationFunction3D interp) {
        return interp.interpolate(x, y, z, this);
    }

    //@Override
    //public double valueAtReal(float rx, float ry, float rz, InterpolationFunction3D interp) {
    //    return wrapped.valueAtReal(rx,ry,rz, interp);
    //}

    @Override
    public double worldValue(float wx, float wy, float wz, InterpolationFunction3D interp) {
        double x = wrapped.getImageSpace().worldToGridX(wx, wy, wz);
        double y = wrapped.getImageSpace().worldToGridY(wx, wy, wz);
        double z = wrapped.getImageSpace().worldToGridZ(wx, wy, wz);


        return interp.interpolate(x, y, z, this);
    }


    @Override
    public double value(int x, int y, int z) {
        return wrapped.value(x + xoffset, y + yoffset, z + zoffset);
    }

    @Override
    public IImageSpace3D getImageSpace() {
        return space;
    }

    @Override
    public double value(int index) {
        return wrapped.value(index + indexOffset);
    }

    @Override
    public int numElements() {
        return space.getNumSamples();
    }

    @Override
    public ValueIterator iterator() {
        return new Iterator3D(this);
    }

    @Override
    public int indexOf(int x, int y, int z) {
        return wrapped.indexOf(x,y,z);
    }

    @Override
    public Index3D indexToGrid(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageBuffer3D createWriter(boolean clear) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Anatomy3D getAnatomy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataType getDataType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDimension(Axis axisNum) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double maxValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double minValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImageInfo getImageInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getImageLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension3D<Integer> getDimensions() {
        throw new UnsupportedOperationException();
    }
}
