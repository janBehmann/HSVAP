package data.operators;

import java.io.File;
import java.io.IOException;

import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureSelection;
import data.Utilities;
import data.View;

import data.fileformats.MatlabFormat;
import data.inmemory.DoubleMatrixView;

public class FeatureWeightsView extends AbstractViewImplementation {

	private View view;
	private View weights;

	public FeatureWeightsView(View v, double[] weights, String path) throws SizeException, IOException{
		super();
		if (FeatureSelection.getNonSpecialFeatures(v).length!= weights.length){
			throw new SizeException("Number of Features doesn't match.");
		}
		
		this.view = new MergeFeaturesView(new SpecialFeatures(v),new FeatureFilter(v,FeatureSelection.getNonSpecialFeatures(v)));
		
		DoubleMatrix weightsMatrix = new DoubleMatrix(2,weights.length);
		weightsMatrix.putRow(0, new DoubleMatrix(weights));
		weightsMatrix.put(1, 0, FeatureSelection.getSpecialFeatures(v).length);
		this.weights = new DoubleMatrixView(weightsMatrix, v.getViewName()+"_weights.json").saveMaterialized(new File(path,v.getViewName()+"_weights.json"), new File(path,v.getViewName()+"_weights.mat"), new MatlabFormat());
		
		setViewName(v.getViewName() + "_WeightedFeatures");
		setViewDescription("Weighted Features of " + v.getViewName());
		setThumbnail(v.getThumbnail());
		for (View c: v.getParentViews()){
			if(c!=null)
				parentViews.add(c);
		}
		this.featureDescriptors = this.view.getFeatureDescriptors().clone();
		this.featureRoles = this.view.getFeatureRoles().clone();
		getLabelMap().putAll(v.getLabelMap());
	
		
	}

	@Override
	public int getNumberOfColumns() {
		return view.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return view.getNumberOfExamples();
	}

	@Override
	public double getMinValue() {
		return view.getMinValue();
	}

	@Override
	public double getMaxValue() {
		return view.getMaxValue();
	}
	
	@Override
	public int getXDimension() {
		return view.getXDimension();
	}
	@Override
	public int getYDimension() {
		return view.getYDimension();
	}
	
	@Override
	public double get(int i, int j) {
		int numSpFea = (int) this.weights.get(1, 0);
		if(j < numSpFea){
			return view.get(i, j);
		} else{
			return view.get(i, j) * weights.get(0,j-numSpFea);
		}
	}


	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		view = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		weights = Utilities.createViewFromJSON(o.getJSONObject("weights"));
	}

	public View getParent() { return view; }
	public View getWeights() { return weights; }
	
}
