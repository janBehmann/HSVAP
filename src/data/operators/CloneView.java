package data.operators;

import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.Utilities;
import data.View;

public class CloneView extends AbstractViewImplementation {
	
	View v;

	public CloneView(View p2) throws IllegalArgumentException {
		this.v = p2;
		
		setViewName(p2.getViewName() + "_copy");
		setViewDescription("Copy of " + p2.getViewName());
		setThumbnail(p2.getThumbnail());
		parentViews.add(p2);
		
		this.featureDescriptors = p2.getFeatureDescriptors().clone();
		this.featureRoles = p2.getFeatureRoles().clone();
		getLabelMap().putAll(p2.getLabelMap());
	}

	public CloneView() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("parent")) {
			v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field parent");
		}
	}

	public View getParent() { return v; }

	@Override
	public int getNumberOfColumns() {
		return v.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return v.getNumberOfExamples();
	}
	@Override
	public double getMinValue() {
		return v.getMinValue();
	}

	@Override
	public double getMaxValue() {
		return v.getMaxValue();
	}
	
	@Override
	public int getXDimension() {
		return v.getXDimension();
	}
	@Override
	public int getYDimension() {
		return v.getYDimension();
	}
	
	@Override
	public double get(int i, int j) {
		return v.get(i, j);	
	}
}
