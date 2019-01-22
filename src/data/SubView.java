package data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import data.inmemory.DoubleArrayView;

/**
 * SubView which contains only values with a specified id of the parent view<br>
 * Used to further classify an already classified view
 * @author Micha Strauch
 */
public class SubView extends AbstractViewImplementation {
	/** List with rows belonging to each label ID */
	private final Map<Integer, int[]> exampleList; 
	/** Parent view or modified view, if {@link #apply(View)}} was called */
	private View view;
	/** Rows of currently selected label ID */
	private int[] examples;
	
	/**
	 * Creates a new SubView of the given view with given label ID preselected
	 */
	public SubView(View view, int label) {
		this.view = view;
		this.exampleList = new TreeMap<Integer, int[]>();
		
		buildExampleList();
		selectLabel(label);
		
		setViewName(view.getViewName());
		setViewDescription(view.getViewDescription());
		setLabelMap(new HashMap<Integer, String>(view.getLabelMap()));
		setFeatureRoles(view.getFeatureRoles().clone());
		setFeatureDescriptors(view.getFeatureDescriptors().clone());
	}
	
	/**
	 * Builds the example List
	 */
	private void buildExampleList(){
			// Retrieve label column
		FeatureRole[] roles = view.getFeatureRoles();
		int i;
		for(i = 0; i < roles.length && roles[i] != FeatureRole.LABEL; i++);
		double[] ex = view.materializeFeature(i);
			// Map examples to their label
		Map<Integer, List<Integer>> labelMap = new HashMap<Integer, List<Integer>>();
		for(i = 0; i < ex.length; i++){
			int label = (int)ex[i];
			if(!labelMap.containsKey(label))
				labelMap.put(label, new LinkedList<Integer>());
			labelMap.get(label).add(i);
		}
			// Cast lists into arrays
		for(Entry<Integer, List<Integer>> label : labelMap.entrySet()) {
			int[] examples = new int[label.getValue().size()];
			i = 0;
			for(int example : label.getValue())
				examples[i++] = example;
			exampleList.put(label.getKey(), examples);
		}
	}
	
	/** Selects the given label */
	public void selectLabel(int label){
		if(exampleList.containsKey(label))
			examples = exampleList.get(label);
		else
			examples = new int[0];
	}

	@Override
	public int getNumberOfColumns() {
		return view.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return examples.length;
	}

	@Override
	public double get(int i, int j) {
		return view.get(examples[i], j);
	}
	
	/**
	 * Returns the complete view<br>
	 * Parent view or modified view, if {@link #apply(View)} was called
	 */
	public View getCompleteView(){
		return view;
	}
	
	/**
	 * Applies the values of the given View to the currently selected label ID 
	 */
	public void apply(View v){
		if(v.getNumberOfColumns() != view.getNumberOfColumns()) throw new RuntimeException("Number of columns don't match");
		if(v.getNumberOfExamples() != examples.length) throw new RuntimeException("Number of examples don't match");
		
		// Apply values:
		double[][] mat = view.materialize();
		double[][] matNew = v.materialize();
		for(int i = 0; i < matNew.length; i++)
			for(int j = 0; j < matNew[i].length; j++)
				mat[examples[i]][j] = matNew[i][j];
		view = new DoubleArrayView(mat, v.getViewName());
		// Apply LabelMap:
		Map<Integer, String> newLabelMap = new HashMap<Integer, String>(getLabelMap());
		newLabelMap.putAll(v.getLabelMap());
		view.setLabelMap(newLabelMap);
		// Apply other properties:
		view.setViewDescription(v.getViewDescription());
		view.setFeatureRoles(getFeatureRoles().clone());
		view.setFeatureDescriptors(getFeatureDescriptors().clone());
	}

}
