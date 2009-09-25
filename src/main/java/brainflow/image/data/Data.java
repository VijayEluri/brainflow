package brainflow.image.data;

import brainflow.image.space.*;
import brainflow.image.interpolation.InterpolationFunction3D;
import brainflow.image.interpolation.InterpolationFunction2D;
import brainflow.image.anatomy.Anatomy3D;
import brainflow.image.anatomy.BrainPoint1D;
import brainflow.image.io.ImageInfo;
import brainflow.image.iterators.ImageIterator;
import brainflow.image.iterators.Iterator3D;
import brainflow.image.iterators.ValueIterator;
import brainflow.image.axis.ImageAxis;
import brainflow.utils.*;
import brainflow.math.Index3D;

import java.util.Arrays;

import org.boxwood.array.IDataGrid3D;


/**
 * Created by IntelliJ IDEA.
 * User: Brad Buchsbaum
 * Date: Mar 9, 2008
 * Time: 9:10:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class Data {


    private Data() {}


    public static IImageData3D asImageData3D(IImageData2D data2d, BrainPoint1D zvalue, double thickness) {
        IImageSpace2D space2d = data2d.getImageSpace();

        ImageAxis zaxis = new ImageAxis(zvalue.getValue() - (thickness / 2.0), zvalue.getValue() + (thickness / 2.0), zvalue.getAnatomy(), 1);
        IImageSpace3D space = new ImageSpace3D(space2d.getImageAxis(Axis.X_AXIS), space2d.getImageAxis(Axis.Y_AXIS), zaxis);
        IImageData3D data3d = new BasicImageData3D(space, data2d.getDataType());

        ImageBuffer3D buffer = data3d.createWriter(false);

        ValueIterator imageIterator = data2d.iterator();

        while(imageIterator.hasNext()) {
            int i = imageIterator.index();
            double val = imageIterator.next();
            buffer.setValue(i, val);
        }


        return buffer.asImageData();

    }

    public static ImageBuffer3D createWriter(IImageData3D input, DataType out) {
        final BasicImageData3D delegate = new BasicImageData3D(input.getImageSpace(),out); 
        final IImageSpace3D space = input.getImageSpace();
        
        return new ImageBuffer3D() {

            public final void setValue(int x, int y, int z, double val) {
                delegate.setValue(x,y,z,val);
            }

            public final IImageData3D asImageData() {
                return delegate;
            }

            public final void setValue(int index, double value) {
                delegate.setValue(index,value);
            }

            public final double value(int index) {
                return delegate.value(index);
            }

            public final int length() {
                return delegate.length();
            }

            @Override
            public int indexOf(int i, int j, int k) {
                return delegate.indexOf(i,j,k);
            }

            @Override
            public final double value(float x, float y, float z, InterpolationFunction3D interp) {
                return delegate.value(x,y,z,interp);
            }

            @Override
            public final double value(int x, int y, int z) {
                return delegate.value(x,y,z);
            }

            @Override
            public ValueIterator iterator() {
                return delegate.iterator();
            }

            @Override
            public final IDataGrid3D subGrid(int x0, int x1, int y0, int y1, int z0, int z1) {
                throw new UnsupportedOperationException();
            }

            @Override
            public final Dimension3D<Integer> dim() {
                return delegate.dim();
            }

            @Override
            public final IImageSpace3D getImageSpace() {
                return space;

            }
        };

    }


    public static IMaskedData createMask(MaskPredicate pred, IImageData dat) {
        if (dat instanceof IImageData2D) {
            return new MaskedData2D((IImageData2D) dat, pred);
        } else if (dat instanceof IImageData3D) {
            return new MaskedData3D((IImageData3D) dat, pred);
        }

        throw new IllegalArgumentException("could not create mask, wrong data class " + dat.getClass());
    }

    public static boolean elementsEquals(IImageData d1, IImageData d2, float tolerance) {
        if (d1.length() != d2.length()) return false;

        ValueIterator iter1 = d1.iterator();
        ValueIterator iter2 = d2.iterator();

        while (iter1.hasNext() && iter2.hasNext()) {
            if (!NumberUtils.equals(iter1.next(), iter2.next(), tolerance)) {
                return false;
            }

        }

        return true;

    }

    

    public static double[] toArray(IImageData data) {
        ValueIterator iter = data.iterator();
        double[] ret = new double[data.length()];

        while (iter.hasNext()) {
            ret[iter.index()] = iter.next();

        }

        return ret;
    }

    public static double[] sort(IImageData data) {
        double[] ret = toArray(data);
        Arrays.sort(ret);
        return ret;
    }


    public static double meanDeviation(IImageData data, double referenceVal) {
        ValueIterator iter = data.iterator();

        double sum = 0;
        int count = 0;

        while (iter.hasNext()) {
            double val = iter.next();
            if (val != 0) {
                sum = sum + Math.pow(val - referenceVal, 2);
                count++;
            }

        }

        return sum / count;


    }


    public static double nonzeroMean(IImageData data) {
        ValueIterator iter = data.iterator();

        double sum = 0;
        int count = 0;
        while (iter.hasNext()) {
            double val = iter.next();
            if (val != 0) {
                sum = sum + val;
                count++;
            }

        }

        return sum / count;

    }

    public static double mean(IImageData data) {
        ValueIterator iter = data.iterator();

        double sum = 0;

        while (iter.hasNext()) {
            sum = sum + iter.next();
        }

        return sum / iter.index();


    }

    /*public static IImageData2D sliceView(final IImageData3D data3d, Anatomy2D sliceAnatomy, int sliceIndex) {
        int[][] permutationMatrix;
        //x, y =  
        //ImageAxis xaxis = data3d.getImageSpace().getImageAxis(sliceAnatomy.XAXIS, true).matchAxis(v)
        //final IImageSpace space2d = new ImageSpace2D(data3d.getImageSpace().getImageAxis(sliceAnatomy.XAXIS))
        return new AbstractImageData2D() {
            @Override
            public double value(int index) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ImageIterator iterator() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public ImageBuffer2D createWriter(boolean clear) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double value(double x, double y, InterpolationFunction2D interp) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double worldValue(double realx, double realy, InterpolationFunction2D interp) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public double value(int x, int y) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }

    }  */

    public static IImageData3D negate(final IImageData3D data) {
        return createScaledData(data, -1);

    }

    public static IImageData3D createScaledData(final IImageData3D data, final double scaleFactor) {
        return new IImageData3D() {
            public IImageSpace3D getImageSpace() {
                return data.getImageSpace();
            }

            public final int indexOf(int x, int y, int z) {
                return data.indexOf(x, y, z);
            }

            public Index3D indexToGrid(int idx) {
                return data.indexToGrid(idx);
            }


            public Anatomy3D getAnatomy() {
                return data.getAnatomy();
            }

            public DataType getDataType() {
                return data.getDataType();
            }

            public int getDimension(Axis axisNum) {
                return data.getDimension(axisNum);
            }

            @Override
            public Dimension3D<Integer> dim() {
                return data.dim();
            }

            public ImageInfo getImageInfo() {
                return data.getImageInfo();
            }

            public String getImageLabel() {
                return data.getImageLabel();
            }

            public ImageBuffer3D createWriter(boolean clear) {
                return data.createWriter(clear);
            }

            @Override
            public IDataGrid3D subGrid(int x0, int x1, int y0, int y1, int z0, int z1) {
                throw new UnsupportedOperationException();
                //return data.subGrid(x0, x1, y0, y1, z0, z1);
            }

            @Override
            public ValueIterator iterator() {
                return new Iterator3D(this);
            }

            public double maxValue() {
                return data.maxValue() * scaleFactor;
            }

            public double minValue() {
                return data.minValue() * scaleFactor;
            }


            public final double value(int index) {
                return data.value(index) * scaleFactor;
            }

            public int length() {
                return data.length();
            }

            public final double value(float x, float y, float z, InterpolationFunction3D interp) {
                return data.value(x, y, z, interp) * scaleFactor;
            }

            public final double value(int x, int y, int z) {
                return data.value(x, y, z) * scaleFactor;
            }

            public final double worldValue(float realx, float realy, float realz, InterpolationFunction3D interp) {
                return data.worldValue(realx, realy, realz, interp) * scaleFactor;
            }

            public String toString() {
                return data.toString();
            }

            public boolean equals(Object obj) {
                //todo how to check for equality of "scaleFactor"?
                return data.equals(obj);


            }
        };
    }


    public static IImageData3D createConstantData(final double value, final IImageSpace3D space) {
        return new AbstractImageData3D(space, DataType.DOUBLE) {
            public double value(int index) {
                return value;
            }


            public void setValue(int idx, double val) {
                throw new UnsupportedOperationException();
            }

            public double minValue() {
                return value;
            }

            public double maxValue() {
                return value;
            }


            public double worldValue(float realx, float realy, float realz, InterpolationFunction3D interp) {
                return value;
            }

            public double value(float x, float y, float z, InterpolationFunction3D interp) {
                return value;
            }

            public double value(int x, int y, int z) {
                return value;
            }

            public void setValue(int x, int y, int z, double val) {
                throw new UnsupportedOperationException();
            }

            public ImageBuffer3D createWriter(boolean clear) {
                throw new UnsupportedOperationException("Cannot create writer for class " + getClass());
            }

            public String getImageLabel() {
                return "constant: " + value;
            }

            @Override
            public IDataGrid3D subGrid(int x0, int x1, int y0, int y1, int z0, int z1) {
                throw new UnsupportedOperationException();
            }
        };

    }

    public static IntImageData3D makeIntData3D(IImageSpace3D space3d) {
        return new IntData3D(space3d);
    }

    public static IntImageData3D makeIntData3D(IImageSpace3D space3d, int[] vals) {
        return new IntData3D(space3d, vals);
    }


    private static class IntData2D extends AbstractImageData2D implements IntImageData2D {
        private IntData2D(ImageSpace2D space, int[] vals) {
            super(space);
            if (vals.length != space.getNumSamples()) {
                throw new IllegalArgumentException("array has wrong length: " + vals.length + " expected: " + space.getNumSamples());
            }
            this.vals = vals;
        }

        private IntData2D(ImageSpace2D space) {
            super(space);

            this.vals = new int[space.getNumSamples()];
        }

        int[] vals = new int[space.getNumSamples()];

        public double value(int index) {
            return vals[index];
        }

        @Override
        public Dimension2D<Integer> dim() {
            return getImageSpace().getDimension();
        }

        public ImageIterator iterator() {
            throw new UnsupportedOperationException();
        }

        

        public IntImageBuffer2D createWriter(boolean clear) {
            return new IntImageBuffer2D() {

                public void setIntValue(int x, int y, int val) {
                   vals[indexOf(x,y)] = val;
                }

                public IntImageData2D asImageData() {
                    return IntData2D.this;
                }

                public void setValue(int x, int y, double val) {
                    vals[indexOf(x,y)] = (int)val;
                }

                public void setValue(int index, double value) {
                    vals[index] = (int)value;
                }

                public double value(int index) {
                    return vals[index];
                }

                public int length() {
                    return vals.length;
                }

                public ValueIterator iterator() {
                    throw new UnsupportedOperationException();
                }

                public double value(float x, float y, InterpolationFunction2D interp) {
                    throw new UnsupportedOperationException();
                }

                public double worldValue(float realx, float realy, InterpolationFunction2D interp) {
                   throw new UnsupportedOperationException();
                }

                public double value(int x, int y) {
                    return vals[indexOf(x,y)];
                }

                public ImageSpace2D getImageSpace() {
                    return IntData2D.this.getImageSpace();
                }

                @Override
                public int indexOf(int i, int j) {
                    return IntData2D.this.indexOf(i,j);
                }

                @Override
                public Dimension2D<Integer> dim() {
                    return IntData2D.this.dim();

                }
            };
        }

        public double value(float x, float y, InterpolationFunction2D interp) {
            throw new UnsupportedOperationException();
        }

        public double worldValue(float realx, float realy, InterpolationFunction2D interp) {
            throw new UnsupportedOperationException();
        }

        public double value(int x, int y) {
            throw new UnsupportedOperationException();
        }

        public int getIntValue(int x, int y) {
            throw new UnsupportedOperationException();
        }
    }



    private static class IntData3D extends AbstractImageData3D implements IntImageData3D {

        private IntData3D(IImageSpace3D space, int[] vals) {
            super(space);
            if (vals.length != space.getNumSamples()) {
                throw new IllegalArgumentException("array has wrong length: " + vals.length + " expected: " + space.getNumSamples());
            }
            this.vals = vals;
        }

        private IntData3D(IImageSpace3D space) {
            super(space);

            this.vals = new int[space.getNumSamples()];
        }

        private int[] vals = new int[space.getNumSamples()];

        public double value(int index) {
            return vals[index];
        }

        public double value(float x, float y, float z, InterpolationFunction3D interp) {
            //todo should be implemented in super class
            throw new UnsupportedOperationException();
        }

        public double worldValue(float realx, float realy, float realz, InterpolationFunction3D interp) {
            //todo should be implemented in super class
            throw new UnsupportedOperationException();
        }

        public double value(int x, int y, int z) {
            //todo should be implemented in super class
            throw new UnsupportedOperationException();
        }

        public int getIntValue(int x, int y, int z) {
            return vals[indexOf(x, y, z)];
        }

        @Override
        public Dimension3D<Integer> dim() {
            return getImageSpace().getDimension();
        }

        @Override
        public IDataGrid3D subGrid(int x0, int x1, int y0, int y1, int z0, int z1) {
            throw new UnsupportedOperationException();
        }

        public IntImageBuffer3D createWriter(boolean clear) {
            final IntData3D delegate = this;
            return new IntImageBuffer3D() {

                IImageSpace3D space = IntData3D.this.getImageSpace();


                public void setValue(int x, int y, int z, double val) {
                    delegate.vals[indexOf(x, y, z)] = (int) val;
                }

                

                public void setIntValue(int x, int y, int z, int val) {
                   delegate.vals[indexOf(x, y, z)] = val;
                }

                public IntImageData3D asImageData() {
                    return delegate;
                }

                public void setValue(int index, double val) {
                    delegate.vals[index] = (int) val;

                }

                public double value(int index) {
                    return delegate.value(index);
                }

                public int length() {
                    return delegate.length();
                }

                public double value(float x, float y, float z, InterpolationFunction3D interp) {
                    return delegate.value(x, y, z, interp);
                }

                public double worldValue(float realx, float realy, float realz, InterpolationFunction3D interp) {
                    return delegate.worldValue(realx, realy, realz, interp);
                }

                public double value(int x, int y, int z) {
                    return delegate.value(x, y, z);
                }

                public ValueIterator iterator() {
                    return delegate.iterator();
                }

                public IImageSpace3D getImageSpace() {
                    return space;
                }

                @Override
                public int indexOf(int i, int j, int k) {
                    return delegate.indexOf(i,j,k);
                }

                @Override
                public Dimension3D<Integer> dim() {
                    return delegate.dim();
                }

                @Override
                public IDataGrid3D subGrid(int x0, int x1, int y0, int y1, int z0, int z1) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }








}
