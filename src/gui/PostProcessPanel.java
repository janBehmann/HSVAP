/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jblas.DoubleMatrix;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import wekatools.WekaTransformer;
import data.FeatureSelection;
import data.Utilities;

/**
 * constructs a panel for Active Learning
 * Initializes user surface and performs calculation of the best candidates for labeling
 * Candidates are visualized on image until they are added to a class
 * added candidates will be included in active train view
 * 
 * DEVELOPER NOTES:
 * IMPORTANT: THERE IS NO METHOD TO CONTROL IF THE AL LOOP IS NOT FINISHED BEFORE USER TAKES ANOTHER ACTION
 * OR IF THE ACTIVE TRAIN VIEW IS CHANGED
 * 
 * MAYBE FOR THIS PREOCEDUTE IT IS BETTER TO CALCULATE JUST ONE CANDIDATE PER LOOP
 * 
* @author: croemer, pschmitter
 */
public class PostProcessPanel extends JPanel {


	private static final long serialVersionUID = -7173075970896811295L;

	private JTextField numberPointsField;
	private JTextField numberClustersField;
	
	private MainGui master;
	private DataPanel dataPanel;

	private JButton activeLearningButton;
	private JButton addToClassButton;
	private JButton addClassButton;
	private JButton skipButton;
	private JButton alternativeButton;
	
	private ArrayList<Integer> formerCandidates = new ArrayList<Integer>();
	
	private JComboBox<String> classBox;
	



	private int calls;
	private DoubleMatrix spektren;
	private int[] candidates;
	private int [] sortIdx;

	public PostProcessPanel(MainGui main, DataPanel dataPanel){
		this.dataPanel = dataPanel;
		this.master = main;
		this.init();
	}

	/**
	 * create GUI-Panel
	 */
	private void init() {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createTitledBorder("active learning options"));
		JLabel pointsLabel = new JLabel("<html>Select Number of Candidates <p/> from Image:</html>");
		pointsLabel.setToolTipText("From the current image the selected number of candidates with lowest probability for their predicted class will be selected for calculation.");
		JLabel clusterLabel = new JLabel("Select Number of Clusters:");
		clusterLabel.setToolTipText("A clustering algorithm (KMeans) will determine as many points as clusters are selected for manual inspection. This is done to ensure that those points are distributed over feature space."); 
		
//		JPanel propPanel = new JPanel();
//		propPanel.setLayout(new GridLayout(2,2));
//		propPanel.add(pointsLabel);
//		propPanel.add(initNumberPoints());
//		propPanel.add(clusterLabel);
//		propPanel.add(initNumberClusters());
//		
//		this.add(propPanel,BorderLayout.CENTER);
//		this.add(initPostProButton(),BorderLayout.EAST);
//		
		JPanel propPanel = new JPanel();
		propPanel.setLayout(new MigLayout("", "[]10[]10[]", "20[]10[]"));
		propPanel.add(pointsLabel);
		propPanel.add(initNumberPoints(), "wrap");
		propPanel.add(clusterLabel);
		propPanel.add(initNumberClusters(), "wrap");
		propPanel.add(initPostProButton(), "span, width 200:220");
		
		this.add(propPanel,BorderLayout.CENTER);
		
		JPanel activePanel = new JPanel();
		//activePanel.add(this.dataPanel.getClassBox());
		//activePanel.add(this.dataPanel.getAddClass());
		this.addClassButton = new JButton("Add Class");
		this.addClassButton.setEnabled(false);
		this.addClassButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataPanel.addClass();
				
				dataPanel.repaint();	
				dataPanel.setItemsForComboBox(classBox);	
			}
		});	
		this.addToClassButton = new JButton("Add to selected Class");
		this.addToClassButton.setEnabled(false);
		this.addToClassButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				addToClass();
				dataPanel.repaint();

			}		
		});
		
		this.alternativeButton = new JButton("Alternative point");
		this.alternativeButton.setEnabled(false);
		this.alternativeButton.setToolTipText("Suggest new point when class is not obvious. Nearest neighbor then is displayed.");
		this.alternativeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				double[] spektrum = spektren.getRow(sortIdx[candidates[calls]]).toArray();
				
				double[] [] uncertainData = master.getProject().getActiveView().materializeFeatureExamples( 
						sortIdx, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
				DoubleMatrix uncertainMatrix = new DoubleMatrix(uncertainData);		
				
				// find nearest neighbor that has not yet been candidate
				formerCandidates.add(candidates[calls]);
				double cand = Double.MAX_VALUE;
				int indices = 0;
				for (int i=0; i<uncertainMatrix.getRows(); i++){	
					if(!formerCandidates.contains(i)){
//					if(i!=candidates[calls]){
						double dist = uncertainMatrix.getRow(i).distance2(new DoubleMatrix(spektrum) );
						if (cand>dist){
							cand = dist;
							indices = i;
						}
					}
				}
				candidates[calls] = indices;
				master.getDataPanel().getPlot().removePlot(master.getDataPanel().getPlot().getPlots().size()-1);
				plotCandidate(calls);
				
			}
		});	
		
		this.skipButton = new JButton("Skip point");
		this.skipButton.setEnabled(false);
		this.skipButton.setToolTipText("Skip point completely.");
		this.skipButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				calls = calls+1;
				formerCandidates = new ArrayList<Integer>();
				
				master.getDataPanel().getPlot().removePlot(master.getDataPanel().getPlot().getPlots().size()-1);
				
				plotCall();
				master.getPaintPanel().repaint();
				master.getDataPanel().repaint();
				master.getAlPanel().repaint();
				
			}
		});	
			
		this.classBox = new JComboBox<String>();
		this.classBox.setEnabled(false);
		activePanel.add(this.classBox);
		activePanel.add(this.addToClassButton);
		activePanel.add(this.addClassButton);
		activePanel.add(this.alternativeButton);
		activePanel.add(this.skipButton);
		
		this.add(activePanel, BorderLayout.SOUTH);
		this.setPreferredSize(new Dimension(1000,30));
	}


	/**
	 * @return the numberPoints
	 */
	private JTextField initNumberPoints() {
		if(numberPointsField==null){
			numberPointsField = new JTextField();
			numberPointsField.setPreferredSize(new Dimension(70, 20));
			numberPointsField.setText("1000");
			numberPointsField.setToolTipText("Enter number of selected points");
		}
		return numberPointsField;
	}

	/**
	 * @return the numberClusters
	 */
	private JTextField initNumberClusters() {
		if(numberClustersField==null){
			numberClustersField = new JTextField();
			numberClustersField.setPreferredSize(new Dimension(70, 20));
			numberClustersField.setText("5");
			numberClustersField.setToolTipText("Enter number of clusters");
		}
		return numberClustersField;
	}

	/**
	 * Create Button with method calls
	 * @return process-button
	 */

	private JButton initPostProButton(){
		if(activeLearningButton==null){
			activeLearningButton=new JButton ("apply active learning");
			activeLearningButton.setToolTipText("Cluster uncertain data and add a label for each centroid");
			activeLearningButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e)
				{
					if (checkInputErrors())
						postProcessClassifiedData();					
						
				}
			});    
		}
		return activeLearningButton;
	}
	
	/**
	 * Checks whether the input parameters for text field in this panel are correctly before processing data
	 * Checks whether an active view is selected and if the active view has probabilites
	 * returns error messages else
	 * @return boolean true if all conditions are met.
	 * 
	 * @author: croemer
	 */
	private boolean checkInputErrors(){
		boolean allow = false;
		try{
			Double.valueOf(this.numberPointsField.getText());
			Double.valueOf(this.numberClustersField.getText());
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Illegal Input Parameters. Numbers only please.");
			return false;
			
		}
		if (Double.valueOf(this.numberPointsField.getText())<=0 || Double.valueOf(this.numberClustersField.getText())<=0 ){
			JOptionPane.showMessageDialog(this, "Only positive values for C and C Interval allowed.");
		} else if (Double.valueOf(this.numberPointsField.getText()) % 1 != 0  || Double.valueOf(this.numberClustersField.getText()) % 1 != 0 ){
			JOptionPane.showMessageDialog(this, "Degree has to be a Natural Number.");
		} else if (this.master.getProject().getActiveView()==null){
			JOptionPane.showMessageDialog(this, "No dataset selected.");
		} else if (FeatureSelection.getProbs(this.master.getProject().getActiveView())==null){
			JOptionPane.showMessageDialog(this, "Selected Active View need to have probabilites for each sample in order for Active Learning to work properly.");
		}  else {
			allow = true;
		}
			
		return allow;
	}
	
	/**
	 * adds current candidate to train view
	 * TODO: Need to Change Color of point in spectrum plot
	 * 
	 * @author: croemer
	 */
	private void addToClass(){	
		double x = master.getProject().getActiveView().get(sortIdx[candidates[calls]], FeatureSelection.getXCoordinate(master.getProject().getActiveView()));
		double y = master.getProject().getActiveView().get(sortIdx[candidates[calls]], FeatureSelection.getYCoordinate(master.getProject().getActiveView()));				
		double[] spektrum = spektren.getRow(sortIdx[candidates[calls]]).toArray();
		master.getDataPanel().addClassPoints(spektrum, new double[]{x-1, y-1}, this.classBox);
		calls = calls+1;
		formerCandidates = new ArrayList<Integer>();
		
		plotCall();
		master.getPaintPanel().repaint();
		master.getDataPanel().repaint();
		master.getAlPanel().repaint();
	}
	
	/**
	 * performs kMeans on AL candidates and determines spectra closest to kcentroids
	 * 
	 * @param sortIdx index of sorted spectra in original data matrix for references
	 * @return int[] array of indizes for candidates
	 * 
	 * @author: pschmitter, croemer
	 */
	private int[] calculateCandidatesWithKmeans(int[] sortIdx){
		try {
			// Transform data for weka
			WekaTransformer wkt = new WekaTransformer();
			
			// Select uncertain data form active view
			double[] [] uncertainData = master.getProject().getActiveView().materializeFeatureExamples( 
					sortIdx, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
			Instances weka = wkt.transformMatrix( uncertainData);		
			// Open warning Frame for K-Means		
			// KMeans clustering
			SimpleKMeans kmeans = new SimpleKMeans();
			
			kmeans.setMaxIterations(100);	// maximal number of iterations
			kmeans.setPreserveInstancesOrder(true);			// preserve the order of the data
			kmeans.setNumClusters( Integer.parseInt(numberClustersField.getText())); // Number of Cluster			
			kmeans.buildClusterer(weka);					// Start Clustering
			int[] assignments = kmeans.getAssignments(); 	// Get Clustering Result			
			
			// Create Matrix of Centroids
			Instances centroids = kmeans.getClusterCentroids();
			DoubleMatrix centroidsDM = new DoubleMatrix(centroids.numInstances(), centroids.numAttributes());
			for (int i=0; i<centroids.numInstances(); i++)
				centroidsDM.putRow(i, new DoubleMatrix(1,centroids.numAttributes(), centroids.instance(i).toDoubleArray()));
			
			// Bestimmung der Distanzen zu den Centroiden
			DoubleMatrix uncertainMatrix = new DoubleMatrix(uncertainData);												
			double[] cand = new double[kmeans.getNumClusters()];
			int[] indices = new int[kmeans.getNumClusters()];
			for (int i = 0; i<cand.length;i++){
				cand[i] = Double.MAX_VALUE;
				indices[i] = 0;
			}
			for (int i=0; i<uncertainMatrix.getRows(); i++){				
				double dist = uncertainMatrix.getRow(i).distance2( centroidsDM.getRow( assignments[i]));
				if (cand[assignments[i]]>dist){
					cand[assignments[i]] = dist;
					indices[assignments[i]] = i;
				}
				
			}
			
			return indices;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * Visualizes spectrum and coordinates of candidate(i)
	 * @param i position of candidate in array
	 * 
	 * @author: croemer
	 */
	private void plotCandidate(int i){
		if (master.getPaintPanel().getDots().size()>=1)
			master.getPaintPanel().getDots().removeLast();
		Point newPoint = new Point();
		double x = master.getProject().getActiveView().get(sortIdx[candidates[i]], FeatureSelection.getXCoordinate(master.getProject().getActiveView()));
		double y = master.getProject().getActiveView().get(sortIdx[candidates[i]], FeatureSelection.getYCoordinate(master.getProject().getActiveView()));
		
		double[] spektrum = spektren.getRow(sortIdx[candidates[i]]).toArray();								
		// -1 not needed because image start at [1,1]
		newPoint.setLocation(x, y);
		master.getPaintPanel().getDots().add(newPoint);
		master.getPaintPanel().getDotsColor().put(newPoint, Color.RED);		 
		master.getDataPanel().getPlot().addLinePlot(null,
				Color.RED,master.getDataPanel().getWavelength(),spektrum);
		master.getDataPanel().getPlot().plotToolBar.repaint();
		master.getPaintPanel().repaint();
		master.getDataPanel().repaint();
		master.getAlPanel().repaint();
	}
	
	/**
	 * calculates spectra with lowest probability on active image; performs KMeans on all spectra
	 * candidates for Active Learning are all spectra closest to the k-centroids
	 * those candidates are stored in this.candidates for user to label
	 * 
	 * @author: croemer / pschmitter
	 */
	public void postProcessClassifiedData(){								
		DoubleMatrix probs = Utilities.materializeAsDoubleMatrix(master.getProject().getActiveView()).getColumns(FeatureSelection.getProbs(master.getProject().getActiveView()));
		this.spektren = Utilities.materializeAsDoubleMatrix(master.getProject().getActiveView()).getColumns(FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
										
		DoubleMatrix probabilities = new DoubleMatrix(probs.rows,1);
		for(int i=0; i<probs.rows; i++){
			double temp_max = 0;
			for (int j = 0; j<probs.columns;j++){
				if (probs.get(i,j)>temp_max)
					temp_max = probs.get(i,j);
			}
			probabilities.put(i, temp_max);
		}
		// Sort probabilities to get uncertain data points 		
		int[] indexSort = probabilities.sortingPermutation();
		this.sortIdx = new int [Integer.parseInt(numberPointsField.getText())];
		for(int i=0;i<sortIdx.length;i++)
			sortIdx[i]=indexSort[i];
		//indices of points for active learning
		this.candidates = calculateCandidatesWithKmeans(sortIdx);
		
		if (candidates!=null){
			this.calls = 0;
			plotCall();
		} else {
			//TODO: Fehler asugeben wenn KMEANS keine Ergebnisse liefert
		}											
		
		
		
	}
	
	/**
	 * determines which candidate for Active Learning is visualized
	 * if calls > #of candidates all AL-Loop Buttons are disabled
	 * 
	 * @author: croemer
	 */
	private void plotCall(){
		if (this.calls < this.candidates.length){
			
			this.addClassButton.setEnabled(true);
			this.addToClassButton.setEnabled(true);
			this.classBox.setEnabled(true);
			this.skipButton.setEnabled(true);
			this.alternativeButton.setEnabled(true);
			
			plotCandidate(this.calls);	
			this.dataPanel.setItemsForComboBox(this.classBox);	
			
			// check for candidate in trainView
			checkTrainData();
			
		} else {
			this.calls = 0;
			this.candidates = null;
			this.sortIdx = null;
			this.addClassButton.setEnabled(false);
			this.addToClassButton.setEnabled(false);
			this.classBox.setEnabled(false);
			this.skipButton.setEnabled(false);
			this.alternativeButton.setEnabled(false);
			
		}
		
	}

	
	/**
	 * method to recursively check whether the candidate is member of training data
	 * options to skip the point or suggest an alternative point (nearest neighbor), which will also be checked (rekursively)
	 * 
	 * @author: s7tischu
	 */
	private void checkTrainData() {
		
		DoubleMatrix sp = new DoubleMatrix(spektren.getRow(sortIdx[candidates[this.calls]]).toArray());
		DoubleMatrix m1 = new DoubleMatrix(master.getProject().getTrainView().materializeFeatures(FeatureSelection.getNonSpecialFeatures(master.getProject().getTrainView())));
		
		boolean already = false;
		for(int i=0; i<m1.rows; i++){
			DoubleMatrix m1_i = m1.getRow(i).eq(sp.transpose());
			
			if(m1_i.max() == 1 & m1_i.max()-m1_i.min()==0){
				already = true;
				break;
			}
		}
		if(already){		// Point/Vector is already in training data

			int messageType = JOptionPane.QUESTION_MESSAGE;
		    String[] options = {"Skip point","Alternative point"};
		    int code = JOptionPane.showOptionDialog(
		    		null, 
		         "Sample is already in training data. How to proceed?", 
		         "Active learning", 0, messageType, 
		         null, options, "Alternative point");


		    if(code == 1){	//Alternative point

		    	double[] spektrum = spektren.getRow(sortIdx[candidates[calls]]).toArray();
				
				double[] [] uncertainData = master.getProject().getActiveView().materializeFeatureExamples( 
						sortIdx, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
				DoubleMatrix uncertainMatrix = new DoubleMatrix(uncertainData);		
				
				// find nearest neighbor that has not yet been candidate
				formerCandidates.add(candidates[calls]);
				double cand = Double.MAX_VALUE;
				int indices = 0;
				for (int i=0; i<uncertainMatrix.getRows(); i++){	
					if(!formerCandidates.contains(i)){
//					if(i!=candidates[calls]){
						double dist = uncertainMatrix.getRow(i).distance2(new DoubleMatrix(spektrum) );
						if (cand>dist){
							cand = dist;
							indices = i;
						}
					}
				}
				candidates[calls] = indices;
				master.getDataPanel().getPlot().removePlot(master.getDataPanel().getPlot().getPlots().size()-1);
				plotCandidate(calls);
				
				checkTrainData();	// rekursiv
				
		    }else if(code==0){		// Skip point

		    	calls = calls+1;
				formerCandidates = new ArrayList<Integer>();
				
				master.getDataPanel().getPlot().removePlot(master.getDataPanel().getPlot().getPlots().size()-1);
				
				plotCall();
				master.getPaintPanel().repaint();
				master.getDataPanel().repaint();
				master.getAlPanel().repaint();
		    }
		}		
	}
	


	

	



}
