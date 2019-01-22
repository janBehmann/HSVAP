package data.fileformats;

import java.io.File;
import java.io.IOException;

import data.FileFormat;
import data.View;

public class MakeJSON {
	

	/**
	 * Create JSON View File for given file.
	 * Usage: MakeJSON datafile outputfile format\n\twhere format is the classname of a supported format
	 * 
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		String datafile = "";
		String outfile = "";
		FileFormat format = new MatlabFormat(); 
		
		
		if (args.length != 3) {
			System.err.println("Not enough parameters.\nUsage: MakeJSON datafile outputfile format\n\twhere format is the classname of a supported format");
			return;
		}
		
		datafile = args[0];
		outfile = args[1];
		format = (FileFormat)Class.forName("data.fileformats." + args[2]).newInstance();
		
		
		View view = new FileView(format, new File(datafile));
		view.save(new File(outfile));		
	}

}
