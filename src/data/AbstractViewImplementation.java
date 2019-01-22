package data;

import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AbstractViewImplementation implements View {
	
	// meta 
	private String viewName;
	private String viewDescription;
	protected Image thumbnail;
	protected ArrayList<View> parentViews;
	protected String[] featureDescriptors;
	protected FeatureRole[] featureRoles;
	protected Map<Integer, String> labelMap;
	protected int nColumns = -1;
	protected int nExamples = -1;
	protected double minValue = Double.MAX_VALUE;
	protected double maxValue = Double.MIN_VALUE;
	protected int XDimension = 0;
	protected int YDimension = 0;
	
	//persistence
	private boolean saved = false;
	
	// getters and setters
	public String getViewName() { return viewName; }
	public void setViewName(String name) { this.viewName = Utilities.makeValidViewName(name); }
	
	public String getViewDescription() { return viewDescription; }
	public void setViewDescription(String description) { this.viewDescription = description; }
	
	public Image getThumbnail() { return thumbnail; }
	public void setThumbnail(Image t) { this.thumbnail = t; } 
	
	public List<View> getParentViews() { return parentViews; }
	
	public String[] getFeatureDescriptors() { return featureDescriptors; }
	public void setFeatureDescriptor(int feature, String description) { getFeatureDescriptors()[feature] = description; }
	public void setFeatureDescriptors(String[] descriptors) { featureDescriptors = descriptors; }
	
	public FeatureRole[] getFeatureRoles() { return featureRoles; } 
	public void setFeatureRole(int feature, FeatureRole r) { getFeatureRoles()[feature] = r; }
	public void setFeatureRoles(FeatureRole[] roles) { featureRoles = roles; }


	public double getMinValue() { return minValue;}
	public void setMinValue(double minValue) { this.minValue = minValue;}
	public double getMaxValue() { return maxValue;}
	public void setMaxValue(double maxValue) { this.maxValue = maxValue;}
	
	public int getXDimension() { return XDimension;}
	public void setXDimension(int XDimension) { this.XDimension = XDimension;}
	public int getYDimension() { return YDimension;}
	public void setYDimension(int YDimension) { this.YDimension = YDimension;}
	
	public Map<Integer, String> getLabelMap() {
		if (labelMap == null) {
			labelMap = new HashMap<Integer, String>();
		} 
		return labelMap;
	}
	public void setLabelMap(Map<Integer, String> labelMap){ this.labelMap = labelMap; }
	
	public String getClassName() { return this.getClass().getName(); }
	
	// constructors
	protected AbstractViewImplementation() {
		parentViews = new ArrayList<View>();
	}

	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException {
	
		if (o.has("viewName")) {
			viewName = o.getString("viewName");
		} else {
			throw new IllegalArgumentException("JSON file has no field viewName");
		}
		
		if (o.has("viewDescription")) {
			viewDescription = o.getString("viewDescription");
		} else {
			throw new IllegalArgumentException("JSON file has no field viewDescription");
		}
		
		if (o.has("parentViews")) {
			JSONArray parentArray = o.getJSONArray("parentViews");
			for (int i=0; i<parentArray.length(); ++i) {
				JSONObject p = (JSONObject)parentArray.get(i);
				parentViews.add(Utilities.createViewFromJSON(p));
			}
		} else {
			throw new IllegalArgumentException("JSON file has no field parentViews");
		}
		
		if (o.has("numberOfColumns")) {
			nColumns = o.getInt("numberOfColumns");
		} else {
			throw new IllegalArgumentException("JSON file has no field numberOfColumns");
		}
		
		if (o.has("numberOfExamples")) {
			nExamples = o.getInt("numberOfExamples");
		} else {
			throw new IllegalArgumentException("JSON file has no field numberOfExamples");
		}
		if (o.has("minValue")) {
			minValue = o.getDouble("minValue");
		} else {
			throw new IllegalArgumentException("JSON file has no field minValue");
		}
		if (o.has("maxValue")) {
			maxValue = o.getDouble("maxValue");
		} else {
			throw new IllegalArgumentException("JSON file has no field maxValue");
		}
		if (o.has("XDimension")) {
			XDimension = o.getInt("XDimension");
		} else {
			throw new IllegalArgumentException("JSON file has no field XDimension");
		}
		if (o.has("YDimension")) {
			YDimension = o.getInt("YDimension");
		} else {
			throw new IllegalArgumentException("JSON file has no field YDimension");
		}
		if (o.has("labelMap")) {
			labelMap = new HashMap<Integer, String>();
			JSONObject map = o.getJSONObject("labelMap");
			for (Object key : map.keySet() ) {
				labelMap.put(Integer.parseInt((String)key), map.getString((String)key));
			}
		} else {
			throw new IllegalArgumentException("JSON file has no field labelMap");
		}
		
		if (o.has("featureDescriptors")) {
		JSONArray featureNameArray = o.getJSONArray("featureDescriptors");
		featureDescriptors = new String[featureNameArray.length()];
		for (int i=0; i<featureNameArray.length(); ++i) {
			featureDescriptors[i] = (String)featureNameArray.get(i);
		}
		} else {
			throw new IllegalArgumentException("JSON file has no field featureDescriptors");
		}
		
		if (o.has("featureRoles")) {
			JSONArray featureRoleArray = o.getJSONArray("featureRoles");
			featureRoles = new FeatureRole[featureRoleArray.length()];
			for (int i=0; i<featureRoles.length; ++i) {
				featureRoles[i] = FeatureRole.valueOf(featureRoleArray.getString(i));
			}
		} else {
			throw new IllegalArgumentException("JSON file has no field featureRoles");
		}
	}

	
	// data access
	@Override
	public double[] materializeFeature(int j) {
		return Utilities.materializeFeature(this, j);
	}

	@Override
	public double[][] materializeFeatures(int [] j) {
		return Utilities.materializeFeatures(this, j);
	}
	@Override
	public double[] materializeExample(int i) {
		return Utilities.materializeExample(this, i);
	}

	@Override
	public double [][] materializeExamples(int [] i) {
		return Utilities.materializeExamples(this, i);
	}
	@Override
	public double[][] materialize() {
		return Utilities.materializeMatrix(this);
	}

	@Override
	public double[][] materializeFeatureExamples(int [] i, int[] j) {
		return Utilities.materializeFeatureExamples(this,i,j);
	}
	
	@Override
	public void materializeMinMaxValue() {
		Utilities.materializeMinMaxValue(this);
	}
	
	@Override
	public void materializeXYDimension() {
		Utilities.materializeXYDimension(this);
	}
	
	// persistence
	public boolean isSaved() {
		return saved;
	}
	public void setSaved(boolean saved) {
		this.saved = saved;
	}
	
	@Override
	public void save(File xmlfile) throws IOException {
		JSONObject o = new JSONObject(this);
		BufferedWriter w = new BufferedWriter(new FileWriter(xmlfile));
		w.write(o.toString());
		w.close();
	}

	@Override
	public View saveMaterialized(File xmlfile, File datafile, FileFormat format) throws IOException {
		if (this.getNumberOfExamples()==0 || this.getNumberOfColumns()==0){
			JOptionPane.showMessageDialog(null, "Warning: Empty data","The data "+this.viewName+ " does not contain any samples. Therefore it cannot be stored.",JOptionPane.WARNING_MESSAGE);			
			return null;
		}
		View materialization = format.writeData(this, datafile);
		
		materialization.setViewName(getViewName());
		materialization.setViewDescription(getViewDescription());
		materialization.getParentViews().addAll(getParentViews());
		
		if (materialization instanceof AbstractViewImplementation) {
			((AbstractViewImplementation)materialization).featureRoles = new FeatureRole[getNumberOfColumns()];
			((AbstractViewImplementation)materialization).featureDescriptors = new String[getNumberOfColumns()];
		}
		for (int i=0; i<getNumberOfColumns(); ++i) {
			materialization.setFeatureRole(i, getFeatureRoles()[i]);
			materialization.getFeatureDescriptors()[i] = getFeatureDescriptors()[i];
		}
		
		materialization.setThumbnail(getThumbnail());
		materialization.getLabelMap().putAll(getLabelMap());
		
		materialization.save(xmlfile);
		return materialization;
	}

}