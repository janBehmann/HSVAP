package data.operators;

import data.FeatureSelection;
import data.View;

public class Features extends FeatureFilter {
	
	public Features(View v) {	
		super(v, FeatureSelection.getNonSpecialFeatures(v));
	}
	
	public Features() {}
	
}
