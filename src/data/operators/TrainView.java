/**
 * Work in Progress
 * C Römer
 */

package data.operators;

import java.awt.Point;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.Range;
import org.jblas.ranges.RangeUtils;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.FeatureSelection;
import data.View;

/**
 * View for training data storage
 * view saves data as [label, features]
 * no other special features are saved within the class
 * only intended for saving data
 * @author roemer
 */

public class TrainView extends AbstractViewImplementation{
	
	//data matrix with features
	private double[][] data;
	
	//string array with the corresponding image name
//	private String[] imageReference;

	//returns data
	public double[][] getData() {
		return data;
	}
	
	//sets data
	public void setData(double[][] data) {
		this.data = data;
	}

	/**
	 * @return the imageReference
	 */
	/*public String[] getImageReference() {
		return imageReference;
	}*/

	/**
	 * @param imageReference the imageReference to set
	 */
	/*public void setImageReference(String[] imageReference) {
		this.imageReference = imageReference;
	}*/
	
	
	/**
	 * StandardConstructor
	 */
	public TrainView(DoubleMatrix data, double[] wls,String name){
		this.setViewName(name);
		this.data=data.toArray2();
		this.setViewDescription("Matrix with labeled training data");
		int nonSpecial = wls.length;
		this.featureDescriptors = new String[nonSpecial+1];
		this.featureRoles = new FeatureRole[nonSpecial +1];
		this.featureRoles[0] = FeatureRole.LABEL;
		this.featureDescriptors[0] = "ClassLabel";
		for (int i=1; i<nonSpecial+1; ++i) {
			this.featureDescriptors[i] = String.valueOf(wls[i-1]);
			this.featureRoles[i] = FeatureRole.FEATURE;			
		}
	}
	
	/**
	 * Constructor initializing an empty view, where the dimensions of the view equal the dimensions
	 * of the currently active view without special features + 1 for label
	 * @param view currently active view
	 */
	public TrainView(View view){		
		this.setViewName("trainData");
		this.setViewDescription("Matrix with labeled training data");
		int nonSpecial = FeatureSelection.getNonSpecialFeatures(view).length;		
		this.featureDescriptors = new String[nonSpecial + 3];		
		this.featureRoles = new FeatureRole[nonSpecial + 3];
		this.featureDescriptors[0] = "Label";
		this.featureRoles[0] = FeatureRole.LABEL;
		this.featureDescriptors[nonSpecial+1] = "X";
		this.featureRoles[nonSpecial+1] = FeatureRole.X;
		this.featureDescriptors[nonSpecial+2] = "Y";
		this.featureRoles[nonSpecial+2] = FeatureRole.Y;
		this.data= new double[0][this.featureRoles.length];
		int loc = 1;
		for (int i=0; i<view.getNumberOfColumns(); ++i) {
			if (view.getFeatureRoles()[i] == FeatureRole.FEATURE){				
				this.featureDescriptors[loc] = view.getFeatureDescriptors()[i];
				this.featureRoles[loc] = FeatureRole.FEATURE;
				loc++;
			}
		}
		
	
		//this.labelMap=view.getLabelMap();
	}
	

	
	/**
	 * Creates a TrainView from a View by adopting all features apart form rgb column.
	 * Only adopts samples whose Label is not -1.
	 * 
	 * @param View to be turned into a TrainView
	 * @return TrainView of input View
	 */
	public static TrainView createTrainViewFromView(View view){		
		
		View f;

		int rgb = FeatureSelection.getRGB(view);
		if(rgb!=-1){
			int[] filter = new int[view.getNumberOfColumns()-1];
			int k = 0;
			for (int i = 0; i<view.getNumberOfColumns(); i++){
				if(view.getFeatureRoles()[i] != FeatureRole.RGB){
					filter[k]=i;
					k=k+1;
				}
			}
			f = new FeatureFilter(view, filter);
		}else{
			f = view;
		}

		int lbl = FeatureSelection.getLabel(f);
		int[] filter2 = new int[f.getNumberOfExamples()];
		int k = 0;
		for (int i = 0; i<f.getNumberOfExamples(); i++){
			if(f.get(i,lbl) != -1){
				filter2[k]=i;
				k=k+1;
			}
		}
		ExampleFilter f2 = new ExampleFilter(f, filter2);

		TrainView tv = new TrainView(f2);
		tv.setFeatureRoles(f.getFeatureRoles());
		tv.setFeatureDescriptors(f.getFeatureDescriptors());
		tv.setData(f.materialize());
		tv.setViewName(view.getViewName()+"_trainData");
		tv.setLabelMap(view.getLabelMap());
		return tv;
	}
	
	//needed to read data from JSON
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException {
		super.initializeValues(o);		
	}
	
	/**
	 * Creates a TrainView from a View by adopting all features apart form rgb column
	 * 
	 * @param View to be turned into a TrainView
	 * @return TrainView of input View
	 */
	public static TrainView createTrainView(View view){		
		
		View f=view;

		//int rgb = FeatureSelection.getRGB(view);
//		if(rgb!=-1){
//			int[] filter = new int[view.getNumberOfColumns()-1];
//			int k = 0;
//			for (int i = 0; i<view.getNumberOfColumns(); i++){
//				if(view.getFeatureRoles()[i] != FeatureRole.RGB){
//					filter[k]=i;
//					k=k+1;
//				}
//			}
//			f = new FeatureFilter(view, filter);
//		}else{
//			f = view;
//		}
		TrainView tv = new TrainView(f);
		tv.setFeatureRoles(f.getFeatureRoles());
		tv.setFeatureDescriptors(f.getFeatureDescriptors());
		tv.setData(f.materialize());
		tv.setViewName(view.getViewName());
		
		return tv;
	}
	
	
	
	
	/**
	 * adds a new sample to trainView
	 * tests if feature dimensions are equal
	 * @param double[] new sample
	 * @param double label index of the class 
	 */
	public void addRow(double[] data, double label){
		if (this.data.length== 0){
		    this.data = new double[1][data.length+1];			    
			for (int i = 1; i<data.length; i++){				
				this.data[0][i] = data[i-1];
			}
			this.data[0][0] = label;
			this.materializeMinMaxValue();
		} else {
			if (this.data[0].length==data.length+1){
				double[][] newData = new double[this.data.length+1][this.data[0].length];
				for (int i = 0; i<this.data.length;i++){
					for (int j=0;j<this.data[0].length;j++){
						newData[i][j] = this.data[i][j];
					}
				}
				for (int i = 1; i<=data.length; i++){				// Errolr corrected that removes the last data entry
					newData[this.data.length][i] = data[i-1];
				}
				newData[this.data.length][0] = label;
				this.data = new double[newData.length][newData[0].length];
				this.data = newData;
				this.materializeMinMaxValue();
			} else {
				System.out.println("Wrong Matrix Dimensions");
				//durch throw ersetzen? Fehlerausgabe an user
			}
		}	
	}
	
	/**
	 * adds a new sample to trainView
	 * tests if feature dimensions are equal
	 * @param double[] new sample
	 * @param double label index of the class 
	 * @param double [] xyCoordinate pixel location within imageName
	 * @param String imageName Reference to the image
	 * @author jbehmann
	 */
	public void addRow(double[] data, double label, double[] xyCoordinate){
		if (this.data.length==0){
		    this.data = new double[1][data.length+3];			    
	//	    this.imageReference=new String[1];
			for (int i = 1; i<=data.length; i++){				
				this.data[0][i] = data[i-1];
			}
			this.data[0][0] = label;
	//		this.imageReference[0]=imageName;
			this.data[0][data.length+1]=xyCoordinate[0];
			this.data[0][data.length+2]=xyCoordinate[1];
			this.materializeMinMaxValue();
			
		} else {
			if (this.data[0].length==data.length+3){
				double[][] newData = new double[this.data.length+1][data.length+3];
				//String[] newReferences = new String[this.data.length+1];
				for (int i = 0; i<this.data.length;i++){
					for (int j=0;j<this.data[0].length;j++){
						newData[i][j] = this.data[i][j];
					}
//					newReferences[i]=this.imageReference[i];
				}
				for (int i = 0; i<data.length; i++){				
					newData[this.data.length][i+1] = data[i];					
				}
				newData[this.data.length][data.length+1] = xyCoordinate[0];
				newData[this.data.length][data.length+2] = xyCoordinate[1];				
				newData[this.data.length][0] = label;
				//newReferences[this.data.length]=imageName;
//				this.data = new double[newData.length][newData[0].length];
				this.data = newData;
				this.materializeMinMaxValue();
//				this.imageReference= newReferences;
			} else {
				System.out.println("Wrong Matrix Dimensions "+this.data[0].length+" != "+data.length+3);
				//durch throw ersetzen? Fehlerausgabe an user
			}
		}	
	}
	
	@Override
	public int getNumberOfColumns() {		
		return this.featureDescriptors.length;
	}
   
	@Override
	public int getNumberOfExamples() {		
		return this.data.length;
	}

	@Override
	/**
	 *No X,Y or RGB stored in trainViews
	 */
	public double get(int i, int j) {
			return this.data[i][j];
		
	}
	
	/**
	 * utility function that returns included label values if available
	 * returns null if no featurerole label is available
	 * returns int[0] if featurerole label is available but no examples are included in the TV
	 * returns the label column otherwise
	 * 
	 * @author jbehmann
	 */
	
	public int[] getLabelValues(){
		//Find Columns
		FeatureRole[] roles = this.getFeatureRoles();
		int j=-1;
		for (int i =0;i<roles.length;i++){
			if(roles[i]==FeatureRole.LABEL){
				j=i;
				break;
			}			
		}
		
		//Cases 1-3
		if (j==-1){
			return null;
		}
		else if(this.getNumberOfExamples()==0){
			return new int[0];
		}
		else {
			double[] source = this.materializeFeature(j);
			int[] dest = new int[source.length];
		    for(int i=0; i<source.length; i++) {
		        dest[i] = (int) source[i];
		    }
		    return dest;					
		}
		
		
		
	}
	/**
	 * utility function that returns included label values if available
	 * returns null if no featurerole label is available
	 * returns int[0] if featurerole label is available but no examples are included in the TV
	 * returns the label column otherwise
	 * 
	 * @author jbehmann
	 */
	
	public double[] getLabelValuesAsDouble(){
		//Find Columns
		FeatureRole[] roles = this.getFeatureRoles();
		int j=-1;
		for (int i =0;i<roles.length;i++){
			if(roles[i]==FeatureRole.LABEL){
				j=i;
				break;
			}			
		}
		
		//Cases 1-3
		if (j==-1){
			return null;
		}
		else if(this.getNumberOfExamples()==0){
			return new double[0];
		}
		else {
			return this.materializeFeature(j);
					
		}
		
		
		
	}
	
	/**
	 * Removes a sample from the trainview if the stored coordinates match p and return true, return false otherwise
	 * @author jbehmann
	 * @param p the coordinates
	 * @return
	 */
	public boolean remove(Point p){
		FeatureRole[] roles = this.getFeatureRoles();
		int x=-1;
		int y=-1;
		for (int i =0;i<roles.length;i++){
			if(roles[i]==FeatureRole.X){
				x=i;				
			}	
			if(roles[i]==FeatureRole.Y){
				y=i;				
			}	
		}
		if (x ==-1 || y==-1){
			return false;
		}
		
		double[] xcoord= this.materializeFeature(x);
		double[] ycoord= this.materializeFeature(y);
		for (int i = 0;i<xcoord.length;i++){
			if (Math.abs(xcoord[i]-p.x)<0.1 && Math.abs(ycoord[i]-p.y)<0.1 ){
				this.remove(i);
				return true;
			}
		}		
		return false;
	}
	
	
	/**
	 * Removes the sample with index i from the trainview 
	 * @author jbehmann
	 * @param i the sample index
	 * @return
	 */
	public boolean remove(int i){
		if(i>this.getNumberOfExamples()-1 || i<0){
			return false;
		}
		DoubleMatrix mat =new DoubleMatrix(data);
		new RangeUtils();
		Range indices = RangeUtils.interval(0,i);
		Range indices2 = RangeUtils.interval(i+1,this.getNumberOfExamples());
		DoubleMatrix newMat = DoubleMatrix.concatVertically(mat.getRows(indices), mat.getRows(indices2)); 
		//System.out.println("entfernt: " + i + " -> "+ mat.rows + " zu " + newMat.rows);
		this.data=newMat.toArray2();
		return true;
	}

}
