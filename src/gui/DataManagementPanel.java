package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTMLEditorKit;

import models.FeatureSelectionModel;
import models.FeatureWeightsModel;
import models.KMeansModel;
import models.ModelChain;
import models.OrdinalSVMModel;
import models.PolynomialApproximationModel;
import models.ModelInterface;
import models.NormalisationModel;
import models.SVMModel;
import models.TransformationModel;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;

import data.AbstractViewImplementation;
import data.FeatureRole;
import data.FeatureSelection;
import data.Project;
import data.Utilities;
import data.View;
import data.inmemory.DoubleMatrixView;
import data.operators.AddLabelsView;
import data.operators.CloneView;
import data.operators.ExampleFilter;
import data.operators.FeatureFilter;
import data.operators.MergeExamplesView;
import data.operators.MergeFeaturesView;
import data.operators.NormView;
import data.operators.TrainView;

/**
 * 
 * @author Till
 *
 * This is the GUI-class for the Datamanagement-tab of the tabbedpane
 */
public class DataManagementPanel extends JPanel{

	private static final long serialVersionUID = 42L;

	private MainGui master;

	// attributes
	private JTextField chainNewName;
	private JList<Vector<JLabel>> datenListe;
	private JList<Vector<JLabel>> trainViewList;
	private JList<TransformationModel> modelList;
	private JList<Vector<Interval>> intervalList;
//	private JList<JRadioButton> radioButtonList;
	private JList<JRadioButton> radioButtonList2;
	private JList<JCheckBox> checkBoxList2;
	private ArrayList<Interval> intervals;
	private JTextPane metaTextPane;
	private JTabbedPane metaTabbedPane;
	private Map<Integer, String> labels;
	private JList<TransformationModel> modelListChain;
	private DefaultListModel<TransformationModel> modelListChainModel;
	private ArrayList<TransformationModel> chainListe;

	private JTextField txt1;
	private JTextField txt2;
	private JTextField txt3;
	private JTextField txt4;
//	private JTextField txtDescription;
	private JTextField nameOfNewView;
	private JTextField nameOfNewView2;
	private JTextField nameOfNewView3;
	private JTextField nameOfNewView7;
	private JTextField renameView;
	private JTextField from;
	private JTextField to;
	private JComboBox<JLabel> classBox;
	private JTextField status;
	private JRadioButton neu;
	private JButton addClass;
	private JRadioButton overwrite;
	private JButton preview;
	private JButton generateThumbnail;
	private JButton rename;
	private JButton setIcon;
	private JButton labelingBtn;
	private JButton createTrainViewBtn;
	private JButton joinBtn;
	private JButton delete;
	private JButton changeFeatureRoles;
	private JButton createModelChain;
	
	private JDialog dialogEditDescription;
	private JButton saveDescription;
	private JButton cancleDescription;
	private JTextArea newDescription;
	private View view;

	private JCheckBox saveModel;

	// Radio buttons for the "Normalisation"-dialog, to decide which type of normalization is used
	private ButtonGroup normTypeGroup;
	private JRadioButton[] normType;
	private JPanel normRadioButtons;

	// TextField to insert numerator of the normalisation
	private JTextField normNumerator;

	private JDialog splitFrame;
	private JDialog joinFrame;
	private JDialog labeling;
	private JDialog normalisation;
	private JDialog featureSelectionFrame;
	private JDialog addFeatures;
	private JDialog createModelChainDialog;

	private JTextField topXValue;
	private JTextField thresholdValue;
	private JRadioButton topX;
	private JRadioButton threshold;

	private JTextArea statusbar2;
	private JTextArea statusbar3;
	private JTextArea statusbar4;

	//stores which train views are selected. Lists are needed in case of multiple selections
	private ArrayList<Integer> orderOfSelectedViews = new ArrayList<Integer>();
	private ArrayList<Integer> orderOfSelectedTV = new ArrayList<Integer>();
	//differentiates if view is selected or train view
	private boolean isselectedViewTrainView;
	
	private boolean applicableMergeF = true;
	private boolean applicableMergeS = true;

	private Vector<JLabel> displayViews;
	private Vector<JLabel> displayViews2;

	private int[] labeled;

	private JTextPane txtPane;
	private JButton selModelBtn;
	private boolean detailed = false;


	private JButton copy;
	private JButton editDescription;
//	private JButton addDescription;

	private JButton saveModelBtn;
	private JButton loadModel;
	private JButton deleteModelBtn;
	private JButton applyModel;
	private JButton normBtn;
	private JButton splitBtn;
	private JButton validateLabel;
	
//	private ButtonGroup bgRadio;
//	private Vector<JRadioButton> radioButtonListViews;
	private Vector<JRadioButton> radioButtonListTrainViews;
	
	// Everything for the adding of VIs
	private JDialog vi;
	private JTextField nameOfNewView8;
	private JButton viBtn; // Button to open the VI dialog	
	private JCheckBox[] viSelections; // Checkboxes to choose which VI's should be saved
	private JPanel viSelection; // Container to group the checkboxes
	private JPanel viPreSelection;
	private JScrollPane viSelectionScroll;
	private JRadioButton[] viPreSelections;
	private ButtonGroup viButtonGroup;
	private JDialog vis;
	private JPanel visPanel;
	private JButton viConfirm;
	private JCheckBox viAppend;
	private JPanel sigmaPanel;
	private JSpinner sigmaInput;
	private JLabel sigmaUnit;

	
	private JPanel datenListenPanel;
	
	
	/**
	 * 
	 * @param master
	 */
	@SuppressWarnings({ })
	public DataManagementPanel (final MainGui master) {
		super();
		this.master=master;

//		this.setLayout(new GridLayout(0,2,10,0));
		setLayout(new MigLayout("fill, hmax 100%", "","[grow,fill]"));

		//ViewPanel: Panel on the left including 
		JPanel viewPanel = new JPanel();
		viewPanel.setBorder(BorderFactory.createTitledBorder("Project / Views"));
		viewPanel.setLayout(new MigLayout("", "[][grow,fill]", ""));
		
		JPanel datenPanel = new JPanel();
		GridBagLayout gblCHB = new GridBagLayout();
		datenPanel.setLayout(gblCHB);
		this.datenListenPanel = new JPanel();
		this.datenListenPanel.setBackground( Color.WHITE);
		addComponent(datenPanel, gblCHB, this.datenListenPanel , 0, 0, 1, 1, 0, 1);
		addComponent(datenPanel, gblCHB, this.getDatenListe(), 1, 0, 1, 1, 1, 1);
		
		JScrollPane scrollPaneD = new JScrollPane(datenPanel);
		scrollPaneD.setViewportView(datenPanel);
		scrollPaneD.setMinimumSize(new Dimension(100, 100));
		
		
		JPanel tvListenPanel = new JPanel();
		GridBagLayout gblTVB = new GridBagLayout();
		tvListenPanel.setLayout(gblTVB);
		addComponent(tvListenPanel, gblTVB, getRadioButtonList2(), 0, 0, 1, 1, 0, 1);
		addComponent(tvListenPanel, gblTVB, getTrainViewListe()	, 1, 0, 1, 1, 1, 1);

		JScrollPane scrollPaneTV = new JScrollPane(tvListenPanel);
		scrollPaneTV.setViewportView(tvListenPanel);
		scrollPaneD.setMinimumSize(new Dimension(100, 100));

		JSplitPane splitPane = new JSplitPane(0, scrollPaneD, scrollPaneTV);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerSize(1);
//		splitPane.setDividerLocation(200);

		//master
		master.setScrollPaneProjekt(splitPane);
				
		copy = new JButton("Copy");
		getCopy().setToolTipText("Copy the view.");
		viewPanel.add(copy, "cell 0 0,width 111, gapy 0,alignx center");
		delete = new JButton("Delete");
		getDelete().setToolTipText("Delete the view.");
		viewPanel.add(delete, "cell 0 1, width 111,alignx center");
		preview = new JButton("Preview");
		getPreview().setToolTipText("Preview the matrix.");
		viewPanel.add(preview, "cell 0 2, width 111,alignx center");
		
		setIcon = new JButton("Load Icon");
		getSetIcon().setToolTipText("Set an icon for the view. Scaled to 40x40 px.");
		viewPanel.add(setIcon, "cell 0 3, width 111, gapy 10,alignx center");
		generateThumbnail = new JButton("<html>Generate<br>Thumbnail");
		getGenerateThumbnail().setToolTipText("Generates Thumbnail from data");
		viewPanel.add(generateThumbnail, "cell 0 4, width 111,alignx center");
		
		renameView = new JTextField();
		viewPanel.add(renameView, "cell 0 5, width 111, gapy 10,alignx center");
		rename = new JButton("Rename");
		getRename().setToolTipText("Rename the view");
		viewPanel.add(rename, "cell 0 6, width 111,alignx center");
		
		validateLabel = getValidateLabel();
		validateLabel.setToolTipText("Validate the label of this view by an annotated image");
		viewPanel.add(validateLabel, "cell 0 7, width 111,alignx center");
		
		splitBtn = new JButton("Split View");
		joinBtn = new JButton("Merge Views");
		labelingBtn = new JButton("Labeling");
		createTrainViewBtn = new JButton("set as TrainView");
		normBtn = new JButton("Normalisation");

		JPanel prepro = new JPanel();
		prepro.setBorder(BorderFactory.createTitledBorder("Preprocessing"));
		prepro.setLayout(new GridLayout(6,0));
		prepro.add(getSplitBtn());
		prepro.add(getJoinBtn());
		prepro.add(getLabelingBtn());
		prepro.add(getCreateTrainViewBtn());
		prepro.add(getNormBtn());
		
		viBtn = new JButton("Set VI's");
		prepro.add(getViBtn());
		viewPanel.add(prepro, "cell 0 8, width 80, gapy 10, alignx center");

		changeFeatureRoles = new JButton("Data Editor");
		viewPanel.add(getChangeFeatureRoles(), "cell 0 9, width 111, gapy 10,alignx center");
		viewPanel.add(new JLabel("<html><center>Use Ctrl for<br>multiple selection.</center></html>"), "cell 0 9, width 111, gapy 10, alignx center");
		viewPanel.add(new JLabel("<html><center>Tick the checkbox to<br>use view in other tabs.</center></html>"), "cell 0 10, width 111,alignx center");
		
		add(viewPanel,"");
/**		
//		GridBagLayout gblviewPanel = new GridBagLayout();
//		viewPanel.setLayout( gblviewPanel );
//		addComponent( viewPanel, gblviewPanel, new JLabel("Use Ctrl for multiple selection.")     , 0, 17, 2, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, new JLabel("Tick the checkbox to use view in other tabs.")   , 0, 18, 2, 1, 0, 0 );

		//Herausgenommen wegen Umbau
//		addComponent( viewPanel, gblviewPanel, splitPane     , 0, 0, 1, 17, 1, 1 );

//		addComponent( viewPanel, gblviewPanel, getCopy()	   , 0, 2, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, delete          , 0, 3, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, preview         , 0, 4, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, new JLabel(" ") , 0, 5, 1, 1, 0, 0 );
//
//
//		addComponent( viewPanel, gblviewPanel, setIcon         , 0, 6, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, generateThumbnail, 0, 7, 1, 1, 0, 0 );
//
//		addComponent( viewPanel, gblviewPanel, new JLabel(" ") , 0, 8, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, renameView      , 0, 9, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, rename          , 0, 10, 1, 1, 0, 0 );
//
//		addComponent( viewPanel, gblviewPanel, new JLabel(" ") 	, 0, 11, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, new JLabel(" ") 	, 0, 12, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, prepro 			, 0, 13, 1, 1, 0, 0 );
//
//		addComponent( viewPanel, gblviewPanel, new JLabel(" ") 	, 0, 14, 1, 1, 0, 0 );
//		addComponent( viewPanel, gblviewPanel, getChangeFeatureRoles(), 0, 15, 1, 1, 0, 0 );
 * 
 */

		JPanel rechtsPanel = new JPanel();
		GridBagLayout gblRechtsPanel = new GridBagLayout();
		rechtsPanel.setLayout(gblRechtsPanel);

		//Metadatenpanel
		JPanel metadatenPanel = new JPanel();
		metadatenPanel.setBorder(BorderFactory.createTitledBorder("Metadata"));

		metaTextPane = new JTextPane();
		metaTextPane.setEditorKit(new MyHTMLEditorKit());
		metaTextPane.setEditable(false);
		metaTextPane.setMinimumSize(new Dimension(100,10));
		metaTextPane.setOpaque(false);

		JScrollPane scrollPaneMeta = new JScrollPane(metaTextPane);
		scrollPaneMeta.setViewportView(metaTextPane);
		scrollPaneMeta.setBorder(BorderFactory.createEmptyBorder());

		editDescription = new JButton("Edit Description");
		GridBagLayout gblmetadatenPanel = new GridBagLayout();
		metadatenPanel.setLayout( gblmetadatenPanel );
		addComponent( metadatenPanel, gblmetadatenPanel, scrollPaneMeta  , 0, 0, 3, 1, 1, 1 );
		addComponent( metadatenPanel, gblmetadatenPanel, new JLabel(" ") , 0, 1, 1, 1, 1, 0 );
		addComponent( metadatenPanel, gblmetadatenPanel, getEditDescription()  , 2, 2, 1, 1, 0, 0 );

		//Metadaten Models
		txtPane=new JTextPane();
		txtPane.setEditable(false);
		txtPane.setMinimumSize(new Dimension(100,10));
		txtPane.setOpaque(false);

		MyHTMLEditorKit kit=new MyHTMLEditorKit();

		txtPane.setOpaque(false);
		txtPane.setEditorKit(kit);
		txtPane.addHyperlinkListener(new HTMLListener());
		txtPane.setEditable(false);

		JScrollPane scrollPaneMetaModels = new JScrollPane(txtPane);
		scrollPaneMetaModels.setViewportView(txtPane);
		scrollPaneMetaModels.setBorder(BorderFactory.createEmptyBorder());

		txtPane.setBorder(BorderFactory.createTitledBorder("Metadata"));


		//Modeling
		JPanel applyModels = new JPanel();

		modelList = new JList<TransformationModel>();

		JScrollPane sp2 = new JScrollPane(getModelListe());

		applyModel = new JButton("Apply");
		nameOfNewView3 =  new JTextField();
		nameOfNewView3.setBorder(BorderFactory.createTitledBorder("Name of new view/Suffix"));

		GridBagLayout gblApplyModels = new GridBagLayout();
		applyModels.setLayout(gblApplyModels);

		loadModel = new JButton("Load Model");
		saveModelBtn= new JButton("Save Model");
		deleteModelBtn = new JButton("Delete");
		createModelChain = new JButton("Create model-chain");

		addComponent(applyModels, gblApplyModels, sp2    		  		, 0, 0, 4, 1, 1, 1 );
		//addComponent(applyModels, gblApplyModels, new JLabel(" ") 		, 0, 1, 1, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, getCreateModelChain()	, 0, 1, 1, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, getDeleteModelBtn()	, 1, 1, 1, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, initSaveModelBtn()	  	, 2, 1, 1, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, getLoadModel()		, 3, 1, 1, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, new JLabel(" ") 		, 0, 2, 4, 1, 0, 0 );
		addComponent(applyModels, gblApplyModels, nameOfNewView3  		, 0, 3, 3, 1, 1, 0 );
		addComponent(applyModels, gblApplyModels, getApplyModel()      , 3, 3, 1, 1, 0, 0 );


		applyModels.setBorder(BorderFactory.createTitledBorder("Apply models/Transform"));

		metaTabbedPane = new JTabbedPane();
		metaTabbedPane.add("Views", metadatenPanel);
		metaTabbedPane.add("Models", scrollPaneMetaModels);

		addComponent(rechtsPanel, gblRechtsPanel, metaTabbedPane  , 1, 0, 1, 1, 1, 1 );
		addComponent(rechtsPanel, gblRechtsPanel, new JLabel(" ") , 1, 1, 1, 1, 1, 0 );
		addComponent(rechtsPanel, gblRechtsPanel, applyModels      , 1, 2, 1, 1, 1, 1 );

		JTextArea statusbar1 = new JTextArea();
		statusbar1.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusbar1.setText("Current Project:     ");
		statusbar1.setOpaque(false);
		statusbar1.setEditable(false);
		//		statusbar1.setS

		statusbar2 = new JTextArea();
		statusbar2.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusbar2.setOpaque(false);
		statusbar2.setEditable(false);
		statusbar2.setToolTipText("Creation date");

		statusbar3 = new JTextArea();
		statusbar3.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusbar3.setOpaque(false);
		statusbar3.setEditable(false);
		statusbar3.setToolTipText("Alteration date");

		statusbar4 = new JTextArea();
		statusbar4.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusbar4.setOpaque(false);
		statusbar4.setEditable(true);
		statusbar4.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				master.getProject().setDescription(statusbar4.getText());
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				master.getProject().setDescription(statusbar4.getText());
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				master.getProject().setDescription(statusbar4.getText());
			}
		});

//		GridBagLayout gblMain = new GridBagLayout();
//		this.setLayout(gblMain);
//
//		addComponent(this, gblMain, viewPanel	, 0, 0, 4, 1, 1, 1);
//		addComponent(this, gblMain, rechtsPanel	, 4, 0, 1, 1, 1, 1);
//		addComponent(this, gblMain, statusbar1	, 0, 1, 1, 1, 0, 0);
//		addComponent(this, gblMain, statusbar2	, 1, 1, 1, 1, 0, 0);
//		addComponent(this, gblMain, statusbar3	, 2, 1, 1, 1, 0, 0);
//		addComponent(this, gblMain, statusbar4	, 3, 1, 2, 1, 1, 0);
		
		add(rechtsPanel,"width 75%, grow,shrink");

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new MigLayout("", "[][][][75%]", ""));
		statusPanel.add(statusbar1);
		statusPanel.add(statusbar2);
		statusPanel.add(statusbar3);
		statusPanel.add(statusbar4,"grow");
		
		add(statusPanel,"south,span");
		
		//Datenlesen
		
		
		//Wenn eine Project-Datei vorhanden
		if(!master.getDirectoryProp().get("LAST_PROJECT").equals("")){

			// If the saved project file does not exists
			if(!new File(master.getDirectoryProp().getProperty("LAST_PROJECT")).exists()){

				//Ask what to do 
				Object[] options = {"Load a different project file",
				"Start with a new project"};
				JFrame frame = new JFrame();
				int n = JOptionPane.showOptionDialog(frame,
						"The last project ("+master.getDirectoryProp().getProperty("LAST_PROJECT")+") cannot be loaded. How to proceed?",
						"Project file does not exist",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						null,     //do not use a custom Icon
						options,  //the titles of buttons
						options[0]); //default button title

				

				// Reset path
				master.getDirectoryProp().setProperty("LAST_PROJECT", "");
				
				//If load other project file
				if (n == 0){
					JFileChooser chooser = new JFileChooser();

					if(master.getDirectoryProp().getProperty("LAST_PROJECT")!=null){
						chooser.setCurrentDirectory(new File(master.getDirectoryProp().getProperty("LAST_PROJECT")));
					}
					int state = chooser.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION ){
						File file = chooser.getSelectedFile();
						master.getDirectoryProp().setProperty("LAST_PROJECT", file.getAbsolutePath());
					}

				}

			}

			if(new File(master.getDirectoryProp().getProperty("LAST_PROJECT")).exists()){
				Project neues =null;
				try {
					neues = new Project(new File(master.getDirectoryProp().getProperty("LAST_PROJECT")));
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (neues != null){
					master.setProject(neues);				
					for(View v: master.getProject().getViewList()){
						v.setSaved(true);
					}
				}
			}
		}
		
		

		

		actualiseListen();


	}


	/**
	 * 
	 * @return Button to Save a model
	 */
	private JButton initSaveModelBtn() {

		saveModelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (getModelListe().getSelectedIndex() !=-1){

					TransformationModel model = (TransformationModel) getModels().get(getModelListe().getSelectedIndex());
					try {
						String firstModelPath = master.getDirectoryProp().getProperty("MODEL_PATH");
						String modelPath;
						if(firstModelPath == null){
							modelPath = model.save("");
						}else{
							modelPath = model.save(firstModelPath);
						}
						master.getDirectoryProp().setProperty("MODEL_PATH",modelPath);

					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		return saveModelBtn;
	}


	public JButton getLoadModel() {


		loadModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					JFileChooser chooser = new JFileChooser();

					if(master.getDirectoryProp().getProperty("MODEL_PATH")!=null){
						chooser.setCurrentDirectory(new File(master.getDirectoryProp().getProperty("MODEL_PATH")));
					}

					chooser.setFileView(new FileView() {

						@Override
						public String getName(File f) {
							String name = f.getName();
							if(name != null){
								if (name.endsWith(".model")){
									name = name.substring(3, name.length());
								}
							}
							return name;


						}
					});

					chooser.setAccessory(new MetaPreview(chooser,true));

					chooser.setFileFilter(new	FileFilter(){

						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.getName().endsWith(".model");
						}

						@Override
						public String getDescription() {
							return "transformation model (.model)";
						}

					});


					switch (chooser.showOpenDialog(master)) {
					case JFileChooser.APPROVE_OPTION:

						TransformationModel tr = TransformationModel.castModel(chooser.getSelectedFile());
						master.getDirectoryProp().setProperty("MODEL_PATH", chooser.getSelectedFile().getParent());

						try{
							if(!chooser.getSelectedFile().getName().substring(3, chooser.getSelectedFile().getName().length()-6).equals("")){

								tr.setName(chooser.getSelectedFile().getName().substring(3, chooser.getSelectedFile().getName().length()-6));
							}
						}catch(Exception el){

						}
						getModels().add(tr);
						actualiseListen();

						break;
					default:
						break;
					}


				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		return loadModel;
	}


	public JButton getCreateModelChain() {

		createModelChain.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(!(getModels().isEmpty())){

					boolean b = false;
					for (TransformationModel tra: getModels()) {
						if(tra.getClass()!=ModelChain.class){
							b = true;
						}
					}
					if(b){
						createModelChainDialog = new JDialog(master, false);
						createModelChainDialog.setTitle("Model Chain");
						createModelChainDialog.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().width *0.3), 255);
						createModelChainDialog.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-createModelChainDialog.getSize().width) / 2,
								(Toolkit.getDefaultToolkit().getScreenSize().height-createModelChainDialog.getSize().height) / 2		);

						GridBagLayout gblCreateModelChainDialog = new GridBagLayout();
						createModelChainDialog.setLayout( gblCreateModelChainDialog );

						chainListe = new ArrayList<TransformationModel>();

						modelListChainModel = new DefaultListModel<TransformationModel>();
						for (TransformationModel tra: getModels()) {

							modelListChainModel.addElement(tra);
							chainListe.add(tra);

						}
						modelListChain = new JList<TransformationModel>(modelListChainModel);

						MyMouseAdaptor myMouseAdaptor = new MyMouseAdaptor();
						modelListChain.addMouseListener(myMouseAdaptor);
						modelListChain.addMouseMotionListener(myMouseAdaptor);

						JButton delChain = new JButton("delete");
						delChain.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {

								chainListe.remove(modelListChain.getModel().getElementAt(modelListChain.getSelectedIndex()));
								((DefaultListModel<TransformationModel>) modelListChain.getModel()).removeAllElements();
								for (TransformationModel tra: chainListe) {
									modelListChainModel.addElement(tra);
								}
							}
						});

						chainNewName = new JTextField();

						JButton enterChain = new JButton("enter");
						enterChain.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent arg0) {

								ArrayList<ModelInterface> list = new ArrayList<ModelInterface>();
								for(int i=0;i< modelListChain.getModel().getSize();i++){
									list.add((ModelInterface) modelListChain.getModel().getElementAt(i));
								}
								if(!list.isEmpty()){

									ModelChain mc = new ModelChain(chainNewName.getText(), "*", new Date(), list);
									getModels().add(mc);
									actualiseListen();
									createModelChainDialog.dispose();
								}
							}
						});
						modelListChain.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

						addComponent(createModelChainDialog, gblCreateModelChainDialog, new JLabel("Choose order of models by dragging:")	, 0, 0, 2, 1, 0, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, new JLabel(" ")										, 0, 1, 2, 1, 0, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, modelListChain										, 0, 2, 2, 1, 1, 1);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, new JLabel(" ")										, 0, 3, 2, 1, 0, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, new JLabel("Name of new model:")					, 0, 4, 2, 1, 1, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, chainNewName										, 0, 5, 2, 1, 1, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, delChain											, 0, 6, 1, 1, 1, 0);
						addComponent(createModelChainDialog, gblCreateModelChainDialog, enterChain											, 1, 6, 1, 1, 1, 0);

						createModelChainDialog.setVisible(true);
					}
				}
			}
		});

		return createModelChain;
	}


	public JButton getDeleteModelBtn() {

		deleteModelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if (getModelListe().getSelectedIndex() !=-1){

					getModels().remove(getModelListe().getSelectedIndex());
					actualiseListen();

				}
			}
		});

		return deleteModelBtn;
	}






	public JButton getApplyModel() {

		applyModel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				int[] selIndices = getDatenListe().getSelectedIndices();
				ArrayList<View> views = getSelectedViews();
				if (views.size()>0){
					if (getModelListe().getSelectedIndices().length==1){
						if (!(nameOfNewView3.getText().equals("") )){

							TransformationModel model = (TransformationModel) getModels().get(getModelListe().getSelectedIndex());
							
							try{
								for(int i=0; i<views.size();i++){

									View view = views.get(i);
//									AbstractViewImplementation view = (AbstractViewImplementation) getViews().get(selIndices[i]);
									View neuerView = null;

									if(model.getClass()==NormalisationModel.class){
										try{
											neuerView = ((NormalisationModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));
										}catch(IOException  vdg){

										}

									}else if(model.getClass()==FeatureSelectionModel.class){

										try {
											neuerView = ((FeatureSelectionModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));
										} catch(Error e2){
											JOptionPane.showMessageDialog(master, "Wavelengths don't match", "Ignore?", JOptionPane.QUESTION_MESSAGE);

										} catch (IOException e1) {
											e1.printStackTrace();
										}

									}else if(model.getClass()==FeatureWeightsModel.class){

										try{

											neuerView = ((FeatureWeightsModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));

										}catch(IllegalArgumentException vdg){
											vdg.printStackTrace();
											System.err.println("IllegalArgumentException");

										} catch (IOException e1) {
											e1.printStackTrace();
										} catch(Error e2){
											JOptionPane.showMessageDialog(master, "Wavelengths don't match", "Ignore?", JOptionPane.QUESTION_MESSAGE);
										}

									}else if(model.getClass()==SVMModel.class){

										//										neuerView = ((SVMModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));
//										neuerView = ((SVMModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));
										SVMModel model_spec= (SVMModel) model;
										String path = master.getDirectoryProp().getProperty("DATA_PATH");
										System.out.println(view);
										System.out.println(path);
										System.out.println(model);
										neuerView= model_spec.applyOn(view, path);
										
										
										master.getAlPanel().resetData(neuerView);										
										master.getAlPanel().getClusterButton().setEnabled(true);
//										master.setLastDisplayedView(neuerView);
										
									}else if(model.getClass()==OrdinalSVMModel.class){
										
										neuerView = ((OrdinalSVMModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view, master.getDirectoryProp().getProperty("DATA_PATH"));
										master.getAlPanel().resetData(neuerView);
										master.getAlPanel().getClusterButton().setEnabled(true);
//										master.setLastDisplayedView(neuerView);
										

									}else if(model.getClass()==KMeansModel.class){

										neuerView = ((KMeansModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view, master.getDirectoryProp().getProperty("DATA_PATH"));
										
										master.getAlPanel().resetData(neuerView);
										//master.getUnsupervisedLearning().getWekaControl().addCluster(((KMeansModel) getModels().get(getModelListe().getSelectedIndex())).getCentroids(), assignments, ((KMeansModel) getModels().get(getModelListe().getSelectedIndex())).getClusterName(),neuerView);
										master.getAlPanel().getClusterButton().setEnabled(true);
										master.getAlPanel().getClusterButton().setSelected(true);
//										master.setLastDisplayedView(neuerView);
									}else if(model.getClass()==PolynomialApproximationModel.class){

										neuerView = ((PolynomialApproximationModel) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));

									}else if(model.getClass()==ModelChain.class){

										try {

											neuerView = ((ModelChain) getModels().get(getModelListe().getSelectedIndex())).applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));

										} catch (IOException e1) {

										} catch (SizeException d){
											JOptionPane.showMessageDialog(master, "Sizes don't match","Error", JOptionPane.ERROR_MESSAGE);

										} catch(IllegalArgumentException il){
											JOptionPane.showMessageDialog(master, "Arguments don't match","Error", JOptionPane.ERROR_MESSAGE);
										}catch(Error e2){
											JOptionPane.showMessageDialog(master, "Wavelengths don't match", "Ignore?", JOptionPane.QUESTION_MESSAGE);
										}catch(Exception ead){
											JOptionPane.showMessageDialog(master, "Not applicable!","Error", JOptionPane.ERROR_MESSAGE);
										}


									}




									if(views.size()==1){
										neuerView.setViewName(nameOfNewView3.getText());
									}else{
										neuerView.setViewName(view.getViewName()+ "_" + nameOfNewView3.getText());
									}


//									getViews().add(neuerView);
									if(isselectedViewTrainView){
										master.getProject().getTViewList().add((TrainView)neuerView);
									}else{
										master.getProject().getViewList().add(neuerView);
									}
								}

							}catch (SizeException exc){
								JOptionPane.showMessageDialog(master,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
								//								exc.printStackTrace();
							}catch (IllegalArgumentException dt){
								JOptionPane.showMessageDialog(master,"Not applicable  "+dt.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							}catch(Error f){
								JOptionPane.showMessageDialog(master, "Wavelengths don't match", "Ignore?", JOptionPane.QUESTION_MESSAGE);
								//							}
								//							catch(Exception ser){
								//								
								//							}
							}catch (IOException e1) {
								JOptionPane.showMessageDialog(master,e1.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							}



							actualiseListen();
							nameOfNewView3.setText("");

						}else{
							JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.INFORMATION_MESSAGE);
						}
					}else{
						JOptionPane.showMessageDialog(master,"Please select model!","Error",JOptionPane.ERROR_MESSAGE);
					}
				}else{
					JOptionPane.showMessageDialog(master,"Please select data!","Error",JOptionPane.INFORMATION_MESSAGE);
				}

			}
		});

		return applyModel;
	}


	public JButton getNormBtn() {

		normBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				if(((!isselectedViewTrainView && orderOfSelectedViews.size() > 0) || (isselectedViewTrainView && orderOfSelectedTV.size() > 0)) && getSelectedView() != null){

					normalisation = new JDialog(master,true);
					//					normalisation.setDefaultLookAndFeelDecorated(true);
					normalisation.setTitle("Normalisation");
					normalisation.setSize(600, 130);
					normalisation.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-normalisation.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height-normalisation.getSize().height) / 2 +50		);
					GridBagLayout gblNorm = new GridBagLayout();
					normalisation.setLayout(gblNorm);

					nameOfNewView7 =  new JTextField();
					nameOfNewView7.setBorder(BorderFactory.createTitledBorder("Name of new view"));
					JButton normBt = new JButton("Norm Data");
					normBt.setToolTipText("Nomalise Data with mean 0 and standard deviation 1");

					saveModel = new JCheckBox("Save model");
					saveModel.setSelected(true);

					// Radio buttons for the "Normalisation"-dialog, to decide which type of normalization is used
					normTypeGroup = new ButtonGroup();
					normType = new JRadioButton[3];
					normRadioButtons = new JPanel();
					normNumerator = new JTextField();

					normRadioButtons.setBorder(BorderFactory.createTitledBorder("Type of normalisation"));
					GridBagLayout gblNormButtons = new GridBagLayout();	
					normRadioButtons.setLayout(gblNormButtons);

					normType[0] = new JRadioButton("Mean [0] - Standarddeviation [1]"); // Mean value: 0, standarddeviation 1
					normType[1] = new JRadioButton("Min - Max [0,1]"); // Min: 0, Max: 1
					normType[2] = new JRadioButton("Divide by"); // Division of all features through a given number

					for ( int i = 0; i < normType.length; i++ ) {
						normTypeGroup.add(normType[i]); // Radio buttons are added to the ButtonGroup, so only one of them can be selected at a given time
					}

					// TextField to insert numerator will be clear
					normType[0].addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							normNumerator.setText("");
						}
					});

					// TextField to insert numerator will be clear
					normType[1].addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							normNumerator.setText("");
						}
					});

					// If TextField is clicked on, last option of normalisation is chosen
					normNumerator.addKeyListener(new KeyListener() {
						@Override
						public void keyTyped(KeyEvent arg0) {
							normType[2].setSelected(true);
						}

						@Override
						public void keyReleased(KeyEvent arg0) {
							normType[2].setSelected(true);
						}

						@Override
						public void keyPressed(KeyEvent arg0) {
							normType[2].setSelected(true);
						}
					});

					addComponent(normRadioButtons, gblNormButtons, normType[0]  , 0, 0, 2, 1, 0, 0);
					addComponent(normRadioButtons, gblNormButtons, normType[1]  , 0, 1, 2, 1, 0, 0);
					addComponent(normRadioButtons, gblNormButtons, normType[2]  , 0, 2, 1, 1, 0, 0);
					addComponent(normRadioButtons, gblNormButtons, normNumerator, 1, 2, 1, 1, 0, 0);

					normBt.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							try{

								View view = getSelectedView();
//								AbstractViewImplementation view = (AbstractViewImplementation) master.getProject().get(getDatenListe().getSelectedIndex());
								DoubleMatrix subtrahend = new DoubleMatrix();
								DoubleMatrix numerator = new DoubleMatrix();
								NormalisationModel model;


								if(!nameOfNewView7.getText().equals("")){
									// Data is materialised
									DoubleMatrix spektren = Utilities.materializeAsDoubleMatrix(view).getColumns(FeatureSelection.getNonSpecialFeatures(view));
									DoubleMatrix spektrenNorm = new DoubleMatrix();

									if ( normType[0].isSelected() ) { // radio button for mean-standard deviation is selected
										try{
											// Mean - standard - normalisation

											// This section is the actual normalisation
											subtrahend = spektren.columnMeans();
											spektrenNorm = spektren.subRowVector(subtrahend);
											numerator = new DoubleMatrix(1,spektren.columns);
											for (int i = 0; i<spektren.columns; i++){
												numerator.put(0, i, new StandardDeviation().evaluate( spektrenNorm.getColumn(i).toArray()) );
											}
											spektrenNorm = spektrenNorm.divRowVector(numerator);
											
										}catch(SizeException sr){
											JOptionPane.showMessageDialog(master,sr.getMessage(),"Dimension Error",JOptionPane.ERROR_MESSAGE);
										}										
									}

									if ( normType[1].isSelected() ) { // The radio button for min-max is selected
										try{
											// Minimum - Maximum - Normalisation

											// This section is the actual normalisation
											subtrahend = spektren.columnMins() ;  // The minimums of each column are saved
											spektrenNorm = spektren.subRowVector( subtrahend);  // The minimum is subtracted from the original features
											numerator = spektren.columnMaxs(); // The maximums of each column are saved
											spektrenNorm = spektrenNorm.divRowVector(numerator); // The normed values are divided through the maximum
											
										}catch(SizeException sr){
											JOptionPane.showMessageDialog(master,sr.getMessage(),"Dimension Error",JOptionPane.ERROR_MESSAGE);
										}										
									}

									if ( normType[2].isSelected() ) { // the radio button for division through a value is selected
										if ( !normNumerator.getText().equals("") ) {
											try{
												// Division through a given value (num, see below)
												double num = Double.parseDouble(normNumerator.getText()); // This number derived from the textfield "normNumerator", is the number, each value is devided through

												// This section is the actual normalisation
												subtrahend = DoubleMatrix.zeros(spektren.columns).transpose() ; // Nothing needs to be subtracted, so an row vector with n - zeros is constructed (subtrahend still needed to apply model, see below)
												numerator = DoubleMatrix.ones(spektren.columns).transpose();    // The numerator needs to be a DoubleMatrix containing n times num (see above), so a row vector with n - ones is constructed ...
												numerator = numerator.mul(num);                               // ... and then multiplied with num
												spektrenNorm = spektren.divRowVector(numerator);                // The values are divided through num and then stored in spektrenNorm
												
											}catch(SizeException sr){
												JOptionPane.showMessageDialog(master,sr.getMessage(),"Dimension Error",JOptionPane.ERROR_MESSAGE);
											}catch(NumberFormatException nan) {
												JOptionPane.showMessageDialog(master,"Not a number", "NaN!",JOptionPane.ERROR_MESSAGE);
											}
										} else {
											JOptionPane.showMessageDialog(master,"No number inserted", "NaN!",JOptionPane.ERROR_MESSAGE);
										}
									}

									if ( normType[0].isSelected() ) {
										model = new NormalisationModel("NormModel_"+view.getViewName()+"_meanstd", view.getViewName(), new Date(), subtrahend,numerator);
									} else if ( normType[1].isSelected() ) {
										model = new NormalisationModel("NormModel_"+view.getViewName()+"_minmax", view.getViewName(), new Date(), subtrahend,numerator);
									} else {
										double num;
										try {
											num = Double.parseDouble(normNumerator.getText());
										} catch (Exception erro){
											num = 0;
										}
										model = new NormalisationModel("NormModel_"+view.getViewName()+"_divide_"+num, view.getViewName(), new Date(), subtrahend,numerator);
									}
									
									if(saveModel.isSelected()){
										getModels().add(model);
									}
									
									NormView neuerView;

									// Aplication of the generated model on the chosen data set
									try{
										neuerView = (NormView) model.applyOn(view,master.getDirectoryProp().getProperty("DATA_PATH"));
										neuerView.setViewName(nameOfNewView7.getText());
										if(selectedViewIsTV()){
											master.getProject().getTViewList().add(new TrainView(neuerView));
										}else{
											master.getProject().getViewList().add(neuerView);
										}
									}catch(IllegalArgumentException sr){
										System.err.println("IllegalArgumentException");
									}catch(Exception E) {
										E.printStackTrace();
									}
								}else{ // No name for the new view is inserted
									JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.INFORMATION_MESSAGE);
								}

								

								normalisation.dispose();
								actualiseListen();
								nameOfNewView7.setText("");


							}catch(Exception wge){
								wge.printStackTrace();
								//					JOptionPane.showMessageDialog(master,wge.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							}

						}
					});

					addComponent(normalisation, gblNorm, normRadioButtons   , 1, 1, 1, 4, 0, 0 );
					addComponent(normalisation, gblNorm, saveModel		 	, 2, 3, 1, 1, 0, 0 );
					addComponent(normalisation, gblNorm, nameOfNewView7  	, 1, 6, 1, 1, 1, 0 );
					addComponent(normalisation, gblNorm, normBt		 		, 2, 6, 1, 1, 0, 0 );

					Dimension dimNorm = new Dimension(400, 180);
					normalisation.setSize(dimNorm);
					normalisation.setPreferredSize(dimNorm);
					normalisation.setMaximumSize(dimNorm);
					normalisation.setMinimumSize(dimNorm);

					normalisation.setVisible(true);

				}else{ // No view is selected
					JOptionPane.showMessageDialog(master,"Please select View!","Error",JOptionPane.INFORMATION_MESSAGE);
				}

			}
		});


		return normBtn;
	}


	public JButton getSplitBtn() {

		splitBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				int selIndex = getDatenListe().getSelectedIndex();

				if (getSelectedView() != null){
					splitFrame = new JDialog(master,true);
					splitFrame.setTitle("Split View");
					splitFrame.setSize(300, 200);

					splitFrame.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-splitFrame.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height-splitFrame.getSize().height) / 2 +50		);

					JButton split = new JButton("Split and save");
					split.setToolTipText("Splits matrix.");
					nameOfNewView = new JTextField();
					JPanel splitpoint = new JPanel();
					GridBagLayout gblSplit = new GridBagLayout();
					splitFrame.setLayout( gblSplit );



					splitpoint.setLayout( new GridLayout(4, 1) );
					txt1 = new JTextField(3);
					txt1.setMaximumSize(new Dimension(1,3));
					txt1.setColumns(3);
					txt2 = new JTextField(3);
					txt2.setMaximumSize(new Dimension(1,3));
					txt2.setColumns(3);
					txt3 = new JTextField(3);
					txt3.setMaximumSize(new Dimension(1,3));
					txt3.setColumns(3);
					txt4 = new JTextField(3);
					txt4.setMaximumSize(new Dimension(1,3));
					txt4.setColumns(3);

					JPanel samples = new JPanel();
					samples.setLayout( new GridLayout(0, 4,20,20) );
					samples.add(new JLabel("from:"));
					samples.add(txt1);
					samples.add(new JLabel("to:"));
					samples.add(txt2);

					JPanel features = new JPanel();
					features.setLayout( new GridLayout(0, 4,20,20) );
					features.add(new JLabel("from:"));
					features.add(txt3);
					features.add(new JLabel("to:"));
					features.add(txt4);

					splitpoint.add(new JLabel("Samples:"));
					splitpoint.add(samples);
					splitpoint.add(new JLabel("Features:"));
					splitpoint.add(features);


					split.addActionListener(new ActionListener() {
						public void actionPerformed (ActionEvent e){

							try{
//								View selView = master.getProject().get(getDatenListe().getSelectedIndex());
								View selView = getSelectedView();

								try{
									int svon = Integer.parseInt(txt1.getText())-1;
									int sbis = Integer.parseInt(txt2.getText());
									int fvon = Integer.parseInt(txt3.getText())-1;
									int fbis = Integer.parseInt(txt4.getText());

									if (svon<0 || sbis<=svon || fbis<=fvon || sbis>selView.getNumberOfExamples() || fbis>selView.getNumberOfColumns() ){
										JOptionPane.showMessageDialog(master,"Range Error!","Error",JOptionPane.ERROR_MESSAGE);
									} else{

										if (!nameOfNewView.getText().equals("")){
											int[] array = new int[sbis-svon];
											for (int i=svon ; i<sbis; i++){
												array[i-svon]=i;
											}
											View splittedView = new ExampleFilter(selView,array);

											int[] array2 = new int[fbis-fvon];
											for (int i=fvon ; i<fbis; i++){
												array2[i-fvon]=i;
											}

											View splittedView2 = new FeatureFilter(splittedView,array2);

											splittedView2.setViewName(nameOfNewView.getText());

											String description = "Splitted view: Rows "+(svon+1)+" to "+sbis+" and Columns "+(fvon+1)+" to "+fbis+" from "+selView.getViewName();
											splittedView2.setViewDescription(description);
//											getViews().add(splittedView2);
											if(selectedViewIsTV()){
												master.getProject().getTViewList().add(new TrainView(splittedView2));
											}else{
												master.getProject().getViewList().add(splittedView2);
											}
											actualiseListen();

											emptySplitFields();

											splitFrame.dispose();

										}else{
											JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.ERROR_MESSAGE);
										}
									}
								} catch (NumberFormatException a){
									throw new Error();
								}

							}catch (Error e3){
								JOptionPane.showMessageDialog(master,"Please insert numbers!","Error",JOptionPane.ERROR_MESSAGE);
							}catch (NumberFormatException e1){
								JOptionPane.showMessageDialog(master,"File broken!","Error",JOptionPane.ERROR_MESSAGE);
							}catch (ArrayIndexOutOfBoundsException b){
								JOptionPane.showMessageDialog(master,"File incomplete!","Error",JOptionPane.ERROR_MESSAGE);
							}catch(NullPointerException rgs){
								JOptionPane.showMessageDialog(master,"Please select data!","Error",JOptionPane.ERROR_MESSAGE);
							}
						}
						//			}
					});  

					//					addComponent( splitFrame, gblSplit, new JLabel("Please set an active view:")				, 0, 0, 1, 1, 1, 1 );
					addComponent( splitFrame, gblSplit, splitpoint    										, 0, 1, 0, 1, 1, 1 );
					addComponent( splitFrame, gblSplit, new JLabel(" ")										, 0, 2, 1, 1, 1, 1 );
					addComponent( splitFrame, gblSplit, new JLabel("Name of new view:")						, 0, 3, 1, 1, 1, 0 );
					addComponent( splitFrame, gblSplit, nameOfNewView 										, 0, 4, 1, 1, 1, 0 );
					addComponent( splitFrame, gblSplit, new JLabel(" ")										, 0, 5, 1, 1, 1, 0 );
					addComponent( splitFrame, gblSplit, split          										, 0, 6, 1, 1, 1, 0 );

					splitFrame.pack();
					splitFrame.setVisible(true);
				}
			}
		});

		return splitBtn;
	}



	public JButton getPreview() {

		preview.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e){
				//				int selIndex = getDatenListe().getSelectedIndex();
				//				View view = getViews().get(selIndex);
				View view = getSelectedView();
				//				if (selIndex != -1){
				if (view != null){
					try{
						JFrame preview = new JFrame();

						preview.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().width *0.5), (int)(Toolkit.getDefaultToolkit().getScreenSize().height *0.5));

						preview.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-preview.getSize().width) / 2,
								(Toolkit.getDefaultToolkit().getScreenSize().height-preview.getSize().height) / 2 +50		);


						Container content = preview.getContentPane();

						preview.setTitle("Matrix-Preview - "+view.getViewName());

						//						MainGui.stopThreads();

						Double[][] data;
						try{
							data = new Double[view.getNumberOfExamples()][view.getNumberOfColumns()];	
							// write data
							for (int i = 0; i<view.getNumberOfExamples(); i++){
								for (int j = 0; j<view.getNumberOfColumns(); j++){
									data[i][j]= (Double)view.get(i, j);
								}
							}
						}catch(java.lang.OutOfMemoryError z){
							data = new Double[10][view.getNumberOfColumns()];	
							// write data
							for (int i = 0; i<10; i++){
								for (int j = 0; j<view.getNumberOfColumns(); j++){
									data[i][j]= (Double)view.get(i, j);
								}
							}
						}
						JTable table = new JTable( data, view.getFeatureDescriptors() );

						table.setEnabled(false);
						table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
						table.getTableHeader().setReorderingAllowed( false );
						table.getTableHeader().setResizingAllowed( true );

						content.add(new JScrollPane(table), BorderLayout.CENTER);
						preview.setVisible(true);

						//						MainGui.resumeThreads();
					}catch(NullPointerException f){
						System.out.println("NPE");
						f.printStackTrace();
					}
				} 

			}
		});   

		return preview;
	}
	
	/**
	 * 
	 * @return
	 */
	public JButton getViBtn() {
		viBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(getDatenListe().getSelectedIndices().length > 0 && getDatenListe().getSelectedIndex() != -1) {
					vi = new JDialog(master, true);
					vi.setTitle("VI");

					// Size of the JDialog
					Dimension dimNorm = new Dimension(400, 180);
					vi.setSize(dimNorm);
					vi.setPreferredSize(dimNorm);
					vi.setMaximumSize(dimNorm);
					vi.setMinimumSize(dimNorm);

					// Location and layout
					vi.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-vi.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height-vi.getSize().height) / 2);
				GridBagLayout gblVi = new GridBagLayout();
					vi.setLayout(gblVi);

					// Components
					nameOfNewView8 =  new JTextField();
					nameOfNewView8.setBorder(BorderFactory.createTitledBorder("Name of new view"));
					JButton viBt = new JButton("Create VI's");
					viBt.setToolTipText("The selected VI's are saved in a new view");

					viAppend = new JCheckBox("Append to existing view");
					
					// Components for Sigma
					sigmaPanel = new JPanel();
					sigmaUnit = new JLabel("nm");
					
					SpinnerNumberModel spinnerModel = new SpinnerNumberModel(20, 0, 100, 0.1);
					sigmaInput = new JSpinner(spinnerModel);
					
					sigmaPanel.add(sigmaInput);
					sigmaPanel.add(sigmaUnit);
					sigmaPanel.setBorder(BorderFactory.createTitledBorder("Epsilon"));
					sigmaPanel.setToolTipText("If a certain wavelength\nis needed to calculate\nan index which is\nnot in the view,\nthe closest wavelenght\nwill be used instead,\nepsilon defines the\nmax distance between those wavelengths");

					viSelection = new JPanel();
					viSelection.setLayout( new GridLayout(27,0) );
					viSelectionScroll = new JScrollPane(viSelection);

					viSelections = new JCheckBox[27];					
					viSelections[ 0] = new JCheckBox("Normalized Difference Vegetation Index");
					viSelections[ 1] = new JCheckBox("Simple Ratio Index");
					viSelections[ 2] = new JCheckBox("Enhanced Vegetation Index");
					viSelections[ 3] = new JCheckBox("Atmospherically Resistant Vegetation Index");
					viSelections[ 4] = new JCheckBox("Sum Green Index");
					viSelections[ 5] = new JCheckBox("Red Edge Normalized Difference Vegetation Index");
					viSelections[ 6] = new JCheckBox("Modified Red Edge Simple Ratio Index");
					viSelections[ 7] = new JCheckBox("Modified Red Edge Normalized Difference Vegetation Index");
					viSelections[ 8] = new JCheckBox("Vogelmann Red Edge Index 1");
					viSelections[ 9] = new JCheckBox("Vogelmann Red Edge Index 2");
					viSelections[10] = new JCheckBox("Vogelmann Red Edge Index 3");
					viSelections[11] = new JCheckBox("Red Edge Position Index");
					viSelections[12] = new JCheckBox("Photochemical Reflectance Index");
					viSelections[13] = new JCheckBox("Structure Insensitive Pigment Index");
					viSelections[14] = new JCheckBox("Red Green Ratio Index");
					viSelections[15] = new JCheckBox("Normalized Difference Nitrogen Index");
					viSelections[16] = new JCheckBox("Normalized Difference Lignin Index");
					viSelections[17] = new JCheckBox("Cellulose Absorption Index");
					viSelections[18] = new JCheckBox("Plant Senescence Reflectance Index");
					viSelections[19] = new JCheckBox("Carotenoid Reflectance Index 1");
					viSelections[20] = new JCheckBox("Carotenoid Reflectance Index 2");
					viSelections[21] = new JCheckBox("Anthocyanin Reflectance Index 1");
					viSelections[22] = new JCheckBox("Anthocyanin Reflectance Index 2");
					viSelections[23] = new JCheckBox("Water Band Index");
					viSelections[24] = new JCheckBox("Normalized Difference Water Index");
					viSelections[25] = new JCheckBox("Moisture Stress Index");
					viSelections[26] = new JCheckBox("Normalized Difference Infrared Index");

					viSelections[ 0].setToolTipText("Normalized difference of green leaf scattering in near-infrared, chlorophyll absorption in RED.");
					viSelections[ 1].setToolTipText("Ratio of green leaf scattering in near-infrared, chlorophyll absorption in RED.");
					viSelections[ 2].setToolTipText("An enhancement on the NDVI to better account for soil background and atmospheric aerosol effects.");
					viSelections[ 3].setToolTipText("An enhancement of the NDVI to better account for atmospheric scattering.");
					viSelections[ 4].setToolTipText("Sum Green Index");
					viSelections[ 5].setToolTipText("A modification of the NDVI using reflectance measurements along the red edge");
					viSelections[ 6].setToolTipText("A ratio of reflectance along the red edge with blue reflection correction.");
					viSelections[ 7].setToolTipText("A modification of the Red Edge NDVI using blue to compensate for scattered light.");
					viSelections[ 8].setToolTipText("A shoulder of the RED-to-NIR transition that is indicative of canopy stress.");
					viSelections[ 9].setToolTipText("A shape of the near-infrared transition that is indicative of the onset of canopy stress and senescence.");
					viSelections[10].setToolTipText("A shape of near-infrared transition that is indicative of the onset of canopy stress and senescence.");
					viSelections[11].setToolTipText("The location of the maximum derivative in near-infrared transition, which is sensitive to chlorophyll concentration. Here: Linearisation according to BARET und GUYOT, 1991");
					viSelections[12].setToolTipText("Useful to estimate absorption by leaf carotenoids (especially xanthophyll) pigments, leaf stress, and carbon dioxide uptake.");
					viSelections[13].setToolTipText("Indicator of leaf pigment concentrations normalized for variations in overall canopy structure and foliage content.");
					viSelections[14].setToolTipText("Ratio of reflectance in RED-to-GREEN sensitive to ratio of anthocyanin to chlorophyll.");
					viSelections[15].setToolTipText("Canopy Nitrogen");
					viSelections[16].setToolTipText("Dry or Senescent Carbon");
					viSelections[17].setToolTipText("Cellulose Absorption Index");
					viSelections[18].setToolTipText("Uses a ratio of carotenoids to chlorophyll to detect onset and degree of plant senescense.");
					viSelections[19].setToolTipText("Detects a relative difference in absorption indicative of changes in leaf total carotenoid concentration relative to chlorophyll concentration.");
					viSelections[20].setToolTipText("Similar to CRI1, but uses a different wavelength to estimate the chlorophyll content.");
					viSelections[21].setToolTipText("Changes in GREEN absorption relative to RED indicate leaf anthocyanins.");
					viSelections[22].setToolTipText("A variant of the ARI1, which is sensitive to changes in GREEN absorption relative to RED, indicating leaf anthocyanins.");
					viSelections[23].setToolTipText("Absorption intensity at 900 nm increases with canopy water content.");
					viSelections[24].setToolTipText("The rate of increase at 857 nm absorption relative to 1241 nm is a direct metric of total volumetric water content of vegetation.");
					viSelections[25].setToolTipText("Detects changes at 1599 nm absorption that is sensitive to the onset of moisture stress in vegetation.");
					viSelections[26].setToolTipText("Absorption intensity at 1649 nm increases with canopy water content.");

					final String[] viAbbreviations = new String[27];
					viAbbreviations[0] = "NDVI";
					viAbbreviations[1] = "SRI";
					viAbbreviations[2] = "EVI";
					viAbbreviations[3] = "ARVI";
					viAbbreviations[4] = "SGI";
					viAbbreviations[5] = "RENDVI";
					viAbbreviations[6] = "MRESRI";
					viAbbreviations[7] = "MRENDVI";
					viAbbreviations[8] = "Vogel1";
					viAbbreviations[9] = "Vogel2";
					viAbbreviations[10] = "Vogel3";
					viAbbreviations[11] = "REPI";
					viAbbreviations[12] = "PRI";
					viAbbreviations[13] = "SIPI";
					viAbbreviations[14] = "RGRI";
					viAbbreviations[15] = "NDNI";
					viAbbreviations[16] = "NDLI";
					viAbbreviations[17] = "CAI";
					viAbbreviations[18] = "PSRI";
					viAbbreviations[19] = "CRI1";
					viAbbreviations[20] = "CRI2";
					viAbbreviations[21] = "ARI1";
					viAbbreviations[22] = "ARI2";
					viAbbreviations[23] = "WBI";
					viAbbreviations[24] = "NDWI";
					viAbbreviations[25] = "MSI";
					viAbbreviations[26] = "NDII";

					for ( int i = 0; i < viSelections.length; i++ ) {
						viSelection.add(viSelections[i]);
						viSelections[i].setSelected(true);
					}
					viSelection.setBorder(BorderFactory.createTitledBorder("VIs"));

					viConfirm = new JButton("Select VIs");

					visPanel = new JPanel();
					GridBagLayout gblVis = new GridBagLayout();
					visPanel.setLayout(gblVis);
					addComponent(visPanel, gblVis, viSelectionScroll, 0, 0, 1, 27, 1, 1);
					addComponent(visPanel, gblVis, viConfirm, 0, 27, 1, 1, 1, 0);

					// JPanel for the selection of all or specific VIs
					viPreSelection = new JPanel();
					viPreSelection.setLayout( new GridLayout(3,0) );

					// RadioButtons to decide on the VIs
					viButtonGroup = new ButtonGroup();
					viPreSelections = new JRadioButton[2];
					viPreSelections[0] = new JRadioButton("All VIs");
					viPreSelections[1] = new JRadioButton("Selected VIs");
					viPreSelections[0].setSelected(true);

					for ( int i = 0; i < viPreSelections.length; i++ ) {
						viButtonGroup.add(viPreSelections[i]);
						viPreSelection.add(viPreSelections[i]);
					}

					// ActionListener for the RadioButton, which decides to select all VIs
					viPreSelections[0].addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							for ( int i = 0; i < viSelections.length; i++ ) {
								viSelections[i].setSelected(true);
							}
						}
					});

					// ActionListener for the RadioButton, which decides to select only selected VIs
					viPreSelections[1].addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							vis = new JDialog(vi,true); 
							vis.setTitle("Select VIs to be saved");

							// Size and position of the JDialog
							Dimension dimVI = new Dimension(370, 720);
							vis.setSize(dimVI);
							vis.setPreferredSize(dimVI);
							vis.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-vis.getSize().width) / 2,
									(Toolkit.getDefaultToolkit().getScreenSize().height-vis.getSize().height) / 2);

							vis.add(visPanel);

							// All selectable VIs are unselected, so just specific VIs can be choosen.
							for ( int i = 0; i < viSelections.length; i++ ) {
								viSelections[i].setSelected(false);
							}

							viConfirm.addActionListener( new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									vis.dispose();									
								}
							});

							vis.setVisible(true);

						}
					});

					viBt.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							// The currently selected view is saved
							AbstractViewImplementation view = (AbstractViewImplementation) getSelectedView();
							try{

								if(!nameOfNewView8.getText().equals("")){ // A name has been inserted

									double sigma = (Double) sigmaInput.getValue();
									
									// All VIs are saved in a Double Matrix
									DoubleMatrix viIndex = new DoubleMatrix(FeatureSelection.extractENVIFeatures(view, sigma));

									boolean[] outOfSigma = new boolean[viIndex.columns];
									
									// Extract the non-special features of the existing view, to put into the VI View
									int[] specialColumns = FeatureSelection.getSpecialFeatures(view);
									DoubleMatrix special = new DoubleMatrix(view.materializeFeatures(specialColumns));
									
									// The selected indexes ( see viSelections ) are extracted from all of the indexes and stored into this DoubleMatrix
									DoubleMatrix selectedViIndexes = new DoubleMatrix();

									// The names of the selected indexes are stored in this list
									LinkedList<String> viNames = new LinkedList<String>();

									for ( int i = 0; i < specialColumns.length; i++ ) {
										viNames.add(view.getFeatureDescriptors()[specialColumns[i]]);
									}

									// Here, only the selected VIs are extracted out of all + values out of sigma are deleted
									for ( int i = 0; i < viIndex.columns; i++ ) {
										if ( viSelections[i].isSelected() ) {
											if ( viIndex.getColumn(i).get(1) != Integer.MIN_VALUE) {
												// This is the first selected index, new DoubleMatrix is created
												if ( selectedViIndexes.rows == 0) 
													selectedViIndexes = viIndex.getColumn(i);
												// Indexes have already been added, so the new one is concated on the right
												else 
													selectedViIndexes = DoubleMatrix.concatHorizontally(selectedViIndexes, viIndex.getColumn(i));
												// The name of the VI is saved
												viNames.add(viAbbreviations[i]);
												outOfSigma[i] = false;
											}
											else {
												outOfSigma[i] = true;
											}
										}
									}

									// The non-special and special Features are merged
									special = DoubleMatrix.concatHorizontally(special, selectedViIndexes);

									// The new view is created
									DoubleMatrixView viView = new DoubleMatrixView(special, nameOfNewView8.getText() + "_VI");

									// The formerly stored feature descriptors are set
									for ( int i = 0; i < viNames.size(); i++ ) {
										viView.setFeatureDescriptor(i, viNames.get(i));
									}

									// The feature roles for the special features are stored
									for ( int i = 0; i < specialColumns.length; i++ ) {
										viView.setFeatureRole(i, view.getFeatureRoles()[specialColumns[i]]);
									}

									// The view is added to the list of view in the GUI or appended to the existing view
									if ( viAppend.isSelected() ) {
										view = new MergeFeaturesView( view, viView );
										view.setViewName(nameOfNewView8.getText() + "_appended_VI");
										master.getProject().add(view);
									} else {
										master.getProject().add(viView);
									}
									
									String notCalculated = "";
									for ( int i = 0; i < outOfSigma.length; i++ )
										if (outOfSigma[i] == true)
											notCalculated = notCalculated + ", " + viAbbreviations[i];
									
									if ( notCalculated.length() != 0 ) {
										JOptionPane.showMessageDialog(master,"Not all ENVIs could be calculated (" + notCalculated.substring(2, notCalculated.length() ) +")","Error",JOptionPane.INFORMATION_MESSAGE);
									}
								}
								else { // No name for the new view is inserted
									JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.INFORMATION_MESSAGE);
								}

								vi.dispose();
								actualiseListen();
								nameOfNewView8.setText("");

							}catch(Exception wge) {
								wge.printStackTrace();
								//JOptionPane.showMessageDialog(getMaster(),wge.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							}
						}
					});

					// Components added
					addComponent(vi, gblVi, viPreSelection	, 1, 1, 1, 2, 0, 0 );
					addComponent(vi, gblVi, viAppend		, 2, 1, 1, 1, 0, 0 );
					addComponent(vi, gblVi, nameOfNewView8 	, 1, 3, 1, 1, 1, 0 );
					addComponent(vi, gblVi, viBt		 	, 2, 3, 1, 1, 0, 0 );
					addComponent(vi, gblVi, sigmaPanel      , 2, 2, 1, 1, 0, 0 );	

					vi.setVisible(true);
				}else{ // No view is selected
					JOptionPane.showMessageDialog(master,"Please select View!","Error",JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		return viBtn;
	}

	public JButton getGenerateThumbnail() {

		generateThumbnail.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e){
				try{

					View view = getSelectedView();
//					int selIndex = getDatenListe().getSelectedIndex();
					if (view != null){

//						View view = getViews().get(selIndex);
						Image im = generateThumbnail(view);
						view.setThumbnail(im);

						actualiseListen();

					}
				}catch(Exception srg){

				}
			}
		});  

		return generateThumbnail;
	}


	public JButton getRename() {

		rename.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
//				int selIndex = getDatenListe().getSelectedIndex();
				View view = getSelectedView();
				if (view == null){
					//					JOptionPane.showMessageDialog(master,"Please select data!","Error",JOptionPane.INFORMATION_MESSAGE);
				} else{
					if (!renameView.getText().equals("")){
						view.setViewName(renameView.getText());
						//TODO Frage: soll bei Namensänderung unsaved sein?
						//						getViews().get(selIndex).setSaved(false);
						actualiseListen();
						renameView.setText("");
					}
				}
			}
		});

		return rename;
	}


	public JButton getSetIcon() {

		setIcon.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				View view = getSelectedView();
//				int selIndex = getDatenListe().getSelectedIndex();
				if (view != null){
					File file = null;
					JFileChooser f = new JFileChooser();
					f.setCurrentDirectory(new File(master.getDirectoryProp().get("DATA_PATH").toString()));
					f.setDialogTitle("Choose an icon. Will be scaled to 40x40px.");
					f.setFileFilter(new FileFilter(){

						@Override public boolean accept( File f ){

							return f.isDirectory() || f.getName().toLowerCase().endsWith(".gif" ) || f.getName().toLowerCase().endsWith(".png" );
						}

						@Override public String getDescription(){
							return "Icon (.gif, .png)";
						}
					} );

					int state = f.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION ){
						file = f.getSelectedFile();
						master.getDirectoryProp().setProperty("DATA_PATH", file.getParent());

						ImageIcon im = new ImageIcon(file.getAbsolutePath());
						ImageIcon scaled = null;
						if (im != null) {
							//							scaled = new ImageIcon(im.getImage().getScaledInstance(40, 40,Image.SCALE_DEFAULT));
							if (im.getIconHeight() != 40) {
								scaled = new ImageIcon(im.getImage().getScaledInstance(-1, 40,Image.SCALE_DEFAULT));
								//							} else if (im.getIconHeight() < 40) { 
								//								scaled = new ImageIcon(im.getImage().getScaledInstance(-1, 40,Image.SCALE_DEFAULT));
								//								//								im.getImage().
							}else{ //no need to miniaturize
								scaled = im;
							}

						}

						//scale Image to 40x40 px
						//							Image i = new BufferedImage(40,40,BufferedImage.TYPE_INT_ARGB);
						//							i.getGraphics().drawImage(buffImg,0,0,40,40,null);
						try{
							view.setThumbnail(scaled.getImage());
						}catch(Exception edr){

						}
						actualiseListen();
					}
				}

			}
		});

		return setIcon;
	}
	
	public JButton getValidateLabel() {
		validateLabel=new JButton("Validate Label");
		validateLabel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				View view = master.getProject().getActiveView();
//				int selIndex = getDatenListe().getSelectedIndex();
				
				if (view != null){
					File file = null;
					JFileChooser f = new JFileChooser();
					f.setCurrentDirectory(new File(master.getDirectoryProp().get("DATA_PATH").toString()));
					f.setDialogTitle("Choose an annotated PNG image. Provide txt class list");
					f.setFileFilter(new FileFilter(){

						@Override public boolean accept( File f ){

							return f.isDirectory() || f.getName().toLowerCase().endsWith(".png" );
						}

						@Override public String getDescription(){
							return "Image (.png)";
						}
					} );

					int state = f.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION ){
						file = f.getSelectedFile();
						master.getDirectoryProp().setProperty("DATA_PATH", file.getParent());

						BufferedImage img = null;
						try {
						    img = ImageIO.read(file);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						
						if (img != null) {
							//							scaled = new ImageIcon(im.getImage().getScaledInstance(40, 40,Image.SCALE_DEFAULT));
							if(img.getWidth()!=master.getProject().getActiveView().getXDimension()){
								System.out.println("Incompatible Dimensions!");
							}
							else{
								File txtFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3)+"txt");
								
								InputStreamReader inputStreamReader;
								LinkedList<String[]> labelColors = new LinkedList<String[]>();
								try {
									inputStreamReader = new InputStreamReader(new FileInputStream (txtFile) );
									BufferedReader bufferedReader = new BufferedReader (inputStreamReader);
									
									String line;
									String[] defColors=new String[]{"r","g","b","c","m","ye","k","w","grey"};
									int counter=0;
									while ((line = bufferedReader.readLine()) != null){
								       String[] lineSplit=line.split("\\s+");
								       String[] entry = new String[2];
								       if (lineSplit.length==1){
								    	   entry[0]=defColors[counter];
								    	   entry[1]=lineSplit[0];
								       }else{
								    	   entry[0]=lineSplit[0];
								    	   entry[1]=lineSplit[1];
								       }
								       labelColors.add(entry);
								       counter++;
								    }
								  bufferedReader.close();
									
									
								} catch (FileNotFoundException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								Map<String, LinkedList<Point>> annos= Utilities.getLabelLocations(img,labelColors);
								System.out.println("#"+annos.entrySet().size());
								for(Entry<String, LinkedList<Point>> entry:annos.entrySet()){
									System.out.println(entry.getKey() +": "+entry.getValue().size() + "px");
								}
								BufferedImage imgVal= Utilities.validateLabel (annos, master.getProject().getActiveView());
								JFrame frame = new JFrame();
								frame.getContentPane().setLayout(new FlowLayout());
								frame.getContentPane().add(new JLabel(new ImageIcon(imgVal)));
								frame.pack();
								frame.setVisible(true);
							}
							
							
							
							
							

						}

						
					
					
					}

				}
			}
		});

		return validateLabel;
	}
	
	private JButton getCreateTrainViewBtn() {
		createTrainViewBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
			//	if(!selectedViewIsTV()){
					View v = getSelectedView();
					TrainView tv = TrainView.createTrainViewFromView(v);
					master.getProject().getTViewList().add(tv);
					actualiseListen();
			//	}
			}
		});
		return createTrainViewBtn;
	}

	public JButton getLabelingBtn() {

		labelingBtn.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {

				if(getDatenListe().getSelectedIndices().length == 1 && getDatenListe().getSelectedIndex() != -1 ){

					labeling = new JDialog(master, true);
					labeling.setTitle("Labeling");
					labeling.setSize(600, 350);
					labeling.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-labeling.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height-labeling.getSize().height) / 2 +50		);

					GridBagLayout gblLabeling = new GridBagLayout();
					labeling.setLayout( gblLabeling );

					if(!getSelectedView().getLabelMap().isEmpty()){
						labels = new HashMap<Integer, String>(getSelectedView().getLabelMap());
					}else{
						labels = new HashMap<Integer, String>();
					}


					JPanel fromto = new JPanel();
					fromto.setLayout( new GridLayout(0, 4) );
					from = new JTextField();
					from.setBorder(BorderFactory.createTitledBorder("from"));
					fromto.add(from);
					to = new JTextField();
					to.setBorder(BorderFactory.createTitledBorder("to"));
					fromto.add(to);
					classBox = new JComboBox<JLabel>();
					// Cell renderer to show icon
					classBox.setRenderer(new IconRenderer2());
					classBox.setBorder(BorderFactory.createTitledBorder("label"));
					fromto.add(classBox);

					actualiseClassBox();

					JButton addInterval = new JButton("Add");
					fromto.add(addInterval);


					intervalList = new JList<Vector<Interval>>();
					intervals = new ArrayList<Interval>();

					getIntervalList().setListData(getIntervalsAsVector());

					neu = new JRadioButton("create new");
					overwrite = new JRadioButton("overwrite");
					ButtonGroup bg = new ButtonGroup();
					bg.add(neu);
					bg.add(overwrite);

					JButton enter = new JButton("Enter");

					JButton reset = new JButton("Reset");
					status = new JTextField();
					status.setEditable(false);

					JButton removeInt = new JButton("Remove");
					addClass = new JButton("Add Class");

					//					JButton removeClass = new JButton("Remove Class");



					if(FeatureSelection.getLabel(getSelectedView()) ==-1){
						overwrite.setEnabled(false);
						neu.setSelected(true);
					}else{
						overwrite.setEnabled(true);
						overwrite.setSelected(true);
					}

					addInterval.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try{
								//TODO
								int index = classBox.getSelectedIndex();
								int labelInt = new ArrayList<Integer>(labels.keySet()).get(index);

								if (!(from.getText().equals("") || to.getText().equals("") || classBox.getSelectedIndex()==-1)){
									try{

										int fromInt = Integer.parseInt(from.getText());
										int toInt = Integer.parseInt(to.getText());

										if (fromInt>toInt || fromInt < 1 || toInt < 1){
											status.setText("Illegal Interval");
										}else{
											if (!labels.containsKey(labelInt)){
												status.setText("Illegal Label");
											}else{
												if(toInt <= getSelectedView().getNumberOfExamples()){
													//TODO
													Interval interval = new Interval(fromInt, toInt, labelInt);
													getIntervals().add(interval);
													status.setText("");

													for (int i = fromInt-1; i<toInt; i++){
														labeled[i] += 1;
													}

													getIntervalList().setListData(getIntervalsAsVector());
												}else{
													status.setText("Interval exceeds number of Samples");
												}
											}
										}
									}catch(NumberFormatException e1){
										status.setText("Please insert numbers.");
									}
								}
								//							}catch(IndexOutOfBoundsException srg){

							}catch(Exception a){
								a.printStackTrace();
							}
						}
					});

					reset.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {

							getIntervals().clear();
							labeled = null;
							status.setText("");

							getIntervalList().setListData(getIntervalsAsVector());
							actualiseListen();
						}
					});

					enter.addActionListener(new ActionListener() {


						@Override
						public void actionPerformed(ActionEvent e) {

							try{

								boolean okay = true;
								for (int i = 0; i<labeled.length; i++){
									if (labeled[i] > 1){
										status.setText("Multiple label at sample "+(i+1));
										okay =false;
										break;
									}
								}
								if (neu.isSelected()){
									for (int i = 0; i<labeled.length; i++){
										if (labeled[i] == 0){
											status.setText("Sample "+(i+1)+" is unlabeled");
											okay =false;
											break;
										}
									}
								}

//								View selView = master.getProject().get(getDatenListe().getSelectedIndex());
								View selView = getSelectedView();
								
								double[] labelArray;
								AddLabelsView newView;
								if (neu.isSelected() &&  okay == true){
									labelArray = new double[selView.getNumberOfExamples()];
									for (Object o: getIntervals()){
										for (int a = ((Interval)o).from-1; a<((Interval)o).to; a++){
											labelArray[a] = ((Interval)o).value;
										}
									}

									//									newView = new AddLabelsView(labelArray, selView, master.getDirectoryProp().getProperty("DATA_PATH"));
									newView = new AddLabelsView(labelArray, selView, master.getDirectoryProp().getProperty("DATA_PATH"));

								}else if (overwrite.isSelected() && FeatureSelection.getLabel(selView) != -1){
									int anzahlLbl=0;

									for (Object o: getIntervals()){
										for (int a = ((Interval)o).from-1; a<((Interval)o).to; a++){
											anzahlLbl++;
										}
									}

									int[] indices = new int[anzahlLbl];
									labelArray = new double[anzahlLbl];

									for (Object o: getIntervals()){
										int ind = 0;	//zweiter Iterator
										for (int a = ((Interval)o).from-1; a<((Interval)o).to; a++){
											indices[ind] = a;
											labelArray[ind] = ((Interval)o).value;
											ind++;
										}
									}	

									try{
										((AddLabelsView)selView).setLabelAt(indices, labelArray, master.getDirectoryProp().getProperty("DATA_PATH"));
										newView = (AddLabelsView) selView;
									}catch(ClassCastException ef){

										//										System.out.println("caught");

										int [] indicesNew = new int[selView.getNumberOfColumns()-1];
										int k=0;
										int lblIndex = FeatureSelection.getLabel(selView);
										for(int i=0;i<selView.getNumberOfColumns();i++){
											if(i!=lblIndex){
												indicesNew[k]=i;
												k++;
											}
										}

										FeatureFilter allButLabel = new FeatureFilter(selView, indicesNew);
										double[] lbl = selView.materializeFeature(lblIndex);
										double[] newlabels = new double[lbl.length];
										for(int i=0; i<lbl.length; i++){
											newlabels[i] = lbl[i];
										}
										for(int i=0; i<indices.length; i++){
											newlabels[indices[i]] = labelArray[i];
										}

										newView = new AddLabelsView(newlabels, allButLabel, master.getDirectoryProp().getProperty("DATA_PATH"));
										newView.setViewName(selView.getViewName() + "_labeled");
										newView.setViewDescription("Labels edited of "+selView.getViewName());
									}
								}else{
									throw new Error();
								}
								newView.setLabelMap(labels);
								if(selectedViewIsTV()){
									master.getProject().getTViewList().add(new TrainView(newView));
								}else{
									master.getProject().getViewList().add(newView);
								}
//								getViews().add(newView);
								labeling.dispose();
								actualiseListen();

							}catch(Error sgr){

							}catch(Exception sgh){
								sgh.printStackTrace();
							}
						}
					});

					getAddClass();


					removeInt.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							try{
								int selInd = getIntervalList().getSelectedIndex();
								
								if (selInd != -1){

									Interval interval = getIntervals().get(selInd);
									for (int i = interval.from-1; i<interval.to; i++){
										labeled[i] -=1;
									}
									getIntervals().remove(selInd);
									getIntervalList().setListData(getIntervalsAsVector());
								}
							}catch(Exception rg){

							}
						}
					});

					addComponent(labeling, gblLabeling, fromto		, 0, 0, 4, 1, 1, 0);
					addComponent(labeling, gblLabeling, new JScrollPane(intervalList), 0, 1, 4, 1, 1, 1);
					addComponent(labeling, gblLabeling, removeInt	, 0, 2, 1, 1, 1, 0);
					addComponent(labeling, gblLabeling, reset 		, 1, 2, 1, 1, 1, 0);
					addComponent(labeling, gblLabeling, addClass 	, 2, 2, 1, 1, 1, 0);
					//					addComponent(labeling, gblLabeling, removeClass	, 3, 2, 1, 1, 1, 0);
					addComponent(labeling, gblLabeling, status		, 0, 3, 4, 1, 1, 0);
					addComponent(labeling, gblLabeling, neu      	, 1, 4, 1, 1, 0, 0);
					addComponent(labeling, gblLabeling, overwrite	, 2, 4, 1, 1, 0, 0);
					addComponent(labeling, gblLabeling, enter		, 3, 4, 1, 1, 1, 0);

					labeling.setVisible(true);

				}else{

				}

			}
		});

		return labelingBtn;
	}


	public JButton getChangeFeatureRoles() {

		changeFeatureRoles.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if ((!isselectedViewTrainView && orderOfSelectedViews.size() > 0) || (isselectedViewTrainView && orderOfSelectedTV.size() > 0)){
					
					if (isselectedViewTrainView){
						new ViewEditor(master, master.getProject().getTViewList().get(orderOfSelectedTV.get(0)));						
					}
					else {
						new ViewEditor(master, master.getProject().getViewList().get(orderOfSelectedViews.get(0)));
						
					}
					
				}
				
			}


		});

		return changeFeatureRoles;
	}



	boolean duplicatesCoordinates(View vA, View vB){
		double[] x_coordA = vA.materializeFeature(FeatureSelection.getXCoordinate(vA));
		double[] y_coordA = vA.materializeFeature(FeatureSelection.getYCoordinate(vA));
		double[] x_coordB = vB.materializeFeature(FeatureSelection.getXCoordinate(vB));
		double[] y_coordB = vB.materializeFeature(FeatureSelection.getYCoordinate(vB));

		Set<Point2D.Double> lump = new HashSet<Point2D.Double>();
		for(int i=0; i<x_coordA.length; i++){
			lump.add(new Point2D.Double(x_coordA[i],y_coordA[i]));
		}
		for(int i=0; i<x_coordB.length; i++){
			if (lump.contains(new Point2D.Double(x_coordB[i],y_coordB[i]))) return true;
		}
		return false;
	}

	public JButton getJoinBtn() {

		joinBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try{
					ArrayList<Integer> orderOfSelected;
					if (isselectedViewTrainView){
						orderOfSelected= orderOfSelectedTV;
					}
					else {
						orderOfSelected = orderOfSelectedViews;
					}
					if (orderOfSelected.size() > 1){

						if(applicableMergeS || applicableMergeF){

//							selected =  (ArrayList<Integer>) orderOfSelected.clone();

							joinFrame = new JDialog(master,true);
							joinFrame.setTitle("Merge Views");
							joinFrame.setSize(300, 150);
							joinFrame.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-joinFrame.getSize().width) / 2,
									(Toolkit.getDefaultToolkit().getScreenSize().height-joinFrame.getSize().height) / 2 +50		);

							JButton joinSamples = new JButton("Join Samples");
							joinSamples.setToolTipText("Concatenates multiple matrices vertically.");
							joinSamples.setEnabled(applicableMergeS);
							JButton joinFeatures = new JButton("Join Features");
							joinFeatures.setToolTipText("Concatenates multiple matrices horizontally.");
							joinFeatures.setEnabled(applicableMergeF);

							nameOfNewView2 = new JTextField();

							//JPanel selListPanel = new JPanel();

							GridBagLayout gblJoin = new GridBagLayout();
							joinFrame.setLayout(gblJoin);


							joinSamples.addActionListener(new ActionListener() {
								public void actionPerformed (ActionEvent e){

									try{
										try{
											ArrayList<View> views = getSelectedViews();

											boolean duplic = false;
											for(int i=0; i<views.size()-1; i++){
												if(duplicatesCoordinates(views.get(i),views.get(i+1))){
													duplic = true;
													break;
												}
											}

											if(duplic){
												JOptionPane.showMessageDialog(master, "Two or more views contain identical coordinates.\nVisualistation will be removed.", "Duplicate Coordinates",  JOptionPane.QUESTION_MESSAGE, iconCrd);
											}
											
											View firstSelView = views.get(0);


											for (int i=1; i<views.size(); i++){

												View selViewNext = views.get(i);

												MergeExamplesView neu = new MergeExamplesView(firstSelView, selViewNext);

												firstSelView = neu;

											}

											if (nameOfNewView2.getText().equals("")){
												throw new Error();
											}

											if(duplic  && FeatureSelection.getRGB(firstSelView)!=-1){
												int[] colOhneRGB = new int[firstSelView.getNumberOfColumns()-1];
												int k=0;
												for(int i = 0; i<firstSelView.getNumberOfColumns();i++){
													if(firstSelView.getFeatureRoles()[i]!=FeatureRole.RGB){
														colOhneRGB[k]= i;
														k = k+1;
													}
												}
												firstSelView = new FeatureFilter(firstSelView,colOhneRGB);
											}
											
											firstSelView.setViewName(nameOfNewView2.getText());
											String description = "Merged samples of ";
											for (View i : views){
												description += i.getViewName() + " and ";
											}
											description = description.substring(0, description.length()-5);
											firstSelView.setViewDescription(description);					
											
//											getViews().add(firstSelView);
											if(!isselectedViewTrainView){
												master.getProject().getViewList().add(firstSelView);
											}else{
												master.getProject().getTViewList().add(new TrainView(firstSelView));
											}

											actualiseListen();

											emptyJoinFields();

											joinFrame.dispose();


										}catch(Error e2){
											JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.INFORMATION_MESSAGE);
										}
									}catch(NumberFormatException b){
										JOptionPane.showMessageDialog(master,"File broken!","Error",JOptionPane.ERROR_MESSAGE);
									}catch(IllegalArgumentException a){
										a.printStackTrace();
										JOptionPane.showMessageDialog(master,a.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
									}catch(Exception srg){
										srg.printStackTrace();
									}
								}
							});   

							joinFeatures.addActionListener(new ActionListener() {
								public void actionPerformed (ActionEvent e){

									try{
										try{
											if (nameOfNewView2.getText().equals("")){
												throw new Error();
											}
											ArrayList<View> views = getSelectedViews();
											View akkumulatorView = views.get(0);
											//DoubleMatrix akkumulatorMatrix = new DoubleMatrix(firstSelView.materialize());

											for (int i=1; i<views.size(); i++){

												View selViewNext = views.get(i);
												//DoubleMatrix selViewNextMatrix = new DoubleMatrix(selViewNext.materialize());
												//									if (akkumulatorView.getNumberOfExamples() != selViewNext.getNumberOfExamples()){
												//										throw new Error("Matrix Dimensions must agree!");
												//									}
												MergeFeaturesView neuerView = new MergeFeaturesView(akkumulatorView,selViewNext);
												//DoubleMatrix neueMatrix = DoubleMatrix.concatHorizontally(akkumulatorMatrix, selViewNextMatrix);
												akkumulatorView = neuerView;

											}

											//MergeFeaturesView m1 = new MergeFeaturesView(getViews().get(selIndices[0]),getViews().get(selIndices[1]));
											akkumulatorView.setViewName(nameOfNewView2.getText());
											String description = "Merged features of ";
											for (View i : views){
												description += i.getViewName() + " and ";
											}
											description = description.substring(0, description.length()-5);
											akkumulatorView.setViewDescription(description);

//											getViews().add(akkumulatorView);
											if(!isselectedViewTrainView){
												master.getProject().getViewList().add(akkumulatorView);
											}else{
												master.getProject().getTViewList().add(new TrainView(akkumulatorView));
											}

											actualiseListen();
											emptyJoinFields();
											joinFrame.dispose();

										}catch(Error e2){
											JOptionPane.showMessageDialog(master,"Please insert name!","Error",JOptionPane.INFORMATION_MESSAGE);
										}
									}catch(NumberFormatException b){
										JOptionPane.showMessageDialog(master,"File broken!","Error",JOptionPane.ERROR_MESSAGE);

									}catch(IllegalArgumentException i){
										JOptionPane.showMessageDialog(master,"Number of examples in parents does not match!","Error",JOptionPane.ERROR_MESSAGE);
									}catch(Error a){
										JOptionPane.showMessageDialog(master,a.getMessage() ,"Error",JOptionPane.ERROR_MESSAGE);
									}	catch(Exception srg){
										srg.printStackTrace();
									}					
								}

								
							});  


							//							addComponent(joinFrame, gblJoin, new JLabel("Select data from this list using Ctrl:"), 0, 0, 2, 1, 1, 1 );
							addComponent(joinFrame, gblJoin, new JLabel(" ")									 , 0, 2, 2, 1, 1, 0 );
							addComponent(joinFrame, gblJoin, new JLabel("Name of new view:")					 , 0, 3, 2, 1, 1, 0 );
							addComponent(joinFrame, gblJoin, nameOfNewView2                     				 , 0, 4, 2, 1, 1, 0 );
							addComponent(joinFrame, gblJoin, new JLabel(" ")									 , 0, 5, 2, 1, 1, 0 );
							addComponent(joinFrame, gblJoin, joinSamples                        				 , 0, 6, 1, 1, 1, 0 );
							addComponent(joinFrame, gblJoin, joinFeatures                       				 , 1, 6, 1, 1, 1, 0 );

							joinFrame.pack();
							joinFrame.setLocationRelativeTo(null);	
							joinFrame.setVisible(true);	
							joinFrame.validate();
							joinFrame.repaint();

						}else{
							JOptionPane.showMessageDialog(master,"Views can't be merged.","Error",JOptionPane.INFORMATION_MESSAGE);
						}
					}else{
						JOptionPane.showMessageDialog(master,"Please select at least two views!","Error",JOptionPane.INFORMATION_MESSAGE);
					}

				}catch(NullPointerException g){

				}catch(Exception srg){
					srg.printStackTrace();
				}
			}
		});

		return joinBtn;
	}

	
	
	public JButton getDelete() {

		delete.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e){
//				
				if(isselectedViewTrainView){
					if(orderOfSelectedTV.size()==0 || orderOfSelectedTV.get(0)==-1) return;
				}
				else{
					if(orderOfSelectedViews.size()==0 || orderOfSelectedViews.get(0)==-1) return;
				}
				

				if(isselectedViewTrainView){
					
					ArrayList<TrainView> liste =  master.getProject().getTViewList();
					for (int ind :orderOfSelectedTV )
						liste.remove(ind);
					master.getProject().setTViewList(liste);
					orderOfSelectedTV.clear();
					
				}
				else{
					master.getProject().getViewList().remove(orderOfSelectedViews.get(0).intValue());
					orderOfSelectedViews.clear();
				}
				
				
					getDatenListe().clearSelection();
					getTrainViewListe().clearSelection();
					
					actualiseListen();
					
					metaTextPane.setText("");
					
				
			}
		});   

		return delete;
	}



	public JButton getSelModelBtn() {

		selModelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				featureSelectionFrame = new JDialog(master, false);
				featureSelectionFrame.setTitle("Feature Selection");
				featureSelectionFrame.setSize(350, 150);
				featureSelectionFrame.setResizable(false);
				featureSelectionFrame.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-featureSelectionFrame.getSize().width) / 2,
						(Toolkit.getDefaultToolkit().getScreenSize().height-featureSelectionFrame.getSize().height) / 2 +50		);

				GridBagLayout gblFS = new GridBagLayout();
				featureSelectionFrame.setLayout( gblFS );

				topX =	new JRadioButton("Select top x:",true);
				topXValue = new JTextField();
				threshold = new JRadioButton("Select values from threshold:",false);
				thresholdValue = new JTextField();
				ButtonGroup group = new ButtonGroup();
				group.add(topX);
				group.add(threshold);

				JButton additionalFeatures = new JButton("Select Features manually");
				additionalFeatures.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						addFeatures = new JDialog(featureSelectionFrame, true);
						addFeatures.setTitle("Select Features manually");
						addFeatures.setSize(205, 500);
						addFeatures.setResizable(false);
						addFeatures.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-addFeatures.getSize().width) / 2 + 300,
								(Toolkit.getDefaultToolkit().getScreenSize().height-addFeatures.getSize().height) / 2 +50		);

						FeatureWeightsModel model = ((FeatureWeightsModel) getModels().get(getModelListe().getSelectedIndex()));
						double[] weights = model.getWeights();
						String[] waveL = model.getWavelengths();

						String[][] tableData = new String[waveL.length][2];
						for(int i=0; i<tableData.length; i++){
							tableData[i][0] = waveL[i];
							tableData[i][1] = String.valueOf(weights[i]);
						}
						String[] header = {"Wavelengths", "Weights"};
						JTable table2 = new JTable(tableData, header);
						table2.setRowHeight(16);
						table2.setEnabled(false);
						int[] indices;
						boolean more = false;
						try{
							double thresholdD = Double.NaN;

							if (topX.isSelected()){

								int x = Integer.parseInt(topXValue.getText());

								ArrayList<Double> weightsSort = new ArrayList<Double>();
								for (int i=0 ; i<weights.length; i++){
									weightsSort.add(weights[i]);
								}
								Collections.sort(weightsSort);
								thresholdD = weightsSort.get(weightsSort.size()-x);

							} else if (threshold.isSelected()){
								thresholdD = Double.parseDouble(thresholdValue.getText());
							}


							int j=0;
							for(int i=0; i<weights.length; i++){
								if(weights[i]>=thresholdD){
									j++;
								}
							}
							indices = new int[j];
							double[] newWeights = new double[j];
							String[] newWaveL = new String[j];

							int k=0;  // Iterator
							for(int i=0; i<weights.length; i++){
								if(weights[i] >= thresholdD){
									indices[k] = i;
									newWeights[k] = weights[i];
									newWaveL[k] = waveL[i];
									k++;
								}
							}

							if(k > Integer.parseInt(topXValue.getText())){
								more = true;
							}

						}catch(Exception we){
							indices = new int[0];
						}


						if(more){
							JOptionPane.showMessageDialog(null,"More than the specified number of weights have been selected.","Info",JOptionPane.INFORMATION_MESSAGE);
						}

						checkBoxList2 = new JList<JCheckBox>();
						checkBoxList2.setToolTipText("Select features");
						checkBoxList2.setCellRenderer(new CheckListRenderer());
						checkBoxList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
						checkBoxList2.setFixedCellHeight(16);
						checkBoxList2.setFixedCellWidth(16);
						checkBoxList2.addMouseListener(new MouseAdapter(){

							public void mouseClicked(MouseEvent event){
								JList<?> list = (JList<?>) event.getSource();
								int index = list.locationToIndex(event.getPoint());

								JCheckBox item2 = (JCheckBox)list.getModel().getElementAt(index);

								if(item2.isSelected()){
									item2.setSelected(false); 
								}else{
									item2.setSelected(true); 
								}

								list.repaint(list.getCellBounds(index, index));
							}
						});  

						Vector<JCheckBox> v = new Vector<JCheckBox>();
						for (int i = 0; i<model.getWeights().length; i++){
							v.add(new JCheckBox());
						}
						checkBoxList2.setListData(v);


						for (int i=0; i<indices.length;i++){
							JCheckBox item = (JCheckBox)checkBoxList2.getModel().getElementAt(indices[i]);
							//								if(i == indices[i]){
							item.setSelected(true); 
							//								}else{
							//									item.setSelected(false);
							//								}
						}

						JScrollPane scrp = new JScrollPane();
						JPanel listen = new JPanel();
						GridBagLayout gblListen = new GridBagLayout();
						listen.setLayout(gblListen);

						addComponent(listen, gblListen, checkBoxList2, 0, 0, 1, 1, 1, 0);
						addComponent(listen, gblListen, table2, 1, 0, 1, 1, 1, 1);
						scrp.setViewportView(listen);

						JButton submit = new JButton("Submit");
						submit.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								int k = 0;
								for (int i=0; i<checkBoxList2.getModel().getSize();i++){
									JCheckBox item = (JCheckBox)checkBoxList2.getModel().getElementAt(i);
									if(item.isSelected()){
										k++;
									}
								}

								int[] finalIndices = new int[k]; 
								k=0;
								for (int i=0; i<checkBoxList2.getModel().getSize();i++){
									JCheckBox item = (JCheckBox)checkBoxList2.getModel().getElementAt(i);
									if(item.isSelected()){
										finalIndices[k] = i;
										k++;
									}
								}

								try{

									FeatureWeightsModel model = ((FeatureWeightsModel) getModels().get(getModelListe().getSelectedIndex()));
									double[] weights = model.getWeights();
									String[] waveL = model.getWavelengths();

									double[] newWeights = new double[finalIndices.length];
									String[] newWaveL = new String[finalIndices.length];

									int m=0;  // Iterator
									for(int i=0; i<finalIndices.length; i++){

										newWeights[m] = weights[finalIndices[i]];
										newWaveL[m] = waveL[finalIndices[i]];
										m++;

									}

									FeatureSelectionModel fsModel = new FeatureSelectionModel("FeatureSelectionModel_"+model.getName()+" manually selected Features", model.getDatensatz(), new Date(), finalIndices, newWeights, newWaveL);
									getModels().add(fsModel);
									actualiseListen();
									addFeatures.dispose();
									featureSelectionFrame.dispose();
								}catch(Exception rg){
									rg.printStackTrace();
								}
							}
						});

						GridBagLayout gblAddFea = new GridBagLayout();
						addFeatures.setLayout(gblAddFea);
						JLabel headerLbl = new JLabel("     Wavelengths   Weights");
						headerLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
						addComponent(addFeatures, gblAddFea, headerLbl, 0, 0, 1, 1, 1, 0);
						addComponent(addFeatures, gblAddFea, scrp, 0, 1, 1, 1, 1, 1);
						addComponent(addFeatures, gblAddFea, submit, 0, 2, 1, 1, 1, 0);

						addFeatures.setVisible(true);

					}
				});

				JButton fsEnter = new JButton("Enter");
				fsEnter.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						try{

							FeatureWeightsModel model = ((FeatureWeightsModel) getModels().get(getModelListe().getSelectedIndex()));
							double[] weights = model.getWeights();
							String[] waveL = model.getWavelengths();
							double thresholdD = Double.NaN;
							String name = "";

							if (topX.isSelected()){

								int x = Integer.parseInt(topXValue.getText());
								ArrayList<Double> weightsSort = new ArrayList<Double>();
								for (int i=0 ; i<weights.length; i++){
									weightsSort.add(weights[i]);
								}
								Collections.sort(weightsSort);
								thresholdD = weightsSort.get(weightsSort.size()-x);

							} else if (threshold.isSelected()){
								thresholdD = Double.parseDouble(thresholdValue.getText());
								name += " threshold "+thresholdD;
							}


							int j=0;
							for(int i=0; i<weights.length; i++){
								if(weights[i]>=thresholdD){
									j++;
								}
							}
							int confirmation = 0;
							if(topX.isSelected()){
								name += " top "+j;
								if(j != Integer.parseInt(topXValue.getText())){
									confirmation=JOptionPane.showConfirmDialog(null, "More than the specified number of weights have been selected.\nSelection Model will have "+j+" features.\nDo you want to continue?","Continue?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
								}
							}
							if(confirmation==0){
								int[] indices = new int[j];
								double[] newWeights = new double[j];
								String[] newWaveL = new String[j];

								int k=0;  // Iterator
								for(int i=0; i<weights.length; i++){
									if(weights[i] >= thresholdD){
										indices[k] = i;
										newWeights[k] = weights[i];
										newWaveL[k] = waveL[i];
										k++;
									}
								}

								FeatureSelectionModel fsModel = new FeatureSelectionModel("FeatureSelectionModel_"+model.getName()+name, model.getDatensatz(), new Date(), indices, newWeights, newWaveL);
								getModels().add(fsModel);
								actualiseListen();
								featureSelectionFrame.dispose();
							}
						}catch(Exception rg){
							rg.printStackTrace();
						}
					}
				});

				addComponent(featureSelectionFrame, gblFS, topX				, 0, 0, 2, 1, 0, 0);
				addComponent(featureSelectionFrame, gblFS, topXValue		, 2, 0, 1, 1, 1, 0);
				addComponent(featureSelectionFrame, gblFS, threshold		, 0, 1, 2, 1, 0, 0);
				addComponent(featureSelectionFrame, gblFS, thresholdValue	, 2, 1, 1, 1, 1, 0);
				addComponent(featureSelectionFrame, gblFS, new JLabel(" ")	, 0, 2, 2, 1, 1, 0);
				addComponent(featureSelectionFrame, gblFS, additionalFeatures, 0, 3, 1, 1, 0, 0);
				addComponent(featureSelectionFrame, gblFS, fsEnter			, 2, 3, 1, 1, 1, 0);

				featureSelectionFrame.setVisible(true);
			}
		});

		return selModelBtn;
	}




	public JButton getCopy() {

		copy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

//				int[] selIndices = getDatenListe().getSelectedIndices();
				ArrayList<View> views = getSelectedViews();
				System.out.println();
				if (views.size() > 0){
					
					for(int i = 0; i<views.size(); i++){
						View selView = views.get(i);
						CloneView clone = new CloneView(selView);
						if(!isselectedViewTrainView){
							master.getProject().getViewList().add(clone);
						}else{
//							getTrainViews().add(clone);
							master.getProject().getTViewList().add(new TrainView(clone));
						}
					}
					actualiseListen();
				}


				//				int selIndex = getDatenListe().getSelectedIndex();
				//
				//				if (selIndex != -1){
				//
				//					View selView = getViews().get(selIndex);
				//					CloneView clone = new CloneView(selView);
				//					getViews().add(clone);
				//					actualiseListen();
				//				}

			}
		});

		return copy;
	}


	public JButton getEditDescription() {

		editDescription.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				view = getSelectedView();
				if (view != null) {
					dialogEditDescription = new JDialog(master,true);
					dialogEditDescription.setTitle("Edit View Description");
					dialogEditDescription.setSize(300, 400);
					dialogEditDescription.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width-dialogEditDescription.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height-dialogEditDescription.getSize().height) / 2 +50		);
					GridBagLayout gbl = new GridBagLayout();
					dialogEditDescription.setLayout(gbl);
					
					newDescription = new JTextArea();
					newDescription.setText(view.getViewDescription());
					
					JScrollPane scrollDescription = new JScrollPane(newDescription);
					
					saveDescription = new JButton("Save");
					cancleDescription = new JButton("Cancle");;
					
					addComponent(dialogEditDescription, gbl, scrollDescription, 0, 0, 3, 1, 1, 1);
					addComponent(dialogEditDescription, gbl, new JLabel(" "), 0, 1, 1, 1, 1, 0);
					addComponent(dialogEditDescription, gbl, saveDescription, 1, 1, 1, 1, 0, 0);
					addComponent(dialogEditDescription, gbl, cancleDescription, 2, 1, 1, 1, 0, 0);
					
					saveDescription.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							if(!newDescription.getText().equals("")){
//								String[] des = newDescription.getText().split(System.lineSeparator());
								view.setViewDescription(newDescription.getText());
								actualiseMeta();
								view.setSaved(false);
							} else {
								int i = JOptionPane.showConfirmDialog(dialogEditDescription, "Do you really want to delete the description?");
								if ( i == JOptionPane.YES_OPTION ) {
									view.setViewDescription("");
									actualiseMeta();
									view.setSaved(false);
								} else {
									// Do nothing
								}
							}
							dialogEditDescription.dispose();
						}
					});
					
					cancleDescription.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							dialogEditDescription.dispose();
						}
					});
					
					dialogEditDescription.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(dialogEditDescription, "Please select a view first", "No view selected", JOptionPane.ERROR_MESSAGE, null);
				}
			}
		});

		return editDescription;
	}

	public JButton getAddClass() {

		addClass.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				final String s = JOptionPane.showInputDialog(labeling, "Insert a new class name" );
				if(s!=null){
					if(!labels.containsValue(s) && !s.trim().isEmpty() ){
						final JColorChooser colorChooser = new JColorChooser();
						JLabel previewLabel = new JLabel("selected Color", JLabel.CENTER);
						previewLabel.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 48));
						previewLabel.setSize(previewLabel.getPreferredSize());
						previewLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
						colorChooser.setPreviewPanel(previewLabel);

						ActionListener okActionListener = new ActionListener() {
							public void actionPerformed(ActionEvent actionEvent) {
								Color classColor = colorChooser.getColor();
								//TODO
								//									classBox.addItem(new JLabel(s, ));
								//									classBox.setSelectedItem(s);
								Set<Integer> keys = labels.keySet();
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
//								System.out.println(label);
								labels.put(label, s+" #"+Integer.toHexString(classColor.getRGB()).substring(2));
								//								master.getProject().get(getDatenListe().getSelectedIndex()).getLabelMap().put(label, s+" #"+Integer.toHexString(classColor.getRGB()).substring(2));
								//											System.out.println(master.getProject().get(getDatenListe().getSelectedIndex()).getLabelMap());


								actualiseMeta();

								actualiseClassBox();
							}
						};

						ActionListener cancelActionListener = new ActionListener() {
							public void actionPerformed(ActionEvent actionEvent) {
							}
						};

						final JDialog dialog = JColorChooser.createDialog(labeling, "Choose Color", true,colorChooser, okActionListener, cancelActionListener);

						dialog.setVisible(true);


					}
					else{
						if(s.trim().isEmpty())
							JOptionPane.showMessageDialog(getAddClass(), "Please enter a name");
						else
							JOptionPane.showMessageDialog(getAddClass(), "Class exists");
					}
				}


			}
		});

		return addClass;
	}



	public JList<Vector<Interval>> getIntervalList() {
		if (intervalList==null){
			intervalList = new JList<Vector<Interval>>();
		}
		return intervalList;
	}
	

	public JList<JRadioButton> getRadioButtonList2() {
		if (radioButtonList2 == null){
			radioButtonList2 = new JList<JRadioButton>();
		}
		radioButtonList2.setToolTipText("Set as active TrainView");
		radioButtonList2.setCellRenderer(new CheckListRenderer());
		radioButtonList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		radioButtonList2.setFixedCellHeight(42);
		radioButtonList2.setFixedCellWidth(20);

		radioButtonList2.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent event){
				JList<?> list = (JList<?>) event.getSource();
				int index = list.locationToIndex(event.getPoint());

				JRadioButton item2 = (JRadioButton)list.getModel().getElementAt(index);

				item2.setSelected(true); 
				master.getProject().setTrainView(index);
//				master.setTitle("HSVap   -   Active View:  " + master.getProject().getTrainView().getViewName());	
				list.repaint(list.getCellBounds(index, index));

				radioButtonList2.revalidate();
				radioButtonList2.repaint();
			}
		});  
		return radioButtonList2;
	}


	public DoubleMatrix generateIndex(DoubleMatrix coordinates){
		HashMap<Point,Integer> index;
		DoubleMatrix indizes = DoubleMatrix.ones(coordinates.getRows());
		if (coordinates!=null){
			index=new HashMap<Point,Integer>((int) ( coordinates.rows*(1/0.75)));
			for (int i = 0; i< coordinates.rows;i++){
				if(indizes.get(i)==1)
					index.put(new Point((int) coordinates.get(i,0), (int) coordinates.get(i,1)),i);
			}

		}
		return indizes;
	}

	/**
	 * This method actualises the text of the meta-panel for the selected View
	 * @param selIndex Selected Index of the Viewlist
	 * @author modified by schmitter
	 */
	public void actualiseMeta() {

		View vv = getSelectedView();
		if(vv!=null){
			//		View vv = getViews().get(selIndex);
			List<View> parents = vv.getParentViews();
			String s = "";
			if (parents.isEmpty()){
				s = "none";
			}else{
				for (View v : parents){
					s=s+"<br>"+v.getViewName();
				}
			}
			int nLab;

			if(FeatureSelection.getLabel(vv)==-1){
				nLab = 0;
			}else{
				nLab = 1;
			}

			int nRGB;

			if(FeatureSelection.getRGB(vv)==-1){
				nRGB = 0;
			}else{
				nRGB = 1;
			}

			int nFea = FeatureSelection.getNonSpecialFeatures(vv).length;

			String nCoords;
			int[] coords = FeatureSelection.getCoordinates(vv);
			int ncoords = 0;
			if (coords[0] != -1) {
				ncoords++;
			}
			if (coords[1] != -1) {
				ncoords++;
			}
			nCoords = Integer.toString(ncoords);

			int numEx = vv.getNumberOfExamples();

			//Image dimensions
			try{
				vv.materializeXYDimension();
			}catch(ArrayIndexOutOfBoundsException e34){

			}
			int rows = vv.getYDimension();
			int cols = vv.getXDimension();

			String[] des = vv.getViewDescription().split("\n");
						
			String metaText= "<html>"+"Description:<br>";
			for ( String string : des ) {
				metaText += string + "<br>";
			}
			metaText += "<br><br>Image dimensions:  " +rows + " x " +cols
					+"<br><br>Number of Columns:  " + vv.getNumberOfColumns()+"<br>Number of Features:  " 
					+ nFea+"<br>Number of Coordinates:  " + nCoords +"<br>Number of Labels:  " + nLab +"<br>Number of RGB-Values:  " + nRGB +"<br><br>Number of Samples:  " 
					+ numEx+"<br><br>Parents:  " +s +"<br><br>Label-Classes:<br>";

			metaTextPane.setText(metaText);

			HashMap<Integer,String>labels = (HashMap<Integer, String>) vv.getLabelMap();

			if (labels != null && !labels.isEmpty()){
				for (Map.Entry<Integer, String> l : labels.entrySet() ){
					// Parse Hex back to Integer
					int tt = (int) Long.parseLong(l.getValue().split("#")[1], 16);
					// Bulid Color
					Color clr = new Color( tt);
					int clrInt = clr.getRGB();
					// Build previewimage for Labels
					int[] clrImage = {	clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
							clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt};
					
					JLabel akku = new JLabel(l.getValue().split("#")[0]);
					ImageProducer p = new MemoryImageSource( 10, 10, clrImage, 0, 10 );
					Image image = akku.createImage( p );
					ImageIcon icon = new ImageIcon(image,"class color");
					akku.setIcon(icon);;
					
					metaTextPane.insertComponent(akku);
					try {
						metaTextPane.getDocument().insertString(metaTextPane.getDocument().getLength(), "\n", new SimpleAttributeSet());
					} catch (BadLocationException e) {
					}
				}
			}else{
				metaTextPane.setText(metaText + "\nunlabeled");
			}

		}else{
			metaTextPane.setText("");
		}
	}

	/**
	 * 
	 * @param detailed Boolean whether the model-meta-information should be detailed (e.g. with all weights displayed) or short
	 */
	public void actModelMeta(boolean detailed) {


		TransformationModel model = (TransformationModel) getModels().get(getModelListe().getSelectedIndex());

		String meta = ((ModelInterface)model).getMetaInfo(detailed); 

		char updown;
		if (detailed == false){
			updown = '\u2193';   //runter
		}else{
			updown = '\u2191';
		}

		txtPane.setText(meta+"<p>"+"<html>\n" +
				"<body>\n" +
				"<a href=#>\n"+ updown +" details</a>" +
				"</body>\n" +
				"</html>");

		if(model.getClass()==FeatureWeightsModel.class){
			txtPane.setText(meta+"<p>"+"<html>\n" +
					"<body>\n" +
					"<a href=#>\n"+ updown +" details</a><br><p>" +
					"</body>\n" +
					"</html>");
			selModelBtn = new JButton("create Feature-Selection-Model");
			getSelModelBtn();
			txtPane.insertComponent(selModelBtn);

		}else{
			txtPane.setText(meta+"<p>"+"<html>\n" +
					"<body>\n" +
					"<a href=#>\n"+ updown +" details</a>" +
					"</body>\n" +
					"</html>");
		}
	}
	
	/**
	 * ActionListener for the RadioButtons
	 * Loads selected data
	 * @author schmitter
	 */
	public ActionListener buttonActionListener = new ActionListener() {
	      public void actionPerformed(ActionEvent actionEvent) {     
	        int selected = Integer.parseInt( actionEvent.getActionCommand());
	        // set active view
			master.getProject().setActiveView(selected);
			// Query if data contains label
			if( master.getProject().getActiveView().getLabelMap().isEmpty()){
				master.getAlPanel().getClusterButton().setEnabled(true);
				master.getAlPanel().revalidate();
				master.getAlPanel().repaint();
			}
			// Load Data 
			 master.getAlPanel().resetData( master.getProject().getActiveView());
			 // Estimate RGB
			 if (FeatureSelection.containsCoordinates( master.getProject().getActiveView())) {
				 master.getAlPanel().getPaintPanel().displayRGB( master.getProject().getActiveView());
			 }
			// Setting the title of the frame
			master.setTitle("HSVap   -   Active View:  " + master.getProject().getActiveView().getViewName());	
			datenListenPanel.repaint();
	      }
	    };
	        
	    
	/**
	 * Method to actualise the GUI-list (JList) with the elements of the ArrayList (Project) stored in the MainGui-class
	 * @author modified by schmitter
	 */
	public void actualiseListen() {
		// actualise Data Panel
		int actViewInd = master.getProject().getActiveViewIndex();
		if(actViewInd!=-1){
			master.getProject().setActiveView(actViewInd);
			// Query if data contains label
			if( master.getProject().getActiveView().getLabelMap().isEmpty()){
				master.getAlPanel().getClusterButton().setEnabled(true);
				master.getAlPanel().revalidate();
				master.getAlPanel().repaint();
			}
			master.setTitle("HSVap   -   Active View:  " + master.getProject().getActiveView().getViewName());
		}
		
		// Clear and reset current Panel
		this.datenListenPanel.removeAll();
		this.datenListenPanel.setLayout( new BoxLayout(this.datenListenPanel, BoxLayout.Y_AXIS));
		this.datenListenPanel.add( Box.createRigidArea(new Dimension(0, 15)));
		// Adding radiobuttons
		ButtonGroup bgRadio = new ButtonGroup();
		for (int i = 0; i<master.getProject().getViewList().size(); i++){
			JRadioButton button = new JRadioButton();
			button.setBackground( Color.WHITE);
			button.addActionListener( buttonActionListener);
			button.setActionCommand( String.valueOf(i));
			this.datenListenPanel.add(button);
			this.datenListenPanel.add( Box.createRigidArea(new Dimension(0, 20)));
			bgRadio.add(button);
		}
		// Fill Daten List
		Vector fl = getFiles();
		getDatenListe().setListData(fl);
		
		// Fill Train List
		radioButtonListTrainViews = new Vector<JRadioButton>();
		ButtonGroup bgRadio2 = new ButtonGroup();
		for (int i = 0; i<master.getProject().getTViewList().size(); i++){
			JRadioButton button = new JRadioButton();
			radioButtonListTrainViews.add(button);
			bgRadio2.add(button);
		}
		getRadioButtonList2().setListData(radioButtonListTrainViews);
		Vector flTV = getFilesTV();
		getTrainViewListe().setListData(flTV);
		// actualise Trainview Panel
		int actViewTVInd = master.getProject().getTrainViewIndex();
		if(actViewTVInd!=-1){
			master.getProject().setTrainView(actViewTVInd);
			radioButtonListTrainViews.get(actViewTVInd).setSelected(true);
		}
		
		// Fill Model Panel
		Vector vec = new Vector<TransformationModel>();
		for(TransformationModel t: getModels()){
			vec.add(t);
		}
		getModelListe().setListData(vec);
		
		statusbar3.setText(Project.DF.format(master.getProject().getAenderungsdatum())+"     ");
		statusbar4.setText(master.getProject().getDescription());
		statusbar2.setText(Project.DF.format(master.getProject().getDate())+"     ");
		this.datenListenPanel.repaint();
	}

	/**
	 * Indicates whether two or more selected view are vertically/horizontally mergeable
	 * Adds the text to the metaTextPane
	 */
	public void generateMergeInfo(){
		applicableMergeF = true;
		applicableMergeS = true;

		ArrayList<View> viewListe = getSelectedViews();
		
		View firstSelView = viewListe.get(0);
//		if(orderOfSelected.get(0)<1000){
//			firstSelView = master.getProject().get(orderOfSelected.get(0));
//		}else{
//			firstSelView = master.getProject().getTViewList().get(orderOfSelected.get(0)-1000);
//		}
		int firstViewNOC = firstSelView.getNumberOfColumns();
		int firstViewNOS = firstSelView.getNumberOfExamples();
		int indexLbl = FeatureSelection.getLabel(firstSelView);
		int indexY = FeatureSelection.getYCoordinate(firstSelView);
		int indexX = FeatureSelection.getXCoordinate(firstSelView);
		String text = "Order of selected Views:<br>"+firstSelView.getViewName();
		
		ArrayList<Integer> orderOfSelected;
		if(isselectedViewTrainView){
			orderOfSelected=orderOfSelectedTV;		
		}
		else{
			orderOfSelected=orderOfSelectedViews;
		}	
		for(int i=1; i<orderOfSelected.size();i++){
			View v = viewListe.get(i);
//			if(orderOfSelected.get(i)<1000){
//				v =  master.getProject().get(orderOfSelected.get(i));
//			}else{
//				v =  master.getProject().getTViewList().get(orderOfSelected.get(i)-1000);
//			}
			if(v.getNumberOfColumns() != firstViewNOC){
				applicableMergeS = false;
			}
			if(v.getNumberOfExamples() != firstViewNOS){
				applicableMergeF = false;
			}else{
				if(FeatureSelection.getLabel(v) != indexLbl || FeatureSelection.getXCoordinate(v) != indexX || FeatureSelection.getYCoordinate(v) != indexY){
					applicableMergeS = false;
				}
			}
			firstViewNOC = v.getNumberOfColumns();
			text+= "<br>"+ v.getViewName();
		}
		if(applicableMergeF){
			text += "<br><br>Features can be merged.";
		}else{
			text += "<br><br>Features cannot be merged.";
		}
		if(applicableMergeS){
			text += "<br>Examples can be merged.";
		}else{
			text += "<br>Examples cannot be merged.";
		}
		metaTextPane.setText(text);
	}


	/**
	 * The class box is the Drop-down Combobox in the Manual Labeling frame, which will be actualised with the views label classes
	 */
	public void actualiseClassBox(){

		classBox.removeAllItems();

		if(!labels.isEmpty()){
			for(Map.Entry<Integer, String> l: labels.entrySet()){
				int clrInt = Color.decode(l.getValue().substring(l.getValue().length()-7, l.getValue().length())).getRGB();
				int[] clrImage = {	clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt,clrInt, clrInt,clrInt,clrInt,clrInt,
						clrInt, clrInt,clrInt,clrInt,clrInt, clrInt, clrInt,clrInt,clrInt,clrInt};
				JLabel akku = new JLabel(l.getValue().substring(0, l.getValue().length()-8).toString());
				ImageProducer p = new MemoryImageSource( 10, 10, clrImage, 0, 10 );
				Image image = akku.createImage( p );
				ImageIcon icon = new ImageIcon(image,"class color");
				akku.setIcon(icon);
				classBox.addItem(akku);
			}
		}else{
			classBox.addItem(new JLabel(" "));
		}
		classBox.repaint();
	}


	public void emptyJoinFields(){
		nameOfNewView2.setText("");
	}

	public void emptySplitFields(){
		txt1.setText("");
		txt2.setText("");
		txt3.setText("");
		txt4.setText("");
		nameOfNewView.setText("");
	}
	/**
	 * Creates the Entries in the View-list
	 * For some reasons JList only accepts Views in a Vector of JLabels 
	 * The list's properties (SelectionListeners) are defined here 
	 * 
	 * @return List of Models with Listeners set
	 */
	public JList<Vector<JLabel>> getTrainViewListe() {
		if(trainViewList==null){
			trainViewList = new JList<Vector<JLabel>>();
			trainViewList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			// CellRenderer to display icon and insert unsaved star "*"
			trainViewList.setCellRenderer(new IconRenderer());
			trainViewList.setName("TVL");

			trainViewList.addMouseListener(new MouseAdapter(){

				public void mousePressed(MouseEvent event){
					//				event.get
					JList<?> list = (JList<?>) event.getSource();
					//				System.out.println(list.getName());
					if(list.locationToIndex(event.getPoint())!=-1){
						int index = list.locationToIndex(event.getPoint());
						isselectedViewTrainView=true;
						selectLists(index);
					}
				}
			});  
		}
		return trainViewList;
	}
	
	

	private void selectLists(int index) {
		metaTabbedPane.setSelectedIndex(0);
		if (!MainGui.ctrl){
			
			orderOfSelectedTV.clear();
			orderOfSelectedViews.clear();											
		}
		
		if(isselectedViewTrainView){				
			if(!orderOfSelectedTV.contains(index)){
				//selectionTrainViewliste.add(master.getProject().getTViewList().get(index));
				orderOfSelectedTV.add(index);
			}else{
				//selectionTrainViewliste.remove(index);
				orderOfSelectedTV.remove(index);
			}
		}else{
			
			if(!orderOfSelectedViews.contains(index)){
				//selectionDatenliste.add(master.getProject().getViewList().get(index));
				orderOfSelectedViews.add(index);
			}else{

				//selectionDatenliste.remove(index);
				orderOfSelectedViews.remove(index);
			}
		}
		
		//		if(String.valueOf(index).length()>=4){
		//			int indTV = index-1000;
		//			if(selIndTV.contains(indTV)){
		//				selIndTV.remove(indTV);
		//			}
		//		}
		int[] selDL = new int[orderOfSelectedViews.size()];
		for(int i=0; i< orderOfSelectedViews.size();i++){
			selDL[i] = orderOfSelectedViews.get(i);
		}
		datenListe.setSelectedIndices(selDL);
		int[] selTVL = new int[orderOfSelectedTV.size()];
		for(int i=0; i< orderOfSelectedTV.size();i++){
			selTVL[i] = orderOfSelectedTV.get(i);
		}
		trainViewList.setSelectedIndices(selTVL);

		metaTextPane.setText("loading...");
		ArrayList<Integer> orderOfSelected;
		if(isselectedViewTrainView){
			orderOfSelected=orderOfSelectedTV;		
		}
		else{
			orderOfSelected=orderOfSelectedViews;
		}	
		if(orderOfSelected.size()==1){
			preview.setEnabled(true);
			master.getSaveMI().setEnabled(true);
			generateThumbnail.setEnabled(true);
			rename.setEnabled(true);
			setIcon.setEnabled(true);
			normBtn.setEnabled(true);
			splitBtn.setEnabled(true);
			changeFeatureRoles.setEnabled(true);
			labelingBtn.setEnabled(true);
			delete.setEnabled(true);
			joinBtn.setEnabled(false);
			if(!isselectedViewTrainView && getSelectedView()!=null &&FeatureSelection.getLabel(getSelectedView())!=-1){
				createTrainViewBtn.setEnabled(true);
			}else{
				createTrainViewBtn.setEnabled(false);
			}

			actualiseMeta();

			if(!selectedViewIsTV() && !datenListe.isSelectionEmpty()){
				if (getIntervals().isEmpty()){
					labeled = new int[master.getProject().getViewList().get(datenListe.getSelectedIndex()).getNumberOfExamples()];
				}
			}
		}else if(orderOfSelected.size()>1){
			preview.setEnabled(false);
			//						saveMaterialized.setEnabled(false);
			master.getSaveMI().setEnabled(true);
			//						save.setEnabled(false);
			generateThumbnail.setEnabled(false);
			rename.setEnabled(false);
			setIcon.setEnabled(false);
			labelingBtn.setEnabled(false);
			createTrainViewBtn.setEnabled(false);
			normBtn.setEnabled(false);
			splitBtn.setEnabled(false);
			changeFeatureRoles.setEnabled(false);
			joinBtn.setEnabled(true);
			delete.setEnabled(false);
			generateMergeInfo();
		}else{
			metaTextPane.setText("");
		}

	}

	/**
	 * Creates the Entries in the View-list
	 * For some reasons JList only accepts Views in a Vector of JLabels 
	 * The list's properties (SelectionListeners) are defined here 
	 * 
	 * @return List of Models with Listeners set
	 */
	public JList<Vector<JLabel>> getDatenListe() {
		if(datenListe==null){
			datenListe = new JList<Vector<JLabel>>();
			datenListe.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			// CellRenderer to display icon and insert unsaved star "*"
			datenListe.setCellRenderer(new IconRenderer());

			datenListe.setName("DL");

			datenListe.addMouseListener(new MouseAdapter(){

				public void mousePressed(MouseEvent event){
					JList<?> list = (JList<?>) event.getSource();
					//				System.out.println(list.getName());
					int index = list.locationToIndex(event.getPoint());
					isselectedViewTrainView=false;
					selectLists(index);
				}
			}); 
		}
		return datenListe;
	}

	/**
	 * @return List of Models with Listeners set 
	 */
	public JList<TransformationModel> getModelListe() {

		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		modelList.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged( ListSelectionEvent e ) {
				try {

					if (e.getValueIsAdjusting() == false){
						actModelMeta(false);
					}

				}catch (Exception e1){
					txtPane.setText("");
				}
			}
		}); 

		modelList.addMouseListener(new MouseAdapter(){

			public void mouseClicked(MouseEvent event){
				metaTabbedPane.setSelectedIndex(1);


			}
		});

		//		modelList.setCellRenderer(new IconRenderer3());

		return modelList;
	}

	/**
	 * Creates a Icon and text for each view. If no image specified, use standard plant thumbnail
	 * @return Vector with JLabels including name and Icon of the View 
	 */
	public Vector<JLabel> getFiles() {
		displayViews = new Vector<JLabel>();
		for (int i=0; i<master.getProject().getViewList().size(); i++){
			JLabel akku = new JLabel(master.getProject().getViewList().get(i).getViewName());
			//			ImageProducer p = new MemoryImageSource( 40, 40, imagePlant, 0, 40 );
			Image image = akku.createImage( p );

			//			Image image = generateThumbnail(getViews().get(i));
			ImageIcon icon = new ImageIcon(image,"Pflanze");


			if (master.getProject().getViewList().get(i).getThumbnail() != null) {
				icon = new ImageIcon(master.getProject().getViewList().get(i).getThumbnail());
			}else{
				master.getProject().getViewList().get(i).setThumbnail(image);
			}
			akku.setIcon(icon);
			displayViews.add(akku);
		}
		metaTextPane.setText("");
		return displayViews;
	}

	/**
	 * Creates a Icon and text for each view. If no image specified, use standard plant thumbnail
	 * @return Vector with JLabels including name and Icon of the View 
	 */
	public Vector<JLabel> getFilesTV() {
		displayViews2 = new Vector<JLabel>();
		for (int i=0; i<master.getProject().getTViewList().size(); i++){
			JLabel akku = new JLabel(master.getProject().getTViewList().get(i).getViewName());
			//			ImageProducer p = new MemoryImageSource( 40, 40, imagePlant, 0, 40 );
			Image image = akku.createImage( p );

			//			Image image = generateThumbnail(getViews().get(i));
			ImageIcon icon = new ImageIcon(image,"Pflanze");


			if (master.getProject().getTViewList().get(i).getThumbnail() != null) {
				icon = new ImageIcon(master.getProject().getTViewList().get(i).getThumbnail());
			}else{
				master.getProject().getTViewList().get(i).setThumbnail(image);
			}
			akku.setIcon(icon);
			displayViews2.add(akku);
		}
		metaTextPane.setText("");
		return displayViews2;
	}

	/**
	 * 
	 * @return List of loaded models
	 */
	public ArrayList<TransformationModel> getModels() {
		return master.getProject().getModels();
	}

	/**
	 * gets the Intervals for the manual Labeling window
	 * 
	 * @return 
	 */
	public ArrayList<Interval> getIntervals() {
		if (intervals == null){
			intervals = new ArrayList<Interval>();
		} 

		return intervals;
	}

	/**
	 * 
	 * @return Vector of Intervals to be written in JList
	 */
	@SuppressWarnings("rawtypes")
	public Vector getIntervalsAsVector(){
		Vector<Interval> vec = new Vector<Interval>();
		for (Interval i: getIntervals()){
			vec.add(i);
		}
		return vec;	
	}

	// TODO update from ImagePanel version
	/**
	 * Takes the RGB-value column and produces an image of it 
	 * 
	 * @param v View
	 * @return A max 40x40px thumbnail of the image data
	 */
	public Image generateThumbnail(View v) {

		// Are rgb values NOT available


		//Calculate rbg values
		double red = 680;
		double green = 540;
		double blue = 435;
		double MaxValue = Double.MIN_VALUE;
		double MinValue = Double.MAX_VALUE;
		String [] waveString = v.getFeatureDescriptors();
		int [] featureIdx = FeatureSelection.getNonSpecialFeatures(v);
		int[] Ind = new int[]{featureIdx[0],featureIdx[0],featureIdx[0]};

		// Finding the nearest bands to rgb
		for (int k = 1; k < featureIdx.length; k++) {
			if (Math.abs(red - Double.parseDouble(waveString[featureIdx[k]])) < Math.abs(red
					- Double.parseDouble(waveString[Ind[0]])))
				Ind[0] = featureIdx[k];

			if (Math.abs(green - Double.parseDouble(waveString[featureIdx[k]])) < Math.abs(green
					- Double.parseDouble(waveString[Ind[1]])))
				Ind[1] = featureIdx[k];

			if (Math.abs(blue - Double.parseDouble(waveString[featureIdx[k]])) < Math.abs(blue
					- Double.parseDouble(waveString[Ind[2]])))
				Ind[2] = featureIdx[k];
		}

		double[] dataRed = v.materializeFeature(Ind[0]);
		double[] dataGreen = v.materializeFeature(Ind[1]);
		double[] dataBlue = v.materializeFeature(Ind[2]);

		// If no information about the wavelength is available
		if(Ind[0] == Ind[1] &  Ind[1] == Ind[2]){
			System.out.println("no rgb image determinable, please add wavelengths");
			Ind[1] =  Integer.parseInt( waveString[ waveString.length-1]);
			Ind[2] =  Integer.parseInt( waveString[ waveString.length/2]);
		}


		// Finding max and min value for scaling
		for (int i = 0; i < dataRed.length; i++) {
			if (MaxValue < dataRed[i] )
				MaxValue = dataRed[i];
			if (MinValue > dataRed[i] )
				MinValue = dataRed[i];
			if (MaxValue < dataBlue[i] )
				MaxValue = dataBlue[i];
			if (MinValue > dataBlue[i] )
				MinValue = dataBlue[i];
			if (MaxValue < dataGreen[i] )
				MaxValue = dataGreen[i];
			if (MinValue > dataGreen[i] )
				MinValue = dataGreen[i];
		}

		//Create Image
		double[] dataRGB = new double[dataRed.length];
		for (int i = 0; i < dataRGB.length; i++) {
			// Scaling
			dataRed[i] = (dataRed[i]-MinValue) / (MaxValue-MinValue) * 255;
			dataGreen[i] = (dataGreen[i]-MinValue) / (MaxValue-MinValue) * 255;
			dataBlue[i] = (dataBlue[i]-MinValue) / (MaxValue-MinValue) * 255;

		}

		//Display the rgb values of the current view
		double[] xCoord = v.materializeFeature(FeatureSelection.getXCoordinate(v));
		double[] yCoord = v.materializeFeature(FeatureSelection.getYCoordinate(v));
		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;

		//Calculating the image size
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

		// Size of the image
		int[] rgbArray = new int[v.getXDimension()* v.getYDimension()];
		// Build the image
		for(int i=0; i<xCoord.length; i++){
			// Fill array with colors, where data exists
			rgbArray[ ((int)yCoord[i]-1) * v.getXDimension() + (int)xCoord[i]-1] = new Color((int)dataRed[i], (int)dataGreen[i], (int)dataBlue[i]).getRGB();
		}
		// add white to the remaining data
		for (int i=0; i<rgbArray.length; i++){
			if (rgbArray[i] == 0)
				rgbArray[i] = Color.WHITE.getRGB();
		}

		//Displaying the image
		MemoryImageSource source = new MemoryImageSource((int) xMax,
				(int) yMax, rgbArray, 0, (int) xMax);
		Image img = createImage(source);

		ImageIcon im = new ImageIcon(img);
		ImageIcon scaled = null;
		if (im != null) {
			if (im.getIconHeight() != 40) {
				scaled = new ImageIcon(im.getImage().getScaledInstance(-1, 40,Image.SCALE_DEFAULT));
			} else { //no need to miniaturize
				scaled = im;
			}
		}

		return scaled.getImage();
	}

	/**
	 * 
	 * @param cont Container, to which the component should be added.
	 * @param gbl For each Component needs to be defined a seperate GridBagLayout.
	 * @param c The Component to add to the Container
	 * @param x position
	 * @param y position
	 * @param width
	 * @param height
	 * @param weightx   1 if component should be as wide as possible, 0 if as narrow/small as possible
	 * @param weighty   1 if component should be as high as possible, 0 if as narrow/small as possible
	 */
	static void addComponent( Container cont,
			GridBagLayout gbl,
			Component c,
			int x, int y,
			int width, int height,
			double weightx, double weighty )
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x; gbc.gridy = y;
		gbc.gridwidth = width; gbc.gridheight = height;
		gbc.weightx = weightx; gbc.weighty = weighty;
		gbl.setConstraints( c, gbc );
		cont.add( c );
	}

	/**
	 * This class defines how the List cells of the view list (Datenliste) are built (with Icons)
	 * The unsaved star ("*") is inserted here.
	 * 
	 * @author Till
	 *
	 */
	class IconRenderer extends DefaultListCellRenderer{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) 
		{

			if(value instanceof JLabel)
			{
				if(master.getProject().getViewList().size() >0 &&master.getProject().getViewList().size() <index &&master.getProject().getViewList().get(index).isSaved()){
					this.setText(((JLabel)value).getText());
				}else{
					this.setText("* "+((JLabel)value).getText());
				}
				this.setIcon(((JLabel)value).getIcon());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}

	/**
	 * List Cell renderer to show icon for each list element in classbox
	 * 
	 * @author Till
	 *
	 */
	class IconRenderer2 extends DefaultListCellRenderer{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) 
		{

			if(value instanceof JLabel)
			{
				this.setText(((JLabel)value).getText());
				this.setIcon(((JLabel)value).getIcon());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			return this;
		}
	}

	/**
	 * Icon renderer only displays text
	 * @author Till
	 *
	 */
	class NonIconRenderer extends DefaultListCellRenderer{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) 
		{

			if(value instanceof JLabel)
			{
				this.setText(((JLabel)value).getText());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}


			return this;
		}
	}

/**
 * 
 * @return the selected View of ViewList or TrainViewList, null: if more than one view is selected
 */
	protected View getSelectedView() {
		ArrayList<Integer> orderOfSelected;
		if(isselectedViewTrainView){
			orderOfSelected=orderOfSelectedTV;		
		}
		else{
			orderOfSelected=orderOfSelectedViews;
		}	
		if(orderOfSelected.size()<=1 && orderOfSelected.get(0)!=-1){
			if(!isselectedViewTrainView ){
				return master.getProject().getViewList().get(orderOfSelected.get(0));
			}else {
				return master.getProject().getTViewList().get(orderOfSelected.get(0));
			}
		}else{
			return null;
		}
	}
	
	/**
	 * 
	 * @return true: if the View returned by the mothod getSelectedView() is from the TrainViewList
	 */
	public boolean selectedViewIsTV() {
		if(isselectedViewTrainView && orderOfSelectedTV.size()<=1){
			return true; 
		}
		else{
			return false;
		}				
	}

	/**
	 * 
	 * @return all selected Views of ViewList and TrainViewList
	 */
	protected ArrayList<View> getSelectedViews() {
		ArrayList<Integer> orderOfSelected;		
		if(isselectedViewTrainView){
			orderOfSelected=orderOfSelectedTV;				
		}
		else{
			orderOfSelected=orderOfSelectedViews;			
		}	
		
		ArrayList<View> liste = new ArrayList<View>();
		for(int i=0; i<orderOfSelected.size(); i++){
				if(!isselectedViewTrainView){
					liste.add(master.getProject().getViewList().get(orderOfSelected.get(i)));
				}else {
					liste.add(master.getProject().getTViewList().get(orderOfSelected.get(i)));
				}
			}
			return liste;

	}

	protected ArrayList<Integer> getOrderOfSelected() {
				
		if(isselectedViewTrainView){
			return orderOfSelectedTV;	
		}
		else{
			return orderOfSelectedViews;			
		}	
		
		
	}
	
	/**
	 * Mouse Adaptor to define dragging behaviour of the modelListChain
	 * @author Till
	 *
	 */
	private class MyMouseAdaptor extends MouseInputAdapter {
		private boolean mouseDragging = false;
		private int dragSourceIndex;

		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				dragSourceIndex = modelListChain.getSelectedIndex();
				mouseDragging = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			mouseDragging = false;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (mouseDragging) {
				int currentIndex = modelListChain.locationToIndex(e.getPoint());
				if (currentIndex != dragSourceIndex) {
					int dragTargetIndex = modelListChain.getSelectedIndex();
					TransformationModel dragElement = modelListChainModel.get(dragSourceIndex);
					modelListChainModel.remove(dragSourceIndex);
					modelListChainModel.add(dragTargetIndex, dragElement);
					dragSourceIndex = currentIndex;
				}
			}
		}
	}

	/**
	 * Class for Interval in Manual Labeling panel
	 * @author Till
	 *
	 */
	class Interval extends Component{

		private static final long serialVersionUID = 1L;
		private int from;
		private int to;
		private int value;

		public Interval(int from, int to, int value){
			this.from = from;
			this.to = to;
			this.value = value;
		}

		public int getFrom() {
			return from;
		}
		public int getTo() {
			return to;
		}
		public int getValue() {
			return value;
		}

		@Override
		public String toString() {
			return "from " + from + " to " + to + ":   " + labels.get(value).substring(0, labels.get(value).length()-8);
		}

	}

	// Plant image data as RGB-values
	final int[] imageData2 = {16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6700903,-9934744,-1966111,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-9934744,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-9922200,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-7681,-16750592,-6710887,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-4144960,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9922200,-16750592,-1973791,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,-9934744,-16750592,-4144960,-16750592,-4136512,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16738048,-16750592,-1966111,-16750592,-16750592,-16750592,-4144960,-4144960,16777215,16777215,16777215,-16750592,-16750592,-9922200,-16750592,-16750592,-6700903,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,-16750592,-6700903,-16750592,-16750592,-16750592,16777215,16777215,-16750592,16777215,-1973791,-16750592,-16750592,-16750592,-16750592,-16750592,-9922200,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-16750592,-16750592,-16750592,16777215,-1973791,-16750592,16777215,-9922200,-16750592,-9922200,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-9922200,-16750592,16777215,16777215,-16750592,-16750592,16777215,-4136512,-16750592,-9922200,-16750592,-9922200,16777215,16777215,16777215,-1973791,-16750592,-4136512,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-9922200,-16750592,16777215,-16738048,-16750592,-4144960,16777215,16777215,-6700903,-16750592,-1973791,16777215,16777215,16777215,-6700903,-16750592,-4136512,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-4144960,16777215,-16750592,-16750592,16777215,16777215,16777215,-4144960,-1966111,16777215,16777215,16777215,16777215,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,-9934744,-9922200,-16750592,-4144960,16777215,16777215,16777215,16777215,-16738048,-1966111,-16750592,-16750592,16777215,16777215,16777215,-16750592,16777215,16777215,16777215,16777215,-1966111,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-9912216,-16750592,-16750592,-7681,16777215,16777215,-1973791,-16750592,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-16750592,-16750592,-9922200,-16750592,-16750592,16777215,16777215,-4144960,-16750592,-16750592,16777215,16777215,-1973791,16777215,16777215,16777215,16777215,-6710887,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-7681,-16750592,-16750592,-16750592,-9922200,-9922200,-16750592,16777215,16777215,-16750592,-16750592,-1966111,16777215,-9922200,16777215,16777215,16777215,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9922200,-16750592,-16750592,-16750592,-16750592,16777215,16777215,-6700903,-16750592,-4144960,16777215,-6710887,16777215,16777215,-16750592,-16750592,-4136512,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9922200,16777215,-16750592,-16750592,16777215,-9922200,16777215,-1973791,-16750592,-4144960,16777215,16777215,16777215,16777215,-4136512,-9922200,-16750592,-16750592,-16750592,-16738048,-16750592,-16750592,-16750592,-4144960,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16750592,-9934744,-16750592,-4136512,-9922200,16777215,-9922200,-16750592,16777215,16777215,-1966111,-4144960,-4136512,16777215,16777215,-1973791,-16750592,-16750592,-16750592,-16750592,-9922200,-4144960,-16750592,-1966111,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-1973791,16777215,-16750592,-9922200,-16750592,16777215,-16750592,16777215,16777215,-6700903,16777215,16777215,16777215,16777215,16777215,16777215,-1973791,-16750592,-16750592,-16750592,-16750592,-16750592,-16750592,-16750592,-9922200,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4144960,-9934744,-9961472,-16777216,-9934848,-16750592,-16750592,-4167680,-16750592,-9961472,-9961472,-4144960,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4144960,-16750592,-16750592,-16750592,-16750592,-16750592,-16750592,-16750592,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4144960,-9961472,-38912,-38912,-4167680,-16750592,-16777216,-16750592,-16750592,-16777216,-16750592,-16777216,-16750592,-4167680,-38912,-4167680,-9961472,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,-4167680,-38912,-16777216,-16777216,-16777216,-16777216,-16750592,-16750592,-16750592,-16777216,-16750592,-16777216,-16750592,-16777216,-16777216,-4167680,-38912,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4144960,-16777216,-2004992,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-16777216,-38912,-38912,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4154944,-16777216,-16777216,-9961472,-4167680,-4167680,-9961472,-9934848,-16777216,-16777216,-16777216,-9961472,-4167680,-4167680,-38912,-38912,-38912,-38912,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-4144960,-16777216,-16777216,-16777216,-16777216,-6723584,-6750208,-4167680,-4167680,-2004992,-38912,-38912,-38912,-38912,-38912,-38912,-38912,-38912,-7711,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-1973791,-16777216,-16777216,-9961472,-4194304,-9961472,-9961472,-4167680,-38912,-38912,-38912,-38912,-38912,-38912,-38912,-6723584,-16777216,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16777216,-16777216,-16777216,-9934848,-4167680,-4167680,-38912,-38912,-2004992,-4167680,-9934848,-6723584,-6723584,-2004992,-38912,-4136479,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16777216,-16777216,-16777216,-16777216,-16777216,-9961472,-16777216,-6750208,-6723584,-2004992,-2004992,-38912,-38912,-38912,-6750208,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16777216,-16777216,-16777216,-6723584,-6723584,-6750208,-6750208,-2004992,-38912,-38912,-38912,-38912,-38912,-6710887,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-16777216,-16777216,-16777216,-6723584,-6723584,-6723584,-6723584,-4194304,-38912,-38912,-38912,-38912,-26368,-1966081,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9934848,-16777216,-16777216,-16777216,-16777216,-6750208,-2004992,-2004992,-4194304,-4167680,-38912,-38912,-38912,-2004992,-16777216,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9961472,-16777216,-16777216,-16777216,-16777216,-6750208,-2004992,-2004992,-4167680,-4194304,-38912,-38912,-38912,-9961472,-16777216,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-1973791,-16777216,-2004992,-16777216,-16777216,-16777216,-16777216,-16777216,-6750208,-2004992,-38912,-2004992,-6723584,-4194304,-1966081,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-9934744,-16777216,-16777216,-38912,-2004992,-2004992,-38912,-2004992,-2004992,-6750208,-4167680,-38912,-6723584,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,-16777216,-16777216,-16777216,-16777216,-4194304,-38912,-38912,-38912,-38912,-9934848,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,-6710887,-9934744,-16777216,-9961472,-9961472,-9961472,-9934744,-9934744,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215,16777215};
	final ImageProducer p = new MemoryImageSource( 40,40, imageData2, 0, 40 );

	final int[] imageDataCrd = {-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-657671,-4012856,-855050,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-920843,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1052685,-5131079,-1315600,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1381393,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1315856,-5854545,-1579028,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1644821,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1644308,-6380888,-1907480,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-1973273,-2104858,-2104858,-2104858,-2633786,-3417623,-2039848,-3815208,-2170394,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-1710101,-6775646,-2039065,-2104858,-2104600,-3552824,-2039321,-3946034,-1973530,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2104858,-2170651,-2170651,-2170650,-2054808,-12150818,-935307,-11696697,-2170395,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-1775893,-7170404,-2104601,-2170651,-2170390,-7378837,-4853829,-10001264,-1908251,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2170651,-2301981,-2301981,-2301981,-1773628,-10142092,-7513515,-5710864,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-1907223,-7367782,-2170395,-2301981,-2301981,-1979479,-16711679,-6108439,-2367517,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2301981,-2368030,-2368030,-2368030,-2169363,-6068182,-13998425,-1645083,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-1973272,-7301989,-2236445,-2368030,-2368030,-3560316,-11581103,-8672816,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2368030,-2499615,-2499615,-2499615,-2499354,-4085367,-9067327,-2106143,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2104857,-7367525,-2368030,-2499615,-2432806,-9675940,-3283755,-10790291,-3022878,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2499615,-2630944,-2630944,-2630944,-2630425,-4807557,-4931628,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2236187,-7301732,-2499359,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2630944,-2367772,-2367772,-2367772,-2637401,-9142110,-2760986,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-1973014,-7104353,-2236443,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-2367772,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1512463,-6183251,-1775635,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-1841428,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-393985,-4933184,-657154,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-722947,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8222835,-7894127,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-8157042,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1357,-10663528,-327681,-1,-1,-21,-10157568,-16767898,-327681,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7118729,-3801089,-1,-1,-1,-2442870,-15661823,-9987889,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3301006,-13286609,-12630634,-327681,-1,-1,-5018794,-7344897,-11131,-14988632,-65537,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-662064,-7973314,-16119740,-3997697,-1,-1,-32,-9618863,-655361,-7968163,-1900545,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3930,-11781489,-327681,-1,-1,-8032128,-1769473,-42,-10480814,-1311233,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7844505,-4063233,-1,-1,-4957,-10983761,-131073,-4421519,-7415041,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3930,-11781489,-327681,-1,-1,-9344906,-1572865,-40,-10349745,-1377025,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7844505,-4063233,-1,-1,-9842,-12620877,-131073,-4290446,-7612417,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3930,-11781489,-327681,-1,-1,-5808010,-4915201,-3924,-12901500,-655361,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7844505,-4063233,-1,-1,-51,-9489021,-458753,-6329238,-4325377,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-4705,-12699001,-393217,-4550769,-3670017,-398671,-12513492,-13945263,-5511689,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-8434853,-4456449,-2107,-9678180,-393217,-5272746,-12830920,-11371103,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855052,-2346,-5528892,-986126,-855310,-855310,-393234,-5342086,-9007697,-590089,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-330009,-6389377,-8816007,-4267009,-592910,-855310,-855563,-1648447,-8159877,-5257247,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-595493,-13820152,-13678958,-1182477,-855310,-855310,-4685751,-9783300,-1069721,-14977088,-526861,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855307,-5078933,-7876623,-4420,-13433536,-2822151,-855310,-459036,-9484234,-1576195,-9934761,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-722949,-4416393,-3946348,-11716729,-1182477,-855310,-855310,-7642501,-3342602,-1088,-11594148,-1969163,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855051,-4407366,-461582,-196620,-8181736,-3610630,-855309,-134741,-10531684,-1051401,-5539733,-7086351,-527374,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855308,-72547,-12044664,-1182477,-855310,-855310,-10659225,-2426378,-131126,-10743218,-2101003,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-789522,-5408954,-11821085,-329997,-855052,-15750,-13999192,-986122,-5078933,-7876623,-461582,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855308,-72547,-12044664,-1182477,-855310,-855310,-7250566,-3670023,-2631,-12052379,-1837835,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855311,-1564,-9683680,-8149806,-264205,-855310,-855309,-198223,-10141291,-1116681,-5868694,-6560015,-527630,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855308,-72804,-12306810,-1182476,-1976361,-1772814,-3564705,-13013842,-4289187,-12872498,-527117,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-854792,-7314901,-11561517,-3159613,-3815737,-1184282,-3159589,-656150,-9351889,-4669239,-10523802,-593166,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-725022,-2961442,-920589,-2042155,-1838350,-591889,-2768962,-4471084,-788492,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855052,-2436665,-3948096,-4144960,-4078908,-1249819,-3357224,-1051660,-1186085,-4014146,-2826263,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-855310,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-921103,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-65794,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-781,-7703999,-13548401,-327681,-1,-1,-1783670,-11119997,-9216170,-9059350,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3303034,-11370368,-8891824,-11289628,-1,-1,-4,-6457527,-8553085,-10979705,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7189504,-8148599,-11191153,-327681,-1,-1,-6267267,-3801089,-1351,-12313481,-851969,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1856,-13169807,-1114113,-1,-59,-9619310,-327681,-5737875,-5178113,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3674,-11781489,-327681,-1,-1,-9870480,-1638401,-42,-10349744,-1377025,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-7512,-12840673,-9382401,-1,-1,-11641,-13276494,-131073,-4355983,-7546625,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3930,-11781489,-327681,-1,-1,-7508607,-2097153,-47,-10807976,-1245441,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-18,-9697239,-2557697,-1,-2901,-10527318,-196609,-4684687,-7085825,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3930,-11781489,-327681,-1,-1,-4491187,-11093508,-14218,-15113799,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-6912641,-983041,-17,-9565913,-2623233,-1,-25,-10472390,-983553,-8623271,-851969,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-3136,-8424529,-262145,-4682356,-3735553,-12,-7719374,-14200696,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-6460,-12961993,-13751735,-4325633,-1852,-9875815,-589825,-1783896,-12700624,-7819555,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,-394759,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	final ImageIcon iconCrd = new ImageIcon(new JLabel().createImage( new MemoryImageSource( 45,45, imageDataCrd, 0, 45 ) ),"Crd");
	/**
	 * HTML-Listener to realise detail function in model-meta-information
	 * 
	 * @author Till
	 *
	 */
	private class HTMLListener implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				if(detailed == true){
					detailed = false;
				}else{
					detailed = true;
				}
				actModelMeta(detailed);
			}
		}
	}

	/**
	 * HTMLEditorKit to realise detail function in model-meta-information
	 * 
	 * @author Till
	 *
	 */
	public class MyHTMLEditorKit extends HTMLEditorKit {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		MyLinkController handler=new MyLinkController();
		public void install(JEditorPane c) {
			MouseListener[] oldMouseListeners=c.getMouseListeners();
			MouseMotionListener[] oldMouseMotionListeners=c.getMouseMotionListeners();
			super.install(c);
			//the following code removes link handler added by original
			//HTMLEditorKit

			for (MouseListener l: c.getMouseListeners()) {
				c.removeMouseListener(l);
			}
			for (MouseListener l: oldMouseListeners) {
				c.addMouseListener(l);
			}

			for (MouseMotionListener l: c.getMouseMotionListeners()) {
				c.removeMouseMotionListener(l);
			}
			for (MouseMotionListener l: oldMouseMotionListeners) {
				c.addMouseMotionListener(l);
			}

			//add out link handler instead of removed one
			c.addMouseListener(handler);
			c.addMouseMotionListener(handler);
		}

		public class MyLinkController extends LinkController {

			private static final long serialVersionUID = 1L;
			public void mouseClicked(MouseEvent e) {

				if (SwingUtilities.isLeftMouseButton(e)) {
					super.mouseClicked(e);
				}
			}
		}
	}




	/**
	 * @return the isselectedViewTrainView
	 */
	public boolean isIsselectedViewTrainView() {
		return isselectedViewTrainView;
	}


	/**
	 * @param isselectedViewTrainView the isselectedViewTrainView to set
	 */
	public void setIsselectedViewTrainView(boolean isselectedViewTrainView) {
		this.isselectedViewTrainView = isselectedViewTrainView;
	}
}

/**
 * ListCellRenderer to realise the JList of JCheckBoxes
 * 
 * @author Till
 *
 */
class CheckListRenderer extends JRadioButton implements ListCellRenderer<Object>{

	private static final long serialVersionUID = 1L;

	public Component getListCellRendererComponent(
			JList<?> list, Object value, int index,
			boolean isSelected, boolean hasFocus){
		setEnabled(list.isEnabled());
		setSelected(((JRadioButton)value).isSelected());
		setBackground(list.getBackground());
		setForeground(list.getForeground());
		return this;
	}
}

/**
 * class to realise the meta information shown in the FileChoosers
 * 
 * @author Till
 *
 */
class MetaPreview extends JComponent implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<String> meta = null;
	File file = null;

	public MetaPreview(JFileChooser fc,boolean big) {
		if(big){
			setPreferredSize(new Dimension(400, 250));
		}else{
			setPreferredSize(new Dimension(200, 250));
		}

		fc.addPropertyChangeListener(this);
	}

	public void loadMeta() {
		if (file == null) {
			meta = null;
			return;
		}


		try{
			if(file.getName().endsWith(".model")){
				TransformationModel tr = TransformationModel.castModel(file);
				String metaString = ((ModelInterface)tr).getMetaInfo(false);
				String [] split = metaString.split("<p>");
				meta = Arrays.asList(split);


			}else if(file.getName().endsWith(".project")){
				meta = new ArrayList<String>();
				meta.add("Project-File");
				Project p = new Project(file);
				meta.add("Creation date:   "+Project.DF.format(p.getDate()));
				meta.add("Alteration date:   "+Project.DF.format(p.getAenderungsdatum()));
				meta.add("Views:");
				for(View v: p.getViewList()){
					meta.add(v.getViewName());
				}
				meta.add("Models:");
				for(TransformationModel v: p.getModels()){
					if(v!=null)
						meta.add(v.getName());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			meta = new ArrayList<String>();
			meta.add("no meta infomation");
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		boolean update = false;
		String prop = e.getPropertyName();

		//If the directory changed, don't show an image.
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			file = null;
			update = true;

			//If a file became selected, find out which one.
		} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			file = (File) e.getNewValue();
			update = true;
		}

		//Update the preview accordingly.
		if (update) {
			meta = null;
			if (isShowing()) {
				loadMeta();
				repaint();
			}
		}
	}

	protected void paintComponent(Graphics g) {
		if (meta == null) {
			loadMeta();
		}
		if (meta != null) {

			for(int i=1; i<=meta.size();i++){
				g.drawString(meta.get(i-1), 10, i*20);
			}
		}
	}
}


