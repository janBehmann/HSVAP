package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;

import weka.core.Instances;
import data.FeatureSelection;
import data.View;
import data.operators.AddLabelsView;

public class KMeansModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7511380944901384782L;
	private Instances centroids;
	private String clusterName;



	public KMeansModel(String name, String datensatz, Date date,Instances centroids,String clusterName) {
		super(name, datensatz, date);
		this.centroids = centroids;
		this.clusterName = clusterName;
	}



	public KMeansModel(File file) throws IOException, ClassNotFoundException {

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		KMeansModel model = (KMeansModel) o.readObject();
		o.close();

		this.centroids = model.getCentroids();
		this.clusterName= model.getClusterName();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());
	}



	public Instances getCentroids() {
		return centroids;
	}
	public String getClusterName() {
		return clusterName;
	}

	public View applyOn(View v, String path) throws IllegalArgumentException, IOException{
		double [] label_erg = new double[v.getNumberOfExamples()];
		int [] idxFeature = FeatureSelection.getNonSpecialFeatures(v);
		for(int i=0;i<v.getNumberOfExamples();i++){
			double minDistance = Double.MAX_VALUE;
			for(int j=0;j<centroids.numInstances();j++){
				double [] clusterCentroids = centroids.instance(j).toDoubleArray();
				double distance=0;
				for(int k=0;k<clusterCentroids.length;k++){
					distance += Math.pow(v.get(i, idxFeature[k])-clusterCentroids[k],2) ;
				}
				distance = Math.sqrt(distance);
				if(distance<minDistance){
					minDistance= distance;
					label_erg[i]=(double) j;
				}
			}
		}

		return new AddLabelsView(label_erg,v, path);

	}



	@Override
	public String getMetaInfo(boolean detailed) {
		String meta = this.getMainMeta();

		meta+="cluster name:   "+ this.getClusterName();
		meta+="centroids:   "+ this.getCentroids().toString();

		if (detailed==true){

		}
		return meta;
	}





}
