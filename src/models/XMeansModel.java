package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.jblas.exceptions.SizeException;

import weka.clusterers.XMeans;
import weka.core.Instances;
import data.View;

public class XMeansModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8858459770899673475L;
	private XMeans xmeans;
	private Instances weka;
	
	
	
	
	public XMeansModel(File file) throws IOException, ClassNotFoundException {
		
		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		XMeansModel model = (XMeansModel) o.readObject();
		o.close();

		this.xmeans = model.getXmeans();
		this.weka = model.getWeka();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());
	}




	public XMeans getXmeans() {
		return xmeans;
	}




	public Instances getWeka() {
		return weka;
	}




	@Override
	public View applyOn(View v, String path) throws SizeException, IllegalArgumentException, IOException, Error {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String getMetaInfo(boolean detailed) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
