package data;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * Views can be saved to disk as a JSON file.
 *  
 * To be able to do this automatically, a view must implement this interface, must 
 * provide a zero-argument constructor and a public get... method for each of its 
 * fields it wants to store. Sadly, the last two requirements cannot be imposed by
 * this interface.
 * 
 * How does saving and loading a view work? I start by explaining the loading part.
 * 
 * Utilities.createViewFromJSON() reads a given JSON file, searches for the field 
 * named className and tries to create an instance of a class of that name using the
 * zero-argument constructor. Subsequently, the method calls initializeValues() of that 
 * instance. initializeValues() has to read all interesting field values from the
 * given JSONObject and to store these in the instance variables.
 * 
 * If your View inherits from AbstractViewImplementation, your initializeValues() 
 * method should start like:
 * 
 * public void initializeValues(JSONObject o) {
 *     super.initializeValues(o);
 *     ...
 * }    
 * 
 * , as AbstractViewImplementation itself has such a method, which reads the values 
 * that are managed by this class (like parents, name, description).
 * 
 * Now that you know how to load a class from JSON, you need to know, how to save it.
 * 
 * This is done by AbstractViewImplementation.save() , which uses the reference 
 * JSON-java implementation from http://json.org/java/ . This particular pice of code
 * makes a json string from a java object by inspecting its public methods, storing
 * the non-null results of all public methods that have a name starting with get .
 * Thus, if you want your instance to store the value of a certain instance variable wurstSalat,
 * there has to be a public get method getWurstSalat() and in your json, you will find
 * the field wurstSalat (Note that capitalization of first letter changes).
 * 
 * As an example, consider the following view that specifies a private variable to 
 * store an integer value:
 * 
 * public class MyView implements AbstractViewImplementation {
 *     private int data;
 *     
 *     public MyView();
 *     
 *     public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 *         super.initializeValues(o);
 *         this.data = o.getInt("data");
 *     }
 *     
 *     public int getData() { return data; }
 * }
 * 
 * As described above, the view has to implement a no-argument constructor and a getData()
 * method that returns the value of data. It furthermore needs to implement initializeValues()
 * that calls the the initializeValues method in AbstractViewImplementation.
 * 
 * @author pwelke
 */
public interface View extends DoubleMatrixInterface {
	
	// meta methods
	public String getViewName();
	public void setViewName(String n);
	
	public String getViewDescription();
	public void setViewDescription(String description);
	
	public Image getThumbnail();
	public void setThumbnail(Image t);
	
	public List<View> getParentViews();
	
	public String[] getFeatureDescriptors();
	public void setFeatureDescriptor(int feature, String descriptor);
	public void setFeatureDescriptors(String[] descriptors);
	
	public FeatureRole[] getFeatureRoles();
	public void setFeatureRole(int feature, FeatureRole role);
	public void setFeatureRoles(FeatureRole[] roles);
	
	public double getMinValue();
	public void setMinValue(double minValue);
	public double getMaxValue();
	public void setMaxValue(double maxValue);
	
	public int getXDimension();
	public void setXDimension(int XDimension);
	public int getYDimension();
	public void setYDimension(int YDimension);
	
	public Map<Integer, String> getLabelMap();
	public void setLabelMap(Map<Integer, String> labelMap);
	
	
	// persistence
	public boolean isSaved();
	public void setSaved(boolean saved);
	
	/**
	 * Save the view without materializing the data. That is: 
	 * An xml file will be created that stores all meta information.
	 * 
	 * At runtime, when you want to use values in this view, this information is
	 * sufficient to find these values in the views or underlying datasets that this
	 * view consists of.
	 * 
	 * @param file
	 */
	public void save(File xmlfile) throws IOException;
	
	/**
	 * Save the view materialized. That means, that all values that you can see in
	 * this view will be written to a file that is specified by datafile and is of 
	 * format specified by fileformat.
	 * An xml file is created that stores a view that points directly to this datafile.
	 * 
	 * This is mainly for speed reasons, as at some point, when a view consists of
	 * a very large tree, lots of data have to be read, filtered, merged and so on.
	 * 
	 * @param file
	 * @param format
	 */
	public View saveMaterialized(File xmlfile, File datafile, FileFormat format) throws IOException;
	
	/**
	 * Each implementing view must initialize its private variables using this method.
	 * 
	 * This method is invoked when a view is loaded from a JSON file. 
	 * 
	 * @param o
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException;
	
}
