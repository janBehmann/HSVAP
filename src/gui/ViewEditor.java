/**
 * 
 */
package gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;

import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.fileformats.FileView;
import data.fileformats.MatlabFormat;
import data.operators.AddRGBView;
import data.operators.TrainView;

/**
 * @author s7tischu
 *
 */
public class ViewEditor extends JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MainGui master;
	private JTextField viewPath;
	private JPanel txtFields;
	private GridBagLayout gblTxtFields;
	private GridBagLayout gblthis = new GridBagLayout();
	private boolean expanded = false;
	private int k;
	private int selIndex;
	private JButton browse; 
	private JTextPane expand;
	private JButton reset;
	private JButton enter;
	private JButton importWvl;
	private View view;


	

	public ViewEditor(MainGui master, View view){
		
		//	try{
		super(master, true);
		this.master = master;
		
		this.setTitle("Data Editor");
		//						this.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().width *0.5), (int)(Toolkit.getDefaultToolkit().getScreenSize().height *0.5));
		this.setSize((int) (Toolkit.getDefaultToolkit().getScreenSize().width *0.5), 450);
		this.setLocation(	(Toolkit.getDefaultToolkit().getScreenSize().width-this.getSize().width) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height-this.getSize().height) / 2 +50		);

		gblthis = new GridBagLayout();
		this.setLayout( gblthis );

		this.view = view;
		

//		view = master.getViews().get(selIndex);

		k = Math.min(19, view.getNumberOfExamples());
		

		//						String[][] data = new String[k][view.getNumberOfColumns()];


		txtFields = new JPanel();
		gblTxtFields = new GridBagLayout();
		txtFields.setLayout( gblTxtFields );



		generateTable(false);

		reset = getReset();

		importWvl = getImportWvl();

		enter = getEnter();

		viewPath = new JTextField();
		viewPath.setEditable(false);

		browse = getBrowse();
		if(this.master.getDataManagementPanel().getOrderOfSelected().size()>0){
			selIndex=this.master.getDataManagementPanel().getOrderOfSelected().get(0);
		}
		else{
			selIndex=-1;
		}
		
		
		if(!this.master.getDataManagementPanel().isIsselectedViewTrainView()){
			if (!master.getProject().getMatPaths_TV().isEmpty()){
				viewPath.setText(master.getProject().getMatPaths().get(selIndex));
			}
		}else{
			if (!master.getProject().getMatPaths_TV().isEmpty()){
			viewPath.setText(master.getProject().getMatPaths_TV().get(selIndex));
			}
		}
//		viewPath.setText(getMaster().getMaster().getProject().getMatPaths().get(selIndex));
		try{

			viewPath.setText(((data.fileformats.FileView) view).getDataFile().getAbsolutePath());

		}catch(ClassCastException dva){

			viewPath.setText("unsaved");

		}




		char updown;
		if (expanded == false){
			updown = '\u2193';   //runter
		}else{
			updown = '\u2191';
		}

		expand=new JTextPane();
		expand.setEditable(false);
		expand.setMinimumSize(new Dimension(100,30));

		MyHTMLEditorKit kit= new MyHTMLEditorKit();


		expand.setOpaque(false);
		expand.setEditorKit(kit);
		expand.addHyperlinkListener(new HTMLListener2());
		expand.setEditable(false);

		expand.setText("<html>\n" +
				"<body>\n" +
				"<a href=#>\n"+ updown +" show all</a>" +
				"</body>\n" +
				"</html>");


		JScrollPane sp = new JScrollPane(txtFields);
		DataManagementPanel.addComponent(this, gblthis, viewPath		, 0, 0, 2, 1, 1, 0);
		DataManagementPanel.addComponent(this, gblthis, browse			, 2, 0, 2, 1, 0, 0);
		DataManagementPanel.addComponent(this, gblthis, new JLabel(" ")	, 0, 1, 4, 1, 1, 0);
		DataManagementPanel.addComponent(this, gblthis, sp 				, 0, 2, 4, 1, 1, 1);
		DataManagementPanel.addComponent(this, gblthis, expand 				, 0, 3, 1, 1, 0, 0);
		DataManagementPanel.addComponent(this, gblthis, new JLabel(" ")	, 0, 4, 1, 1, 1, 0);
		DataManagementPanel.addComponent(this, gblthis, importWvl		, 1, 4, 1, 1, 0, 0);
		DataManagementPanel.addComponent(this, gblthis, reset			, 2, 4, 1, 1, 0, 0);
		DataManagementPanel.addComponent(this, gblthis, enter			, 3, 4, 1, 1, 0, 0);


		this.setVisible(true);


	}

	public JButton getImportWvl() {
		
		importWvl = new JButton("Import wavelength data");
		importWvl.setEnabled(FeatureSelection.getRGB(view)==-1);
		importWvl.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int selIndex = master.getDataManagementPanel().getDatenListe().getSelectedIndex();
				View view = master.getProject().getViewList().get(selIndex);

				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(new FileFilter() {

					@Override
					public String getDescription() {
						return "txt-File (csv-format)";
					}

					@Override
					public boolean accept(File arg0) {
						return arg0.getAbsolutePath().endsWith(".txt") || arg0.isDirectory();
					}
				});
				switch (fc.showOpenDialog(master.getDataManagementPanel())) {
				case JFileChooser.APPROVE_OPTION:

					try{
						// read meta data
						BufferedReader br = new BufferedReader(new FileReader(fc.getSelectedFile()));
						try {		
							String[] featureDescriptors = new String[view.getNumberOfColumns()];

							int i = 0;
							String line;
							while((line = br.readLine()) != null) {
								for (String dsc : line.split(",")) {
									featureDescriptors[i] = dsc.trim();
									++i;
								}
							}
							br.close();
							FeatureRole[] m = view.getFeatureRoles();
							int k = 0;
							for(int ii = 0; ii<view.getNumberOfColumns(); ii++){
								if(m[ii].equals(FeatureRole.FEATURE)){
									view.setFeatureDescriptor(ii,featureDescriptors[k]);
									k++;
								}
							}

						} catch (IOException e) {
							br.close();
							e.printStackTrace();
						}

						master.getProject().add(new AddRGBView(Utilities.materializeRGB(view), view));
						master.getDataManagementPanel().actualiseListen();
						getThis().dispose();

					}catch(IOException db){
					}
					break;
				default:
					break;
				}
			}
		});
		return importWvl;
	}
	public JButton getEnter2(){
		enter = new JButton("Enter");
		enter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				getThis().dispose();
				master.getDataManagementPanel().actualiseListen();
			}
			
		});
		return enter;
	}
	
	public JButton getEnter() {
		enter = new JButton("Enter");
		enter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int selIndex = master.getDataManagementPanel().getDatenListe().getSelectedIndex();
				View view = master.getProject().getViewList().get(selIndex);

				for(int i = 0; i<view.getNumberOfColumns(); i++){
					@SuppressWarnings("unchecked")
					FeatureRole fr = (FeatureRole) ((JComboBox<FeatureRole>)txtFields.getComponent(i)).getSelectedItem();
					String desc = ((JTextField)txtFields.getComponent(i+view.getNumberOfColumns())).getText();

					//									if(!(view.getFeatureRoles()[i].equals(FeatureRole.FEATURE)&&fr.equals(FeatureRole.FEATURE))){
					//										view.setFeatureDescriptor(i, fr.name());
					//									}
					view.setFeatureRole(i, fr);
					view.setFeatureDescriptor(i, desc);
				}

				view.setSaved(false);
				getThis().dispose();
				master.getDataManagementPanel().actualiseListen();
			}
		});
		return enter;
	}

	public JButton getBrowse() {
		browse = new JButton("Edit");
		browse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				JFileChooser fc = new JFileChooser(master.getDirectoryProp().getProperty("DATA_PATH"));
				fc.setDialogTitle("Set the view path");
				fc.setFileFilter(new FileFilter(){

					@Override public boolean accept( File f ){

						return f.isDirectory() || f.getName().toLowerCase().endsWith(".mat" );
					}

					@Override public String getDescription(){
						return "Mat-File (.mat)";
					}
				} );

				int state = fc.showSaveDialog( null );

				if ( state == JFileChooser.APPROVE_OPTION ){
					File file = fc.getSelectedFile();
					String path = file.getAbsolutePath();
					if(!path.endsWith(".mat")){
						path = path + ".mat";
					}

//					String fileName2 = path.substring(0,path.length()-4).concat(".json");

					View newView;

					
					newView = new FileView(new MatlabFormat(), new File(path));		//with accompanying txt file
						
					
//					System.out.println("Loading completed.");
//						newView = v.saveMaterialized(new File(fileName2), new File(path), new MatlabFormat());
					newView.setSaved(true);
//					System.out.println(selIndex);
					if(master.getDataManagementPanel().getOrderOfSelected().size()>0){
						selIndex=master.getDataManagementPanel().getOrderOfSelected().get(0);
					}
					else{
						selIndex=-1;
					}
					if(selIndex!=-1){  // Fall: Datei ersetzen in Vieweditor
						
						if(newView.getNumberOfColumns()==view.getNumberOfColumns()){
							newView.setFeatureDescriptors(view.getFeatureDescriptors());
							newView.setFeatureRoles(view.getFeatureRoles());
						}else if(newView.getNumberOfColumns()>view.getNumberOfColumns()){
							String[] newDescr = new String[newView.getNumberOfColumns()];
							FeatureRole[] newRole = new FeatureRole[newView.getNumberOfColumns()];
							for(int i=0;i<view.getNumberOfColumns();i++){
								newDescr[i] = view.getFeatureDescriptors()[i];
								newRole[i] = view.getFeatureRoles()[i];
							}
							for(int i=view.getNumberOfColumns();i<newView.getNumberOfColumns();i++){
								newDescr[i] = FeatureRole.FEATURE.name();
								newRole[i] = FeatureRole.FEATURE;
							}
							newView.setFeatureDescriptors(newDescr);
							newView.setFeatureRoles(newRole);
						}else {
							String[] newDescr = new String[newView.getNumberOfColumns()];
							FeatureRole[] newRole = new FeatureRole[newView.getNumberOfColumns()];
							for(int i=0;i<newView.getNumberOfColumns();i++){
								newDescr[i] = view.getFeatureDescriptors()[i];
								newRole[i] = view.getFeatureRoles()[i];
							}

							newView.setFeatureDescriptors(newDescr);
							newView.setFeatureRoles(newRole);
						}
						
						newView.setViewName(view.getViewName());
						newView.setLabelMap(view.getLabelMap());
						if(master.getDataManagementPanel().getOrderOfSelected().size()>0){
							selIndex=master.getDataManagementPanel().getOrderOfSelected().get(0);
						}
						else{
							selIndex=-1;
						}
						if(!master.getDataManagementPanel().isIsselectedViewTrainView()){
							master.getProject().getViewList().set(selIndex,newView);
						}else{
							master.getProject().getTViewList().set(selIndex,(TrainView) newView);
						}
						
					}else{			//Fall: Datei bei einladen fehlerhaft
						
						master.getProject().add(newView);
						
					}
					String pathNeu = ((data.fileformats.FileView)newView).getDataFile().getAbsolutePath();
					if(master.getDataManagementPanel().getOrderOfSelected().size()>0){
						selIndex=master.getDataManagementPanel().getOrderOfSelected().get(0);
					}
					else{
						selIndex=-1;
					}
					if(!master.getDataManagementPanel().isIsselectedViewTrainView()){
						int sz = master.getProject().getViewList().size()-1;
						master.getProject().getMatPaths().set(sz, pathNeu);
					}else{
						int sz = master.getProject().getTViewList().size()-1;
						master.getProject().getMatPaths_TV().set(sz, pathNeu);
					}
					
					viewPath.setText(pathNeu);

					master.getDirectoryProp().setProperty("DATA_PATH",((data.fileformats.FileView)newView).getDataFile().getParent());

					view = newView;
					
					
					
					reset.setEnabled(true);

					enter.setEnabled(true);


					//TODO 
//					viewPath.setText("Data lost.");
					viewPath.setBorder(BorderFactory.createEmptyBorder());
					viewPath.setText(((data.fileformats.FileView) view).getDataFile().getAbsolutePath());
					generateTable(false);
					
					
					
				}
			}
		});
		return browse;
	}

	public JButton getReset() {
		reset = new JButton("Reset");
		reset.addActionListener(new ActionListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {

				int selIndex = master.getDataManagementPanel().getDatenListe().getSelectedIndex();
				View view = master.getProject().getViewList().get(selIndex);

				for(int i = 0; i<view.getNumberOfColumns(); i++){
					((JComboBox<FeatureRole>)txtFields.getComponent(i)).setSelectedItem(view.getFeatureRoles()[i]);
				}
				for(int i = 0; i<view.getNumberOfColumns(); i++){
					((JTextField)txtFields.getComponent(i+view.getNumberOfColumns())).setText(view.getFeatureDescriptors()[i]);
				}
			}
		});
		return reset;
	}


	

	/**
	 * HTML-Listener to realise expand function in view editor
	 * 
	 * @author Till
	 *
	 */
	private class HTMLListener2 implements HyperlinkListener {
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

				if(expanded == true){
					expanded = false;
				}else{
					expanded = true;
				}

				generateTable(expanded);

				char updown;
				if (expanded == false){
					updown = '\u2193';   //runter
				}else{
					updown = '\u2191';
				}
				expand.setText("<html>\n" +
						"<body>\n" +
						"<a href=#>\n"+ updown +" show all</a>" +
						"</body>\n" +
						"</html>");

			}
		}
	}

	public void generateTable(boolean expanded) {
		

		txtFields.removeAll();

		k = 10;
		if(view.getNumberOfExamples()<k){
			k = view.getNumberOfExamples();
		}

		for (int j = 0; j<view.getNumberOfColumns(); j++){
			JComboBox<FeatureRole> cbx = new JComboBox<FeatureRole>();
			for(FeatureRole f: FeatureRole.values()){
				cbx.addItem(f);
			}
			cbx.setSelectedItem(view.getFeatureRoles()[j]);
			DataManagementPanel.addComponent(txtFields, gblTxtFields, cbx, j, 1, 1, 1, 1, 0);
		}


		for (int j = 0; j<view.getNumberOfColumns(); j++){
			JTextField tf = new JTextField(view.getFeatureDescriptors()[j]);
			DataManagementPanel.addComponent(txtFields, gblTxtFields, tf, j, 3, 1, 1, 1, 0);	
		}

		DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel("Feature Roles:"), 0, 0, view.getNumberOfColumns(), 1, 1, 0);
		DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel("Feature Descriptors:"), 0, 2, view.getNumberOfColumns(), 1, 1, 0);
		DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel(" "), 0, 4, view.getNumberOfColumns(), 1, 1, 0);
		DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel("First "+k+" rows of the data:"), 0, 5, view.getNumberOfColumns(), 1, 1, 0);




		if(!expanded){

			//			getThis();

			// write data
			for (int i = 0; i<k; i++){
				for (int j = 0; j<view.getNumberOfColumns(); j++){
					JTextField l = new JTextField(String.valueOf(view.get(i, j)));
					l.setEditable(false);
					l.setBackground(Color.WHITE);
					DataManagementPanel.addComponent(txtFields, gblTxtFields, l, j, 6+i, 1, 1, 1, 0);
				}
			}
			DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel(" "), 0, 7+k, view.getNumberOfColumns(), 1, 1, 1);
		}else{

			// write data
			int h= view.getNumberOfExamples();
			for (int i = 0; i<h; i++){
				for (int j = 0; j<view.getNumberOfColumns(); j++){
					JTextField l = new JTextField(String.valueOf(view.get(i, j)));
					l.setEditable(false);
					l.setBackground(Color.WHITE);
					DataManagementPanel.addComponent(txtFields, gblTxtFields, l, j, 6+i, 1, 1, 1, 0);
				}
			}
			DataManagementPanel.addComponent(txtFields, gblTxtFields, new JLabel(" "), 0, 7+h, view.getNumberOfColumns(), 1, 1, 1);
		}
		getThis().repaint();
		getThis().revalidate();

	}

	private ViewEditor getThis(){
		return this;
	}

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
}
