package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.ImageIcon;

import models.TransformationModel;
import data.fileformats.MatlabFormat;
import data.inmemory.DoubleMatrixView;
import data.operators.TrainView;

public class Project{

	private Date date;			//date of creation
	private Date aenderungsdatum;		//date of last alteration
	private String description;
	public static final SimpleDateFormat DF = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private int activeView = -1;
	private int trainView = -1;
	private ArrayList<TransformationModel> models;
	private ArrayList<String> matPaths;    //String representation of each view
	private ArrayList<String> matPaths_TV;    //String representation of each trainview
	private ArrayList<View> viewList;
	private ArrayList<TrainView> tViewList;
	
	/**
	 * Creates an empty project whose dates are the actual date and whose description is shown in the program as "Set description here"
	 */
	public Project() {
		viewList=new ArrayList<View>();
		tViewList = new ArrayList<TrainView>();
		this.date = new Date();
		this.aenderungsdatum = new Date();
		this.description = "Set description here";
	}

/**
 * This Constructor reads in all of the Views and models of the project.
 * This is the Parser for the .project file. See method Project.save(String) for grammar/syntax/structure.
 * 
 * @param file  txt-based file with a certain structure, see save method
 * @throws IOException
 * @throws ClassNotFoundException
 * @throws InstantiationException
 * @throws IllegalAccessException
 * @throws ParseException
 */
	public Project(File file) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ParseException{
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		String line = br.readLine();
		String[] parts = line.split(";");

		viewList=new ArrayList<View>();
		tViewList = new ArrayList<TrainView>();
		String[] pfadeViews = parts[2].trim().substring(1, parts[2].length()-2).split(",");
		String[] pfadeTrainViews = parts[3].trim().substring(1, parts[3].length()-2).split(",");
		String[] pfadeModels = parts[4].trim().substring(1, parts[4].length()-2).split(",");
		br.close();

		this.date = DF.parse(parts[0].trim());
		this.aenderungsdatum = DF.parse(parts[1].trim());

		FileInputStream fileIS = new FileInputStream( new File(parts[5].trim()) );
		ObjectInputStream o = new ObjectInputStream( fileIS );
		@SuppressWarnings("unchecked")
		ArrayList<ImageIcon> thumbnails = (ArrayList<ImageIcon>) o.readObject();
		o.close();
		
		FileInputStream fileIS_TV = new FileInputStream( new File(parts[6].trim()) );
		ObjectInputStream o_TV = new ObjectInputStream( fileIS_TV );
		@SuppressWarnings("unchecked")
		ArrayList<ImageIcon> thumbnails_TV = (ArrayList<ImageIcon>) o_TV.readObject();
		o_TV.close();
		
		if(parts.length>7){
			this.description="";
			for(int i=7;i<parts.length;i++){
				if(i==7){
					this.description += parts[i].trim()+";";
				}else{
					this.description += parts[i]+";";
				}
			}
			this.description = this.description.substring(0, this.description.length()-1);
		}else{
			this.description = parts[7].trim();
		}

		for(int i=0; i<pfadeViews.length;i++){
			View view = null;
			try{
				view = Utilities.createViewFromJSON(new File(pfadeViews[i].trim()));
				view.setThumbnail(thumbnails.get(i).getImage());
				view.setSaved(true);
				this.add(view,pfadeViews[i].trim());

			}catch(Exception ins){
//				ins.printStackTrace();
			}
		}

		for(int i=0; i<pfadeTrainViews.length;i++){
			TrainView view = null;
			try{
				view = TrainView.createTrainViewFromView(Utilities.createViewFromJSON(new File(pfadeTrainViews[i].trim())));
				view.setThumbnail(thumbnails_TV.get(i).getImage());
				view.setSaved(true);
				this.addTrainView(view,pfadeTrainViews[i].trim());

			}catch(Exception ins){
//				ins.printStackTrace();
			}
		}
		
		for(int i=0; i<pfadeModels.length;i++){
			TransformationModel tr = TransformationModel.castModel(new File(pfadeModels[i].trim()));
			this.getModels().add(tr);
		}
	}
	
	/**
	 * Returns the Index of the trainView or -1 if there's no trainView set.
	 * @return index of the trainview in the instance of Project, i.e. ArrayList<View>
	 */
		public int getTrainViewIndex() {
			if ((!tViewList.isEmpty())) {
				return trainView;
			} else {
				return -1;
			}
		}



		/**
		 * Set the trainView to the view at position given by index. 
		 * if index is negative or larger than any valid index, an 
		 * {@link ArrayIndexOutOfBoundsException} is thrown.
		 * @param index
		 */
		public void setTrainView(int index) {
			if ((index >= 0) && (index < tViewList.size())) {
				trainView = index;
			} else {
				throw new ArrayIndexOutOfBoundsException(index);
			}
		}
	
		/**
		 * Set train view to first occurrence of view in project. 
		 * If project does not contain view, 
		 * @param v
		 */
		public void setTrainView(View v) {
			
			int index = this.tViewList.indexOf(v);
			if (index != -1) {
				setTrainView(index);
			} else {
				throw new IllegalArgumentException(v.getViewName() + " is not contained in Project.");
			}


		}
		
		
	/**
	 * Return the train view, or null, if the Project does not contain any views or there is no train View.
	 * At creation of a project, the active view is initialized to the view with index
	 * 0.
	 * @return
	 */
	public TrainView getTrainView(){
		if ((!tViewList.isEmpty()) && trainView!=-1)
			return getTViewList().get(trainView);
		else
			return null;		
	}

	/**
	 * Return the active view, or null, if the Project does not contain any views or there is no active View.
	 * At creation of a project, the active view is initialized to the view with index
	 * 0.
	 * @return
	 */
	public View getActiveView() {
		if ((!viewList.isEmpty()) && activeView!= -1) {
			return viewList.get(activeView);
		} else {
			return null;
		}
	}

/**
 * Returns the Index of the active View or -1 if there's no active View set.
 * @return index of the active view in the instance of Project, i.e. ArrayList<View>
 */
	public int getActiveViewIndex() {
		if ((!viewList.isEmpty())) {
			return activeView;
		} else {
			return -1;
		}
	}



	/**
	 * Set the active view to the view at position given by index. 
	 * if index is negative or larger than any valid index, an 
	 * {@link ArrayIndexOutOfBoundsException} is thrown.
	 * @param index
	 */
	public void setActiveView(int index) {
		if ((index >= 0) && (index < viewList.size())) {
			activeView = index;
		} else {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	/**
	 * Sets -1 as index of active View, if there is no active View
	 */
	public void setUnselected(){
		activeView = -1;
	}


	/**
	 * Set active view to first occurrence of view in project. 
	 * If project does not contain view, 
	 * @param v
	 */
	public void setActiveView(View v) {
		ArrayList<View> list = new ArrayList<View>();
		for(View m: viewList){
			list.add(m);
		}
		int index = list.indexOf(v);
		if (index != -1) {
			setActiveView(index);
		} else {
			throw new IllegalArgumentException(v.getViewName() + " is not contained in Project.");
		}


	}

	public Date getDate() {
		return date;
	}

	public Date getAenderungsdatum(){
		return aenderungsdatum;
	}

	public String getDescription(){
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	/**
	 * This method writes all paths of views and models as well as project-description, dates and thumbnails into a txt-based file (.project).
	 * Therefore it materializes all Views, serializes all models and serializes each view's thumbnail.
	 * The thumbnails are serialized in a separate file whose path is written in the .project file.
	 * The two lists are printed via the ArrayList.toString() methods, so like that: [a, b, c]
	 * The elements date, aenderungsdatum, pathListViews, pathListModels, thumbFileName and description are separated with ";"
	 * 
	 * @param txtFileName path where project will be saved
	 * @return path where project was saved (possibly ".project" has been appended)
	 * @throws IOException
	 */
	public String save(String txtFileName) throws IOException{

		this.aenderungsdatum = new Date();

		if(!txtFileName.endsWith(".project")){
			txtFileName += ".project";
		}

		ArrayList<String> pathListViews = new ArrayList<String>(); 
		ArrayList<ImageIcon> thumbnails = new ArrayList<ImageIcon>(); 
		int i = 1;
		for(int j=0; j<viewList.size(); j++){
			View v = viewList.get(j);
			String fileName = txtFileName.substring(0,txtFileName.length()-8) + "_" + i + ".view";
			File file = new File(fileName);
			String fileName2 = txtFileName.substring(0,txtFileName.length()-7) + "_" + i + ".mat";
			File file2 = new File(fileName2);
			getMatPaths();
			if(matPaths.size()<=j || matPaths.get(j).isEmpty()){
				
				View neu = new DoubleMatrixView(Utilities.materializeAsDoubleMatrix(v), v.getViewName());
				neu.setViewDescription(v.getViewDescription());
				neu.setFeatureDescriptors(v.getFeatureDescriptors().clone());
				neu.setFeatureRoles(v.getFeatureRoles().clone());
				neu.getLabelMap().putAll(v.getLabelMap());
				neu.saveMaterialized(file, file2, new MatlabFormat());
				getMatPaths().set(j,file.getAbsolutePath());
				viewList.get(j).setSaved(true);
			}
			v.setSaved(true);

			pathListViews.add(getMatPaths().get(j));

			thumbnails.add(new ImageIcon(v.getThumbnail()));

			i++;
		}
		ArrayList<String> pathListViews_TV = new ArrayList<String>(); 
		ArrayList<ImageIcon> thumbnails_TV = new ArrayList<ImageIcon>(); 
		i = 1;
		for(int j=0; j<this.getTViewList().size(); j++){
			View v = this.getTViewList().get(j);
			String fileName = txtFileName.substring(0,txtFileName.length()-8) + "_TV_" + i + ".tv";
			File file = new File(fileName);
			String fileName2 = txtFileName.substring(0,txtFileName.length()-7) + "_TV_" + i + ".mat";
			File file2 = new File(fileName2);
			getMatPaths_TV();
			if(matPaths_TV.size()<=j || matPaths_TV.get(j)==null || matPaths_TV.get(j).isEmpty()){
				
				View neu = new DoubleMatrixView(Utilities.materializeAsDoubleMatrix(v), v.getViewName());
				neu.setViewDescription(v.getViewDescription());
				neu.setFeatureDescriptors(v.getFeatureDescriptors().clone());
				neu.setFeatureRoles(v.getFeatureRoles().clone());
				neu.getLabelMap().putAll(v.getLabelMap());
				
				neu.saveMaterialized(file, file2, new MatlabFormat());
				getMatPaths_TV().set(j,file.getAbsolutePath());
				viewList.get(j).setSaved(true);
			}
			v.setSaved(true);

			pathListViews_TV.add(getMatPaths_TV().get(j));

			thumbnails_TV.add(new ImageIcon(v.getThumbnail()));

			i++;
		}

		ArrayList<String> pathListModels = new ArrayList<String>(); 
		int j = 1;
		for(TransformationModel tr: this.getModels()){	
			if (tr!=null){
				String fileName = txtFileName.substring(0,txtFileName.length()-8) + "_" + j + ".model";
				File file2 = new File(fileName);
				String finalFileName  = file2.getParent().concat(File.separator+TransformationModel.MODELS.get(tr.getClass())+"_"+file2.getName());
				File file = new File(finalFileName);
	
				FileOutputStream fileOS = new FileOutputStream( file );
				ObjectOutputStream o = new ObjectOutputStream( fileOS );
				o.writeObject  (tr);
				o.close();
	
				pathListModels.add(file.getAbsolutePath());
	
				j++;
			}
		}
		
		String thumbFileName = txtFileName.substring(0,txtFileName.length()-8)+"_thumb.ser";
		FileOutputStream fileOS = new FileOutputStream( thumbFileName );
		ObjectOutputStream o = new ObjectOutputStream( fileOS );
		o.writeObject  ( thumbnails_TV );
		o.close();
		
		String thumbFileName_TV = txtFileName.substring(0,txtFileName.length()-8)+"_thumb_TV.ser";
		FileOutputStream fileOS_TV = new FileOutputStream( thumbFileName_TV );
		ObjectOutputStream o_TV = new ObjectOutputStream( fileOS_TV );
		o_TV.writeObject  ( thumbnails_TV );
		o_TV.close();
		
		File file = new File(txtFileName);
		FileWriter fw = new FileWriter(file , true);
		fw.write(DF.format(this.date)+"; "+DF.format(this.aenderungsdatum)+"; "+pathListViews.toString()+"; "+pathListViews_TV.toString()+"; "+pathListModels.toString()+"; "+thumbFileName+"; "+thumbFileName_TV+"; "+this.description);
		fw.close();

		return file.getAbsolutePath();
	}

	public ArrayList<TransformationModel> getModels() {
		if (this.models == null){
			models = new ArrayList<TransformationModel>();
		}
		return models;
	}
	
	
	public boolean add(View e) {   //for same size???
//		ArrayList<String> newPaths = new ArrayList<String>(matPaths.size()+1);
//		for(int i=0; i<this.size(); i++){
//			newPaths.add(matPaths.get(i));
//		}
		
//		System.out.println(e.);
		if(e.getClass()==TrainView.class){
			getMatPaths_TV().add("");  //od null?
			return getTViewList().add((TrainView) e);
		}else{
			getMatPaths().add("");  //od null?
			return viewList.add(e);
		}
	}
	

	public boolean addTrainView(TrainView e, String s) {
			getMatPaths_TV().add(s);
			return getTViewList().add(e);	
	}
	
	private boolean add(View e, String s) {
		if(e.getClass()==TrainView.class){
			getMatPaths_TV().add(s);
			return getTViewList().add((TrainView) e);
		}else{
			getMatPaths().add(s);
			return viewList.add(e);
		}
		
	}
	
	
	public View remove(int arg0) {
		matPaths.remove(arg0);
		return viewList.remove(arg0);
	}

	public ArrayList<String> getMatPaths(){
		if(matPaths == null){
			matPaths = new ArrayList<String>(viewList.size());
		}
		return matPaths;
	}


	/**
	 * @return the viewList
	 */
	public ArrayList<View> getViewList() {
		return viewList;
	}

	/**
	 * @param viewList the viewList to set
	 */
	public void setViewList(ArrayList<View> viewList) {
		this.viewList = viewList;
	}

	

	/**
	 * @param tViewList the tViewList to set
	 */
	public void settViewList(ArrayList<TrainView> tViewList) {
		this.tViewList = tViewList;
	}

	public void setPath(int i, String absolutePath) {
		getMatPaths().set(i, absolutePath);
	}

	/**
	 * @return the tViewList
	 */
	public ArrayList<TrainView> getTViewList() {
		if(tViewList == null){
			tViewList = new ArrayList<TrainView>();
		}
		return tViewList;
	}

	/**
	 * @param tViewList the tViewList to set
	 */
	public void setTViewList(ArrayList<TrainView> tViewList) {
		this.tViewList = tViewList;
	}

	/**
	 * @return the matPaths_TV
	 */
	public ArrayList<String> getMatPaths_TV() {
		if(matPaths_TV == null){
			matPaths_TV = new ArrayList<String>(this.getTViewList().size());
		}
		return matPaths_TV;
	}

	/**
	 * @param matPaths_TV the matPaths_TV to set
	 */
	public void setMatPaths_TV(ArrayList<String> matPaths_TV) {
		this.matPaths_TV = matPaths_TV;
	}

}
