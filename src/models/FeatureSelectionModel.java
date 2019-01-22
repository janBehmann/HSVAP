package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;

import data.FeatureSelection;
import data.View;
import data.operators.FeatureFilter;
import data.operators.FeatureWeightsView;
import data.operators.MergeFeaturesView;
import data.operators.SpecialFeatures;

/**
 * This class is a model for selecting features. The indices define which of the non-special-features will be kept.
 * Additionaly the weight values of the previous featureweightsmodel are retained as well as the waveLength-strings (to detect a Error("wrong Wavelengths") if this view selects different features than it used to).
 *   
 * 
 * @author Till
 *
 */
public class FeatureSelectionModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4714875489636030792L;
	private int [] indices;
	private double [] weights;
	private String[] waveLengths;


	public FeatureSelectionModel(String name, String datensatz, Date date, int[] indices, double[] weights, String[] wavelengths) {
		super(name, datensatz, date);
		this.indices = indices;
		this.weights = weights;
		this.waveLengths = wavelengths;
	}


	public FeatureSelectionModel() {
		super();
	}


	public FeatureSelectionModel(File file) throws IOException, ParseException, ClassNotFoundException{

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		FeatureSelectionModel model = (FeatureSelectionModel) o.readObject();
		o.close();

		this.waveLengths = model.getWaveLengths();
		this.indices = model.getIndices();
		this.weights = model.getWeights();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());

	}

	public View applyOn(View v, String path) throws SizeException, IOException{		

		int[] nSpIndices = FeatureSelection.getNonSpecialFeatures(v);

		for(int i=0; i<indices.length; i++){
			if(!this.waveLengths[i].trim().equals(v.getFeatureDescriptors()[nSpIndices[this.indices[i]]].trim())){
				throw new Error("wrong Wavelengths");
			}
		}
		
		
		SpecialFeatures sp = new SpecialFeatures(v);
		FeatureFilter nonSp = new FeatureFilter(v, FeatureSelection.getNonSpecialFeatures(v));

		//		int[] indices = FeatureSelection.getNonSpecialFeatures(v);
		//
		//		for(int i=0; i<indices.length; i++){
		//			if(!this.waveLengths[i].trim().equals(v.getFeatureDescriptors()[indices[i]].trim())){
		//				throw new Error("wrong Wavelengths");
		//			}
		//		}

		View filteredView = new FeatureFilter(nonSp,this.indices);
		View finalView = new MergeFeaturesView(sp, filteredView);
		FeatureWeightsView neu = new FeatureWeightsView(finalView, this.weights, path);
		neu.setViewName(v.getViewName()+"_selected Features");
		neu.setViewDescription("selected Features of "+v.getViewName());
		return neu;
	}


	public int [] getIndices() {
		return indices;
	}

	public double [] getWeights() {
		return weights;
	}

	public String[] getWaveLengths() {
		return waveLengths;
	}


	public String toolTipText(){
		return DF.format(this.getDate()) + ", indices " +  this.indices  + " applied on " + this.getDatensatz();
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String getMetaInfo(boolean detailed) {

		String meta = this.getMainMeta();
		meta+="Selected number of features: " + this.getIndices().length;;

		if (detailed==true){
			meta+="<p>weights:   "+new DoubleMatrix(this.getWeights()).toString();
			meta+="<p>wavelengths:   "+new ArrayList(Arrays.asList(this.getWaveLengths())).toString();
		}
		
		return meta;
	}

}
