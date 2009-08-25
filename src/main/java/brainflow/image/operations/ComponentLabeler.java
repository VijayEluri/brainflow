package brainflow.image.operations;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import brainflow.image.iterators.ValueIterator;
import brainflow.image.space.Axis;
import brainflow.image.data.*;
import brainflow.math.Index3D;
import brainflow.utils.DataType;
import cern.colt.list.IntArrayList;


public class ComponentLabeler {

	/**Mask for the provided brain data. Used to indicate which voxels
	 * belong to the background and which ones belong to the foreground */
	private IMaskedData3D imageMask;
	/**Datagrid storing the labelled values */
	private IImageData3D labelledVolume;
	/**Writer used for setting values in the volume */
	private ImageBuffer3D labelledVolumeImageWriter;

	/**The size of the sub-volume used in the iterations */
	private int subVolumeSizeRadius;

	/**Linked list containing the newly labelled voxels */
	private LinkedList <Index3D> newlyLocatedVoxelsBuffer;

	/**Current label value */
	private int currentLabel;
	/**Starting label value */
	private int startingLabel;
	/**Number of different labels assigned */
    private static int diffLabels;

	/**Buttom dimensions of where the image should be labelled */
	private int bX, bY, bZ;

	/**Top dimensions of where the image should be labelled */
	private int tX, tY, tZ;

	/**Dimensions of the image */
	private int x, y, z;

	/**List storing the indeces for a specified cluster */
	private IntArrayList cellCluster;
	/**The label of a current cluster being retrieved from a
	 * labelled volume */
	private int clusterLabel;

	public boolean labelled = false;

	/**--------Class constructor------------------ */
	@SuppressWarnings("static-access")
	public ComponentLabeler(IMaskedData3D imageMask,
		ImageBuffer3D labelledVolumeImageWriter, IImageData3D labelledVolume,
			int bX, int bY, int bZ, int tX, int tY, int tZ,
			int subSize, int startingLabel) {

		this.imageMask = imageMask;

		//Set the starting label volume
		this.currentLabel = startingLabel;
		this.startingLabel = startingLabel;

        //todo static variable?
		this.diffLabels = 1;

		//Set the size of the sub-volume
		this.subVolumeSizeRadius = (subSize-1)/2;

		//Initialize the unlabelled volume
		this.labelledVolumeImageWriter = labelledVolumeImageWriter;
		this.labelledVolume = labelledVolume;

		newlyLocatedVoxelsBuffer = new LinkedList<Index3D>();

		/**Set the dimension values of where the algorithm is to iterate
		 * through */
		this.bX = bX; this.bY = bY; this.bZ = bZ;
		this.tX = tX; this.tY = tY; this.tZ = tZ;

		/**Set the dimensions for the image */
		this.x = labelledVolume.getDimension(Axis.X_AXIS);
		this.y = labelledVolume.getDimension(Axis.Y_AXIS);
		this.z = labelledVolume.getDimension(Axis.Z_AXIS);
	}

    /**Given an index, label the cluster containing that index with the
	 * given label */
	public void labelCluster(int clusterLabel, int index){

		newlyLocatedVoxelsBuffer.push(imageMask.indexToGrid(index));

		/**Use iteration and recursion to get all the cells
		 * within the cluster */
		while(newlyLocatedVoxelsBuffer.size() > 0){
			Index3D center = newlyLocatedVoxelsBuffer.pop();
			labelClusterRecursive(clusterLabel, center, center);
		}
	}

	/**Run the labelling components algorithm
	 * Param: totAlg - total number of algorithms labelling the image
	 *        algNum - the index of this current algorithm */
	public void label(){

		/**Iterate through all voxels in the specified volume - This should be done
		 * once for making sure all voxels become labelled */
		for(int iX = bX; iX < tX; iX++){
			for(int iY = bY; iY < tY; iY++){
				for(int iZ = bZ; iZ < tZ; iZ++){

					Index3D tempIndex3D = new Index3D(iX, iY, iZ);

					/**If an unlabelled voxel is found add it onto the stack */
					if(labelledVolume.value(iX, iY, iZ) < startingLabel
							&& imageMask.isTrue(iX, iY, iZ)) {
						newlyLocatedVoxelsBuffer.push(tempIndex3D);

						/**Continue to recurse through sub-volumes for label voxels
						 * until the queue is empty */
						while(!newlyLocatedVoxelsBuffer.isEmpty()){
							Index3D center = newlyLocatedVoxelsBuffer.pop();
							labelRecursive(center, center);
						}

						//Increment the label for the next unlabelled component
						currentLabel = currentLabel + 10;
						diffLabels++;
					}
				}
			}
		}
		labelled = true;
	}

	/**Merge labelled sub-volumes along specified by the
	 * parameter dimensions */
	public void merge(int lX, int lY, int lZ, int rX, int rY, int rZ){
		for(int i = lX; i < rX; i++){
			for(int j = lY; j < rY; j++){
				for(int k = lZ; k < rZ; k++){
					int label1 = (int) labelledVolume.value(i, j, k);
					for(int t1 = i-1; t1 <= i+1 && t1 >= 0 && t1 < x; t1++){
						for(int t2 = j-1; t2 <= j+1 && t2 >= 0 && t2 < y; t2++){
							for(int t3 = k-1; t3 <= k+1 && t3 >= 0 && t3 < z; t3++){
								if(label1 > 0 && (int) labelledVolume.value(t1, t2, t3) != label1){
									labelCluster(label1, t1 + (t2*x) + (t3*x*y));
								}
							}
						}
					}
				}
			}
		}
	}

	/**Given an index, return the corresponding cell cluster */
 	public IntArrayList getNeighbourCluster(int index){

		/**List for storing the indexes of the cells in the cluster containing
		 * the given index */
		cellCluster = new IntArrayList();

		/**The label of the cluster */
		clusterLabel = (int)labelledVolumeImageWriter.value(index);

		/**Clear the linked list for the use of this algorithm */
		newlyLocatedVoxelsBuffer.clear();
		newlyLocatedVoxelsBuffer.push(imageMask.indexToGrid(index));

		/**Use iteration and recursion to get all the cells
		 * within the cluster */
		while(newlyLocatedVoxelsBuffer.size() > 0){
			Index3D center = newlyLocatedVoxelsBuffer.pop();
			getNeighbouringCells(center, center);
		}
		return cellCluster;
	}

	/**Method for returning the labelled volume */
	public IImageData3D getLabelledComponents() {
		return labelledVolume;
	}

	/**Returns the volume containing the size of each
	 * cluster */
	public IImageData3D getSizeVolume(ImageBuffer3D cluster_size_writer) {
		IImageData3D clusterSize = cluster_size_writer.asImageData();
		ValueIterator iter = clusterSize.iterator();

		int label;
		Map<Integer,Integer> sizes = getClusterSizes();

		do {
			label = (int)labelledVolume.value(iter.index());
			if(label > 0) {
				cluster_size_writer.setValue(iter.index(),
						sizes.get(label));
			}
			iter.next();
		} while(iter.hasNext());

		return clusterSize;
	}

	/**Method used for labelling a voxel */
	private void label(Index3D index) {
		labelledVolumeImageWriter.
			setValue(index.i1(), index.i2(), index.i3(), currentLabel);
	}

	/**Method used for getting the label for a voxel */
	private int getLabel(Index3D index) {
		return (int)labelledVolumeImageWriter.
			value(index.i1(), index.i2(), index.i3());
	}

	/**Method for checking if a voxel is in the boundary of a sub-volume */
	private boolean checkSubBoundary(Index3D idx, Index3D center) {
		if(idx.i1() - center.i1() <= subVolumeSizeRadius &&
				idx.i2() - center.i2() <= subVolumeSizeRadius &&
				idx.i3() - center.i3() <= subVolumeSizeRadius &&
				center.i1() - idx.i1() <= subVolumeSizeRadius &&
				center.i2() - idx.i2() <= subVolumeSizeRadius &&
				center.i3() - idx.i3() <= subVolumeSizeRadius
		) {
			return true;
		} else {
			return false;
		}
	}

	/**Method that returns an array containing the size of each
	 * labelled cluster */
	public Map<Integer, Integer> getClusterSizes() {
		ValueIterator imageIterator = labelledVolume.iterator();

		Map<Integer, Integer> sizes = new HashMap<Integer, Integer>();

		do {
			int temp = (int)imageIterator.next();
			try {
				if(sizes.get(temp) != null) {
					int crt = sizes.get(temp) + 1;
					sizes.put(temp, crt);
				} else {
					sizes.put(temp, 1);
				}
			} catch (NullPointerException e) {

			}

		} while(imageIterator.hasNext());

		return sizes;
	}

	/**Helper method used to label the components in a sub-volume */
	private void labelRecursive(Index3D current, Index3D center) {

		/**Add a voxel to the linked list if it belongs to the
		 * foreground and is unlabelled */
		if(getLabel(current) < startingLabel
				&& imageMask.isTrue(current.i1(),current.i2(),current.i3())) {

			if(!current.equals(center)) {
				newlyLocatedVoxelsBuffer.push(current);
			}
			/**Spread recursively from the voxel if it exists within the
			 * boundary of the center voxel and the image itself */
			if(checkSubBoundary(current, center)) {

				label(current);

				/**Check for all 26-neighbour voxels reachable from the
				 * current voxel */
				for(int i = current.i1()-1; i < tX && i >= bX
				&& i <= current.i1() + 1; i++) {
					for(int j = current.i2()-1; j < tY
					&& j >= bY && j <= current.i2()+1; j++) {
						for(int k = current.i3()-1; k < tZ
						&& k >=bZ && k<=current.i3()+1; k++) {
							labelRecursive(new Index3D(i, j, k), center);
			} 	} 	} 	}
		}
	}

	/**Helper method for searching the corresponding cells that are
	 * part of the given cluster */
	private void getNeighbouringCells(Index3D current, Index3D center) {

		int integerIndexForm = current.i1() + current.i2()*x + current.i3()*x*y;

		/**Check if the voxel is part of the cluster */
		if((getLabel(current) == clusterLabel) &&
				!cellCluster.contains(integerIndexForm)) {

			if(!current.equals(center)) {
				newlyLocatedVoxelsBuffer.push(current);
			}

			/**Spread recursively from the voxel if it exists within the
			 * boundary of the center voxel */
			if(checkSubBoundary(current, center)) {
				cellCluster.add(integerIndexForm);

				/**Check for all 26-neighbour voxels reachable from the
				 * current voxel */
				for(int i = current.i1()-1; i < x
				&& i >= 0 && i <= current.i1() + 1; i++) {
					for(int j = current.i2()-1; j < y
					&& j >= 0 && j <= current.i2()+1; j++) {
						for(int k = current.i3()-1; k < z
						&& k >= 0 && k<=current.i3()+1; k++) {
							getNeighbouringCells(new Index3D(i, j, k), center);
						}
					}
				}
			}
		}
	}

	/**Helper function for labelling a cluster specified by an index */
	private void labelClusterRecursive(int clusterLabel, Index3D current,
			Index3D center) {

		/**Check if the voxel has already been labelled with the same value
		 * as the corresponding voxels on the other half of the volume */
		if(getLabel(current) != clusterLabel &&
				imageMask.isTrue(current.i1(),current.i2(),current.i3())) {

			if(!current.equals(center)) {
				newlyLocatedVoxelsBuffer.push(current);
			}

			/**Spread recursively from the voxel if it exists within the
			 * boundary of the center voxel */
			if(checkSubBoundary(current, center)) {
				labelledVolumeImageWriter.
					setValue(current.i1(), current.i2(), current.i3(), clusterLabel);

				/**Check for all 26-neighbour voxels reachable from the
				 * current voxel */
				for(int i = current.i1()-1; i < x
				&& i >= 0 && i <= current.i1() + 1; i++) {
					for(int j = current.i2()-1; j < y
					&& j >= 0 && j <= current.i2()+1; j++) {
						for(int k = current.i3()-1; k < z
						&& k >= 0 && k<=current.i3()+1; k++) {
							labelClusterRecursive(clusterLabel,
									new Index3D(i, j, k), center);
						}
					}
				}
			}
		}
	}
}