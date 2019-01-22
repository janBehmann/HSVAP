package data.operators;

import data.FeatureSelection;
import data.View;

public class SpecialFeatures extends FeatureFilter {

	public SpecialFeatures(View v) {
		super(v, FeatureSelection.getSpecialFeatures(v));
	}

}
