package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jblas.DoubleMatrix;

import models.TransformationModel;
import data.FeatureSelection;
import data.FileFormat;
import data.Project;
import data.Utilities;
import data.View;
import data.fileformats.ENVIFormat;
import data.fileformats.FileView;
import data.fileformats.MatlabFormat;
import data.fileformats.TextFormat;
import data.inmemory.DoubleMatrixView;
import data.operators.TrainView;

public class MainGui extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final DateFormat DF = new SimpleDateFormat("yyyy_MM_dd");;

	private JTextField workspace;
//	private JTextField python;

	private JFrame licenseFrame;
	private JFrame pathConfigurationFrame;

	private JMenu fileMenu;
	private JMenu editMenu;
	private JMenu sourceMenu;

	private JMenuItem loadMI;
	private JMenuItem loadTDfromFolder;
	private JMenuItem saveMI;
	private JMenuItem saveProjectMI;
	private JMenuItem loadProjectMI;
	private JMenuItem configMI;
	private JMenuItem exitMI;

	private JMenuItem helpMI;

	private JMenuItem licenseMI;
	
	private JMenuItem pathConfigurationMI;

	private JMenuItem aboutMI;

	private JPanel startIcon;

	private JTabbedPane tabbedPane;

//	private View lastDisplayedView;

	private Properties directoryProp;

	public static boolean ctrl;

	public AlPanel getAlPanel() {
		return alPanel;
	}

	public void setAlPanel(AlPanel alPanel) {
		this.alPanel = alPanel;
	}
	
	private JMenuItem hsvapViewMI;

	private JMenuItem tabbedViewMI;

	// private JPanel alPanel;

	private ImagePanel paintPanel;

	private AlPanel alPanel;

	private DataPanel dataPanel;

	// private DataMat dataMatrix;
	// private MatrixStorage matrixStorage;
	// private MetadataStorage metaData;

	private Project project;

	private JMenuItem extraImageMI;

	private FeatureSelectionPanel featureSelection;
	private DataManagementPanel dataManagementPanel;

	private Logger log;

//	private Handler fileLogHandler;
	private Handler textAreaLogHandler;
	private JTextArea logTextArea;

	private JSplitPane splitPane;
	private JSplitPane splitPaneProjektLog;
	private JScrollPane scrollPaneLog;
	private JSplitPane scrollPaneProjekt;

	public MainGui() throws HeadlessException {
		this.setLookAndFeel();
		// super();
		this.initialize();
		this.setVisible(true);
		this.selectWorkspace();
	}

	private void initialize() {

		// Logger
		log = Logger.getLogger(MainGui.class.getName());
		/*try {
			fileLogHandler = new FileHandler("hsVap.log", true);    
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.addHandler(fileLogHandler);*/
//		consolenLogHandler = new ConsoleHandler();
//		log.addHandler(consolenLogHandler);
		textAreaLogHandler = new TextComponentHandler();
		log.addHandler(textAreaLogHandler);
		
		this.getContentPane().setLayout(new BorderLayout(1,1));
//		this.getContentPane().setLayout(new GridLayout(1, 1));
//		this.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));
//		this.getContentPane().setLayout(new MigLayout("", "[shrink]", "[shrink]"));
//		this.getContentPane().setLayout(new MigLayout("", "[::"+fullScreen.width+"]", "[::"+(fullScreen.height-taskBarSize)+"]"));
		
		Toolkit kit = this.getToolkit();
		Dimension fullScreen = kit.getScreenSize();
		Dimension size = new Dimension(
				new Double(fullScreen.width * 0.75).intValue(), new Double(
						fullScreen.height * 0.75).intValue());
		//height of the task bar
//		Insets scnMax = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
//		int taskBarSize = scnMax.bottom;
//		System.out.println(fullScreen.toString()+" "+taskBarSize);
		this.setPreferredSize(size);
		this.setSize(size);
		this.setMaximumSize(new Dimension(fullScreen.height,fullScreen.width));
		this.setLocation(
				(Toolkit.getDefaultToolkit().getScreenSize().width - getSize().width) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - getSize().height) / 2);
		// this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.setJMenuBar(getJMBar());
		this.setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

		this.setTitle("HSVap");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(
				"logos/prisma2.jpg"));
		// this.setContentPane(getStartIcon());
		// this.setContentPane(getTabbedPane());
		
		this.getContentPane().add(getSplitPane());
//		this.getContentPane().add(getSplitPane(), "cell 0 0,grow");
		// getTabbedPane();
		getStartIcon();

		KeyboardFocusManager manager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new MyDispatcher());

		// System.out.println("initialisierung abgeschlossen");
		log.info("Initialization successful");
	}
	
	private void selectWorkspace() {
		if (getDirectoryProp().get("DATA_PATH").equals("")) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Select a workspace, where working memory files will be saved.");
			fc.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return "workspace directory";
				}

			});
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);
			int h = fc.showSaveDialog(this);
			if (h == JFileChooser.APPROVE_OPTION) {
				getDirectoryProp().setProperty("DATA_PATH",
						fc.getSelectedFile().getAbsolutePath());
				getDirectoryProp().setProperty(
						"LAST_PROJECT",
						fc.getSelectedFile().getAbsolutePath() + File.separator
								+ DF.format(new Date()) + ".project");
				
				if (!new File (getDirectoryProp().getProperty("LAST_PROJECT")).exists()){
					File tmp = new File(fc.getSelectedFile().getAbsolutePath() + File.separator
							+ DF.format(new Date()) + ".project");
					try {
						tmp.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				getDirectoryProp().setProperty("DATA_PATH",
						new File("").getAbsolutePath());
				getDirectoryProp().setProperty(
						"LAST_PROJECT",
						new File("").getAbsolutePath() + File.separator
								+ DF.format(new Date()) + ".project");
			}
		}
	}

	/**
	 * Method to control the skin of the GUI
	 * 
	 * @author jan behmann
	 */
	private void setLookAndFeel() {
		int number = 3;
		try {
			// LAF of the running OS
			// UIManager.setLookAndFeel(
			// UIManager.getSystemLookAndFeelClassName() );
			// NIMBUS LAF
			// UIManager.setLookAndFeel(
			// "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
			// Cross Plattform LAF
			// Available Look and Feels
			// System.out.println(UIManager.getInstalledLookAndFeels()[0].getName());
			// System.out.println(UIManager.getInstalledLookAndFeels()[1].getName());
			// System.out.println(UIManager.getInstalledLookAndFeels()[2].getName());
			// System.out.println(UIManager.getInstalledLookAndFeels()[3].getName());
			// System.out.println(UIManager.getInstalledLookAndFeels()[4].getName());
			UIManager
					.setLookAndFeel(UIManager.getInstalledLookAndFeels()[number]
							.getClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * Function to show an information frame while saving the project
	 * @author jbehmann
	 */
	private void showProjectSavingPanel(){
		JDialog fr = new JDialog(getThis(), true);
		fr.setTitle("Saving...");
		fr.setUndecorated(true);
		JLabel g = new JLabel(
				"<html>The Project is about to be saved. This can take a while.<br>Program will close automatically when saved.</html>");
		g.setVisible(true);
		g.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
		g.setOpaque(true);

		fr.add(g);
		g.validate();
		fr.pack();
		fr.setLocationRelativeTo(null);
		fr.validate();
		fr.repaint();
		fr.setVisible(true);
	}

	
	/**
	 * Saves the current project including views, trainviews, models, thumbnails and properties
	 */
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			processClosing();
		}
	}
	
	/**
	 * Method to be called every time the programm is exited.
	 * Saves the current project including views, trainviews, models, thumbnails and properties
	 */
	private void processClosing() {
		// Info frame in additional thread
		Thread t2 = new Thread() {
			public void run() {
				showProjectSavingPanel();
			}
		};
		t2.start();
		
		//Main Thread for saving the views
		Thread t = new Thread() {
			public void run() {

				boolean allSaved = true;
				boolean shouldSave = true;
				
				// Saving the views
					ArrayList<String> pathListViews = new ArrayList<String>();
					ArrayList<ImageIcon> thumbnails = new ArrayList<ImageIcon>();
					int i = 1;
					
					String projectFile = getDirectoryProp().getProperty("LAST_PROJECT");
					
					//Request for new path
					if (!new File (projectFile).exists()){
						Object[] options = {"Select a different project file for saving",
						"Discard current project"};
						JFrame frame = new JFrame();
						int n = JOptionPane.showOptionDialog(frame,
								"The last project ("+getDirectoryProp().getProperty("LAST_PROJECT")+") is not accesible. How to proceed?",
								"Project file does not exist",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.INFORMATION_MESSAGE,
								null,     //do not use a custom Icon
								options,  //the titles of buttons
								options[0]); //default button title

						// Reset path
						getDirectoryProp().setProperty("LAST_PROJECT", "");
						
						//If load other project file
						if (n == 0){
							JFileChooser chooser = new JFileChooser();

							if(getDirectoryProp().getProperty("LAST_PROJECT")!=null){
								chooser.setCurrentDirectory(new File(getDirectoryProp().getProperty("MODEL_PATH")));
							}
							int state = chooser.showOpenDialog( null );

							if ( state == JFileChooser.APPROVE_OPTION ){
								File file = chooser.getSelectedFile();
								getDirectoryProp().setProperty("LAST_PROJECT", file.getAbsolutePath());
							}
							else{
								shouldSave=false;
							}
						}
						else{
							shouldSave=false;
						}
					}
											
					if(shouldSave && new File( getDirectoryProp().getProperty("LAST_PROJECT")).exists()){ 	
					
					for (View v : getProject().getViewList()) {
						//name =  project_i.view
						String fileName = getDirectoryProp().getProperty(
								"LAST_PROJECT").substring(
								0,
								getDirectoryProp().getProperty(
										"LAST_PROJECT").lastIndexOf("."))
								+ "_" + i + ".view";
						
						File file = new File(fileName);
						
						String fileName2 = fileName.substring(0,
								fileName.length() - 7)
								+ "_" + i + ".mat";
						File file2 = new File(fileName2);

						//if not already saved
						if (v.getClass() != FileView.class) {
							
							View neu = new DoubleMatrixView(
									Utilities.materializeAsDoubleMatrix(v),
									v.getViewName());
							neu.setViewDescription(v.getViewDescription());
							neu.setFeatureDescriptors(v
									.getFeatureDescriptors().clone());
							neu.setFeatureRoles(v.getFeatureRoles().clone());
							neu.getLabelMap().putAll(v.getLabelMap());
							try {
								//save data 
								neu.saveMaterialized(file, file2,
										new MatlabFormat());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								v.save(file);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						v.setSaved(true);
						pathListViews.add(file.getAbsolutePath());

						thumbnails.add(new ImageIcon(v.getThumbnail()));

						i++;
					}

					
					//Saving the TrainViews
					ArrayList<String> pathListViews_TV = new ArrayList<String>();
					ArrayList<ImageIcon> thumbnails_TV = new ArrayList<ImageIcon>();
					i = 1;
					for (View v : getProject().getTViewList()) {
						// for(int j=0; j<this.getTViewList().size(); j++){
						// View v = this.getTViewList().get(j);
						String fileName = getDirectoryProp().getProperty(
								"LAST_PROJECT").substring(
								0,
								getDirectoryProp().getProperty(
										"LAST_PROJECT").lastIndexOf("."))
								+ "_TV_" + i + ".tv";
						// String fileName =
						// txtFileName.substring(0,txtFileName.length()-8) +
						// "_TV_" + i + ".tv";
						File file = new File(fileName);
						String fileName2 = fileName.substring(0,
								fileName.length() - 5)
								+ "_" + i + ".mat";
						File file2 = new File(fileName2);

						if (v.getClass() != FileView.class) {
							// v.saveMaterialized(file, file2, new
							// MatlabFormat());
							// new
							// DoubleMatrixView(Utilities.materializeAsDoubleMatrix(v),v.getViewName()).saveMaterialized(file,
							// file2, new MatlabFormat());

							View neu = new DoubleMatrixView(
									Utilities.materializeAsDoubleMatrix(v),
									v.getViewName());
							neu.setViewDescription(v.getViewDescription());
							neu.setFeatureDescriptors(v
									.getFeatureDescriptors().clone());
							neu.setFeatureRoles(v.getFeatureRoles().clone());
							neu.getLabelMap().putAll(v.getLabelMap());
							try {
								neu.saveMaterialized(file, file2,new MatlabFormat());
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								v.save(file);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						v.setSaved(true);
						pathListViews_TV.add(file.getAbsolutePath());

						thumbnails_TV.add(new ImageIcon(v.getThumbnail()));

						i++;
					}
					
					//Saving the models
					i = 1;
					ArrayList<String> pathListModels = new ArrayList<String>();
					if (!getProject().getModels().isEmpty()) {
						for (TransformationModel tr : getProject()
								.getModels()) {
							// if (!v.isSaved()){
							// allSaved = false;
							// }
							// else{
							String fileName = getDirectoryProp()
									.getProperty("LAST_PROJECT").substring(
											0,
											getDirectoryProp().getProperty(
													"LAST_PROJECT")
													.lastIndexOf("."))
									+ "_" + i + ".model";
							String finalFileName;
							try {
								finalFileName = tr.saveAtPath(fileName);
								pathListModels.add(finalFileName);
								// v.save(file);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								// e1.printStackTrace();
							} catch (Exception d) {
								// d.printStackTrace();
							}

							i++;
						}
					}

					//Saving the Thumbnails of the views as serialized ArrayList<ImageIcon>
					String thumbFileName = getDirectoryProp().getProperty(
							"LAST_PROJECT").substring(
							0,
							getDirectoryProp().getProperty("LAST_PROJECT")
									.length() - 8)
							+ "_thumb.ser";

					ObjectOutputStream o;
					try {
						o = new ObjectOutputStream(new FileOutputStream(	thumbFileName));
						o.writeObject(thumbnails);
						o.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Saving the Thumbnails of the TrainViews as serialized ArrayList<ImageIcon>
					String thumbFileName_TV = getDirectoryProp()
							.getProperty("LAST_PROJECT").substring(
									0,
									getDirectoryProp().getProperty(
											"LAST_PROJECT").length() - 8)
							+ "_thumb_TV.ser";

					ObjectOutputStream o_TV;
					try {
						o_TV = new ObjectOutputStream(new FileOutputStream(thumbFileName_TV));
						o_TV.writeObject(thumbnails_TV);
						o_TV.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Saving the Project file
					FileWriter fw;
					try {
						fw = new FileWriter(	new File(getDirectoryProp().getProperty(	"LAST_PROJECT")), false);
						fw.write(Project.DF.format(getProject().getDate())
								+ "; " + Project.DF.format(new Date()) + "; "
								+ pathListViews.toString() + "; "
								+ pathListViews_TV.toString() + "; "
								+ pathListModels.toString() + "; "
								+ thumbFileName + "; " + thumbFileName_TV
								+ "; " + getProject().getDescription());
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//Saving the directory properties
					try {
						getDirectoryProp().store(new FileOutputStream(new File("config",
										"directory.properties")), "");
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
					}
					else{
						allSaved=false;
					}
				if (allSaved == true) {
					System.exit(0);
				} else {
					if (JOptionPane
							.showConfirmDialog(
									getThis(),
									"Some data has not been saved.\nDo you want to close the window?",
									"Confirm closing",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						System.exit(0);
					}
				}

			}
		};
		t.start();
	}

	/**
	 * get the start Icon,
	 * 
	 * @return
	 */
	private JPanel getStartIcon() {
		if (startIcon == null) {
			startIcon = new JPanel();
			startIcon.setLayout(new GridLayout(1, 1));
			startIcon.add(new JLabel(new ImageIcon("logos/HSVap.jpg")));
		}
		return startIcon;
	}

	/**
	 * @param tabbedPane
	 *            the tabbedPane to set
	 */
	public void setTabbedPane(JTabbedPane tabbedPane) {
		this.tabbedPane = tabbedPane;
	}

	/**
	 * Tabs setzen
	 * Loading the image and the trainView (if applicable) if the classification panel is accessed
	 * @return
	 * @author modified by schmitter
	 */
	public JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addChangeListener( new ChangeListener() {
				int lastTabIndex = -1;
				@Override
				public void stateChanged( ChangeEvent event) {
					JTabbedPane sourceTabbedPane = (JTabbedPane) event.getSource();
					int newIndex = sourceTabbedPane.getSelectedIndex();
					// Go to DataManagement
					if (newIndex == 0) {

					}
					// Go to Feature Selection
					if (newIndex == 1) {
						getFeatureSelection().loadData();
					}
					// Go to ActiveLearning
					if (newIndex == 2) {
						if (getProject().getActiveViewIndex() != -1) {
							// does the view contain coordinates?
							if (FeatureSelection.containsCoordinates( getProject().getActiveView())) {

								getAlPanel().resetData( getProject().getActiveView());
								dataPanel.loadTrainView();
								
								getAlPanel().getSelectVisualisation();
							}
							else
								getPaintPanel().setLocked(true);
							//leaving tab Classification
							if (lastTabIndex == 2)
								getDataPanel().getImageProcessingPanel().clearSelections();
							lastTabIndex = newIndex;
						}
					}
				}
			});
			tabbedPane.addTab("Data-Management", getDataManagementPanel());
			tabbedPane.addTab("Feature Selection", getFeatureSelection());
			tabbedPane.addTab("Classification", getActiveLearning());
		}
		return tabbedPane;
	}
	

	private FeatureSelectionPanel getFeatureSelection() {		
		if (featureSelection == null) {		
			featureSelection = new FeatureSelectionPanel(this);
			featureSelection.loadData();
			/*featureSelection.addComponentListener(new ComponentListener() {
				public void componentHidden(ComponentEvent e) {
				}

				public void componentMoved(ComponentEvent e) {
				}

				public void componentResized(ComponentEvent e) {
				}

				public void componentShown(ComponentEvent e) {

					

				}
			});*/
		}
		return featureSelection;
	}

	// private JPanel getActiveLearning(){
	// dataPanel = new DataPanel(this);
	// if (alPanel ==null){
	// final JScrollPane scrollPane = new JScrollPane();
	// if(getProject().getActiveView()!=null){
	// if (FeatureSelection.getXCoordinate(getProject().getActiveView())!=-1 &&
	// FeatureSelection.getYCoordinate(getProject().getActiveView())!=-1){
	// paintPanel=new ImagePanel(scrollPane,this);
	// paintPanel.displayRGB(getProject().getActiveView());
	// }
	// }else {
	// paintPanel = new ImagePanel(new File("logos/logo.png"), scrollPane,this);
	// }
	// alPanel = new AlPanel(this);
	// }
	// return alPanel;
	// }

	public DataManagementPanel getDataManagementPanel() {
		if (dataManagementPanel == null) {
			dataManagementPanel = new DataManagementPanel(this);
		}
		return dataManagementPanel;
	}

	public void setDataManagementPanel(DataManagementPanel daten) {
		this.dataManagementPanel = daten;
	}

	// public MatrixStorage getMatrixStorage1() {
	// return matrixStorage;
	// }
	//
	// public MetadataStorage getMetaData1() {
	// return metaData;
	// }
	/**
	 * Menu Optionen setzen
	 * 
	 * @return
	 */
	private JMenuBar getJMBar() {
		JMenuBar bar = new JMenuBar();
		bar.add(getFileMenu());
		bar.add(getEditMenu());
		bar.add(getSourceMenu());
		bar.add(getHelpMenu());
		return bar;
	}

	public JSplitPane getSplitPane() {
		Toolkit kit = this.getToolkit();
		Dimension fullScreen = kit.getScreenSize();
		Dimension size = new Dimension(
				new Double(fullScreen.width * 0.75).intValue(), new Double(
						fullScreen.height * 0.75).intValue());
		
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setResizeWeight(0.3);
			splitPane.setDividerLocation((int)(0.3*size.width));
			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerSize(5);


			splitPaneProjektLog = new JSplitPane();
			splitPaneProjektLog.setResizeWeight(0.8);
			splitPaneProjektLog.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setLeftComponent(splitPaneProjektLog);
			splitPaneProjektLog.setMinimumSize(new Dimension(100, 100));
			System.out.println(splitPane.getHeight());
			splitPaneProjektLog.setPreferredSize(new Dimension(size.height,200));
			splitPaneProjektLog.setSize(new Dimension(size.height,200));
			

			for (Handler handler : log.getHandlers()) {
				if (handler instanceof TextComponentHandler) {
					TextComponentHandler textAreaHandler = (TextComponentHandler) handler;
					logTextArea = textAreaHandler.getTextArea();
				}
			}

			scrollPaneLog = new JScrollPane(logTextArea);

			splitPaneProjektLog.setRightComponent(scrollPaneLog);

			if (tabbedPane == null) {
				tabbedPane = getTabbedPane();
			}

			splitPane.setRightComponent(tabbedPane);

			// scrollPaneProjekt = new JScrollPane();
			splitPaneProjektLog.setLeftComponent(scrollPaneProjekt);

		}

		return splitPane;
		/*
		 * 
		 * 
		 * JScrollPane scrollPane = new JScrollPane();
		 * splitPane_1.setLeftComponent(scrollPane);
		 * 
		 * JTree tree = new JTree(); scrollPane.setViewportView(tree);
		 * 
		 * JScrollPane scrollPane_1 = new JScrollPane();
		 * splitPane_1.setRightComponent(scrollPane_1);
		 * 
		 * JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		 * splitPane.setRightComponent(tabbedPane);
		 * 
		 * JPanel pn_DataManagement = new JPanel();
		 * tabbedPane.addTab("Data Management", null, pn_DataManagement, null);
		 * 
		 * JPanel pn_FeatureSelection = new JPanel();
		 * tabbedPane.addTab("Feature Selection", null, pn_FeatureSelection,
		 * null);
		 * 
		 * JPanel pn_Classification = new JPanel();
		 * tabbedPane.addTab("Classification", null, pn_Classification, null);
		 */

	}
	public JSplitPane getScrollPaneProjekt() {
		return scrollPaneProjekt;
	}

	public void setScrollPaneProjekt(JSplitPane scrollPaneProjekt) {
		this.scrollPaneProjekt = scrollPaneProjekt;
	}

	private JMenu getSourceMenu() {
		sourceMenu = new JMenu("Source");
		sourceMenu.add(new JMenuItem("Select Workspace"));
		sourceMenu.add(new JMenuItem("Organize Imports"));
		sourceMenu.add(new JMenuItem("Clean Up"));
		return sourceMenu;
	}

	private JMenu getEditMenu() {
		editMenu = new JMenu("Edit");
		editMenu.add(getTabbedViewMI());
		editMenu.add(getHSVapViewMI());
		editMenu.add(getExtraImage());
		// editMenu.add(getWekaInstancesMI());
		return editMenu;
	}

	/**
	 * HSVap View
	 * 
	 * @return
	 */
	private JMenuItem getHSVapViewMI() {
		if (hsvapViewMI == null) {
			hsvapViewMI = new JMenuItem();
			hsvapViewMI.setText("Switch to HSVAp View");
			hsvapViewMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					setContentPane(startIcon);
					tabbedPane.setVisible(false);
					startIcon.setVisible(true);
					hsvapViewMI.setEnabled(false);
					tabbedViewMI.setEnabled(true);
				}
			});
		}

		return hsvapViewMI;
	}

	private JMenuItem getTabbedViewMI() {
		if (tabbedViewMI == null) {
			tabbedViewMI = new JMenuItem();
			tabbedViewMI.setText("Switch to Tabbed View");
			tabbedViewMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {

					setContentPane(new JPanel());
					setContentPane(getSplitPane());
					tabbedPane.setVisible(true);
					startIcon.setVisible(false);
					hsvapViewMI.setEnabled(true);
					tabbedViewMI.setEnabled(false);
					getContentPane().repaint();
				}
			});
		}
		return tabbedViewMI;
	}

	private JMenuItem getExtraImage() {
		if (extraImageMI == null) {
			extraImageMI = new JMenuItem();
			extraImageMI.setText("Generate Extra Image Frame");
			extraImageMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// generateExtraFrame();

				}
			});
		}

		return extraImageMI;
	}

	

	

	private JMenu getHelpMenu() {
		fileMenu = new JMenu("Help");
		fileMenu.add(getHelpMI());
		fileMenu.add(getLicenseMI());
		fileMenu.add(getPathConfigurationMI());
		fileMenu.add(getAboutMI());

		return fileMenu;
	}

	private JMenuItem getAboutMI() {
		if (aboutMI == null) {
			aboutMI = new JMenuItem();
			aboutMI.setText("About HSVap");
			aboutMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// w�hlt den Workspace
					// �ffnet das online Wiki im Browser
					try {
						Desktop.getDesktop()
								.browse(new URI(
										"http://www.ikg.uni-bonn.de/hsvap"));
					} catch (IOException e1) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
						e1.printStackTrace();
					} catch (Exception e2) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
					}
				}
			});
		}
		return aboutMI;
	}

	private JMenuItem getLicenseMI() {
		if (licenseMI == null) {
			licenseMI = new JMenuItem();
			licenseMI.setText("License Information");
			licenseMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// w�hlt den Workspace
					JFrame licenseFrame = getLicenseFrame();
					licenseFrame.setVisible(true);
				}
			});
		}
		return licenseMI;
	}
	
	/**
	 * initializes the help frame
	 * 
	 * @return
	 */
	private JFrame getLicenseFrame() {
		if (licenseFrame == null) {
			JLabel helpText = new JLabel();
			helpText.setText("<HTML><BODY>"
					+ "<h2>Copyright / Licence</h2>"
					+ "(c) 2016 Department of Geoinformation<br/>"
					+ "Institute of Geodesy and Geoinformation<br/>"
					+ "University of Bonn<br/>"
					+ "<br/>"
					+ "<p>This program is free software: you can redistribute it and/or modify it "
					+ "under the terms of the GNU General Public License as published by the Free "
					+ "Software Foundation, either version 3 of the License, or any later version.</p>"
					+ "<p>This program is distributed in the hope that it will be useful, but "
					+ "WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY "
					+ "or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for "
					+ "more details.</p>"
					+ "<p>You should have received a copy of the GNU General Public License along "
					+ "with this program.  If not, see <a href=\"http://www.gnu.org/licenses/\">http://www.gnu.org/licenses</a>.</p>"
					+ "<br/>" + "<h2>Version: 0.9</h2>" + "</BODY></HTML> ");

			helpText.setHorizontalTextPosition(SwingConstants.CENTER);
			helpText.setVerticalAlignment(SwingConstants.TOP);
			helpText.setHorizontalAlignment(SwingConstants.CENTER);
			licenseFrame = new JFrame();

			licenseFrame.setMinimumSize(new Dimension(600, 430));
			licenseFrame.setPreferredSize(new Dimension(600, 430));

			licenseFrame.setContentPane(helpText);
			licenseFrame.setTitle("About HSVap");
			licenseFrame.setLocationRelativeTo(null);
		}
		return licenseFrame;
	}
	
	private JMenuItem getPathConfigurationMI() {
		if (pathConfigurationMI == null) {
			pathConfigurationMI = new JMenuItem();
			pathConfigurationMI.setText("See path configurations");
			pathConfigurationMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFrame pathConfigurationFrame = getPathConfigurationFrame();
					pathConfigurationFrame.setVisible(true);
				}
			});
		}
		return pathConfigurationMI;
	}
	
	private JFrame getPathConfigurationFrame() {
		if ( pathConfigurationFrame == null ) {
			String conf[] = getDirectoryProp().toString().split(",");
			conf[0] = conf[0].replace("{", "");
			conf[conf.length-1] = conf[conf.length-1].replace("}", "");
			String config = new String();
			for (int i = 0; i < conf.length; i++) {
				config += conf[i] + "<br/>";
			}
			
			JLabel configuration = new JLabel();			
			configuration.setText("<HTML><BODY>"+config+"</BODY></HTML> ");
			
			pathConfigurationFrame = new JFrame();
	
			pathConfigurationFrame.setMinimumSize(new Dimension(400, 130));
			pathConfigurationFrame.setPreferredSize(new Dimension(400, 130));
	
			pathConfigurationFrame.setContentPane(configuration);
			pathConfigurationFrame.setTitle("Configuration");
			pathConfigurationFrame.setLocationRelativeTo(null);
		}
		return pathConfigurationFrame;
	}

	private JMenuItem getHelpMI() {
		if (helpMI == null) {
			helpMI = new JMenuItem();
			helpMI.setText("Online Help");
			helpMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// w�hlt den Workspace
					// �ffnet das online Wiki im Browser
					try {
						Desktop.getDesktop()
								.browse(new URI(
										"http://www.ikg.uni-bonn.de/hsvap"));
					} catch (IOException e1) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
						e1.printStackTrace();
					} catch (URISyntaxException e1) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
						e1.printStackTrace();
					} catch (Exception e2) {
						showErrorFrame("Browser not avaible. Update Java-Distribution");
					}
				}
			});
		}
		return helpMI;
	}

	private JMenu getFileMenu() {
		fileMenu = new JMenu("File");
		fileMenu.add(getLoadMI());
		fileMenu.add(getLoadTDfromFolder());
		fileMenu.add(getSaveMI());
		fileMenu.add(getLoadProjectMI());
		fileMenu.add(getSaveProjectMI());
		fileMenu.add(getConfigMI());
		fileMenu.add(getExitMI());

		return fileMenu;
	}

	private JMenuItem getConfigMI() {

		if (configMI == null) {
			configMI = new JMenuItem();
			configMI.setText("Configuration");
			configMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// w�hlt den Workspace
					JFrame configFrame = new JFrame("Config Dialog");
					configFrame.getContentPane().setLayout(
							new BoxLayout(configFrame.getContentPane(),
									BoxLayout.Y_AXIS));
					Container pane = configFrame.getContentPane();
					GridBagLayout glbConfig = new GridBagLayout();
					pane.setLayout(glbConfig);

					// pane.add();
					// pane.add(new JCheckBox("Unsupervied Learning", true));
					// pane.add(new JCheckBox("Supervised Learning", true));
					// pane.add();
					// pane.add(new JCheckBox("HPC Computing", false));
					DataManagementPanel.addComponent(pane, glbConfig,
							new JCheckBox("Exploration and Labeling", true), 0,
							0, 3, 1, 1, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							new JCheckBox("Unsupervied Learning", true), 0, 1,
							3, 1, 1, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							new JCheckBox("Supervised Learning", true), 0, 2,
							3, 1, 1, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							new JCheckBox("Feature Selection", true), 0, 3, 3,
							1, 1, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							new JCheckBox("HPC Computing", false), 0, 4, 3, 1,
							1, 0);

					workspace = new JTextField(getDirectoryProp().getProperty(
							"DATA_PATH"));
					workspace.setEditable(false);
					JButton browseW = new JButton("...");
//					python = new JTextField(getDirectoryProp().getProperty(
//							"PYTHON_PATH"));
//					python.setEditable(false);
//					JButton browseP = new JButton("...");

					browseW.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent arg0) {

							JFileChooser fc = new JFileChooser();
							try {
								fc.setCurrentDirectory(new File(
										getDirectoryProp().getProperty(
												"DATA_PATH")));
							} catch (Exception d) {
							}

							fc.setDialogTitle("Select a workspace, where working memory files will be saved.");
							fc.setFileFilter(new FileFilter() {

								@Override
								public boolean accept(File f) {
									return f.isDirectory();
								}

								@Override
								public String getDescription() {
									return "workspace directory";
								}

							});
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							fc.setAcceptAllFileFilterUsed(false);
							int h = fc.showSaveDialog(getThis());
							if (h == JFileChooser.APPROVE_OPTION) {
								getDirectoryProp().setProperty("DATA_PATH",
										fc.getSelectedFile().getAbsolutePath());
								workspace.setText(getDirectoryProp()
										.getProperty("DATA_PATH"));
							}
						}
					});

//					browseP.addActionListener(new ActionListener() {
//
//						@Override
//						public void actionPerformed(ActionEvent e) {
//
//							JFileChooser fc = new JFileChooser();
//							try {
//								fc.setCurrentDirectory(new File(
//										getDirectoryProp().getProperty(
//												"PYTHON_PATH")));
//							} catch (Exception d) {
//							}
//
//							fc.setDialogTitle("Select python executable.");
//							int h = fc.showSaveDialog(getThis());
//							if (h == JFileChooser.APPROVE_OPTION) {
//								getDirectoryProp().setProperty("PYTHON_PATH",
//										fc.getSelectedFile().getAbsolutePath());
//								python.setText(getDirectoryProp().getProperty(
//										"PYTHON_PATH"));
//							}
//
//						}
//					});

					DataManagementPanel.addComponent(pane, glbConfig,
							new JLabel(" "), 0, 5, 3, 1, 0, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							new JLabel("Set workspace:    "), 0, 6, 1, 1, 0, 0);
					DataManagementPanel.addComponent(pane, glbConfig,
							workspace, 1, 6, 1, 1, 1, 0);
					DataManagementPanel.addComponent(pane, glbConfig, browseW,
							2, 6, 1, 1, 0, 0);
//					DataManagementPanel.addComponent(pane, glbConfig,
//							new JLabel("Set Python path:  "), 0, 7, 1, 1, 0, 0);
//					DataManagementPanel.addComponent(pane, glbConfig, python,
//							1, 7, 1, 1, 1, 0);
//					DataManagementPanel.addComponent(pane, glbConfig, browseP,
//							2, 7, 1, 1, 0, 0);
					configFrame.pack();
					configFrame.setLocation(
							(Toolkit.getDefaultToolkit().getScreenSize().width - configFrame
									.getSize().width) / 2,
							(Toolkit.getDefaultToolkit().getScreenSize().height - configFrame
									.getSize().height) / 2);
					configFrame.setVisible(true);
				}
			});
		}
		return configMI;
	}

	private JMenuItem getExitMI() {
		if (exitMI == null) {
			exitMI = new JMenuItem();
			exitMI.setText("Exit");
			exitMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					// w�hlt den Workspace
					processClosing();
//					System.exit(0);

				}
			});
		}
		return exitMI;
	}

	public JMenuItem getSaveMI() {
		if (saveMI == null) {
			saveMI = new JMenuItem();
			saveMI.setText("Save selected Data (Ctrl + M)");
			saveMI.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveMI();
				}
			});
		}
		return saveMI;
	}

	private void saveMI() {
		// w�hlt den Workspace
		JFrame fenster = new JFrame();
		// String position=null;
		fenster.setTitle("Choose Destination");
		ArrayList<View> views= getDataManagementPanel().getSelectedViews();
		
		// int[] selIndex =
		// getDataManagementPanel().getDatenListe().getSelectedIndices();
		// if (selIndex.length != 0){
		if (views.size() != 0) {

			for (int i = 0; i < views.size(); i++) {
//				int selIndex = getDataManagementPanel().getOrderOfSelected()
//						.get(i);
				
				View selView = views.get(i);

				String fileName = selView.getViewName();

				File file = new File(fileName);

				// der File chooser �ffnet ein "Datei�ffnen fenster"
				JFileChooser chooser = new JFileChooser();
				// chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
				FileFilter jfilter = new FileNameExtensionFilter("View File",
						"view", "tv");
				chooser.addChoosableFileFilter(jfilter);
				FileFilter mfilter = new FileNameExtensionFilter("Mat File",
						"mat");
				chooser.addChoosableFileFilter(mfilter);
				FileFilter tfilter = new FileNameExtensionFilter("Text File (.txt)",
						"txt");
				chooser.addChoosableFileFilter(tfilter);
				chooser.setFileFilter(mfilter);
				if (!getDirectoryProp().getProperty("SAVE_PATH").equals("")) {
					chooser.setCurrentDirectory(new File(getDirectoryProp()
							.getProperty("SAVE_PATH")));
				}
				chooser.setSelectedFile(file);
				

				switch (chooser.showSaveDialog(fenster)) {
				case JFileChooser.APPROVE_OPTION:

					try {
						getDirectoryProp().setProperty("SAVE_PATH",
								chooser.getSelectedFile().getParent());
						String path = chooser.getSelectedFile()
								.getAbsolutePath();
						if (chooser.getFileFilter().equals(jfilter)) {

							if (!this.getDataManagementPanel().selectedViewIsTV()) {
								if (!path.endsWith(".view")) {
									path += ".view";
								}
							} else {
								if (!path.endsWith(".tv")) {
									path += ".tv";
								}
							}

							selView.save(new File(path));

						} else if (chooser.getFileFilter().equals(mfilter)) {

							if (!path.endsWith(".mat")) {
								path += ".mat";
							}
							String fileName2;
							if (!this.getDataManagementPanel().selectedViewIsTV()) {
								fileName2 = path
										.substring(0, path.length() - 4)
										.concat(".view");
							} else {
								fileName2 = path
										.substring(0, path.length() - 4)
										.concat(".tv");
							}

							selView.saveMaterialized(new File(fileName2),new File(path),new MatlabFormat());
							
						} else if (chooser.getFileFilter().equals(tfilter)) {
							if (!path.endsWith(".txt")) {
								path = path.replaceFirst("[.][^.]+$", "").concat(".txt");
							}
							String header;
							if (!this.getDataManagementPanel().selectedViewIsTV()) {
								header = path.replaceFirst("[.][^.]+$", "").concat(".view");
							} else {
								header = path.replaceFirst("[.][^.]+$", "").concat(".tv");
							}
							selView.saveMaterialized(new File(header), new File(path), new TextFormat());
						}

						selView.setSaved(true);
						getDataManagementPanel().actualiseListen();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					break;
				default:
					break;
				}
			}
		}

		// int returnVal = chooser.showSaveDialog(fenster);
		// if(returnVal == JFileChooser.APPROVE_OPTION) {
		// file= chooser.getSelectedFile() ;
		// if (file!=null){
		// position=file.getAbsolutePath();
		// try {
		// } catch (FileNotFoundException e3) {
		// showErrorFrame("Cookie-File not found");
		// e3.printStackTrace();
		// } catch (InvalidPropertiesFormatException e4) {
		// showErrorFrame("Cookie-File not in a proper format");
		// e4.printStackTrace();
		// } catch (IOException e1) {
		// showErrorFrame("Cookie-File not found");
		// e1.printStackTrace();
		// }

		// }
		// }
	}

	/**
	 * The menu item responsible for creating views from data files.
	 * 
	 * 2013-10-29 rewrite by pwelke
	 */
	private JMenuItem getLoadMI() {
		if (loadMI == null) {
			loadMI = new JMenuItem();
			loadMI.setText("Load Data File (Ctrl + L)");
			loadMI.addActionListener(new ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {

					loadMI();

				}
			});
		}
		return loadMI;
	}
	
	
	/**
	 * The menu item responsible for creating views from data files.
	 * 
	 * 2013-10-29 rewrite by pwelke
	 */
	private JMenuItem getLoadTDfromFolder() {
		if (loadTDfromFolder == null) {
			loadTDfromFolder = new JMenuItem();
			loadTDfromFolder.setText("Load TrainData from Folder");
			loadTDfromFolder.addActionListener(new ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {

					loadTDfromF();

				}
			});
		}
		return loadTDfromFolder;
	}
	

	
	/**
	 * Loads TrainData from Folder
	 * 
	 * 
	 */
	
	
	private void loadTDfromF(){
		JFileChooser chooser = new JFileChooser(new File("logos"));
		if (!getDirectoryProp().getProperty("LOAD_PATH").equals("")) {
			chooser.setCurrentDirectory(new File(getDirectoryProp()
					.getProperty("LOAD_PATH")));
		}
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(loadTDfromFolder) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();

			getDirectoryProp().setProperty("LOAD_PATH", file.getParent());
			Map<Integer,String> labelMap = new HashMap<Integer,String>();
			DoubleMatrix tData=null; 
			double[] wls = null;
			if (file != null) {
				for (final File fileEntry : file.listFiles()) {
			        if (fileEntry.isFile() && fileEntry.getName().endsWith(".csv")) {
			        	String name_whole = fileEntry.getName().split("\\.")[0];
			        	String name = name_whole.split("-")[0];
			        	int number = 0;
			        	if (name_whole.split("-").length>1){
			        		number = Integer.parseInt(name_whole.split("-")[1]);
			        	}
			        	else
			        		number=0;
			        	int id=-1;
			        	boolean found = false;
			        	for (Entry<Integer,String> entry : labelMap.entrySet()){
			        		if(name.equals(entry.getValue().split(" ")[0])){
			        			id= entry.getKey();
			        			found=true;
			        			break;
			        		}
			        	}
			        	
			        	if (!found){
			        		int maxi = -1;
			        		for (int temp :labelMap.keySet()){
			        			if(temp>maxi){
			        				maxi=temp;
			        			}
			        		}
			        		id=maxi+1;	
			        		Color color = new Color((float)Math.random(), (float)Math.random(),(float)Math.random());
			        		//+" #"+Integer.toHexString(classColor.getRGB()).substring(2)
			        		String colorString = Integer.toHexString(color.getRGB()).substring(2);
			        		labelMap.put(id, name+" #"+colorString);
			        	}
			        	String csvFile = fileEntry.getAbsolutePath();
			            String line = "";
			            String cvsSplitBy = ";";
			            BufferedReader br = null;
			            try {

			                br = new BufferedReader(new FileReader(csvFile));
			                if(wls==null){
			                	String wavelength= br.readLine();
			                	String[] wlsString =wavelength.split(";");
			                	wls=new double[wlsString.length];
			                	for (int i=0; i<wlsString.length;i++){
			                		wls[i]=Double.parseDouble(wlsString[i]);
			                	}
			                }
			                else{
			                	br.readLine();
			                }
			                
			                while ((line = br.readLine()) != null) {			                    
			                    String[] dataStrings = line.split(cvsSplitBy);
			                    double[] data = new double[wls.length+1];
			                    data[0]=id;
			                    for (int i=0; i<dataStrings.length;i++){
			                    	data[i+1]=Double.parseDouble(dataStrings[i]);
			                	}
			                    if(tData==null){
			                    	tData = new DoubleMatrix(0,wls.length+1);
			                    	System.out.println("TData inititalsiert!");
//			                    	System.out.println(tData);
			                    }
			                    DoubleMatrix row = new DoubleMatrix(data);
			                    row = row.transpose();
//			                    System.out.println(row);
			                    tData=DoubleMatrix.concatVertically(tData,row);
//			                    System.out.println(tData);
			                }

			            } catch (FileNotFoundException e) {
			                e.printStackTrace();
			            } catch (IOException e) {
			                e.printStackTrace();
			            } finally {
			                if (br != null) {
			                    try {
			                        br.close();
			                    } catch (IOException e) {
			                        e.printStackTrace();
			                    }
			                }
			            }
			        	
			        	
			        
			        }
			    }
				//System.out.println(tData);
				if(tData.length>0 && wls !=null){
					TrainView tv = new TrainView(tData,wls,file.getName());
					tv.setLabelMap(labelMap);
					this.getProject().addTrainView(tv, file.getName());	
					this.repaint();
					this.getDataManagementPanel().actualiseListen();
				}else{
					System.out.println("No Train Data in folder. Nothing loaded!");
				}
				
			}
		}
		
		
		
		
	}
	
	
	private void loadMI() {
		JFileChooser chooser = new JFileChooser(new File("logos"));
		if (!getDirectoryProp().getProperty("LOAD_PATH").equals("")) {
			chooser.setCurrentDirectory(new File(getDirectoryProp()
					.getProperty("LOAD_PATH")));
		}
		chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
		chooser.setAcceptAllFileFilterUsed(false);

		// add all supported file extensions
		FileFilter filter = new FileFilter() {

			@Override
			public String getDescription() {
				return "All Files (.mat, .view, .tv, .hdr, .txt)";
			}

			@Override
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".mat")
						| f.getAbsolutePath().endsWith(".view")
						| f.getAbsolutePath().endsWith(".tv")
						| f.getAbsolutePath().endsWith(".hdr")
						| f.getAbsolutePath().endsWith(".txt")
						| f.isDirectory();
			}
		};
		chooser.addChoosableFileFilter(filter);
		FileFilter mfilter = new FileNameExtensionFilter("Mat File (.mat)",
				"mat");
		chooser.addChoosableFileFilter(mfilter);
		FileFilter jfilter = new FileNameExtensionFilter(
				"View File (.view/.tv)", "view", "tv");
		chooser.addChoosableFileFilter(jfilter);
		chooser.setFileFilter(filter);
		FileFilter efilter = new FileNameExtensionFilter(
				"Envi Header File (.hdr)", "hdr");
		chooser.addChoosableFileFilter(efilter);
		FileFilter tfilter = new FileNameExtensionFilter("Text File (.txt)",
				"txt");
		chooser.addChoosableFileFilter(tfilter);

		// if user has selected a file
		if (chooser.showOpenDialog(loadMI) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();

			getDirectoryProp().setProperty("LOAD_PATH", file.getParent());

			if (file != null) {
				View view = null;
				FileFormat format = null;

				// if file ends with .json, file will be a jsonfile
				if (file.getAbsolutePath().endsWith(".view")
						| file.getAbsolutePath().endsWith(".tv")) {

					try {
						view = Utilities.createViewFromJSON(file);
						System.out.println("Loading completed.");
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InstantiationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						System.out.println(e1.getMessage());
					}

				} else if (file.getAbsolutePath().endsWith(".mat")) { // if file
																		// ends
																		// with
																		// .mat,
																		// file
																		// will
																		// be a
																		// matfile
																		// with
																		// or
																		// without
																		// accompanying
																		// txt
																		// file

					view = new FileView(new MatlabFormat(), file); // with
																				// accompanying
																				// txt
																				// file

					System.out.println("Loading completed.");

				} else if (file.getAbsolutePath().endsWith(".hdr")) { // if file
																		// ends
																		// with
																		// .hdr,
																		// file
																		// will
																		// be an
																		// envi
																		// header
					format = new ENVIFormat(file);

					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					File parentFile= file.getParentFile();
					File dataFile = new File("zero");
					String fileName_red;
					 if (file.getName().indexOf(".") > 0) {
						 fileName_red= file.getName().substring(0, file.getName().lastIndexOf("."));
					 } else {
						 fileName_red = file.getName();
					 }
					 for (final File fileEntry : parentFile.listFiles()) {
//						 String[] segs = fileEntry.getName().split(Pattern.quote( "." ));
//						 System.out.println(segs[0]);
						 

						 if (!fileEntry.getName().endsWith("hdr")){
							 
							 String fileEntryName_red;
							 if (fileEntry.getName().indexOf(".") > 0) {
								 fileEntryName_red= fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf("."));
							 } else {
								 fileEntryName_red = fileEntry.getName();
							 }
							 

							 if (fileEntryName_red.equals(fileName_red)) {

								 dataFile = fileEntry;
								 System.out.println("File found!");
								 break;
							 }
						 }
					 }
//					File dataFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-4));
					//&& fileEntry.getName().contains(Pattern.quote( "." )) 
					
					
					
					
					
					System.out.println(dataFile.getAbsolutePath());
					if (dataFile.exists()){
						file = dataFile;
					}
					else{
						// select data file to load
						JFileChooser dataFileChooser = new JFileChooser(new File(
								"logos"));
						dataFileChooser
								.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
						dataFileChooser
								.setDialogTitle("Please select the data for the header");
						FileFilter dfilter = new FileNameExtensionFilter(
								"Mat Files", "dat", "raw");
						dataFileChooser.setFileFilter(dfilter);
						if (dataFileChooser.showOpenDialog(loadMI) == JFileChooser.APPROVE_OPTION) {
							file = dataFileChooser.getSelectedFile();
						}
					}

					// create view using the file format that was selected by
					// choosing a file filter
					try {
						view = new FileView(format, file);
						System.out.println("Loading completed.");
					} catch (java.lang.Error er) {
						er.printStackTrace();
					}
				} else if (file.getAbsolutePath().endsWith(".txt")) {
					view = new FileView(new TextFormat(), file);
					System.out.println("Loading completed.");
				} 
				else {
					view = null;
				}
				if (view != null) {
					try {
						view.setViewName(file.getName());
						String rawFile =file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf("."));
						if (file.getAbsolutePath().endsWith(".tv")|| new File(rawFile+".tv").exists()) {
							getProject().getTViewList().add(TrainView.createTrainViewFromView(view));
						} else {
							getProject().add(view);
						}
						view.setSaved(true);
						getDataManagementPanel().actualiseListen();
						try {
							view.materializeXYDimension();
						} catch (ArrayIndexOutOfBoundsException e34) {

						}

						view.get(0, 0);
					} catch (Throwable t) {
						System.err.println(t.getMessage());
						System.err.println("Ladefehler");
					/*	ViewEditor ve = new ViewEditor(
								this.getDataManagementPanel(), view);*/
					}
				} else {
					System.out.println("Loading failed.");
				}
			}
		}
	}

	public JMenuItem getSaveProjectMI() {
		if (saveProjectMI == null) {
			saveProjectMI = new JMenuItem();
			saveProjectMI.setText("Save Project (Ctrl + S)");
			saveProjectMI
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {

							saveProjMI();
						}
					});
		}
		return saveProjectMI;
	}

	/**
	 * Saves the current project.
	 */
	private void saveProjMI() {
		JFileChooser chooser = new JFileChooser();
		String fileN = File.separator
				+ new SimpleDateFormat("dd_MM_yyyy").format(new Date())
				+ ".project";

		File file = new File(new File(getDirectoryProp().getProperty(
				"LAST_PROJECT")).getParent()
				+ fileN);
		chooser.setCurrentDirectory(new File(getDirectoryProp().getProperty(
				"LAST_PROJECT")).getParentFile());
		chooser.setSelectedFile(file);
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) { // Only folders are accepted
				return f.isDirectory() || f.getName().endsWith(".project");
			}

			@Override
			public String getDescription() {
				return "project-File";
			}

		});

		switch (chooser.showSaveDialog(getThis())) {
		case JFileChooser.APPROVE_OPTION:
			try {

				String txtFileName = chooser.getSelectedFile()
						.getAbsolutePath();
				String newLastProject = getProject().save(txtFileName);
				getDirectoryProp().setProperty("LAST_PROJECT", newLastProject);
				getDataManagementPanel().actualiseListen();

			} catch (IOException e1) {
				e1.printStackTrace();
			}

			break;
		default:
			break;
		}
	};

	public JMenuItem getLoadProjectMI() {
		if (loadProjectMI == null) {
			loadProjectMI = new JMenuItem();
			loadProjectMI.setText("Open Project (Ctrl + O)");
			loadProjectMI
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							loadProjMI();
						}
					});
		}
		return loadProjectMI;
	}

	private void loadProjMI() {
		int h = JOptionPane
				.showConfirmDialog(
						null,
						"Do you want to save the current project? Otherwise it will be discarded.",
						"Save current project?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (h == 0) {
			JFileChooser chooser = new JFileChooser();
			String fileN = File.separator
					+ new SimpleDateFormat("dd_MM_yyyy").format(new Date())
					+ ".project";
			File file = new File(new File(getDirectoryProp().getProperty(
					"LAST_PROJECT")).getParent()
					+ fileN);
			chooser.setCurrentDirectory(new File(getDirectoryProp()
					.getProperty("LAST_PROJECT")).getParentFile());
			chooser.setSelectedFile(file);
			chooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}

				@Override
				public String getDescription() {
					return "project-File";
				}

			});

			switch (chooser.showSaveDialog(getThis())) {
			case JFileChooser.APPROVE_OPTION:

				try {

					String txtFileName = chooser.getSelectedFile()
							.getAbsolutePath();
					String newLastProject = getProject().save(txtFileName);
					getDirectoryProp().setProperty("LAST_PROJECT",
							newLastProject);
					getDataManagementPanel().actualiseListen();

				} catch (IOException e1) {
					e1.printStackTrace();
				}

				break;
			default:
				break;
			}
		}
		try {

			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File(getDirectoryProp()
					.getProperty("LAST_PROJECT")).getParentFile());
			chooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().endsWith(".project");
				}

				@Override
				public String getDescription() {
					return "project-File";
				}

			});

			// chooser.setFileView(new FileView() {
			// });
			chooser.setAccessory(new MetaPreview(chooser, false));

			switch (chooser.showOpenDialog(getThis())) {
			case JFileChooser.APPROVE_OPTION:

				// saveProjectMI.setAction(new Lef)

				Project proj = new Project(chooser.getSelectedFile());
				getThis().project = proj;
				getDataManagementPanel().actualiseListen();

				break;
			default:
				break;
			}
		} catch (Exception es) {
			
		}
	}

	protected JPanel getActiveLearning() {
		// matrixStorage = new MatrixStorage();
		dataPanel = new DataPanel(this);

		if (alPanel == null) {
			final JScrollPane scrollPane = new JScrollPane();
			if (getProject().getActiveView() != null) {
				if (FeatureSelection.containsCoordinates(getProject().getActiveView())){
					// paintPanel=new ImagePanel(new
					// File("logos/Feature_Val20.png"));

					paintPanel = new ImagePanel(scrollPane, this);
					// paintPanel.setMat(datamatrixStorage);
					paintPanel.displayRGB(getProject().getActiveView());
				}
			} else {
				paintPanel = new ImagePanel(new File("logos/logo.png"),
						scrollPane, this);

				// paintPanel = new ImagePanel(scrollPane, dataPanel);
				// LabelMat mat = new LabelMat();
				// mat.setSampleMatrix();
				// paintPanel.setLabelMat(mat);
			}
			alPanel = new AlPanel(this);
		}
		return alPanel;
	}

	/**
	 * @return the paintPanel
	 */
	public ImagePanel getPaintPanel() {
		return paintPanel;
	}

	/**
	 * @param paintPanel
	 *            the paintPanel to set
	 */
	public void setPaintPanel(ImagePanel paintPanel) {
		this.paintPanel = paintPanel;
	}

	/**
	 * @return the dataPanel
	 */
	public DataPanel getDataPanel() {
		return dataPanel;
	}

	/**
	 * @param dataPanel
	 *            the dataPanel to set
	 */
	public void setDataPanel(DataPanel dataPanel) {
		this.dataPanel = dataPanel;
	}

//	/**
//	 * @return the lastDisplayedView
//	 */
//	public View getLastDisplayedView() {
//		return lastDisplayedView;
//	}
//
//	/**
//	 * @param lastDisplayedView
//	 *            the lastDisplayedView to set
//	 */
//	public void setLastDisplayedView(View lastDisplayedView) {
//		this.lastDisplayedView = lastDisplayedView;
//	}

	/**
	 * @return the directoryProp
	 */
	public Properties getDirectoryProp() {

		if (directoryProp == null) {
			if (!new File("config", "directory.properties").exists()) {
				directoryProp = new Properties();
				directoryProp.put("DATA_PATH", "");
				directoryProp.put("LAST_PROJECT", "");
				directoryProp.put("LAST_MODEL", "");
				directoryProp.put("LOAD_PATH", "");
				directoryProp.put("SAVE_PATH", "");
				try {
					directoryProp.store(new FileOutputStream(new File("config",
							"directory.properties")), "");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				directoryProp = new Properties();
				try {
					File file = new File("config", "directory.properties");
					directoryProp.load(new FileInputStream(file));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return directoryProp;
	}
	
	/**
	 * show error Frame
	 * 
	 * @param error
	 */
	public void showErrorFrame(String error) {
		if (!error.contentEquals(""))
			JOptionPane.showMessageDialog(null, error, "Warning",
					JOptionPane.ERROR_MESSAGE);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Locale.setDefault(Locale.ENGLISH);
		new MainGui();

	}

	public Project getProject() {
		if (project == null) {
			project = new Project();
		}
		return project;
	}

	public void setProject(Project p) {
		this.project = p;
	}

	public MainGui getThis() {
		return this;
	}

	private class MyDispatcher implements KeyEventDispatcher {

		private final Set<Integer> pressed = new HashSet<Integer>();

		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {

				pressed.add(e.getKeyCode());
				// if(!pressed.isEmpty()){
				// System.out.println(pressed.toString());
				// }
				if (pressed.contains(17) && e.getKeyCode() == 79) { // Str o
					loadProjMI();
				}
				if (pressed.contains(17) && e.getKeyCode() == 83) { // Str s
					saveProjMI();
				}
				if (pressed.contains(17) && e.getKeyCode() == 76) { // Str l
					loadMI();
				}
				if (pressed.contains(17) && e.getKeyCode() == 77) { // Str m
					saveMI();
				}
				if (e.getKeyCode() == 17) { // Str
					ctrl = true;
				}

			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				pressed.remove(e.getKeyCode());
				if (e.getKeyCode() == 17) { // Str
					ctrl = false;
				}
			} else if (e.getID() == KeyEvent.KEY_TYPED) {

			}
			return false;
		}
	}
	
	public Logger getLog() {
		return log;
	}
}
