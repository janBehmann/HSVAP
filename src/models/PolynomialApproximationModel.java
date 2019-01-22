package models;

import gui.ApproximationWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.jblas.DoubleMatrix;

import data.FeatureRole;
import data.FeatureSelection;
import data.Utilities;
import data.View;
import data.inmemory.DoubleMatrixView;
import data.operators.FeatureFilter;
import data.operators.MergeFeaturesView;


public class PolynomialApproximationModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7464969773008417627L;
	
	private ArrayList<int[]> xData;
	private ArrayList<Integer> order;

	/**
	 * Constructor to make a PolynomialApproximationModel out of the .model-file, i.e. deserializing the file
	 * 
	 * @param file
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public PolynomialApproximationModel(File file) throws IOException, ClassNotFoundException {

		//deserialize
		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		PolynomialApproximationModel model = (PolynomialApproximationModel) o.readObject();
		o.close();

		this.xData = model.getxData();
		this.order = model.getOrder();
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());

	}

	/**
	 * The x-Data-range at position i in the ArrayList xData is approximated with the order at position i in the ArrayList order
	 * 
	 * @param name Name
	 * @param datensatz String for dataset (viewname)
	 * @param date Actual date
	 * @param xData Arraylist of xData-Arrays
	 * @param order Arraylist of orders
	 */
	public PolynomialApproximationModel(String name, String datensatz, Date date, ArrayList<int[]> xData, ArrayList<Integer> order) {
		super(name, datensatz, date);
		if(order.size()!=xData.size()){
			throw new Error();
		}
		this.order=order;
		this.xData=xData;

	}

	/**
	 * Constructor to set Meta information
	 * 
	 * @param name Name
	 * @param datensatz String for dataset (viewname)
	 * @param date Actual date
	 */
	public PolynomialApproximationModel(String name, String datensatz, Date date){
		super(name, datensatz, date);
		this.order = new ArrayList<Integer>() ;
		this.xData = new ArrayList<int[]>();
	}

	
	public View applyOn(View v, String path) throws IOException{
		int sumOrder = 0;
		for(Integer i: order){
			sumOrder += i+1;
		}
	

		DoubleMatrix coeffMatrix = new DoubleMatrix(v.getNumberOfExamples(),sumOrder);

		DoubleMatrix data = Utilities.materializeAsDoubleMatrix(v);
		int[] nonSpFea = FeatureSelection.getNonSpecialFeatures(v);
		DoubleMatrix nonSpFeatures= data.getColumns(nonSpFea);


		FeatureFilter f = new FeatureFilter(v, FeatureSelection.getSpecialFeatures(v));

		String[] descr = new String[sumOrder];
		int nextIndex = 0;
		
		double[] x = FeatureSelection.getNonSpecialFeatureDescriptor(v);
		
		for(int i=0; i<xData.size();i++){
		
			int fromInd = xData.get(i)[0];
			int toInd = xData.get(i)[xData.get(i).length-1];
			
			int leng = toInd-fromInd+1;
			
//			int k=0;
//			double[] xData = new double[leng]; 
//			for(int l=fromInd;l<=toInd;l++){
//				xData[k]=plotspect[l];
//				k++;
//			}
			
			double[] xData = ApproximationWindow.getXData(fromInd, toInd, x);
			
			for(int j=0;j<nonSpFeatures.rows;j++){
				int kk=0;
				double[] yData = new double[leng]; 
				for(int ii=fromInd;ii<=toInd;ii++){
					yData[kk]=nonSpFeatures.get(j,ii);
					kk++;
				}
				feature_selection.PolynomialRegression pol = new feature_selection.PolynomialRegression(xData, yData, order.get(i));
				
				for(int l=nextIndex; l<nextIndex+order.get(i)+1; l++){
					coeffMatrix.put(j, l, pol.beta(l-nextIndex));
					descr[l] = "x_"+(l-nextIndex)+"_degree"+order.get(i)+"_"+xData[0]+"_"+xData[xData.length-1];
					//					System.out.println("x_"+(l-nextIndex)+"_order"+order.get(i)+"_"+xData.get(i)[0]+"_"+xData.get(i)[xData.get(i).length-1]);
				}
			}
			nextIndex+=order.get(i)+1;
			
			
//			feature_selection.PolynomialRegression polSample = new feature_selection.PolynomialRegression(xData, yData, order.get(i));

//			double[] plotspectAppr = new double[yData.length];
//			for(int j = 0; j<xData.length;j++){
//				plotspectAppr[j] = polSample.predict(xData[j]);
//			}
			
//			Coefficient polynomApprox = new Coefficient();
//			// Aufstellen der Kovarianzmatrix 
//			double[][] covMat =DoubleMatrix.eye(xData.get(i).length).toArray2();
//
//			double [] xData_d = new double[xData.get(i).length];
//			String[] descrp = v.getFeatureDescriptors();
//			try{
//				for (int m = 0;m<xData.get(i).length;m++){
//					xData_d[m] = Double.parseDouble(descrp[nonSpFea[xData.get(i)[m]]]);
//				}
//			}catch(Exception e){
//				for (int m = 0;m<xData.get(i).length;m++){
//					xData_d[m] = xData.get(i)[m];
//				}
//			}
//
//			for(int j=0;j<v.getNumberOfExamples();j++){
//
//				double[] coeff = polynomApprox.curveApproximation(nonSpFeatures.get(j, xData.get(i)).toArray2(), xData_d, covMat, order.get(i));
//				for(int l=nextIndex; l<nextIndex+order.get(i)+1; l++){
//					coeffMatrix.put(j, l, coeff[l-nextIndex]);
//					descr[l] = "x_"+(l-nextIndex)+"_degree"+order.get(i)+"_"+xData_d[0]+"_"+xData_d[xData_d.length-1];
//					//					System.out.println("x_"+(l-nextIndex)+"_order"+order.get(i)+"_"+xData.get(i)[0]+"_"+xData.get(i)[xData.get(i).length-1]);
//				}
//			}
//			nextIndex+=order.get(i)+1;

		}
		
		

		String desc = "Approximated View of "+v.getViewName();

		View coeffView =  new DoubleMatrixView(coeffMatrix,"Coefficients");//.saveMaterialized(new File(path,v.getViewName()+"_coeffs.json"), new File(path,v.getViewName()+"_coeffs.mat"), new MatlabFormat());

		for(int i=0; i<coeffView.getNumberOfColumns(); i++){
			coeffView.setFeatureRole(i, FeatureRole.FEATURE);
		}
		coeffView.setFeatureDescriptors(descr);

		coeffView.setViewName("Coefficients");
		coeffView.setViewDescription("Coefficients");
		f.setViewName("Special Features");
		f.setViewDescription("Special Features");

		MergeFeaturesView neuerView = new MergeFeaturesView(f, coeffView);
		neuerView.setViewDescription(desc);
		neuerView.setViewName(v.getViewName()+"_approximated");

		//		View neu = new DoubleMatrixView(Utilities.materializeAsDoubleMatrix(neuerView), neuerView.getViewName()).saveMaterialized(new File(path,v.getViewName()+"_materialised.json"), new File(path,v.getViewName()+"_materialised.mat"), new MatlabFormat());
		//		neu.setViewDescription(desc);
		//		neu.setFeatureDescriptors(neuerView.getFeatureDescriptors().clone());
		//		neu.setFeatureRoles(neuerView.getFeatureRoles().clone());
		//		neu.getLabelMap().putAll(v.getLabelMap());
		return neuerView;
	}

/**
 * Add a new approximation range to the existing ones
 * 
 * @param xData
 * @param order
 */
	public void addAproximation(int[] xData, int order){
		this.order.add(order);
		this.xData.add(xData);
	}


	public ArrayList<int[]> getxData() {
		return xData;
	}

	public ArrayList<Integer> getOrder() {
		return order;
	}

	/**
	 * Defines which Text will be written in the ModelMetaPanel
	 */
	@Override
	public String getMetaInfo(boolean detailed) {
		String meta = this.getMainMeta();

		meta += "number of approximations: "+this.getxData().size();

		//			meta+="indices:   "+((PolynomialApproximationModel) model).getPolynomApprox().NumCoeff();

		if (detailed==true){

			ArrayList<String> xDataString = new ArrayList<String>();
			for(int[] da: this.getxData()){
				String s = "[";
				for(double d: da){
					s+=d+", ";
				}
				//				System.out.println(s.length());
				String s2 = s.substring(0, s.length()-2)+"]";
				xDataString.add(s2);
			}

			//			meta += "<p>indices of : "+xDataString.toString();
			meta += "<br><br>degrees: "+this.getOrder().toString();
		}

		return meta;
	}

}


