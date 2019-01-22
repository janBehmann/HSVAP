package data.inmemory;


import org.jblas.DoubleMatrix;

import data.AbstractViewImplementation;
import data.Utilities;

public class DoubleMatrixView extends AbstractViewImplementation {
	
	DoubleMatrix data;
	
	public DoubleMatrixView(DoubleMatrix m, String name) {
		data = m;
		
		setFeatureDescriptors(Utilities.createGenericFeatureNames(m.columns));
		setFeatureRoles(Utilities.createFeatureRoleArray(m.columns));
		setViewName(name);
		setViewDescription("View on DoubleMatrix");
	}

	@Override
	public int getNumberOfColumns() {
		return data.columns;
	}

	@Override
	public int getNumberOfExamples() {
		return data.rows;
	}

	@Override
	public double get(int i, int j) {
		return data.get(i, j);
	}

}
