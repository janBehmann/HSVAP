package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.jblas.DoubleMatrix;
import org.jblas.exceptions.SizeException;

import data.View;
import data.operators.NormView;


public class NormalisationModel extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = -879400591433485239L;
	private DoubleMatrix subtrahend;
	private DoubleMatrix divisor;


	public NormalisationModel(String name, String datensatz, Date date, DoubleMatrix subtrahend, DoubleMatrix divisor) {
		super(name, datensatz, date);
		this.subtrahend = subtrahend;
		this.divisor = divisor;
	}


	public NormalisationModel(File file) throws IOException, ParseException, ClassNotFoundException{

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		NormalisationModel model = (NormalisationModel) o.readObject();
		o.close();

		this.subtrahend = model.subtrahend;
		this.divisor = model.divisor;
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());
	}

	/**
	 * Normalize data columnwise
	 * 
	 * @param spektren: spectral data/features data
	 * @return
	 */
	public DoubleMatrix normSpektren(DoubleMatrix spektren){
		DoubleMatrix spektrenNorm = spektren.subRowVector( this.subtrahend);
		spektrenNorm = spektrenNorm.divRowVector(this.divisor);
		return spektrenNorm;
	}

	public View applyOn(View view, String path) throws SizeException, IllegalArgumentException, IOException{
		return new NormView(view, subtrahend, divisor, path);

	}

	public String toolTipText(){
		return DF.format(this.getDate()) + ", subtrahend " +  this.subtrahend + ", divisor " + this.divisor + " applied on " + this.getDatensatz();
	}

	public DoubleMatrix getSubtrahend() {
		return subtrahend;
	}

	public DoubleMatrix getDivisor() {
		return divisor;
	}

	@Override
	public String getMetaInfo(boolean detailed) {
		String meta = this.getMainMeta();

		meta+="Applicable to view with number of features: " + this.getSubtrahend().length;;
		
		if (detailed==true){
			meta+="<p>subtrahend: " + this.getSubtrahend().toString() + "<p>divisor: " + this.getDivisor().toString();
		}
		return meta;
	}
	
	public String toCSV(){
		
		String result = ""+Arrays.toString(this.getSubtrahend().data).substring(1, Arrays.toString(this.getSubtrahend().data).length()-1)+"\n"+Arrays.toString(this.getDivisor().data).substring(1, Arrays.toString(this.getDivisor().data).length()-1);
		
		return result;
	}
	
}
