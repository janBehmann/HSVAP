MakeJSON.jar is a command line tool to generate a view description file in JSON format 
for any supported data file format.

This folder contains three files.

1.) this readme 
2.) MakeJSON.xml an ant file to compile the jar
3.) MakeJSON.jar an executable jar file.

USAGE:

Usage: 
java -jar MakeJSON datafile outputfile format

	where format is the classname of a supported format,
	datafile is the path to a file containing hyperspectral data, and
	outputfile is the path to the JSON file that will be created.
	
	
@author: pwelke