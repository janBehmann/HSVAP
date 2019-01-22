package data.operators;

import java.io.File;
import java.io.IOException;

import org.jblas.DoubleMatrix;
import org.json.JSONObject;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.Utilities;
import data.View;
import data.fileformats.MatlabFormat;
import data.inmemory.DoubleMatrixView;



/**
 * Adds a column of labels as the first column to a view
 * @author schubert, heavy augmentations 2013-09-30 pwelke
 * 
 *
 */
public class AddLabelsView extends AbstractViewImplementation {

	View labels;
	View v;

	public AddLabelsView(double[] labels, View view, String path) throws IllegalArgumentException, IOException {


		this.labels = new DoubleMatrixView(new DoubleMatrix(labels), view.getViewName()+"_labels").saveMaterialized(new File(path,view.getViewName()+"_labels.json"), new File(path,view.getViewName()+"_labels.mat"), new MatlabFormat());

		this.v = view;

		setViewName(view.getViewName() + "_labeled");
		setViewDescription(view.getViewName()+"_labeled");
		setThumbnail(view.getThumbnail());
		parentViews.add(view);
		parentViews.add(this.labels);

		featureDescriptors = new String[view.getNumberOfColumns() + 1];
		featureRoles = new FeatureRole[view.getNumberOfColumns() + 1];
		featureDescriptors[0] = "Label";
		featureRoles[0] = FeatureRole.LABEL;
		for (int i=0; i<view.getNumberOfColumns(); ++i) {
			featureDescriptors[i+1] = view.getFeatureDescriptors()[i];
			featureRoles[i+1] = view.getFeatureRoles()[i];
		}
		getLabelMap().putAll(view.getLabelMap());
	}


	public AddLabelsView(double[] labels,  View view, String labeledViewName, String labelsViewName, String path) throws IllegalArgumentException, IOException {

		this.labels = new DoubleMatrixView(new DoubleMatrix(labels), labelsViewName).saveMaterialized(new File(path,labelsViewName+".json"), new File(path,labelsViewName+".mat"), new MatlabFormat());
		this.v = view;

		setViewName(labeledViewName);
		setViewDescription(labeledViewName);
		setThumbnail(view.getThumbnail());
		parentViews.add(view);
		parentViews.add(this.labels);

		featureDescriptors = new String[view.getNumberOfColumns() + 1];
		featureRoles = new FeatureRole[view.getNumberOfColumns() + 1];
		featureDescriptors[0] = "Label";
		featureRoles[0] = FeatureRole.LABEL;
		for (int i=0; i<view.getNumberOfColumns(); ++i) {
			featureDescriptors[i+1] = view.getFeatureDescriptors()[i];
			featureRoles[i+1] = view.getFeatureRoles()[i];
		}
		getLabelMap().putAll(view.getLabelMap());
	}
	public AddLabelsView() {}
	public void initializeValues(JSONObject o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		super.initializeValues(o);
		if (o.has("parent")) {
			v = Utilities.createViewFromJSON(o.getJSONObject("parent"));
			labels = Utilities.createViewFromJSON(o.getJSONObject("labels"));
		} else {
			throw new IllegalArgumentException("JSON file has no field parent");
		}
	}

	public View getLabels() { return labels; }
	public View getParent() { return v; }


	@Override
	public int getNumberOfColumns() {
		return 1 + v.getNumberOfColumns();
	}

	@Override
	public int getNumberOfExamples() {
		return v.getNumberOfExamples();
	}
	@Override
	public double getMinValue() {
		return v.getMinValue();
	}

	@Override
	public double getMaxValue() {
		return v.getMaxValue();
	}
	
	@Override
	public int getXDimension() {
		return v.getXDimension();
	}
	@Override
	public int getYDimension() {
		return v.getYDimension();
	}
	
	@Override
	public double get(int i, int j) {
		if (j == 0) {
			return labels.get(i, j);
		} else {
			return v.get(i, j - labels.getNumberOfColumns());
		}
	}

	public void setLabelAt(int[] indices, double[] newLbl, String path) throws IOException{
		double[] labels = new double[this.labels.getNumberOfExamples()];
		for(int i=0; i<this.labels.getNumberOfExamples(); i++){
			labels[i] = this.labels.get(i, 0);
		}
		for(int i=0; i<indices.length; i++){
			labels[indices[i]] = newLbl[i];
		}
		this.labels = new DoubleMatrixView(new DoubleMatrix(labels), "New Labels").saveMaterialized(new File(path,this.getViewName()+"_newlabels.json"), new File(path,this.getViewName()+"_newlabels.mat"), new MatlabFormat());
	}
}

