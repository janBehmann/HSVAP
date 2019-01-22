package data;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

public interface FileFormat {
	
	/**
	 * Write data that is visible in view to file in the format of choice.
	 * Return a (new!) View that points to that file.
	 * 
	 * @param view
	 * @param file
	 * @return
	 */
	public View writeData(View view, File file) throws IOException;
	
	/**
	 * Return a queryable View of file.
	 * That is, a view that can answer get(i,j) for all i,j in the proper ranges.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public View readData(File file) throws IOException;

	/**
	 * Return a short description of the file format.
	 * @return
	 */
	public String getInformation();
	
	/**
	 * Has to be 
	 * 
	 * public String getClassName() { return this.getClass().getName(); }
	 * 
	 * for all implementing classes.
	 * 
	 * @return
	 */
	public String getClassName();
	
	/**
	 * given a json containing information of the field values
	 * of an object of this class, initialize these fields with
	 * values from the json after the zero-argument constructor was invoked.
	 * @param o
	 */
	public void initializeValues(JSONObject o);
	
}
