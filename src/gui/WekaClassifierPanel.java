/**
 * 
 */
package gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import models.SVMModel;
import models.SVMModel.KernelType;
import net.miginfocom.swing.MigLayout;

import org.jblas.DoubleMatrix;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import wekatools.WekaTransformer;
import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.inmemory.DoubleArrayView;

/**
 * Panel for supervised Learning
 * Implements WEKA wrapper for SVM and cross validation
 * allows for grid search parameter optimization
 * 
 * SVM models are trained on the currently active train view and applied to all images which are referenced within the train view
 * To apply the SVM model to different images, the SVM model has to be saved and applied within the data management panel
 * @author croemer
 *
 */
public class WekaClassifierPanel extends JPanel {

	private static final long serialVersionUID = -578273101094899863L;
	private JComboBox<Object> kernelBox;
	//testFields for model Learning on main panel
	private JTextField inputTextC;
	private JTextField inputTextKernel;
	
	private MainGui master;

	
	private JTextField inputTextCMin;
	private JTextField inputTextCMax;
	private JTextField inputTextCInt;
	private JTextField inputTextCBase;
	private JCheckBox inputLogCInt;
	
	private JTextField inputTextKernelMin;
	private JTextField inputTextKernelMax;
	private JTextField inputTextKernelInt;
	private JTextField inputTextKernelBase;
	private JCheckBox inputLogKernelInt;
	
	//Needed for reference in action listener events
	public JPanel reference = this;



	/**
	 * Constructor
	 * sets parameters to default values
	 * @param main
	 * @param superLearnPanel panel for svm parameter selection
	 */

	public WekaClassifierPanel(MainGui main){
		super();		

		this.master = main;	
		this.init();
	}

	/**
	 * create GUI-Panel
	 */
	private void init() {
		this.setBorder(BorderFactory.createTitledBorder("C-SVM Parameter Selection"));
//		GridBagLayout gb =new GridBagLayout();
//		this.setLayout(gb);
//		GridBagConstraints c = new GridBagConstraints();
//
//		c.fill = GridBagConstraints.BOTH;
//		c.weightx = 70;
//		c.weighty = 70;
//		c.insets = new Insets(0,0,0,0);
		
		this.setLayout(new MigLayout("", "",""));

		JButton process = processButton();
		
//		c.gridx = 2;
//		c.gridy = 0;
//		c.gridwidth = 1;
//		c.gridheight = 1;
//		gb.setConstraints(getKernelBox(), c);
		
		this.add(getKernelBox(), "width 250:280");
		
//		c.gridx = 2;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		c.gridheight = 1;
		JButton gsb = new JButton ("<html>Parameter Grid<p/>Optimization</html>");
		gsb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				//only if trainView is selected and not empty
				if (master.getProject().getTrainView()!=null && master.getProject().getTrainView().getNumberOfExamples()>0)
					gridSearch();
				else
					JOptionPane.showMessageDialog(reference, "No training data selected. GridSearch will automatically learn a SVM model after parameter configurations.");
			}
		});
//		gb.setConstraints(gsb, c);
		this.add(gsb, "height 43, wrap");
		
//		c.gridx = 4;
//		c.gridy = 0;
//		c.gridwidth = 1;
//		c.gridheight = 1;
		inputTextC = new JTextField();			
		inputTextC.setEditable(true);
		inputTextC.setBorder(BorderFactory.createTitledBorder("Set C"));
//		gb.setConstraints(inputTextC, c);
		this.add(inputTextC, "width 100, split 2");
		
//		c.gridx = 4;
//		c.gridy = 1;
//		c.gridwidth = 1;
//		c.gridheight = 1;
		inputTextKernel = new JTextField();			
		inputTextKernel.setEditable(false);
		inputTextKernel.setVisible(false);
		inputTextKernel.setBorder(BorderFactory.createTitledBorder("Set Gamma for RBF Kernel"));
//		gb.setConstraints(inputTextKernel, c);
		this.add(this.inputTextKernel, "width 150:180, wrap 20");
		
		
//		c.gridx = 5;
//		c.gridy = 0;
//		c.gridwidth = 1;
//		c.gridheight = 2;
//		gb.setConstraints(process, c);
		this.add(process, "width 250:280, height 30:50");
//		this.setPreferredSize(new Dimension(5000,30));
		
		JButton ordinalClass = new JButton("<html><center>Ordinal<br>Classification</center></html>");
		ordinalClass.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame frame = new JFrame();
				frame.add(new OrdinalClassificationPanel(master));
				frame.pack();
				frame.setLocationRelativeTo(master);
				frame.setVisible(true);
			}
		});
		this.add(ordinalClass, "height 30:50");
		
//		//Following is hidden, as it is still not ready to use
//		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt F11"), "showOrdinalClassificationPanel");
//		getActionMap().put("showOrdinalClassificationPanel", new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				JFrame frame = new JFrame();
//				frame.add(new OrdinalClassificationPanel(master));
//				frame.pack();
//				frame.setLocationRelativeTo(master);
//				frame.setVisible(true);
//			}
//		});
		
	}
	
	/**
	 * Create Combox to select the Kernel Function
	 * @return
	 * @author modifiey by schmitter
	 */
	public JComboBox<Object> getKernelBox(){
		if(kernelBox==null){
			String[] kernelList ={"LINEAR", "RBF", "POLYNOMIAL"};
			kernelBox = new JComboBox<Object>(kernelList);
			kernelBox.setBorder(BorderFactory.createTitledBorder("Kernel Function"));
			// set linear kernel as default
			kernelBox.setSelectedIndex(0);
			
			kernelBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// Create default ranges and set textfield visible if needed
					switch(kernelBox.getSelectedIndex()){
					case 0:
						
						inputTextKernel.setEditable(false);
						inputTextKernel.setVisible(false);
						break;
						
					case 1:	
						inputTextKernel.setBorder(BorderFactory.createTitledBorder("Set Gamma for RBF Kernel"));
						inputTextKernel.setEditable(true);
						inputTextKernel.setVisible(true);
						break;
					case 2:
						inputTextKernel.setBorder(BorderFactory.createTitledBorder("Set Degree for Polynomial Kernel"));
						inputTextKernel.setEditable(true);						
						inputTextKernel.setVisible(true);
						break;
					}
				}
			});
		}
		return kernelBox;
	}
	
	
	
	/**
	 * Create Button to start the Support Vector Machine
	 * Checks if trainView exists and if it has data and starts SVM if everything is OK
	 * @return process-button
	 * @author croemer
	 */
	private JButton processButton(){
		JButton process=new JButton ("Classify");		
		process.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{				
				if (master.getProject().getTrainView()!=null && master.getProject().getTrainView().getNumberOfExamples()>0){
					String name = JOptionPane.showInputDialog("Enter name for Model");
					if(name != null)
						classifySVM(name);
				}
			}
		});
		return process;
	}



	/**
	 * trains the svm model with train data in train view
	 * predicts probabilities of all instances in the images of the train view afterwards
	 * 
	 * author: strauch (snippets from croemer)
	 */
	public void classifySVM(String modelName){
		try{
			this.master.getLog().info("Classification started");
			DoubleMatrix spektrenNorm =  Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumns(FeatureSelection.getNonSpecialFeatures(master.getProject().getTrainView()));
			DoubleMatrix label = Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumn(FeatureSelection.getLabel(master.getProject().getTrainView()));
			double c = Double.parseDouble(inputTextC.getText());
			double kernel = inputTextKernel.getText().isEmpty() ? 0 : Double.parseDouble(inputTextKernel.getText());
			KernelType type = KernelType.valueOf((String)kernelBox.getSelectedItem());
			SVMModel model = new SVMModel(spektrenNorm, label, c, kernel, type, master.getProject().getTrainView().getLabelMap());
			model.setName(modelName);
			
			//save model
			master.getProject().getModels().add(model);
			master.getDataManagementPanel().actualiseListen();
			//predict probabilities for all images in trainView
			if (this.master.getProject().getTrainView() != null){
				//find unique image references in train view
				HashSet<String> uniqueImages = new HashSet<String>();
				//for (String name: this.master.getProject().getTrainView().getImageReference()){
					uniqueImages.add(this.master.getProject().getActiveView().getViewName());
				//}

				//check all views in project whether they are used by the active train view
				//list needs to be copied as view list will be modified inside loop
				ArrayList<View> viewListCopy = new ArrayList<View>();
				for (int i  = 0; i<this.master.getProject().getViewList().size();i++)
					viewListCopy.add(this.master.getProject().getViewList().get(i));
				for( View v :viewListCopy){
					for (String name: uniqueImages){
						if (v.getViewName() == name){	
							this.master.getProject().add(model.applyOn(v, null));
							this.master.getDataManagementPanel().actualiseListen();
						}
					}
				}
			}
			this.master.getLog().info("Classification done");
		} catch (NumberFormatException nfe){
			JOptionPane.showMessageDialog(this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
			nfe.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * trains the svm model with train data in train view
	 * predicts probabilities of all instances in the images of the train view afterwards
	 * 
	 * author: croemer
	 */
	@Deprecated
	public void classifySVM_old(){ 
		
		
		
				
				try{
		this.master.getLog().info("Classification started");
		
		DoubleMatrix spektrenNorm =  Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumns(FeatureSelection.getNonSpecialFeatures(master.getProject().getTrainView()));
		DoubleMatrix label = Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumn(FeatureSelection.getLabel(master.getProject().getTrainView()));
		SMO classifier = svmProbModel(spektrenNorm, label);
		
		//predict probabilities for all images in trainView
		if (classifier!=null && this.master.getProject().getTrainView() != null){
			//find unique image references in train view
			HashSet<String> uniqueImages = new HashSet<String>();
			//for (String name: this.master.getProject().getTrainView().getImageReference()){
				uniqueImages.add(this.master.getProject().getActiveView().getViewName());
			//}

			//check all views in project whether they are used by the active train view
			//list needs to be copied as view list will be modified inside loop
			ArrayList<View> viewListCopy = new ArrayList<View>();
			for (int i  = 0; i<this.master.getProject().getViewList().size();i++)
				viewListCopy.add(this.master.getProject().getViewList().get(i)); 			
			for( View v :viewListCopy){
				for (String name: uniqueImages){
					if (v.getViewName() == name){	
						//get features and coordinates from the current view v for prediction
						DoubleMatrix features = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getNonSpecialFeatures(v));												
						DoubleMatrix coordinates = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getCoordinates(v));						
						//matrix for prediction results: x,y,label, probabilities
						double[][] result = new double[v.getNumberOfExamples()][3+this.master.getProject().getTrainView().getLabelMap().size()+features.columns];
						
						//transform features to instances, last column = nominal feature for label
						Instances inst = getInstances(features);	
						
						//for each sample of the current view v					   
						for (int i = 0; i <v.getNumberOfExamples(); i++){														
							
							
							//probabilities of each class for sample
							double[] prob = classifier.distributionForInstance(inst.instance(i));
							//coordinates of sample
							result[i][0] = coordinates.get(i,0);
							result[i][1] = coordinates.get(i,1);	
							//temp to get max prob
							double temp = 0;
							//prediction for label
							result[i][2] = 0;
							//for all probs get max prob for label prediction and write prob to results
							for (int j = 1; j<prob.length+1;j++){
								if (prob[j-1]>temp){
									temp = prob[j-1];
									result[i][2] = j;
								}
								result[i][j+2] = prob[j-1];		
							}
							System.out.println("1: "+features.getColumns());
							System.out.println("2: "+features.columns);
							System.out.println("3: "+prob.length+3+features.columns);
							System.out.println("4: "+prob.length);
							System.out.println("5: "+result.length);
							System.out.println("6: "+result[0].length);
							
							for (int j = prob.length+3; j<prob.length+3+features.columns;j++){																
								result[i][j] = features.get(i,j-prob.length-3); 										
							}
							
							
							
						}
						//apply results to new view
						String viewname = v.getViewName() + "_result";
						DoubleArrayView dav = new DoubleArrayView(result, viewname);
						dav.setFeatureRole(0, FeatureRole.X);
						dav.setFeatureRole(1, FeatureRole.Y);
						dav.setFeatureRole(2, FeatureRole.LABEL);
						dav.setLabelMap(v.getLabelMap());
						
						for (int i = 3; i<3+this.master.getProject().getTrainView().getLabelMap().size(); i++){
							dav.setFeatureRole(i, FeatureRole.PROB);
						}
						String[] descs = v.getFeatureDescriptors();
						int[] indizes = FeatureSelection.getNonSpecialFeatures(v);
						
						
						for (int  i = 3+this.master.getProject().getTrainView().getLabelMap().size(); i<dav.getNumberOfColumns();i++){
							dav.setFeatureRole(i, FeatureRole.FEATURE);
							dav.setFeatureDescriptor(i, descs[indizes[i-3-this.master.getProject().getTrainView().getLabelMap().size()]]);
						}
						
						
						
						
						this.master.getProject().add(dav);
						this.master.getDataManagementPanel().actualiseListen();
					}
						
				}
				
			}
			
		}
		this.master.getLog().info("Classification done");
				}catch (Exception e){
					e.printStackTrace();
				}
		
		
	}

	
	
	/**
	 * Initialized Text Field for Kernel Parameters input
	 * Used to set up the GridSearch Panel if the chosen option is either RBF or Polynomial Kernel
	 * @param inputPanel	GrisSearch Panel
	 * 
	 * author: croemer
	 */
	private void initializeKernelInput(JPanel inputPanel){
		//kernelmin
		this.inputTextKernelMin = new JTextField();			
		this.inputTextKernelMin.setEditable(true);
		if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF"))
			this.inputTextKernelMin.setBorder(BorderFactory.createTitledBorder("Set Gamma Min"));
		else 
			this.inputTextKernelMin.setBorder(BorderFactory.createTitledBorder("Set Degree Min"));
		inputPanel.add(this.inputTextKernelMin);
		
		//kernelmax
		this.inputTextKernelMax = new JTextField();			
		this.inputTextKernelMax.setEditable(true);
		if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF"))
			this.inputTextKernelMax.setBorder(BorderFactory.createTitledBorder("Set Gamma Max"));
		else 
			this.inputTextKernelMax.setBorder(BorderFactory.createTitledBorder("Set Degree Max"));
		inputPanel.add(this.inputTextKernelMax);

		//kernelinterval
		this.inputTextKernelInt = new JTextField();			
		this.inputTextKernelInt.setEditable(true);
		if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF"))
			this.inputTextKernelInt.setBorder(BorderFactory.createTitledBorder("Set Gamma Interval"));
		else
			this.inputTextKernelInt.setBorder(BorderFactory.createTitledBorder("Set Degree Interval"));
		this.inputTextKernelBase = new JTextField();
		this.inputTextKernelBase.setToolTipText("<html>Set &gt;1 for increasing intervals (e.g. 1.1 for slowly incr., 10 for quickly incr.).<p>Set &lt;1 for decreasing intervals (e.g. 0.9 for slowly decr., 0.1 for quickly decr.).</html>");
		this.inputTextKernelBase.setEditable(true);
		this.inputTextKernelBase.setEnabled(false);
		this.inputTextKernelBase.setBorder(BorderFactory.createTitledBorder("Set Base"));
		this.inputLogKernelInt = new JCheckBox("logarithmic");
		this.inputLogKernelInt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				inputTextKernelBase.setEnabled(inputLogKernelInt.isSelected());
				if(inputLogKernelInt.isSelected())
					inputTextKernelInt.setBorder(BorderFactory.createTitledBorder("Set number of grid points"));
				else
					if(((String)kernelBox.getSelectedItem()).equalsIgnoreCase("RBF"))
						inputTextKernelInt.setBorder(BorderFactory.createTitledBorder("Set Gamma Interval"));
					else
						inputTextKernelInt.setBorder(BorderFactory.createTitledBorder("Set Degree Interval"));
			}
		});
		//		inputPanel.add(this.inputTextKernelInt);
		JPanel inputKernelInt = new JPanel();
		inputKernelInt.setLayout(new BoxLayout(inputKernelInt, BoxLayout.Y_AXIS));
		inputKernelInt.add(this.inputLogKernelInt);
		inputKernelInt.add(this.inputTextKernelBase);
		inputKernelInt.add(this.inputTextKernelInt);
		inputPanel.add(inputKernelInt);
	}
	
	/**
	 * Initialized Text Field for C Parameters input
	 * Used to set up the GridSearch Panel 
	 * @param inputPanel	GrisSearch Panel
	 * 
	 * author: croemer
	 */
	private void initializeCInput(JPanel inputPanel){
		//cmin
		this.inputTextCMin = new JTextField();			
		this.inputTextCMin.setEditable(true);
		this.inputTextCMin.setBorder(BorderFactory.createTitledBorder("Set C Min"));
		inputPanel.add(this.inputTextCMin);
		
		//cmax
		this.inputTextCMax = new JTextField();			
		this.inputTextCMax.setEditable(true);
		this.inputTextCMax.setBorder(BorderFactory.createTitledBorder("Set C Max"));
		inputPanel.add(this.inputTextCMax);

		//interval
		this.inputTextCInt = new JTextField();			
		this.inputTextCInt.setEditable(true);
		this.inputTextCInt.setBorder(BorderFactory.createTitledBorder("Set C Interval"));
		this.inputTextCBase = new JTextField();
		this.inputTextCBase.setToolTipText("<html>Set &gt;1 for increasing intervals (e.g. 1.1 for slowly incr., 10 for quickly incr.).<p>Set &lt;1 for decreasing intervals (e.g. 0.9 for slowly decr., 0.1 for quickly decr.).</html>");
		this.inputTextCBase.setEditable(true);
		this.inputTextCBase.setEnabled(false);
		this.inputTextCBase.setBorder(BorderFactory.createTitledBorder("Set Base"));
		this.inputLogCInt = new JCheckBox("logarithmic");
		this.inputLogCInt.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				inputTextCBase.setEnabled(inputLogCInt.isSelected());
				if(inputLogCInt.isSelected())
					inputTextCInt.setBorder(BorderFactory.createTitledBorder("Set number of grid points"));
				else
					inputTextCInt.setBorder(BorderFactory.createTitledBorder("Set C Interval"));
			}
		});
		JPanel inputCInt = new JPanel();
		inputCInt.setLayout(new BoxLayout(inputCInt, BoxLayout.Y_AXIS));
		inputCInt.add(this.inputLogCInt);
		inputCInt.add(this.inputTextCBase);
		inputCInt.add(this.inputTextCInt);
		inputPanel.add(inputCInt);
	}
	
	/**
	 * Return parameter ranges for GridSearch Parameter Optimization for NonLinear Kernel
	 * calls a check for correctness of input parameters
	 * 
	 * @return HaspMap	HaspMap with all parameters for c and the chosen kernel
	 * @throws NumberFormatException	throws nfe if illegal characters are used within textfields
	 * 
	 * author: croemer
	 */
	private HashMap<String, Double> getNonLinearParameter() throws NumberFormatException {
		HashMap<String, Double> parameter = new HashMap<String, Double>();
		@SuppressWarnings("serial")
		JPanel paramSelectPanel = new JPanel(){

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(500, 250);
            }
		};
		
		if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF"))
			paramSelectPanel.setBorder(BorderFactory.createTitledBorder("RBF-SVM Parameter Selection"));
		else 
			paramSelectPanel.setBorder(BorderFactory.createTitledBorder("Polynomial-SVM Parameter Selection"));
		paramSelectPanel.setLayout(new GridLayout(2,3));
		
		initializeCInput(paramSelectPanel);
		initializeKernelInput(paramSelectPanel);
		int option = JOptionPane.showConfirmDialog(this, paramSelectPanel);
		if (option == JOptionPane.YES_OPTION){
			if (checkInputErrors(0)){
				parameter.put("CMIN", Double.valueOf(this.inputTextCMin.getText()));
				parameter.put("CMAX", Double.valueOf(this.inputTextCMax.getText()));
				parameter.put("CINT", Double.valueOf(this.inputTextCInt.getText()));
				if(inputLogCInt.isSelected()){
					parameter.put("CBASE", Double.valueOf(this.inputTextCBase.getText()));
				}
				parameter.put("KernelMIN", Double.valueOf(this.inputTextKernelMin.getText()));
				parameter.put("KernelMAX", Double.valueOf(this.inputTextKernelMax.getText()));
				parameter.put("KernelINT", Double.valueOf(this.inputTextKernelInt.getText()));
				if(inputLogKernelInt.isSelected()){
					parameter.put("KernelBASE", Double.valueOf(this.inputTextKernelBase.getText()));
				}
			}
		}
		return parameter;
	}
	

	/**
	 * Return parameter ranges for GridSearch Parameter Optimization for Linear Kernel
	 * calls a check for correctness of input parameters
	 * 
	 * @return HaspMap	HaspMap with all parameters for c 
	 * @throws NumberFormatException	throws nfe if illegal characters are used within textfields
	 * 
	 * author: croemer
	 */
	private HashMap<String, Double> getLinearParameter() throws NumberFormatException {
		HashMap<String, Double> parameter = new HashMap<String, Double>();
		
		//create Panel for parameter selection
	
		@SuppressWarnings("serial")
		JPanel paramSelectPanel = new JPanel(){
        

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(500, 150);
            }
        };		
		
		paramSelectPanel.setBorder(BorderFactory.createTitledBorder("C-SVM Parameter Selection"));
		paramSelectPanel.setLayout(new GridLayout(0,3));
		
		initializeCInput(paramSelectPanel);
		
		int option = JOptionPane.showConfirmDialog(this, paramSelectPanel);
		if (option == JOptionPane.YES_OPTION){
			if (checkInputErrors(0)){
				parameter.put("CMIN", Double.valueOf(this.inputTextCMin.getText()));
				parameter.put("CMAX", Double.valueOf(this.inputTextCMax.getText()));
				parameter.put("CINT", Double.valueOf(this.inputTextCInt.getText()));
				if(inputLogCInt.isSelected()){
					parameter.put("CBASE", Double.valueOf(this.inputTextCBase.getText()));
				}
			}
		}
		return parameter;
	}

	
	/**
	 * Checks whether the input parameters are legal for gridSearch
	 * @return boolean true if parameters are legal
	 * 
	 * param: cases	int = 0 for grid Search and 1 for modelLearning
	 * author: croemer
	 */
	private boolean checkInputErrors(int cases){
		boolean allow = false;
		
		if (cases==0){
			if (Double.valueOf(this.inputTextCMin.getText())<=0 || Double.valueOf(this.inputTextCMax.getText())<=0 || Double.valueOf(this.inputTextCInt.getText())<0 ){
				JOptionPane.showMessageDialog(this, "Only positive values for C and C Interval allowed.");
			} else if (Double.valueOf(this.inputTextCInt.getText())==0){
				JOptionPane.showMessageDialog(this, "An Interval of zero leeds to infinite loop.");
			} else if (Double.valueOf(this.inputTextCMin.getText())>Double.valueOf(this.inputTextCMax.getText())){
				JOptionPane.showMessageDialog(this, "CMIN greater as CMAX.");
			} else {
				if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF")){
					if (Double.valueOf(this.inputTextKernelMin.getText())<0 || Double.valueOf(this.inputTextKernelMax.getText())<0 || Double.valueOf(this.inputTextKernelInt.getText())<0 ){
						JOptionPane.showMessageDialog(this, "Only positive values for Gamma and Gamma Interval allowed.");
					} else if (Double.valueOf(this.inputTextKernelInt.getText())==0){
						JOptionPane.showMessageDialog(this, "An Interval of zero leeds to infinite loop.");
					} else if (Double.valueOf(this.inputTextKernelMin.getText())>Double.valueOf(this.inputTextKernelMax.getText())){
						JOptionPane.showMessageDialog(this, "GammaMIN greater as GammaMAX.");
					} else {
						allow = true;
					}	
				} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("Polynomial")){				
						if (Double.valueOf(this.inputTextKernelMin.getText())<0 || Double.valueOf(this.inputTextKernelMax.getText())<0 || Double.valueOf(this.inputTextKernelInt.getText())<0 ){
							JOptionPane.showMessageDialog(this, "Only positive values for Degree and Degree Interval allowed.");
						} else if (Double.valueOf(this.inputTextKernelInt.getText())==0){
							JOptionPane.showMessageDialog(this, "An Interval of zero leeds to infinite loop.");
						} else if (Double.valueOf(this.inputTextKernelMin.getText())>Double.valueOf(this.inputTextKernelMax.getText())){
							JOptionPane.showMessageDialog(this, "DegreeMIN greater as DegreeMAX.");
						} else if (Double.valueOf(this.inputTextKernelMin.getText()) == 0 || Double.valueOf(this.inputTextKernelMin.getText()) % 1 != 0 || Double.valueOf(this.inputTextKernelMax.getText())% 1 != 0 || Double.valueOf(this.inputTextKernelInt.getText())%1 != 0 ){
							JOptionPane.showMessageDialog(this, "Degree has to be a Natural Number .");
						} else {
							allow = true;
						}				
				} else {
					allow = true;
				}
			}
		} else if (cases == 1){
			if (Double.valueOf(this.inputTextC.getText())<=0 ){
				JOptionPane.showMessageDialog(this, "Only positive values for C and C Interval allowed.");					
			} else {
				if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF")){
					if (Double.valueOf(this.inputTextKernel.getText())<0){
						JOptionPane.showMessageDialog(this, "Only positive values for Gamma and Gamma Interval allowed.");					
					} else {
						allow = true;
					}	
				} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("Polynomial")){				
						if (Double.valueOf(this.inputTextKernel.getText())<0){
							JOptionPane.showMessageDialog(this, "Only positive values for Degree and Degree Interval allowed.");
						} else if (Double.valueOf(this.inputTextKernel.getText()) == 0 || Double.valueOf(this.inputTextKernel.getText()) % 1 != 0){
							JOptionPane.showMessageDialog(this, "Degree has to be a Natural Number .");
						} else {
							allow = true;
						}				
				} else {
					allow = true;
				}
			}
		}
		
		
		return allow;
	}

	/**
	 * does a gridSearch for optimal SVM parameters
	 * 
	 * ToDo: Selection of optimal parameters
	 * running: trains an svm model for all parameter combinations
	 * 
	 * changed: s7tischu
	 * logarithmic axis: going with constant interval over non-logarithmic axis from log_base(min) to log_base(max); exponentiate each value
	 * 
	 * note: half of the calculated interval is added to the upper bound of iteration, just if summation of #points times interval is slightly above the maximal; No additional iteration executed.
	 * 
	 */
	private void gridSearch(){
		try{
			DoubleMatrix spektrenNorm =  Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumns(FeatureSelection.getNonSpecialFeatures(master.getProject().getTrainView()));
			DoubleMatrix label = Utilities.materializeAsDoubleMatrix(master.getProject().getTrainView()).getColumn(FeatureSelection.getLabel(master.getProject().getTrainView()));
			double max = 0;
			double maxKernel = 0;
			
			
			//Linear Kernel
			if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("LINEAR")){
				double[] svmparam = new double[1];
				HashMap<String, Double> parameters = getLinearParameter();
				if (parameters.containsKey("CMIN")){

					if(inputLogCInt.isSelected()){	// C logarithmic


						if(parameters.get("CBASE")<1){
							double minTemp = parameters.get("CMAX");
							parameters.put("CMAX", parameters.get("CMIN"));
							parameters.put("CMIN", minTemp);
						}
						// calculate interval from number of points
						double interval  = ((Math.log(parameters.get("CMAX"))/Math.log(parameters.get("CBASE"))) - ((Math.log(parameters.get("CMIN"))/Math.log(parameters.get("CBASE")) ))) / (parameters.get("CINT")-1);

						// half of the calculated interval is added to the upper bound of iteration, just if summation of #points times interval is slightly above the maximal; No additional iteration executed.
						for (double i = Math.log(parameters.get("CMIN"))/Math.log(parameters.get("CBASE")); i<=Math.log(parameters.get("CMAX"))/Math.log(parameters.get("CBASE"))+interval/2; i = i + interval){
							double paramC;
							if(parameters.get("CBASE")<1){
								paramC = (parameters.get("CMAX")+parameters.get("CMIN"))-Math.pow(parameters.get("CBASE"),i);
							}else{
								paramC = Math.pow(parameters.get("CBASE"),i);
							}
							svmparam[0] = paramC;

							double temp = trainSVM(spektrenNorm, svmparam, label);
							if (temp > max){
								max = temp;
								this.inputTextC.setText(Double.toString(paramC));							
							}
						}	


					}else{		// C non-logarithmic
						for (double i = parameters.get("CMIN"); i<=parameters.get("CMAX"); i = i + parameters.get("CINT")){

							svmparam[0] = i;
							double temp = trainSVM(spektrenNorm, svmparam, label);
							if (temp > max){
								max = temp;
								this.inputTextC.setText(Double.toString(i));							
							}
						}
					}
//					System.out.println("Maximum Accuracy: "+max+" , C = "+this.inputTextC.getText());
					Logger.getLogger(MainGui.class.getName()).info("Maximum Accuracy: "+max+" , C = "+this.inputTextC.getText());
					double[] svmParam= new double[1];
					svmParam[0] = Double.parseDouble(this.inputTextC.getText());
					double[][] conMat=  trainSVM_confusion(spektrenNorm, svmparam, label);
					this.showConfusionMatrix(conMat);
				}

			}
			//Non-Linear Kernel
			if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF") || ((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("Polynomial")){
				HashMap<String, Double> parameters = getNonLinearParameter();
				
				if(parameters.isEmpty()) return; // Dialog canceled
				
				double[] svmparam = new double[2];
				
				if(inputLogKernelInt.isSelected()){		// Kernel logarithmic
					if(parameters.get("KernelBASE")<1){
						double minTemp = parameters.get("KernelMAX");
						parameters.put("KernelMAX", parameters.get("KernelMIN"));
						parameters.put("KernelMIN", minTemp);
					}
				}
								
				
				if(inputLogCInt.isSelected()){	// C logarithmic
					if(parameters.get("CBASE")<1){
						double minTemp = parameters.get("CMAX");
						parameters.put("CMAX", parameters.get("CMIN"));
						parameters.put("CMIN", minTemp);
					}
					
					
					// calculate interval from number of points
					double interval  = ((Math.log(parameters.get("CMAX"))/Math.log(parameters.get("CBASE"))) - ((Math.log(parameters.get("CMIN"))/Math.log(parameters.get("CBASE")) ))) / (parameters.get("CINT")-1);
					
					// half of the calculated interval is added to the upper bound of iteration, just if summation of #points times interval is slightly above the maximal; No additional iteration executed.
					for (double i = Math.log(parameters.get("CMIN"))/Math.log(parameters.get("CBASE")); i<=Math.log(parameters.get("CMAX"))/Math.log(parameters.get("CBASE"))+interval/2; i = i + interval){

						double paramC; 
						if(parameters.get("CBASE")<1){
							paramC = (parameters.get("CMAX")+parameters.get("CMIN"))-Math.pow(parameters.get("CBASE"),i);
						}else{
							paramC = Math.pow(parameters.get("CBASE"),i);
						}

						svmparam[0] = paramC;

						if(inputLogKernelInt.isSelected()){		// Kernel logarithmic
							
							// calculate interval from number of points
							double intervalKernel  = ((Math.log(parameters.get("KernelMAX"))/Math.log(parameters.get("KernelBASE"))) - ((Math.log(parameters.get("KernelMIN"))/Math.log(parameters.get("KernelBASE")) ))) / (parameters.get("KernelINT")-1);
							
							// half of the calculated interval is added to the upper bound of iteration, just if summation of #points times interval is slightly above the maximal; No additional iteration executed.
							for (double j = Math.log(parameters.get("KernelMIN"))/Math.log(parameters.get("KernelBASE")); j<=Math.log(parameters.get("KernelMAX"))/Math.log(parameters.get("KernelBASE")) +intervalKernel/2 ; j = j + intervalKernel){
								double paramKernel; 
								if(parameters.get("KernelBASE")<1){
									paramKernel = (parameters.get("KernelMAX")+parameters.get("KernelMIN"))-Math.pow(parameters.get("KernelBASE"),j);
								}else{
									paramKernel = Math.pow(parameters.get("KernelBASE"),j);
								}

								svmparam[1] = paramKernel;

								double temp = trainSVM(spektrenNorm, svmparam, label);
								if (temp > maxKernel){
									maxKernel = temp;
									this.inputTextC.setText(Double.toString(paramC));							
									this.inputTextKernel.setText(Double.toString(paramKernel));
								}
							}
						}else{		// Kernel non-logarithmic
							for (double j = parameters.get("KernelMIN"); j<=parameters.get("KernelMAX"); j = j + parameters.get("KernelINT")){
								double paramKernel = j;

								svmparam[1] = paramKernel;

								
								double temp = trainSVM(spektrenNorm, svmparam, label);
								if (temp > maxKernel){
									maxKernel = temp;
									this.inputTextC.setText(Double.toString(paramC));							
									this.inputTextKernel.setText(Double.toString(paramKernel));
								}
							}
						}
					}
				}else{	// C non-logarithmic
					for (double i = parameters.get("CMIN"); i<=parameters.get("CMAX"); i = i + parameters.get("CINT")){

						svmparam[0] = i;

						if(inputLogKernelInt.isSelected()){		// Kernel logarithmic
							
							// calculate interval from numer of points
							double intervalKernel  = ((Math.log(parameters.get("KernelMAX"))/Math.log(parameters.get("KernelBASE"))) - ((Math.log(parameters.get("KernelMIN"))/Math.log(parameters.get("KernelBASE")) ))) / (parameters.get("KernelINT")-1);
							
							// half of the calculated interval is added to the upper bound of iteration, just if summation of #points times interval is slightly above the maximal; No additional iteration executed.
							for (double j = Math.log(parameters.get("KernelMIN"))/Math.log(parameters.get("KernelBASE")); j<=Math.log(parameters.get("KernelMAX"))/Math.log(parameters.get("KernelBASE")) +intervalKernel/2 ; j = j + intervalKernel){
								double paramKernel; 
								if(parameters.get("KernelBASE")<1){
									paramKernel = (parameters.get("KernelMAX")+parameters.get("KernelMIN"))-Math.pow(parameters.get("KernelBASE"),j);
								}else{
									paramKernel = Math.pow(parameters.get("KernelBASE"),j);
								}

								svmparam[1] = paramKernel;

								double temp = trainSVM(spektrenNorm, svmparam, label);
								if (temp > maxKernel){
									maxKernel = temp;
									this.inputTextC.setText(Double.toString(i));							
									this.inputTextKernel.setText(Double.toString(paramKernel));
								}
							}
						}else{		// Kernel non-logarithmic
							
							for (double j = parameters.get("KernelMIN"); j<=parameters.get("KernelMAX"); j = j + parameters.get("KernelINT")){
								double paramKernel = j;

								svmparam[1] = paramKernel;

								double temp = trainSVM(spektrenNorm, svmparam, label);
								if (temp > maxKernel){
									maxKernel = temp;
									this.inputTextC.setText(Double.toString(i));							
									this.inputTextKernel.setText(Double.toString(paramKernel));
								}
							}
						}
					}
				}
//				System.out.println("Maximum Accuracy: "+maxKernel+" , C = "+this.inputTextC.getText()+" , Kernel = "+this.inputTextKernel.getText());
				Logger.getLogger(MainGui.class.getName()).info("Maximum Accuracy: "+maxKernel+" , C = "+this.inputTextC.getText()+" , Kernel = "+this.inputTextKernel.getText());
			}
		}catch ( NumberFormatException e ) {
			JOptionPane.showMessageDialog(this, "Illegal Parameters");
		}
		
		
		
		
	}
	
	/**
	 * creates instances for prediction, label is automatically generated and added on last position
	 * labels are needed for prediction in weka
	 * @param spektrenNorm features for classification
	 * @return Instances weka format for classifier
	 * @throws Exception if transformation to weka does not work
	 * 
	 * @author croemer
	 */
	@Deprecated
	private Instances getInstances(DoubleMatrix spektrenNorm) throws Exception {
		double[][] learning = new double[spektrenNorm.rows][spektrenNorm.columns+1];
		for (int i = 0; i<spektrenNorm.rows; i++){
			for (int j = 0; j<spektrenNorm.columns+1; j++){
				if (j<spektrenNorm.columns)
					learning[i][j] =  spektrenNorm.get(i,j);
				else
					learning[i][j] =  0;
			}
						
		}
		//transform double[][] to weka instances
		WekaTransformer wkt = new WekaTransformer();
		
		Instances inst = wkt.transformMatrix(learning);
		inst.setClassIndex(inst.numAttributes()-1);
		
		
		//convert numeric label to nominal label, needed for weka classifier
		NumericToNominal convert = new NumericToNominal();			
		String[] filter_options = new String[2];
		//defines the attribute to convert
		filter_options[0] = "-R";
		filter_options[1] = Integer.toString(inst.numAttributes());			
		convert.setOptions(filter_options);
		convert.setInputFormat(inst);			
		inst = Filter.useFilter(inst, convert);	
		
		
		
		return inst;
	}
	
	/**
	 * transforms the DoubleMatrices for non-special features and label to Instances for Weka processing
	 * @param spektrenNorm	DoubleMatrix with features
	 * @param label	DoubleMatrix with label vector
	 * @return	inst	Instance: data in Weka Format
	 * @throws Exception	Weka Exceptions for Instance errors
	 */
	@Deprecated
	private Instances getInstances(DoubleMatrix spektrenNorm, DoubleMatrix label) throws Exception{
		double[][] learning = new double[spektrenNorm.rows][spektrenNorm.columns+1];
		for (int i = 0; i<spektrenNorm.rows; i++){
			for (int j = 0; j<spektrenNorm.columns+1; j++){
				if (j<spektrenNorm.columns)
					learning[i][j] =  spektrenNorm.get(i,j);
				else
					learning[i][j] =  label.get(i);
			}
						
		}
		
		
		//transform double[][] to weka instances
		WekaTransformer wkt = new WekaTransformer();			
		Instances inst = wkt.transformMatrix(learning);
		//define attribute as class label
		inst.setClassIndex(inst.numAttributes()-1);
		
		//convert numeric label to nominal label, needed for weka classifier
		NumericToNominal convert = new NumericToNominal();			
		String[] filter_options = new String[2];
		//defines the attribute to convert
		filter_options[0] = "-R";
		filter_options[1] = Integer.toString(inst.numAttributes());			
		convert.setOptions(filter_options);
		convert.setInputFormat(inst);			
		inst = Filter.useFilter(inst, convert);	
		
		return inst;		
	}
	
	/**
	 * learns the SVM model for classification
	 * used in GridSearch
	 * 
	 * author: strauch
	 */
	private double trainSVM(DoubleMatrix spektrenNorm, double[] param, DoubleMatrix label){
		try{
			KernelType type = KernelType.valueOf((String)this.kernelBox.getSelectedItem());
			SVMModel model = new SVMModel(spektrenNorm, label, param[0], param.length > 1 ? param[1] : 0, type, master.getProject().getTrainView().getLabelMap());
			return model.getCrossValidation(10, new Random(1)).pctCorrect();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e){
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * learns the SVM model for classification
	 * used for CV for confusion matrix
	 * 
	 * author: strauch
	 */
	private double[][] trainSVM_confusion(DoubleMatrix spektrenNorm, double[] param, DoubleMatrix label){
		try{
			KernelType type = KernelType.valueOf((String)this.kernelBox.getSelectedItem());
			SVMModel model = new SVMModel(spektrenNorm, label, param[0], param.length > 1 ? param[1] : 0, type, master.getProject().getTrainView().getLabelMap());
			return model.getCrossValidation(10, new Random(1)).confusionMatrix();
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private void showConfusionMatrix (double[][] confMat){
		Object[] columnNames = this.master.getProject().getTrainView().getLabelMap().values().toArray();
		Object[] names = new String[columnNames.length+1];
		names[0]="";
		Object[][] objTable = new Object[confMat.length][confMat[0].length+1];
		for (int i=0; i<columnNames.length;i++){
			names[i+1]=(String) columnNames[i];
			objTable[i][0]=columnNames[i];
			System.out.println(names[i]);
		}
		
		for (int i=0; i<confMat.length;i++){
			for (int j=0; j<confMat[i].length;j++){
				objTable[i][j+1]= confMat[i][j];
			}
		}
		
		JTable table = new JTable(objTable,names);
		Frame frame = new JFrame("Confusion Matrix");
		frame.add(new JScrollPane(table));
		frame.pack();
		frame.setVisible(true);
	
	}
	
	/**
	 * learns the SVM model for classification
	 * used in GridSearch
	 * 
	 * author: croemer
	 * @param spektrenNorm
	 * @throws Exception 
	 */
	@Deprecated
	private double trainSVM_old(DoubleMatrix spektrenNorm, double[] param, DoubleMatrix label){
		try {
			//return value with percentage of correctly classified training samples
			double perc = 0;	
			//concatenate features and class label to singe double[][] learning
			
			Instances inst = getInstances(spektrenNorm, label);																								
			//Initialize LIBSVM classifier for WEKA wrapper											
			SMO classifier = new SMO();
					
			//define kernel options in dependence on kernel settings
			if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("LINEAR")){				
				String[] options = {"-C",Double.toString(param[0]),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E","1.0"};
				classifier.setOptions(options);  
			} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF")){				
				String[] options = {"-C",Double.toString(param[0]),"-M","1","-K","weka.classifiers.functions.supportVector.RBFKernel","-G",Double.toString(param[1])};
				classifier.setOptions(options);
			} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("POLYNOMIAL")){
				String[] options = {"-C",Double.toString(param[0]),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E",Double.toString(param[1])};				
				classifier.setOptions(options);
			}						
			classifier.buildClassifier(inst);	
			
			//x-fold cross validation
			Evaluation eval = new Evaluation(inst);
			eval.crossValidateModel(classifier, inst, 10, new Random(1) );		
			//calculate percentage of correctly classified samples
			perc = eval.pctCorrect();						
			//return percentage 
			return perc;
			
			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
			return 0;
		} catch (Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * learns the SVM model for classification
	 * used to learn the model with probabilites
	 * 
	 * author: croemer
	 * @param spektrenNorm
	 * @throws Exception 
	 */
	@Deprecated
	private SMO svmProbModel(DoubleMatrix spektrenNorm, DoubleMatrix label){			
		try {			
			
			Instances inst = getInstances(spektrenNorm, label);																								
			//Initialize LIBSVM classifier for WEKA wrapper	
			
			SMO classifier = new SMO();
			
			
					
			//if  input parameters are correct, else return null
			if (checkInputErrors(1)){
				//define kernel options in dependence on kernel settings
				if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("LINEAR")){
					String[] options = {"-C",this.inputTextC.getText(),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E","1.0"};					
					classifier.setOptions(options);
				} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("RBF")){
					String[] options = {"-C",this.inputTextC.getText(),"-M","1","-K","weka.classifiers.functions.supportVector.RBFKernel","-G",this.inputTextKernel.getText()};					
					classifier.setOptions(options);
				} else if(((String)this.kernelBox.getSelectedItem()).equalsIgnoreCase("POLYNOMIAL")){
					String[] options = {"-C",this.inputTextC.getText(),"-M","1","-K","weka.classifiers.functions.supportVector.PolyKernel","-E",this.inputTextKernel.getText()};					
					classifier.setOptions(options);
				}						
				classifier.buildClassifier(inst);	
				
				
				
				return classifier;
			} else {
				SMO classifiers = null;
				return classifiers;
			}
			
			
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
			SMO classifier = null;
			return classifier;
		} catch (Exception e){
			e.printStackTrace();
			SMO classifier = null;
			return classifier;
		}
	}


	

	

	

	



}
