/**
 * 
 */
package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import models.KMeansModel;
import net.miginfocom.swing.MigLayout;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import wekatools.WekaTransformer;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.operators.AddLabelsView;
import edu.mines.jtk.awt.ColorMap;



/**
 * Section of the Unsupervied Learning Panel for Using the Weka-Methods
 * 
 * 
 * @author jb
 * @author modified by schmitter
 *
 */
public class WekaPanel extends JPanel {
	
	private JButton saveModel;
	
	private static final long serialVersionUID = -578273101094899863L;

	/**
	 * Textfield for Number of classes for k-means
	 */
	private JTextField textNrCluster;

	/**
	 * Number of classes for k-means
	 */
	private int nrCluster;
	/**
	 * Centroide des k-menas
	 */
	private Instances clusterCentroids;

	private MainGui master;


	public WekaPanel(MainGui main){
		super();
		this.master = main;
		this.nrCluster = 15;
		this.init();
	}

	/**
	 * create GUI-Panel
	 * @author modified by schmitter
	 */
	private void init() {
		this.setBorder(BorderFactory.createTitledBorder("WEKA: K-Means Algorithm"));
//		this.setLayout(new GridLayout(1,3));
		this.setLayout(new MigLayout("", "[]10[]20[]",""));

		// Create Textfields for K-Means
//		JTextField nameNumberClasses = new JTextField("Number of Cluster:");
//		nameNumberClasses.setEditable(false);
		JLabel nameNumberClasses = new JLabel("Number of Cluster:");
		this.add(nameNumberClasses);
		// Create input textfield for number of cluster
		textNrCluster = new JTextField();
		textNrCluster.setPreferredSize(new Dimension(50, 20));
		textNrCluster.setToolTipText("Enter parameter k for kMeans. This defines the number of clusters calculated.");
		textNrCluster.setEditable(true);
		textNrCluster.setText( String.valueOf(nrCluster));
		
		this.add(textNrCluster, "wrap");
		// Add process button to gui
		// JButton process = processButton();
		this.add(processButton(), "span, width 100:152");
		this.add(getSaveModel(), "width 100:170, height 20:30");
		this.setPreferredSize(new Dimension(5000,30));
	}


	/**
	 * Create Button "cluster" which estimates the kmeans
	 * 
	 * @return process-button
	 * @author modified schmitter
	 */
	private JButton processButton(){
		JButton process=new JButton ("Cluster");
		process.setToolTipText("Cluster the spectra with the k-means algorithm");

		process.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				master.getLog().info("KMeans clustering started");
										
				WekaTransformer wkt = new WekaTransformer();				
				Instances weka = wkt.transformMatrix( Utilities.materializeAsDoubleMatrix(master.getProject().getActiveView()).getColumns( FeatureSelection.getNonSpecialFeatures( master.getProject().getActiveView())), false);				

				SimpleKMeans kmeans = new SimpleKMeans();
				
				try {
					kmeans.setMaxIterations(100);
					kmeans.setPreserveInstancesOrder(true);
					nrCluster = Integer.parseInt( textNrCluster.getText());
					if (checkInputErrors()){
						kmeans.setNumClusters( nrCluster);						
						startWekaThread( kmeans, weka);
						master.getLog().info("KMeans clustering successful");
					}
					//								kmeans.buildClusterer(weka);
					//								assignments = kmeans.getAssignments();
					
				} catch (NumberFormatException nfe){
					JOptionPane.showMessageDialog(master, "Illegal parameters for number of clusters.");
					master.getLog().info("KMeans clustering aborted");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					master.getLog().info("KMeans clustering aborted");
				}
				master.getDataManagementPanel().actualiseListen();
			}
		});     

		return process;
	}
	
	/**
	 * Checks whether the input parameters are legal for kMeans
	 * @return boolean true if parameters are legal
	 * 	 
	 * author: croemer
	 */
	private boolean checkInputErrors(){
		boolean allow = false;
		
			if (Double.valueOf(this.textNrCluster.getText())<=0  ){
				JOptionPane.showMessageDialog(this, "Only positive values for number of clusters allowed.");
			} else if (Double.valueOf(this.textNrCluster.getText()) % 1 != 0) {
				JOptionPane.showMessageDialog(this, "Number of Clusters has to be a natural number.");			
			} else {
				allow = true;
			}
			
		return allow;
	}

	/**
	 * calls Weka-Kmeans in a additional Thread
	 * @param kmeans
	 * @param weka
	 * @param emptyPixel
	 * @author modified by schmitter
	 */
	public void startWekaThread(SimpleKMeans kmeans, Instances weka){		
		
				try {
					// cluster data
					kmeans.buildClusterer( weka);

					// Get the clustering results (assignments)
					int[] assignments= kmeans.getAssignments();
					Instances centroids = kmeans.getClusterCentroids();
					double [] doubleAssignments = new double[ assignments.length];
					for(int i=0;i<assignments.length;i++)
						doubleAssignments[i]= assignments[i];

					// Building the result Image
					addCluster( centroids, doubleAssignments, "KMeans", master.getProject().getActiveView());
					clusterCentroids = centroids;
					master.getAlPanel().getClusterButton().setEnabled(true);
					// Create new view with clustering result
					AddLabelsView newView = new AddLabelsView( doubleAssignments, master.getProject().getActiveView(), master.getDirectoryProp().getProperty("DATA_PATH"));
					
					ColorMap colorMap = new ColorMap( 0, centroids.numInstances(), master.getAlPanel().getColorModel());

					HashMap<Integer,String> lblMap = new HashMap<Integer, String>();
					int numCluster = kmeans.getNumClusters();
					for(int i = 1; i<=numCluster; i++){
//						System.out.println("cluster_"+(i)+" #"+Integer.toHexString(cm.getRGB(i-1)).substring(0, 6));
//						lblMap.put(i-1, "cluster_"+(i)+" #"+Integer.toHexString( colorMap.getColorModel().getRGB(i-1)).substring(0, 6));
						lblMap.put(i-1, "cluster_"+(i)+" #"+ Integer.toHexString( colorMap.getColor(i).getRGB()));
					}
					newView.setLabelMap(lblMap);
					master.getProject().add(newView);

					int[] koordinaten = FeatureSelection.getCoordinates(master.getProject().getActiveView());
					if(koordinaten[0]==-1 | koordinaten[1]==-1){
						addCluster(centroids, doubleAssignments, "KMeans",master.getProject().getActiveView());
						clusterCentroids = centroids;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		


	


	public void addCluster(Instances centroids, double[] assignments, String name,View v){
		BufferedImage image = displayCluster(assignments,v);
		LinkedList<double[]> spectralResult = new LinkedList<double[]>();
		double [][] assignmentsNew = new double[assignments.length][1];
		for (int i2 = 0; i2<centroids.numInstances();i2++){
			Instance centroid = centroids.instance(i2);
			spectralResult.add(centroid.toDoubleArray());
			assignmentsNew[i2][0] = assignments[i2];
		}
		master.getDataPanel().addImage(name+" Result: " +centroids.numInstances()+" Cluster",  spectralResult, image, assignmentsNew);
	}


	public BufferedImage displayCluster(double [] assignments, View v){
		
		ColorMap colorMap = new ColorMap( 0, this.nrCluster, master.getAlPanel().getColorModel());

		double[] xCoord = v.materializeFeature(FeatureSelection.getXCoordinate(v));
		double[] yCoord = v.materializeFeature(FeatureSelection.getYCoordinate(v));
		double xMin = Double.MAX_VALUE;
		double xMax = 0;
		double yMin = Double.MAX_VALUE;
		double yMax = 0;
		for (int i = 0; i < xCoord.length; i++) {
			if (xMin > xCoord[i])
				xMin = xCoord[i];
			if (xMax < xCoord[i])
				xMax = xCoord[i];
			if (yMin > yCoord[i])
				yMin = yCoord[i];
			if (yMax < yCoord[i])
				yMax = yCoord[i];
		}
		xMax = xMax-xMin+1;
		yMax = yMax-yMin+1;
//		int[] imageArray = new int [(int)xMax* (int)yMax];
		int[] imageArray = new int[master.getProject().getActiveView().getXDimension()* master.getProject().getActiveView().getYDimension()];
		int iter =0;
		for (int y=0; y<yMax; y++){
			for (int x = 0; x<xMax; x++){
				if(y*yMax+x != (yCoord[iter]-1)*yMax+(xCoord[iter]-1))
					imageArray[y*(int)xMax+x] = Color.WHITE.getRGB();
				else{
					imageArray[y*(int)xMax+x] = colorMap.getIndex(assignments[iter]);
					iter++;
				}
			}
		}
		MemoryImageSource source =new MemoryImageSource((int) xMax,(int) yMax, colorMap.getColorModel(), imageArray, 0, (int) xMax);
		Image img = createImage(source);
		BufferedImage image = ImagePanel.toBufferedImage(img);
		return image;
	}
	
	/**
	 * @return the saveModel
	 */
	public JButton getSaveModel() {
		if(saveModel==null){
			saveModel = new JButton("save Model");
			saveModel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if( getClusterCentroids()!=null){
					KMeansModel model = new KMeansModel("KMeansModel_"+master.getProject().getActiveView().getViewName(), 
							master.getProject().getActiveView().getViewName(), new Date(), 
							getClusterCentroids(), "K-Means");
					
					master.getDataManagementPanel().getModels().add(model);
					master.getDataManagementPanel().actualiseListen();
					try {
						String dataPath = model.save(master.getDirectoryProp().getProperty("DATA_PATH"));
						master.getDirectoryProp().setProperty("DATA_PATH",dataPath);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
					else{
						JOptionPane.showMessageDialog(saveModel, "svm model does't exist", "no svm model", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return saveModel;
	}

	/**
	 * @return the clusterCentroids
	 */
	public Instances getClusterCentroids() {
		return clusterCentroids;
	}

	
}
