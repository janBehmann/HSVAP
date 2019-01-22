/**
 * 
 */
package imageprocessing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;

import org.jblas.DoubleMatrix;

import data.FeatureSelection;
import data.View;

/**
 * ImageSelection tools
 * 
 * This class defines all static methods which calculate 
 * things concerning the image selection.
 * 
 * 
 * @author Till
 *
 */
public abstract class ImageSelection {
	
	
	/**
	 * Method to select all pixels inside the shapes.
	 * Used to create a new View from the selected areas.
	 * 
	 * @param view The view whose examples should be selected.
	 * @param shp The list of shapes to define the areas to be selected.
	 * @return The indices of the view's examples which are inside the shapes.
	 */
	public static int[] selectInside(View view, Shape shp){
		double[][] coordinates = view.materializeFeatures(FeatureSelection.getCoordinates(view));
		
		ArrayList<Integer> selectedIndizes = new ArrayList<Integer>();
		for(int i = 0; i<coordinates.length; i++){
			
			if(shp.contains(coordinates[i][0], coordinates[i][1])){
				selectedIndizes.add(i);
			}

		}
		int[] indInt = new int[selectedIndizes.size()];
		for(int i = 0; i<selectedIndizes.size(); i++){
			indInt[i] = selectedIndizes.get(i);
		}
		return indInt;
	}
	
	
	/**
	 * Method to select all pixels outside the shape.
	 * Used to create a new View from the unselected area (selected area removed).
	 * 
	 *  
	 * @param view The view whose examples should be selected.
	 * @param shp The shape to define the area to be removed.
	 * @return The indices of the view's examples which are outside the shape.
	 */
	public static int[] selectOutside(View view, Shape shp){
		double[][] coordinates = view.materializeFeatures(FeatureSelection.getCoordinates(view));
		
		ArrayList<Integer> selectedIndizes = new ArrayList<Integer>();
		for(int i = 0; i<coordinates.length; i++){
			if(!shp.contains(coordinates[i][0], coordinates[i][1])){
				selectedIndizes.add(i);
			}
		}
		int[] indInt = new int[selectedIndizes.size()];
		for(int i = 0; i<selectedIndizes.size(); i++){
			indInt[i] = selectedIndizes.get(i);
		}
		return indInt;
	}

	
	
	/**
	 * This method controls the selection of a rectangle.
	 * Rectangle is selected by pressing and then dragging mouse.
	 * For example: - Different calculation, when the rectangle is dragged to the bottom-right/bottom-left/top-right/top-left direction
	 *              - Different handling of the selected rectangle, if the cursor exites the image display/is outside the image
	 * 
	 * @param width  Width of the picture display.
	 * @param height Heigth of the picture display.
	 * @param scale  Current zooming scale.
	 * @param p   	Current point/position of the cursor while dragging
	 * @param firstPoint     Position of the cursor when mouse has been pressed.
	 * @return
	 */
	public static Rectangle selectRectangle(int width, int height, double scale, Point p, Point firstPoint){
		int a,b,c,d;
		
		//TODO: eigentlich größer/kleiner gleich
		
		//inside image
		if (width> p.x & height> p.y){

			if(firstPoint.x<p.getX() && firstPoint.y<p.getY()){			// dragging direction: down-right
				a = firstPoint.x;
				b = firstPoint.y;
				c = (int) (p.getX()-firstPoint.x);
				d = (int) (p.getY()-firstPoint.y);
			}else if(firstPoint.x>p.getX() && firstPoint.y<p.getY()){	// dragging direction: down-left
				a = (int) p.getX();
				b = firstPoint.y;
				c = (int) (firstPoint.x-p.getX());
				d = (int) (p.getY()-firstPoint.y);
			}else if(firstPoint.x<p.getX() && firstPoint.y>p.getY()){	// dragging direction: up-right
				a = firstPoint.x;
				b = (int) p.getY();
				c = (int) (p.getX()-firstPoint.x);
				d = (int) (firstPoint.y-p.getY());
			}else {												// dragging direction: up-left
				a = (int) p.getX();
				b = (int) p.getY();
				c = (int) (firstPoint.x-p.getX())-4;
				d = (int) (firstPoint.y-p.getY())-4;
			}



			//			}else if(width> e.getX()/this.scale){
			//				
			//			}else if(height> e.getY()/this.scale){

		}else{			// ouside the image underneath and to the right of the image
			if(firstPoint.x<p.getX() && firstPoint.y<p.getY()){
				a = firstPoint.x;
				b = firstPoint.y;
				c = (int) (width-firstPoint.x);
				d = (int) (height-firstPoint.y);
			}else if(firstPoint.x>p.getX() && firstPoint.y<p.getY()){
				a = (int) width;
				b = firstPoint.y;
				c = (int) (firstPoint.x-width);
				d = (int) (p.getY()-firstPoint.y);
			}else if(firstPoint.x<p.getX() && firstPoint.y>p.getY()){
				a = firstPoint.x;
				b = (int) height;
				c = (int) (p.getX()-firstPoint.x);
				d = (int) (firstPoint.y-height);
			}else {
				a = (int) p.getX();
				b = (int) p.getY();
				c = (int) p.getX()-1;
				d = (int) p.getY()-1;
			}

		}

		Rectangle r = new Rectangle(a,b,c,d);
		return r;
	}

	/**
	 * Method to adapt the coordinates, most top-left coordinate will be (1,1)
	 * i.e. subtract the smallest occuring coordinate from all coordinates.
	 * 
	 * @param koordinaten former Coordinates 
	 * @return adapted coordinates: double[][] with 2 columns
	 */
	public static double[][] adaptCoordinates(double[][] koordinaten){
		
		DoubleMatrix dm = new DoubleMatrix(koordinaten);
		DoubleMatrix cMins = dm.columnMins();

		double[][] neueKoordinaten = new double[koordinaten.length][2];
		for (int i=0;i<koordinaten.length; i++){
			neueKoordinaten[i][0] = koordinaten[i][0] - cMins.get(0, 0)+1;
			neueKoordinaten[i][1] = koordinaten[i][1] - cMins.get(0, 1)+1;
		}
		return neueKoordinaten;
	}
		
}
