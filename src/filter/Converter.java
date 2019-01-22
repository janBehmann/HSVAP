package filter;

import org.jblas.DoubleMatrix;

import data.FeatureRole;
import data.FeatureSelection;
import data.View;
import data.inmemory.DoubleMatrixView;

/**
 * This class offers methods to transform views into matrices and array and vice
 * versa.
 * 
 * @author Axel Forsch
 *
 */
public class Converter {
	/**
	 * This method transforms a given view with an image representation into a
	 * 3-dimensional array. The first 2 dimensions are used for the
	 * representation of one single feature at the correct position in the image
	 * and the third dimension includes all features of this type. For example,
	 * a view with 2 different wavelength with a 3x3 image representation would
	 * become a 3x3x2 array, containing the data of the first wavelength in
	 * [posX][posY][1] and the data for the second wavelength in
	 * [posX][posY][2].
	 * 
	 * @param v
	 *            View with an image representation
	 * @return features as a 3d-double-array (see text)
	 */
	public double[][][] getAsArray(View v, String type) {
		if (FeatureSelection.getXCoordinate(v) != -1
				&& FeatureSelection.getYCoordinate(v) != -1) {
			int posX = FeatureSelection.getXCoordinate(v);
			int posY = FeatureSelection.getYCoordinate(v);

			int minX = (int) getMin(v.materializeFeature(posX));
			int maxX = (int) getMax(v.materializeFeature(posX));
			int minY = (int) getMin(v.materializeFeature(posY));
			int maxY = (int) getMax(v.materializeFeature(posY));

			double[][][] viewArray = null;
			
			if (type.equals("feature")) {
				int[] features = FeatureSelection.getNonSpecialFeatures(v);
				int numberOfFeatures = features.length;
				viewArray = new double[maxX - minX + 1][maxY - minY + 1][numberOfFeatures];
				viewArray = setDefault(viewArray,Double.MIN_VALUE);
				
				for (int i = 0; i < v.getNumberOfExamples(); i++) {
					double[] sample = v.materializeExample(i);
					int currX = (int) sample[posX] - minX;
					int currY = (int) sample[posY] - minY;
					for (int j = 0; j < numberOfFeatures; j++) {
						viewArray[currX][currY][j] = sample[features[j]];
					}
				}
			} else if (type.equals("label")) {
				int label = FeatureSelection.getLabel(v);
				viewArray = new double[maxX - minX + 1][maxY - minY + 1][1];
				viewArray = setDefault(viewArray,Double.MIN_VALUE);

				for (int i = 0; i < v.getNumberOfExamples(); i++) {
					double[] sample = v.materializeExample(i);
					int currX = (int) sample[posX] - minX;
					int currY = (int) sample[posY] - minY;
					viewArray[currX][currY][0] = sample[label];
				}
			} else if (type.equals("prob")) {
				int[] probs = FeatureSelection.getProbs(v);
				int numberOfProbs = probs.length;
				viewArray = new double[maxX - minX + 1][maxY - minY + 1][numberOfProbs];
				viewArray = setDefault(viewArray,Double.MIN_VALUE);

				for (int i = 0; i < v.getNumberOfExamples(); i++) {
					double[] sample = v.materializeExample(i);
					int currX = (int) sample[posX] - minX;
					int currY = (int) sample[posY] - minY;
					for (int j = 0; j < numberOfProbs; j++) {
						viewArray[currX][currY][j] = sample[probs[j]];
					}
				}
			}
			return viewArray;
		} else {
			return null;
		}
	}

	/**
	 * Tries to convert a 3xn matrix into a view. To extract the right values
	 * for the coordinates and the original name an original view must be
	 * appended.
	 * 
	 * @param array
	 *            Array to transform
	 * @param original
	 *            Original view, before filtering
	 * @param name
	 *            Name of the new view
	 * @return New view with array elements as values
	 */
	public DoubleMatrixView replaceInView(double[][][] array, View original, String name,
			String type) {

		int posX = FeatureSelection.getXCoordinate(original);
		int posY = FeatureSelection.getYCoordinate(original);

		int minX = (int) getMin(original.materializeFeature(posX));
		int minY = (int) getMin(original.materializeFeature(posY));

		// Extract coords from the original view
		// TODO X muss immer die erste Spalte sein, Y immer die 2.!!!!
		int[] coordColumns = FeatureSelection.getCoordinates(original);
		double coords[][] = original.materializeFeatures(coordColumns);

		// Extract features from the original view
		int[] featureColumns = FeatureSelection.getNonSpecialFeatures(original);
		double features[][] = original.materializeFeatures(featureColumns);

		if (type.equals("feature")) {
			// Extract features from the array and replace the ones from the
			// original view
			features = new double[original.getNumberOfExamples()][FeatureSelection.getNonSpecialFeatures(original).length];
			for (int i = 0; i < features.length; i++) {
				for (int j = 0; j < features[1].length; j++) {
					features[i][j] = array[(int) coords[i][0] - minX][(int) coords[i][1]
								- minY][j];
				}
			}
		}

		DoubleMatrix matrix = DoubleMatrix.concatHorizontally(new DoubleMatrix(coords),
				new DoubleMatrix(features));

		// Extract features from the original view
		int labelAmount = 0;
		try {
			int labelColumns = FeatureSelection.getLabel(original);
			double labels[] = original.materializeFeature(labelColumns);
			labelAmount = 1;

			if (type.equals("label")) {
				// Extract labels from the array and replace the ones from the
				// original view
				labels = new double[array.length * array[1].length];
				for (int i = 0; i < labels.length; i++) {
					labels[i] = Math
							.round(array[(int) coords[i][0] - minX][(int) coords[i][1]
									- minY][0]);
				}
			}

			matrix = DoubleMatrix.concatHorizontally(matrix, new DoubleMatrix(labels));
		} catch (Exception e) {
			// Keine Labels in View
		}

		// Extract probs from the original view
		int probAmount = 0;
		try {
			int[] probColumns = FeatureSelection.getProbs(original);
			double[][] probs = original.materializeFeatures(probColumns);
			probAmount = probs[1].length;

			if (type.equals("prob")) {
				// Extract features from the array and replace the ones from the
				// original view
				probs = new double[array.length * array[1].length][array[1][1].length];
				for (int i = 0; i < probs.length; i++) {
					for (int j = 0; j < probs[1].length; j++) {
						probs[i][j] = array[(int) coords[i][0] - minX][(int) coords[i][1]
								- minY][j];
					}
				}
			}

			matrix = DoubleMatrix.concatHorizontally(matrix, new DoubleMatrix(probs));
		} catch (Exception e) {
			// Keine Probs in View
		}

		DoubleMatrixView filteredView = new DoubleMatrixView(matrix, name + "_filtered");

		// Setzen der FeatureRoles, dabei ist immer folgende Abfolge in den
		// gefilterten Views: 1. Coord, 2. Feature, 3. Label, 4. Prob
		FeatureRole[] featureRoles = new FeatureRole[2 + features[1].length + labelAmount
				+ probAmount]; // Anzahl der Coords + Features + Labels + Probs
		featureRoles[0] = FeatureRole.X;
		featureRoles[1] = FeatureRole.Y;
		for (int i = 2; i < features[1].length + 2; i++) {
			featureRoles[i] = FeatureRole.FEATURE;
		}
		if (labelAmount != 0)
			featureRoles[features[1].length + 2] = FeatureRole.LABEL;
		for (int i = features[1].length + 3; i < featureRoles.length; i++) {
			featureRoles[i] = FeatureRole.PROB;
		}
		filteredView.setFeatureRoles(featureRoles);

		// Setzen der FeatureDescriptors
		String[] featureDescriptors = new String[2 + features[1].length + labelAmount
				+ probAmount];
		featureDescriptors[0] = "x";
		featureDescriptors[1] = "y";
		for (int i = 2; i < features[1].length + 2; i++) {
			featureDescriptors[i] = FeatureSelection
					.getNonSpecialFeatureDescriptorString(original)[i - 2];
		}
		if (labelAmount != 0)
			featureDescriptors[features[1].length + 2] = "label";
		for (int i = features[1].length + 3; i < featureDescriptors.length; i++) {
			featureDescriptors[i] = "prob";
		}
		filteredView.setFeatureDescriptors(featureDescriptors);

		filteredView.setThumbnail(original.getThumbnail());

		return filteredView;
	}

	/**
	 * Method to find the smallest element in an array.
	 * 
	 * @param array
	 *            array of double values
	 * @return minimum value
	 */
	private double getMin(double[] array) {
		double min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	/**
	 * Method to find the biggest element in an array.
	 * 
	 * @param array
	 *            array of double values
	 * @return maximum value
	 */
	private double getMax(double[] array) {
		double min = array[0];
		for (int i = 1; i < array.length; i++) {
			if (array[i] > min) {
				min = array[i];
			}
		}
		return min;
	}
	
	private double[][][] setDefault(double[][][] matrix, double value) {
		double[][][] result = new double[matrix.length][matrix[0].length][matrix[0][0].length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				for (int k = 0; k < result[0][0].length; k++) {
					result[i][j][k] = value;
				}
			}
		}
		return result;
	}
}
