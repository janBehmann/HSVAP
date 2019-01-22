package models;

import java.io.IOException;

import org.jblas.exceptions.SizeException;

import data.View;

/**
 * 
 * This interface ensures that every model has the methods applyOn and getMetaInfo.
 * The methods are defined for/in each model individually.
 * 
 * @author Till
 *
 */
public interface ModelInterface {
	
	/**
	 * 
	 * @param v: View the model will be applied on
	 * @param path: The data path where new materialization data will be saved (this is the DATAPATH in the directory.properties file: e.g. getMaster().getDirectoryProp().getProperty("DATA_PATH"))
	 * @return view after applying model
	 * @throws SizeException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws Error
	 */
	public View applyOn(View v, String path) throws SizeException, IllegalArgumentException, IOException, Error;

	/**
	 * 
	 * @param detailed: boolean whether the information is detailed or not
	 * @return String of different meta information
	 */
	public String getMetaInfo(boolean detailed);
	
}
