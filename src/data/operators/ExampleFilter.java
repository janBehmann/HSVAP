package data.operators;

import org.json.JSONArray;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.Utilities;
import data.View;

public class ExampleFilter extends AbstractViewImplementation {
	
	int[] filter;
	View v;
	
	public ExampleFilter(View v, int[] filter) {
		this.v = v;
		this.filter = filter;
		
		setViewName(v.getViewName() + "_filtered");
		setViewDescription("Subset of examples of " + v.getViewDescription());
		setThumbnail(v.getThumbnail());
		getParentViews().add(v);
		
		featureDescriptors = v.getFeatureDescriptors().clone();
		featureRoles = v.getFeatureRoles().clone();
		getLabelMap().putAll(v.getLabelMap());
	}
	
	public ExampleFilter() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("parent")) {
			v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field parent");
		}
		if (o.has("filter")) {
			JSONArray filterArray = o.getJSONArray("filter");
			filter = new int[filterArray.length()];
			for (int i=0; i<filterArray.length(); ++i) {
				filter[i] = filterArray.getInt(i);
			}
		} else {
			throw new IllegalArgumentException("JSON file has no field filter");
		}

	}

	public View getParent() { return v; }
	public int[] getFilter() { return filter; }

	@Override
	public int getNumberOfColumns() {
		return v.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		if (filter == null) {
			return v.getNumberOfExamples();
		} else {
			return filter.length;
		}
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
		if (filter == null) {
			return v.get(i,j);
		} else {
			return v.get(filter[i], j);
		}
	}

}
