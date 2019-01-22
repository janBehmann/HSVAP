package data.operators;

import org.jblas.DoubleMatrix;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.Utilities;
import data.View;
import data.inmemory.DoubleMatrixView;



/**
 * Adds a column of labels as the first column to a view
 * @author schubert, heavy augmentations 2013-09-30 pwelke
 * 
 *
 */
public class AddRGBView extends AbstractViewImplementation {

	View rgb;
	View v;

	public AddRGBView(double[] rgb, View view) throws IllegalArgumentException {

		this.rgb = new DoubleMatrixView(new DoubleMatrix(rgb), "RGB");
		this.v = view;
		
		setViewName(view.getViewName() + "_rgb");
		setViewDescription("Added RGB to " + view.getViewName());
		setThumbnail(view.getThumbnail());
		parentViews.add(view);
		parentViews.add(this.rgb);
	
		featureDescriptors = new String[view.getNumberOfColumns() + 1];
		featureRoles = new FeatureRole[view.getNumberOfColumns() + 1];
		featureDescriptors[0] = "Rgb";
		featureRoles[0] = FeatureRole.RGB;
		for (int i=0; i<view.getNumberOfColumns(); ++i) {
			featureDescriptors[i+1] = view.getFeatureDescriptors()[i];
			featureRoles[i+1] = view.getFeatureRoles()[i];
		}
		getLabelMap().putAll(view.getLabelMap());
	}

	public AddRGBView() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("parent")) {
			v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		} else {
			throw new IllegalArgumentException("JSON file has no field parent");
		}
	}

	public View getRGB() { return rgb; }
	public View getParent() { return v; }


	@Override
	public int getNumberOfColumns() {
		return 1 + v.getNumberOfColumns();
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
		if (j == 0) {
			return rgb.get(i, j);
		} else {
			return v.get(i, j - rgb.getNumberOfColumns());
		}
	}
}

