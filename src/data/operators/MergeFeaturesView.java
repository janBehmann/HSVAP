package data.operators;

import java.util.HashMap;

import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;


/**
 * Merge Features of two given Views into a new View.
 * @author pwelke
 *
 */
public class MergeFeaturesView extends AbstractViewImplementation {

	View p1;
	View p2;
	int[] exampleMap;

	public MergeFeaturesView(View p1, View p2) throws IllegalArgumentException {
		if (coordinateCompatible(p1, p2)) {
			p2 = new ExampleFilter(p2, exampleMap);
			if (labelCompatible(p1, p2)) {
				this.p1 = p1;
			}
			this.p1.materializeXYDimension();
			this.p2 = p2;
			parentViews.add(p1);
			parentViews.add(p2);
			setViewDescription("Merged features of " + p1.getViewName() + " and " + p2.getViewName());
			setFeatureDescriptors(p1.getFeatureDescriptors(), p2.getFeatureDescriptors());			
			setFeatureRoles(p1.getFeatureRoles(), p2.getFeatureRoles());
			getLabelMap().putAll(p1.getLabelMap());
			getLabelMap().putAll(p2.getLabelMap());
		} else { 
			throw new IllegalArgumentException("Number of examples in parents does not match");
		}
	}
	
	public MergeFeaturesView( View p1, View p2, String name) throws IllegalArgumentException {
		this(p1, p2);
		this.setViewName(name);
		
	}

	public MergeFeaturesView() {}

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
		return p1.getNumberOfColumns() + p2.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return p1.getNumberOfExamples();
	}

	@Override
	public double getMinValue() {
		return p1.getMinValue();
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
	public double getMaxValue() {
		return p1.getMaxValue();
	}
	
	@Override
	public double get(int i, int j) {
		if (j < p1.getNumberOfColumns()) {
			return p1.get(i, j);
		} else {
			return p2.get(i, j - p1.getNumberOfColumns());
		}
	}

	private void setFeatureDescriptors(String[] f1, String[] f2) {
		// TODO remove quadratic efford for comparison
		boolean flag = false;
		for (int i=0; i<f1.length; ++i) {
			for (int j=0; j<f2.length; ++j) {
				if (f1[i].equals(f2[j])) {
					flag = true;
					break;
				}
			}
			if (flag) {
				break;
			}
		}
		featureDescriptors = new String[f1.length + f2.length];
		if (flag) {
			for (int i=0; i<f1.length; ++i) {
				featureDescriptors[i] = f1[i];
			}
			for (int i=0; i<f2.length; ++i) {
				featureDescriptors[i+f1.length] =f2[i];
			}
		} else {
			for (int i=0; i<f1.length; ++i) {
				featureDescriptors[i] = f1[i];
			}
			for (int i=0; i<f2.length; ++i) {
				featureDescriptors[i+f1.length] = f2[i];
			}
		}
	}

	private void setFeatureRoles(FeatureRole[] f1, FeatureRole[] f2) {
		featureRoles = new FeatureRole[f1.length + f2.length];

		for (int i=0; i<f1.length; ++i) {
			featureRoles[i] = f1[i];
		}
		for (int i=0; i<f2.length; ++i) {
			featureRoles[i+f1.length] = f2[i];
		}

	}


	private boolean coordinateCompatible(View p1, View p2) throws IllegalArgumentException {
		//		if (p1.getNumberOfExamples() == p2.getNumberOfExamples()) {
		//			int x1 = FeatureSelection.getXCoordinate(p1);
		//			int x2 = FeatureSelection.getXCoordinate(p2);
		//			int y1 = FeatureSelection.getYCoordinate(p1);
		//			int y2 = FeatureSelection.getYCoordinate(p2);
		//
		//			// if both have x coords
		//			if ((x1 != -1) && (x2 != -1) && (y1 != -1) && (y2 != -1)) {
		//				exampleMap = mapCoordinates(p1, p2, x1, y1, x2, y2);
		//				return true;
		//
		//			} else {
		//				if ((x1 != -1) || (x2 != -1) || (y1 != -1) || (y2 != -1)) {
		//					// cannot map coordinates, if there is only partial information
		//					return false;
		//				} else {
		//					return true;
		//				}
		//			}
		//		}	
		//		return false;
		return (p1.getNumberOfExamples() == p2.getNumberOfExamples());
	}

	public static int[] mapCoordinates(View p1, View p2, int x1, int y1, int x2, int y2) throws IllegalArgumentException {
		// coords are ints
		HashMap<Long, Integer> map = new HashMap<Long, Integer>(p1.getNumberOfExamples());
		for (int i=0; i<p2.getNumberOfExamples(); ++i) {
			long key = (long)p2.get(i, x2);
			key <<= 32;
			key += (long)p2.get(i, y2);
			map.put(key, i);
		}
		// can find the index of a pixel in p2 by building key long from
		// pixel in p1 and lookup in map.
		int[] indices = new int[p1.getNumberOfExamples()];
		boolean isNotIdentityMap = false;
		for (int i=0; i<p1.getNumberOfExamples(); ++i) {
			long key = (long)p1.get(i, x1);
			key <<= 32;
			key += (long)p1.get(i, y1);
			Integer value = map.get(key);
			if (value == null) {
				throw new IllegalArgumentException("Cannot map features, pixel (" + p1.get(i, x1) + "," + p1.get(i,y1) + ") has no corresponding pixel.");
			}
			indices[i] = value;		
			if (indices[i] != i) {
				isNotIdentityMap = true;
			}
		}
		if (isNotIdentityMap) {
			return indices; 
		} else {
			return null;
		}
	}

	public boolean labelCompatible(View p1, View p2) {

		if (p1.getNumberOfExamples() != p2.getNumberOfExamples()) {
			return false;
		} else {
			int l1 = FeatureSelection.getLabel(p1);
			int l2 = FeatureSelection.getLabel(p2);
			if ((l1 != -1) && (l2 != -1)) {
				for (int i=0; i<p1.getNumberOfExamples(); ++i) {
					if (p1.get(i, l1) != p2.get(i, l2)) {
						return false;
					}
				}
				return true;
			} else {
				return true;
			}
		}


	}

}

