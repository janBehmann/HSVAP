package data.fileformats;

import gui.MainGui;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.FileFormat;
import data.Utilities;
import data.View;

/**
 * A FileView represents a hyperspectral image that is stored in a file.
 * 
 * The file may be of various formats. Each file format (e.g. matlab file, or csv file)
 * is represented by a class implementing the interface FileFormat.
 * 
 * The FileFormat is responsible for providing the data as a view (e.g. load it to a 
 * jblas.DoubleMatrix and wrapping it in a DoubleMatrixView), which then in turn is 
 * referenced by the FileView class. 
 * 
 * @author pwelke
 *
 */
public class FileView extends AbstractViewImplementation {

	private File file;
	private FileFormat format;

	private View loadedData = null;

	
	/**
	 * Create a view from file. The Contents of the file are loaded to memory, as this is necessary
	 * to determine the meta data.
	 * @param format
	 * @param file
	 */
	public FileView(FileFormat format, File file) {
		this.format = format;
		this.file = file;
		
		loadData();
		
		setViewName(file.getAbsolutePath());
		setViewDescription(format.getInformation() + " file at position " + file);
	}
	
	/**
	 * If the metadata of the file at hand is known, you can create a FileView with that meta data without
	 * loading the file to memory. This comes in handy, when loading Views from JSON files.
	 * 
	 * 
	 * Note that in this case, any meta data provided by the File (e.g. if its a MatlabFormat file) 
	 * is overwritten by the provided meta data.
	 * @param format
	 * @param file
	 * @param nExamples
	 * @param nColumns
	 * @param featureRoles
	 * @param featureDescriptors
	 */
	public FileView(FileFormat format, File file, int nExamples, int nColumns, FeatureRole[] featureRoles, String[] featureDescriptors) {
		this.format = format;
		this.file = file;
		
		this.nExamples = nExamples;
		this.nColumns = nColumns;
		
		setViewName(file.getAbsolutePath());
		setViewDescription(format.getInformation() + " file at position " + file);
		
		this.featureDescriptors = featureDescriptors;
		this.featureRoles = featureRoles;	
	}


	public FileView() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("fileFormat")) {
			format = Utilities.createFileFormatFromJSON(o.getJSONObject("fileFormat"));
		} else {
			throw new IllegalArgumentException("JSON file has no field fileFormat");
		}
		if (o.has("dataFile")) {
			file = new File(o.getString("dataFile"));
		} else {
			throw new IllegalArgumentException("JSON file has no field dataFile");
		}
	}

	@Override
	public int getNumberOfColumns() { 
		if (nColumns == -1) {
			loadData();
		}
		return nColumns; 
	}
	@Override
	public int getNumberOfExamples() { 
		if (nExamples == -1) {
			loadData();
		}
		return nExamples; 
	}
	public File getDataFile() { return file; }
	public FileFormat getFileFormat() { return format; } 

	@Override
	public double get(int i, int j) throws NullPointerException {
		if (loadedData!=null){
			return loadedData.get(i,j);
		} else {
			loadData();
			if(loadedData == null){
				throw new Error("Data lost.");
			}
			return loadedData.get(i, j);
		}
	}

	private void loadData() {
		try {
			if (loadedData == null) {
							
//				MainGui.stopThreads();

				System.out.println("Loading contents of file " + file + " to memory");
				Logger.getLogger(MainGui.class.getName()).info("Loading contents of file " + file + " to memory");
				
				loadedData = format.readData(file);
				if (loadedData ==null)
					return;
//				MainGui.resumeThreads();
				
				// overwrite meta data if not already set (e.g. by user or loaded from json)
				if (nColumns == -1) {
					nColumns = loadedData.getNumberOfColumns();
				}
				if (nExamples == -1) {
					nExamples = loadedData.getNumberOfExamples();
				}	
				if (true) { //Always use new Feature Descriptors instead getFeatureDescriptors() == null
					setFeatureDescriptors(loadedData.getFeatureDescriptors());
				}
				if (true) { //Always use new feature Roles instead getFeatureRoles() == null
					setFeatureRoles(loadedData.getFeatureRoles());
				}
				this.setLabelMap(loadedData.getLabelMap());
				
			}
		} catch (IOException e) {
			System.out.println("Could not load file " + file);
			e.printStackTrace();
		}
	}
}
