package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;

import data.BinaryTree;

/**
 * Displays a {@link BinaryTree} using a {@link TreeNodeRenderer}
 * @author Micha Strauch
 * @param <T> Type of values in binary tree
 */
public class JBinaryTree<T> extends JPanel {
	private static final long serialVersionUID = -2038175630276431131L;
	/** Map containing generated component for each tree node */
	private final Map<BinaryTree<T>, JComponent> componentMap;
	/** Minimal Gap between components in  x/y direction */
	private int yGap = 25, xGap = 10;
	/** Creates components for TreeNodes */
	private TreeNodeRenderer<T> renderer;
	/** Tree to display */
	private Set<BinaryTree<T>> trees;
	/** Color for the lines */
	private Color lineColor = Color.BLUE;
	/** Whether nodes can be moved with the mouse */
	private boolean enableNodeMove = false;
	
	/**
	 * Creates a JBinaryTree of the given trees using the given renderer
	 * @param renderer Renderer to create components for tree nodes
	 * @param tree Tree to display
	 */
	public JBinaryTree(TreeNodeRenderer<T> renderer){
		setLayout(null);
		this.componentMap = new HashMap<BinaryTree<T>, JComponent>();
		this.trees = new HashSet<BinaryTree<T>>();
		setRenderer(renderer);
		for(BinaryTree<T> tree : trees)
			addTree(tree);
	}
	
	public TreeNodeRenderer<T> getRenderer() {
		return renderer;
	}
	
	public void setRenderer(TreeNodeRenderer<T> renderer) {
		if(renderer == null) throw new NullPointerException("The renderer must not be null");
		this.renderer = renderer;
	}
	
	public Set<BinaryTree<T>> getTrees() {
		return trees;
	}
	
	public void addTree(BinaryTree<T> tree){
		if(tree == null) throw new NullPointerException();
		trees.add(tree);
	}
	
	public boolean removeTree(BinaryTree<T> tree){
		return trees.remove(tree);
	}
	
	/** Line color between nodes */
	public Color getLineColor() {
		return lineColor;
	}
	
	/** Sets the line color between nodes (null := no line) */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
	
	/** Whether nodes can be moved with the mouse */
	public boolean isNodeMoveEnabled(){
		return enableNodeMove;
	}
	
	/** If set to true nodes can be moved using the mouse */
	public void setNodeMoveEnabled(boolean enableNodeMove) {
		this.enableNodeMove = enableNodeMove;
	}
	
	/** Minimal horizontal gap between nodes */
	public int getXGap() {
		return xGap;
	}
	
	/** Sets minimal horizontal gap between nodes */
	public void setXGap(int xGap) {
		this.xGap = xGap;
	}
	
	/** Minimal vertical gap between nodes */
	public int getYGap() {
		return yGap;
	}
	
	/** Sets minimal vertical gap between nodes */
	public void setYGap(int yGap) {
		this.yGap = yGap;
	}
	
	/** Updates components using the renderer */
	public void updateTree(){
		final Map<BinaryTree<T>, JComponent> oldMap = new HashMap<BinaryTree<T>, JComponent>(componentMap);
		componentMap.clear();
		for(BinaryTree<T> tree : trees){
			tree.execute(new BinaryTree.TreeNodeAcceptor<T>() {
				@Override
				public void accept(final BinaryTree<T> value) {
					JComponent oldComp = oldMap.get(value);
					JComponent newComp = renderer.render(value, oldComp);
					newComp.setSize(newComp.getPreferredSize());
					if(oldComp != null)
						newComp.setLocation(oldComp.getLocation());
					if(oldComp != newComp){
						MouseAdapter nodeMover = new MouseAdapter() {
							Point p = null;
							Set<JComponent> comps = new HashSet<JComponent>(); 
							
							public void mousePressed(MouseEvent e) {
								if(!enableNodeMove) return;
								p = e.getLocationOnScreen();
								comps.clear();
								value.execute(new BinaryTree.TreeNodeAcceptor<T>() {
									@Override
									public void accept(BinaryTree<T> value) {
										JComponent comp = componentMap.get(value);
										if(comp != null) comps.add(comp);
									}
								}, BinaryTree.IN_ORDER);
							}
							
							public void mouseReleased(MouseEvent e) {
								p = null;
							};
							
							public void mouseDragged(MouseEvent e) {
								if(p == null) return;
								Point pNew = e.getLocationOnScreen();
								int dx = pNew.x - p.x, dy = pNew.y - p.y;
								p = pNew;
								for(JComponent comp : comps){
									Point p = comp.getLocation();
									comp.setLocation(p.x + dx, p.y + dy);
								}
								updateSize();
							};
						}; 
						newComp.addMouseListener(nodeMover);
						newComp.addMouseMotionListener(nodeMover);
					}
					componentMap.put(value, newComp);
				}
			}, BinaryTree.IN_ORDER);
		}
		
			// Remove old components
		Collection<JComponent> comps = new HashSet<JComponent>(oldMap.values());
		comps.removeAll(componentMap.values());
		for(JComponent comp : comps)
			remove(comp);
		
			// Add new components
		comps.clear();
		comps.addAll(componentMap.values());
		comps.removeAll(oldMap.values());
		for(JComponent comp : comps)
			add(comp);
		
			// Update size
		updateSize();
	}
	
	/** Updates minimum and preferred size of this component */
	public void updateSize(){
		int xMax = 0, yMax = 0;
		if(getInsets() != null){
			xMax = getInsets().left;
			yMax = getInsets().top;
		}
		for(JComponent comp : componentMap.values()){
			if(xMax < comp.getX() + comp.getWidth())
				xMax = comp.getX() + comp.getWidth();
			if(yMax < comp.getY() + comp.getHeight())
				yMax = comp.getY() + comp.getHeight();
		}
		if(getInsets() != null){
			xMax += getInsets().right;
			yMax += getInsets().bottom;
		}
		setMinimumSize(new Dimension(xMax, yMax));
		setPreferredSize(new Dimension(xMax, yMax)); // Not sure if necessary, also overrides exterior changes
		
		revalidate();
		repaint();
	}
	
	/** Lays out components in a tree shape. Call {@link #repaint()} afterwards to display. */
	public void layoutTree(){
		// Using final arrays so they can be changed inside sub class
		final int[] yMax = {getInsets() == null ? 0 : getInsets().top};
		final int[] xMin = {0};
		for(BinaryTree<T> tree : trees){
			final int[] heights = new int[tree.getHeight()], y = new int[tree.getHeight()];
				// Get row heights
			tree.execute(new BinaryTree.TreeNodeAcceptor<T>() {
				@Override
				public void accept(BinaryTree<T> value) {
					JComponent comp = componentMap.get(value);
					if(comp == null) return;
					int depth = value.getDepth();
					if(heights[depth] < comp.getHeight())
						heights[depth] = comp.getHeight();
				}
			}, BinaryTree.IN_ORDER);
			y[0] = yMax[0] + heights[0];
			for(int i = 1; i < y.length; i++)
				y[i] = y[i-1] + yGap + heights[i];
			yMax[0] = y[y.length - 1];
			
				// Build tree in Post-Order
			tree.execute(new BinaryTree.TreeNodeAcceptor<T>(){
				int xMax = getInsets() == null ? 0 : getInsets().left;
				@Override
				public void accept(BinaryTree<T> value) {
					JComponent comp = componentMap.get(value);
					if(comp == null) return;
					int x = xMax, depth = value.getDepth();
					JComponent lChild = componentMap.get(value.getLeft());
					JComponent rChild = componentMap.get(value.getRight());
					if(lChild != null && rChild != null)
						x = (lChild.getX() + rChild.getX() + (lChild.getWidth() + rChild.getWidth()) / 2) / 2;
					else if(lChild != null)
						x = lChild.getX() + lChild.getWidth();
					else if(rChild != null)
						x = rChild.getX();
					else
						x = xMax;
					if(lChild != null || rChild != null)
						x -= comp.getWidth() / 2;
					comp.setLocation(x, y[depth]);
					if(x < xMin[0])
						xMin[0] = x;
					x += comp.getWidth() + xGap;
					if(xMax < x)
						xMax = x;
				}
			}, BinaryTree.POST_ORDER);
			
			y[0] += yGap;
		}
		
		// Take care no component has negative x
		if(xMin[0] < 0){
			int deltaX = -xMin[0];
			for(JComponent comp : componentMap.values())
				comp.setLocation(comp.getX() + deltaX, comp.getY());
		}
		
			// Update size
		updateSize();
	}
	
	@Override
	public void doLayout() {
		if(getLayout() != null)
			super.doLayout();
		else if(!enableNodeMove)
			layoutTree();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(Entry<BinaryTree<T>, JComponent> entry : componentMap.entrySet())
			drawLine(g, entry.getValue(), componentMap.get(entry.getKey().getParent()));
	}
	
	/** Draws a line from the center of c1 to the center of c2 on g */
	private void drawLine(Graphics g, JComponent c1, JComponent c2){
		if(lineColor == null || c1 == null || c2 == null) return; 
		int x1 = c1.getX() + c1.getWidth() / 2, y1 = c1.getY() + c1.getHeight() / 2;
		int x2 = c2.getX() + c2.getWidth() / 2, y2 = c2.getY() + c2.getHeight() / 2;
		g.setColor(lineColor);
		g.drawLine(x1, y1, x2, y2);
	}
	
	/**
	 * Renderer to create JComponents for the tree nodes
	 * @author Micha Strauch
	 * @param <T> Value type stored in the tree
	 */
	public static interface TreeNodeRenderer<T> {
		/**
		 * Returns a JComponent for the given tree node
		 * @param node Tree node to create component for
		 * @param previousRender Previous created component, can be reused to reduce storage on heap
		 * @return Created (or reused) component
		 */
		public JComponent render(BinaryTree<T> node, JComponent previousRender);
	}
}
