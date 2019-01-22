/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jblas.DoubleMatrix;
import org.math.plot.Plot2DPanel;
import org.math.plot.components.LegendPanel;
import org.math.plot.components.LegendPanel.Legend;
import org.math.plot.plots.LinePlot;
import org.math.plot.plots.Plot;

import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.operators.TrainView;
import edu.mines.jtk.awt.ColorMap;



/**
 * @author Jens Kleinmanns
 *
 */
/**
 * @author jkleinma
 *
 */
public class DataPanel extends JPanel {


	private static final long serialVersionUID = 8343325743796364040L;
	private JButton loadData;
	private JButton save2DData;
	private JComboBox<String> classBox;
	private JButton addClass;
	private JButton removeClass;
	private JButton clearPlot;
	private JList<String> classList;
	private DefaultListModel<String> listModel;
	private Plot2DPanel plot;
	private int currentPlot = -1;
	
	//private HashMap<Integer,DoubleMatrix> coordinateMap;
	private HashMap<Double,DoubleMatrix> meanMap;
	private JComboBox<String> plotSelection;
	private HashMap<String, double[][]> result;
	private HashMap<String, LinkedList<double[]>> spectralResults;
	
	private JTabbedPane tabPanel;
	private JPanel classPanel;
	private PostProcessPanel postProcessControl;
	private MainGui master;
	private double [] wavelength;
	private boolean dotsOrg;
	private WekaClassifierPanel wcp;
	private WekaPanel ulp;
	private ImageProcessingPanel imageProcessingPanel;
	private JButton legendToggleButton;
	private FilterPanel fip;
	
	/**
	 * Constructor
	 * initialize  Buttons,ComboBox,List and their ActionListeners
	 * @param newDataMatrix: loaded Data and Coordinates
	 */
	public DataPanel(MainGui main){
		
		
		this.classBox = new JComboBox<String>(new String[0]);
		this.classBox.setPreferredSize(new Dimension(200,20));
		this.classBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				

				if(classBox.getSelectedItem()!=null ){
					reloadCoordinates();														
				}
			}
		});		
		
		addClass = new JButton("add Class");
		addClass.setPreferredSize(new Dimension(97,20));
		addClass.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addClass();
			}
		});	

		this.master = main;
		//this.coordinateMap=new HashMap<Integer, DoubleMatrix>();
		this.meanMap = new HashMap<Double, DoubleMatrix>();
		this.meanMap = new HashMap<Double,DoubleMatrix>();

		this.result = new HashMap<String,double[][]>();
		this.spectralResults = new HashMap<String,LinkedList<double[]>>();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


		plot = new Plot2DPanel();
		plot.setPreferredSize(new Dimension(500,400));
		plot.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		plot.setAxisLabel(0, "Wavelength [nm]");
		
//		plot.getAxis(1).setLabelAngle(-0.1);
//		double[] posi={100.0,100.0}; 
//		plot.getAxis(1).setLabelPosition(posi);
		plot.setAxisLabel(1, "Reflectance [rel. unit]");
		double[] loc= plot.getAxis(0).getLegendCoord();
		double offsetX =-0.55;
		loc[0] +=offsetX; 
		double offsetY =-0.15;
		loc[1] +=offsetY; 
		plot.getAxis(0).setLabelPosition(loc);
		
		loc= plot.getAxis(1).getLegendCoord();
		offsetX =-0.15;
		offsetY =-0.5;
		double angle = -(Math.PI/2)*1;
		loc[0] +=offsetX; 
		loc[1] +=offsetY; 
		plot.getAxis(1).setLabelPosition(loc);
		plot.getAxis(1).setLabelAngle(angle);
		
		plot.plotToolBar.add(getLegendToggleButton());
		
		this.add(getPlotSelection(),BorderLayout.NORTH);
		this.add(plot,BorderLayout.NORTH);
		this.add(getTabPanel(),BorderLayout.SOUTH);
//		float [][] colorMapTemp = {{  0,  0,  255}, 
//				{0,	 (float) 127.5,  0}, 
//				{255,  0,  0},
//				{0,  (float) 191.25,  (float) 191.25}, 
//				{(float) 191.25,  0,	 (float) 191.25}, 
//				{(float) 191.25,  (float) 191.25,  0}, 
//				{(float) 63.75,	 (float) 63.75,  (float) 63.75}, 
//				{51,  204,  204},
//				{255,  153,  0}, 
//				{255,  255,  0}, 
//				{0,  255,  0}, 
//				{0,	 255,  255}, 
//				{153,  51,  102},
//				{153,  51,  0}, 
//				{255,  0,  255}, 
//				{204,  153,  255} };
		

		dotsOrg = true;

	}

	@Override
	public Dimension getPreferredSize() {

		Dimension dim = new Dimension();
		dim.height = this.getHeight();
		dim.width = 450;
		return dim;
		
		
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
					if(plot.plotLegend.isVisible()){
						plot.plotLegend.setVisible(false);
						plot.removeLegend();
						System.out.println("Sichtbar");
					}
					else{
						plot.plotLegend.setVisible(true);
						System.out.println("UnSichtbar");
						addLabelLegend();
					}
				}
			});	
		}
		
		return legendToggleButton;
	}
	
	
public void addClassPoints(double[] spectrum, double[] coordinate, JComboBox<String> box){
			
		
		if (FeatureSelection.getXCoordinate(master.getProject().getActiveView()) != -1 && box.getSelectedItem()!=null){
			String selectedClass = box.getSelectedItem().toString();
														
			
			
						  
			TrainView tView = (TrainView) this.master.getProject().getTrainView();
			
			tView.addRow(spectrum, getKeyByValue(tView.getLabelMap(),selectedClass), coordinate);	
			
			this.reloadCoordinates();			
//			listModel.addElement(master.getProject().getActiveView().get(row,FeatureSelection.getXCoordinate(master.getProject().getActiveView()))+" , "+master.getProject().getActiveView().get(row,FeatureSelection.getYCoordinate(master.getProject().getActiveView())));

		}
	}


   

	/**
	 * add data and coordinates of selected points to current class 
	 * and update list of coordinates
	 */
	public void addClassPoints(int row, double[] coordinate){
			
		
		if (FeatureSelection.getXCoordinate(master.getProject().getActiveView()) != -1 && classBox.getSelectedItem()!=null){
			String selectedClass = classBox.getSelectedItem().toString();
			
			
			
			
			double[] spectrum = this.master.getProject().getActiveView().materializeExample(row);
			FeatureRole[] roles = this.master.getProject().getActiveView().getFeatureRoles();
		   boolean[] index=new boolean[spectrum.length];
		   int numofFeature=0;
			for (int i =0; i<spectrum.length;i++){
				if(roles[i].compareTo(FeatureRole.FEATURE)==0){
					index[i]=true;
					numofFeature++;
				}				
				else{
					index[i]=false;
				}
			}
			double[] spectrumFeature= new double[numofFeature];
			int counter =0;
			for (int i =0; i<spectrum.length;i++){
				if(index[i]){
					spectrumFeature[counter]= spectrum[i];
					counter++;
				}				
			}
						
			TrainView tView = (TrainView) this.master.getProject().getTrainView();
			tView.addRow(spectrumFeature, getKeyByValue(tView.getLabelMap(),selectedClass), coordinate);	
			
			this.reloadCoordinates();			
//			listModel.addElement(master.getProject().getActiveView().get(row,FeatureSelection.getXCoordinate(master.getProject().getActiveView()))+" , "+master.getProject().getActiveView().get(row,FeatureSelection.getYCoordinate(master.getProject().getActiveView())));

		}
	}
	
	/**
	 * Method checks if trainview is set in master and creates an empty train view if not
	 */
	private void initTrainView(){
		if (this.master.getProject().getTrainViewIndex()==-1){			
			TrainView trainView = new TrainView(this.master.getProject().getActiveView());
			this.master.getProject().addTrainView(trainView,"");
			this.master.getProject().setTrainView(trainView);
			this.master.getDataManagementPanel().actualiseListen();			
		}
	}



	/**
	 * @param position
	 */
	public void plotCurrentSpectra (Point position){
		plot.removeLegend();
		if (!master.getPaintPanel().getIndex(master.getProject().getActiveView()).containsKey(position )){
			if( currentPlot!=-1 && plot.getPlots().size()>currentPlot && plot.getPlots().size()!=0){

				if(plot.getPlots()!=null &&plot.getPlots().get(currentPlot)!=null){
					plot.removePlot(currentPlot);
				}
				currentPlot=-1;


				if (getWavelength()[0] <  getWavelength()[getWavelength().length-1]){
					plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
				}
				plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
				plot.repaint();
				currentPlot=-1;
				this.addLabelLegend();
			}
			return;
		}

		int row = master.getPaintPanel().getIndex(master.getProject().getActiveView()).get(position);

		double[][] spect= master.getProject().getActiveView().materializeFeatureExamples(new int[]{row}, 
				FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));

		if( currentPlot!=-1){
			if(plot.getPlots()!=null && plot.getPlots().size()>currentPlot && plot.getPlots().get(currentPlot)!=null){
				plot.removePlot(currentPlot);
			}
			currentPlot=-1;
		}
		plot.addLinePlot("",Color.BLACK,getWavelength(), spect[0]);

		currentPlot=plot.getPlotIndex(plot.getPlots().getLast());
		
		if (getWavelength()[0] <  getWavelength()[getWavelength().length-1]){
			plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
		}
		plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
		this.addLabelLegend();


	}
	
	
	
	/**
	 * function that extracts the index for a class name from the current trainView
	 * @author jbehmann
	 * @param name - the class name 
	 * @return the index for retrieving the colors
	 */
	public int findColorIndex(String name){	
		int indexLabel=-1;
		for (Entry<Integer,String> entry :this.master.getProject().getTrainView().getLabelMap().entrySet()){
			if(entry.getValue().toString().startsWith(name))
				indexLabel=entry.getKey();
		}
		return indexLabel;
	}

	
	/**
	 * Function for plotting all spectra or the mean spectra of the classes
	 * @author jbehmann
	 * @param position Pixelposition des neuen Spektrums
	 */
	public void plotSpectra (Point position){
		plot.removeLegend();
		// Plotten der mittleren Spectra
		if(position==null && plotSelection.getSelectedItem().equals("mean of spectra")){
			plot.removeAllPlots();
			double [] spect;
			int maxLabel =0;
			if (this.master.getProject().getTrainView()!=null && this.master.getProject().getTrainView().getLabelMap()!=null){
				maxLabel = this.master.getProject().getTrainView().getLabelMap().size();
			}
			
			for(double i=1;i<=maxLabel;i++){		
				if (this.meanMap.get(i)!= null && !this.meanMap.get(i).isEmpty() ){
					spect = this.meanMap.get(i).columnMeans().toArray();				
					plot.addLinePlot("",Utilities.getColor(this.master.getProject().getTrainView().getLabelMap().get((int)i)),getWavelength(),spect);
				}
			}
			currentPlot = -1;
			plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
			plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
			this.addLabelLegend();
			return;
		}

		// Plotten aller Spectra
		if(position==null && plotSelection.getSelectedItem().equals("all spectra")){
			plot.removeAllPlots();
			
			int maxLabel =0;
			if (this.master.getProject().getTrainView()!=null && this.master.getProject().getTrainView().getLabelMap()!=null){
				maxLabel = this.master.getProject().getTrainView().getLabelMap().size();
			}
			
			for(double i=1;i<=maxLabel;i++){		
				DoubleMatrix spect = this.meanMap.get(i);
				if(spect!=null){
					for (int j=0;j< spect.rows; j++){
						plot.addLinePlot("",Utilities.getColor(this.master.getProject().getTrainView().getLabelMap().get((int) i)),getWavelength(),spect.getRow(j).toArray());
					}
				}
			}
			currentPlot = -1;
			if( getWavelength().length!=0){
				plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
				plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
			}
			this.addLabelLegend();
			return;
		}

		if (!master.getPaintPanel().getIndex(master.getProject().getActiveView()).containsKey(position ))
			return;
		if (classBox.getItemCount()==0)
			return;
		
		//Find Spectra for the pixel		
		int row=(int) master.getPaintPanel().getIndex(master.getProject().getActiveView()).get(position);
		this.addClassPoints(row, new double[]{position.x, position.y});
		
		//Schwarzen Plot entfernen
		String selectedClass = classBox.getSelectedItem().toString();
		if(currentPlot!=-1)
			plot.removePlot(currentPlot);
		
//		DoubleMatrix localData= meanMap.get(key);
		
		// Materialisieren des neuen Spektrums
		double[][] spectra= master.getProject().getActiveView().materializeFeatureExamples(new int[]{row}, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
		double[] spectrum = spectra[0];
		double indexLabel=Double.MIN_VALUE;

		for (Entry<Integer,String> entry :this.master.getProject().getTrainView().getLabelMap().entrySet()){
			if(entry.getValue().toString().startsWith(selectedClass))

				indexLabel=entry.getKey();
		}
		
		//Hinzuf�gen des neuen Spektrums zu mean Map
		DoubleMatrix currentSpectra= meanMap.get(indexLabel);
		if (currentSpectra==null || currentSpectra.rows==0){
			currentSpectra=new DoubleMatrix(1,spectrum.length,spectrum);
		}
		else{
			currentSpectra=DoubleMatrix.concatVertically(currentSpectra,new DoubleMatrix(1,spectrum.length,spectrum));	
		}
		meanMap.put(indexLabel, currentSpectra);
		
		
		//Plotten des Neuen
		if(plotSelection.getSelectedItem().equals("all spectra")){
//			double[][] spect= master.getProject().getActiveView().materializeFeatureExamples(new int[]{row}, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
			Color col = Utilities.getColor(this.master.getProject().getTrainView().getLabelMap().get((int) indexLabel));
			plot.addLinePlot("",col,getWavelength(), spectrum);
		}
		
		//Plotten aller mittleren Spektren
		else if(plotSelection.getSelectedItem().equals("mean of spectra")){
			plot.removeAllPlots();

			double [] spect;
			int maxLabel = this.master.getProject().getTrainView().getLabelMap().size();
				
			//Itterate over all classres
			for(double i=1;i<=maxLabel;i++){				
				if (this.meanMap.get(i)!= null && !this.meanMap.get(i).isEmpty() ){
					spect = this.meanMap.get(i).columnMeans().toArray();
					plot.addLinePlot("",Utilities.getColor(this.master.getProject().getTrainView().getLabelMap().get(i)),getWavelength(),spect);
				}

			}
		}
		currentPlot = -1;

		plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
		plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
		this.addLabelLegend();
	}


	/**
	 * load data and coordinates of all class from xml-file
	 * @author modified by schmitter
	 */
	public void loadData(){
		if(FeatureSelection.getLabel(master.getProject().getActiveView())!=-1){
			LinkedList<Point> dots = new LinkedList<Point>();
			HashMap<Point,Color> dotsColor = new HashMap<Point,Color>();
			plot.removeAllPlots();
			currentPlot = -1;
			listModel.clear();
			master.getProject().getTrainView().getLabelMap().clear();
			if(plotSelection.getSelectedItem().equals("all spectra") || plotSelection.getSelectedItem().equals("mean of spectra"))
				plotSelection.setSelectedItem("all spectra");
			double [] activeLabel = master.getProject().getActiveView().materializeFeature(FeatureSelection.getLabel(master.getProject().getActiveView()));
			double [] tempX = master.getProject().getActiveView().materializeFeature(FeatureSelection.getXCoordinate(master.getProject().getActiveView()));
			double [] tempY = master.getProject().getActiveView().materializeFeature(FeatureSelection.getYCoordinate(master.getProject().getActiveView()));

			for (int key:master.getProject().getActiveView().getLabelMap().keySet()){
				master.getProject().getTrainView().getLabelMap().put(key, master.getProject().getActiveView().getLabelMap().get((int)key).substring(0,master.getProject().getActiveView().getLabelMap().get((int)key).length()-8));
				meanMap.put((double) key, new DoubleMatrix());

				classBox.addItem(master.getProject().getTrainView().getLabelMap().get(key));
//				classColor.put(labelMap.get(key), Color.decode(master.getProject().getActiveView().getLabelMap().get((int)key).substring(master.getProject().getActiveView().getLabelMap().get((int)key).length()-7)));

			}
			for(int i=0;i<master.getProject().getActiveView().getNumberOfExamples();i++){
				if(activeLabel[i]>0){
					Point tempPoint = new Point((int) tempX[i], (int) tempY[i]);
					classBox.setSelectedItem(master.getProject().getTrainView().getLabelMap().get(activeLabel[i]));
					this.plotSpectra(tempPoint);
					dots.add(tempPoint);
					dotsColor.put(tempPoint,Utilities.getColor(this.master.getProject().getTrainView().getLabelMap().get(classBox.getSelectedItem())));
				}
			}
			// repaint the dataPanel and the active learning panel
			this.repaint();
			this.updateUI();
			master.getAlPanel().repaint();

		}
		else
			JOptionPane.showMessageDialog(loadData, "this view contains no labels", "no labels", JOptionPane.ERROR_MESSAGE);



	}


	/**
	 * add a new Class to Dropdown- Menu 
	 * Initialized a new TrainView if necessary and adds classes to Train View
	 */
	public void addClass(){
		initTrainView();
		final String s = JOptionPane.showInputDialog( addClass,"Insert a new class name" );
		//TODO: restriktivere Abfrage bei ClassNames
		if(s!=null){
			if(!master.getProject().getTrainView().getLabelMap().containsValue(s) && !s.trim().isEmpty() ){
				final JColorChooser colorChooser = new JColorChooser();
				JLabel previewLabel = new JLabel("selected Color", JLabel.CENTER);
				previewLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 48));
				previewLabel.setSize(previewLabel.getPreferredSize());
				previewLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
				colorChooser.setPreviewPanel(previewLabel);

				ActionListener okActionListener = new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {	
						
						Set<Integer> keys = master.getProject().getTrainView().getLabelMap().keySet();
						
						Iterator<Integer> it = keys.iterator();
						Integer key; // aktueller Schlüssel
						int label = 1;
						while (it.hasNext()) // noch Schlüssel übrig
						{
							key = it.next();  							
							if((key-label)>1){
								label++;
								break;
							}
							else{
								label++;
							}

						}						
						//coordinateMap.put(label, new DoubleMatrix(0,2));
						master.getProject().getTrainView().getLabelMap().put(label, s+" #"+Integer.toHexString(colorChooser.getColor().getRGB()).substring(2));						
						meanMap.put((double) label, new DoubleMatrix());
						classBox.addItem(s);
						
						classBox.setSelectedItem(s);
						classBox.repaint();
					}
				};

				ActionListener cancelActionListener = new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
					}
				};

				final JDialog dialog = JColorChooser.createDialog(null, "Choose Color", true,
						colorChooser, okActionListener, cancelActionListener);

				dialog.setVisible(true);


			}
			else{
				if(s.trim().isEmpty())
					JOptionPane.showMessageDialog(addClass, "Please enter a name");
				else
					JOptionPane.showMessageDialog(addClass, "Class exists");
			}
		}
		this.addLabelLegend();
	}
	/**
	 * remove a class from the dropdown-menu 
	 * TODO: Veraltete Funktion! �berarbeiten oder entfernen
	 */
	public void removeClass(){

		if(classBox.getSelectedItem()!=null){
			int answer = JOptionPane.showConfirmDialog(removeClass, "Do you want to delete the Class:" +classBox.getSelectedItem() , "", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.YES_OPTION){
				for(int i = 0;i <listModel.getSize();i++){
					String []point_split = ((String) listModel.get(i)).split(",");
					Point tempPoint = new Point((int) Double.parseDouble(point_split[0]),(int) Double.parseDouble(point_split[1]));
					master.getPaintPanel().getDotsColor().remove(tempPoint);
					plot.removePlot(master.getPaintPanel().getDots().indexOf(tempPoint));
					master.getPaintPanel().getDots().remove(tempPoint);
					//activeLabel [master.getPaintPanel().getIndex(master.getProject().getActiveView()).get(tempPoint)] = 0; 
				}
				plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
				plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
				master.getProject().getTrainView().getLabelMap().remove(getKeyByValue(master.getProject().getTrainView().getLabelMap(),(String) classBox.getSelectedItem()));
				meanMap.remove(getKeyByValue(master.getProject().getTrainView().getLabelMap(),(String) classBox.getSelectedItem()));
				master.getPaintPanel().getParent().getParent().repaint();
//				classColor.remove(classBox.getSelectedItem());
				this.master.getProject().getTrainView().getLabelMap().remove(classBox.getSelectedItem());
				classBox.removeItem(classBox.getSelectedItem());
				listModel.clear();
				int[] activeLabel=this.master.getProject().getTrainView().getLabelValues();
				if(classBox.getSelectedItem()!=null & activeLabel!=null){
					double label_ind = getKeyByValue(master.getProject().getTrainView().getLabelMap(),(String) classBox.getSelectedItem());
					for(int i=0; i<activeLabel.length;i++){
						if(activeLabel[i]==label_ind)
							listModel.addElement(master.getProject().getActiveView().get(i,FeatureSelection.getXCoordinate(master.getProject().getActiveView()))+" , "+master.getProject().getActiveView().get(i,FeatureSelection.getYCoordinate(master.getProject().getActiveView())));
					}

				}
				currentPlot = -1;

			}
		}


	}



	



	/**

	/**
	 * @return
	 */
	public JButton getLoadData(){
		if(loadData ==null){
			loadData = new JButton("load label");
			loadData.setPreferredSize(new Dimension(97,20));
			loadData.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Do something here
					loadData();
				}
			});
		}
		return loadData;
	}


	/**
	 * @return
	 */
	public JComboBox<String> getClassBox(){
		
	
		return classBox;
	}

	/**
	 * @param classBox the classBox to set
	 */
	public void setClassBox(JComboBox<String> classBox) {
		this.classBox = classBox;
	}

	/**
	 * @return the currentPlot
	 */
	public int getCurrentPlot() {
		return currentPlot;
	}

	/**
	 * @param currentPlot the currentPlot to set
	 */
	public void setCurrentPlot(int currentPlot) {
		this.currentPlot = currentPlot;
	}

	/**
	 * @return
	 */
	public JButton getAddClass(){
		
		return addClass;
	}

	/**
	 * @return
	 */
	public JButton getRemoveClass(){
		if(removeClass==null){
			removeClass = new JButton("remove Class");
			removeClass.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeClass();
				}
			});
		}
		return removeClass;

	}

	/**
	 * @return the clearPlot
	 */
	public JButton getClearPlot() {
		if(clearPlot==null){
			clearPlot = new JButton("Clear Plot");
			clearPlot.setPreferredSize(new Dimension(97,20));
			clearPlot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					plot.removeAllPlots();
					currentPlot = -1;
				}
			});
		}
		return clearPlot;
	}

	/**
	 * @param clearPlot the clearPlot to set
	 */
	public void setClearPlot(JButton clearPlot) {
		this.clearPlot = clearPlot;
	}

	/**
	 * @return
	 */
	public JList<String> getClassList(){
		listModel = new DefaultListModel<String>();
		classList = new JList<String>(listModel);
		classList.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent arg0) {
				

			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				

			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				

			}

			@Override
			public void mouseReleased(MouseEvent event) {
				
				JPopupMenu popmen = new JPopupMenu();
				popmen.add( new AbstractAction("remove") {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override public void actionPerformed( ActionEvent e ) {
						int answer = JOptionPane.showConfirmDialog(removeClass, "Do you want to delete the selected pixels from this class" , "", JOptionPane.YES_NO_OPTION);
						if(answer == JOptionPane.YES_OPTION){
							for(int i:classList.getSelectedIndices()){

								String [] point_String =listModel.get(i).toString().substring(1, listModel.get(i).toString().length()-1).split(","); 	
								Point tempPoint = new Point((int) Double.parseDouble(point_String[0].trim()), (int) Double.parseDouble(point_String[1].trim()));
								master.getPaintPanel().getDotsColor().remove(tempPoint);
								int rem = master.getPaintPanel().getDots().indexOf(tempPoint);
								if(rem>0){
									plot.removePlot(master.getPaintPanel().getDots().indexOf(tempPoint));
								}
								plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
								plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
								currentPlot -=1;
								master.getPaintPanel().getDots().remove(tempPoint);
								master.getProject().getTrainView().remove(tempPoint);
							}
							int[] aray1 = classList.getSelectedIndices();
							Integer[] liste= new Integer[aray1.length];
							for (int i=0;i<liste.length;i++){
								liste[i] = new Integer(aray1[i]);
							}
							Arrays.sort(liste, Collections.reverseOrder());
							for(int i:liste)
								listModel.remove(i);
							currentPlot =-1;
							master.getPaintPanel().getParent().getParent().repaint();
							

						}

					}
				} );
				if ( event.isPopupTrigger() )
					popmen.show( event.getComponent(), event.getX(), event.getY() );

			}

			@Override
			public void mousePressed(MouseEvent event) {
				
			}


		});

		return classList;

	}

	/**
	 * @return
	 */
	public JComboBox<String> getPlotSelection(){
		if(plotSelection==null){
			plotSelection = new JComboBox<String>(new String[]{});
			plotSelection.setToolTipText("Selection of the plot");
			//selection.addItem("RGB View");
			plotSelection.addItem("all spectra");
			plotSelection.addItem("mean of spectra");
			//selection.setSelectedIndex(0);
			plotSelection.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(plotSelection.getSelectedIndex()!=-1  ){

						if(currentPlot!=-1){
							plot.removePlot(currentPlot);
							currentPlot = -1;}
						if (spectralResults.containsKey(plotSelection.getSelectedItem().toString())){
							
							if (spectralResults.get(plotSelection.getSelectedItem().toString()).size()>0){
								if(plotSelection.getSelectedItem().toString().contains("Result") && !plotSelection.getSelectedItem().toString().contains("SVM")){
									plot.removeAllPlots();
									// Number of Clusters to paint
									int numCluster = spectralResults.get(plotSelection.getSelectedItem().toString()).size();
									for(int i=0;i<numCluster;i++){
										double[] centroid = spectralResults.get(plotSelection.getSelectedItem().toString()).get(i);
										// Change range of the colormap
										ColorMap colorMap = new ColorMap( 0, numCluster, master.getAlPanel().getColorModel());
										//	Plot each centroid with the corresponding color
										plot.addLinePlot("", colorMap.getColor(i), getWavelength(), centroid);										
										
									}
								}
								if(plotSelection.getSelectedItem().toString().contains("Result") && plotSelection.getSelectedItem().toString().contains("SVM")){
									plot.removeAllPlots();
									for(int i=0;i<spectralResults.get(plotSelection.getSelectedItem().toString()).size();i++){
										double[] centroid = spectralResults.get(plotSelection.getSelectedItem().toString()).get(i);

																				

										plot.addLinePlot("",Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(plotSelection.getSelectedItem())),getWavelength(), centroid);										

									}	
								}
								if(plotSelection.getSelectedItem().toString().contains("spectra") && plotSelection.getSelectedItem().toString().contains("iteration")){
									plot.removeAllPlots();
									master.getPaintPanel().getDots().clear();
									master.getPaintPanel().getDotsColor().clear();
									for(int i=0;i<result.get(plotSelection.getSelectedItem().toString()).length;i++){
										master.getPaintPanel().getDots().add(new Point((int) master.getProject().getActiveView().get((int)result.get(plotSelection.getSelectedItem().toString())[i][1],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get((int)result.get(plotSelection.getSelectedItem().toString())[i][1],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))));
										

										master.getPaintPanel().getDotsColor().put(new Point((int) master.getProject().getActiveView().get((int)result.get(plotSelection.getSelectedItem().toString())[i][1],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get((int)result.get(plotSelection.getSelectedItem().toString())[i][1],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))),	Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(plotSelection.getSelectedItem())));

								
									}
									dotsOrg = false;
									for(int i=0;i<spectralResults.get(plotSelection.getSelectedItem().toString()).size();i++){
										double[] centroid = spectralResults.get(plotSelection.getSelectedItem().toString()).get(i);

												

										plot.addLinePlot("",Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(plotSelection.getSelectedItem())),getWavelength(), centroid);		

									}
									master.getAlPanel().repaint();
									master.getPaintPanel().getParent().getParent().repaint();
									master.getAlPanel().getRgbButton().setSelected(true);
									master.getAlPanel().getSelectVisualisation().setEnabled(false);
								}
							}							
						}
						if(plotSelection.getSelectedItem().equals("all spectra")){
							
							if(!dotsOrg){
								master.getPaintPanel().getDots().clear();
								master.getPaintPanel().getDotsColor().clear();
								DoubleMatrix labelMatrix = new DoubleMatrix(master.getProject().getTrainView().getLabelValuesAsDouble());
								int [] labelIndizes = labelMatrix.findIndices();
								for(int i=0;i<labelIndizes.length;i++){
									master.getPaintPanel().getDots().add(new Point((int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))));
									master.getPaintPanel().getDotsColor().put(new Point((int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))),	Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(labelMatrix.toArray()[labelIndizes[i]])));

							
								}
								dotsOrg = true;
							}
//							plot.removeAllPlots();
//							double [] spect;		
//							DoubleMatrix labelMatrix = new DoubleMatrix(getActiveLabel());
//							int [] labelIndizes = labelMatrix.findIndices();
//							System.out.println("Anzahl = "+labelIndizes.length);
//							for(int i=0;i<labelIndizes.length;i++){
//									spect = master.getProject().getActiveView().materializeFeatureExamples(new int[] {labelIndizes[i]}, FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()))[0]; 
//									plot.addLinePlot("",classColor.get(labelMap.get(getActiveLabel()[labelIndizes[i]])),getWavelength(),spect);
//							}
							
							plotSpectra(null);	
							
							//master.getAlPanel().getRgbButton().setSelected(true);
							master.getAlPanel().getSelectVisualisation().setEnabled(false);
						}
						if(plotSelection.getSelectedItem().equals("mean of spectra")){
							System.out.println("mean spectra");
							if(!dotsOrg){
								master.getPaintPanel().getDots().clear();
								master.getPaintPanel().getDotsColor().clear();
								DoubleMatrix labelMatrix = new DoubleMatrix(master.getProject().getTrainView().getLabelValuesAsDouble());
								int [] labelIndizes = labelMatrix.findIndices();
								for(int i=0;i<labelIndizes.length;i++){
									master.getPaintPanel().getDots().add(new Point((int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))));									
									master.getPaintPanel().getDotsColor().put(new Point((int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getXCoordinate(master.getProject().getActiveView())), (int) master.getProject().getActiveView().get(labelIndizes[i],FeatureSelection.getYCoordinate(master.getProject().getActiveView()))),		Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(labelMatrix.toArray()[labelIndizes[i]])));

							
								}
								dotsOrg = true;
							}
							
							plotSpectra(null);							

							//master.getAlPanel().getRgbButton().setSelected(true);
							master.getAlPanel().getSelectVisualisation().setEnabled(false);
						}
						if (getWavelength().length!=0){
							plot.setFixedBounds(0,getWavelength()[0], getWavelength()[getWavelength().length-1]);
							plot.setFixedBounds(1,master.getProject().getActiveView().getMinValue(), master.getProject().getActiveView().getMaxValue());
						}
						master.getPaintPanel().repaint();
					}
				}
			});



		}

		return plotSelection;
	}

	/**
	 * @return
	 */
	public JPanel getClassPanel(){
		if(classPanel==null){
			classPanel = new JPanel();
			classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.PAGE_AXIS));
			JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.LINE_AXIS));

			tempPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//	tempPanel.add(getSaveData());
//			tempPanel.add(Box.createRigidArea(new Dimension(10, 0)));		
			tempPanel.add(getLoadData());
			tempPanel.add(Box.createHorizontalGlue());			
//			tempPanel.add(Box.createHorizontalStrut(5));
			tempPanel.add(getClearPlot());

			JPanel tempPanel1 = new JPanel();
			tempPanel1.setLayout(new BoxLayout(tempPanel1, BoxLayout.LINE_AXIS));

			tempPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));	
			tempPanel1.add(getClassBox());
			tempPanel1.add(Box.createRigidArea(new Dimension(10, 0)));
			
			tempPanel1.add(this.addClass);
			tempPanel1.add(Box.createRigidArea(new Dimension(10, 0)));

			tempPanel1.add(getRemoveClass());

			classPanel.add(tempPanel,BorderLayout.CENTER);
			classPanel.add(tempPanel1, BorderLayout.CENTER);

			classPanel.add(new JScrollPane(getClassList()),BorderLayout.SOUTH);
		}
		setItemsForComboBox(this.classBox);
		return classPanel;
	}
	
	public void setItemsForComboBox(JComboBox<String> comboBox){
//		for (int i = 0; i<comboBox.getItemCount();i++)
//			comboBox.removeItemAt(i);
		comboBox.removeAllItems();
		if (master.getProject().getTrainView()!=null){
			Set<Integer> keys = master.getProject().getTrainView().getLabelMap().keySet();
			Iterator<Integer> it = keys.iterator();
			Integer key; // aktueller Schlüssel		
			while (it.hasNext()) // noch Schlüssel übrig
			{
				key = it.next();				
				String[] s =master.getProject().getTrainView().getLabelMap().get(key).split(" ");
				comboBox.addItem(s[0]);
			}
		}
	}

	/**
	 * @return
	 */
	public JTabbedPane getTabPanel(){
		if(tabPanel==null){
			this.tabPanel = new JTabbedPane();
			this.tabPanel.add("create Classes",getClassPanel());
			this.postProcessControl = new PostProcessPanel(master, this);
			this.tabPanel.add("active learning",postProcessControl);
			this.wcp = new WekaClassifierPanel(master);
			this.tabPanel.add("Classification", wcp);
			this.ulp = new WekaPanel(master);
			this.tabPanel.add("Clustering", ulp);
			this.fip = new FilterPanel(master);
			this.tabPanel.add("Filtering", fip);
//			tabPanel.setEnabledAt(1,false);
			this.tabPanel.add("Image processing", getImageProcessingPanel());
			
			this.tabPanel.getModel().addChangeListener(new ChangeListener() {
			    int lastTabIndex = -1;
			    
			    public void stateChanged(ChangeEvent e) {
			         int newIndex = tabPanel.getSelectedIndex();

			         //leaving tab Image processing
			         if (lastTabIndex == 4) {
			        	 getImageProcessingPanel().clearSelections();
			         }

			         lastTabIndex = newIndex;
			         if(master.getProject().getActiveView()!=null){
			        	 fip.setDefaultName(master.getProject().getActiveView().getViewName() + "_filtered");
			    
			         }
			    }
			});
		}
		return tabPanel;
	}
	
	/**
	 * @return the imageProcessingPanel
	 */
	public ImageProcessingPanel getImageProcessingPanel() {
		if(imageProcessingPanel== null){
			imageProcessingPanel = new ImageProcessingPanel(master);
		}
		return imageProcessingPanel;
	}
	
	/**
	 * @return the result
	 */
	public HashMap<String, double[][]> getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(HashMap<String, double[][]> result) {
		this.result = result;
	}

	/**
	 * @return the spectralResults
	 */
	public HashMap<String, LinkedList<double[]>> getSpectralResults() {
		return spectralResults;
	}

	/**
	 * @param spectralResults the spectralResults to set
	 */
	public void setSpectralResults(
			HashMap<String, LinkedList<double[]>> spectralResults) {
		this.spectralResults = spectralResults;
	}

	public JButton getsave2DButton(){
		if (save2DData==null){
			save2DData = new JButton();
			save2DData.setText("save 2D plot");
			save2DData.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						// open save dialog
						JFileChooser chooser = new JFileChooser(new File("logos"));
						chooser.setSelectedFile(new File("plot.png"));
						chooser.showSaveDialog(null);

						// open a new window to show plot in fullscreen (higher resolution)
						JFrame fullScreen = new JFrame();
						fullScreen.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
						Plot2DPanel plotFS = new Plot2DPanel();
						fullScreen.add(plotFS);
						fullScreen.setVisible(true);
						

//						// add plot elements and title to the fullscreen plot and scale the axis
						double minx=Double.MAX_VALUE;
						double maxx=Double.MIN_VALUE;
						double miny=Double.MAX_VALUE;
						double maxy=Double.MIN_VALUE;
						
						for(Plot linePlot:getPlot().getPlots()){
							plotFS.addPlot(linePlot); // Linien
							if(minx>linePlot.getData()[0][0])
								minx= linePlot.getData()[0][0];
							if(maxx<linePlot.getData()[linePlot.getData().length-1][0])
								maxx= linePlot.getData()[linePlot.getData().length-1][0];
							
							double max = linePlot.getData()[0][1];
							double min = linePlot.getData()[0][1];							
						      for (int j = 1; j < linePlot.getData().length; j++) {
						          if (linePlot.getData()[j][1] > max) {
						              max = linePlot.getData()[j][1];
						          }
						          if (linePlot.getData()[j][1] < min) {
						              min = linePlot.getData()[j][1];
						          }
						      }
						      if(miny>min)
									miny= min;
								if(maxy<max)
									maxy= max;
							
						}
						plotFS.setFixedBounds(0,minx, maxx);
						plotFS.setFixedBounds(1, miny, maxy);
						plotFS.setAxisLabel(0,"Wavelength [nm]");
						plotFS.setAxisLabel(1, "Reflectance [-]");
						
						

						// save the fullscreen plot
						plotFS.plotToolBar.setVisible(false);
						plotFS.toGraphicFile(new File(chooser.getSelectedFile().toString()));
						fullScreen.setVisible(false);
						
					} catch (IOException ei) {
						ei.printStackTrace();
					}
				}
			});
		}
		return save2DData;
	}




	/**
	 * @return the plot
	 */
	public Plot2DPanel getPlot() {
		return plot;
	}

	/**
	 * @param plot the plot to set
	 */
	public void setPlot(Plot2DPanel plot) {
		this.plot = plot;
	}




	/**
	 * @param name
	 * @param image
	 */
	public void addImage(String name, BufferedImage image){
		
		plotSelection.addItem(name);
	}

	/**
	 * @param name
	 * @param spectralResult
	 * @param image
	 */
	public void addImage(String name, LinkedList<double[]>spectralResult,BufferedImage image){
		
		this.spectralResults.put(name,spectralResult);
		rebuildSelection(name);
	}

	/**
	 * @param name
	 * @param spectralResult
	 * @param image
	 * @param result
	 */
	public void addImage(String name, LinkedList<double[]>spectralResult,BufferedImage image, double[][] result){
		this.spectralResults.put(name,spectralResult);
		this.result.put(name,result);
		rebuildSelection(name);
	}

	/**
	 * 
	 */
	public void rebuildSelection(String name){
		plotSelection.addItem(name);					
	}

	public void rebuildSelection(String name, LinkedList<double[]>spectralResult, double[][] result){
		this.spectralResults.put(name,spectralResult);
		this.result.put(name,result);
		plotSelection.addItem(name);	
	}

	/**
	 * @return the postProcessControl
	 */
	public PostProcessPanel getPostProcessControl() {
		return postProcessControl;
	}

	/**
	 * @param postProcessControl the postProcessControl to set
	 */
	public void setPostProcessControl(PostProcessPanel postProcessControl) {
		this.postProcessControl = postProcessControl;
	}

	public double getKeyByValue(Map<Integer, String> map, String selectedClass) {		
		for (Entry<Integer, String> entry : map.entrySet()) {	
			String[] string = entry.getValue().split(" #");
			if (selectedClass.equals(string[0])) {
				return  entry.getKey();
			}
		}
		return -1;
	}

	


	/**
	 * @param v
	 * @return
	 */
	public double[] getWavelength(){
		if(wavelength ==null){
			String [] waveString = master.getProject().getActiveView().getFeatureDescriptors();
			int [] featureIdx = FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView());
			wavelength = new double [featureIdx.length];
			for (int i=0; i< featureIdx.length;i++)
				wavelength[i] = Double.parseDouble(waveString[featureIdx[i]]);
		}
		return wavelength;

	}

	/**
	 * @param wavelength the wavelength to set
	 */
	public void setWavelength(double[] wavelength) {
		this.wavelength = wavelength;
	}

	/**
	 * @return the listModel
	 */
	public DefaultListModel<String> getListModel() {
		return listModel;
	}

	/**
	 * @param listModel the listModel to set
	 */
	public void setListModel(DefaultListModel<String> listModel) {
		this.listModel = listModel;
	}

//	
//	/**
//	 * Adds the Coordinates to the CoordinatesMap
//	 * @param indexLabel  Class index
//	 * @param p Coordinates
//	 */
//	public void addCoordinates(int indexLabel, Point p) {
//		DoubleMatrix old_coords= this.coordinateMap.get(indexLabel);
//		DoubleMatrix new_coords=DoubleMatrix.concatVertically(old_coords, new DoubleMatrix(1,2,new double[]{p.x,p.y}));
//		this.coordinateMap.put(indexLabel, new_coords);
//	}
	/**
	 * Reloads and display the coordinates in coordinateMap
	 * The coordinate Information are grabbed from the Current TrainView 
	 * @author jbehmann
	 */
	public void reloadCoordinates() {
		//Reset the Coordinate-List
		listModel.clear();
		
		
		FeatureRole[] roles= this.master.getProject().getTrainView().getFeatureRoles();
		int index = -1;
		int indexLabel=-2;
		int indexX=-2;
		int indexY=-2;
		for (FeatureRole role: roles){
			index++;
			if (role==FeatureRole.LABEL){
				indexLabel=index;
			}
			if (role==FeatureRole.X){
				indexX=index;
			}
			if (role==FeatureRole.Y){
				indexY=index;
			}
		}
		TrainView tv = (TrainView) this.master.getProject().getTrainView();
		
		// Request for all required columns of the TrainView
		if(indexLabel != -2 && indexY != -2 && indexX != -2 && tv!=null && tv.getNumberOfExamples()!=0 && tv.materialize()!=null){
			int classIndex = this.findColorIndex(this.getClassBox().getSelectedItem().toString());
			DoubleMatrix label = new DoubleMatrix(this.master.getProject().getTrainView().materializeFeature(indexLabel));
			DoubleMatrix coordinates = new DoubleMatrix(this.master.getProject().getTrainView().materializeFeatures(new int[]{indexX, indexY}));
		    DoubleMatrix classSamples=label.eq(classIndex);
		    
		    for (int i =0; i<classSamples.rows; i++){
		    	//Add the coordinates if its class is the active class 
		    	if(classSamples.get(i)==1.0)
		    		listModel.addElement("("+coordinates.get(i,0) + "," + coordinates.get(i,1)+ ")");// in "+ tv.getgetImageReference()[i]);	
		    }
		}
				
	}
	 /**
	  * Reloads the cordinates, classes and colors from the current TrainView if the active View has changed
	  * @author jbehmann
	  * 
	  */
	public void loadTrainView(){ 
		// Only if a TrainView is available
		if (this.master.getProject().getTrainView()!=null){
			setItemsForComboBox(this.classBox);
			/*
			TrainView tv = this.master.getProject().getTrainView();
			int classIndex = this.findColorIndex(this.getClassBox().getSelectedItem().toString());
			
			// Test if all required Columns are available in the TrainView
			FeatureRole[] roles= tv.getFeatureRoles();
			int index = -1;
			int indexLabel=-2;
			int indexX=-2;
			int indexY=-2;
			for (FeatureRole role: roles){
				index++;
				if (role==FeatureRole.LABEL){
					indexLabel=index;
				}
				if (role==FeatureRole.X){
					indexX=index;
				}
				if (role==FeatureRole.Y){
					indexY=index;
				}
			}
			
			
			
			// If all required columns are available -> test
			if(indexLabel !=-2 && indexY !=-2 && indexX != -2 && tv!=null && tv.getNumberOfExamples()!=0 && tv.materialize()!=null ){
				DoubleMatrix label = new DoubleMatrix(tv.materializeFeatureExamples(FeatureSelection.getLabel(tv)));
				DoubleMatrix coordinates = new DoubleMatrix(this.master.getProject().getTrainView().materializeFeatureExamples(trainIndex , new int[]{indexX, indexY}));
			    DoubleMatrix classSamples=label.eq(classIndex);
			    
			    LinkedList<Point> dotList = new LinkedList<Point>();
			    HashMap<Point,Color> dotsColor = new HashMap<Point,Color>();
			    for (int i =0; i<classSamples.rows; i++){
			    	//Add the coordinates of the selected class
			    	if(classSamples.get(i)==1.0){
			    		listModel.addElement("("+coordinates.get(i,0) + "," + coordinates.get(i,1)+ ") in "+ viewList[trainIndex[i]]);
			    	}
			    	
			    	//Add the points and colors labeled in the current View 
			    	Point p = new Point();
		    		p.x= (int)coordinates.get(i,0);
		    		p.y= (int) coordinates.get(i,1);
		    		dotList.add(p);
		    		String[] stringColors = tv.getLabelMap().get((int)label.get(i)).split(" ");
		    		String colorString= stringColors[stringColors.length-1];
		    		dotsColor.put(p,getColor(colorString));
		    	}
			    
			    // Setting the dots and Colors to the image
			    this.master.getPaintPanel().setDots(dotList);
			    this.master.getPaintPanel().setDotsColor(dotsColor);	    
			    
			}	*/
		}		
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
		plot.addLegend("SOUTH");
		LegendPanel legends=plot.plotLegend;
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
	
}
