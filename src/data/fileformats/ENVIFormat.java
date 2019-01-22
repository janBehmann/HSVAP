package data.fileformats;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.HashMap;

import javax.imageio.stream.ImageInputStreamImpl;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.jblas.DoubleMatrix;
import org.json.JSONObject;

import data.FeatureRole;
import data.FileFormat;
import data.Utilities;
import data.View;
import data.inmemory.DoubleMatrixView;

/**
 * FileFormat to read ENVI hyperspectral data files
 * 
 * @author pwelke
 *
 */
public class ENVIFormat implements FileFormat {

	private File header;

	/**
	 * Argument free constructor for persistence. 
	 * 
	 * Note: You may use this constructor to create a view on an envi file at /bla/morebla/data.bla .
	 * Then, the implementation will try to find a header at /bla/morebla/data.hdr .
	 * If the header file has a different name or extension, you have to use the one argument 
	 * constructor instead.
	 */
	public ENVIFormat() {
		header = null;
	}


	/**
	 * Read ENVI files that are described by the given header file
	 * @param header
	 */
	public ENVIFormat(File header) {
		this.header = header;
	}

	@Override
	public View writeData(View view, File file) throws IOException {
		// TODO
		throw new IOException("Writing ENVII files is not implemented");
	}


	/**
	 * Assures that there is a non-null header file reference.
	 * If header is null, then set header file to same file name with extension .hdr
	 * @param file
	 */
	private void assertHeader(File file) {
		if (header == null) {
			String fileNameWithOutExt = file.getName().replaceFirst("[.][^.]+$", "");
			if (file.getParentFile() != null) {
				header = new File(file.getParent()+ File.separator + fileNameWithOutExt + ".hdr");
			} else {
				header = new File(fileNameWithOutExt + ".hdr");
			}
		}
	}


	/**
	 * Read an ENVI file using the header file that was specified when creating 
	 * this FileFormat object.
	 */
	@Override
	public View readData(File file) throws IOException {
		assertHeader(file);
		HashMap<String, String> props = readHeader(header);

		DoubleMatrix data = readEnvi(file, props);		
		View view = new DoubleMatrixView(data, file.getAbsolutePath());
		System.out.println("Data loaded");
		view.setFeatureRoles(Utilities.createFeatureRoleArray(view.getNumberOfColumns()));
		view.setFeatureDescriptors(Utilities.createGenericFeatureNames(view.getNumberOfColumns()));
		view.setFeatureDescriptor(0, FeatureRole.Y.toString());
		view.setFeatureDescriptor(1, FeatureRole.X.toString());
		view.setFeatureRole(0, FeatureRole.Y);
		view.setFeatureRole(1, FeatureRole.X);
		int rgbIndex = data.columns-1;
		view.setFeatureRole(rgbIndex, FeatureRole.RGB);
		view.setFeatureDescriptor(rgbIndex, FeatureRole.RGB.toString());


		if (props.containsKey("wavelength")) {
			// set right wavelength names, if existing

			String textWls = props.get("wavelength");
			if (textWls.startsWith("{")){
				textWls=textWls.substring(1, textWls.length()-1);
			}
			if (textWls.endsWith("}")){
				textWls=textWls.substring(0, textWls.length()-2);
			}
			props.put("wavelength", textWls);


			String[] wavelengths = props.get("wavelength").split(",");
			for (int i=0; i<wavelengths.length; ++i) {
				view.setFeatureDescriptor(i+2,wavelengths[i].trim());
			}	
			// set rgb accordingly
			double[] rgb = Utilities.materializeRGB(view);
			for (int i=0; i<rgb.length; ++i) {
				data.put(i, rgbIndex, rgb[i]);
			}

		} else {
			view.setFeatureDescriptors(Utilities.createGenericFeatureNames(view.getNumberOfColumns()));
		}
		return view;
	}

	public String getHeaderPosition() { return header.getPath(); }

	@Override
	public String getInformation() {
		return "ENVII file format";
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}

	/**
	 * Read binary ENVI file using the information in a header file given by props.
	 * 
	 * @author Jan Behmann
	 * @since 2013-10-11 changes by Pascal Welke:
	 * @param location of the data file file
	 * @param props Properties from header file
	 * @return DoubleMatrix
	 */
	public static DoubleMatrix readEnvi(File data, HashMap<String, String> props) throws IOException {

		float[][][] hsBild = readENVIBinFloat(data, props);
		// plus three for x, y, and rgb
		DoubleMatrix dataCube = DoubleMatrix.zeros(hsBild.length * hsBild[0].length, hsBild[0][0].length + 3);

		// write pixel positions
		int rowCounter = 0;
		for (int i = 0; i< hsBild.length;i++){
			for (int j = 0; j< hsBild[0].length;j++){
				dataCube.put(rowCounter, 0, i);
				dataCube.put(rowCounter, 1, j);
				rowCounter++;
			}
		}

		// write features
		rowCounter = 0;
		for (int i = 0; i<hsBild.length; i++){
			for (int j = 0; j<hsBild[0].length; j++){
				for (int k = 0; k<hsBild[0][0].length; k++){
					dataCube.put(rowCounter, k + 2, (double) hsBild[i][j][k]);
				}
				rowCounter++;
			}
		}

		// create rgb values
		View temp = new DoubleMatrixView(dataCube.getRange(0, dataCube.rows, 2, dataCube.columns-1), "temp view for rgb computation");
		temp.setFeatureDescriptors(props.get("wavelength").split(","));
		double[] rgb = Utilities.materializeRGB(temp);
		for (int i=0; i<rgb.length; ++i) {
			dataCube.put(i, dataCube.columns-1, rgb[i]);
		}

		return dataCube;
	}


	/**
	 * Read Envi binary Dat-File
	 * 
	 * @author Jan Behmann
	 * @since 2013-10-11 changes by Pascal Welke:
	 * @param location: of the Dat-file 
	 * @param props: the information of the header file
	 * @return Class MatrixStorage: loaded DataCube
	 */
	public static int[][][] readENVIBin(File location, HashMap<String,String> props) throws IOException {
		int bandanzahl = Integer.valueOf(props.get("bands"));
		int zeilenanzahl = Integer.valueOf(props.get("lines"));
		int spaltenanzahl = Integer.valueOf(props.get("samples"));
		int dataType = Integer.valueOf(props.get("data type"));
		FileInputStream fis = null;
		ImageInputStreamImpl iisI = null;
		int[][][] hsBild = new int [zeilenanzahl][spaltenanzahl][bandanzahl];

		if (props.get("interleave").equalsIgnoreCase("bil")){
			try {
				fis = new FileInputStream(location);
				iisI = new MemoryCacheImageInputStream(fis);
				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				for (int row=0; row<zeilenanzahl; row++){
					for (int band=0; band<bandanzahl; band++){
						for (int col=0; col<spaltenanzahl; col++){
							if (dataType == 12){
								hsBild[row][col][band] = iisI.readUnsignedShort();
							}
							else{
								hsBild[row][col][band] = (int) (Integer.MAX_VALUE*iisI.readFloat());
							}
						}
					}
				}
			} finally {
				iisI.close();
				fis.close();
			}
		}

		if (props.get("interleave").equalsIgnoreCase("bsq")){
			try {
				fis = new FileInputStream(location);
				iisI = new MemoryCacheImageInputStream(fis);
				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				for (int band=0; band<bandanzahl; band++){
					for (int row=0; row<zeilenanzahl; row++){
						for (int col=0; col<spaltenanzahl; col++){
							if (dataType == 12){
								hsBild[row][col][band] = iisI.readUnsignedShort();
							}
							else{
								hsBild[row][col][band] = (int) (Integer.MAX_VALUE*iisI.readFloat());
							}
						}
					}
				}
			} finally {
				iisI.close();
				fis.close();
			}
		}

		if (props.get("interleave").equalsIgnoreCase("bip")){
			try {
				fis = new FileInputStream(location);
				iisI = new MemoryCacheImageInputStream(fis);
				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				for (int row=0; row<zeilenanzahl; row++){
					for (int col=0; col<spaltenanzahl; col++){
						for (int band=0; band<bandanzahl; band++){
							if (dataType == 12){
								hsBild[row][col][band] = iisI.readUnsignedShort();
							}
							else{
								hsBild[row][col][band] = (int) (Integer.MAX_VALUE*iisI.readFloat());
							}
						}
					}
				}
			} finally {
				fis.close();
				iisI.close();
			}
		}
		return hsBild;
	}


	/**
	 * Read Envi binary Dat-File
	 * 
	 * @author Jan Behmann
	 * @since 2013-10-11 changes by Pascal Welke:
	 * @param location: of the Dat-file 
	 * @param props: the information of the header file
	 * @return Class MatrixStorage: loaded DataCube
	 */
	public static double[][][] readENVIBinDouble(File location, HashMap<String,String> props) throws IOException {
		int bandanzahl = Integer.valueOf(props.get("bands"));
		int zeilenanzahl = Integer.valueOf(props.get("lines"));
		int spaltenanzahl = Integer.valueOf(props.get("samples"));
		int dataType = Integer.valueOf(props.get("data type"));
//		FileInputStream fis = null;
//		ImageInputStreamImpl iisI = null;
		FileInputStream fis = new FileInputStream(location);
		BufferedInputStream bin = new BufferedInputStream(fis);
		DataInput din;
		if(Integer.valueOf(props.get("byte order"))==0){
			din= new LittleEndianDataInputStream(bin);
		}
		else{
			din = new DataInputStream(bin);	
		}
//		DataInputStream din = new DataInputStream(bin);
		
		double[][][] hsBild = new double [zeilenanzahl][spaltenanzahl][bandanzahl];
		System.out.println("Binary reading started");
		if (props.get("interleave").equalsIgnoreCase("bil")){
			try {
//				fis = new FileInputStream(location);
//				iisI = new MemoryCacheImageInputStream(fis);
//				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				for (int row=0; row<zeilenanzahl; row++){
					for (int band=0; band<bandanzahl; band++){
						for (int col=0; col<spaltenanzahl; col++){
							if (dataType == 12){
								hsBild[row][col][band] = (double) din.readUnsignedShort();
							}
							else{
								hsBild[row][col][band] = din.readFloat();
							}
						}
					}


					System.out.print("\rBinary reading at "+(int) ((row/(double)zeilenanzahl)*100)+"%");



				}
			} finally {
				((InputStream) din).close();
				bin.close();
				fis.close();
			}
		}

		if (props.get("interleave").equalsIgnoreCase("bsq")){
			try {
//				fis = new FileInputStream(location);
//				BufferedInputStream bin = new BufferedInputStream(fis);
//				DataInputStream din = new DataInputStream(bin);
				//				iisI = new MemoryCacheImageInputStream(fis);
				//				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				if (dataType == 12){
					for (int band=0; band<bandanzahl; band++){
						for (int row=0; row<zeilenanzahl; row++){
							for (int col=0; col<spaltenanzahl; col++){

								hsBild[row][col][band] = (double) din.readUnsignedShort();

							}
						}
						System.out.print("\rBinary reading at "+(int) ((band/(double)bandanzahl)*100)+"%");


					}
				}
				else{

					for (int band=0; band<bandanzahl; band++){
						for (int row=0; row<zeilenanzahl; row++){
							for (int col=0; col<spaltenanzahl; col++){


								hsBild[row][col][band] = din.readFloat();

							}
						}
						System.out.print("\rBinary reading at "+(int) ((band/(double)bandanzahl)*100)+"%");


					}
				}
				} finally {
					((InputStream) din).close();
					bin.close();
					fis.close();
				}
			}

			if (props.get("interleave").equalsIgnoreCase("bip")){
				try {
//					fis = new FileInputStream(location);
//					iisI = new MemoryCacheImageInputStream(fis);
//					iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
					for (int row=0; row<zeilenanzahl; row++){
						for (int col=0; col<spaltenanzahl; col++){
							for (int band=0; band<bandanzahl; band++){
								if (dataType == 12){
									hsBild[row][col][band] = (double) din.readUnsignedShort();
								}
								else{
									hsBild[row][col][band] = din.readFloat();
								}
							}
						}
						//					System.out.print("\r                                                          \r");
						System.out.print("\rBinary reading at "+(int) ((row/(double)zeilenanzahl)*100)+"%");


					}
				} finally {
					((InputStream) din).close();
					bin.close();
					fis.close();
				}
			}
			System.out.println("");
			System.out.println("Binary reading completed");
			return hsBild;
		}

	
	/**
	 * Read Envi binary Dat-File
	 * 
	 * @author Jan Behmann
	 * @since 2013-10-11 changes by Pascal Welke:
	 * @param location: of the Dat-file 
	 * @param props: the information of the header file
	 * @return Class MatrixStorage: loaded DataCube
	 */
	public static float[][][] readENVIBinFloat(File location, HashMap<String,String> props) throws IOException {
		int bandanzahl = Integer.valueOf(props.get("bands"));
		int zeilenanzahl = Integer.valueOf(props.get("lines"));
		int spaltenanzahl = Integer.valueOf(props.get("samples"));
		int dataType = Integer.valueOf(props.get("data type"));
//		FileInputStream fis = null;
//		ImageInputStreamImpl iisI = null;
		FileInputStream fis = new FileInputStream(location);
		BufferedInputStream bin = new BufferedInputStream(fis);
		DataInput din;
		if(Integer.valueOf(props.get("byte order"))==0){
			din= new LittleEndianDataInputStream(bin);
		}
		else{
			din = new DataInputStream(bin);	
		}
//		DataInputStream din = new DataInputStream(bin);
		
		float[][][] hsBild = new float [zeilenanzahl][spaltenanzahl][bandanzahl];
		System.out.println("Binary reading started");
		if (props.get("interleave").equalsIgnoreCase("bil")){
			try {
//				fis = new FileInputStream(location);
//				iisI = new MemoryCacheImageInputStream(fis);
//				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				for (int row=0; row<zeilenanzahl; row++){
					for (int band=0; band<bandanzahl; band++){
						for (int col=0; col<spaltenanzahl; col++){
							if (dataType == 12){
								hsBild[row][col][band] = (float) din.readUnsignedShort();
							}
							else{
								hsBild[row][col][band] = din.readFloat();
							}
						}
					}


					System.out.print("\rBinary reading at "+(int) ((row/(double)zeilenanzahl)*100)+"%");



				}
			} finally {
				((InputStream) din).close();
				bin.close();
				fis.close();
			}
		}

		if (props.get("interleave").equalsIgnoreCase("bsq")){
			try {
//				fis = new FileInputStream(location);
//				BufferedInputStream bin = new BufferedInputStream(fis);
//				DataInputStream din = new DataInputStream(bin);
				//				iisI = new MemoryCacheImageInputStream(fis);
				//				iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
				if (dataType == 12){
					for (int band=0; band<bandanzahl; band++){
						for (int row=0; row<zeilenanzahl; row++){
							for (int col=0; col<spaltenanzahl; col++){

								hsBild[row][col][band] = (float) din.readUnsignedShort();

							}
							
						}
						System.out.print("\rBinary reading at "+(int) ((band/(double)bandanzahl)*100)+"%");


					}
				}
				else{

					for (int band=0; band<bandanzahl; band++){
						for (int row=0; row<zeilenanzahl; row++){
							for (int col=0; col<spaltenanzahl; col++){


								hsBild[row][col][band] = din.readFloat();

							}
						}
						System.out.print("\rBinary reading at "+(int) ((band/(double)bandanzahl)*100)+"%");


					}
				}
				} finally {
					((InputStream) din).close();
					bin.close();
					fis.close();
				}
			}

			if (props.get("interleave").equalsIgnoreCase("bip")){
				try {
//					fis = new FileInputStream(location);
//					iisI = new MemoryCacheImageInputStream(fis);
//					iisI.setByteOrder(ByteOrder.LITTLE_ENDIAN);
					for (int row=0; row<zeilenanzahl; row++){
						for (int col=0; col<spaltenanzahl; col++){
							for (int band=0; band<bandanzahl; band++){
								if (dataType == 12){
									hsBild[row][col][band] = (float) din.readUnsignedShort();
								}
								else{
									hsBild[row][col][band] = din.readFloat();
								}
							}
						}
						//					System.out.print("\r                                                          \r");
						System.out.print("\rBinary reading at "+(int) ((row/(double)zeilenanzahl)*100)+"%");


					}
				} finally {
					((InputStream) din).close();
					bin.close();
					fis.close();
				}
			}
			System.out.println("");
			System.out.println("Binary reading completed");
			return hsBild;
		}


		/**
		 * Read Envi Ascii Header-File
		 * 
		 * @author Jan Behmann
		 * @since 2013-10-11 changes by Pascal Welke:
		 * @param location: of the hdr-file 
		 * @return HashMap<String, String>: the information of the header file
		 */
		public static HashMap<String, String> readHeader(File location) throws IOException {
			HashMap<String, String> props = new HashMap<String, String>();       
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(location));
				String line = reader.readLine();
				if (line.trim().equals("ENVI")) {
					// repeat until all lines are read
					while ((line = reader.readLine()) != null) {
						if (line.contains("=")) {
							String[] contents = line.split("=");
							String value = contents[1].trim();
							if (value.equals("{")) {
								StringBuffer b = new StringBuffer();
								for (line=reader.readLine(); !line.contains("}"); line=reader.readLine()) {
									b.append(line.trim());
								}
								// add last line
								b.append(line.trim());
								b.delete(b.lastIndexOf("}"), b.length());
								props.put(contents[0].trim().toLowerCase(), b.toString());
							} else {
								props.put(contents[0].trim().toLowerCase(), value);
							}
						} else {
							if (line.trim().equals("")) {
								continue;
							} else {
								System.out.println("Parse-Error: "+line);
								//							throw new IOException("Parsing error of envii header");
								continue;
							}
						}
					}
				} else {
					throw new IOException(location.getName() + " seems to be no envii header");
				}
			} finally {
				reader.close();
			}
			System.out.println("Parsing of header file succesful");
			return props;
		}


		@Override
		public void initializeValues(JSONObject o) {
			if (o.has("headerPosition")) {
				this.header = new File(o.getString("headerPosition"));
			}
		}

	}
