package models;

import gui.MainGui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import libsvm.svm;
import libsvm.svm_node;
//import sun.misc.InnocuousThread;

import org.jblas.DoubleMatrix;
import org.jblas.ranges.*;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import wekatools.WekaTransformer;
import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.inmemory.DoubleArrayView;
import data.operators.AddLabelsView;

public class SVMModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6071649388993413023L;
	
	@Deprecated
	private libsvm.svm_model svm_model_old;
	private Instances inst;
	private SMO svmModel;
	private Map<Integer,String>	labelMap;
	private int[] sortedLabel;

	/** Creates a new SVMModel using the given SMO */
	public SVMModel(SMO svmModel, Map<Integer, String> labelMap){
	 	this.svmModel = svmModel;
		this.labelMap = labelMap;
	}
	
	/**
	 * Learns the SVM Model for classification
	 * 
	 * author: strauch (snippets from croemer)
	 */
	public SVMModel(DoubleMatrix spektrenNorm, DoubleMatrix label, double c, double kernel, KernelType type, Map<Integer, String> labelMap) throws Exception{
		this.labelMap = labelMap;
		
		// Create sorted list of label for applyOn
		SortedSet<Integer> sortedLabel = new TreeSet<Integer>();
		for(int i = 0; i < label.length; i++)
			sortedLabel.add((int)label.get(i));
		this.sortedLabel = new int[sortedLabel.size()];
		int i = 0;
		for(int labelID : sortedLabel)
			this.sortedLabel[i++] = labelID;
		
		inst = getInstances(spektrenNorm, label);																								
		svmModel = new SMO();
		
		//define kernel options in dependence on kernel settings
		String[] options;
		switch(type){
			case LINEAR:
				options = new String[]{"-C",Double.toString(c),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E","1.0"};
				break;
			case RBF:
				options = new String[]{"-C",Double.toString(c),"-M","1","-K","weka.classifiers.functions.supportVector.RBFKernel","-G",Double.toString(kernel)};
				break;
			case POLYNOMIAL:
				options = new String[]{"-C",Double.toString(c),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E",Double.toString(kernel)};
				break;
			default: throw new Exception("Unknown KernelType");
		}
		svmModel.setOptions(options);
		svmModel.buildClassifier(inst);
	}
	
	/**
	 * makes a x-fold cross validation for this svm model<br>
	 * Throws NullPointer if not created using {@link #SVMModel(DoubleMatrix, DoubleMatrix, double, double, KernelType, Map)}
	 *  
	 * author: strauch (snippets from croemer)
	 */
	public Evaluation getCrossValidation(int numFolds, Random rand) throws Exception{
		//x-fold cross validation
		Evaluation eval = new Evaluation(inst);
		eval.crossValidateModel(svmModel, inst, numFolds, rand );
		eval.confusionMatrix();
		//calculate percentage of correctly classified samples
		return eval;						
	}
	
	/**
	 * transforms the DoubleMatrices for non-special features and label to Instances for Weka processing
	 * @param spektrenNorm	DoubleMatrix with features
	 * @param label	DoubleMatrix with label vector
	 * @return	inst	Instance: data in Weka Format
	 * @throws Exception	Weka Exceptions for Instance errors
	 */
	private Instances getInstances(DoubleMatrix spektrenNorm, DoubleMatrix label) throws Exception{
		double[][] learning = new double[spektrenNorm.rows][spektrenNorm.columns+1];
		for (int i = 0; i<spektrenNorm.rows; i++){
			for (int j = 0; j<spektrenNorm.columns+1; j++){
				if (j<spektrenNorm.columns)
					learning[i][j] =  spektrenNorm.get(i,j);
				else
					learning[i][j] =  label.get(i);
			}
						
		}
		
		
		//transform double[][] to weka instances
		WekaTransformer wkt = new WekaTransformer();			
		Instances inst = wkt.transformMatrix(learning);
		//define attribute as class label
		inst.setClassIndex(inst.numAttributes()-1);
		
		//convert numeric label to nominal label, needed for weka classifier
		NumericToNominal convert = new NumericToNominal();			
		String[] filter_options = new String[2];
		//defines the attribute to convert
		filter_options[0] = "-R";
		filter_options[1] = Integer.toString(inst.numAttributes());	// why not just "last"?		
		convert.setOptions(filter_options);
		convert.setInputFormat(inst);			
		inst = Filter.useFilter(inst, convert);	
		
		return inst;		
	}

	@Deprecated
	public SVMModel(String name, String datensatz, Date date, libsvm.svm_model svm_model,Map<Integer,String> labelMap) {
		super(name, datensatz, date);
		this.svm_model_old = svm_model;
		this.labelMap = labelMap;
	}

	public SVMModel(File file) throws IOException, ClassNotFoundException{

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		SVMModel model = (SVMModel) o.readObject();
		o.close();

		this.svm_model_old = model.getSVMModel_Old();
		this.svmModel = model.svmModel;
		this.labelMap = model.getLabelMap();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());

	}

	@Deprecated
	public libsvm.svm_model getSVMModel_Old() {
		return svm_model_old;
	}

	public SMO getSVMModel(){
		return svmModel;
	}

	public Map<Integer,String> getLabelMap(){
		return labelMap;
	}

	/**
	 * predicts probabilities of all instances in the images of the train view
	 * 
	 * author: strauch (snippets from croemer)
	 */
	public View applyOn(View v, String path) throws IllegalArgumentException, IOException{ //TODO what is path for?
		try{
			if (sortedLabel==null){
				Set<Integer> keys = labelMap.keySet();		
				SortedSet<Integer> labelTree = new TreeSet<Integer>();
				for(int val : keys)
					labelTree.add(val);
				this.sortedLabel = new int[labelTree.size()];
				int i = 0;
				for(int labelID : labelTree)
					this.sortedLabel[i++] = labelID;				
			}
			//get features and coordinates from the current view v for prediction
//			DoubleMatrix features = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getNonSpecialFeatures(v));												
			DoubleMatrix coordinates = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getCoordinates(v));						
			//matrix for prediction results: x,y,label, probabilities
			double[][] result = new double[v.getNumberOfExamples()][3+labelMap.size()+FeatureSelection.getNonSpecialFeatures(v).length];
			
			//transform features to instances, last column = nominal feature for label
//			Instances inst = getInstances(features);
			DoubleMatrix features;
			int counter = 0; 
			int inc = 10000;
//			if (Utilities.materializeAsDoubleMatrix(v).rows>inc){				
//				features = Utilities.materializeAsDoubleMatrix(v).getRows(new IntervalRange(0,inc-1));
//				features= features.getColumns(FeatureSelection.getNonSpecialFeatures(v));
//			}
//			else{
//				features = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getNonSpecialFeatures(v));		
//			}
//			Instances inst = getInstances(features);
			Instances inst = null;
			//for each sample of the view v					   
			for (int i = 0; i <v.getNumberOfExamples(); i++){
				if(inst==null || inst.numInstances()==0 && counter*inc<v.getNumberOfExamples()){
					DoubleMatrix featureTemp = Utilities.materializeAsDoubleMatrix(v).getRows(new IntervalRange(counter*inc,Math.min((counter*inc)+inc,v.getNumberOfExamples())));//, FeatureSelection.getNonSpecialFeatures(v)[0],FeatureSelection.getNonSpecialFeatures(v)[FeatureSelection.getNonSpecialFeatures(v).length-1]);				
					featureTemp=featureTemp.getColumns(FeatureSelection.getNonSpecialFeatures(v));						
					inst = getInstances(featureTemp);
//					inst = Instances.mergeInstances(inst, instTemp);	
					System.out.println("Classifying "+(counter*inc));
					counter=counter+1;
				}			
				
//				DoubleMatrix features = Utilities.materializeAsDoubleMatrix(v).get(new int[]{i},FeatureSelection.getNonSpecialFeatures(v));
				//transform features to instances, last column = nominal feature for label
//				Instances inst = getInstances(features);
				//probabilities of each class for sample
				if (inst.numInstances()==0){
					DoubleMatrix featureTemp = Utilities.materializeAsDoubleMatrix(v).getRow(i);//, FeatureSelection.getNonSpecialFeatures(v)[0],FeatureSelection.getNonSpecialFeatures(v)[FeatureSelection.getNonSpecialFeatures(v).length-1]);				
					featureTemp=featureTemp.getColumns(FeatureSelection.getNonSpecialFeatures(v));						
					inst = getInstances(featureTemp);
//					inst = Instances.mergeInstances(inst, instTemp);	
//					System.out.println("Classifying "+(counter*inc));
//					counter=counter+1;
						
				}
				double[] prob = svmModel.distributionForInstance(inst.firstInstance()); 
				
				//instance(0)
				//coordinates of sample
				result[i][0] = coordinates.get(i,0);
				result[i][1] = coordinates.get(i,1);	
				//temp to get max prob
				double temp = -1;
				//prediction for label
				result[i][2] = 0;
				//for all probs get max prob for label prediction and write prob to results
//				System.out.println(prob[0]+","+prob[1]);
//				System.out.println(sortedLabel[0]+","+sortedLabel[1]);
				
				for (int j = 1; j<prob.length+1;j++){
					if (prob[j-1]>temp){
						temp = prob[j-1];
						result[i][2] = sortedLabel[j-1];
					}
					result[i][j+2] = prob[j-1];		
				}
//				System.out.println("1: "+features.getColumns());
//				System.out.println("2: "+features.columns);
//				System.out.println("3: "+prob.length+3+features.columns);
//				System.out.println("4: "+prob.length);
//				System.out.println("5: "+result.length);
//				System.out.println("6: "+result[0].length);
				
				for (int j = prob.length+3; j<prob.length+3+inst.firstInstance().numAttributes()-1;j++){
					result[i][j] =  inst.firstInstance().value(j-prob.length-3);//Utilities.materializeAsDoubleMatrix(v).get(i,inst.firstInstance().value(j-prob.length-3)); 	//FeatureSelection.getNonSpecialFeatures(v)[j-prob.length-3]			
				}
				inst.delete(0);
					
			}
			
//			//prediction in percent
//			double[] perc = new double[labelMap.size()];
//			System.out.println("---" + perc.length + "---");
//			for(int i = 0; i < result.length; i++)
//				perc[(int)result[i][2]-1]++;
//			for(int i = 0; i < perc.length; i++){
//				perc[i] /= result.length;
//				System.out.printf("%s: %.2f%%\n", labelMap.get(i+1), perc[i]*100);
//			}
			
			
			//apply results to new view
			String viewname = v.getViewName() + "_result";
			DoubleArrayView dav = new DoubleArrayView(result, viewname);
			dav.setFeatureRole(0, FeatureRole.X);
			dav.setFeatureRole(1, FeatureRole.Y);
			dav.setFeatureRole(2, FeatureRole.LABEL);
//			dav.setLabelMap(v.getLabelMap());
			dav.setLabelMap(labelMap);
			for (int i = 3; i<3+labelMap.size(); i++)
				dav.setFeatureRole(i, FeatureRole.PROB);
			String[] descs = v.getFeatureDescriptors();
			int[] indizes = FeatureSelection.getNonSpecialFeatures(v);
			
			for (int  i = 3+labelMap.size(); i<dav.getNumberOfColumns();i++){
				dav.setFeatureRole(i, FeatureRole.FEATURE);
				dav.setFeatureDescriptor(i, descs[indizes[i-3-labelMap.size()]]);
			}
			
			//update view description 
			Map<Integer, Integer> counts = Utilities.countLabel(dav);
			StringBuilder description = new StringBuilder();
			for(Entry<Integer, String> label : labelMap.entrySet()) {
				description.append("\n").append(label.getValue().split(" #")[0]).append(": ");
				int count = 0;
				if(counts.containsKey(label.getKey()))
					count = counts.get(label.getKey());
				double perc = (count * 100.) / dav.getNumberOfExamples();
				description.append(String.format("%.1f%% (%,d/%,d)", perc, count, dav.getNumberOfExamples()));
			}
			dav.setViewDescription(description.toString());
			Logger.getLogger(MainGui.class.getName()).info(description.toString());
			
			return dav;
		} catch(IllegalArgumentException e){ throw e;
		} catch(IOException e){ throw e;
		} catch(Exception e){ e.printStackTrace(); throw new IOException(e);	}
	}
	
	/**
	 * creates instances for prediction, label is automatically generated and added on last position
	 * labels are needed for prediction in weka
	 * @param spektrenNorm features for classification
	 * @return Instances weka format for classifier
	 * @throws Exception if transformation to weka does not work
	 * 
	 * @author croemer
	 */
	private Instances getInstances(DoubleMatrix spektrenNorm) throws Exception {
		double[][] learning = new double[spektrenNorm.rows][spektrenNorm.columns+1];
		for (int i = 0; i<spektrenNorm.rows; i++){
			for (int j = 0; j<spektrenNorm.columns+1; j++){
				if (j<spektrenNorm.columns)
					learning[i][j] =  spektrenNorm.get(i,j);
				else
					learning[i][j] =  0;
			}
						
		}
		//transform double[][] to weka instances
		WekaTransformer wkt = new WekaTransformer();
		
		Instances inst = wkt.transformMatrix(learning);
		inst.setClassIndex(inst.numAttributes()-1);
		
		
		//convert numeric label to nominal label, needed for weka classifier
		NumericToNominal convert = new NumericToNominal();			
		String[] filter_options = new String[2];
		//defines the attribute to convert
		filter_options[0] = "-R";
		filter_options[1] = Integer.toString(inst.numAttributes());			
		convert.setOptions(filter_options);
		convert.setInputFormat(inst);			
		inst = Filter.useFilter(inst, convert);	
		
		
		
		return inst;
	}

	@Deprecated
	public View applyOnOld(View v, String path) throws IllegalArgumentException, IOException{

		svm_node[] x = new svm_node[FeatureSelection.getNonSpecialFeatures(v).length];
		v.setLabelMap(labelMap);
		int totalClasses = labelMap.size();
		double[] label_erg = new double[v.getNumberOfExamples()];
		double [] prob_estimates = new double[totalClasses]; 
		int [] idxFeatures = FeatureSelection.getNonSpecialFeatures(v);
		for(int i=0;i<v.getNumberOfExamples();i++){
			for(int j=0; j< idxFeatures.length ;j++){
				x[j] = new svm_node();
				x[j].index = j+1;
				x[j].value = v.get(i,idxFeatures[j]);
			}		

			label_erg[i] =svm.svm_predict_probability(this.getSVMModel_Old(), x, prob_estimates);

		}

		return new AddLabelsView(label_erg,v,path);

	}

	@Deprecated
	public String toolTipTextOld(){
		return DF.format(this.getDate()) + ", gamma " +  this.svm_model_old.param.gamma  + " applied on " + this.getDatensatz();
	}
	
	@Override
	public String getMetaInfo(boolean detailed) {
		StringBuilder meta = new StringBuilder("<u>svm parameter:</u>");
		
		meta.append("<br>c: ").append(svmModel.getC());
		meta.append("<br>epsilon: ").append(svmModel.getEpsilon());
		//TODO Add more values?
		
		if(detailed){
			meta.append("<p>bias: ").append(new DoubleMatrix(svmModel.bias()));
			//TODO Add more values?
		}
		
		return meta.toString();
	}

	@Deprecated
	public String getMetaInfoOld(boolean detailed) {

		String meta = this.getMainMeta();

		meta+="<u>svm parameter:</u><br>degree: "+ this.getSVMModel_Old().param.degree;
		meta += "<br>gamma: "+this.getSVMModel_Old().param.gamma;
		meta += "<br>kernel_type: "+this.getSVMModel_Old().param.kernel_type;
		meta += "<br>probability: "+this.getSVMModel_Old().param.probability;
		meta += "<br>nr_weight: "+this.getSVMModel_Old().param.nr_weight;
		//TODO parameter

		if (detailed==true){

			meta += "<p>weights: "+new DoubleMatrix(this.getSVMModel_Old().param.weight).toString();
			DoubleMatrix a = new DoubleMatrix(this.getSVMModel_Old().param.weight_label.length);
			for (int i=0; i< this.getSVMModel_Old().param.weight_label.length; i++){
				a.put(i, this.getSVMModel_Old().param.weight_label[i]);
			}
			meta += "<p>weight_label: "+a.toString();

		}

		return meta;
	}
	
	public static enum KernelType {
		LINEAR, RBF, POLYNOMIAL
	}

}
