/**
 * 
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.IndexColorModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import data.FeatureRole;
import data.FeatureSelection;
import data.View;
import edu.mines.jtk.awt.ColorMap;

/**
 * @author jkleinma
 *
 */
public class AlPanel extends JPanel{

	private static final long serialVersionUID = -204817714436935578L;

	private ImagePanel paintPanel;
//	private LinkedList<Point> dots;
	private JPanel imageViewPanel;
	private JPanel plotPanel;
	private JRadioButton rgbButton;
	private JRadioButton grayButton;
	private JRadioButton clusterButton;
	private JTextField xCoordinate;
	private JTextField yCoordinate;
	private ButtonGroup group;
	private JComboBox<String> waveBox;
	private JComboBox<String> selectColorMap;
	
	private JComboBox<String> red;
	private JComboBox<String> green;
	private JComboBox<String> blue;
	private boolean changeStatus;
	
	private DataPanel dataPanel;
	private MainGui master;
	
	/**
	 *  ColorModel for Clustering and 2D-Plot
	 *  @author schmitter
	 */
	private IndexColorModel colorModel;
	/**
	 *  Button to save the current Image
	 *  @author schmitter
	 */
	private JButton saveButton;


	/**
	 * @param main
	 */
	public AlPanel(MainGui main) {
		// TODO Auto-generated constructor stub
		super();
		master = main;
		this.paintPanel = master.getPaintPanel();
		this.dataPanel = master.getDataPanel();
		this.paintPanel.getMouseMotionListeners();

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		imageViewPanel = new JPanel();
		imageViewPanel.setBorder(BorderFactory.createTitledBorder("Image"));
		imageViewPanel.setLayout(new BorderLayout());
		
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.LINE_AXIS));
		radioPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		radioPanel.add(getRgbButton());
		radioPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		radioPanel.add(getGrayButton());
		radioPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		radioPanel.add(getClusterButton());
		
		// Add Textfield and Labels for current cursor position
		xCoordinate = new JTextField("0");
		xCoordinate.setEditable(false);
		yCoordinate = new JTextField("0");
		yCoordinate.setEditable(false);
		radioPanel.add( Box.createRigidArea(new Dimension(50, 0)));
		radioPanel.add( new JLabel("X:"));
		radioPanel.add( Box.createRigidArea(new Dimension(10, 0)));
		radioPanel.add(xCoordinate);
		radioPanel.add( Box.createRigidArea(new Dimension(20, 0)));
		radioPanel.add( new JLabel("Y:"));
		radioPanel.add( Box.createRigidArea(new Dimension(10, 0)));
		radioPanel.add(yCoordinate);
		
		// Add Save Image Button
		radioPanel.add(getSaveButton());
		
		group = new ButtonGroup();
		group.add(getRgbButton());
		group.add(getGrayButton());
		group.add(getClusterButton());
		imageViewPanel.add(radioPanel,BorderLayout.PAGE_START);
		imageViewPanel.add(Box.createRigidArea(new Dimension(0,5)));
		imageViewPanel.add(paintPanel.getImagePanel(),BorderLayout.CENTER);
		imageViewPanel.add(getSelectVisualisation(),BorderLayout.SOUTH);
		
		plotPanel = new JPanel();
		plotPanel.setLayout(new BoxLayout(plotPanel,BoxLayout.PAGE_AXIS));
		plotPanel.setBorder(BorderFactory.createTitledBorder("Data Selection"));
		plotPanel.add(dataPanel);

		this.add(imageViewPanel);
		this.add(plotPanel);
		
		// Set default colormap to "jet"
		this.colorModel = ColorMap.JET;
	}

	



	/**
	 * @return
	 */
	public ImagePanel getPaintPanel() {
		return paintPanel;
	}


	/**
	 * @param paintPanel
	 */
	public void setPaintPanel(ImagePanel paintPanel) {
		this.paintPanel = paintPanel;
	}


	/**
	 * Check Box for RGB data. visualize data
	 * @return the rgbButton
	 */
	public JRadioButton getRgbButton() {
		if (rgbButton ==null){
			rgbButton = new JRadioButton("RGB View");
			rgbButton.setSelected(true);
			rgbButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					//Do something here
					waveBox.setEnabled(false);
					selectColorMap.setEnabled(false);
					
					red.setEnabled(true);
					red.removeAllItems();
					green.setEnabled(true);
					green.removeAllItems();				
					blue.setEnabled(true);
					blue.removeAllItems();
					
					// Fill rgb with feature names
					String [] waveString = master.getProject().getActiveView().getFeatureDescriptors();
					int [] featureIdx = FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView());
					changeStatus = false;
					for(int i=0; i<featureIdx.length;i++){	
						red.addItem(waveString[featureIdx[i]]);
						green.addItem(waveString[featureIdx[i]]);
						blue.addItem(waveString[featureIdx[i]]);
					}
					changeStatus = true;
					
					if(master.getProject().getActiveViewIndex()>=0){
						if(!master.getDataPanel().getPlotSelection().getSelectedItem().toString().contains("all spectra") 
								&& !master.getDataPanel().getPlotSelection().getSelectedItem().toString().contains("mean of spectra")){
							for( int i=0;i<master.getDataPanel().getPlotSelection().getItemCount();i++){
								if(master.getDataPanel().getPlotSelection().getItemAt(i).toString().contains("all spectra") 
										|| master.getDataPanel().getPlotSelection().getItemAt(i).toString().contains("mean of spectra")){
									master.getDataPanel().getPlotSelection().setSelectedIndex(i);
									break;
								}
							}
						}
						paintPanel.displayRGB( master.getProject().getActiveView());
						
						
						double rrr = 680; double ggg = 540; double bbb = 435;
						red.setSelectedIndex( findClosestWavelength( rrr));
						green.setSelectedIndex(findClosestWavelength( ggg));
						blue.setSelectedIndex( findClosestWavelength( bbb));
					}
				}
			});
		}
		return rgbButton;
	}


	/**
	 * @param rgbButton the rgbButton to set
	 */
	public void setRgbButton(JRadioButton rgbButton) {
		this.rgbButton = rgbButton;
	}


	/**
	 * @return the grayButton
	 */
	public JRadioButton getGrayButton() {
		if(grayButton==null){
			grayButton = new JRadioButton("Gray Scale");
			grayButton.setEnabled(true);
			grayButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(master.getProject().getActiveViewIndex()>=0){
						waveBox.setEnabled(true);
						selectColorMap.setEnabled(true);
						red.setEnabled(false);
						green.setEnabled(false);			
						blue.setEnabled(false);
						
						// Fill waveBox with feature names
						waveBox.removeAllItems();
						String [] waveString = master.getProject().getActiveView().getFeatureDescriptors();
						int [] featureIdx = FeatureSelection.getFeatures(master.getProject().getActiveView());
						for(int i=0; i<featureIdx.length;i++){	
							waveBox.addItem(waveString[featureIdx[i]]);
						}
						//waveBox.setSelectedIndex(0);
						
						if(!master.getDataPanel().getPlotSelection().getSelectedItem().equals("all spectra") &&
								!master.getDataPanel().getPlotSelection().getSelectedItem().equals("mean of spectra")){
							for( int i=0;i<master.getDataPanel().getPlotSelection().getItemCount();i++){
								if(master.getDataPanel().getPlotSelection().getItemAt(i).equals("all spectra") ||
										master.getDataPanel().getPlotSelection().getItemAt(i).equals("mean of spectra")){
									master.getDataPanel().getPlotSelection().setSelectedIndex(i);
									break;
								}
							}
						}
						//paintPanel.displaySingleBand(master.getProject().getActiveView(), waveBox.getSelectedIndex());		
						//System.out.println("Welenlängen # : "+  Math.floor(waveBox.getItemCount()/2.0));
						if(waveBox.getItemCount()>0)
							waveBox.setSelectedIndex((int) Math.floor(waveBox.getItemCount()/2.0));
						paintPanel.repaint();
						master.repaint();
					}
				}
			});

		}
		return grayButton;
	}


	/**
	 * @param grayButton the grayButton to set
	 */
	public void setGrayButton(JRadioButton grayButton) {
		this.grayButton = grayButton;
	}


	/**
	 * @return the clusterButton
	 */
	public JRadioButton getClusterButton() {
		if (clusterButton ==null){
			clusterButton = new JRadioButton("Clustering");		
			clusterButton.setEnabled(true);
			clusterButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//Do something here
					waveBox.setEnabled(false);
					red.setEnabled(false);
					green.setEnabled(false);			
					blue.setEnabled(false);
					selectColorMap.setEnabled(true);
					
					if(master.getDataPanel().getPlotSelection().getSelectedItem()!=null){
						if(master.getDataPanel().getPlotSelection().getSelectedItem().toString().contains("spectra") 
								|| master.getDataPanel().getPlotSelection().getSelectedItem().toString().contains("spectra")){
							for( int i=0;i<master.getDataPanel().getPlotSelection().getItemCount();i++){
								if(!master.getDataPanel().getPlotSelection().getItemAt(i).toString().contains("spectra") 
										&& !master.getDataPanel().getPlotSelection().getItemAt(i).toString().contains("spectra")){
									master.getDataPanel().getPlotSelection().setSelectedIndex(i);
									break;
								}
							}
						}
						
						paintPanel.displayLabel( master.getProject().getActiveView());
						paintPanel.repaint();
						master.repaint();
					}
				}
			});
		}
		return clusterButton;
	}
	
	public JButton getSaveButton(){
		this.saveButton = new JButton("save");
		this.saveButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				master.getAlPanel().getPaintPanel().saveImage();
			}
		});
		return saveButton;
	}


	/**
	 * @param clusterButton the clusterButton to set
	 */
	public void setClusterButton(JRadioButton clusterButton) {
		this.clusterButton = clusterButton;
	}
	
	

	/**
	 * Find the feature with the clostest wavelength to a given wavelength
	 * @param waveLength in nm
	 * @return mini position of the found feature
	 */
	public int findClosestWavelength( double waveLength){
		String[] desciptors = master.getProject().getActiveView().getFeatureDescriptors();
		FeatureRole[] roles = master.getProject().getActiveView().getFeatureRoles();
		int countNonFeatures = 0;
		int mini = -1; double miniValue = Double.MAX_VALUE;
		for (int i=0; i<desciptors.length; i++){
			if (roles[i].equals(FeatureRole.FEATURE)){
				double diff = Double.MAX_VALUE;
				try {
					diff = Math.abs( Double.parseDouble(desciptors[i]) - waveLength);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(master, "Not a Number", "Wrong Input Format", JOptionPane.INFORMATION_MESSAGE);
				}
				if (diff < miniValue){
					mini = i;
					miniValue = diff;
				}
			}
			else
				countNonFeatures++;
		}
		return mini-countNonFeatures;
	}
	

	/**
	 * Construct selection boxes for the feature and different visualisations
	 * @return JPanel selectVisualisation
	 * @author schmitter
	 */
	public JPanel getSelectVisualisation() {
		JPanel selectVisualisation = new JPanel();
		this.changeStatus = true;
		if(waveBox==null){
			selectVisualisation.setLayout(new GridLayout(1,4));
			this.waveBox = new JComboBox<String>();
			this.waveBox.setToolTipText("Selection of the wavelength");
			this.waveBox.setVisible(true);
			this.waveBox.setBorder(BorderFactory.createTitledBorder("Wavelength"));
			this.waveBox.setEnabled(false);
			this.waveBox.addItemListener(new ItemListener() {
				// Change selected feature
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						Object item = event.getItem();
						String[] desc= master.getProject().getActiveView().getFeatureDescriptors();
						int[] index= FeatureSelection.getFeatures(master.getProject().getActiveView());
						//int clostestWavelength = findClosestWavelength( Double.parseDouble(item.toString()));
						int clostestWavelength=-1;
						for (int i = 0; i<index.length;i++){
							if (desc[index[i]].equals(item.toString())){
								clostestWavelength=i;
							}
						}
						
						if(item != null && clostestWavelength<0)
							paintPanel.displaySingleBand(master.getProject().getActiveView(), waveBox.getSelectedIndex());
						if(item != null)
							paintPanel.displaySingleBand(master.getProject().getActiveView(), clostestWavelength);
					}
				}			

			});
			this.waveBox.setEditable(true);
			
			// creat combobox to select the colormap
			this.selectColorMap = new JComboBox<String>();
			this.selectColorMap.setToolTipText("Select Color Map");
			this.selectColorMap.setVisible(true);
			this.selectColorMap.setBorder(BorderFactory.createTitledBorder("Color Map"));
			this.selectColorMap.setEnabled(false);
			
			// fill combobox with different colormaps
			this.selectColorMap.addItem("Jet");
			this.selectColorMap.addItem("Gray");
			this.selectColorMap.addItem("Hue");
			this.selectColorMap.addItem("Prism");
			this.selectColorMap.addItem("Red-White-Blue");
			
			// add listener when colormap gets changed
			this.selectColorMap.addItemListener( new ItemListener() {
				public void itemStateChanged(ItemEvent event) {
					if (event.getStateChange() == ItemEvent.SELECTED) {
						// change the selected colormap
						int selectedIndex = selectColorMap.getSelectedIndex();
						switch (selectedIndex){
						case 0:
							master.getAlPanel().setColorModel( ColorMap.JET);
							break;
						case 1:
							master.getAlPanel().setColorModel( ColorMap.GRAY);
							break;
						case 2:
							master.getAlPanel().setColorModel( ColorMap.HUE);
							break;
						case 3:
							master.getAlPanel().setColorModel( ColorMap.PRISM);
							break;
						case 4:
							master.getAlPanel().setColorModel( ColorMap.RED_WHITE_BLUE);
							break;
						}
						// if the visualisation of one feature is selected
						if (master.getAlPanel().getGrayButton().isSelected()){
							//int clostestWavelength = findClosestWavelength( Double.parseDouble( waveBox.getSelectedItem().toString()));
							
							String[] desc= master.getProject().getActiveView().getFeatureDescriptors();
							int[] index= FeatureSelection.getFeatures(master.getProject().getActiveView());
							//int clostestWavelength = findClosestWavelength( Double.parseDouble(item.toString()));
							int clostestWavelength=-1;
							for (int i = 0; i<index.length;i++){
								if (desc[index[i]].equals(waveBox.getSelectedItem().toString())){
									clostestWavelength=i;
								}
							}
							
							if(event.getItem() != null && clostestWavelength<0)
								paintPanel.displaySingleBand(master.getProject().getActiveView(), waveBox.getSelectedIndex());
							if(event.getItem() != null)
								paintPanel.displaySingleBand(master.getProject().getActiveView(), clostestWavelength);
						}		
						if (master.getAlPanel().getClusterButton().isSelected())
							paintPanel.displayLabel( master.getProject().getActiveView());
					}
				}
			});
			
			// add combobox for the red channel
			this.red = new JComboBox<String>();
			this.red.setToolTipText("Selection of feature for the red channel");
			this.red.setVisible(true);
			this.red.setBorder(BorderFactory.createTitledBorder("Red Channel"));
			this.red.setEnabled(false);
			this.red.addItemListener( new ChangeImageListener());
			this.red.setEditable(true);
			
			// add combobox for the green channel
			this.green = new JComboBox<String>();
			this.green.setToolTipText("Selection of feature for the green channel");
			this.green.setVisible(true);
			this.green.setBorder(BorderFactory.createTitledBorder("Green Channel"));
			this.green.setEnabled(false);
			this.green.addItemListener( new ChangeImageListener());
			this.green.setEditable(true);
			
			// add combobox for the blue channel
			this.blue = new JComboBox<String>();
			this.blue.setToolTipText("Selection of feature for the blue channel");
			this.blue.setVisible(true);
			this.blue.setBorder(BorderFactory.createTitledBorder("Blue Channel"));
			this.blue.setEnabled(false);
			this.blue.addItemListener( new ChangeImageListener());
			this.blue.setEditable(true);
			
			// add components to the panel 
			selectVisualisation.add(red);
			selectVisualisation.add(green);
			selectVisualisation.add(blue);
			selectVisualisation.add(waveBox);
			selectVisualisation.add(selectColorMap);
		}
		return selectVisualisation;
	}
		
	
	/**
	 * ItemListener changes image if wavelength was changed 
	 * @author schmitter
	 */
	private class ChangeImageListener implements ItemListener{

		public void itemStateChanged(ItemEvent event) {
			if (event.getStateChange() == ItemEvent.SELECTED  && changeStatus){
				try{
					// estimate and visulize new image
					paintPanel.displayImage( master.getProject().getActiveView(), 
							Double.parseDouble(red.getSelectedItem().toString()),
							Double.parseDouble(green.getSelectedItem().toString()),
							Double.parseDouble(blue.getSelectedItem().toString()));
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(master, "Not a Number", "Wrong Input Format", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
	}
	
	
	/**
	 * @param waveBox the waveBox to set
	 */
	public void setWaveBox(JComboBox<String> waveBox) {
		this.waveBox = waveBox;
	}


	/**
	 * @return the dataPanel
	 */
	public DataPanel getDataPanel() {
		return dataPanel;
	}


	/**
	 * @param dataPanel the dataPanel to set
	 */
	public void setDataPanel(DataPanel dataPanel) {
		this.dataPanel = dataPanel;
	}


	public void resetData(View v){

		getDataPanel().getClearPlot().doClick();
		getDataPanel().getClassBox().removeAllItems();
		//getDataPanel().getClassColor().clear();
		getDataPanel().getListModel().clear();
		getDataPanel().setWavelength(null);
		getDataPanel().getPlotSelection().removeAllItems();
		getClusterButton().setEnabled(true);
		getPaintPanel().getDots().clear();
		getPaintPanel().getDotsColor().clear();
		//		System.out.println(getPaintPanel().minSpectrum + " "+minSpectrum);
		v.materializeMinMaxValue();
		getDataPanel().getPlotSelection().addItem("all spectra");
		getDataPanel().getPlotSelection().addItem("mean of spectra");		
		
		getPaintPanel().generateIndex(v);			
	}


	/**
	 * @return the xCoordinate
	 */
	public JTextField getxCoordinate() {
		return xCoordinate;
	}


	/**
	 * @param xCoordinate the xCoordinate to set
	 */
	public void setxCoordinate(JTextField xCoordinate) {
		this.xCoordinate = xCoordinate;
	}


	/**
	 * @return the yCoordinate
	 */
	public JTextField getyCoordinate() {
		return yCoordinate;
	}

	/**
	 * @param yCoordinate the yCoordinate to set
	 */
	public void setyCoordinate(JTextField yCoordinate) {
		this.yCoordinate = yCoordinate;
	}

	/**
	 * @return the imageViewPanel
	 */
	public JPanel getImageViewPanel() {
		return imageViewPanel;
	}


	/**
	 * @param imageViewPanel the imageViewPanel to set
	 */
	public void setImageViewPanel(JPanel imageViewPanel) {
		this.imageViewPanel = imageViewPanel;
	}


	/**
	 * @return the colorModel
	 */
	public IndexColorModel getColorModel() {
		return colorModel;
	}


	/**
	 * @param colorModel the colorModel to set
	 */
	public void setColorModel(IndexColorModel colorModel) {
		this.colorModel = colorModel;
	}


	/**
	 * @return the waveBox
	 */
	public JComboBox<String> getWaveBox() {
		return waveBox;
	}


	/**
	 * @return the selectColorMap
	 */
	public JComboBox<String> getSelectColorMap() {
		return selectColorMap;
	}


	/**
	 * @param selectColorMap the selectColorMap to set
	 */
	public void setSelectColorMap(JComboBox<String> selectColorMap) {
		this.selectColorMap = selectColorMap;
	}




}
