package data.inmemory;

import java.util.ArrayList;

import data.AbstractViewImplementation;
import data.Utilities;

public class ArrayListView extends AbstractViewImplementation {
	
	ArrayList<double[]> data;
	
	public ArrayListView(String[] features, ArrayList<double[]> data, String name) {
		if (data.isEmpty()) {
			throw new IllegalArgumentException("ArrayList is empty");
		}
		for (int i=0; i<data.size(); ++i) {
			if (data.get(i).length != data.get(0).length) {
				throw new IllegalArgumentException("Different number of features in different examples.");
			}
		}
		this.data = data;
		
		// set meta data
		if (features != null) {
			featureDescriptors = features;
		} else {
			featureDescriptors = Utilities.createGenericFeatureNames(data.get(0).length);
		}
		
		featureRoles = Utilities.createFeatureRoleArray(data.get(0).length);
		setViewName(name);
		setViewDescription("View on ArrayList of double[]");
	}
	
	public ArrayListView(ArrayList<double[]> data, String name) {
		this(null, data, name);
	}

	@Override
	public int getNumberOfColumns() {
		if (!data.isEmpty()) {
			return data.get(0).length;
		} else {
			return 0;
		}
	}

	@Override
	public int getNumberOfExamples() {
		return data.size();
	}

	@Override
	public double get(int i, int j) {
		return data.get(i)[j];
	}

	@Override
	public String getViewName() {
		return null;
	}

}
