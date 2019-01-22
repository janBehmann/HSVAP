package data.fileformats;

import java.io.*;
import java.util.ArrayList;

import data.*;

/**
 * A header file stores the Feature descriptors and the feature roles in a
 * ".head"-file. 
 * The header is formatted the following: 
 * FeatureDescriptor1 delimiter FeatureRole1 
 * FeatureDescriptor2 delimiter FeatureRole2 
 * ... ... ...
 * 
 * @author Axel Forsch
 * @version 1.0 - 30.07.2015
 */
public class HeaderFile {

	private String delimiter;

	// Feature Names and Roles are stored in this varibles
	ArrayList<FeatureRole> featureRoles = new ArrayList<FeatureRole>();
	ArrayList<String> featureDescriptors = new ArrayList<String>();

	public HeaderFile(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Writes a header file for a given view, storing the FeatureDescriptors and
	 * -Roles
	 * 
	 * @param view
	 *            View to be stored
	 * @param file
	 *            File the data should be stored to
	 * @return true if header was successfully writen, else false
	 * @throws IOException
	 */
	public boolean writeHeader(View view, File file) throws IOException {
		// if view is not empty
		if (view.getNumberOfColumns() != 0) {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));

			// write feature descriptors and roles
			for (int i = 0; i < view.getNumberOfColumns(); i++) {
				out.write(view.getFeatureDescriptors()[i]);
				out.write(delimiter);
				out.write(view.getFeatureRoles()[i].toString());
				out.newLine();
			}
			out.close();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Reads the FeatureDescriptors and -Roles of a new view from a file.
	 * 
	 * @param file
	 *            File to be read
	 * @return true if header was successfully read
	 */
	public boolean readHeader(File file) {
		// BufferedReader to read the file
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(file));

			// As long as there is a new line, read this line
			for (String line = in.readLine(); line != null; line = in
					.readLine()) {
				String[] items = line.split(delimiter);

				items[0].trim();
				featureDescriptors.add(items[0]);

				if (items.length > 1) {
					items[1].trim();
					if (items[1].equals("X"))
						featureRoles.add(FeatureRole.X);
					else if (items[1].equals("Y"))
						featureRoles.add(FeatureRole.Y);
					else if (items[1].equals("Feature"))
						featureRoles.add(FeatureRole.FEATURE);
					else if (items[1].equals("Label"))
						featureRoles.add(FeatureRole.LABEL);
					else if (items[1].equals("Prob"))
						featureRoles.add(FeatureRole.PROB);
					else if (items[1].equals("RGB"))
						featureRoles.add(FeatureRole.RGB);
					else
						System.out.println("Feature role not supported!");
				} else {
					featureRoles.add(null);
				}
			}

			// Test, delete pls
			System.out.println("Roles:");
			for (FeatureRole r : featureRoles) {
				System.out.println(r.toString());
			}
			System.out.println("Names:");
			for (String n : featureDescriptors) {
				System.out.println(n);
			}

			// Closing of the reader
			in.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getClassName() {
		return this.getClass().getName();
	}

	public FeatureRole[] getFeatureRoles() {
		return featureRoles.toArray(new FeatureRole[featureRoles.size()]);
	}

	public String[] getFeatureDescriptors() {
		return featureDescriptors
				.toArray(new String[featureDescriptors.size()]);
	}
}
