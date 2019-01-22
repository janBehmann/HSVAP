package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import models.SVMModel.KernelType;

import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;

import weka.core.Utils;
import data.BinaryTree;
import data.FeatureRole;
import data.SubView;
import data.Utilities;
import data.View;
import data.BinaryTree.TreeNodeAcceptor;
import data.BinaryTree.TreeNodeRepresentative;

/**
 * Ordinal SVM Model: Classifies using a binary tree, each nod is an SVM Model while the separate classes are in the leafes  
 * @author Micha Strauch
 */
public class OrdinalSVMModel extends TransformationModel implements ModelInterface, Serializable {
	private static final long serialVersionUID = 2540962811169101442L;
	/** Map with labels and there IDs */
	private final Map<Integer, String> labelMap;
	/** BinaryTree containing SVM Models */
	private BinaryTree<TreeNode> models;
	
	/**
	 * Learns a new Ordinal SVM Model
	 * @param labelTree Binary Tree with label IDs in the leaves
	 */
	public OrdinalSVMModel(final DoubleMatrix spektrenNorm, final DoubleMatrix label, final double c, final double kernel,
				final KernelType type, final Map<Integer, String> labelMap, final BinaryTree<Integer> labelTree) throws Exception {
		this(spektrenNorm, label, c, kernel, type, labelMap, new BinaryTreeSeparator(labelTree));
	}
	
	/**
	 * Learns a new Ordinal SVM Model
	 * @param separator Separator to separate labels in each knot into left or right group
	 */
	public OrdinalSVMModel(final DoubleMatrix spektrenNorm, final DoubleMatrix label, final double c, final double kernel, 
				final KernelType type, final Map<Integer, String> labelMap, final Separator separator) throws Exception {
		this.labelMap = labelMap;
			// Build BinaryTree
		this.models = new BinaryTree<TreeNode>(new TreeNode(new ArrayList<Integer>(labelMap.keySet())));
		this.models.execute(new TreeNodeAcceptor<TreeNode>() { // Create tree top-down
			@Override public void accept(BinaryTree<TreeNode> value) {
				if(value.getValue().values.size() < 2) return;
				int depth = value.getDepth();
				List<Integer> left = new ArrayList<Integer>(), right = new ArrayList<Integer>();
				for(Integer labelID : value.getValue().values){
					if(separator.isLeft(labelID, labelMap.get(labelID), depth))
						left.add(labelID);
					else
						right.add(labelID);
				}
				value.getValue().setLabel(left, right);
				
				if(!left.isEmpty())
					value.setLeft(new BinaryTree<TreeNode>(new TreeNode(left)));
				if(!right.isEmpty())
					value.setRight(new BinaryTree<TreeNode>(new TreeNode(right)));
			}
		}, BinaryTree.LEVEL_ORDER);
			// Create SVNModels
		this.models.execute(new TreeNodeAcceptor<TreeNode>() {
			@Override public void accept(BinaryTree<TreeNode> value) {
				if(value.isLeaf()) return;
				DoubleMatrix nodeSpektrenNorm = cutSpektrenNorm(value, spektrenNorm, label);
				DoubleMatrix nodeLabel = groupLabel(value, label);
				
				try{
					value.getValue().model = new SVMModel(nodeSpektrenNorm, nodeLabel, c, kernel, type, labelMap);
				} catch(Exception e){
					throw new RuntimeException("Exception while creating OrdinalSVMModel", e);
				}
			}
		}, BinaryTree.IN_ORDER);
	}
	
	/**
	 * Loads Ordinal SVM Model from file
	 * @param file File to load from
	 */
	public OrdinalSVMModel(File file) throws IOException, ClassNotFoundException {
		ObjectInputStream o = null;
		OrdinalSVMModel model = null;
		try{
			FileInputStream fileIS = new FileInputStream(file);
			o = new ObjectInputStream(fileIS);
			model = (OrdinalSVMModel) o.readObject();
		} finally { // Could be replaced with try-with-resources after Java 7
			if(o != null) o.close();
		}
		
		this.labelMap = model.getLabelMap();
		this.models = model.models;
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());
	}
	
	/**
	 * Returns the label map
	 */
	public Map<Integer, String> getLabelMap() {
		return labelMap;
	}
	
	@Override
	public View applyOn(View v, String path) throws SizeException, IllegalArgumentException, IOException, Error {
		v.setLabelMap(labelMap);
		return applyOn(v, path, models);
	}

	/**
	 * Applies Ordinal SVM Model on the Tree recursively top down (pre order)
	 */
	private View applyOn(View v, String path, BinaryTree<TreeNode> tree) throws SizeException, IllegalArgumentException, IOException, Error {
		View applied = tree.getValue().model.applyOn(v, path);
		SubView sub = null;
		if(tree.getLeft() != null && !tree.getLeft().isLeaf()){
			sub = new SubView(applied, tree.getValue().leftID);
			if(sub.getNumberOfExamples() > 0){
				View leftView = applyOn(sub, path, tree.getLeft());
				sub.apply(leftView);
			}
		}
		if(tree.getRight() != null && !tree.getRight().isLeaf()){
			if(sub == null)
				sub = new SubView(applied, tree.getValue().rightID);
			else
				sub.selectLabel(tree.getValue().rightID);
			if(sub.getNumberOfExamples() > 0){
				View rightView = applyOn(sub, path, tree.getRight());
				sub.apply(rightView);
			}
		}
		
		return sub == null ? applied : sub.getCompleteView();
	}

	@Override
	public String getMetaInfo(boolean detailed) {
		//TODO getMetaInfo
		String labelTree = models.toString(new TreeNodeRepresentative<TreeNode>() {
			@Override
			public String represent(BinaryTree<TreeNode> node){
				if(!node.isLeaf() || node.getValue() == null)
					return null;
				return node.getValue().values2string();
			}
		});
		String labels = "<p>";
		for(Entry<Integer, String> e : labelMap.entrySet()){
			String[] split = e.getValue().split("#");
			if(split.length == 2){
				labels += e.getKey() + ": ";
				labels += split[0].trim() + "<br>";
			}
		}
		String meta = "<b>Ordinal SVM Model</b><p>" + labelTree + labels;
		if(detailed){
			final StringBuilder detailedBuilder = new StringBuilder();
			String svnTree = models.toString(new TreeNodeRepresentative<TreeNode>() {
				int counter = 1;
				@Override
				public String represent(BinaryTree<TreeNode> node) {
					if(node.getValue() == null)
						return null;
					if(node.isLeaf())
						return "<i><font color='blue'>" + node.getValue().values2string() + "</font></i>";
					if(node.getValue().model == null)
						return null;
					detailedBuilder.append("<p><b>SVM_").append(counter).append("</b><br>");
					detailedBuilder.append(node.getValue().model.getMetaInfo(false).replace("\n", "<br>"));
					return "SVM_" + counter++;
				}
			});
			meta += "<p>" + svnTree + detailedBuilder;
		}
		return meta;
	}
	
	/**
	 * Returns only those rows from spektrenNorm, which belong to the given node
	 */
	private DoubleMatrix cutSpektrenNorm(BinaryTree<TreeNode> node, DoubleMatrix spektrenNorm, DoubleMatrix label){
		int[] rows = node.getValue().getRows(label);
		return spektrenNorm.getRows(rows);
	}
	
	/**
	 * Returns only those rows from label, which belong to the given node
	 */
	private DoubleMatrix groupLabel(BinaryTree<TreeNode> node, DoubleMatrix label){
		int[] rows = node.getValue().getRows(label);
		int leftID = node.getValue().leftID, rightID = node.getValue().rightID;
		DoubleMatrix grouped = label.getRows(rows);
		if(node.isLeaf()) return grouped;
		for(int i = 0; i < grouped.length; i++){
			if(node.getLeft() != null && node.getLeft().getValue().values.contains((int)grouped.data[i]))
				grouped.data[i] = leftID;
			else
				grouped.data[i] = rightID;
		}
		return grouped;
	}
	
	/**
	 * Wrapper class for the knots of the BinaryTree
	 * @author Micha Strauch
	 */
	private class TreeNode implements Serializable {
		private static final long serialVersionUID = -7465381721444434157L;
		/** SVM Model of this knot */
		public SVMModel model = null;
		/** Label IDs belonging to this knot */
		public final List<Integer> values; // Collection would be fine, but is not serializable
		/** ID of the left and right label group. Used in the SVM Model */ 
		public int leftID = 1, rightID = 2;
		/** Constructs a new TreeNode with given label IDs */
		public TreeNode(List<Integer> values){
			this.values = values;
		}
		/**
		 * Determines value for leftID and rightID, if children are leaves
		 * @param left List of label IDs from left child
		 * @param rightList of label IDs from right child
		 */
		public void setLabel(List<Integer> left, List<Integer> right){
			if(left.size() == 1){
				leftID = left.get(0);
				if(leftID == rightID)
					rightID++;
			}
			if(right.size() == 1){
				rightID = right.get(0);
				if(leftID == rightID)
					leftID++;
			}
		}
		/** Returns the indices of rows belonging to this Knot */ 
		public int[] getRows(DoubleMatrix label){
			int nRows = 0;
			for(double l : label.data)
				if(values.contains((int)l))
					nRows++;
			int[] rows = new int[nRows];
			nRows = 0;
			for(int i = 0; i < label.length; i++)
				if(values.contains((int)label.get(i)))
					rows[nRows++] = i;
			return rows;
		}
		/** Returns values joined by colon if multiple */
		public String values2string(){
			if(values.isEmpty())
				return "";
			if(values.size() == 1)
				return Integer.toString(values.get(0));
			StringBuilder builder = new StringBuilder();
			for(int val : values){
				if(builder.length() > 0)
					builder.append(':');
				builder.append(val);
			}
			return builder.toString();
		}
		@Override
		public String toString() {
			return leftID + ":" + rightID;
		}
	}
	
	/**
	 * Interface to determine in each knot if a label belongs to the left or right group
	 * @author Micha Strauch
	 */
	public static interface Separator {
		/**
		 * Returns true if the given label belongs to the left group, false otherwise.
		 * @param labelID Label ID to check for
		 * @param label Name of the label
		 * @param level Level in the Tree (root level = 0)
		 * @return True if the given label belongs to the left group
		 */
		public boolean isLeft(int labelID, String label, int level);
	}
	
	/**
	 * Implementation of the {@link Separator} for a BinaryTree with label ID in the leaves
	 * @author Micha Strauch
	 */
	private static class BinaryTreeSeparator implements Separator {
		/** Map with route direction for each label ID */
		private final Map<Integer, List<Boolean>> map;
		
		/** Constructor for the BinaryTreeSeparator */
		public BinaryTreeSeparator(BinaryTree<Integer> tree) {
			map = new TreeMap<Integer, List<Boolean>>();
			buildMap(tree, new LinkedList<Boolean>());
		}
		
		/**
		 * Recursively builds map top down (pre order)
		 * @param subtree Current node
		 * @param history Route from root to this node 
		 */
		private void buildMap(BinaryTree<Integer> subtree, LinkedList<Boolean> history){
			if(subtree.getValue() != null)
				map.put(subtree.getValue(), new ArrayList<Boolean>(history));
			if(subtree.getLeft() != null){
				history.add(true);
				buildMap(subtree.getLeft(), history);
				history.removeLast();
			}
			if(subtree.getRight() != null){
				history.add(false);
				buildMap(subtree.getRight(), history);
				history.removeLast();
			}
		}
		
		@Override
		public boolean isLeft(int labelID, String label, int level) {
			List<Boolean> list = map.get(labelID);
			if(list == null) throw new RuntimeException("Unknown Label ID: " + labelID);
			return list.get(level);
		}
	}

}
