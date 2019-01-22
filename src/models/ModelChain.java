package models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.jblas.exceptions.SizeException;

import data.View;

/**
 * This class simulates a model but actually is a ordered list of models
 * 
 * @author Till
 *
 */
public class ModelChain extends TransformationModel implements Serializable, ModelInterface{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4896325522208279044L;
	private ArrayList<ModelInterface> models;


/**
 * Constructor to create a "model" which will apply several models successively on a view
 * 
 * @param name Name
 * @param datensatz Viewname
 * @param date Date 
 * @param models ArrayList of the models this "model" will apply successively on a view
 */
	public ModelChain(String name, String datensatz, Date date,ArrayList<ModelInterface> models) {
		super(name, datensatz, date);
		this.models = models;
	}

	public ModelChain(File file) throws IOException, ParseException, ClassNotFoundException{

		FileInputStream fileIS = new FileInputStream( file );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		ModelChain model = (ModelChain) o.readObject();
		o.close();

		this.models = model.models;
		this.setName(model.getName());
		this.setDatensatz(model.getDatensatz());
		this.setDate(model.getDate());

	}

	@Override
	public View applyOn(View v, String path) throws SizeException, IllegalArgumentException, IOException {
		View view = v;

		// successively apply models on view
		for(ModelInterface t: models){
			View vw = t.applyOn(view, path);
			view = vw;
		}

		return view;
	}

	public ArrayList<ModelInterface> getModels() {
		return models;
	}

	@Override
	public String getMetaInfo(boolean detailed) {

		String meta = this.getMainMeta();

		if(detailed){
			meta+="<p>Models:<p>";
			for(ModelInterface tr: this.models){
				meta+="-----------------------<br>"+tr.getMetaInfo(false) + "<p>";
			}
		}
		return meta;
	}

}
