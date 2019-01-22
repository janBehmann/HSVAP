package data.fileformats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import data.FileFormat;
import data.Utilities;
import data.View;
import data.inmemory.ArrayListView;

public class CSVFormat implements FileFormat {

	private String delimiter;
	private boolean firstRowIsDescription;

	public CSVFormat(String delimiter, boolean firstRowIsDescription) {
		this.delimiter = delimiter;
		this.firstRowIsDescription = firstRowIsDescription;
	}
	
	public CSVFormat() { delimiter = " "; }
	
	@Override
	public void initializeValues(JSONObject o) { 
		firstRowIsDescription = o.getBoolean("firstRowIsDescription");
		delimiter = o.getString("delimiter");
	}

	@Override
	public View writeData(View view, File file) throws IOException {
		// if the view is not empty
		if (view.getNumberOfColumns() != 0) {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			// write feature descriptors, if you wish
			if (firstRowIsDescription) {
				for (int i=0; i<view.getNumberOfColumns(); ++i) {
					for (int j=0; j<view.getFeatureDescriptors().length - 1; ++j) {
						out.write(view.getFeatureDescriptors()[j]);
						out.write(delimiter);
					}
					out.write(view.getFeatureDescriptors()[view.getNumberOfColumns() - 1]);
					out.newLine();
				}
			}
			
			// write data
			for (int i=0; i<view.getNumberOfExamples(); ++i) {
				double[] example = view.materializeExample(i);
				for (int j=0; j<example.length - 1; ++j) {
					out.write(Double.toString(example[j]));
					out.write(delimiter);
				}
				out.write(Double.toString(example[example.length - 1]));
				out.newLine();
			}
			out.close();
			return new FileView(this, file, view.getNumberOfExamples(), view.getNumberOfColumns(), view.getFeatureRoles(), view.getFeatureDescriptors());
		} else {
			return null;
		}
	}

	@Override
	public View readData(File file) throws IOException {
		ArrayList<double[]> data = new ArrayList<double[]>();
		String[] featureDescriptors = null;
		
		// read data and maybe feature descriptors
		BufferedReader in = new BufferedReader(new FileReader(file));		
		if (firstRowIsDescription) {
			featureDescriptors = in.readLine().split(delimiter);
		}
		for (String line=in.readLine(); line!=null; line=in.readLine()) {
			String[] items = line.split(delimiter);
			double[] values = new double[items.length];
			for (int i=0; i<items.length; ++i) {
				values[i] = Double.parseDouble(items[i]);
			}
			data.add(values);
		}
		in.close();
		
		// write meta data
		View result = new ArrayListView(data, file.getName());
		if (firstRowIsDescription) {
			result.setFeatureDescriptors(featureDescriptors);
		} else {
			result.setFeatureDescriptors(Utilities.createGenericFeatureNames(result.getNumberOfColumns()));
		}
		result.setFeatureRoles(Utilities.createFeatureRoleArray(result.getNumberOfColumns()));
		
		return result;
	}

	@Override
	public String getInformation() { return "CSV Format"; }
	@Override
	public String getClassName() { return this.getClass().getName(); }
	public String getDelimiter() { return delimiter; }
	public boolean getFirstRowIsDescription() { return firstRowIsDescription; }

	
}
