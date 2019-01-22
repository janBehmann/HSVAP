package data;


/**
 * The DoubleMatrixInterface represents a two-dimensional double matrix.
 * Any implementing class can be accessed as a getNumberOfRows x getNumberOfColumns matrix.
 * 
 * For example, any View implements this interface, but in addition has meta information.
 *  
 * @author pwelke
 *
 */
public interface DoubleMatrixInterface {
	
		/**
		 * Returns the number of features in this view of the data.
		 * @return
		 */
		public int getNumberOfColumns();
		
		/**
		 * Returns the number of examples in this view of the data.
		 * @return
		 */
		public int getNumberOfExamples();
		
		/**
		 * Returns the value of feature j in the i'th example.
		 * @param i
		 * @param j
		 * @return
		 */
		public double get(int i, int j); 
		
		/**
		 * Return the values of feature j in all examples as a double array.
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the i'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 * 
		 * @param j
		 * @return
		 */
		public double[] materializeFeature(int j);
		
		/**
		 * Return the values of features j in all examples as a double array.
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the i'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 * 
		 * @param j
		 * @return
		 */
		public double[][] materializeFeatures(int [] j);
		
		/**
		 * Return example i as a double array.
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the j'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 *
		 * @param j
		 * @return
		 */
		public double[] materializeExample(int i);
		
		/**
		 * Return examples i as a double array.
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the j'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 *
		 * @param j
		 * @return
		 */
		public double[][] materializeExamples(int []i);
		
		/**
		 * Return all data visible in this view as a double[][] array.
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the [i][j]'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 * @return
		 */
		public double[][] materialize();

		/**
		 * Return examples i and features j as a double array
		 * 
		 * Conventionally, the array is expected to be an independent copy. 
		 * I.e. changing the [i][j]'th value in the returned array does not change 
		 * the value returned by get(i,j) in the original view.
		 * @return
		 */
		public double[][] materializeFeatureExamples(int[] i, int[] j);

		/**
		 * calculate min and max value of data
		 */
		public void materializeMinMaxValue();
		
		/**
		 * calculate x and y dimension of data
		 */
		public void materializeXYDimension();
}
