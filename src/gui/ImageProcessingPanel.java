/**
 * 
 */
package gui;

import imageprocessing.ImageSelection;
import imageprocessing.Mode;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jblas.DoubleMatrix;

import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.fileformats.MatlabFormat;
import data.inmemory.DoubleMatrixView;
import data.operators.ExampleFilter;
import data.operators.FeatureFilter;
import data.operators.MergeFeaturesView;

/**
 * This class is the JPanel for the ImageProcessing tab in the Active learning panel.
 * There are currently three modes for selecting parts of the image: Rectangle, Polygon and Lasso
 * The parts can either be deleted from the image or used to create a new view.
 * 
 * 
 * @author Till
 *
 */
public class ImageProcessingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainGui master;
	private JButton rect;
	private JButton poly;
	private JButton lasso;
	
	protected static boolean editMode = false;
	private JButton removeBtn;
	private JButton newViewBtn;
	private JButton addTrainDataBtn;
	private Mode selectionMode;
//	private ArrayList<Shape> shapes;
	



	/**
	 * @param master
	 */
	public ImageProcessingPanel(MainGui master) {
		super();
		this.master = master;
		this.initialise();
	}



	private void initialise() {
		
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		
		JPanel left = new JPanel();
		left.setBorder(BorderFactory.createTitledBorder("Selection mode"));
		left.setLayout(new GridLayout(2, 2));
		rect = new JButton("Rectangle");
		rect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				getThis().setEditMode(true,Mode.RECT);

			}
		});

		left.add(getRect());
		poly = new JButton("Polygon");
		poly.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				getThis().setEditMode(true,Mode.POLY);
			}
		});

		left.add(getPoly());
		lasso = new JButton("Lasso");
		lasso.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				getThis().setEditMode(true,Mode.LASSO);

			}
		});
		left.add(getLasso());
		
		JPanel right = new JPanel();
		right.setBorder(BorderFactory.createTitledBorder("Process"));
		right.setLayout(new GridLayout(2, 2));
		removeBtn = new JButton("Remove");
		removeBtn.setEnabled(false);
		removeBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try{
					View view = getMaster().getProject().getActiveView();
					Shape shp = getMaster().getPaintPanel().getSelShape();
					
					// Select all areas outside all selected shapes
					int[] indInt = ImageSelection.selectOutside(view, shp);
					
					ExampleFilter newView = new ExampleFilter(view, indInt);
					//TODO: Koordinaten anpassen?
					newView.setViewName(view.getViewName()+"_selection");
					newView.materializeXYDimension();

//					getMaster().getProject().add(newView);
					int activeViewIndex = new Integer(getMaster().getProject().getActiveViewIndex());
					getMaster().getProject().getViewList().set(activeViewIndex, newView);
					getMaster().getDataManagementPanel().actualiseListen();

					getMaster().getTabbedPane().setSelectedIndex(0);
					getMaster().getProject().setActiveView(activeViewIndex);
					getMaster().getTabbedPane().setSelectedIndex(2);
					
					clearSelections();
					
					
				}catch(Exception e){
					clearSelections();
				}

			}
		});
		
		right.add(getRemoveBtn());
		
		newViewBtn = new JButton("Create new View");
		newViewBtn.setEnabled(false);
		newViewBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try{
					View view = getMaster().getProject().getActiveView();
					Shape shp = getMaster().getPaintPanel().getSelShape();
					
					// Select all areas inside all selected shapes
					int[] indInt = ImageSelection.selectInside(view, shp);

					ExampleFilter newView = new ExampleFilter(view, indInt);
					double[][] koordinaten = Utilities.materializeFeatures(newView, FeatureSelection.getCoordinates(newView));

					// Adapt coordinates to start with (1,1)
					double[][] neueKoordinaten = ImageSelection.adaptCoordinates(koordinaten);
					
					String name  = view.getViewName()+"_selection";
					DoubleMatrix coordMat1= new DoubleMatrix(neueKoordinaten);
					DoubleMatrix coordMat2= DoubleMatrix.concatHorizontally(coordMat1.getColumn(1),coordMat1.getColumn(0));
										
					View coords = new DoubleMatrixView(coordMat2, name);
					coords.setFeatureRoles(new FeatureRole[]{FeatureRole.X,FeatureRole.Y});
					coords.setFeatureDescriptors(new String[]{FeatureRole.X.name(),FeatureRole.Y.name()});
					coords.setViewName(name);
					
					int[] indizes = new int[newView.getNumberOfColumns()-2];
					int k = 0;
					for(int i=0;i<newView.getNumberOfColumns();i++){
						if(i!=FeatureSelection.getXCoordinate(newView) && i!=FeatureSelection.getYCoordinate(newView)){
							indizes[k] = i;
							k++;
						}
					}
					FeatureFilter ff = new FeatureFilter(newView,indizes);
					ff.setViewName(name);
					View n = new MergeFeaturesView(coords, ff);
					n.setViewName(name);
					View m = n.saveMaterialized(new File(master.getDirectoryProp().getProperty("DATA_PATH"),name+"_"+shp.toString()+"_merge.json"), new File(master.getDirectoryProp().getProperty("DATA_PATH"),name+"_"+shp.toString()+"_merge.mat"), new MatlabFormat());
					
					m.setViewName(name);
					m.materializeXYDimension();
					
					m.setFeatureRoles(view.getFeatureRoles());
					m.setFeatureDescriptors(view.getFeatureDescriptors());


					getMaster().getProject().add(m);
					getMaster().getDataManagementPanel().actualiseListen();
					clearSelections();

				} catch (Exception e) {

					clearSelections();
					e.printStackTrace();
				};
			}
		});
		
		right.add(getNewViewBtn());
//		right.add(new JButton("..."));

		addTrainDataBtn = new JButton("add to Train Data");
		addTrainDataBtn.setEnabled(false);
		addTrainDataBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(getMaster().getProject().getActiveView()!=null && getMaster().getProject().getTrainView()!=null){
					if(!getMaster().getProject().getTrainView().getLabelMap().isEmpty()){
						try{
							View view = getMaster().getProject().getActiveView();
							Shape shp = getMaster().getPaintPanel().getSelShape();

							// Select all areas inside all selected shapes
							int[] indInt = ImageSelection.selectInside(view, shp);

							Object[] comboBoxClasses = new Object[getMaster().getProject().getTrainView().getLabelMap().size()];
							int k = 0;
							for (int key:getMaster().getProject().getTrainView().getLabelMap().keySet()){
								String ss = getMaster().getProject().getTrainView().getLabelMap().get(key);
								comboBoxClasses[k] = ss.substring(0, ss.length()-8).toString();
								k++;
							}


							Icon icon = UIManager.getIcon("OptionPane.questionIcon");
							String selectedClass = (String)JOptionPane.showInputDialog(
									getMaster(),
									"Choose the class to add the samples.",
									"Choose class",
									JOptionPane.PLAIN_MESSAGE,
									icon,
									comboBoxClasses,
									"Choose class");

							int selInd = -9999;
							for(int i = 0; i<comboBoxClasses.length; i++){
								if(comboBoxClasses[i] == selectedClass){
									selInd = i;
									break;
								}
							}

							int labelInteger = new ArrayList<Integer>(getMaster().getProject().getTrainView().getLabelMap().keySet()).get(selInd);

							View actView = getMaster().getProject().getActiveView();
							for(int i = 0; i<indInt.length; i++){
								double[] row = actView.materializeExample(indInt[i]);

								double[] xyCoordinate = {row[FeatureSelection.getXCoordinate(actView)],row[FeatureSelection.getYCoordinate(actView)]};
								int[] nonSpecialFeatures = FeatureSelection.getNonSpecialFeatures(actView);
								double[] rest = new double[nonSpecialFeatures.length];
								for(int j=0; j<nonSpecialFeatures.length;j++){
									rest[j] = row[nonSpecialFeatures[j]];
								}
								getMaster().getProject().getTrainView().addRow(rest, labelInteger,xyCoordinate);
							}
							clearSelections();
						}catch(Exception e){
							clearSelections();
						}
					}
				}
			}
		});
		
		right.add(getAddTrainDataBtn());

		
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clearSelections();
			}
		});
		
		DataManagementPanel.addComponent(this, gbl, left , 0, 2, 1, 1, 1, 0);
		DataManagementPanel.addComponent(this, gbl, right, 1, 2, 1, 1, 3, 0);
		DataManagementPanel.addComponent(this, gbl, new JLabel(""), 0, 3, 2, 1, 1, 1);
		DataManagementPanel.addComponent(this, gbl, reset, 0, 4, 1, 1, 0, 0);
		DataManagementPanel.addComponent(this, gbl, new JLabel(""), 1, 4, 1, 1, 1, 0);
		DataManagementPanel.addComponent(this, gbl, new JLabel(""), 0, 5, 2, 1, 1, 5);
		
	}

	
	public JButton getRect() {
		return rect;
	}







	/**
	 * @return the poly
	 */
	public JButton getPoly() {
		return poly;
	}



	/**
	 * @return the lasso
	 */
	public JButton getLasso() {
		return lasso;
	}




	/**
	 * Removes pixels described by selShape in PaintPanel
	 * 
	 * @return the removeBtn
	 */
	public JButton getRemoveBtn() {
		return removeBtn;
	}



	/**
	 * This method defines the ActionLstener of the createNewView-Button.
	 * All pixels which lie inside the selected Shape will be part of the new view
	 * If a part of an image is selected the coordinates need to be adapted so that the top-left pixel has coordnates (1, 1).
	 *  
	 * @return the newViewBtn
	 */
	public JButton getNewViewBtn() {
		return newViewBtn;
	}

	
	public JButton getAddTrainDataBtn() {
		return addTrainDataBtn;
	}



	/**
	 * Method to set EditMode and SelectionMode.
	 * Set Buttons enabled/disabled.
	 * 
	 * @param jesOrNo  boolean whether edit mode will be activated or not
	 * @param mode The mode of Selecting (i.e. Rectangle, Polygon, Lasso)
	 */
	public void setEditMode(boolean jesOrNo, Mode mode){
		editMode = jesOrNo;
		
		removeBtn.setEnabled(jesOrNo);
		newViewBtn.setEnabled(jesOrNo);
		addTrainDataBtn.setEnabled(jesOrNo);
		
		getRect().setEnabled(!jesOrNo);
		getPoly().setEnabled(!jesOrNo);
		getLasso().setEnabled(!jesOrNo);
		

		
		this.selectionMode = mode;
	}
	


	
	/**
	 * @return the selectionMode
	 */
	public Mode getSelectionMode() {
		return selectionMode;
	}

	/**
	 * This method disables the edit mode and deletes all selected shapes.
	 */
	public void clearSelections(){

		setEditMode(false, Mode.NONE);
		getMaster().getPaintPanel().setSelShape(null);
		getRemoveBtn().setEnabled(false);
		getNewViewBtn().setEnabled(false);
		getMaster().getPaintPanel().repaint();
		getMaster().getPaintPanel().parent.repaint();
	}

//	/**
//	 * This method resets the selection.
//	 */
//	public void reset(){
//
//		getMaster().getPaintPanel().setSelShape(null);
//		getRemoveBtn().setEnabled(false);
//		getNewViewBtn().setEnabled(false);
//		getRect().setEnabled(true);
//		getPoly().setEnabled(true);
//		getLasso().setEnabled(true);
//		getMaster().getPaintPanel().repaint();
//		getMaster().getPaintPanel().parent.repaint();
//	}

	private ImageProcessingPanel getThis() {
		return this;
	}

	private MainGui getMaster() {
		return master;
	}
}
