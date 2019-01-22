package data.operators;
//TODO: Unused at the moment, Marked for deletion

import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.Utilities;
import data.View;

public class Transpose extends AbstractViewImplementation {
	
	private View v;
	
	public Transpose(View v) {
		this.v = v;
		this.setViewName(v.getViewName() + "_transpose");
		this.setViewDescription("Transpose of " + v.getViewDescription());
		this.setThumbnail(v.getThumbnail());
		this.getParentViews().add(v);
		
		this.featureDescriptors = Utilities.createGenericFeatureNames(v.getNumberOfExamples());
		this.featureRoles = Utilities.createFeatureRoleArray(v.getNumberOfExamples());
		//labels are not copied...
	}

	@Override
	public int getNumberOfColumns() {
		return v.getNumberOfExamples();
	}

	@Override
	public int getNumberOfExamples() {
		return v.getNumberOfColumns();
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
	
	public View getParent() {
		return v;
	}

	@Override
	public double get(int i, int j) {
		return v.get(j, i);
	}
	
	public Transpose() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException {
		super.initializeValues(o);
		if (o.has("parent")) {
			v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field parent");
		}
	}

}
