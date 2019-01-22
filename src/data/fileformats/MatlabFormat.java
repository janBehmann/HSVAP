package data.fileformats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.json.JSONObject;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

import data.FeatureRole;
import data.FileFormat;
import data.Utilities;
import data.View;
import data.inmemory.DoubleArrayView;


/**
 * Load and write a dataset as a combination of Matlab .mat and .txt file.
 * 
 * When reading from a file, the columns of the dataset in the .mat file are expected to be 
 * 		y x feature1, ..., featurek
 * The .txt file then must contain
 * 		feature1, ..., featurek
 * separated by commas.
 * There can be no special attributes and the feature names need to be 
 * parseable by Double.parseDouble(). While loading, the view adds a special feature RGB
 * that is computed from the bands closest to red, green, blue in this dataset.
 * 
 * When saving, it expects a view to have the format
 * 		y x feature1, ..., featurek
 * While it is possible for the view to contain special features, they will not be stored
 * in the txt file and may cause trouble when loading that file again.
 * 
 * 
 * @author pwelke
 *
 */
public class MatlabFormat implements FileFormat {

	
	@Override
	
	/**
	 * 
	 * Writes the data view to the specified location in a mat file 
	 * Removed: (and a txt file. The columns are reorganized by x,y,feature,label,prob. Missing FeatureTypes are ommited) 
	 * @author jbehmann
	 * 
	 */
	public View writeData(View view, File file) throws IOException {
		
		ArrayList<MLArray> struct = new ArrayList<MLArray>(1);
		
		
		//Rearanging the featur roles is removed
//		//Identify locations of featureRoles
//		FeatureRole[] roles = view.getFeatureRoles();
//		int x_pos=-1;
//		int y_pos=-1;
//		int labelPos=-1;
//		int probabilityPos=-1;
//		LinkedList<Integer> featurePos = new LinkedList<Integer>();
//		
//		for (int i=0;i<roles.length;i++){
//			if (roles[i]==FeatureRole.X){
//				x_pos=i;
//			}
//			if (roles[i]==FeatureRole.Y){
//				y_pos=i;
//			}
//			if(roles[i]==FeatureRole.FEATURE){
//				featurePos.add(i);				
//			}
//			if(roles[i]==FeatureRole.LABEL){
//				labelPos=i;				
//			}
//			if(roles[i]==FeatureRole.PROB){
//				probabilityPos=i;				
//			}
//		}
//		
//		//Generate matrices of a single Featurerole
//		DoubleMatrix coords= new DoubleMatrix();
//		if(x_pos !=-1 && x_pos !=-1){
//			coords = new DoubleMatrix(view.materializeFeatures(new int[]{x_pos,y_pos}));
//		}
//		int[] featurePosi= new int[featurePos.size()];
//		int j = 0;
//		for (int pos : featurePos){
//			featurePosi[j]=pos;
//			j++;
//		}
//		DoubleMatrix features = new DoubleMatrix();
//		if (featurePos.size()!=0){
//			features = new DoubleMatrix(view.materializeFeatures(featurePosi));
//		}
//		DoubleMatrix label = new DoubleMatrix();
//		if (labelPos!=-1){
//			label = new DoubleMatrix(view.materializeFeature(labelPos));
//		}
//		DoubleMatrix prob = new DoubleMatrix();
//		if (probabilityPos!=-1){
//			prob = new DoubleMatrix(view.materializeFeature(probabilityPos));
//		}
//		
//		//Assemble data matrix in the correct order
//		DoubleMatrix data_con=new DoubleMatrix();
//		if(!coords.isEmpty()&& !features.isEmpty()){
//			data_con= DoubleMatrix.concatHorizontally(coords, features);
//		}else if(!features.isEmpty()){
//			data_con= features;
//		}else if(!coords.isEmpty()){
//			data_con= features;
//		}
//		if(!label.isEmpty()){
//			data_con= DoubleMatrix.concatHorizontally(data_con, label);
//		}
//		if(!prob.isEmpty()){
//			data_con= DoubleMatrix.concatHorizontally(data_con, prob);
//		}
//		double[][] data = data_con.toArray2();
				
		double[][] data = view.materialize();
		MLDouble d = new MLDouble("data2D", data);
		struct.add(d);
		// constructor writes stuff to disk. strange.
		new MatFileWriter(file, struct);
		
		
		// Writing a txt file is now removed
//		String textfilename = file.getAbsolutePath();
//		textfilename = textfilename.substring(0, textfilename.length()-4);
//		textfilename = textfilename + ".txt";
//
//		BufferedWriter bw = new BufferedWriter(new FileWriter(textfilename));
//		String[] desc = view.getFeatureDescriptors();
//		try {
//			if(x_pos !=-1 && x_pos !=-1)
//				bw.write("x,y,");
//			
//			for (int i : featurePos) {
//				bw.write(desc[i]);
//				bw.write(",");
//			}
//			if(labelPos!=-1){
//				bw.write("label");
//				bw.write(",");
//			}
//			if(probabilityPos!=-1){
//				bw.write("probability");
//				bw.write(",");
//			}
//			
//			bw.close();
//		} catch (IOException e) {
//			bw.close();
//			e.printStackTrace();
//		}
		
		
		

		return new FileView(this, file, view.getNumberOfExamples(), view.getNumberOfColumns(), view.getFeatureRoles(), view.getFeatureDescriptors());
	}

	
	@Override
	
	/**
	 * loads the data from a mat files.
	 * header information are provided in a separate json file (extension .view or .tv)
	 * header information in a txt file are also accepted for downward compatibility
	 * @author jbehmann
	 */
	public View readData(File file) throws IOException {
		//Reading the data
		MatFileReader mfr = new MatFileReader(file.getAbsolutePath());
		List<String> list = new ArrayList<String>(mfr.getContent().keySet());
		double[][] tempMat =((MLDouble)mfr.getContent().get(list.get(0))).getArray();
		View result = new DoubleArrayView(tempMat, file.getAbsolutePath());
		
		String textfilenamePath = file.getAbsolutePath();
		textfilenamePath = textfilenamePath.substring(0, textfilenamePath.length()-4);
		String textfilename_txt = textfilenamePath + ".txt";
		String textfilename_view = textfilenamePath + ".view";
		String textfilename_tv = textfilenamePath + ".tv";
		
		
		//IF header file is not available
		if(!new File(textfilename_txt).exists() &&!new File(textfilename_view).exists() && !new File(textfilename_tv).exists()){
			// add meta information
			result.setFeatureDescriptors(Utilities.createGenericFeatureNames(result.getNumberOfColumns()));
			result.setFeatureRoles(Utilities.createFeatureRoleArray(result.getNumberOfColumns()));
		}
		else {
			// read meta data		
			if (new File(textfilename_view).exists()){
				// Meta data in view file
				try {
					View v = Utilities.createViewFromJSON(new File(textfilename_view));
					result.setFeatureDescriptors(v.getFeatureDescriptors());
					result.setFeatureRoles(v.getFeatureRoles());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
			else if(new File(textfilename_tv).exists()){
				// Meta data in tv file
				try {
					View v = Utilities.createViewFromJSON(new File(textfilename_tv));
					result.setFeatureDescriptors(v.getFeatureDescriptors());
					result.setFeatureRoles(v.getFeatureRoles());
					result.setLabelMap(v.getLabelMap());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if (new File(textfilename_txt).exists()){
				// Meta data in txt file
			BufferedReader br = new BufferedReader(new FileReader(textfilename_txt));
			String line;
			LinkedList<String> desc= new LinkedList<String>();
			try{
				while((line = br.readLine()) != null) {
					for (String dsc : line.split(",")) {
						desc.add(dsc.trim());
					}
				}
				br.close();
			} catch (IOException e) {
				br.close();
				e.printStackTrace();
			}

			// if old format
			if(desc.size()==(result.getNumberOfColumns()-2)){
				String[] featureDescriptors = new String[result.getNumberOfColumns()];
				FeatureRole[] featureRoles = new FeatureRole[result.getNumberOfColumns()];
				featureDescriptors[0] = "y";
				featureDescriptors[1] = "x";
				featureRoles[0] = FeatureRole.Y;
				featureRoles[1] = FeatureRole.X;

				int i = 2;
				for (String dsc : desc) {
					featureDescriptors[i] = dsc;
					featureRoles[i] = FeatureRole.FEATURE;
					++i;
				}

				result.setFeatureDescriptors(featureDescriptors);
				result.setFeatureRoles(featureRoles);
			}
			// if new format		
			else if(desc.size()==(result.getNumberOfColumns())){
				String[] featureDescriptors = new String[result.getNumberOfColumns()];
				FeatureRole[] featureRoles = new FeatureRole[result.getNumberOfColumns()];
				int i=0;
				for (String dsc:desc){
					if(dsc.equalsIgnoreCase("x")){
						featureRoles[i] = FeatureRole.X;
						featureDescriptors[i]= dsc;
					}else if(dsc.equalsIgnoreCase("y")){
						featureRoles[i] = FeatureRole.Y;
						featureDescriptors[i]= dsc;
					}else if (dsc.matches("\\d+([.]{1}\\d+)?")){
						featureRoles[i] = FeatureRole.FEATURE;
						featureDescriptors[i]= dsc;
					}else if (dsc.equalsIgnoreCase("label")){
						featureRoles[i] = FeatureRole.LABEL;
						featureDescriptors[i]= dsc;
					}else if (dsc.equalsIgnoreCase("probability")){
						featureRoles[i] = FeatureRole.PROB;
						featureDescriptors[i]= dsc;
					}
					else{
						featureRoles[i] = FeatureRole.FEATURE;
						featureDescriptors[i]= dsc;
						System.err.println("Feature " + dsc + " cannot be assigned to a FeatureRole!");
						JFrame frame = new JFrame();						
						JOptionPane.showMessageDialog(frame,
								"Feature " + dsc + " cannot be assigned to a FeatureRole!",
							    "Import warning",
							    JOptionPane.WARNING_MESSAGE);
						frame.pack();
						frame.setVisible(true);
					}
					i++;

				}


				result.setFeatureDescriptors(featureDescriptors);
				result.setFeatureRoles(featureRoles);
			}
			//txt format broken
			else{
				result.setFeatureDescriptors(Utilities.createGenericFeatureNames(result.getNumberOfColumns()));
				result.setFeatureRoles(Utilities.createFeatureRoleArray(result.getNumberOfColumns()));
			}
			}
		}
//		return new AddRGBView(Utilities.materializeRGB(result), result);
		return result;
	}

	
	@Override
	public String getInformation() {
		return "Matlab v5";
	}

	
	@Override
	public String getClassName() {
		return this.getClass().getName();
	}

	
	@Override
	public void initializeValues(JSONObject o) {
		// nothing to do
	}

}

