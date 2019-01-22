package data;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.jblas.DoubleMatrix;
import org.json.JSONArray;
import org.json.JSONObject;



public class Utilities {

	public static double[] materializeExample(View v, int i) {
		double[] example = new double[v.getNumberOfColumns()];
		for (int j=0; j<v.getNumberOfColumns(); ++j) {
			example[j] = v.get(i, j);
		}
		return example;
	}

	public static double[][] materializeExamples(View v, int [] i) {
		double[][] example = new double[i.length][v.getNumberOfColumns()];
		for (int ii=0; ii<i.length; ii++){
			example[ii] = materializeExample(v,i[ii]);

		}
		return example;
	}

	public static double[] materializeFeature(View v, int j) {
		double[] feature = new double[v.getNumberOfExamples()];
		for (int i=0; i<v.getNumberOfExamples(); ++i) {
			feature[i] = v.get(i, j);
		}
		return feature;
	}

	public static double[][] materializeFeatures(View v, int [] j) {
		double[][] feature = new double[v.getNumberOfExamples()][j.length];
		for (int i=0; i<v.getNumberOfExamples(); ++i) {
			for (int jj=0; jj<j.length; jj++){
				feature[i][jj] = v.get(i,j[jj]);
			}
		}
		return feature;
	}
	/**
	 * Get the data represented by the View v as a double[][] array. 
	 * Note: everytime you use this method, you will create a new copy 
	 * of all data represented by v in memory.
	 * @param v
	 * @return
	 */
	public static double[][] materializeMatrix(View v) {
		double[][] matrix = new double[v.getNumberOfExamples()][];
		for (int i=0; i<v.getNumberOfExamples(); ++i) {
			matrix[i] = materializeExample(v, i);
		}
		return matrix;
	}

	public static double[][] materializeFeatureExamples(View v, int[] i, int [] j){
		double [][] matrix = new double[i.length][j.length];
		for (int ii=0; ii<i.length;ii++){
			for (int jj=0; jj<j.length; jj++)
				matrix[ii][jj] = v.get(i[ii], j[jj]);
		}

		return matrix;
	}

	/**
	 * Get the data represented by the View v as a jblas.DoubleMatrix. 
	 * Note: everytime you use this method, you will create a new copy 
	 * of all data represented by view in memory.
	 * @param v
	 * @return
	 */
	public static DoubleMatrix materializeAsDoubleMatrix(View v) {
		DoubleMatrix data = new DoubleMatrix(v.getNumberOfExamples(), v.getNumberOfColumns());
		for (int i=0; i<v.getNumberOfExamples(); ++i) {
			for (int j=0; j<v.getNumberOfColumns(); ++j) {
				data.put(i,  j, v.get(i, j));
			}
		}
		return data;
	}

	/*public static Matrix materializeAsJamaMatrix(View v) {
		Matrix data = new Matrix(v.getNumberOfExamples(), v.getNumberOfColumns());
		for (int i=0; i<v.getNumberOfExamples(); ++i) {
			for (int j=0; j<v.getNumberOfColumns(); ++j) {
				data.set(i, j, v.get(i, j));			
			}
		}
		return data;
	}*/

	public static View createViewFromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String className = o.getString("className");
		@SuppressWarnings("unchecked")
		Class<? extends View> c = (Class<? extends View>)Class.forName(className);
		View view = c.newInstance();
		view.initializeValues(o);
		return view;
	}

	public static View createViewFromJSON(File f) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
		BufferedReader r = new BufferedReader(new FileReader(f));
		String json = r.readLine();
		JSONObject o = new JSONObject(json);
		if (!o.has("viewName")) {
			o.putOnce("viewName", f.getAbsolutePath());
		}
		r.close();
		return createViewFromJSON(o);
	}

	public static FileFormat createFileFormatFromJSON(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		String className = o.getString("className");
		@SuppressWarnings("unchecked")
		Class<? extends FileFormat> c = (Class<? extends FileFormat>)Class.forName(className);
		FileFormat format = c.newInstance();
		format.initializeValues(o);
		return format;
	}

	public static String[] createGenericFeatureNames(int nFeatures) {
		String[] result = new String[nFeatures];
		for (int i=0; i<nFeatures; ++i) {
			result[i] = Integer.toString(i);
		}
		return result;
	}
	
	public static FeatureRole[] createFeatureRoleArray(int nFeatures) {
		FeatureRole[] result = new FeatureRole[nFeatures];
		for (int i=0; i<nFeatures; ++i) {
			result[i] = FeatureRole.FEATURE;
		}
		return result;
	}
	
	/**
	 * Transforms a string to a `valid` view name by replacing any conflicting characters 
	 * with a space and trimming the result.
	 * It returns "null" if the result of these operations is the empty string.
	 * If the input is null, a NullPointerException is thrown.
	 * 
	 * A `valid` view name has to satisfy the following rules:
	 * - it must not be null or empty
	 * - it must not begin with - or .
	 * - it must not contain / or \
	 * - it must not contain any whitespace character but space
	 * - it must not begin or end with whitespaces
	 * 
	 * @param candidateName
	 * @return
	 */
	public static String makeValidViewName(String candidateName) {
		
		String name = candidateName.replaceAll("\\\\", " ");
		name = name.replaceAll("/", " ");
		name = name.replaceAll("\\s+", " ");
		name = name.trim();

		while (name.startsWith("-") || name.startsWith(".")) {
			name = name.substring(1, name.length());
		}
		
		if (name.equals("")) {
			return "null";
		} else {
			return name;
		}
	}
	
	/**
	 * 
	 * @param v
	 * @return
	 */
	public static double[] materializeRGB(View v) {
		if(FeatureSelection.getRGB(v)==-1){
		double red = 680;
		double green = 540;
		double blue = 435;
		
		double MaxValue = 0;
		String[] waveString = v.getFeatureDescriptors();
		int[] featureIdx = FeatureSelection.getNonSpecialFeatures(v);
		int[] colorIndex = new int[]{featureIdx[0],featureIdx[0],featureIdx[0]};
		
		// convert featureDescription Strings to doubles
		// TODO explodes, if there is a nonspecial feature that is not parsable by Double.parseDouble()
		double[] waveDoubles = new double[waveString.length];
		for (int k=0; k<featureIdx.length; ++k) {
			waveString[featureIdx[k]]=waveString[featureIdx[k]].replaceAll(Pattern.quote("}"),"");
			waveString[featureIdx[k]]=waveString[featureIdx[k]].replaceAll(Pattern.quote("{"),"");
			waveDoubles[featureIdx[k]] = Double.parseDouble(waveString[featureIdx[k]].trim().split(" ")[waveString[featureIdx[k]].trim().split(" ").length-1]);
		}
		
		// find features that are closest to wavelengths of red, green, and blue
		for (int k = 1; k < featureIdx.length; ++k) {
			if (Math.abs(red - waveDoubles[featureIdx[k]]) < Math.abs(red - waveDoubles[colorIndex[0]]))
				colorIndex[0] = featureIdx[k];

			if (Math.abs(green - waveDoubles[featureIdx[k]]) < Math.abs(green - waveDoubles[colorIndex[1]]))
				colorIndex[1] = featureIdx[k];

			if (Math.abs(blue - waveDoubles[featureIdx[k]]) < Math.abs(blue - waveDoubles[colorIndex[2]]))
				colorIndex[2] = featureIdx[k];	
		}

		// materialize features and create output array
		double[] dataRed = v.materializeFeature(colorIndex[0]);
		double[] dataGreen = v.materializeFeature(colorIndex[1]);
		double[] dataBlue = v.materializeFeature(colorIndex[2]);
		double[] dataRGB = new double[dataRed.length];
		
		// find maximum value for normalization
		for (int i = 0; i < dataRed.length; ++i) {
			if (MaxValue < dataRed[i] )
				MaxValue = dataRed[i];
			if (MaxValue < dataBlue[i] )
				MaxValue = dataBlue[i];
			if (MaxValue < dataGreen[i] )
				MaxValue = dataGreen[i];
		}
		 
		for (int i = 0; i < dataRGB.length; i++) {
			// normalize
			dataRed[i] = dataRed[i] / MaxValue * 255.0;
			dataGreen[i] = dataGreen[i] / MaxValue * 255.0;
			dataBlue[i] = dataBlue[i] / MaxValue * 255.0;
			
			// some strange invert if value smaller than some threshold
			if (dataRed[i] < 16 & dataGreen[i] < 16 & dataBlue[i] < 16) {
				dataRed[i] = 255 - dataRed[i];
				dataGreen[i] = 255 - dataGreen[i];
				dataBlue[i] = 255 - dataBlue[i];
			}
			
			// convert data to int storing rgb 
			dataRGB[i] = (255 << 24) | ((int) Math.round((dataRed[i])) << 16)
					| ((int) Math.round((dataGreen[i])) << 8)
					| (int) Math.round(dataBlue[i]);
		}	
		return dataRGB;
	}
		else{
			return v.materializeFeature(FeatureSelection.getRGB(v));  
		}
	}

	
	/**
	 * Calculation of min and max values. Aproximated for large amounts of data
	 * @param v
	 * @throws NullPointerException
	 */
	public static void materializeMinMaxValue(View v) throws NullPointerException {
		//if(v.getMinValue()==Double.MAX_VALUE && v.getMaxValue()==Double.MIN_VALUE){
			if (v.getNumberOfExamples()<10000){
			DoubleMatrix spectrum= new DoubleMatrix(v.materializeFeatures(FeatureSelection.getNonSpecialFeatures(v)));
			v.setMinValue(spectrum.min());
			v.setMaxValue(spectrum.max());
			}else{
				int[] selection = new int[(int) Math.floor(v.getNumberOfExamples()/10)+1];
				int counter=0;
				for (int i =0; i<v.getNumberOfExamples();i=i+10){
					selection[counter]=i;
					counter++;					
				}
			DoubleMatrix spectrum= new DoubleMatrix(v.materializeFeatureExamples(selection, FeatureSelection.getNonSpecialFeatures(v)));
			v.setMinValue(spectrum.min());
			v.setMaxValue(spectrum.max()*1.1);
			}

		//}
	}

	
	public static void materializeXYDimension(View v) {
		if(v.getXDimension()==0 && v.getXDimension()==0){
			DoubleMatrix koordinaten= new DoubleMatrix(v.materializeFeatures(FeatureSelection.getCoordinates(v)));
			DoubleMatrix maxs = koordinaten.columnMaxs();
			DoubleMatrix mins = koordinaten.columnMins();
			v.setXDimension((int) (maxs.get(0, 0)-mins.get(0, 0))+1);
			v.setYDimension((int) (maxs.get(1, 0)-mins.get(1, 0))+1);
		}
	}
	
	/**
	 * Function that extracts the RGB Color from the defining string of a Label 
	 * -inverts "+Integer.toHexString(colorChooser.getColor().getRGB()).substring(2));
	 * @author jbehmann
	 */
	public static Color getColor(String name){
		if(name==null)
			return Color.GRAY;
		Color col = Color.decode(name.substring(name.length()-7, name.length()));
		return col;
	}
	
	/**
	 * Function that extracts the pixel positions of 10 predefined colors
	 */
	public static Map<String, LinkedList<Point>> getLabelLocations(BufferedImage img, LinkedList<String[]> labelColors){
		
		Map<String,LinkedList<Point>> map = new HashMap<String,LinkedList<Point>>();
		
		
		
		LinkedList<Point> r =new LinkedList<Point>();
		LinkedList<Point> g =new LinkedList<Point>();
		LinkedList<Point> b =new LinkedList<Point>();
		LinkedList<Point> c =new LinkedList<Point>();
		LinkedList<Point> m =new LinkedList<Point>();
		LinkedList<Point> ye =new LinkedList<Point>();
		LinkedList<Point> k =new LinkedList<Point>();
		LinkedList<Point> w =new LinkedList<Point>();
		LinkedList<Point> grey =new LinkedList<Point>();
			
		for (int x =0; x<img.getWidth();x++){
			for (int y =0; y<img.getHeight();y++){
				int clr=  img.getRGB(x,y); 
				int  red   = (clr & 0x00ff0000) >> 16;
				int  green = (clr & 0x0000ff00) >> 8;
				int  blue  =  clr & 0x000000ff;
				
				if(red==255 && green ==0 && blue==0){
					r.add(new Point(x,y));
				}
				if(red==0 && green ==255 && blue==0){
					g.add(new Point(x,y));
				}
				if(red==0 && green ==0 && blue==255){
					b.add(new Point(x,y));
				}
				if(red==0 && green ==255 && blue==255){
					c.add(new Point(x,y));
				}
				if(red==255 && green ==0 && blue==255){
					m.add(new Point(x,y));
				}
				if(red==255 && green ==255 && blue==0){
					ye.add(new Point(x,y));
				}
				if(red==0 && green ==0 && blue==0){
					k.add(new Point(x,y));
				}
				if(red==255 && green ==255 && blue==255){
					w.add(new Point(x,y));
				}
				if(red==128 && green ==128 && blue==128){
					grey.add(new Point(x,y));
				}
				
			}
		}
		for (String[] entry : labelColors){
			if(entry[0].equalsIgnoreCase("r")) map.put(entry[1],r);
			if(entry[0].equalsIgnoreCase("g")) map.put(entry[1],g);
			if(entry[0].equalsIgnoreCase("b")) map.put(entry[1],b);
			if(entry[0].equalsIgnoreCase("c")) map.put(entry[1],c);
			if(entry[0].equalsIgnoreCase("m")) map.put(entry[1],m);
			if(entry[0].equalsIgnoreCase("ye")) map.put(entry[1],ye);
			if(entry[0].equalsIgnoreCase("k")) map.put(entry[1],k);
			if(entry[0].equalsIgnoreCase("w")) map.put(entry[1],w);
			if(entry[0].equalsIgnoreCase("grey")) map.put(entry[1],grey);			
		}
		return map;
	}
	
	/**
	 * Compares validations with the Labels of the current View 
	 * @param anno
	 * @return
	 */
	public static BufferedImage validateLabel (Map<String,LinkedList<Point>> anno, View cv){
		Map<Integer,String> lm = cv.getLabelMap();
		BufferedImage im = new BufferedImage(cv.getXDimension(),cv.getYDimension(), 3);
		ConfusionMatrix cm = new ConfusionMatrix();
		FeatureRole[] fr=cv.getFeatureRoles();
		DoubleMatrix x = new DoubleMatrix(0,0);
		DoubleMatrix y = new DoubleMatrix(0,0);
		Map<Integer,LinkedList<Integer>> validation = new HashMap<Integer,LinkedList<Integer>>();
		 DoubleMatrix label= new DoubleMatrix(0,0);;
		for (int i =0; i<fr.length;i++){
			if(fr[i]==FeatureRole.LABEL)
				label =  new DoubleMatrix(cv.materializeFeature(i));
			if(fr[i]==FeatureRole.X)
				x =  new DoubleMatrix(cv.materializeFeature(i));
			if(fr[i]==FeatureRole.Y)
				y=  new DoubleMatrix(cv.materializeFeature(i));			
		}
		if (label.length==0){
			System.out.println("No Label found in active View");
			return null;			
		}
			
		for (int key : lm.keySet()){
			LinkedList<Integer> result= new LinkedList<Integer>();
			validation.put(key, result);
			String name = lm.get(key).split(" ")[0];
			LinkedList<Point> annos = anno.get(name);
			if (annos!=null){
				for (Point punkt:annos){
					int[] indices = x.eq(punkt.getX()).and(y.eq(punkt.getY())).findIndices();
					int vals = (int) label.get(indices[0]);
					result.add(vals);
					cm.increaseValue(lm.get(vals).split(" ")[0], lm.get(key).split(" ")[0]);
//					System.out.println("Added entry: "+lm.get(vals)+"/"+ lm.get(key));
					if (vals==key){
						
//						System.out.println("Correct");
						im.setRGB((int)punkt.getX(), (int) punkt.getY(), new Color(0, 255, 0).getRGB());
					}
					else{
//						System.out.println("Wrong");
						im.setRGB((int)punkt.getX(), (int) punkt.getY(), new Color(255, 0, 0).getRGB());
					}
				}
			}
			
				
				
		}
		System.out.println(cm);
		
			
		
		
		
		
		
		
		return im;
		
	}
	
	
	
	/** 
	 * Converts a FeatureRole-array into a String-array containing keywords of feature roles.
	 * @param roles array of FeatureRoles
	 * @return array of strings
	 * @author Axel Forsch
	 */
	public static String[] role2string(FeatureRole[] roles) {
		String strings[] = new String[roles.length];
		for (int i = 0; i < roles.length; i++) {
			switch (roles[i]) {
			case LABEL:
				strings[i] = "LABEL";
			case X:
				strings[i] = "X";
			case Y:
				strings[i] = "Y";
			case FEATURE:
				strings[i] = "FEATURE";
			case RGB:
				strings[i] = "RGB";
			case PROB:
				strings[i] = "PROB";
			};
		}
		return strings;
	}
	
	/** 
	 * Converts a String-array containing keywords of feature roles into a FeatureRole-array.
	 * @param strings array of strings
	 * @return array of FeatureRoles
	 * @author Axel Forsch
	 */
	public static FeatureRole[] string2role(String[] strings) {
		FeatureRole roles[] = new FeatureRole[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if ( strings[i].equalsIgnoreCase("label") )
				roles[i] = FeatureRole.LABEL;
			else if ( strings[i].equalsIgnoreCase("x") )
				roles[i] = FeatureRole.X;
			else if ( strings[i].equalsIgnoreCase("y") )
				roles[i] = FeatureRole.Y;
			else if ( strings[i].equalsIgnoreCase("feature") )
				roles[i] = FeatureRole.FEATURE;
			else if ( strings[i].equalsIgnoreCase("rgb") )
				roles[i] = FeatureRole.RGB;
			else if ( strings[i].equalsIgnoreCase("prob") )
				roles[i] = FeatureRole.PROB;
			else {
				System.out.println("Error while converting role, unknown feature role");
				return null;
			}
		}
		return roles;
	}
	
	/**
	 * Converts a JSONArray Object into an String array
	 * @param json JSONArray containing strings
	 * @return String array
	 * @author Axel Forsch
	 */
	public static String[] jsonArray2string(JSONArray json) {
		String[] result = new String[json.length()];
		for ( int i = 0; i < json.length(); i++ ) {
			result[i] = json.getString(i);
		}
		return result;
	}
	
	/**
	 * Counts the number of appearances of each label in this view
	 * @param view View to count labels for
	 * @return Map with number of appearances of each label id
	 * @author Micha Strauch
	 */
	public static Map<Integer, Integer> countLabel(View view){
		Map<Integer, Integer> count = new TreeMap<Integer, Integer>();
		// Find column with labels in view
		int column = -1;
		FeatureRole[] roles = view.getFeatureRoles();
		for(int i = 0; i < roles.length; i++){
			if(FeatureRole.LABEL.equals(roles[i])){
				column = i;
				break;
			}
		}
		if(column < 0) return count;
		double[] labels = view.materializeFeature(column);
		// Count labels
		for(double d : labels){
			int label = (int) d;
			if(count.containsKey(label))
				count.put(label, count.get(label) + 1);
			else
				count.put(label, 1);
		}
		return count;
	}
	
}
