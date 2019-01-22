package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ButtonUI;

import org.jblas.DoubleMatrix;

import models.OrdinalSVMModel;
import models.SVMModel;
import data.BinaryTree;
import data.BinaryTree.StringInterpreter;
import data.BinaryTree.TreeNodeAcceptor;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.operators.TrainView;

public class OrdinalClassificationPanel extends JPanel {
	private static final long serialVersionUID = -1850791872109016010L;
	private final MainGui master;
	private final OrdinalClassificationConfigPanel configPanel;
	private JBinaryTree<NodeWrapper> treePanel = null;
	private JTextField cText;
	private int classificationCount = 0;
	
	public OrdinalClassificationPanel(MainGui master){
		assert(master != null);
		this.master = master;
		this.configPanel = new OrdinalClassificationConfigPanel(getTreePanel());
		setLayout(new BorderLayout());
		ScrollPane scrPane = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		scrPane.add(getTreePanel());
		add(scrPane, BorderLayout.CENTER);
		add(configPanel, BorderLayout.EAST);
		add(createParameterPanel(), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(800, 600));
	}
	
	private JPanel createParameterPanel(){
		JPanel panel = new JPanel();
		
		JLabel cLabel = new JLabel("Set C:");
		cText = new JTextField("1", 5);
		JButton classify = new JButton("Classify");
		classify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (master.getProject().getTrainView()!=null && master.getProject().getTrainView().getNumberOfExamples()>0){
					String name = JOptionPane.showInputDialog("Enter name for Model");
					if(name != null)
						classifyOrdinalSVM(name);
				}
			}
		});
		
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));
		panel.add(cLabel);
		panel.add(cText);
		panel.add(classify);
		
		return panel;
	}
	
	/**
	 * trains the svm model with train data in train view
	 * predicts probabilities of all instances in the images of the train view afterwards
	 * 
	 * author: strauch
	 */
	private void classifyOrdinalSVM(String modelName){
		try{
			double c = Double.parseDouble(cText.getText());
			BinaryTree<NodeWrapper> root = null;
			for(BinaryTree<NodeWrapper> tree : treePanel.getTrees()){
				if(tree.getValue() == null) continue;
				root = tree;
				break;
			}
			if(root != null){
				master.getLog().info("Ordinal Classification started");
				// Create Model
				TrainView trainView = master.getProject().getTrainView();
				BinaryTree<Integer> labelTree = buildLabelTree(root);
				DoubleMatrix spektrenNorm =  Utilities.materializeAsDoubleMatrix(trainView).getColumns(FeatureSelection.getNonSpecialFeatures(trainView));
				DoubleMatrix label = Utilities.materializeAsDoubleMatrix(trainView).getColumn(FeatureSelection.getLabel(trainView));
				OrdinalSVMModel model = new OrdinalSVMModel(spektrenNorm, label, c, 0, SVMModel.KernelType.LINEAR, trainView.getLabelMap(), labelTree);
				model.setName(modelName);
				// Save Model
				master.getProject().getModels().add(model);
				master.getDataManagementPanel().actualiseListen();
				// Apply Model
				HashSet<String> uniqueImages = new HashSet<String>();
				uniqueImages.add(master.getProject().getActiveView().getViewName());
				ArrayList<View> viewListCopy = new ArrayList<View>(master.getProject().getViewList());
				for(View v : viewListCopy){
					for(String name : uniqueImages){
						if(v.getViewName() == name){
							master.getProject().add(model.applyOn(v, null));
							master.getDataManagementPanel().actualiseListen();
						}
					}
				}
				master.getLog().info("Ordinal Classification done");
			} else {
				JOptionPane.showMessageDialog(OrdinalClassificationPanel.this, "Tree not found. Did you build one?", "No tree found", JOptionPane.ERROR_MESSAGE);
			}
		} catch(NumberFormatException nfe){
			JOptionPane.showMessageDialog(OrdinalClassificationPanel.this, "Invalid SVM parameter values.", "SVM Paramter", JOptionPane.ERROR_MESSAGE);
			nfe.printStackTrace();
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private BinaryTree<Integer> buildLabelTree(BinaryTree<NodeWrapper> from){
		if(from == null) return null;
		BinaryTree<Integer> to = new BinaryTree<Integer>();
		if(from.getValue() instanceof LeafWrapper)
			to.setValue(((LeafWrapper) from.getValue()).getTypeID());
		else {
			if(from.getLeft() != null)
				to.setLeft(buildLabelTree(from.getLeft()));
			if(from.getRight() != null)
				to.setRight(buildLabelTree(from.getRight()));
		}
		return to;
	}
	
	private JBinaryTree<NodeWrapper> getTreePanel(){
		if(treePanel == null){
			JBinaryTree.TreeNodeRenderer<NodeWrapper> renderer = new JBinaryTree.TreeNodeRenderer<NodeWrapper>() {
				@Override
				public JComponent render(final BinaryTree<NodeWrapper> node, JComponent previousRender) {
					JButton but = (JButton) previousRender;
					if(but == null){
						but = new JButton();
						but.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								configPanel.edit(node);
							}
						});
					}
					String text = node.getValue() == null ? "<i>null</i>" : node.getValue().getName();
					if(node.getValue() instanceof KnotWrapper)
						text = "<b>" + text + "</b>";
					but.setText("<html>" + text + "</html>");
					but.setForeground(node.getValue().getColor());
					but.setBorderPainted(false);
					but.setContentAreaFilled(false);
					but.setOpaque(false);
					return but;
				}
			};
			
			treePanel = new JBinaryTree<NodeWrapper>(renderer);
			View v;
			if((v = master.getProject().getTrainView()) != null)
				for(int i : v.getLabelMap().keySet())
					treePanel.addTree(new BinaryTree<NodeWrapper>(new LeafWrapper(i)));
			treePanel.setNodeMoveEnabled(true);
			treePanel.updateTree();
			treePanel.layoutTree();
			
			JMenuItem fromString = new JMenuItem("From String");
			fromString.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String definition = JOptionPane.showInputDialog(OrdinalClassificationPanel.this, "Please enter tree definition. <i>Example: {1;{3;2}}</i>");
					if(definition != null)
						buildTree(definition);
				}
			});
			JMenuItem addClassification = new JMenuItem("Add Classification");
			addClassification.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					addClassification();
				}
			});
			JMenuItem layout = new JMenuItem("Do Layout");
			layout.addActionListener(new ActionListener() { // I do miss lambda expression...
				@Override
				public void actionPerformed(ActionEvent e) {
					treePanel.layoutTree();
				}
			});
			JPopupMenu popup = new JPopupMenu();
			popup.add(fromString);
			popup.addSeparator();
			popup.add(addClassification);
			popup.add(layout);
			treePanel.setComponentPopupMenu(popup);
		}
		return treePanel;
	}
	
	public void buildTree(String definition){ // Eg: {3;{1;2}}
		// Disable config panel
		configPanel.edit(null);
		// Find classes
		Set<BinaryTree<NodeWrapper>> trees = treePanel.getTrees();
		final Map<Integer, BinaryTree<NodeWrapper>> leaves = new HashMap<Integer, BinaryTree<NodeWrapper>>();
		for(BinaryTree<NodeWrapper> tree : trees){
			tree.execute(new TreeNodeAcceptor<NodeWrapper>() {
				@Override
				public void accept(BinaryTree<NodeWrapper> value) {
					if(value.getValue() instanceof LeafWrapper){
						LeafWrapper leaf = (LeafWrapper)value.getValue();
						leaves.put(leaf.typeID, value);
					}
				}
			}, BinaryTree.IN_ORDER);
		}
		// Build tree by definition
		BinaryTree<NodeWrapper> tree = BinaryTree.fromString(definition, new StringInterpreter<NodeWrapper>() {
			int classID = 0;
			@Override
			public NodeWrapper getValue(String value) {
				try{
					int id = Integer.parseInt(value);
					BinaryTree<NodeWrapper> leaf = leaves.remove(id);
					if(leaf != null)
						return leaf.getValue();
				} catch(NumberFormatException nfe){}
				if(value.isEmpty())
					value = "Classification " + ++classID;
				return new KnotWrapper(value);
			}
		});
		// Add tree and left over classes
		trees.clear();
		trees.add(tree);
		if(!leaves.isEmpty())
			trees.addAll(leaves.values());
		// Refresh tree panel
		treePanel.updateTree();
		treePanel.layoutTree();
		treePanel.repaint();
	}
	
	public void updateLeaves(){
		JBinaryTree<NodeWrapper> forest = getTreePanel();
		View v = master.getProject().getTrainView();
		if(v == null) return;
		final Collection<Integer> ids = new HashSet<Integer>(v.getLabelMap().keySet());
		for(BinaryTree<NodeWrapper> tree : forest.getTrees()){
			tree.execute(new BinaryTree.TreeNodeAcceptor<NodeWrapper>() {
				@Override
				public void accept(BinaryTree<NodeWrapper> node) {
					if(node.getValue() instanceof LeafWrapper){
						LeafWrapper leaf = (LeafWrapper) node.getValue();
						if(!ids.remove(leaf.getTypeID()))
								leaf.name = "<stroke>" + leaf.name + "</stroke>";
					}
				}
				
			}, BinaryTree.IN_ORDER);
		}
		for(int id : ids)
			forest.addTree(new BinaryTree<NodeWrapper>(new LeafWrapper(id)));
		forest.updateTree();
	}
	
	public void addClassification(){
		getTreePanel().addTree(new BinaryTree<NodeWrapper>(new KnotWrapper("Classification " + ++classificationCount)));
		getTreePanel().updateTree();
	}
	
	interface NodeWrapper {
		public String getName();
		public Color getColor();
	}
	
	class LeafWrapper implements NodeWrapper {
		private int typeID;
		private String name;
		private Color color = Color.BLACK;
		
		private LeafWrapper(int typeID){
			setTypeID(typeID);
		}
		
		void setTypeID(int typeID){
			View v = master.getProject().getTrainView();
			if(v == null || !v.getLabelMap().containsKey(typeID)) throw new RuntimeException("Unkown ID"); //TODO Better approach 
			this.typeID = typeID;
			String val = v.getLabelMap().get(typeID);
			name = val.substring(0, val.length() - 8);
			color = Color.decode(val.substring(val.length() - 7));
		}
		
		public int getTypeID(){return typeID;}
		public String getName(){return name;}
		public Color getColor(){return color;}
	}
	
	class KnotWrapper implements NodeWrapper {
		private Object name;
		
		public KnotWrapper(String name) {
			this.name = name;
		}
		
		public void setName(String name){
			this.name = name;
		}
		
		public String getName(){return name == null ? "null" : name.toString();}
		public Color getColor(){return Color.BLACK;}
	}
	
}
