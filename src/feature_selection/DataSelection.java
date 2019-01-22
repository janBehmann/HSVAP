package feature_selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.jblas.DoubleMatrix;
import org.apache.commons.math3.stat.StatUtils;
import data.FeatureSelection;
import data.Utilities;
import data.View;

/**
 * Calculates statistics for features of views
 * 
 * @author roemer
 *
 */
public class DataSelection {

	/**
	 * calculates the mean of each class and stores the result in a (classes,features) DoubleMatrix 
	 * 
	 * @param	View view to calculate mean from
	 * @return	DoubleMatrix resulting DoubleMatrix
	 * 
	 * @author croemer
	 */
	public static DoubleMatrix meanOfClasses(View v){
		DoubleMatrix data = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getNonSpecialFeatures(v));
		
		Set<Integer> keys =v.getLabelMap().keySet();
		DoubleMatrix meanMatrix = new DoubleMatrix(keys.size(), data.columns); 
		
		int tempRow = 0;
		for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();){
			int key = iterator.next();
			DoubleMatrix tempMatrix = data.getRows(getSamplesOfClass(v,key));
			meanMatrix.putRow(tempRow, tempMatrix.columnMeans());
			
			
			tempRow++;
		}
		
		return meanMatrix;
	}

	/**
	 * calculated the standard deviation of each class
	 * 
	 * @param v	View View with features
	 * 
	 * @return stdMatrix	Matrix with standardDeviation of #classes x #features dimension with
	 * 						features = standard deviation of each feature for this class
	 * @author croemer
	 */
	public static DoubleMatrix std(View v){

		DoubleMatrix stdMatrix = var(v);
		for (int i = 0; i<stdMatrix.rows;i++){
			for (int j = 0; j<stdMatrix.columns;j++){
				stdMatrix.put(i, j, Math.sqrt(stdMatrix.get(i,j)));
			}
		}
		return stdMatrix;
	}

	/**
	 * calculated the variance of features for each class
	 * 
	 * @param v	View View with features
	 * 
	 * @return varMatrix	Matrix with variance of #classes x #features dimension with
	 * 						features = standard deviation of each feature for this class
	 * @author: croemer
	 */

	public static DoubleMatrix var(View v){
		DoubleMatrix data = Utilities.materializeAsDoubleMatrix(v).getColumns(FeatureSelection.getNonSpecialFeatures(v));
		Set<Integer> keys =v.getLabelMap().keySet();
		
//		DoubleMatrix varMatrix = new DoubleMatrix(keys.size(), data.columns);
		
//		int tempRow = 0;
//		for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();){
//			int key = iterator.next();
//			DoubleMatrix tempMatrix = data.getRows(getSamplesOfClass(v,key));
//			DoubleMatrix classMean = tempMatrix.columnMeans();
//			for(int i = 0; i<tempMatrix.rows;i++){
//				for (int j = 0; j<tempMatrix.columns;j++){
//					double variance = StatUtils.variance(column.toArray(), columnMeans.get(i));
//					varMatrix.put(tempRow, j, varMatrix.get(tempRow,j)+Math.pow((tempMatrix.get(i,j)-classMean.get(j))/tempMatrix.rows,2));
//				}
//			}
//			
//			
//			tempRow++;
//		}
		
		
		DoubleMatrix varMatrix = new DoubleMatrix(keys.size(), data.columns);
		int tempRow = 0;
		for (int key : keys){
			DoubleMatrix tempMatrix = data.getRows(getSamplesOfClass(v,key));
			DoubleMatrix columnMeans = tempMatrix.columnMeans();
			for (int i = 0; i < varMatrix.columns; i++)	{				
				DoubleMatrix column = tempMatrix.getColumn(i);				
				double variance = StatUtils.variance(column.toArray(), columnMeans.get(i));
				if (variance == 0)
					variance = 1e-6;
				varMatrix.put(tempRow, i, variance);
			}
			tempRow++;

		}
		return varMatrix;
		
	}

	
	
	/**
	 * getSamplesOfClass returns an int[] array with indices for all samples belonging to
	 * the class classIndex from View v
	 * 
	 * @param v View from which to select samples
	 * @param classIndex	index of Class. Class index has to be the internal key of the class, not the name!
	 * @return int[] array wih indices
	 * 
	 * @author croemer
	 */
	public static int[] getSamplesOfClass(View v, int classIndex){
		DoubleMatrix label = Utilities.materializeAsDoubleMatrix(v).getColumn(FeatureSelection.getLabel(v));
		ArrayList<Integer> tempIndices = new ArrayList<Integer>();
		for (int i = 0; i<label.rows; i++){
			if ((int) label.get(i)==classIndex)
				tempIndices.add(i);
		}
		int[] result = new int[tempIndices.size()];
		for (int i = 0; i<tempIndices.size();i++){
			result[i] = tempIndices.get(i);
		}
		return result;
	}

}
