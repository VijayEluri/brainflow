package brainflow.image;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import brainflow.image.data.IImageData;
import brainflow.image.iterators.ImageIterator;
import brainflow.image.io.IImageDataSource;
import brainflow.math.ArrayUtils;
import brainflow.utils.IRange;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Bradley Buchsbaum
 * @version 1.0
 */

public class Histogram {

    private int numBins;

    private IImageData data;

    private double binSize;

    private DoubleArrayList binList;

    private DoubleArrayList binIntervals;

    private double minValue;

    private double maxValue;

    private IRange ignore;

    private boolean computed;

    public Histogram(IImageData _data, int _numBins) {
        data = _data;
        numBins = _numBins;
    }

    public int getNumBins() {
        return numBins;
    }

    public DoubleArrayList computeBins() {
        if (computed)
            return binList;

        maxValue = data.maxValue();
        minValue = data.minValue();

        int[] bins = new int[numBins];

        binSize = (maxValue - minValue) / numBins;

        ImageIterator iter = data.iterator();
        
        while (iter.hasNext()) {
            double val = iter.next();

            if (ignore != null)
                if (ignore.contains(val))
                    continue;

            int nbin = (int) ((val - minValue) / binSize);
            if (nbin >= bins.length)
                nbin = bins.length - 1;

            bins[nbin]++;
        }

        computed = true;
        binList = new DoubleArrayList(ArrayUtils.castToDoubles(bins));
        computeBinIntervals(binSize);
        binList.trimToSize();
        return binList;
    }

    public void ignoreRange(IRange range) {
        ignore = range;
    }

    public double getBinSize() {
        computeBins();
        return binSize;
    }

    public int whichBin(double value) {
        computeBins();
      
        for (int i=0; i<binIntervals.size()-1; i++) {
            double b1 = binIntervals.get(i);
            double b2 = binIntervals.get(i+1);

            if (value >= b1 && value <= b2) {
                return i;
            }

        }

        return -1;
    }

    public int getCount(int bin) {
        return (int)binList.get(bin);
    }

  
    public DoubleArrayList getBinIntervals() {
         if (!computed)
            computeBins();
        return binIntervals;
    }

    private double[] computeBinIntervals(double binSize) {
        if (binIntervals == null) {
            double[] intervals = new double[numBins + 1];
            intervals[0] = minValue;
            intervals[intervals.length - 1] = maxValue;
            for (int i = 1; i < intervals.length - 1; i++) {
                intervals[i] = intervals[i - 1] + binSize;
            }
            binIntervals = new DoubleArrayList(intervals);
            return intervals;
        }

        binIntervals.trimToSize();
        return binIntervals.elements();
    }

    public int getHighestBin() {
         if (!computed)
            computeBins();
        return binList.indexOf(Descriptive.max(binList));
    }

    public double binMean() {
        if (!computed)
            computeBins();
        return Descriptive.mean(binList);
    }

    public double binStandardDeviation() {
        if (!computed)
            computeBins();
        return Math.sqrt(Descriptive.sampleVariance(binList, binMean()));
    }

    public double binMedian() {
        if (!computed)
            computeBins();
        DoubleArrayList sortedBins = binList.copy();
        sortedBins.sort();
        return Descriptive.median(sortedBins);
    }

    public double intervalMedian() {
        if (!computed)
            computeBins();
        return Descriptive.median(binIntervals);
    }

    public double getMinValue() {
        return data.minValue();
    }

    public double getMaxValue() {
        return data.maxValue();
    }

    public static void main(String[] args) {
        IImageDataSource dataSource = null; //TestUtils.quickDataSource("resources/data/global_mean+orig.HEAD");
        Histogram histo = new Histogram(dataSource.getData(),256);
        histo.computeBins();
        
    }


}