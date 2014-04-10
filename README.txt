To run:
1. make sure you copy the keys (invoices - from column E) into "keys.txt"
2. double click on "RUN.bat"
	---


notes:
-The program creates Output.txt and Unfound Keys.txt inside the "results" folder
-This will create Output.txt with the results of searching for each
key in the "S0101_810" folder. 
-All keys which could not be found in the process will be noted in 
"Unfound Keys.txt"



The program now uses relative paths, so as long as the folder names (that contain the files to be searched) dont change, it should work.
	---

If there have been changes to the folder structure, or you want to know what the code does, you can find the 
java code in code/TMMCFinder.java

This file is for reference only, making changes directly to it will do nothing. The code needs to be compiled into a .jar
to work. The code bounces control back and forth between a few batch files, they can all be found in the batch folder or 
main folder

Michael Watkins
12/03/2013
mwatkins@advics-ohio.com
sniktawekim@gmail.com