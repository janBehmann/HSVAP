package feature_selection;
/*************************************************************************
 *  Compilation:  javac -cp .:jama.jar PolynomialRegression.java
 *  Execution:    java  -cp .:jama.jar PolynomialRegression
 *  Dependencies: jama.jar StdOut.java
 * 
 *  % java -cp .:jama.jar PolynomialRegression
 *  0.01 N^3 + -1.64 N^2 + 168.92 N + -2113.73 (R^2 = 0.997)
 *
 *************************************************************************/

import Jama.Matrix;
import Jama.QRDecomposition;

/**
 *  The <tt>PolynomialRegression</tt> class performs a polynomial regression
 *  on an set of <em>N</em> data points (<em>y<sub>i</sub></em>, <em>x<sub>i</sub></em>).
 *  That is, it fits a polynomial
 *  <em>y</em> = &beta;<sub>0</sub> +  &beta;<sub>1</sub> <em>x</em> +
 *  &beta;<sub>2</sub> <em>x</em><sup>2</sup> + ... +
 *  &beta;<sub><em>d</em></sub> <em>x</em><sup><em>d</em></sup>
 *  (where <em>y</em> is the response variable, <em>x</em> is the predictor variable,
 *  and the &beta;<sub><em>i</em></sub> are the regression coefficients)
 *  that minimizes the sum of squared residuals of the multiple regression model.
 *  It also computes associated the coefficient of determination <em>R</em><sup>2</sup>.
 *  <p>
 *  This implementation performs a QR-decomposition of the underlying
 *  Vandermonde matrix, so it is not the fastest or most numerically
 *  stable way to perform the polynomial regression.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */

/**
 * slightly changed
 * now returns SST and SSE; SSE changed to mean SSE 
 * removed main-method
 * @author Till
 *
 */
public class PolynomialRegression {
    private final int N;         // number of observations
    private final int degree;    // degree of the polynomial regression
    private final Matrix beta;   // the polynomial regression coefficients
    private double SSE;          // sum of squares due to error
    private double SST;          // total sum of squares
    private Matrix residuals;

  /**
     * Performs a polynomial reggression on the data points <tt>(y[i], x[i])</tt>.
     * @param x the values of the predictor variable
     * @param y the corresponding values of the response variable
     * @param degree the degree of the polynomial to fit
     * @throws java.lang.IllegalArgumentException if the lengths of the two arrays are not equal
     */
    public PolynomialRegression(double[] x, double[] y, int degree) {
        this.degree = degree;
        N = x.length;

        // build Vandermonde matrix
        double[][] vandermonde = new double[N][degree+1];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j <= degree; j++) {
                vandermonde[i][j] = Math.pow(x[i], j);
            }
        }
        Matrix X = new Matrix(vandermonde);

        // create matrix from vector
        Matrix Y = new Matrix(y, N);

        // find least squares solution
        QRDecomposition qr = new QRDecomposition(X);
        beta = qr.solve(Y);


        // mean of y[] values
        double sum = 0.0;
        for (int i = 0; i < N; i++)
            sum += y[i];
        double mean = sum / N;

        // total variation to be accounted for
        for (int i = 0; i < N; i++) {
            double dev = y[i] - mean;
            SST += dev*dev;
        }

        // variation not accounted for
        residuals = X.times(beta).minus(Y);
        SSE = residuals.norm2() * residuals.norm2() / residuals.getRowDimension();
    }

   /**
     * Returns the <tt>j</tt>th regression coefficient
     * @return the <tt>j</tt>th regression coefficient
     */
    public double beta(int j) {
        return beta.get(j, 0);
    }

   /**
     * Returns the degree of the polynomial to fit
     * @return the degree of the polynomial to fit
     */
    public int degree() {
        return degree;
    }

   /**
     * Returns the coefficient of determination <em>R</em><sup>2</sup>.
     * @return the coefficient of determination <em>R</em><sup>2</sup>, which is a real number between 0 and 1
     */
    public double R2() {
        if (SST == 0.0) return 1.0;   // constant function
        return 1.0 - SSE/SST;
    }
    
    /**
     * Returns the total sum of squares
     * @return the total sum of squares; sum of the squares of the deviations about the mean
     */
    public double getTotalSOS() {
        return SST;
    }
    
    /**
     * Returns the sum of squares due to error
     * @return the sum of squares due to error, which is the residuals sum of squares
     */
    public double getResidualsSOS() {
        return SSE;
    }
    
    /**
     * Returns the residuals
     * @return the residuals
     */
    public double[] getResiduals() {
        return residuals.transpose().getArray()[0];
    }

   /**
     * Returns the expected response <tt>y</tt> given the value of the predictor
     *    variable <tt>x</tt>.
     * @param x the value of the predictor variable
     * @return the expected response <tt>y</tt> given the value of the predictor
     *    variable <tt>x</tt>
     */
    public double predict(double x) {
        // horner's method
        double y = 0.0;
        for (int j = degree; j >= 0; j--)
            y = beta(j) + (x * y);
        return y;
    }


}
