package models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


public class TransformationModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2003976008472503388L;
	private String name;
	private String datensatz;
	private Date date;
	public static final SimpleDateFormat DF = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public static final HashMap<Class<?>, String> MODELS = new HashMap<Class<?>, String>();
	{
		MODELS.put(FeatureSelectionModel.class, "FS");
		MODELS.put(FeatureWeightsModel.class, "FW");
		MODELS.put(SVMModel.class, "SV");
		MODELS.put(NormalisationModel.class, "NM");
		MODELS.put(KMeansModel.class, "KM");
		MODELS.put(XMeansModel.class, "XM");
		MODELS.put(PolynomialApproximationModel.class, "PA");
		MODELS.put(ModelChain.class, "MC");
		MODELS.put(OrdinalSVMModel.class, "OS");
	}


	public TransformationModel(String name, String datensatz, Date date) {
		super();
		this.name = name;
		this.datensatz = datensatz;
		this.date = date;
	}

/**
 * Creates a Transformationmodel of its class (class specified by the filename-beginning/prefix) from a .model-file
 * 
 * @param file .model-File (serialized file)
 * @return TransformationModel of class SVMModel, FeatureWeightsModel, etc.
 * @throws ClassNotFoundException
 * @throws IOException
 * @throws ParseException
 */
	public static TransformationModel castModel(File file) throws ClassNotFoundException, IOException, ParseException{
		TransformationModel tr = null;
		if(file.getName().startsWith("SV")){
			tr = new SVMModel(file);
		}else if(file.getName().startsWith("FW")){
			tr = new FeatureWeightsModel(file);
		}else if(file.getName().startsWith("FS")){
			tr = new FeatureSelectionModel(file);
		}else if(file.getName().startsWith("NM")){
			tr = new NormalisationModel(file);
		}else if(file.getName().startsWith("KM")){
			tr = new KMeansModel(file);
		}else if(file.getName().startsWith("XM")){
			tr = new XMeansModel(file);
		}else if(file.getName().startsWith("PA")){
			tr = new PolynomialApproximationModel(file);
		}else if(file.getName().startsWith("MC")){
			tr = new ModelChain(file);
		}else if(file.getName().startsWith("OS")){
			tr = new OrdinalSVMModel(file);
		}

		return tr;
	}

/**
 * Method to save (i.e. serialize) the model. Position is specified by filechooser.
 * Returns the path-position where file is finally saved
 * 
 * @param currentPath: normally path where last model was saved 
 * @return path-position where file is finally saved (selected by filechooser, excluding filename)
 * @throws IOException
 */
	public String save(String currentPath) throws IOException{

		File file = new File(this.getName()+".model");
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(currentPath));
		chooser.setSelectedFile(file);
		chooser.setFileFilter(new	FileFilter(){

			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "transformation model (.model)";
			}

		});

		switch (chooser.showSaveDialog(null)) {
		case JFileChooser.APPROVE_OPTION:

			String path2  = chooser.getSelectedFile().getAbsolutePath();
			String path = path2.substring(0, path2.length()-chooser.getSelectedFile().getName().length()).concat(MODELS.get(this.getClass()) + "_" + chooser.getSelectedFile().getName());

			if (path.endsWith(".txt") && this instanceof NormalisationModel){
				NormalisationModel copy = (NormalisationModel) this; 
				PrintWriter printWriter = null;
				try{
					printWriter = new PrintWriter(path);
					printWriter.println(copy.toCSV());
				}
				finally{
					if ( printWriter != null ) 
					{
						printWriter.close();
					}
				}

				// Serialize
				currentPath = chooser.getSelectedFile().getParent();
				break;
			}
			if (!path.endsWith(".model")){
				path += ".model";
			}

			// Serialize
			FileOutputStream fileOS = new FileOutputStream( path );
			ObjectOutputStream o = new ObjectOutputStream( fileOS );
			o.writeObject  ( this );
			o.close();
			currentPath = chooser.getSelectedFile().getParent();
			break;
		default:
			break;
		}

		return currentPath;

	}

	/**
	 * Save model at path-position
	 * 
	 * @param path2: 
	 * @return path-position where file is saved (including filename)
	 * @throws IOException
	 */
	public String saveAtPath(String path2) throws IOException{

		File fu = new File(path2);
//		System.out.println(path2+" "+fu.getName()+" "+fu.getParent()+" ");
//		String path2  = chooser.getSelectedFile().getAbsolutePath();
		
		String path = path2.substring(0, path2.length()-fu.getName().length()).concat(MODELS.get(this.getClass()) + "_" + fu.getName());
		if (!path.endsWith(".model")){
			path += ".model";
		}

		// Serialize
		FileOutputStream fileOS = new FileOutputStream( path );
		ObjectOutputStream o = new ObjectOutputStream( fileOS );
		o.writeObject  ( this );
		o.close();

		return path;

	}


	public TransformationModel(){
		super();
	};

	public String getName() {
		return name;
	}

	public String getDatensatz() {
		return datensatz;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDatensatz(String datensatz) {
		this.datensatz = datensatz;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Method to summarize name, type (class), creation date and viewname of a model
	 * @return String with general meta information
	 */
	public String getMainMeta(){
		return "Name:   "+this.getName()+"<p>Type:   "+this.getClass().getName().substring(7, this.getClass().getName().length())+"<p>Date:   "+TransformationModel.DF.format(this.getDate())+"<p>Datensatz:   "+this.getDatensatz()+"<p>";
	}
}
