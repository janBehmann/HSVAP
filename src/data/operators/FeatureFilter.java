package data.operators;

import org.json.JSONArray;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.Utilities;
import data.View;

public class FeatureFilter extends AbstractViewImplementation {
	
	int[] filter;
	View v;
	
	public FeatureFilter(View v, int[] filter) {
		this.filter = filter;
		this.v = v;
		
		setViewName("FilteredView of " + v.getViewName());
		setViewDescription("Subset of columns of " + v.getViewDescription());
		setThumbnail(v.getThumbnail());
		getParentViews().add(v);
		
		if (filter != null) {
			featureDescriptors = new String[filter.length];
			featureRoles = new FeatureRole[filter.length];
			for (int i=0; i<filter.length; ++i) {
				featureDescriptors[i] = v.getFeatureDescriptors()[filter[i]];
				featureRoles[i] = v.getFeatureRoles()[filter[i]];
			}
		} else {
			featureDescriptors = v.getFeatureDescriptors().clone();
			featureRoles = v.getFeatureRoles().clone();
		}
		getLabelMap().putAll(v.getLabelMap());	
		
		this.v.materializeXYDimension();
	}
	
	public FeatureFilter() {}
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
		if (filter == null) {
			return v.getNumberOfColumns();
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
	public int getNumberOfExamples() {
		// TODO Auto-generated method stub
		return v.getNumberOfExamples();
	}

	@Override
	public double get(int i, int j) {
		if (filter == null) {
			return v.get(i,j);
		} else {
			return v.get(i, filter[j]);
		}
	}

}
