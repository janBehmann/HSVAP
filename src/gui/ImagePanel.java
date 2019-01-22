package gui;

import imageprocessing.ImageSelection;
import imageprocessing.Mode;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jblas.DoubleMatrix;

import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.inmemory.DoubleMatrixView;
import data.operators.FeatureFilter;
import data.operators.MergeFeaturesView;
import edu.mines.jtk.awt.ColorMap;



public class ImagePanel extends JPanel implements MouseListener,
MouseMotionListener, MouseWheelListener {

	private static final long serialVersionUID = -204817714436935578L;
	private BufferedImage image;
	private File fileLocation;
	private LinkedList<Point> dots;
	// private LabelMat labelMat;
	protected JScrollPane parent;
	private MainGui master;
	private boolean locked = false;
	private HashMap<Point, Color> dotsColor;
	private HashMap<Point, Integer> index;
	private double maxSpectrum;
	private double minSpectrum;
	/**
	 * scaling factor for zooming
	 * @author schmitter
	 */
	private double scale;

	/**
	 * selection shapes
	 * @author Till
	 */
	private Shape selShape;
	private ArrayList<Point> lassoPoints;
	private Point firstPoint;
	private ArrayList<Point> polyPoints;
	private Point polyPoint;
	private Point upperLeftPoint=new Point(0,0);

	/**
	 * @param newParent
	 * @param master
	 * @author modified by schmitter 
	 */
	public ImagePanel(JScrollPane newParent, MainGui master) {
		super();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		// Zoom with mouse wheel
		this.addMouseWheelListener(this);
		dots = new LinkedList<Point>();
		dotsColor = new HashMap<Point, Color>();
		newParent.setViewportBorder( BorderFactory.createLineBorder(
				this.getBackground(), 10));
		parent = newParent;
		this.master = master;
		this.makeParentHeader();
		//		parent.setSize(this.getPreferredSize());
		//		this.setSize(this.getPreferredSize());
		master.getAlPanel().getImageViewPanel().setSize(this.getPreferredSize());
		maxSpectrum = Double.MIN_VALUE;
		minSpectrum = Double.MAX_VALUE;
		this.scale=1;
	}


	/**
	 * @param location
	 * @param newParent
	 * @param master
	 * @author modified by schmitter 
	 */
	public ImagePanel(File location, JScrollPane newParent, MainGui master) {
		super();
		this.master = master;
		try {
			image = ImageIO.read(location);
		} catch (IOException ex) {
			// handle exception...
			System.out.println("Could not load image");
			ex.printStackTrace();
		}
		fileLocation = location;
		this.setBackground(Color.LIGHT_GRAY);
		newParent.setViewportBorder(BorderFactory.createLineBorder(
				this.getBackground(), 10));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		// Zoom with mouse wheel
		this.addMouseWheelListener(this);
		dots = new LinkedList<Point>();
		dotsColor = new HashMap<Point, Color>();
		parent = newParent;
		this.makeParentHeader();
		locked = false;
		this.scale=1;

	}



	/**
	 * @author modified by schmitter 
	 */
	@Override
	public Dimension getPreferredSize() {
		if (image == null)
			return new Dimension();
		Dimension dim = new Dimension();
		dim.height = (int) (this.image.getHeight() * this.scale);
		dim.width = (int) (this.image.getWidth() * this.scale);
		return dim;
	}

	
	/**
	 * Estimates the size of the plotted dots
	 * @return size of the dots
	 * @author schmitter
	 */
	public int getOvalSize(){
		int ovalSize = (int) Math.round(7/this.scale);
		if (ovalSize<1)
			ovalSize = 1;
		if (ovalSize>7)
			ovalSize=7;
		return ovalSize;
	}
	
	
	/**
	 * @author modified by schmitter 
	 */
	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setBackground(Color.BLACK);
		g2.setColor(Color.red);
		// Scaling image 
		g2.scale(this.scale, this.scale);
		g2.drawImage(image, 0, 0, null); // see javadoc for more info on the
		// plot labeled data -> dots
		int ovalSize = this.getOvalSize();
		for (Point dot : dots) {
			Point dotTrans= new Point(dot);
			dotTrans.translate(-(int)upperLeftPoint.getX(), -(int)upperLeftPoint.getY());
			g2.setColor(dotsColor.get( dot));
			// Scaling of labeled points 
			g2.fillOval( dotTrans.x -(ovalSize/2), dotTrans.y-(ovalSize/2), ovalSize, ovalSize);
		}
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.translate(-(int)upperLeftPoint.getX(), -(int)upperLeftPoint.getY());
		
		
		//draw current selected shape
		if(selShape!=null){
			Shape selShapeTransform = affineTransform.createTransformedShape(selShape);		
			Color fillCl = new Color(0f,0f,1f,.2f );
			g2.setColor(fillCl);
			g2.fill(selShapeTransform);
			g2.setColor(Color.BLUE);
			g2.setStroke(new BasicStroke((float) (1/this.scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
			g2.draw(selShapeTransform);
		}



		//draw Lasso
		if(master.getDataPanel().getImageProcessingPanel().getSelectionMode() == Mode.LASSO){
			if(getLassoPoints().size()>1){
				g2.setColor(new Color(139,0,0));
				g2.setStroke(new BasicStroke((float) (1/this.scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				g2.drawLine(firstPoint.x-(int)upperLeftPoint.getX(), firstPoint.y-(int)upperLeftPoint.getY(), getLassoPoints().get(0).x-(int)upperLeftPoint.getX(), getLassoPoints().get(0).y-(int)upperLeftPoint.getY());
				for (int i = 1; i<getLassoPoints().size();i++){
					g2.drawLine(getLassoPoints().get(i-1).x-(int)upperLeftPoint.getX(), getLassoPoints().get(i-1).y-(int)upperLeftPoint.getY(), getLassoPoints().get(i).x-(int)upperLeftPoint.getX(), getLassoPoints().get(i).y-(int)upperLeftPoint.getY());
				}
			}
		}

		//draw Polygon
		if(master.getDataPanel().getImageProcessingPanel().getSelectionMode() == Mode.POLY){
			if(getPolyPoints().size()>1){
				g2.setColor(new Color(139,0,0));
				g2.setStroke(new BasicStroke((float) (1/this.scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
				//						g2.drawLine(getPolyPoints().get(0).x, getPolyPoints().get(0).y, getPolyPoints().get(0).x, getPolyPoints().get(0).y);
				for (int i = 1; i<getPolyPoints().size();i++){
					g2.drawLine(getPolyPoints().get(i-1).x-(int)upperLeftPoint.getX(), getPolyPoints().get(i-1).y-(int)upperLeftPoint.getY(), getPolyPoints().get(i).x-(int)upperLeftPoint.getX(), getPolyPoints().get(i).y-(int)upperLeftPoint.getY());
				}
			}
			if(getPolyPoints().size()>0){
				if(polyPoint!=null){
					g2.setColor(new Color(139,0,0));
					g2.setStroke(new BasicStroke((float) (1/this.scale), BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
					g2.drawLine(getPolyPoints().get(getPolyPoints().size()-1).x-(int)upperLeftPoint.getX(), getPolyPoints().get(getPolyPoints().size()-1).y-(int)upperLeftPoint.getY(), polyPoint.x-(int)upperLeftPoint.getX(), polyPoint.y-(int)upperLeftPoint.getY());
				}
			}	
		}
		g2.setBackground(Color.BLACK);
	}

	/**
	 * @return
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}

	/**
	 * @return
	 */
	public File getFileLocation() {
		return fileLocation;
	}

	/**
	 * @param fileLocation
	 */
	public void setFileLocation(File fileLocation) {
		this.fileLocation = fileLocation;
	}

	/**
	 * @author modified by schmitter 
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// Scaling the image point
		Point p = transformPosition(e);
		
		if (!ImageProcessingPanel.editMode && !locked && master.getDataPanel().getClassBox().getItemCount() != 0 && index.get( p)!=null) {
			master.getDataPanel().plotSpectra( p);
			dots.add( p);
			
			// Find Color
			int indexLabel=master.getDataPanel().findColorIndex(master.getDataPanel().getClassBox().getSelectedItem().toString());
			this.master.getDataPanel().reloadCoordinates();

			dotsColor.put( p,Utilities.getColor(master.getProject().getTrainView().getLabelMap().get(indexLabel)));
			this.getParent().getParent().repaint();
			this.repaint();
		}

		
		
		/**
		 * Polygon selection is regulated here. Add every click location to list of points.
		 * If the distence to first point of polygon is smaller than a certain amount of pixels (minimum 2)
		 * the polygon ring/circle will be closed
		 */
		if(ImageProcessingPanel.editMode){
			
			if(SwingUtilities.isLeftMouseButton(e)){
				
				Mode md = master.getDataPanel().getImageProcessingPanel().getSelectionMode();
				//				Shape r;
				if(md==Mode.POLY){
					if (this.image.getWidth()>= p.getX() & this.image.getHeight()>= p.getY()){

						getPolyPoints().add(p);
						double objektfang = 10/this.scale;
						if(objektfang<2){
							objektfang = 2;
						}
						if(p.distance(getPolyPoints().get(0))<objektfang && getPolyPoints().size()>2){
							Point pp = (Point) getPolyPoints().get(0).clone();
							getPolyPoints().add(pp);
							int npoints = getPolyPoints().size();
							int[] xpoints = new int[npoints];
							int[] ypoints = new int[npoints];;
							for(int i = 0; i<npoints;i++){
								xpoints[i] = getPolyPoints().get(i).x;
								ypoints[i] = getPolyPoints().get(i).y;
							}
							Polygon r = new Polygon(xpoints, ypoints, npoints);
							selShape = r;

							getPolyPoints().removeAll(getPolyPoints());
						}
					}
					//Activate Buttons if shape exists
					master.getDataPanel().getImageProcessingPanel().getNewViewBtn().setEnabled(!(selShape == null));
					master.getDataPanel().getImageProcessingPanel().getRemoveBtn().setEnabled(!(selShape == null));
					
				}
			}else if(SwingUtilities.isRightMouseButton(e)){   // Reset im mose clicked right
				master.getDataPanel().getImageProcessingPanel().clearSelections();
			}
		}
		this.repaint();
		this.parent.repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		Cursor crossHair = new Cursor(Cursor.CROSSHAIR_CURSOR);
		setCursor(crossHair);
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(normalCursor);

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TU NICHTS
		Point p = transformPosition(e);
		/**
		 * The first point for all dragging selections is set
		 */
		if (this.image.getWidth()> p.getX() & this.image.getHeight()> p.getY()){
			firstPoint =new Point(p);
		}else{
			firstPoint = null;
		}
		if(ImageProcessingPanel.editMode){
			if(firstPoint!=null){
				Mode md = master.getDataPanel().getImageProcessingPanel().getSelectionMode();

				if(md==Mode.LASSO){
					selShape = null;
				}
			}
			this.repaint();
			this.parent.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!locked) {
			// dots.add(e.getPoint());
			// this.getParent().getParent().repaint();
		}
		Point p = transformPosition(e);
		
		
//		 Lasso selection is regulated here. 
//		 If the distance to first point of drag line is smaller than a certain amount of pixels (minimum 2)
//		 the circle will be closed
		 
		if(ImageProcessingPanel.editMode){
			if(firstPoint!=null){
				Mode md = master.getDataPanel().getImageProcessingPanel().getSelectionMode();

				if(md==Mode.LASSO){
					double objektfang = 10/this.scale;
					if(objektfang<2){
						objektfang = 2;
					}
					if(p.distance(firstPoint)<objektfang){
						Point pp = (Point) firstPoint.clone();
						getLassoPoints().add(pp);
						int npoints = getLassoPoints().size();
						int[] xpoints = new int[npoints];
						int[] ypoints = new int[npoints];;
						for(int i = 0; i<npoints;i++){
							xpoints[i] = getLassoPoints().get(i).x;
							ypoints[i] = getLassoPoints().get(i).y;
						}
						Polygon r = new Polygon(xpoints, ypoints, npoints);
						selShape = r;
						getLassoPoints().removeAll(getLassoPoints());
					}else{
						getLassoPoints().removeAll(getLassoPoints());
					}
				}
			}
			
			//Reset Buttons if lasso shape hasn't been closed
			master.getDataPanel().getImageProcessingPanel().getNewViewBtn().setEnabled(!(selShape == null));
			master.getDataPanel().getImageProcessingPanel().getRemoveBtn().setEnabled(!(selShape == null));
			
		}
		this.repaint();
		this.parent.repaint();
	}
	/**
	 * Scaling the image if the mouse wheel is rotated.
	 * Decreasing is not possible/implemented
	 * @author schmitter 
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double precise = e.getPreciseWheelRotation();
		// Old Cursor Position
		Point oldCursor = new Point( e.getX(), e.getY());  	
		// Change the scaling factor
		if (precise < 0)
			this.zoomIn( oldCursor);

		if (precise > 0)
			this.zoomOut( oldCursor);
			
		this.repaint();
		this.parent.repaint();
	}
	
	
	/**
	 * Zooming in to the current cursor position
	 * @param oldCursor Position
	 * @author schmitter
	 */
	public void zoomIn( Point oldCursor){
		// Change Viewport center around cursor position
		Rectangle oldView = parent.getViewport().getViewRect();
		Point dP = new Point ( oldCursor.x - oldView.x, oldCursor.y - oldView.y);
		this.scale = this.scale * 1.1;
		if (this.scale >= 30)
			this.scale = this.scale/1.1;
		else{
			// Estimate and set position of new viewport
			int xPos = (int) Math.round(dP.x*0.1 + 1.1*oldView.x);
			int yPos = (int) Math.round(dP.y*0.1 + 1.1*oldView.y);
			Point newViewPos = new Point(xPos, yPos);
			parent.getViewport().setViewPosition(newViewPos);

			// Fire MouseEvent
			master.getPaintPanel().dispatchEvent( new MouseEvent(this,
					MouseEvent.MOUSE_MOVED,
					System.currentTimeMillis(),
					MouseEvent.NOBUTTON,
					(int) Math.round(oldCursor.x*1.1), 
					(int) Math.round(oldCursor.y*1.1),  
					0,
					false));
		}
	}
	/**
	 * Zooming out to the current cursor position
	 * @param oldCursor Position
	 * @author schmitter
	 */
	public void zoomOut ( Point oldCursor){
		// Change Viewport center around cursor position
		Rectangle oldView = parent.getViewport().getViewRect();
		Point dP = new Point ( oldCursor.x - oldView.x, oldCursor.y - oldView.y);
		this.scale = this.scale * 0.9;
		// Minimal Descrease is 1
		if (this.scale < 1)
			this.scale = 1;
		else {
			// Estimate and set position of new viewport			
			int xPos = (int) Math.round(dP.x*-0.1 + 0.9*oldView.x);
			int yPos = (int) Math.round(dP.y*-0.1 + 0.9*oldView.y);
			Point newViewPos = new Point(xPos, yPos);
			parent.getViewport().setViewPosition(newViewPos);

			// Fire MouseEvent
			master.getPaintPanel().dispatchEvent( new MouseEvent(this,
					MouseEvent.MOUSE_MOVED,
					System.currentTimeMillis(),
					MouseEvent.NOBUTTON,
					(int) Math.round(oldCursor.x*0.9), 
					(int) Math.round(oldCursor.y*0.9),  
					0,
					false));
		}
	}
	
	
	


	/**
	 * @return the dots
	 */
	public LinkedList<Point> getDots() {
		return dots;
	}

	/**
	 * @param dots
	 *            the dots to set
	 */
	public void setDots(LinkedList<Point> dots) {
		this.dots = dots;
	}

	/**
	 * @author modified by schmitter 
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = transformPosition(e);
		if (!locked) {			
			if (this.master.getProject().getActiveView()!=null){
				// Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
				// Add 1 because array starts at 0 and image at 1
				if(FeatureSelection.getNonSpecialFeatures(this.master.getProject().getActiveView()).length>0)
					master.getDataPanel().plotCurrentSpectra(p);						
				// Update Textfield with current cursor position
				if (this.image.getWidth()> p.getX() & this.image.getHeight()> p.getY()){
					master.getAlPanel().getxCoordinate().setText( String.valueOf( (int) p.getX() +1));
					master.getAlPanel().getyCoordinate().setText( String.valueOf( (int) p.getY() +1));
				}
				// if outside the image
				else{
					master.getAlPanel().getxCoordinate().setText( "0");
					master.getAlPanel().getyCoordinate().setText( "0");
				}
			}
		}

		/**
		 * draw line to current cursor location
		 */
		if(ImageProcessingPanel.editMode){
			Mode md = master.getDataPanel().getImageProcessingPanel().getSelectionMode();

			if(md==Mode.POLY){
				if (this.image.getWidth()>= p.getX() & this.image.getHeight()>= p.getY()){
					polyPoint = new Point( p);
				}else{
					polyPoint = null;
				}
			}
		}
		this.repaint();
		this.parent.repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		Point p = transformPosition(e);
		/**
		 * Lasso selection is regulated here. 
		 * Add every drag location to list of points.
		 */
		if(ImageProcessingPanel.editMode){
			
			if(firstPoint!=null){
				Mode md = master.getDataPanel().getImageProcessingPanel().getSelectionMode();
				//				Shape r;
				if(md==Mode.RECT){
					Rectangle r = ImageSelection.selectRectangle(this.image.getWidth(), this.image.getHeight(), this.scale, p, firstPoint);

					selShape = r;

				}else if(md==Mode.LASSO){
					if (this.image.getWidth()>= p.getX() & this.image.getHeight()>= p.getY()){
						getLassoPoints().add(p);
					} 
				}else{
				}

				
			}
			this.repaint();
			this.parent.repaint();
		}

	}



	/**
	 * Function to display a single band in a greyscale image
	 * @param v hsImage
	 * @param spektrum bandnumber
	 */
	public void displaySingleBand(View v, int spektrum) {
		if (spektrum == -1){
			return;
		}
		//Crop image
		double[][] coordinates = Utilities.materializeFeatures(v, FeatureSelection.getCoordinates(v));
		DoubleMatrix cMins = new DoubleMatrix(coordinates).columnMins();
		upperLeftPoint=new Point((int)cMins.get(0), (int)cMins.get(1));
		v= cropView(v);

		double[] data = v.materializeFeature(FeatureSelection.getFeatures(v)[spektrum]);
		double dataMaxValue = Double.MIN_VALUE;
		double dataMinValue = Double.MAX_VALUE;
		
		DoubleMatrix matrix = new DoubleMatrix(data);
		double std= Math.sqrt(matrix.sub(matrix.mean()).mul(matrix.sub(matrix.mean())).get(0));
		System.out.println("Min: "+matrix.min()+"  ,Max: "+matrix.max()+"  ,Mean: "+matrix.mean()+"  ,Std: "+std);
		
		for (int i = 0; i < data.length; i++) {
			if (dataMaxValue < data[i])
				dataMaxValue = data[i];
			if (dataMinValue > data[i])
				dataMinValue = data[i];
		}

		int[] dataSpektrum = new int[data.length];
		double dataMaxValue1 = 0;
		double dataMinValue1 = 300;
		for (int i = 0; i < data.length; i++) {
			dataSpektrum[i] = (int) Math.round((data[i]-dataMinValue) / (dataMaxValue-dataMinValue) * 255);
			//######## Removed: Threshold for greyscale 
			//			if (dataSpektrum[i] < 16)
			//				dataSpektrum[i] = 255 - dataSpektrum[i];
			if (dataMaxValue1 < dataSpektrum[i])
				dataMaxValue1 = dataSpektrum[i];
			if (dataMinValue1 > dataSpektrum[i])
				dataMinValue1 = dataSpektrum[i];
			dataSpektrum[i] = (255 << 24) | (dataSpektrum[i] << 16) | (dataSpektrum[i] << 8)
					| dataSpektrum[i];
		}

		double[] xCoord = v.materializeFeature(FeatureSelection.getXCoordinate(v));
		double[] yCoord = v.materializeFeature(FeatureSelection.getYCoordinate(v));
		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;
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

		v.setXDimension((int) xMax);
		v.setYDimension((int) yMax);
		
		// Size of the image
		int[] dataSpektrumImage = new int[v.getXDimension()* v.getYDimension()];
		// Build the image
		
		for(int i=0; i<v.getNumberOfExamples(); i++){
			// Fill array with colors, where data exists
			dataSpektrumImage[ ((int)yCoord[i]-1) * v.getXDimension() + (int)xCoord[i]-1] = dataSpektrum[i];
		}
		// add white to the remaining data
		for (int i=0; i<dataSpektrumImage.length; i++){
			if (dataSpektrumImage[i] == 0)
				dataSpektrumImage[i] = Color.WHITE.getRGB();
			// TODO due to selection of the colormap: white is not available 
		}

		ColorMap colorMap = new ColorMap( dataMinValue, dataMaxValue, master.getAlPanel().getColorModel());

		MemoryImageSource source =new MemoryImageSource((int) xMax,(int) yMax, colorMap.getColorModel(), dataSpektrumImage, 0, (int) xMax);
		Image img = createImage(source);
		image = ImagePanel.toBufferedImage(img);
		this.getPreferredSize();
		locked = false;

		this.parent.repaint();
		this.repaint();
	}

	/**
	 * @author schmitter
	 */
	public void displayLabel(View v) {
		// querry if labels are available
		if (FeatureSelection.getLabel(v) >= 0){
			
			//Crop image
			double[][] coordinates = Utilities.materializeFeatures(v, FeatureSelection.getCoordinates(v));
			DoubleMatrix cMins = new DoubleMatrix(coordinates).columnMins();
			upperLeftPoint=new Point((int)cMins.get(0), (int)cMins.get(1));
			v= cropView(v);			
			
			// get the labels and the corresponding positions
			double[] data = v.materializeFeature( FeatureSelection.getLabel( v));
			double[] xCoord = v.materializeFeature(FeatureSelection.getXCoordinate(v));
			double[] yCoord = v.materializeFeature(FeatureSelection.getYCoordinate(v));
			// get numer of clustert for the colormap
			int nrCluster = 0;
			for (int i=0; i<data.length; i++){
				if (data[i]>nrCluster)
					nrCluster = (int) data[i];
			}

			// Size of the image
			int[] dataSpektrumImage = new int[master.getProject().getActiveView().getXDimension()* master.getProject().getActiveView().getYDimension()];
			
			Map<Integer,String> labels = master.getProject().getActiveView().getLabelMap();		
			MemoryImageSource source;
			// If labelmap is available
			if (labels != null && !labels.isEmpty()){
				for(int i=0; i<xCoord.length; i++){
					// Parse Hex back to Integer
//					System.out.println(labels);
//					System.out.println(labels.size());
//					System.out.println(data[i]);
//					System.out.println(labels.get(labels.keySet().toArray()[0]));
					Color clr = new Color(0,0,0);
					if(  labels.get((int) data[i])==null){
						clr=Color.red;
					}
					else{
						int tt = (int) Long.parseLong( labels.get((int) data[i]).split("#")[1] , 16);
						// Bulid Color
						clr = new Color( tt);
					}
					
					// Fill array with colors, where data exists
					dataSpektrumImage[ ((int)yCoord[i]-1) * master.getProject().getActiveView().getXDimension() + (int)xCoord[i]-1] =  clr.getRGB();
				}
				// set color for background
				for (int i=1; i<dataSpektrumImage.length; i++){
					if (dataSpektrumImage[i]==0){
						dataSpektrumImage[i] = Color.WHITE.getRGB();
					}
				}
				
				source =new MemoryImageSource( master.getProject().getActiveView().getXDimension(), 
						master.getProject().getActiveView().getYDimension(), dataSpektrumImage,
						0, master.getProject().getActiveView().getXDimension());
			}
			// If labelmap is not available
			else{
				ColorMap colorMap = new ColorMap( 0, nrCluster, master.getAlPanel().getColorModel());
				// Build the image
				for(int i=0; i<xCoord.length; i++){
					// Fill array with colors, where data exists
					dataSpektrumImage[ ((int)yCoord[i]-1) * master.getProject().getActiveView().getXDimension() + (int)xCoord[i]-1] = colorMap.getIndex((int) data[i]);
				}
				source =new MemoryImageSource( master.getProject().getActiveView().getXDimension(), 
						master.getProject().getActiveView().getYDimension(), colorMap.getColorModel(), dataSpektrumImage,
						0, master.getProject().getActiveView().getXDimension());
			}
			
			Image img = createImage(source);
			image = ImagePanel.toBufferedImage(img);
			locked = false;
			this.parent.repaint();
			this.repaint();
		}
		else {
			// waring if there are no labels
			JOptionPane.showMessageDialog(master, "no label available", "View info", JOptionPane.INFORMATION_MESSAGE);
		}
	}


	/**
	 * Displaying an image of the current view with given bands (red, green, blue) 
	 * @param v -> visulized view
	 * @param red wavelength
	 * @param green wavelength
	 * @param blue wavelength
	 * @author revised by jbehmann, modified by schmitter
	 */
	public void displayImage(View v, double red, double green, double blue){
		
		//Crop image
		double[][] coordinates = Utilities.materializeFeatures(v, FeatureSelection.getCoordinates(v));
		DoubleMatrix cMins = new DoubleMatrix(coordinates).columnMins();
		upperLeftPoint=new Point((int)cMins.get(0), (int)cMins.get(1));
		v= cropView(v);
		
		// Are rgb values NOT available

		//If available rgb values should be used
		//FeatureSelection.getRGB(master.getProject().getActiveView())==-1){
		//if(true){
		//Calculate rbg values

		double MaxValue = Double.MIN_VALUE;
		double MinValue = Double.MAX_VALUE;
		String [] waveString = v.getFeatureDescriptors();
		int [] featureIdx = FeatureSelection.getNonSpecialFeatures(v);
		
		if(featureIdx.length==0){
			return;
		}
		
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
		
		// If no information about the wavelength is available
		if(Ind[0] == Ind[1] &  Ind[1] == Ind[2]){
					System.out.println("no rgb image determinable, please add wavelengths");
					Ind[0] =   featureIdx[0];
					Ind[1] =   featureIdx[featureIdx.length-1];
					Ind[2] =  featureIdx[(int)featureIdx.length/2];
		}
		
		double[] dataRed = v.materializeFeature(Ind[0]);
		double[] dataGreen = v.materializeFeature(Ind[1]);
		double[] dataBlue = v.materializeFeature(Ind[2]);

		


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

			// Convert black to white ????
			//						if (dataRed[i] < 16 & dataGreen[i] < 16 & dataBlue[i] < 16) {
			//							dataRed[i] = 255 - dataRed[i];
			//							dataGreen[i] = 255 - dataGreen[i];
			//							dataBlue[i] = 255 - dataBlue[i];
			//						}
			// Herstellen der Hexadezimalschreibweise? STRANGE!
			dataRGB[i] = (255 << 24) | ((int) Math.round((dataRed[i])) << 16)
					| ((int) Math.round((dataGreen[i])) << 8)
					| (int) Math.round(dataBlue[i]);
		}

		// ADD rgb values to current view
		//v=new AddRGBView(dataRGB,v);
		//}

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

		v.setXDimension((int) xMax);
		v.setYDimension((int) yMax);


		// Size of the image
		int[] rgbArray = new int[v.getXDimension()* v.getYDimension()];	
		// Build the image
		for(int i=0; i<v.getNumberOfExamples(); i++){
			// Fill array with colors, where data exists
			rgbArray[ ((int)yCoord[i]-1) * v.getXDimension() + (int)xCoord[i]-1] = 
					(int) dataRGB[i];
			//(int) v.get(i, FeatureSelection.getRGB(v));
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
		image = ImagePanel.toBufferedImage(img);
		
		this.repaint();
		this.parent.repaint();
	}

	/**
	 * Function that displays a RGB representation of the current view
	 * If rgb feature is available it is used oterwise recalculated from the wavelength (red = 680 / green = 540 / blue = 435)
	 * @param v the current view
	 * @author revised by jbehmann, modified by schmitter
	 */
	public void displayRGB(View v){
		this.displayImage(v, 680, 540, 435);
	}



	/**
	 * source: http://www.dreamincode.net/code/snippet1076.htm as of 2012-08-05
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see Determining If an Image Has Transparent Pixels
		boolean hasAlpha = false;

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
			e.printStackTrace();
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),
					image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.getGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}


	/**
	 * Saves the current image as png file
	 * @author schmitter
	 */
	public void saveImage( ){
		JFileChooser chooser = new JFileChooser();
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG", "jpg"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("BMP", "bmp"));
		chooser.setDialogTitle("Save Image");
		// Querry if a save path exists
		if (!master.getDirectoryProp().getProperty("SAVE_PATH").equals("")) 
			chooser.setCurrentDirectory(new File(master.getDirectoryProp()
					.getProperty("SAVE_PATH")));
		
		int saveValue = chooser.showSaveDialog(this);
		if (saveValue == JFileChooser.APPROVE_OPTION)
		
			master.getDirectoryProp().setProperty("DATA_PATH", chooser.getSelectedFile().getParent());

			try {
				if(chooser.getFileFilter().getDescription().equalsIgnoreCase("jpg"))
					ImageIO.write(this.image, "jpg", new File(chooser.getSelectedFile().getAbsolutePath() + ".jpg"));
				else if(chooser.getFileFilter().getDescription().equalsIgnoreCase("bmp"))
					ImageIO.write(this.image, "bmp", new File(chooser.getSelectedFile().getAbsolutePath() + ".bmp"));
				else 
					ImageIO.write(this.image, "png", new File(chooser.getSelectedFile().getAbsolutePath() + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Could not save the displayed image");
				e.printStackTrace();
			}
	}
	

	/**
	 * Creates the Row- and Columnheader by reference to the current "image"
	 * Scaling is possible
	 * 
	 * CHECK IF HEADER AND PIXELS ARE EQUAL
	 *  
	 * @author modified by schmitter 
	 */
	public void makeParentHeader() {

		JLabel rowheader = new JLabel() {
			private static final long serialVersionUID = -8283267257890676356L;
			Font f = new Font("SansSerif", Font.BOLD, 10);
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// size of the header
				Rectangle r = new Rectangle(0, 0, (int) (image.getWidth()*scale), (int) (image.getHeight()*scale));
				g.setFont(f);
				g.setColor(Color.black);
				int step = (int) (30/scale);
				// set lines and text on the header
				for (int i = step - (r.y % step); i < image.getHeight(); i += step){
					int pos = (int) (r.y+i * scale);
					g.drawLine(0, pos, 3, pos);
					g.drawString("" + (r.y+i), 6, pos + 3);
				}
			}
			@Override
			public Dimension getPreferredSize() {
				Dimension d;
				try {
					d = new Dimension(25, (int) (image.getHeight()*scale));
				} catch (Exception e) {
					d = new Dimension(25, 100);
				}
				return d;
			}
		};

		JLabel columnheader = new JLabel() {
			private static final long serialVersionUID = 7296145138844086044L;
			Font f = new Font("SansSerif",  Font.BOLD, 10);

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				// size of the header
				Rectangle r = new Rectangle(0, 0, (int) (image.getWidth()*scale), (int) (image.getHeight()*scale));
				g.setFont(f);
				g.setColor(Color.black);
				int step = (int) (30/scale);
				// set lines and text on the header
				for (int i = step - (r.x % step); i < image.getWidth(); i += step) {
					int pos = (int) (r.x+i * scale);
					g.drawLine(pos, 0, pos, 3);
					g.drawString("" + (r.x + i), pos - 10, 16);
				}
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension( (int) (image.getWidth()*scale), 25);
			}
		};
		rowheader.setBackground(Color.lightGray);
		rowheader.setOpaque(true);
		columnheader.setBackground(Color.lightGray);
		columnheader.setOpaque(true);
		parent.setRowHeaderView(rowheader);
		parent.setColumnHeaderView(columnheader);
	}
	

	/**
	 * @return the ImagePanel embedded in the parent
	 */
	public JScrollPane getImagePanel() {
		final JScrollPane scrollPane =this.parent;// new JScrollPane();
		scrollPane.setViewportView(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					@Override
					public void adjustmentValueChanged(AdjustmentEvent e) {
						scrollPane.repaint();
					}
				});
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {
					@Override
					public void adjustmentValueChanged(AdjustmentEvent e) {
						scrollPane.repaint();
					}
				});
		return scrollPane;
	}

	/**
	 * Clear Image panel Content
	 * 
	 * @author janb
	 * 
	 */
	public void clearImage() {
		// image.= new BufferedImage(null, null, locked, null);
		Graphics2D g = image.createGraphics(); // not getGraphics -- see the API
		g.setBackground(Color.LIGHT_GRAY);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
		g.dispose();
		repaint();
		fileLocation = null;
	}

	

	/**
	 * 
	 */
	public void generateIndex(View v) {
		if (FeatureSelection.getXCoordinate(v) != -1
				&& FeatureSelection.getYCoordinate(v) != -1) {
			index = new HashMap<Point, Integer>((int) (v
					.getNumberOfExamples() * (1 / 0.75)));
			try {
				for (int i = 0; i < v.getNumberOfExamples(); i++) {
					index.get(new Point(0,1));
					// if(indizes.get(i)==1)

					index.put(new Point((int) v.get(i,FeatureSelection.getXCoordinate(v)), (int) v.get(i,FeatureSelection.getYCoordinate(v))), i);

				}
			} catch (NullPointerException npe){
				System.out.println("Could not read data");
				npe.printStackTrace();
			}

		}
	}

	/**
	 * @return the dotsColor
	 */
	public HashMap<Point, Color> getDotsColor() {
		return dotsColor;
	}

	/**
	 * @param dotsColor
	 *            the dotsColor to set
	 */
	public void setDotsColor(HashMap<Point, Color> dotsColor) {
		this.dotsColor = dotsColor;
	}

	/**
	 * @return the index
	 */
	public HashMap<Point, Integer> getIndex(View v) {
		if(index==null)
			generateIndex(v);
		return index;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public void setIndex(HashMap<Point, Integer> index) {
		this.index = index;
	}

	/**
	 * @return the maxSpectrum
	 */
	public double getMaxSpectrum(View v) {
		if(maxSpectrum==Double.MIN_VALUE){
			double [][] spectrum = v.materializeFeatures(FeatureSelection.getNonSpecialFeatures(v));
			for (int i = 0;i<v.getNumberOfExamples();i++){
				for (int j= 0; j<FeatureSelection.getNonSpecialFeatures(v).length; j++ ){
					if(maxSpectrum<spectrum[i][j])
						maxSpectrum = spectrum[i][j];
				}
			}
		}
		return maxSpectrum;
	}

	/**
	 * @param maxSpectrum
	 *            the maxSpectrum to set
	 */
	public void setMaxSpectrum(double maxSpectrum) {
		this.maxSpectrum = maxSpectrum;
	}

	/**
	 * @return the minSpectrum
	 */
	public double getMinSpectrum(View v) {
		if(minSpectrum==Double.MAX_VALUE){
			double [][] spectrum = v.materializeFeatures(FeatureSelection.getNonSpecialFeatures(v));
			for (int i = 0;i<v.getNumberOfExamples();i++){
				for (int j= 0; j<FeatureSelection.getNonSpecialFeatures(v).length; j++ ){
					if(minSpectrum>spectrum[i][j])
						minSpectrum = spectrum[i][j];
				}
			}
		}
		return minSpectrum;
	}

	/**
	 * @param minSpectrum the minSpectrum to set
	 */
	public void setMinSpectrum(double minSpectrum) {
		this.minSpectrum = minSpectrum;
	}

	public double[] getMinMaxSpectrum(View v){
		if(minSpectrum==Double.MAX_VALUE && maxSpectrum==Double.MIN_VALUE){
			double [][] spectrum = v.materializeFeatures(FeatureSelection.getNonSpecialFeatures(v));
			for (int i = 0;i<v.getNumberOfExamples();i++){
				for (int j= 0; j<FeatureSelection.getNonSpecialFeatures(v).length; j++ ){
					if(minSpectrum>spectrum[i][j])
						minSpectrum = spectrum[i][j];
					if(maxSpectrum<spectrum[i][j])
						maxSpectrum = spectrum[i][j];
				}
			}
		}
		if(minSpectrum==Double.MAX_VALUE){
			double [][] spectrum = master.getProject().getActiveView().materializeFeatures(FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
			for (int i = 0;i<master.getProject().getActiveView().getNumberOfExamples();i++){
				for (int j= 0; j<FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()).length; j++ ){
					if(minSpectrum>spectrum[i][j])
						minSpectrum = spectrum[i][j];
				}
			}
		}
		if( maxSpectrum==Double.MIN_VALUE){
			double [][] spectrum = master.getProject().getActiveView().materializeFeatures(FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()));
			for (int i = 0;i<master.getProject().getActiveView().getNumberOfExamples();i++){
				for (int j= 0; j<FeatureSelection.getNonSpecialFeatures(master.getProject().getActiveView()).length; j++ ){
					if(maxSpectrum<spectrum[i][j])
						maxSpectrum = spectrum[i][j];
				}
			}
		}
		return new double[]{minSpectrum,maxSpectrum};

	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @param scale the scale to set
	 */
	public void setScale(int scale) {
		this.scale = scale;
	}


	/**
	 * @return the selShapes
	 */
	public Shape getSelShape() {
		return selShape;
	}

	/**
	 * @param selShape the selShape to set
	 */
	public void setSelShape(Shape selShape) {
		this.selShape = selShape;
	}

	/**
	 * @return the lassoPoints
	 */
	public ArrayList<Point> getLassoPoints() {
		if(lassoPoints==null){
			lassoPoints = new ArrayList<Point>();
		}
		return lassoPoints;
	}

	/**
	 * @return the polyPoints
	 */
	public ArrayList<Point> getPolyPoints() {
		if(polyPoints==null){
			polyPoints = new ArrayList<Point>();
		}
		return polyPoints;
	}

	/**
	 * @return the locked
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * @param locked the locked to set
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	
	
	/**
	 * Functions that removes the blank space within a hs image for visualization
	 * @author jbehmann
	 * @param sparseView view which spans not the full image
	 * @return cropped view
	 */
	public View cropView(View sparseView){
		//Update of the coordinates
		double[][] koordinaten = Utilities.materializeFeatures(sparseView, FeatureSelection.getCoordinates(sparseView));
		double[][] neueKoordinaten = ImageSelection.adaptCoordinates(koordinaten);
		String name  = sparseView.getViewName();
		View coords = new DoubleMatrixView(new DoubleMatrix(neueKoordinaten), name);
		coords.setFeatureRoles(new FeatureRole[]{FeatureRole.X,FeatureRole.Y});
		coords.setFeatureDescriptors(new String[]{FeatureRole.X.name(),FeatureRole.Y.name()});
		coords.setViewName(name);
		
		//Remove the ccordinates from the spase view
		int[] indizes = new int[sparseView.getNumberOfColumns()-2];
		int k = 0;
		for(int i=0;i<sparseView.getNumberOfColumns();i++){
			if(i!=FeatureSelection.getXCoordinate(sparseView) && i!=FeatureSelection.getYCoordinate(sparseView)){
				indizes[k] = i;
				k++;
			}
		}
		FeatureFilter ff = new FeatureFilter(sparseView,indizes);
		ff.setViewName(name);
		
		//Merge new coordinates with old view without coordinates
		View n = new MergeFeaturesView(coords, ff);
		n.setViewName(name);
				
		return n;		
				
	}
	
	/**
	 * Function that transfors the pixel position into the image coordinate system with regard to scal and offset
	 * @param e The mouseEvent
	 * @author jbehmann
	 * @return the position in image coordinate system 
	 */
	public Point transformPosition ( MouseEvent e ){
		Point imageCoordinate = new Point( (int) (e.getX()/this.scale)+(int) upperLeftPoint.getX() , (int) (e.getY()/ this.scale)+ (int) upperLeftPoint.getY());
		
		return imageCoordinate;
	}
	
}
