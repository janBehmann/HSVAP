package filter;

import data.*;
import data.inmemory.*;

public class BoxFilter {

	private int boxSize;
	private View view;
	private String name;

	public BoxFilter(View view, int boxSize, String name) {
		// Rows and columns of the view must be extracted, same for the features
		if (FeatureSelection.getXCoordinate(view) != -1
				&& FeatureSelection.getYCoordinate(view) != -1) {
			this.view = view;
			this.boxSize = boxSize;
			this.name = name;
		}
	}

	/**
	 * Filters all features of the set view with the set boxsize via
	 * boxfiltering
	 * 
	 * @return filtered view
	 */
	public DoubleMatrixView filter(String type) {
		Converter converter = new Converter();
		double[][][] data = converter.getAsArray(view, type);
		double[][][] filtered = new double[data.length][data[1].length][data[1][1].length];
		for (int i = 0; i < filtered[1][1].length; i++) { // All layers
															// (different
															// wavelengths, are
															// filtered
															// iteratively)
			filtered = replaceLayer(filtered, executeFilter(getLayer(data, i)), i);
		}
		// return converter.getFeatureArrayAsView(filtered, view, name);
		return converter.replaceInView(filtered, view, name, type);
	}

	// public double[][][] testFilter( double[][][] data ) {
	// double[][][] filtered = new
	// double[data.length][data[1].length][data[1][1].length];
	// for ( int i = 0; i < filtered[1][1].length; i++ ) {
	// filtered = replaceLayer(filtered, executeFilter(getLayer(data,i)), i);
	// }
	// return (filtered);
	// }

	/**
	 * Filters a 2d-array with the set boxsize via boxfiltering
	 * 
	 * @param data
	 *            data to be filtered
	 * @return filtered array
	 */
	public double[][] executeFilter(double[][] data) {
		double[][] filtered = new double[data.length][data[1].length];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[1].length; j++) {
				if ( data[i][j] != Double.MIN_VALUE) {
					double sum = 0;
					int counter = 0;
					for (int k = i - (boxSize / 2); k <= i + (boxSize) / 2; k++) {
						for (int l = j - (boxSize / 2); l <= j + (boxSize) / 2; l++) {
							if (k >= 0 && k < data.length && l >= 0 && l < data[1].length && data[k][l] != Double.MIN_VALUE) {
								sum += data[k][l];
								counter++;
							}
						}
					}
					sum /= counter;
					filtered[i][j] = sum;
				} else {
					filtered[i][j] = 0;
				}
			}
		}
		return filtered;
	}

	/**
	 * Replaces one "layer" of the 3d-array by a new layer, one layer is the
	 * 2d-array where the third dimension is constant
	 * 
	 * @param array
	 *            3d-array in which the layer should be replaced
	 * @param data
	 *            new data for this layer
	 * @param layer
	 *            constant of the third dimension (which layer should be
	 *            extracted?)
	 * @return 3d-array with the replaced array
	 */
	public double[][][] replaceLayer(double[][][] array, double[][] data, int layer) {
		if (array.length == data.length && array[1].length == data[1].length) {
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[1].length; j++) {
					array[i][j][layer] = data[i][j];
				}
			}
			return array;
		} else {
			System.out.println("Dimensionsfehler bei replaceLayer");
			return null;
		}
	}

	/**
	 * Returns a one "layer" of the 3d-array, one layer is the 2d-array where
	 * the third dimension is constant
	 * 
	 * @param array
	 *            3d-array from which the layer should be extracted
	 * @param layer
	 *            constant of the third dimension (which layer should be
	 *            extracted?)
	 * @return 2d-array of this layer
	 */
	public double[][] getLayer(double[][][] array, int layer) {
		double[][] layerArray = new double[array.length][array[1].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[1].length; j++) {
				layerArray[i][j] = array[i][j][layer];
			}
		}
		return layerArray;
	}
	
//	private void showMatrix(double[][][] matrix) {
//		for ( int k = 0; k < matrix[0][0].length; k++ ) {
//			for ( int i = 0; i < matrix.length; i++ ) {
//				for ( int j = 0; j < matrix[0].length; j++ ) {
//					System.out.print(matrix[i][j][k] + " ");
//				}
//				System.out.println();
//			}
//			System.out.println();
//		}		
//	}
}
