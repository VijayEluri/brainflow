package sc.brainflow.core

import layer.ImageLayer3D
import brainflow.image.space.IImageSpace3D
import boxwood.binding.Observable

/**
 * Created by IntelliJ IDEA.
 * User: Brad
 * Date: Aug 30, 2010
 * Time: 6:58:46 AM
 * To change this template use File | Settings | File Templates.
 */

trait ImageLayerModel {

  def name: String

  def space: IImageSpace3D

  def layers: Seq[ImageLayer3D]


}


object ImageLayerModel {

  def apply(layers: ImageLayer3D*) : ImageLayerModel = {
    new ImageLayerModelImpl("anonymous", layers.toSeq)
  }

  def apply(name: String, layers: ImageLayer3D*) : ImageLayerModel = {
    new ImageLayerModelImpl(name, layers.toSeq)
  }

  //implicit def toModelState(model: ImageLayerModel) : ImageViewModelState = {
  //  new ImageViewModelState(model)
  //}

  class ImageLayerModelImpl(val name: String, val layers: Seq[ImageLayer3D]) extends ImageLayerModel {
    def space = layers(0).data.getImageSpace

  }
}


object ImageViewModel {

  def apply(layers: ImageLayer3D*) : ImageViewModel = {
    new ImageViewModel(ImageLayerModel(layers:_*))
  }
  //implicit def toModel(mstate: ImageViewModelState) = mstate.model

}

class ImageViewModel(val layerModel: ImageLayerModel) {

  val visibility: Seq[Observable[Boolean]] = (0 until layerModel.layers.size).map(x => Observable[Boolean](true))

  val selectedIndex: Observable[Int] = Observable(0)

  def selectedLayer = layerModel.layers(selectedIndex.value)

  def layers = layerModel.layers

  def space = layerModel.space

  def isVisible(layer: ImageLayer3D) = {
    require(layerModel.layers.contains(layer))
    visibility(layerModel.layers.indexOf(layer)).value
  }

  def isVisible(index: Int) = visibility(index).value

  def isSelected(layer: ImageLayer3D) = {
    require(layerModel.layers.contains(layer))
    layerModel.layers.indexOf(layer) == selectedIndex.value
  }


}




