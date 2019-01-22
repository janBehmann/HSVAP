package data;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Utility class for feature selection in Views.
 * 
 * Contains static methods to find special features, standard features and so on.
 * @author pwelke
 *
 */
public class FeatureSelection {
	
	public static int getXCoordinate(View v) {
		FeatureRole[] roles = v.getFeatureRoles();
		for (int i=0; i<roles.length; ++i) {
			if (roles[i].equals(FeatureRole.X)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int getYCoordinate(View v) {
		FeatureRole[] roles = v.getFeatureRoles();
		for (int i=0; i<roles.length; ++i) {
			if (roles[i].equals(FeatureRole.Y)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int getRGB(View v) {
		FeatureRole[] roles = v.getFeatureRoles();
		for (int i=0; i<roles.length; ++i) {
			if (roles[i].equals(FeatureRole.RGB)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * returns an integer array with indices of all PROB Features
	 * 
	 * @param v	View to search for PROBs
	 * @return	array with indices of PROB features or empty array if none are present
	 */
	public static int[] getProbs(View v){
		FeatureRole[] roles = v.getFeatureRoles();
		int numbers = 0;
		for (int i=0; i<roles.length; ++i) {
			if (roles[i].equals(FeatureRole.PROB)) 
				numbers++;			
		}
		if (numbers>0){
			int[] probs = new int[numbers];
			numbers = 0;
			for (int i=0; i<roles.length; ++i) {
				if (roles[i].equals(FeatureRole.PROB)){ 
					probs[numbers] = i;	
					++numbers;
				}
			}
			return probs;
		}
		
		return null;
		
	}
	
	public static int getLabel(View v) {
		FeatureRole[] roles = v.getFeatureRoles();
		for (int i=0; i<roles.length; ++i) {
			if (roles[i].equals(FeatureRole.LABEL)) {
				return i;
			}
		}
		return -1;
	}
	
	public static int[] getCoordinates(View v) {
		int[] xy = new int[2];
		xy[0] = getXCoordinate(v);
		xy[1] = getYCoordinate(v);
		return xy;
	}
	
	public static int[] getFeatures(View v) {
		int nNonSpecial = 0;
		for (int i=0; i<v.getNumberOfColumns(); ++i) {
			if (v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)) {
				++nNonSpecial;
			}
		}
		int[] nonSpecialFeatures = new int[nNonSpecial];
		nNonSpecial = 0;
		for (int i=0; i<v.getNumberOfColumns(); ++i) {
			if (v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)) {
				nonSpecialFeatures[nNonSpecial] = i;
				++nNonSpecial;				
			}
		}
		return nonSpecialFeatures;
	}
	
	public static int[] getNonSpecialFeatures(View v) {
		int nNonSpecial = 0;
		for (int i=0; i<v.getNumberOfColumns(); ++i) {
			if (v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)) {
				boolean doubleTest = true;
				try{
				String waveString=v.getFeatureDescriptors()[i].replaceAll(Pattern.quote("}"),"");
				waveString=waveString.replaceAll(Pattern.quote("{"),"");
				Double.parseDouble(waveString.trim().split(" ")[waveString.trim().split(" ").length-1]);
				}catch(NumberFormatException nfe){  
					doubleTest=false;  
				}  
				
				if(doubleTest)
					++nNonSpecial;
			}
		}
		int[] nonSpecialFeatures = new int[nNonSpecial];
		nNonSpecial = 0;
		for (int i=0; i<v.getNumberOfColumns(); ++i) {
			if (v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)) {
				boolean doubleTest = true;
				try{
				String waveString=v.getFeatureDescriptors()[i].replaceAll(Pattern.quote("}"),"");
				waveString=waveString.replaceAll(Pattern.quote("{"),"");
				Double.parseDouble(waveString.trim().split(" ")[waveString.trim().split(" ").length-1]);
				}catch(NumberFormatException nfe){  
					doubleTest=false;  
				} 
				
				if(doubleTest){
					nonSpecialFeatures[nNonSpecial] = i;
					++nNonSpecial;
				}
			}
		}
		return nonSpecialFeatures;
	}
	
	
	/**
	 * Returns an array a of indices of all special features present in a View v.
	 * 
	 * a.length equals the number of special features in v. The array is sorted. 
	 * That means that the special features are returned in the order that they occur in v.
	 * @param v
	 * @return
	 * @author pwelke
	 */
	public static int[] getSpecialFeatures(View v) {
		int[] probs = getProbs(v);
		int konst;
		if (probs!= null)
			konst = probs.length;
		else
			konst = 0;
		
		int[] special = new int[4+konst];
		
			
		special[0] = getXCoordinate(v);
		special[1] = getYCoordinate(v);
		special[2] = getLabel(v);
		special[3] = getRGB(v);
		if (probs != null){
			for (int i = 4; i<special.length;i++)
				special[i] = probs[i-4];
		}
		
		int notFound = 0;
		for (int i=0; i<special.length; ++i) {
			if (special[i] == -1) {
				++notFound;
			}
		}
		
		if (notFound != 0) {
			// some special features are missing, create new array containing only indices of existing ones
			int[] existingSpecial = new int[special.length - notFound];
			int j = 0;
			for (int i=0; i<special.length; ++i) {
				if (special[i] != -1) {
					existingSpecial[j] = special[i];
					++j;
				}
			}
			special = existingSpecial;
		}
		Arrays.sort(special);
		return special;
	}
	
	/**
	 * Calculates and returns the ENVI VIs from http://geol.hu/data/online_help/Vegetation_Indices.html 
	 * @author jbehmann
	 * @param view - the data View from which the features are calculates
	 * @return the VIs (rows are samples/ columns are 20 or 26 VIs)
	 */
	public static double[][]  extractENVIFeatures(View v){
		double[][] spectra=v.materializeFeatures(getNonSpecialFeatures(v));
		String[] featureNames =v.getFeatureDescriptors();
		Double[] wavelength=new Double[getNonSpecialFeatures(v).length];
		int count = 0;
		for (int i=0; i<featureNames.length;i++){
			if(v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)){
				wavelength[count]= Double.valueOf(featureNames[i]);
				count++;
			}
		}
		
		boolean wide = false;
		if(Collections.max(Arrays.asList(wavelength))>1000){
			wide=true;
		}
		
		// Wenn mehr als 1000nm dann wide = true
		//boolean wide =true;
		double[][] features = new double[spectra.length][27];
		
		
		int red_index=getIndex(670.0,wavelength);
		int red_inf_index=getIndex(800.0,wavelength);
		int blue_index=getIndex(490.0,wavelength);
		int Index_500=getIndex(500.0,wavelength);
		int Index_600=getIndex(600.0,wavelength);
			
		//Broadband indices
		for (int i = 0; i< spectra.length; i++){
			//			Normalized Difference Vegetation Index
			//		Normalized difference of green leaf scattering in near-infrared, chlorophyll absorption in RED.
			features[i][0]= (spectra[i][red_inf_index]-spectra[i][red_index])/(spectra[i][red_inf_index]+spectra[i][red_index]);
			//			Simple Ratio Index
			//		Ratio of green leaf scattering in near-infrared, chlorophyll absorption in RED.
			features[i][1]= (spectra[i][red_inf_index])/(spectra[i][red_index]);
			//			Enhanced Vegetation Index
			//		An enhancement on the NDVI to better account for soil background and atmospheric aerosol effects.
			features[i][2]= 2.5*(spectra[i][red_inf_index]-spectra[i][red_index])/(spectra[i][red_inf_index]+6*spectra[i][red_index]-5.5*spectra[i][blue_index]+1);
			//			Atmospherically Resistant Vegetation Index
			//		An enhancement of the NDVI to better account for atmospheric scattering.			
			features[i][3]= (spectra[i][red_inf_index]-(2*spectra[i][red_index]-spectra[i][blue_index]))/(spectra[i][red_inf_index]+(2*spectra[i][red_index]-spectra[i][blue_index]));
			//			Sum Green Index
			//		
			double sum = 0;
			for (int j = Index_500; j<=Index_600; j++){
				sum += spectra[i][j];
			}
			features[i][4]= sum/(Index_600-Index_500+1);			
		}
		
		//Narrowband Greenness
		
		
		
		int r750=getIndex(750.0,wavelength);
		int r705=getIndex(705.0,wavelength);
		int r445=getIndex(445.0,wavelength);
		int r740=getIndex(740.0,wavelength);
		int r720=getIndex(720.0,wavelength);
		int r734=getIndex(734.0,wavelength);
		int r747=getIndex(747.0,wavelength);
		int r715=getIndex(715.0,wavelength);
		int r726=getIndex(726.0,wavelength);
		int r670=getIndex(670.0,wavelength);
		int r780=getIndex(780.0,wavelength);
		int r700=getIndex(700.0,wavelength);
		
		for (int i = 0; i< spectra.length; i++){
			//		Red Edge Normalized Difference Vegetation Index
			//		A modification of the NDVI using reflectance measurements along the red edge
			features[i][5]= (spectra[i][r750]-spectra[i][r705])/(spectra[i][r750]+spectra[i][r705]);
			//		Modified Red Edge Simple Ratio Index
			//		A ratio of reflectance along the red edge with blue reflection correction.
			features[i][6]= (spectra[i][r750]-spectra[i][r445])/(spectra[i][r750]+spectra[i][r445]);
			//		Modified Red Edge Normalized Difference Vegetation Index
			//		A modification of the Red Edge NDVI using blue to compensate for scattered light.
			features[i][7]= (spectra[i][r750]-spectra[i][r705])/(spectra[i][r750]+spectra[i][r705]-2*spectra[i][r445]);
			//		Vogelmann Red Edge Index 1
			//		A shoulder of the RED-to-NIR transition that is indicative of canopy stress.
			features[i][8]= (spectra[i][r740])/(spectra[i][r720]);
			//		Vogelmann Red Edge Index 2
			//		A shape of the near-infrared transition that is indicative of the onset of canopy stress and senescence.
			features[i][9]= (spectra[i][r734]-spectra[i][r747])/(spectra[i][r715]+spectra[i][r726]);
			//		Vogelmann Red Edge Index 3
			//		A shape of near-infrared transition that is indicative of the onset of canopy stress and senescence.
			features[i][10]= (spectra[i][r734]-spectra[i][r747])/(spectra[i][r715]+spectra[i][r720]);
			//		Red Edge Position Index
			//		The location of the maximum derivative in near-infrared transition, which is sensitive to chlorophyll concentration
			//		Here: Linearisierung nach BARET und GUYOT, 1991
			double R_re= (spectra[i][r670]+spectra[i][r780])/2;
			features[i][11]=700+40*((R_re + spectra[i][r700] ) / (spectra[i][r740]- spectra[i][r700]));
			
		}
		
		
		
		//Light Use Efficiency
		int r531=getIndex(531.0,wavelength);
		int r570=getIndex(570.0,wavelength);
		int r680=getIndex(680.0,wavelength);
		int r800=getIndex(800.0,wavelength);
		int r1510=getIndex(1510.0,wavelength);
		int r1680=getIndex(1680.0,wavelength);
		int r500=getIndex(500.0,wavelength);
		int r2000=getIndex(2000.0,wavelength);
		int r2200=getIndex(2200.0,wavelength);
		int r2100=getIndex(2100.0,wavelength);
		int r1754=getIndex(1754.0,wavelength);
		for (int i = 0; i< spectra.length; i++){
			//		Photochemical Reflectance Index
			//		Useful to estimate absorption by leaf carotenoids (especially xanthophyll) pigments, leaf stress, and carbon dioxide uptake.
			features[i][12]= (spectra[i][r531]-spectra[i][r570])/(spectra[i][r531]+spectra[i][r570]);
			//		Structure Insensitive Pigment Index
			//		Indicator of leaf pigment concentrations normalized for variations in overall canopy structure and foliage content.
			features[i][13]= (spectra[i][r800]-spectra[i][r445])/(spectra[i][r800]-spectra[i][r680]);
			//		Red Green Ratio Index
			//		Ratio of reflectance in RED-to-GREEN sensitive to ratio of anthocyanin to chlorophyll.
			double sum_green=0;
			for (int j = Index_500; j<=Index_600; j++){
				sum_green += spectra[i][j];
			}
			sum_green= sum_green/(Index_600-Index_500+1);
			double sum_red=0;
			for (int j = Index_600; j<=r700; j++){
				sum_red += spectra[i][j];
			}
			sum_red= sum_red/(r700-Index_600+1);			
			features[i][14]= sum_red/sum_green;
			
			//Canopy Nitrogen
			//	Normalized Difference Nitrogen Index
			if (wide){
				features[i][15]=(Math.log(1/spectra[i][r1510])- Math.log(1/spectra[i][r1680]))/(Math.log(1/spectra[i][r1510])+ Math.log(1/spectra[i][r1680]));
			//Dry or Senescent Carbon
				//Normalized Difference Lignin Index
				features[i][16]=(Math.log(1/spectra[i][r1754])- Math.log(1/spectra[i][r1680]))/(Math.log(1/spectra[i][r1754])+ Math.log(1/spectra[i][r1680]));
				//Cellulose Absorption Index
				features[i][17]=0.5*((spectra[i][r2000]-spectra[i][r2200])/spectra[i][r2100]);
			}
			
			//	Plant Senescence Reflectance Index
			//		Uses a ratio of carotenoids to chlorophyll to detect onset and degree of plant senescense.
			features[i][18]= (spectra[i][r680]-spectra[i][r500])/spectra[i][r750];			
		}
		
		
		//LEAF PIGMENTS
		
		int r510=getIndex(510.0,wavelength);
		int r550=getIndex(550.0,wavelength);
		int r900=getIndex(900.0,wavelength);
		int r970=getIndex(970.0,wavelength);
		int r819=getIndex(819.0,wavelength);
		int r1649=getIndex(1649.0,wavelength);
		int r1599=getIndex(1599.0,wavelength);
		int r857=getIndex(857.0,wavelength);
		int r1241=getIndex(1241.0,wavelength);
		
		
		for (int i = 0; i< spectra.length; i++){
			//		Carotenoid Reflectance Index 1
			//		Detects a relative difference in absorption indicative of changes in leaf total carotenoid concentration relative to chlorophyll concentration.features[i][20]= (spectra[i][r531]-spectra[i][r570])/(spectra[i][531]+spectra[i][570]);
			features[i][19]=(1/spectra[i][r510])-(1/spectra[i][r550]);
			//		Carotenoid Reflectance Index 2
			//		Similar to CRI1, but uses a different wavelength to estimate the chlorophyll content.
			features[i][20]= (1/spectra[i][r510])-(1/spectra[i][r700]);
			//		Anthocyanin Reflectance Index 1
			//		Changes in GREEN absorption relative to RED indicate leaf anthocyanins.
			features[i][21]= (1/spectra[i][r550])-(1/spectra[i][r700]);
			//		Anthocyanin Reflectance Index 2
			//		A variant of the ARI1, which is sensitive to changes in GREEN absorption relative to RED, indicating leaf anthocyanins.
			features[i][22]= spectra[i][r800]*((1/spectra[i][r550])-(1/spectra[i][r700]));
			
			
			
					
					
			if (wide){
				//Canopy Water Content
				//		Water Band Index
				//		Absorption intensity at 900 nm increases with canopy water content.
				features[i][23]=spectra[i][r900]/spectra[i][r970];
				//Normalized Difference Water Index
				//The rate of increase at 857 nm absorption relative to 1241 nm is a direct metric of total volumetric water content of vegetation.
				features[i][24]=(spectra[i][r857]-spectra[i][r1241])/(spectra[i][r857]+spectra[i][r1241]);
				//Moisture Stress Index
				//Detects changes at 1599 nm absorption that is sensitive to the onset of moisture stress in vegetation.
				features[i][25]=spectra[i][r1599]/spectra[i][r819];
				//Normalized Difference Infrared Index
				//Absorption intensity at 1649 nm increases with canopy water content.
				features[i][26]=(spectra[i][r819]-spectra[i][r1649])/(spectra[i][r819]+spectra[i][r1649]);
				
			}					
		}
		
		
		return features;
		
	}
	
	/**
	 * Returns a double array with the description of all Non-Special Features
	 * Checks if it is possible to transform them to double values, else returns an array numerated from
	 * 1 to numberOfFeatures
	 * @param v	view with data from which to get Feature Descriptors
	 * @return	double array
	 * 
	 * @author croemer
	 */
	public static double[] getNonSpecialFeatureDescriptor(View v){
		String[] descriptors = v.getFeatureDescriptors();
		int[] nsFeatures = FeatureSelection.getNonSpecialFeatures(v);
		
		try{
			double [] nsFeatureDesc = new double[nsFeatures.length];
			for (int i = 0; i < nsFeatures.length; i++)
				nsFeatureDesc[i] = Double.parseDouble(descriptors[nsFeatures[i]]);
			
			return nsFeatureDesc;
		} catch (NumberFormatException nfe){
			double [] nsFeatureDesc = new double[nsFeatures.length];
			for (int i = 0; i < nsFeatures.length; i++)
				nsFeatureDesc[i] = i;
			
			return nsFeatureDesc;
		}				
	}
	
	public static String[] getNonSpecialFeatureDescriptorString(View v){
		String[] descriptors = v.getFeatureDescriptors();
		int[] nsFeatures = FeatureSelection.getNonSpecialFeatures(v);
				
		String [] nsFeatureDesc = new String[nsFeatures.length];
		for (int i = 0; i < nsFeatures.length; i++)
			nsFeatureDesc[i] =descriptors[nsFeatures[i]];
		
		return nsFeatureDesc;	
	}
	
	/**
	 * Finds the nearest wavlength in wls to the location wl
	 * @author jbehmann
	 * @param wl requestes wavength
	 * @param wls available band
	 * @return the corresponding index of wls	 * 
	 */
	private static int getIndex(double wl, Double[] wls){
		double distance=Double.MAX_VALUE;
		int result=Integer.MIN_VALUE;
		for (int i =0; i<wls.length;i++){
			double local_distance = Math.abs(wls[i]-wl);
			if(local_distance< distance){
				distance = local_distance;
				result=i;
			}
		}
		return result;
	}
	
	/**
	 * Finds the nearest wavlength in wls to the location wl
	 * @author jbehmann & Axel Forsch
	 * @param wl requestes wavength
	 * @param wls available band
	 * @param sigma defines the maximum distance a wavelength is allowed to have from the searched wavelenght to be valid
	 * @return the corresponding index of wls or -1 if no wavelength in sigma-environment is found
	 */
	private static int getIndex(double wl, Double[] wls, double sigma){
		double distance=Double.MAX_VALUE;
		int result=-1; // Error value, wavelength is more than sigma away
		for (int i =0; i<wls.length;i++){
			double local_distance = Math.abs(wls[i]-wl); // distance of the current wl from the searched wl
			if( local_distance < distance && local_distance < sigma ){ // the wl is better than the last one and in sigma range
				distance = local_distance;
				result=i; // save current wl
			}
		}
		return result; 
	}
	
	/**
	 * Calculates and returns the ENVI VIs from http://geol.hu/data/online_help/Vegetation_Indices.html 
	 * @author jbehmann & Axel Forsch
	 * @param view - the data View from which the features are calculates
	 * @return the VIs (rows are samples/ columns are 20 or 26 VIs)
	 */
	public static double[][]  extractENVIFeatures(View v, double sigma){
		double[][] spectra=v.materializeFeatures(getNonSpecialFeatures(v));
		String[] featureNames =v.getFeatureDescriptors();
		Double[] wavelength=new Double[getNonSpecialFeatures(v).length];
		int count = 0;
		for (int i=0; i<featureNames.length;i++){
			if(v.getFeatureRoles()[i].equals(FeatureRole.FEATURE)){
				try {
					wavelength[count]= Double.valueOf(featureNames[i]);
					count++;
				} catch (NumberFormatException noWavelength ) {
					// TODO Was soll passieren?
				}
			}
		}

		int nF = Integer.MIN_VALUE;

		double[][] features = new double[spectra.length][27];

		int red_index=getIndex(670.0,wavelength,sigma);
		int red_inf_index=getIndex(800.0,wavelength,sigma);
		int blue_index=getIndex(490.0,wavelength,sigma);
		int Index_500=getIndex(500.0,wavelength,sigma);
		int Index_600=getIndex(600.0,wavelength,sigma);

		//Broadband indices
		for (int i = 0; i< spectra.length; i++){
			//			Normalized Difference Vegetation Index
			//		Normalized difference of green leaf scattering in near-infrared, chlorophyll absorption in RED.
			try {
				features[i][0]= (spectra[i][red_inf_index]-spectra[i][red_index])/(spectra[i][red_inf_index]+spectra[i][red_index]);
				//			Simple Ratio Index
				//		Ratio of green leaf scattering in near-infrared, chlorophyll absorption in RED.
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][0] = nF;
			}
			try {
				features[i][1]= (spectra[i][red_inf_index])/(spectra[i][red_index]);
				//			Enhanced Vegetation Index
				//		An enhancement on the NDVI to better account for soil background and atmospheric aerosol effects.
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][1] = nF;
			}
			try {
				features[i][2]= 2.5*(spectra[i][red_inf_index]-spectra[i][red_index])/(spectra[i][red_inf_index]+6*spectra[i][red_index]-5.5*spectra[i][blue_index]+1);
				//			Atmospherically Resistant Vegetation Index
				//		An enhancement of the NDVI to better account for atmospheric scattering.			
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][2] = nF;
			}
			try {
				features[i][3]= (spectra[i][red_inf_index]-(2*spectra[i][red_index]-spectra[i][blue_index]))/(spectra[i][red_inf_index]+(2*spectra[i][red_index]-spectra[i][blue_index]));
				//			Sum Green Index
				//		
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][3] = nF;
			}
			try {
				double sum = 0;
				for (int j = Index_500; j<=Index_600; j++){
					sum += spectra[i][j];
				}
				features[i][4]= sum/(Index_600-Index_500+1);		
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][4] = nF;
			}
		}

		//Narrowband Greenness



		int r750=getIndex(750.0,wavelength,sigma);
		int r705=getIndex(705.0,wavelength,sigma);
		int r445=getIndex(445.0,wavelength,sigma);
		int r740=getIndex(740.0,wavelength,sigma);
		int r720=getIndex(720.0,wavelength,sigma);
		int r734=getIndex(734.0,wavelength,sigma);
		int r747=getIndex(747.0,wavelength,sigma);
		int r715=getIndex(715.0,wavelength,sigma);
		int r726=getIndex(726.0,wavelength,sigma);
		int r670=getIndex(670.0,wavelength,sigma);
		int r780=getIndex(780.0,wavelength,sigma);
		int r700=getIndex(700.0,wavelength,sigma);

		for (int i = 0; i< spectra.length; i++){
			try {
				//		Red Edge Normalized Difference Vegetation Index
				//		A modification of the NDVI using reflectance measurements along the red edge
				features[i][5]= (spectra[i][r750]-spectra[i][r705])/(spectra[i][r750]+spectra[i][r705]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][5] = nF;
			}
			try {
				//		Modified Red Edge Simple Ratio Index
				//		A ratio of reflectance along the red edge with blue reflection correction.
				features[i][6]= (spectra[i][r750]-spectra[i][r445])/(spectra[i][r750]+spectra[i][r445]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][6] = nF;
			}
			try {
				//		Modified Red Edge Normalized Difference Vegetation Index
				//		A modification of the Red Edge NDVI using blue to compensate for scattered light.
				features[i][7]= (spectra[i][r750]-spectra[i][r705])/(spectra[i][r750]+spectra[i][r705]-2*spectra[i][r445]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][7] = nF;
			}
			try {
				//		Vogelmann Red Edge Index 1
				//		A shoulder of the RED-to-NIR transition that is indicative of canopy stress.
				features[i][8]= (spectra[i][r740])/(spectra[i][r720]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][8] = nF;
			}
			try {
				//		Vogelmann Red Edge Index 2
				//		A shape of the near-infrared transition that is indicative of the onset of canopy stress and senescence.
				features[i][9]= (spectra[i][r734]-spectra[i][r747])/(spectra[i][r715]+spectra[i][r726]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][9] = nF;
			}
			try {
				//		Vogelmann Red Edge Index 3
				//		A shape of near-infrared transition that is indicative of the onset of canopy stress and senescence.
				features[i][10]= (spectra[i][r734]-spectra[i][r747])/(spectra[i][r715]+spectra[i][r720]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][10] = nF;
			}
			try {
				//		Red Edge Position Index
				//		The location of the maximum derivative in near-infrared transition, which is sensitive to chlorophyll concentration
				//		Here: Linearisierung nach BARET und GUYOT, 1991
				double R_re= (spectra[i][r670]+spectra[i][r780])/2;
				features[i][11]=700+40*((R_re + spectra[i][r700] ) / (spectra[i][r740]- spectra[i][r700]));
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][11] = nF;
			}
		}



		//Light Use Efficiency
		int r531=getIndex(531.0,wavelength,sigma);
		int r570=getIndex(570.0,wavelength,sigma);
		int r680=getIndex(680.0,wavelength,sigma);
		int r800=getIndex(800.0,wavelength,sigma);
		int r1510=getIndex(1510.0,wavelength,sigma);
		int r1680=getIndex(1680.0,wavelength,sigma);
		int r500=getIndex(500.0,wavelength,sigma);
		int r2000=getIndex(2000.0,wavelength,sigma);
		int r2200=getIndex(2200.0,wavelength,sigma);
		int r2100=getIndex(2100.0,wavelength,sigma);
		int r1754=getIndex(1754.0,wavelength,sigma);
		for (int i = 0; i< spectra.length; i++){
			try {
				//		Photochemical Reflectance Index
				//		Useful to estimate absorption by leaf carotenoids (especially xanthophyll) pigments, leaf stress, and carbon dioxide uptake.
				features[i][12]= (spectra[i][r531]-spectra[i][r570])/(spectra[i][r531]+spectra[i][r570]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][12] = nF;
			}
			try {
				//		Structure Insensitive Pigment Index
				//		Indicator of leaf pigment concentrations normalized for variations in overall canopy structure and foliage content.
				features[i][13]= (spectra[i][r800]-spectra[i][r445])/(spectra[i][r800]-spectra[i][r680]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][13] = nF;
			}
			try {
				//		Red Green Ratio Index
				//		Ratio of reflectance in RED-to-GREEN sensitive to ratio of anthocyanin to chlorophyll.
				double sum_green=0;
				for (int j = Index_500; j<=Index_600; j++){
					sum_green += spectra[i][j];
				}
				sum_green= sum_green/(Index_600-Index_500+1);
				double sum_red=0;
				for (int j = Index_600; j<=r700; j++){
					sum_red += spectra[i][j];
				}
				sum_red= sum_red/(r700-Index_600+1);			
				features[i][14]= sum_red/sum_green;
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][14] = nF;
			}
			try {
				//Canopy Nitrogen
				//	Normalized Difference Nitrogen Index
				features[i][15]=(Math.log(1/spectra[i][r1510])- Math.log(1/spectra[i][r1680]))/(Math.log(1/spectra[i][r1510])+ Math.log(1/spectra[i][r1680]));
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][15] = nF;
			}
			try {
				//Dry or Senescent Carbon
				//Normalized Difference Lignin Index
				features[i][16]=(Math.log(1/spectra[i][r1754])- Math.log(1/spectra[i][r1680]))/(Math.log(1/spectra[i][r1754])+ Math.log(1/spectra[i][r1680]));
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][16] = nF;
			}
			try {
				//Cellulose Absorption Index
				features[i][17]=0.5*((spectra[i][r2000]-spectra[i][r2200])/spectra[i][r2100]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][17] = nF;
			}
			try {

				//	Plant Senescence Reflectance Index
				//		Uses a ratio of carotenoids to chlorophyll to detect onset and degree of plant senescense.
				features[i][18]= (spectra[i][r680]-spectra[i][r500])/spectra[i][r750];			
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][18] = nF;
			}
		}


		//LEAF PIGMENTS

		int r510=getIndex(510.0,wavelength,sigma);
		int r550=getIndex(550.0,wavelength,sigma);
		int r900=getIndex(900.0,wavelength,sigma);
		int r970=getIndex(970.0,wavelength,sigma);
		int r819=getIndex(819.0,wavelength,sigma);
		int r1649=getIndex(1649.0,wavelength,sigma);
		int r1599=getIndex(1599.0,wavelength,sigma);
		int r857=getIndex(857.0,wavelength,sigma);
		int r1241=getIndex(1241.0,wavelength,sigma);


		for (int i = 0; i< spectra.length; i++){
			try {
				//		Carotenoid Reflectance Index 1
				//		Detects a relative difference in absorption indicative of changes in leaf total carotenoid concentration relative to chlorophyll concentration.features[i][20]= (spectra[i][r531]-spectra[i][r570])/(spectra[i][531]+spectra[i][570]);
				features[i][19]=(1/spectra[i][r510])-(1/spectra[i][r550]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][19] = nF;
			}
			try {
				//		Carotenoid Reflectance Index 2
				//		Similar to CRI1, but uses a different wavelength to estimate the chlorophyll content.
				features[i][20]= (1/spectra[i][r510])-(1/spectra[i][r700]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][20] = nF;
			}
			try {
				//		Anthocyanin Reflectance Index 1
				//		Changes in GREEN absorption relative to RED indicate leaf anthocyanins.
				features[i][21]= (1/spectra[i][r550])-(1/spectra[i][r700]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][21] = nF;
			}
			try {
				//		Anthocyanin Reflectance Index 2
				//		A variant of the ARI1, which is sensitive to changes in GREEN absorption relative to RED, indicating leaf anthocyanins.
				features[i][22]= spectra[i][r800]*((1/spectra[i][r550])-(1/spectra[i][r700]));
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][22] = nF;
			}
			try {
				//Canopy Water Content
				//		Water Band Index
				//		Absorption intensity at 900 nm increases with canopy water content.
				features[i][23]=spectra[i][r900]/spectra[i][r970];
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][23] = nF;
			}
			try {
				//Normalized Difference Water Index
				//The rate of increase at 857 nm absorption relative to 1241 nm is a direct metric of total volumetric water content of vegetation.
				features[i][24]=(spectra[i][r857]-spectra[i][r1241])/(spectra[i][r857]+spectra[i][r1241]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][24] = nF;
			}
			try {
				//Moisture Stress Index
				//Detects changes at 1599 nm absorption that is sensitive to the onset of moisture stress in vegetation.
				features[i][25]=spectra[i][r1599]/spectra[i][r819];
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][25] = nF;
			}
			try {
				//Normalized Difference Infrared Index
				//Absorption intensity at 1649 nm increases with canopy water content.
				features[i][26]=(spectra[i][r819]-spectra[i][r1649])/(spectra[i][r819]+spectra[i][r1649]);
			} catch ( ArrayIndexOutOfBoundsException notFound ) {
				features[i][26] = nF;	
			}					
		}
		return features;
	}
	
			
	/**
	 * Query if a view contains coordinates x,y 
	 * @author schmitter
	 * @param v queried view 
	 * @return true if this view contains coordinates, else false
	 */
	public static boolean containsCoordinates(View v){
		boolean coordinate = false;
		if (getXCoordinate( v) != -1 && getYCoordinate( v) != -1)
			coordinate = true;

		return coordinate;
	}
}
