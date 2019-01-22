package data.fileformats;

import java.io.*;
import java.util.ArrayList;

import org.json.JSONObject;

import data.*;
import data.inmemory.ArrayListView;

/**
 * FileFormat to load and store data in ".txt" files. Those files must always be
 * paired with a header file (".head"), as the header stores FeatureRoles and
 * -Descriptors and the text file stores the data. 
 * Important is, that data and
 * header file need to have the same name except for the extension (e.g.:
 * "Text.txt" and "Test.head") 
 * The text files are formatted the following: -
 * Examples in rows - Features in columns
 * 
 * @author Axel Forsch
 * @version 1.0 - 30.07.2015
 */
public class TextFormat implements FileFormat {

	private String delimiter;

	public TextFormat(String delimiter) {
		this.delimiter = delimiter;
	}

	public TextFormat() {
		delimiter = " ";
	}

	@Override
	public void initializeValues(JSONObject o) {
		delimiter = o.getString("delimiter");
	}

	/**
	 * This method writes the data (".txt") and the header (".head") file for
	 * the selected view.
	 * 
	 * @param view
	 *            View to be stored
	 * @param file
	 *            File of the datafile
	 * @return new View
	 * @throws IOException
	 */
	@Override
	public View writeData(View view, File file) throws IOException {
		if (view.getNumberOfColumns() > 0) {
			// Write the data file
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (int i = 0; i < view.getNumberOfExamples(); i++) {
				double[] example = view.materializeExample(i);
				for (int j = 0; j < example.length - 1; j++) {
					out.write(Double.toString(example[j]));
					out.write(delimiter);
				}
				out.write(Double.toString(example[example.length - 1]));
				out.newLine();
			}
			out.close();
			return new FileView(this, file, view.getNumberOfExamples(),
					view.getNumberOfColumns(), view.getFeatureRoles(),
					view.getFeatureDescriptors());
		}
		return null;
	}

	/**
	 * Reads the data from a file.
	 * 
	 * @param file
	 *            File to be read.
	 * @return New view
	 * @throws IOException
	 */
	@Override
	public View readData(File file) throws IOException {
		ArrayList<double[]> data = new ArrayList<double[]>();

		// read data and maybe feature descriptors
		BufferedReader in = new BufferedReader(new FileReader(file));

		for (String line = in.readLine(); line != null; line = in.readLine()) {
			String[] items = line.split(delimiter);
			double[] values = new double[items.length];
			for (int i = 0; i < items.length; ++i) {
				values[i] = Double.parseDouble(items[i]);
			}
			data.add(values);
		}
		in.close();

		// write meta data
		View result = new ArrayListView(data, file.getName());
		in = new BufferedReader(new FileReader(file.getAbsolutePath().replaceFirst("[.][^.]+$", "")+".json"));
		JSONObject o = new JSONObject(in.readLine());
		String[] featureDescriptors = Utilities.jsonArray2string(o.getJSONArray("featureDescriptors"));
		FeatureRole[] featureRoles = Utilities.string2role(Utilities.jsonArray2string(o.getJSONArray("featureRoles")));
		
		result.setFeatureDescriptors(featureDescriptors);
		result.setFeatureRoles(featureRoles);

		return result;
	}

	@Override
	public String getInformation() {
		return "Text Format";
	}

	@Override
	public String getClassName() {
		return this.getClass().getName();
	}

	public String getDelimiter() {
		return delimiter;
	}
}
