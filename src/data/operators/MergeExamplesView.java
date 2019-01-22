package data.operators;


import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.Utilities;
import data.View;

public class MergeExamplesView extends AbstractViewImplementation {

	View p1;
	View p2;


	/**
	 * merge examples of views p1 and p2
	 * @param p1
	 * @param p2
	 * @throws IllegalArgumentException
	 */
	public MergeExamplesView(View p1, View p2) throws IllegalArgumentException {
		if(p1.getNumberOfColumns()==p2.getNumberOfColumns()){

			for (int i=0; i<p1.getNumberOfColumns();i++){
				if (!p1.getFeatureRoles()[i].equals(p2.getFeatureRoles()[i])){
					throw new IllegalArgumentException("Feature descriptions do not match");				
				}	
			}
			this.p1 = p1;
			this.p2 = p2;
			this.p1.materializeXYDimension();
			getParentViews().add(p1);
			getParentViews().add(p2);
			
			featureDescriptors = p1.getFeatureDescriptors();
			featureRoles = p1.getFeatureRoles();
			getLabelMap().putAll(p1.getLabelMap());
			getLabelMap().putAll(p2.getLabelMap());
		} else {
			throw new IllegalArgumentException("Different number of features");
		}

	}

	public MergeExamplesView() {}

	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("firstParent")) {
			p1 = Utilities.createViewFromJSON(o.getJSONObject("firstParent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field firstParent");
		}
		if (o.has("secondParent")) {
			p2 = Utilities.createViewFromJSON(o.getJSONObject("secondParent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field secondParent");
		}
	}

	public View getFirstParent() { return p1; }
	public View getSecondParent() { return p2; }


	@Override
	public int getNumberOfColumns() {
		return p1.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return p1.getNumberOfExamples() + p2.getNumberOfExamples();
	}

	@Override
	public double getMinValue() {
		return p1.getMinValue();
	}

	@Override
	public double getMaxValue() {
		return p1.getMaxValue();
	}
	
	@Override
	public int getXDimension() {
		return p1.getXDimension();
	}
	@Override
	public int getYDimension() {
		return p1.getYDimension();
	}

	
	@Override
	public double get(int i, int j) {
		if (i < p1.getNumberOfExamples()) {
			return p1.get(i, j);
		} else {
			return p2.get(i- p1.getNumberOfExamples(), j );
		}
	}




}
