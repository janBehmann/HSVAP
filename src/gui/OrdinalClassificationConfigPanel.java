package gui;

import gui.OrdinalClassificationPanel.KnotWrapper;
import gui.OrdinalClassificationPanel.NodeWrapper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import data.BinaryTree;
import net.miginfocom.swing.MigLayout;

public class OrdinalClassificationConfigPanel extends JPanel {
	private static final int LEFT = 0, RIGHT = 1;
	private static final long serialVersionUID = 202985890337330512L;
	private final JBinaryTree<NodeWrapper> tree;
	private final JTextField name;
	private final MultiComboBox<BinaryTree<NodeWrapper>> comboBoxes;
	private BinaryTree<NodeWrapper> currentNode;
	
	public OrdinalClassificationConfigPanel(JBinaryTree<NodeWrapper> tree){
		this.tree = tree;
		name = new JTextField();
		comboBoxes = new MultiComboBox<BinaryTree<NodeWrapper>>(2);
		@SuppressWarnings({"unchecked", "serial"})
		ListCellRenderer<BinaryTree<NodeWrapper>> renderer = new BasicComboBoxRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				try {
					value = ((BinaryTree<NodeWrapper>)value).getValue().getName();
				} catch(ClassCastException e){
				} catch(NullPointerException e){value = "- NONE -";}
				return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			}
		};
		comboBoxes.setRenderer(renderer);
		currentNode = null;
		JButton apply = new JButton("Apply");
		apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		
		setLayout(new MigLayout());
		add(new JLabel("Name:"));
		add(name, "wrap");
		add(new JLabel("Left: "));
		add(comboBoxes.getComboBox(LEFT), "wrap");
		add(new JLabel("Right: "));
		add(comboBoxes.getComboBox(RIGHT), "wrap");
		add(apply, "span");
		
		setVisible(false);
	}
	
	public void edit(BinaryTree<NodeWrapper> node){
		currentNode = node;
		if(node != null && node.getValue() instanceof KnotWrapper){
			// Build list of selectable elements
			Set<BinaryTree<NodeWrapper>> selectable = new HashSet<BinaryTree<NodeWrapper>>();
			selectable.addAll(tree.getTrees());
			if(node.getLeft() != null)
				selectable.add(node.getLeft());
			if(node.getRight() != null)
				selectable.add(node.getRight());
			// Can't select itself
			selectable.remove(node);
			// Can't select it's own root
			BinaryTree<NodeWrapper> root = node.getParent();
			while(root != null && root.getParent() != null)
				root = root.getParent();
			if(root != null)
				selectable.remove(root);
			
			comboBoxes.initialize(selectable, true, node.getLeft(), node.getRight());
			
			KnotWrapper knot = (KnotWrapper)node.getValue();
			name.setText(knot.getName());
		}
		setVisible(node != null && node.getValue() instanceof KnotWrapper);
	}
	
	public void apply(){
		if(currentNode != null && currentNode.getValue() instanceof KnotWrapper){
			KnotWrapper knot = (KnotWrapper)currentNode.getValue();
			knot.setName(name.getText());
			
			BinaryTree<NodeWrapper> selectedLeft = comboBoxes.getSelectedItem(LEFT);
			BinaryTree<NodeWrapper> selectedRight = comboBoxes.getSelectedItem(RIGHT);
			
			if(currentNode.getLeft() != selectedLeft){
				if(currentNode.getLeft() != null)
					tree.addTree(currentNode.getLeft());
				tree.removeTree(selectedLeft);
				currentNode.setLeft(selectedLeft);
			}
			if(currentNode.getRight() != selectedRight){
				if(currentNode.getRight() != null)
					tree.addTree(currentNode.getRight());
				tree.removeTree(selectedRight);
				currentNode.setRight(selectedRight);
			}
			tree.updateTree();
		}
	}
	
}
