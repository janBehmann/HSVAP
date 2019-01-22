package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;

import data.FeatureSelection;
import data.View;
import data.operators.FeatureWeightsView;

public class FeatureWeightsModel extends TransformationModel implements Serializable, ModelInterface{


	/**
	 * 
	 */
	private static final long serialVersionUID = 3489520827326861453L;
	private double[] weights;
	//TODO
	private String[] waveLengths;


	public FeatureWeightsModel() {
		super();
	}


	public FeatureWeightsModel(String name, String datensatz, Date date, double[] weights, String[] wavelengths) {
		super(name, datensatz, date);
		this.weights = weights;
		this.waveLengths = wavelengths;
	}

	public FeatureWeightsModel(File file) throws IOException, ParseException, ClassNotFoundException{

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		FeatureWeightsModel model = (FeatureWeightsModel) o.readObject();
		o.close();

		this.waveLengths = model.getWavelengths();
		this.weights = model.getWeights();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());

	}


	public View applyOn(View v, String path) throws SizeException, IOException {	
		//TODO: evtl. Kontrolle ob gleiche Features

		int[] indices = FeatureSelection.getNonSpecialFeatures(v);

		for(int i=0; i<indices.length; i++){
			if(!this.waveLengths[i].trim().equals(v.getFeatureDescriptors()[indices[i]].trim())){
				throw new Error("wrong Wavelengths");
			}
		}

		return new FeatureWeightsView(v, this.weights, path);
	}


	public double[] getWeights() {
		return weights;
	}

	public String[] getWavelengths() {
		return waveLengths;
	}

	public String toolTipText(){
		return DF.format(this.getDate()) + ", weights " +  this.weights  + " applied on " + this.getDatensatz();
	}


	@Override
	public String getMetaInfo(boolean detailed) {
		String meta = this.getMainMeta(); 
		meta+="Applicable to view with number of features: " + this.getWeights().length;

		if (detailed==true){
			meta+="<p>weights:   "+new DoubleMatrix(this.getWeights()).toString();
		}

		return meta;
	}

}
