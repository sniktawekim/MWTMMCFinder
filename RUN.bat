@pushd \\Adssap1\edi_shared\MWTMMCFinder
@chdir code


java -jar "TMMCFinder.jar" > ../logs/runlog.txt
@echo Process completed. Look in results folder for output files.
@pause
