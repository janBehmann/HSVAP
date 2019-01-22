package data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a binary tree
 * @author Micha Strauch
 * @param <T> Value type  
 */
public class BinaryTree<T> implements Cloneable, Serializable {
	private static final long serialVersionUID = 3477245917342527125L;
	/** Order for iterating over tree, see {@link #execute(TreeNodeAcceptor, int)} */
	public static final int PRE_ORDER = 0, IN_ORDER = 1, POST_ORDER = 2, LEVEL_ORDER = 3;
	/** Parent, left and right node */
	private BinaryTree<T> parent, left, right;
	/** Value of this node */
	private T value;
	
	/** Default constructor for empty node */
	public BinaryTree(){
		this(null);
	}
	
	/**
	 * Constructor for node with predefined value
	 * @param value Value of this node
	 */
	public BinaryTree(T value){
		parent = (left = (right = null));
		this.value = value;
	}
	
	/** Set left branch */
	public void setLeft(BinaryTree<T> left) {
		if(this.left != null)
			this.left.setParent(null);
		this.left = left;
		if(left != null)
			left.setParent(this);
	}
	
	/** Returns the left branch */
	public BinaryTree<T> getLeft() {
		return left;
	}
	
	/** Set right branch */
	public void setRight(BinaryTree<T> right) {
		if(this.right != null)
			this.right.setParent(null);
		this.right = right;
		if(right != null)
			right.setParent(this);
	}
	
	/** Returns the right branch */
	public BinaryTree<T> getRight() {
		return right;
	}
	
	/** Set parent node */
	private void setParent(BinaryTree<T> parent) {
		this.parent = parent;
	}
	
	/** Returns the parent node */
	public BinaryTree<T> getParent() {
		return parent;
	}
	
	/** Set value */
	public void setValue(T value) {
		this.value = value;
	}
	
	/** Returns the value */
	public T getValue() {
		return value;
	}
	
	/** Returns the depth of this node */
	public int getDepth(){
		if(parent == null) return 0;
		return parent.getDepth() + 1;
	}
	
	/** Returns the height of this tree */
	public int getHeight(){
		int lHeight = 1, rHeight = 1;
		if(left != null) lHeight = left.getHeight() + 1;
		if(right != null) rHeight = right.getHeight() + 1;
		return lHeight < rHeight ? rHeight : lHeight;
	}
	
	/**
	 * Returns true if this node is a left node.<br>
	 * A false return does not mean it's a right node,
	 * you can check that via {@link #isRight()}! 
	 */
	public boolean isLeft(){
		return parent != null && parent.left == this;
	}
	
	/**
	 * Returns true if this node is a right node.<br>
	 * A false return does not mean it's a left node,
	 * you can check that via {@link #isLeft()}! 
	 */
	public boolean isRight(){
		return parent != null && parent.right == this;
	}
	
	/** Returns true if this node has no parent */
	public boolean isRoot(){
		return parent == null;
	}
	
	/** Returns true if this node has no children */
	public boolean isLeaf(){
		return left == null && right == null;
	}
	
	/**
	 * Executes a method with all nodes in this tree in the given order.<br>
	 * Possible values for mode: {@link #PRE_ORDER}, {@link #IN_ORDER}, {@link #POST_ORDER}, {@link #LEVEL_ORDER}
	 * @param acceptor TreeNodeAcceptor to accept all nodes
	 * @param mode Order to give nodes to acceptor
	 */
	public void execute(TreeNodeAcceptor<T> acceptor, int mode){
		if(mode == LEVEL_ORDER){
			Queue<BinaryTree<T>> queue = new LinkedList<BinaryTree<T>>();
			queue.add(this);
			BinaryTree<T> current;
			while(!queue.isEmpty()){
				current = queue.poll();
				acceptor.accept(current);
				if(current.left != null) queue.add(current.left);
				if(current.right != null) queue.add(current.right);
			}
		} else {
			if(mode == PRE_ORDER) acceptor.accept(this);
			if(left != null) left.execute(acceptor, mode);
			if(mode == IN_ORDER) acceptor.accept(this);
			if(right != null) right.execute(acceptor, mode);
			if(mode == POST_ORDER) acceptor.accept(this);
		}
	}
	
	/**
	 * Adds all values to the given list, according to the given order.<br>
	 * Possible values for mode: {@link #PRE_ORDER}, {@link #IN_ORDER}, {@link #POST_ORDER}, {@link #LEVEL_ORDER}
	 * @param list List to add values to
	 * @param mode Order to add values to list
	 * @return Given list
	 */
	public List<T> add(final List<T> list, int mode){
//		execute(t -> list.add(t.value), mode);
		execute(new TreeNodeAcceptor<T>() {
			@Override
			public void accept(BinaryTree<T> t) {
				list.add(t.value);
			}
		}, mode);
		return list;
	}
	
	@Override
	public BinaryTree<T> clone() {
		BinaryTree<T> clone = new BinaryTree<T>(value);
		if(left != null) clone.setLeft(left.clone());
		if(right != null) clone.setRight(right.clone());
		return clone;
	}
	
	@Override
	public String toString() {
		return toString(new TreeNodeRepresentative<T>() {
			@Override
			public String represent(BinaryTree<T> node) {
				return node.getValue() == null ? null : node.getValue().toString();
			}
		});
	}
	
	/** Lets you determine the string value of the given node, instead of simply using getValue().toString() */  
	public String toString(TreeNodeRepresentative<T> representative){
		return toString(new StringBuilder(), representative).toString();
	}
	
	/** Similar to {@link #toString()} but adds the string to the given StringBuilder */
	public StringBuilder toString(StringBuilder builder, TreeNodeRepresentative<T> representative){
		String sValue = representative.represent(this);
		if(sValue != null && !sValue.isEmpty()){
			sValue = sValue.replaceAll("([{}\\\\;])", "\\\\$1");
			builder.append(sValue);
		}
		if(left != null || right != null){
			builder.append('{');
			if(left != null) left.toString(builder, representative);
			builder.append(';');
			if(right != null) right.toString(builder, representative);
			builder.append('}');
		}
		return builder;
	}
	
	/**
	 * Creates a BinaryTree from the given String using the given StringInterpreter for the values<br>
	 * <i>With a properly implemented interpreter, BinaryTree.fromString(someBinaryTree.toString(), someInterpreter) should return a copy of someBinaryTree</i> 
	 * @param parse String to construct BinaryTree from
	 * @param interpreter Interpreter for converting Strings to the wanted value
	 * @return Constructed BinaryTree
	 */
	public static <M> BinaryTree<M> fromString(String parse, StringInterpreter<M> interpreter){
		BinaryTree<M> tree = new BinaryTree<M>();
			// Find first occurrence of an unescaped { and the last occurrence of an unescaped }
		int start = -1, end = -1;
		boolean escape = false;
		for(int i = 0; i < parse.length(); i++){
			if(escape){
				escape = false;
				continue;
			}
			char c = parse.charAt(i);
			if(c == '{' && start < 0) start = i;
			if(c == '}') end = i;
			if(c == '\\') escape = true;
		}
		if(start < 0) start = parse.length();
		if(end < 0) end = parse.length();
			// Get node value using interpreter
		tree.setValue(interpreter.getValue(parse.substring(0, start).replaceAll("\\\\([{}\\\\;])", "$1")));
			// Handle children
		if(start < end){
			parse = parse.substring(start+1, end);
				// Find occurrence of unescaped ;
			int split = -1, deep = 0;
			escape = false;
			for(int i = 0; i < parse.length(); i++){
				if(escape){
					escape = false;
					continue;
				}
				char c = parse.charAt(i);
				if(c == '{') deep++;
				if(c == '}') deep--;
				if(c == ';' && deep == 0) split = i;
				if(c == '\\') escape = true;
			}
				// Determine children recursively
			if(split >= 0){
				tree.setLeft(BinaryTree.fromString(parse.substring(0, split), interpreter));
				tree.setRight(BinaryTree.fromString(parse.substring(split + 1), interpreter));
			}
		}
		return tree;
	}
	
	/**
	 * Prints the tree into an <i>ASCII-Art</i> using the given representative
	 * @param representative Converts all TreeNodes into Strings
	 * @return Created <i>ASCII-Art</i>
	 */
	public String print(TreeNodeRepresentative<T> representative){
		//TODO Print ASCII Art
		return toString();
	}
	
	/**
	 * Interface for parsing String values to the wanted type  
	 * @author Micha Strauch
	 * @param <M> Type the BinaryTree will be later of
	 */
	public static interface StringInterpreter<M>{
		/** Returns the value for the given string<br>
		 	<i>An empty string could be a null value</i>*/
		public M getValue(String value);
	}
	
	/**
	 * Interface to accept TreeNodes, used for iterating through a tree.<br>
	 * <i>From Java 1.8 onwards it could be replaced using a {@link java.util.function.Consumer}</i>
	 * @author Micha Strauch
	 * @param <M> Value types stored in tree
	 */
	public static interface TreeNodeAcceptor<M>{
		/**
		 * Will be called on each TreeNode, while iterating through the tree.
		 * See {@link BinaryTree#execute(TreeNodeAcceptor, int)} for more information.
		 * @param value Current tree node
		 */
		public void accept(BinaryTree<M> value);
	}
	
	/**
	 * Interface to represent TreeNodes as a String, so that the tree can be printed into a String representation<br>
	 * <i>From Java 1.8 onwards it could be replaced using a {@link java.util.function.Function}</i>
	 * @author Micha Strauch
	 * @param <M> Value types stored in tree
	 */
	public static interface TreeNodeRepresentative<M> {
		/**
		 * Will be called on each TreeNode, while iterating through the tree.
		 * See {@link BinaryTree#print(TreeNodeRepresentative)} for more information.
		 * @param value Current tree node
		 * @return String representation of the given node 
		 */
		public String represent(BinaryTree<M> value);
	}
	
}
