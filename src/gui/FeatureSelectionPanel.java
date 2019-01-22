package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import models.FeatureWeightsModel;
import net.miginfocom.swing.MigLayout;

import org.jblas.DoubleMatrix;
import org.math.plot.Plot2DPanel;
import org.math.plot.components.LegendPanel;
import org.math.plot.components.LegendPanel.Legend;
import org.math.plot.plotObjects.BaseLabel;
import org.math.plot.plots.LinePlot;
import org.math.plot.plots.Plot;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.attributeSelection.SVMAttributeEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import wekatools.WekaTransformer;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.operators.TrainView;
import feature_selection.DataSelection;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 * @author Marvin Diehl
 *
 */
public class FeatureSelectionPanel extends JPanel {

	private static final long serialVersionUID = 3734187467180476744L;
	
	private JButton jbCompute;
	private JPanel buttonPanel;

	private JComboBox<String> jcobAlgorithmenList = new JComboBox<String>(new String[] {"Relieff","InfoGain","SVM","Correlation Matrix"});
	
	private Plot2DPanel plotPanel;
	private Plot2DPanel plotSpectrenPanel;
	
	private JPanel jpPlotGrid;
	//saves the results of feature selection within a double array
	private double[] y_val;

	private JCheckBox jcbNorm;

	private JPanel jpPlotPanel;

	private JPanel jpWestCFPData;
	private JPanel jpSelectedFeaturePanel;
	private JRadioButton jrbSelectMeanData;
	private JRadioButton jrbSelectStdData;
	private JRadioButton jrbSelectAllData;
	private JRadioButton jrbSelectVarData;
	private JRadioButton jrbSelectFilter;
	private JPanel jpSelectDataPanel;	
	int corrPxlSize;
	private MainGui master;

	private JButton jbSaveModelWeights;

	private FeatureWeightsModel fwm;
	
	private View lastDisplayed;
	private View featureView;

	private JButton legendToggleButton;

	private JButton legendToggleButtonSpectra;

	/**
	 * Constructor
	 * initialize Buttons and their ActionListeners
	 * @param main
	 */

	public FeatureSelectionPanel(MainGui main) {

		this.master = main;
		this.setLayout(new BorderLayout());
	
		/*
		 *  computeButton starts data filtering
		 */
		jbCompute = new JButton("Apply Algorithm");
		jbCompute.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {

				// Filterung mit einem der Filter aus weka
				if (jcobAlgorithmenList.getSelectedItem().toString()=="Correlation Matrix"){
					showCorrMatrix();
				} else {			
					weightBy(jcobAlgorithmenList.getSelectedItem().toString());
					
					jbSaveModelWeights.setEnabled(true);
					
				}				
				
			}
		});
		//initializes tool tips for algorithms in jcobAlgorithmenList
		initAlgorithmToolTip();
		// normalized weights to [0,1]
		jcbNorm = new JCheckBox("norm weights");
		jcbNorm.setToolTipText("If checked, the resulting weights will be normalized to [0,1]");
		jcbNorm.setSelected(true);


		/*
		 *  buttonPanel befindet sich ganz oben �ber die gesamte Breite und beinhaltet die Button
		 *  loadData, computeButton, sowie die Liste der Filter und die CheckBoxen normZ und norm
		 */
		buttonPanel = new JPanel();
//		buttonPanel.setLayout(new GridLayout(1,3));
		buttonPanel.setLayout(new MigLayout());
		buttonPanel.add(jbCompute, "width 80:130");
		buttonPanel.add(jcobAlgorithmenList, "gapleft 8, width 130:170");
		buttonPanel.add(jcbNorm, "gapleft 10");

		jbSaveModelWeights = new JButton("save weights-model");
		jbSaveModelWeights.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				master.getDataManagementPanel().getModels().add(fwm);
				master.getDataManagementPanel().actualiseListen();

			}
		});
		buttonPanel.add(jbSaveModelWeights, "gapleft 10");
		
		jbSaveModelWeights.setEnabled(false);

		// weightPanel (ganz rechts) zeigt die Liste der Gewichte und die Sortierbuttons an
		JLabel weightHeader = new JLabel("  weights:");
		weightHeader.setPreferredSize(new Dimension(107,31));
		
		// selectedFeaturePanel linker Bereich in dem �ber Buttons eingestellt werden kann, 
		// welche Daten angezeigt werden sollen und im unteren Teil (westCFPData) k�nnen die 
		// ausgew�hlten Wellenl�ngenbereiche gespeichert werden
		jpSelectedFeaturePanel = new JPanel();
//		jpSelectedFeaturePanel.setLayout(new GridLayout(2,1));
		jpSelectedFeaturePanel.setLayout(new MigLayout());
		jpWestCFPData = new JPanel();
		jpWestCFPData.setLayout(new BorderLayout());
		jpWestCFPData.add(new JLabel(" show data :       "),BorderLayout.NORTH);
		jpSelectedFeaturePanel.add(jpWestCFPData, "wrap");
		

		/*
		 * selectAllData alle Daten darstellen
		 */
		jrbSelectAllData = new JRadioButton("all");
		jrbSelectAllData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				plotStatistics();
			}
		});
		jpSelectDataPanel = new JPanel();
		jpSelectDataPanel.setLayout(new GridLayout(0,1));
		jpSelectDataPanel.add(jrbSelectAllData);

		/*
		 * selectMeanData Mittelwerte der Klassen darstellen
		 */
		jrbSelectMeanData = new JRadioButton("mean");
		jrbSelectMeanData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				plotStatistics();
			}
		});
		
		jpSelectDataPanel.add(jrbSelectMeanData);

		/*
		 * selectStdData Standardabweichung der Klassen darstellen
		 */
		jrbSelectStdData = new JRadioButton("std");
		jrbSelectStdData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				plotStatistics();
			}
		});		
		jpSelectDataPanel.add(jrbSelectStdData);

		/*
		 * selectVarData Varianz der Klassen darstellen
		 */
		jrbSelectVarData = new JRadioButton("var");
		jrbSelectVarData.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				plotStatistics();
			}
		});
		jpSelectDataPanel.add(jrbSelectVarData);

		/*
		 * selectFilter Filterergebnis darstellen
		 */
		jrbSelectFilter = new JRadioButton("filter result");
		jrbSelectFilter.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if(jrbSelectFilter.isSelected()){
					if (y_val!=null){
						plotPanel.removeAllPlots();
						DoubleMatrix wPlotData = new DoubleMatrix(1,y_val.length);
						for(int i=0;i<y_val.length;i++)
							wPlotData.put(0,i,y_val[i]);
						plotData2D(plotPanel, wPlotData, true, 2);
					
	
						plotPanel.removeAllPlotables();
						BaseLabel title = new BaseLabel("Filter Result - " + jcobAlgorithmenList.getSelectedItem().toString(), new java.awt.Color(0,66,145), 0.5, 1.1);
						title.setFont(new Font("Arial", Font.BOLD, 15));
						plotPanel.addPlotable(title);
					}
				}
				else{
					jrbSelectFilter.setSelected(true);
				}
			}
		});
		jpSelectDataPanel.add(jrbSelectFilter);

		jpWestCFPData.add(jpSelectDataPanel,BorderLayout.CENTER);
		
		JButton approxButton = new JButton("<html><center>Polynomial<br>Approximation</center><html>");
		approxButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				new ApproximationWindow(true,getThis(),featureView);
			}
		});
		jpWestCFPData.add(approxButton,BorderLayout.SOUTH);
		
		// plot for data (upper plot)
		plotSpectrenPanel = new Plot2DPanel();
		plotSpectrenPanel.setFixedBounds(0, 0, 1);
		plotSpectrenPanel.setFixedBounds(1, 0, 1);
		plotSpectrenPanel.plotToolBar.add(getLegendToggleButtonSpectra());
		
		
		// plot for filter result (lower plot)
		plotPanel = new Plot2DPanel();
		plotPanel.setFixedBounds(0, 0, 1);
		plotPanel.setFixedBounds(1, 0, 1);
		plotPanel.plotToolBar.add(getLegendToggleButton());
		// main panel for the two plots
		jpPlotGrid = new JPanel(new GridLayout(2,1));

		// create a panel for the legend
		JPanel plotInGridO = new JPanel(new BorderLayout());
	
		// add panel to the main plot-panel
		
		plotInGridO.add(plotSpectrenPanel,BorderLayout.CENTER);
		plotInGridO.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		jpPlotGrid.add(plotInGridO);

		// create a panel for the legend
		JPanel plotInGridU = new JPanel(new BorderLayout());

		// add panel to the main plot-panel
		
		plotInGridU.add(plotPanel,BorderLayout.CENTER);
		plotInGridU.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		jpPlotGrid.add(plotInGridU);

		// Feature Selection Panel
		jpPlotPanel = new JPanel(new BorderLayout());
		jpPlotPanel.add(jpPlotGrid,BorderLayout.CENTER);
	
		jpPlotPanel.add(jpSelectedFeaturePanel,BorderLayout.WEST);

		// Frame
		this.add(buttonPanel,BorderLayout.NORTH);
		this.add(jpPlotPanel,BorderLayout.CENTER);
		

		//		MatrixStorage matrixStorage = master.getMatrixStorage();
		ButtonGroup selectMenu = new ButtonGroup();
		selectMenu.add(jrbSelectMeanData);
		selectMenu.add(jrbSelectAllData);
		selectMenu.add(jrbSelectStdData);
		selectMenu.add(jrbSelectVarData);
		
		plotPanel.plotLegend.setVisible(false);
		plotSpectrenPanel.plotLegend.setVisible(false);
	}
	
	private FeatureSelectionPanel getThis() {
		return this;
	}
	
	public MainGui getMaster(){
		return master;
	}
	
	/**
	 * initialized plotting of all data, mean, std and var of the features in the upper plot panel
	 * checks which radio button is selected in order to decide which statistic must be calculated
	 * 
	 * catches a null pointer exception if no data is loaded
	 * 
	 * @author: croemer
	 */
	private void plotStatistics(){
		try{
			this.plotSpectrenPanel.removeAllPlots();
			BaseLabel title = new BaseLabel(null,null,null);
			if (this.jrbSelectAllData.isSelected()){
				plotData2D(this.plotSpectrenPanel, Utilities.materializeAsDoubleMatrix(this.featureView).getColumns(FeatureSelection.getNonSpecialFeatures(featureView)), true,0);
				title = new BaseLabel("All Data", new java.awt.Color(0,66,145), 0.5, 1.1);
			}
			
			if (this.jrbSelectStdData.isSelected()){
				plotData2D(this.plotSpectrenPanel, DataSelection.std(this.featureView), true,1);
				title = new BaseLabel("Standard Deviation", new java.awt.Color(0,66,145), 0.5, 1.1);
			}
			
			if (this.jrbSelectMeanData.isSelected()){
				plotData2D(this.plotSpectrenPanel, DataSelection.meanOfClasses(this.featureView), true,1);
				title = new BaseLabel("Mean", new java.awt.Color(0,66,145), 0.5, 1.1);
			}
			
			if (this.jrbSelectVarData.isSelected()){
				plotData2D(this.plotSpectrenPanel, DataSelection.var(this.featureView), true,1);
				title = new BaseLabel("Variance", new java.awt.Color(0,66,145), 0.5, 1.1);
			}
		

			this.plotSpectrenPanel.removeAllPlotables();
			
			title.setFont(new Font("Arial", Font.BOLD, 15));
			this.plotSpectrenPanel.addPlotable(title);
		//if no data is currently loaded
		} catch (NullPointerException nfe) {
			JOptionPane.showMessageDialog(this, "There is currently no data loaded for the FeatureSelection panel to operate with.");
			nfe.printStackTrace();
		}

	}

	
	/**
	 * weightBy
	 * weights attributes by using Weka-Filters
	 * @param filterType
	 */
	public void weightBy(String filterType){

		//Build the data for the evaluation (classes and values)
		WekaTransformer wkt = new WekaTransformer();

		// Weka instances with the values
		Instances values = wkt.transformMatrix(Utilities.materializeAsDoubleMatrix(featureView).getColumns(FeatureSelection.getNonSpecialFeatures(featureView)),false);

		// Weka instances with the classes	
		DoubleMatrix classMatrix = Utilities.materializeAsDoubleMatrix(this.featureView).getColumn(FeatureSelection.getLabel(this.featureView));
		Instances classes = wkt.transformMatrix(classMatrix,false);
		// merge values and classes in one Weka Instances
		Instances wekaInstances = new Instances(wkt.transformMatrix(DoubleMatrix.zeros(1,values.numAttributes()+1),false));
		wekaInstances.delete();
		for(int i=0;i<values.numInstances();i++)
			wekaInstances.add(values.instance(i).mergeInstance(classes.instance(i)));
		// make the last column to be the class
		wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
		/*
		 *  change class from numeric to nominal (for all filters necessary, except Relieff)
		 */
		if(filterType != "Relieff"){
			NumericToNominal filter = new NumericToNominal();

			String[] options = new String[2];
			options[0] = "-R";
			options[1] = String.valueOf(wekaInstances.numAttributes());

			try {
				filter.setOptions(options);
				filter.setInputFormat(wekaInstances);
				wekaInstances = Filter.useFilter(wekaInstances, filter);
				wekaInstances.setClassIndex(wekaInstances.numAttributes() - 1);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		/*
		 * evaluate, plot and show weights
		 */
		try {
			/*
			 * Choose filter and filter the data
			 * 
			 * y-val: array with vales of the features
			 * x_val: array with wavelengths (!!!ACHTUNG: MUSS NOCH MIT DEN RICHTIGEN WELLENL�NEGEN 
			 * BEARBEITET WERDEN; BISLANG WERDEN DIE WELLENL�NGEN NUR VON 1 BIS ANZAHL FEATURES BENANNT!!!)
			 */
			y_val = new double[wekaInstances.numAttributes()-1];
			

			// Relieff-Filter
			if(filterType == "Relieff"){
				ReliefFAttributeEval attributeFilter = new ReliefFAttributeEval();
				attributeFilter.buildEvaluator(wekaInstances);

				for(int i=0;i<wekaInstances.numAttributes()-1;i++){
					y_val[i] = attributeFilter.evaluateAttribute(i);
			
				}
			}
			// Information Gain-Filter
			else if(filterType == "InfoGain"){
				InfoGainAttributeEval infoGain = new InfoGainAttributeEval();
				infoGain.buildEvaluator(wekaInstances);

				for(int i=0;i<wekaInstances.numAttributes()-1;i++){
					y_val[i] = infoGain.evaluateAttribute(i);
			
				}
			}
			
			
			// SVM-Filter
			else if(filterType == "SVM"){
				SVMAttributeEval svmA = new SVMAttributeEval();
				svmA.buildEvaluator(wekaInstances);

				for(int i=0;i<wekaInstances.numAttributes()-1;i++){
					y_val[i] = svmA.evaluateAttribute(i);
			
				}
			}
					

			/*
			 *  norm weights between 0 and 1
			 */
			if(jcbNorm.isSelected() == true){
				double min = getMin(y_val);
				double max = getMax(y_val);

				for(int i=0;i<wekaInstances.numAttributes()-1;i++){
					y_val[i] = (y_val[i] - min)/(max - min);
				}
			}

			/*
			 * Create FeatureWeightsModel and put in models list
			 */
			View v = master.getProject().getTrainView();
			int[] indices = FeatureSelection.getNonSpecialFeatures(v);
			String [] wvL = new String[indices.length];
			for(int i=0; i<indices.length; i++){
				wvL[i] = v.getFeatureDescriptors()[indices[i]].trim();
			}
			fwm = new FeatureWeightsModel("FeatureWeightsModel_"+v.getViewName(), v.getViewName(), new Date(), y_val, wvL);

			/*
			 * plot the filter results
			 */
			plotPanel.removeAllPlots();
			DoubleMatrix wPlotData = new DoubleMatrix(1,y_val.length);
			for(int i=0;i<y_val.length;i++)
				wPlotData.put(0,i,y_val[i]);
			
		

			// add title
			plotPanel.removeAllPlotables();
			BaseLabel title = new BaseLabel("Filter Result - " + jcobAlgorithmenList.getSelectedItem().toString(), new java.awt.Color(0,66,145), 0.5, 1.1);
			title.setFont(new Font("Arial", Font.BOLD, 15));
			plotPanel.addPlotable(title);
			double[][] tt = new double[1][y_val.length];
			tt[0] = y_val;
			DoubleMatrix x_valTest = new DoubleMatrix(tt);
			plotData2D(plotPanel, x_valTest, true,2);
			
			
			// update buttons etc.
		
		
			//			removeFeatureButton.setEnabled(true);
			jrbSelectFilter.setEnabled(true);
			jrbSelectFilter.setSelected(true);
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}

	
	/**
	 * Initializes plotting of selected data in selected plot window
	 * Only if the double matrix spect can be assigned to the local class variable featureView
	 * @param plotWindow panel where the plot will be printed
	 * @param spect	DoubleMatrix with values for printing
	 * @param remove decides if the old plot should be overwritten or deleted
	 * @param plotColor	sets if all data is plotted or just means / std etc. Needed to print spectra in the right color. 0 : all data is plotted in the right color; 1: mean, std, var; 2: filter results in black
	 * 
	 * @author croemer
	 */
	public void plotData2D(Plot2DPanel plotWindow, DoubleMatrix spect, boolean remove, int plotColor){
		
		if(remove)
			plotWindow.removeAllPlots();
		//either returns double values of feature descriptors or 1..n if there are no double values inside
		double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView);
				
		
		
		for (int i = 0; i<spect.rows; i++){
			Color color = Color.BLACK;
			double[] plotspect = spect.getRow(i).toArray();
			if (plotColor == 0){
				double[] label = Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumn(FeatureSelection.getLabel(master.getProject().getTrainView())).toArray();			
				color = Utilities.getColor(this.featureView.getLabelMap().get((int) label[i]));
			} else if (plotColor==1) {
				//i+1 as label start with 1
				color = Utilities.getColor(this.featureView.getLabelMap().get(i+1));
			}
			plotWindow.addLinePlot(null, color, FeatureSelection.getNonSpecialFeatureDescriptor(this.featureView), plotspect);
			
		}
		// Plot auf die Daten skalieren
		plotWindow.setFixedBounds(0, getMin(x), getMax(x));		
		plotWindow.setFixedBounds(1,spect.min(), spect.max());								
	}

	
	/**
	 * getMax
	 * Compute the maximum of an array
	 * @param array
	 * @return
	 */
	public static double getMax(double[] array){
		double max = Double.MIN_VALUE;
		for(double value: array){
			if(value > max)
				max = value;
		}
		return max;
	}

	/**
	 * getMin
	 * Compute the minimum of an array
	 * @param array
	 * @return
	 */
	public static double getMin(double[] array){
		
		double min = Double.MAX_VALUE;
		for(double value: array){
			if(value < min)
				min = value;
		}
		return min;
	}

	/**
	 * loadData
	 * load data of all class
	 */
	public void loadData(){
		boolean a=false;
		String message  = "";

		//try{			
			if (!master.getDataManagementPanel().getOrderOfSelected().isEmpty() && !master.getProject().getTViewList().isEmpty()){
				this.featureView = master.getProject().getTViewList().get(master.getDataManagementPanel().getOrderOfSelected().get(0));
			//if(this.featureView!= null){
				if(this.featureView!=lastDisplayed){
					this.jrbSelectAllData.setSelected(true);
					plotStatistics();															
					
					
					
					jbSaveModelWeights.setEnabled(false);
				
					
	//				chooseFeature.setVisible(false);
					
					lastDisplayed = this.featureView;
	
				}
			}
		
		try{
		boolean b = false;
		if(master.getProject().getViewList().get(master.getDataManagementPanel().getOrderOfSelected().get(0)).getLabelMap()==null){
			message+="- no label-names\n";
			b=true;
		}else{
			if(this.featureView.getLabelMap().isEmpty()){
				message+="- no label-names2\n";
				b=true;
			}
		}
		if(a){
			message+="- no wavelengths as feature descriptors\n";
			b = true;
		}
		if(b){
			JOptionPane.showMessageDialog(master, message, "View info", JOptionPane.INFORMATION_MESSAGE);
		}
		}catch(Exception f){
			
		}
	};
	

	public Plot2DPanel getPlotSpectren() {
		return plotSpectrenPanel;
	}

	/**
	 * computed the correlation matrix between features and shows the correlation matrix within a table
	 * TODO: the table needs a left border for description and an export funtion to save it as a csv or similar
	 * this function is then usable to show cross validation results!
	 * 
	 * @author: croemer
	 */
	private void showCorrMatrix(){
		DoubleMatrix correlations = new DoubleMatrix(new PearsonsCorrelation().computeCorrelationMatrix(Utilities.materializeAsDoubleMatrix(this.featureView).getColumns(FeatureSelection.getNonSpecialFeatures(featureView)).toArray2()).getData());
		
		JFrame preview = new JFrame();

		preview.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().width *0.5), (int)(Toolkit.getDefaultToolkit().getScreenSize().height *0.5));
		preview.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-preview.getSize().width) / 2,(Toolkit.getDefaultToolkit().getScreenSize().height-preview.getSize().height) / 2 +50		);

		Container content = preview.getContentPane();
		preview.setTitle("CorrelationMatrix - "+this.featureView.getViewName());

	
		Double[][] plotCorrs = new Double[correlations.rows][correlations.columns];
		for (int i = 0; i<plotCorrs.length;i++)
			for (int j = 0; j<plotCorrs[0].length;j++)
				plotCorrs[i][j] =correlations.get(i,j); 
		
		
		JTable table = new JTable( plotCorrs, FeatureSelection.getNonSpecialFeatureDescriptorString(this.featureView) );

		table.setEnabled(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed( false );
		table.getTableHeader().setResizingAllowed( true );

		content.add(new JScrollPane(table), BorderLayout.CENTER);
		preview.setVisible(true);
	}
	
	/**
	 * Initializes MouseListener to show tool tips on mouse entered events for combo Box jcobAlgorithmenList
	 */
	private void initAlgorithmToolTip(){
		/*
		 * filterList beinhaltet alle zur Filterung der Daten vorhandenen Filter
		 * get informations about the filtertypes by hovering the combobox "filterList"
		 * (help for filtertypes)
		 */
		jcobAlgorithmenList.addMouseListener(new MouseListener(){

			// Popup in dem die Informationen zu den Filtern dargestellt werden
			JPopupMenu hoverNorm = new JPopupMenu();

			// wenn die Maus �ber den ausgew�hlten Filter bewegt wird, �ffnet sich das Popup
			public void mouseEntered(MouseEvent me) {			
				// Informationen zum Relieff Filter
				if(jcobAlgorithmenList.getSelectedIndex()==0){
					hoverNorm.add(new String("<html>ReliefFAttributeEval :"+
							"<br/> <br/>Evaluates the worth of an attribute by repeatedly " +
							"<br/>sampling an instance and considering the value of the given " +
							"<br/>attribute for the nearest instance of the same and different " +
							"<br/>class. Can operate on both discrete and continuous class data."+
							"<br/> <br/>http://www.cs.waikato.ac.nz/ml/weka/"+
							"<br/>(weka.attributeSelection.ReliefFAttributeEval)</html>")
							);
				}
				// Informationen zum Information Gain Filter
				else if(jcobAlgorithmenList.getSelectedIndex()==1){
					hoverNorm.add(new String("<html>InfoGainAttributeEval :"+
							"<br/> <br/>Evaluates the worth of an attribute by measuring the " +
							"<br/>information gain with respect to the class."+
							"<br/>InfoGain(Class,Attribute) = H(Class) - H(Class | Attribute)." +
							"<br/> <br/>http://www.cs.waikato.ac.nz/ml/weka/"+
							"<br/>(weka.attributeSelection.InfoGainAttributeEval)</html>")
							);
				}
				
				
				
				// Informationen zum SVM Filter
				else if(jcobAlgorithmenList.getSelectedIndex()==2){
					hoverNorm.add(new String("<html>SVMAttributeEval :"+
							"<br/> <br/>Evaluates the worth of an attribute by using an SVM classifier."+
							"<br/> <br/>For more information see:"+
							"<br/>Guyon, I., Weston, J., Barnhill, S., & Vapnik, V. (2002)."+
							"<br/>Gene selection for cancer classification using " +
							"<br/>support vector machines. Machine Learning, 46, 389-422"+
							"<br/> <br/>http://www.cs.waikato.ac.nz/ml/weka/"+
							"<br/>(weka.attributeSelection.SVMAttributeEval)</html>")
							);
				}
				

				hoverNorm.setBackground(new java.awt.Color(255, 255, 255));
				// Position zur Maus wo das Popup ge�ffnet wird
				hoverNorm.show(me.getComponent(), me.getX(), me.getY()+18);
			}
			// wenn die Maus den ausgew�hlten Filter verl�sst, schlie�t sich das Popup
			public void mouseExited(MouseEvent e) {
				hoverNorm.removeAll();
				hoverNorm.setVisible(false);
			}
			public void mouseClicked(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseReleased(MouseEvent e) {
			}	
		});
	}
	
	
	/**
	 * adds a Legend to the Plot2D based on the classes and the colors in the trainView labelMap
	 * @author jbehmann
	 * @return true if a trainView was found and a legend was added
	 */
	public boolean addLabelLegend(){
		// Exists a TV ? 
		TrainView tv = this.master.getProject().getTrainView();
		if(tv == null || tv.getLabelMap().size()==0){
			return false;
		}
		
		//Extract Classes in correct order;
		Map<Integer, String> classes = tv.getLabelMap();
		int ind = 0;
		int found =0; 
		LinkedList<String> classList = new LinkedList<String>();
		while (found != classes.size()){
			String classtemp = classes.get(ind);
			if(classtemp!=null){
				found ++;
				classList.add(classtemp);
			}
			ind++;
		}
		
		//Build Legend List;
		LinkedList<Legend> legendList = new LinkedList<Legend>();
		plotPanel.addLegend("SOUTH");
		LegendPanel legends=plotPanel.plotLegend;
		for (String classString :classList){
			double[][] multi = new double[1][1];			
			Plot pl= new LinePlot(classString.substring(0, classString.length()-8), Utilities.getColor(classString), multi );
			Legend l = legends.new Legend(pl);
			legendList.add(l);
		}
		
		//Add legend to Plot
		legends.updateLegends(legendList);		
		return true;
	}
	
	/**
	 * JButton to contral the visibility of the 2D plot legend
	 * @author jbehmann
	 * @return JButton for menu of Plot2D Panel
	 */
	public JButton getLegendToggleButton(){
		if(legendToggleButton == null){
			legendToggleButton= new JButton("Legend");
			legendToggleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(plotPanel.plotLegend.isVisible()){
						plotPanel.plotLegend.setVisible(false);
						plotPanel.removeLegend();
						System.out.println("Sichtbar");
					}
					else{
						plotPanel.plotLegend.setVisible(true);
						System.out.println("UnSichtbar");
						addLabelLegend();
					}
				}
			});	
		}
		
		return legendToggleButton;
	}
	/**
	 * adds a Legend to the Plot2D based on the classes and the colors in the trainView labelMap
	 * @author jbehmann
	 * @return true if a trainView was found and a legend was added
	 */
	public boolean addLabelLegendSpectra(){
		// Exists a TV ? 
		TrainView tv = this.master.getProject().getTrainView();
		if(tv == null || tv.getLabelMap().size()==0){
			return false;
		}
		
		//Extract Classes in correct order;
		Map<Integer, String> classes = tv.getLabelMap();
		int ind = 0;
		int found =0; 
		LinkedList<String> classList = new LinkedList<String>();
		while (found != classes.size()){
			String classtemp = classes.get(ind);
			if(classtemp!=null){
				found ++;
				classList.add(classtemp);
			}
			ind++;
		}
		
		//Build Legend List;
		LinkedList<Legend> legendList = new LinkedList<Legend>();
		plotSpectrenPanel.addLegend("SOUTH");
		LegendPanel legends=plotSpectrenPanel.plotLegend;
		for (String classString :classList){
			double[][] multi = new double[1][1];			
			Plot pl= new LinePlot(classString.substring(0, classString.length()-8), Utilities.getColor(classString), multi );
			Legend l = legends.new Legend(pl);
			legendList.add(l);
		}
		
		//Add legend to Plot
		legends.updateLegends(legendList);		
		return true;
	}
	
	/**
	 * JButton to contral the visibility of the 2D plot legend
	 * @author jbehmann
	 * @return JButton for menu of Plot2D Panel
	 */
	public JButton getLegendToggleButtonSpectra(){
		if(legendToggleButtonSpectra == null){
			legendToggleButtonSpectra= new JButton("Legend");
			legendToggleButtonSpectra.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(plotSpectrenPanel.plotLegend.isVisible()){
						plotSpectrenPanel.plotLegend.setVisible(false);
						plotSpectrenPanel.removeLegend();
						System.out.println("Sichtbar");
					}
					else{
						plotSpectrenPanel.plotLegend.setVisible(true);
						System.out.println("UnSichtbar");
						addLabelLegendSpectra();
					}
				}
			});	
		}
		
		return legendToggleButtonSpectra;
	}

}