package data.inmemory;


import data.AbstractViewImplementation;
import data.Utilities;

public class DoubleArrayView extends AbstractViewImplementation {

	private double[][] data;

	public DoubleArrayView(double[][] data, String name) {		
		if (data.length == 0) {
			throw new IllegalArgumentException("ArrayList is empty");
		}
		for (int i=0; i<data.length; ++i) {
			if (data[i].length != data[0].length) {
				throw new IllegalArgumentException("Different number of features in different examples.");
			}
		}
		this.data = data;
		
		setFeatureDescriptors(Utilities.createGenericFeatureNames(data[0].length));
		setFeatureRoles(Utilities.createFeatureRoleArray(data[0].length));
		setViewName(name);
		setViewDescription("View on double[][]");
	}

	@Override
	public int getNumberOfColumns() {
		return data[0].length;
	}

	@Override
	public int getNumberOfExamples() {
		return data.length;
	}

	@Override
	public double get(int i, int j) {
		return data[i][j];
	}

	@Override
	public double[] materializeExample(int i) {
		return data[i].clone();
	}

	@Override
	public double[][] materialize() {
		return data.clone();
	}



}
