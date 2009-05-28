package brainflow.core;

import brainflow.image.io.BrainIO;
import brainflow.core.layer.ImageLayer3D;
import brainflow.core.layer.LayerList;
import brainflow.core.BrainFlowException;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Oct 14, 2008
 * Time: 2:22:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class BF {


   private static final String dataDir = "data/";


    public static URL getDataURL(String fileName) {
        if (fileName.startsWith(dataDir)) {
            return ClassLoader.getSystemClassLoader().getResource(fileName);
        } else {
            return ClassLoader.getSystemClassLoader().getResource(dataDir + fileName);
        }
    }


    public static ImageViewModel createModel(URL ... urls) throws BrainFlowException {

        //todo add unique ID so that name is not always "untitled"?
        LayerList<ImageLayer3D> layers = new LayerList<ImageLayer3D>();

        for (URL url : urls) {
            if (!BrainIO.isSupportedFile(url.toString())) {
                throw new IllegalArgumentException("url " + url + " is not a recognized image file.");
            }

            layers.add(new ImageLayer3D(BrainIO.loadVolume(url)));


        }

        return new ImageViewModel("untitled", layers);

    }

    public static IImageDisplayModel createModel(String ... filenames) throws BrainFlowException {

        //todo add unique ID so that name is not always "untitled"?
        IImageDisplayModel model = new ImageDisplayModel("untitled");

        for (String fn : filenames) {
            if (!BrainIO.isSupportedFile(fn)) {
                throw new IllegalArgumentException("file " + fn + " is not a recognized image file.");
            }

            ImageLayer3D layer = new ImageLayer3D(BrainIO.loadVolume(fn));
            model.addLayer(layer);

        }

        return model;

    }

    public static IImageDisplayModel createModel(String modelName, String ... filenames) throws BrainFlowException {

        IImageDisplayModel model = new ImageDisplayModel(modelName);
        
        for (String fn : filenames) {
            if (!BrainIO.isSupportedFile(fn)) {
                throw new BrainFlowException("file " + fn + " is not a recognized image file.");
            }

            ImageLayer3D layer = new ImageLayer3D(BrainIO.loadVolume(fn));
            model.addLayer(layer);

        }

        return model;
        
    }

}
