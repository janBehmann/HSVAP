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

/**
 * 
 * @author s7tischu
 *
 *
 *
 */
public class NormView extends AbstractViewImplementation {

	private View v;
	private View meanStd;
    private double minV;
    private double maxV;


	public NormView(View v, DoubleMatrix mean, DoubleMatrix std, String path) throws SizeException, IllegalArgumentException, IOException{
		super();
		
		if(mean.columns!=std.columns || mean.columns != FeatureSelection.getNonSpecialFeatures(v).length){
			throw new IllegalArgumentException();
		}
		
		this.v = new MergeFeaturesView(new SpecialFeatures(v),new FeatureFilter(v,FeatureSelection.getNonSpecialFeatures(v)));

		
		int k = 4;
		if(mean.columns>4){
			k = mean.columns;
		}

		DoubleMatrix matrix = new DoubleMatrix(3, k);
		for(int i=0;i<mean.columns;i++){
			matrix.put(0,i, mean.get(0, i));
			matrix.put(1,i, std.get(0, i));
		}
		matrix.put(2, 0, FeatureSelection.getSpecialFeatures(v).length);
		this.meanStd = new DoubleMatrixView(matrix, v.getViewName()+"_meanstd").saveMaterialized(new File(path,v.getViewName()+"_meanstd.json"), new File(path,v.getViewName()+"_meanstd.mat"), new MatlabFormat());
	
		
		setViewName(v.getViewName() + "_normed");
		setViewDescription("NormedView of " + v.getViewName());
		setThumbnail(v.getThumbnail());
		parentViews.add(v);
		for (View c: v.getParentViews()){
			if(c!=null)
				parentViews.add(c);
		}
		this.featureDescriptors = this.v.getFeatureDescriptors().clone();
		this.featureRoles = this.v.getFeatureRoles().clone();
		getLabelMap().putAll(v.getLabelMap());
		DoubleMatrix spectrum= new DoubleMatrix(this.materializeFeatures(FeatureSelection.getNonSpecialFeatures(this)));
		maxV=spectrum.max();
		minV=spectrum.min();
	}

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
		return minV;
		//return v.getMinValue();
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
	public double getMaxValue() {
		return maxV;
		//return v.getMaxValue
	}
	
	@Override
	public double get(int i, int j) {

		int numSpFea = (int) meanStd.get(2, 0);
		if( j<numSpFea ){
			return v.get(i, j);
		} else{
			
			return (v.get(i, j) - meanStd.get(0,j-numSpFea)) / meanStd.get(1,j-numSpFea);
		}
	}


	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
		meanStd = Utilities.createViewFromJSON(o.getJSONObject("meanStd"));
	}

	public View getParent() { return v; }
	public View getMeanStd() { return meanStd; }
}
