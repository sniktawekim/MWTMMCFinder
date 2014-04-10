package tmmcfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Michael Watkins Dec 3, 2013 sniktawekim@gmail.com for any questions
 */
public class TMMCFinder {

    public static final String drivePath = "J:\\EDI_Shared\\MWTMMCFinder\\";
    public static final String mergedDB = "merged.txt";
    public static final String blockHead = "HDR00"; //indicates a line is a header

    public static String batPre = "batch\\";
    public static String logPre = "logs\\";
    public static String outPre = "results\\";

    public static String workingDirectory = "";

    public static int totalInvoices = 0;

    public static String cleanBat;
    public static String mergeBat;
    public static String outputTxt;
    public static String unfKeysTxt;
    public static String parentPath = "";
    static ArrayList<String> combinedData;
    static ArrayList<String> keys;
    static ArrayList<String> output;
    static ArrayList<String> log;

    static BufferedReader br = null;

    public static void main(String[] args)
            throws IOException, InterruptedException {
        
        combinedData = new ArrayList();//all the contents of the merged files
        keys = new ArrayList();//stores invoices to find in combinedData
        output = new ArrayList();//to create the ISO output file
        log = new ArrayList();//to store any unfound keys

        init();//initialize some settings for the program to smoothly run
        makeMerged();//combine all the iso files into merged.txt
        loadMerged();//load the merged.txt into combinedData arraylist
        loadKeys();//load keys from keys.txt into keys arraylist
        createOutputArrays();//populates output and log arraylists
        buildOutputFiles();//writes output and log arraylists to files

        //a bat to delete the merged.txt, cuz why not.
        runCMD("start " + workingDirectory + cleanBat);
    }

    public static void init() {
        setCD();//configuring current directory

        //building correct path to bat files
        cleanBat = batPre + "clean.bat";
        mergeBat = batPre + "merge.bat";

        //creating correct filenames for output
        String timeStamp = 
                new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(Calendar.getInstance().getTime());
        
        outputTxt = outPre + "IS0101_810_" + timeStamp + ".txt";
        unfKeysTxt = outPre + "Unfound Keys.txt";

        //run the cleanBat to remove sloppy files from last run.
        runCMD("start " + workingDirectory + cleanBat);
    }

    public static void makeMerged() throws IOException, InterruptedException {
        System.out.println("making merged.txt");

        //CMD is much more effecient at file merging than I can do with java
        //so it is better to make CMD merge the files
        runCMD("start " + workingDirectory + mergeBat);//run merge.bat

        File finished = new File(workingDirectory + "finished.txt");

        //merge.bat creates "finished.txt" when it is done. 
        //this loop will pause the java program until the merge.bat
        //has signaled its completion by creating the finished file.
        while (!finished.exists()) {
        }
        System.out.println("finished making Merged.txt");
        finished.delete();//so that it will work again next run
    }

    /**
     * This method simply loads the merged.txt into the combinedData arraylist
     */
    public static void loadMerged() {
        System.out.println("Creating merged ArrayList.");

        try {
            final File folder = 
                    new File(workingDirectory + parentPath + mergedDB);
            String sCurrentLine;
            br = new BufferedReader(new FileReader(folder));

            while ((sCurrentLine = br.readLine()) != null) {
                combinedData.add(sCurrentLine);
            }

            System.out.println("Finished creating merged ArrayList");
        } catch (IOException e) {
            System.out.println("Error in " + workingDirectory + 
                    parentPath + mergedDB + ": " + e);
        }

    }

    /**
     * This method simply loads the keys from keys.txt into the arraylist
     */
    public static void loadKeys() {
        try {
            System.out.println("Loading keys into ArrayList");

            final File folder = new File(workingDirectory + 
                    parentPath + "keys.txt");
            
            String sCurrentLine;
            br = new BufferedReader(new FileReader(folder));

            while ((sCurrentLine = br.readLine()) != null) {
                keys.add(sCurrentLine);
            }

            System.out.println("Finished loading keys into ArrayList");
        } catch (IOException e) {
            
            System.out.println("Error in " + workingDirectory + 
                    parentPath + "keys.txt" + ": " + e);
        }
    }

    /**
     * This method simply writes to files from the arraylists
     */
    private static void buildOutputFiles() {
        try { //output file
            System.out.println("Creating Output file.");
            PrintWriter writer = 
                    new PrintWriter(workingDirectory + outputTxt, "UTF-8");
                    //for output file

            for (int i = 0; i < output.size(); i++) {
                writer.println(output.get(i));
            }
            writer.close();
            System.out.println("Finished creating Output file.");

            System.out.println("Creating Unfounded Keys.txt");
            writer = new PrintWriter(workingDirectory + unfKeysTxt, "UTF-8");
                //for log file
            
            writer.println("Total number: " + log.size());
            for (int i = 0; i < log.size(); i++) {
                writer.println(log.get(i));
            }
            writer.println("Total number of invoices found: " + totalInvoices);
            writer.close();

            System.out.println("Finished creating Unfounded Keys.txt");

        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * This method grabs invoices based on the keys and puts them into the
     * output arraylists
     */
    private static void createOutputArrays() {
        System.out.println("Analyzing merged ArrayList with keys ArrayList");
        
        //really hope this sequence of characters 
       //never shows up as an invoice key:
        String oldKey = "~~~!@#$#@!!@#$!@#$!@~~!@#!@~!~!";
        for (int keyArrayIncrementor = 0;
                keyArrayIncrementor < keys.size();
                keyArrayIncrementor++) {//loop through key file
            
            if (oldKey.compareToIgnoreCase(keys.get(keyArrayIncrementor)) != 0)
            {//check if it is a repeat
                oldKey = keys.get(keyArrayIncrementor);
                boolean found = false;
                for (int dbArrayIncrementor = 0;
                        dbArrayIncrementor < combinedData.size();
                        dbArrayIncrementor++) {//loop through DB file
                    
                    if (combinedData.get(dbArrayIncrementor)
                            .contains(keys.get(keyArrayIncrementor)))
                    {// if DB contains the key
                        
                        System.out.println("FOUND KEY! line:" + dbArrayIncrementor + " key:" + keys.get(keyArrayIncrementor) + " key index:" + keyArrayIncrementor);
                        found = true;
                        try {
                            boolean foundHead = false;
                            int headerOffset = 1;//the matched line will 
                            //be after the header, so the header is at least 1
                            //before the matched line. This means we want to
                            //start looking at the line directly previous to it
                            //for the header, and search further and further
                            //back until we find it.
                            while (dbArrayIncrementor > 0) {

                                if (combinedData.get(dbArrayIncrementor - headerOffset).contains(blockHead)) {//Once we found the header
                                    foundHead = true;

                                    //building small array for this particular
                                    //invoice block. 
                                    ArrayList<String> toAdd = new ArrayList();

                                    int currentLoc = dbArrayIncrementor - headerOffset + 1;
                                    //while the line we are looking at isn't the header
                                    while (!combinedData.get(currentLoc).contains(blockHead) && dbArrayIncrementor < combinedData.size()) {
                                        //if it contains the key we are looking for
                                        if (combinedData.get(currentLoc).contains(keys.get(keyArrayIncrementor))) {
                                            //we want to add it to the block for the iso file
                                            toAdd.add(combinedData.get(currentLoc));//add line   
                                            totalInvoices++;
                                        }
                                        currentLoc++;
                                    }
                                    addNewHeaderLine(dbArrayIncrementor, headerOffset, toAdd.size());

                                    for (int l = 0; l < toAdd.size(); l++) {
                                        output.add(toAdd.get(l));
                                    }
                                    break;
                                } else {//if the line we are currently analyzing
                                    //is not a header
                                    headerOffset++;//increment so we look
                                    //at the line previous to this one
                                    System.out.println("No header before! looking for header " + headerOffset + " lines back");
                                }

                            }
                            if (foundHead) {//if we found the key that matches our invoice
                                break;//we are finished with this key and are ready for the next
                            }
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("Output array out of bounds, i = " + dbArrayIncrementor);
                        }

                    }
                }
                if (found == false) {
                    log.add(keys.get(keyArrayIncrementor));
                }
            }
        }
    }

    /**
     * This method changes the timestamp on a headerline by decrementing an hour
     *
     * @param i is the index of the combinedData array for the header line we
     * are going to change
     */
    private static void addNewHeaderLine(int i, int k, int numEntries) {
        //get old header line
        String headerline = combinedData.get(i - k);

        //get the old time (\\s+ is a regex to grab all whitespace)
        //the split function returns an array, and we are grabbing the text
        //after the 7th instance of \\s+ (6 because arrays start at 0)
        String oldTime = headerline.split("\\s+")[6];//gets the 7th token, the time
        //create new time based on old time
        String newTime = changeTime(oldTime);
        System.out.println("Changed time from " + oldTime + " to " + newTime);
        //this pattern defines the section in the old headerline
        //which is the old time
        Pattern p = Pattern.compile("\\s" + oldTime + "\\s");
        //look for the pattern match in the old header line
        Matcher m = p.matcher(headerline);
        //if you found a match (this MUST happen)
        if (m.find()) {
            //make new time have proper spaces before and after
            //by replacing the oldTime pattern with this newTime int
            newTime = m.group().replace(oldTime, newTime);
            //replace the old time section in the header line with
            //this new time section
            headerline = headerline.replaceFirst(m.group(), newTime);
        }

        String oldEntries;
        oldEntries = headerline.split("\\s+")[4];
        Pattern ep = Pattern.compile("\\s" + oldEntries + "\\s");
        //look for the pattern match in the old header line
        Matcher em = ep.matcher(headerline);
        if (em.find()) {
            String newEntry = em.group().replaceFirst(oldEntries, "" + numEntries);
            headerline = headerline.replace(em.group(), newEntry);
        } else {
            System.out.println("ERROR, entry number not found in HDR! oldEntries:" + oldEntries);
            runCMD("start " + workingDirectory + cleanBat);
            System.exit(1);
        }
        //add the new headerline to the output file array
        output.add(headerline);
    }

    /**
     * This method decrements the time by an hour
     *
     * @param old the string version of the time to be changed
     * @return returns a new string version with the new time
     */
    private static String changeTime(String old) {
        String hourString = "00";
        String minuteString = "00";
        String secondString = "00";

        int hours = Integer.parseInt(old.substring(0, 2));
        int minutes = Integer.parseInt(old.substring(2, 4));
        int seconds = Integer.parseInt(old.substring(4, 6));

        if (hours > 0) {
            hours = hours-1;
            if (hours < 1) {
                return hourString + old.substring(2);
            } else if (hours < 10) {
                return "0" + hours + old.substring(2);
            } else {
                return hours + old.substring(2);
            }
        } else {//transaction happened at 00 hours 
            System.out.println("hours were 0");
            if (minutes > 0) {
                minutes = minutes-1;
                if (minutes < 1) {
                    return hourString + minuteString + old.substring(4);
                } else if (minutes < 10) {
                    return hourString + "0" + minutes + old.substring(4);
                } else {
                    return hourString + minutes + old.substring(4);
                }
            } else {//if hours and minutes are 0s
                System.out.println("minutes were 0");
                if (seconds > 0) {
                    seconds = seconds-1;
                    if (seconds < 1) {
                        return hourString + minuteString + secondString;
                    } else if (seconds < 10) {
                        return hourString + minuteString + "0" + seconds;
                    } else {
                        return hourString + minuteString + seconds;
                }

            } else {
                    return "000000";
                }
        }

    }

}

private static void runCMD(String cmd) {
        try {
            Process pr = Runtime.getRuntime().exec("cmd /c " + cmd);

            pr.waitFor();

        } catch (Exception e1) {
            System.out.println(e1.getMessage());
        







}
    }

    /**
     * This method configures the global variables for use in dealing with the
     * current directory and important paths
     */
    private static void setCD() {
        URL location = TMMCFinder.class  

    .getProtectionDomain().getCodeSource().getLocation();
        String currPath = location.getFile();

    File file = new File(location.getPath());
    parentPath  = file.getParent();
    parentPath  = parentPath.substring(0, parentPath.lastIndexOf("\\")) + "\\";
    //parentPath = currentRelativePath.toAbsolutePath().getParent().toString() + "\\";

    System.out.println (
            

    "parent path:" + parentPath);
        if (currPath.startsWith (
             
        "C:")) {
            workingDirectory = drivePath;
    }

    
        else {
            batPre = parentPath + batPre;
        logPre = parentPath + logPre;
        outPre = parentPath + outPre;
    }
}
}
