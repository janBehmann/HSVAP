/**
 * 
 */
package wekatools;

import org.jblas.DoubleMatrix;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author jbehmann
 *
 */
public class WekaTransformer implements MatrixTransformer<Instances> {

	
	/**
	 * Transform double[][] to Weka Instances
	 */	
	public Instances transformMatrix(double[][] data) {
		FastVector names = new FastVector();
		for (int i = 0; i<data[0].length; i++){
			names.addElement(new Attribute("NUMERIC"+i));
		}
		Instances wekaInstances = new Instances("FeatureHyper", names, data.length);
		for (double[] instanceHyp:data){
			Instance instanceLok= new Instance(1, instanceHyp);
			wekaInstances.add(instanceLok);
		}		
		return wekaInstances;		
	}
	/**
	 * Transform dDoubleMatrix to Weka Instances
	 */	
	public Instances transformMatrix(DoubleMatrix data) {
		FastVector names = new FastVector();
		for (int i = 0; i<data.columns; i++){
			names.addElement(new Attribute("NUMERIC"+i));
		}
		Instances wekaInstances = new Instances("FeatureHyper", names, data.length);
		int r=0;
		for (DoubleMatrix instanceHyp=data.getRow(r);r<data.rows;r++){
			Instance instanceLok= new Instance(1, instanceHyp.toArray());
			wekaInstances.add(instanceLok);
		}		
		return wekaInstances;		
	}
	
	/**
	 * Transform dDoubleMatrix to Weka Instances
	 */	
	public Instances transformMatrix(DoubleMatrix data, boolean clearEmpty) {
		FastVector names = new FastVector();
		for (int i = 0; i<data.columns; i++){
			names.addElement(new Attribute("NUMERIC"+i));
		}
		Instances wekaInstances = new Instances("FeatureHyper", names, data.length);
		
		if(clearEmpty)
			for (int r=0;r<data.rows;r++){
				DoubleMatrix instanceHyp=data.getRow(r);
				if (instanceHyp.get((int) (instanceHyp.length/2))!=0){
					Instance instanceLok= new Instance(1, instanceHyp.toArray());
					wekaInstances.add(instanceLok);
				}
			}	
		else{
			for (int r=0;r<data.rows;r++){
				DoubleMatrix instanceHyp=data.getRow(r);
				Instance instanceLok= new Instance(1, instanceHyp.toArray());
				wekaInstances.add(instanceLok);				
			}	
		}
		return wekaInstances;		
	}
}
