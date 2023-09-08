/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * This class holds common test cases which can be run from the PerformanceTest maintenance
 * page. The methods are called with java reflection by the method name. Each test method
 * must have no parameters and must be of return type boolean (or Boolean), which indicates
 * whether the test ran successfully or not. The methods may throw exceptions instead, if
 * the test fails. Each new test method must be inserted into the description map to get
 * activated. Other classes may extend this class to add custom test cases.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class TLPerformanceTests extends PerformanceTests {

	/**
	 * Configuration for {@link TLPerformanceTests}.
	 */
	public interface Config extends ConfigurationItem {
		/**
		 * @see #getTempDir()
		 */
		String TEMP_DIR = "tempDir";

		/**
		 * Path of the temporary directory. Uses {@link FileManager} syntax.
		 */
		@Name(TEMP_DIR)
		@Mandatory
		String getTempDir();
	}

    private static final String FILE_PREFIX = "TLTest";

    /**
     * Main method for external execution of the tests.
     *
     * @param args command line parameters are ignored.
     */
    public static void main(String[] args) {
        new TLPerformanceTests().runTestsExternal();
    }



    // Test descriptions:

    @Override
	protected Map createDescriptionMap() {
		Map<String, String> theMap = new LinkedHashMap<>();
        theMap.put("simpleMath", "This test does some simple mathematical computations.");
        theMap.put("stringManipulation", "This test does some simple string manipulations.");
        theMap.put("memoryAllocation1", "This test allocates many small arrays.");
        theMap.put("memoryAllocation2", "This test makes random accesses in a big array.");
        theMap.put("memoryAllocation3", "This test allocates memory for some objects.");
//        theMap.put("databaseAccess", "This test does some database queries.");
        theMap.put("streaming", "This test writes a big answer page to test server - client network transfer speed.");
        theMap.put("sleep", "This test sleeps for some milliseconds.");
        theMap.put("writeLargeFiles", "Write 2 large files (a 300MB) to temp dir");
        theMap.put("writeSmallFiles", "Write 3000 small files (a 2,5KB) to temp dir");
        theMap.put("readFiles",       "Read all created test files in the temp dir. Reading is performed in blocks of 4KB.");
        theMap.put("deleteFiles",     "Delete all created test files in the temp dir");
        return theMap;
    }

    @Override
	protected Map createDurationMap() {
		Map<String, String> theMap = super.createDurationMap();
        theMap.put("simpleMath", DebugHelper.getTime(7341));
        theMap.put("stringManipulation", DebugHelper.getTime(5726));
        theMap.put("memoryAllocation1", DebugHelper.getTime(1618));
        theMap.put("memoryAllocation2", DebugHelper.getTime(2346));
        theMap.put("memoryAllocation3", DebugHelper.getTime(2940));
//        theMap.put("databaseAccess", null);
        theMap.put("streaming", DebugHelper.getTime(1136) + " (if server is on localhost)");
        theMap.put("sleep", DebugHelper.getTime(2005));
        return theMap;
    }



    // Test preparations:

    @Override
	protected void prepareTest(String aTest) throws Exception {
        if (aTest == null) return;
        if (aTest.startsWith("memoryAllocation")) {
            System.gc();
        }
        if ("streaming".equals(aTest)) {
            outWriter.flush();
        }
    }

    @Override
	protected void cleanUpTest(String aTest) throws Exception {
        if (aTest == null) return;
        if (aTest.startsWith("memoryAllocation")) {
            System.gc();
        }
    }



    // The test methods:

    /**
     * This is an example method for the PerformanceTest maintenance page.<br/> This method
     * only sleeps for a while.
     *
     * @return <code>true</code>, if the test run successfully, <code>false</code>
     *         otherwise (if the sleep was interrupted)
     * @throws Exception
     *             if something goes wrong
     */
    public boolean sleep() throws Exception {
        long sleepTime = 2000;
        try {
            logMessage("Now sleeping...zzZ");
            Thread.sleep(sleepTime);
            return true;
        }
        catch (InterruptedException ex) {
            return false;
        }
    }



    /**
     * This test does some simple mathematical computations.
     */
    public void simpleMath() {
        int count = 100000000;
        double value = 0.0;
        for (int i = 0; i < count; i++) {
            value += Math.random();
        }
    }



    /**
     * This test does some simple string manipulations.
     */
    public void stringManipulation() {
        int count = 100000;
        String s = "";
        for (int i = 0; i < count; i++) {
            s += "x";
        }
        assert s.length() == count;
    }



    /**
     * This test does some database queries.
     */
    public void databaseAccess() throws Exception {
        logMessage("Not implemented yet.");
    }



    /**
     * This test does some memory allocations.
     */
    public void memoryAllocation1() throws Exception {
        memoryAllocation(100000, 256, false);
    }



    /**
     * This test does some memory allocations.
     */
    public void memoryAllocation2() throws Exception {
        memoryAllocation(1, 5000000, true);
    }



    private void memoryAllocation(int arraycount, int arraysize, boolean testRandomly) throws Exception {
        Random fortune = new Random();
		ArrayList theList;

        // allocate the memory
        long time = System.currentTimeMillis();
        logMessage("Allocating memory (" + arraycount + " int array" + (arraycount == 1 ? "" : "s") + " of size " + arraysize + ")...");
        try {
			theList = new ArrayList<>(arraycount);
            for (int i = 0; i < arraycount; i++) {
                int[] theArray = new int[arraysize];
                theList.add(theArray);
            }
        }
        catch (OutOfMemoryError e) {
            theList = null;
            System.gc();
            throw e;
        }
        logTime(time);


        // test memory access straight forward
        if (testRandomly) {

            // write the memory
            time = System.currentTimeMillis();
            logMessage("Using the memory (write randomly)...");
            for (int i = 0; i < arraycount * arraysize; i++) {
                int[] theArray = (int[])theList.get(fortune.nextInt(theList.size()));
                theArray[fortune.nextInt(theArray.length)] = fortune.nextInt();
            }
            logTime(time);

            // read the memory
            time = System.currentTimeMillis();
            logMessage("Using the memory (read randomly)...");
            int theResult = 0;
            for (int i = 0; i < arraycount * arraysize; i++) {
                int[] theArray = (int[])theList.get(fortune.nextInt(theList.size()));
                int j = theArray[fortune.nextInt(theArray.length)];
                if (j > 0) theResult++;
                else if (j < 0) theResult--;
                else theResult += 0;
            }
            // Use the result so that the compiler doesn't optimize this away
            if (theResult < 0) {
				Integer.toString(theResult);
            }
            logTime(time);
        }

        // test memory access randomly
        else {

            // write the memory
            time = System.currentTimeMillis();
            logMessage("Using the memory (write straight forward)...");
            for (int i = 0; i < theList.size(); i++) {
                int[] theArray = (int[])theList.get(i);
                for (int j = 0; j < theArray.length; j++) {
                    theArray[j] = fortune.nextInt();
                }
            }
            logTime(time);

            // read the memory
            time = System.currentTimeMillis();
            logMessage("Using the memory (read straight forward)...");
            int theResult = 0;
            for (int i = 0; i < theList.size(); i++) {
                int[] theArray = (int[])theList.get(i);
                for (int j = 0; j < theArray.length; j++) {
                    int k = theArray[j];
                    if (k > 0) theResult++;
                    else if (k < 0) theResult--;
                    else theResult += 0;
                }
            }
            // Use the result so that the compiler doesn't optimize this away
            if (theResult < 0) {
				Integer.toString(theResult);
            }
            logTime(time);
        }

    }



    /**
     * This test does some memory allocations.
     */
    public void memoryAllocation3() throws Exception {
        Random fortune = new Random();
        int theResult = 0;

        int count = 1000000;
		HashMap<Integer, String> theMap = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
			// Intentional use of constructor to force garbage creation
			theMap.put(Integer.valueOf(i), "Integer: " + i);
        }

        for (int i = 0; i < count; i++) {
			// Intentional use of constructor to force garbage creation
			theMap.remove(Integer.valueOf(fortune.nextInt()));
        }

        System.gc();

        for (int i = 0; i < count; i++) {
			// Intentional use of constructor to force garbage creation
			if (theMap.get(Integer.valueOf(fortune.nextInt())) != null) {
                theResult++;
            }
        }

        // Use the result so that the compiler doesn't optimize this away
        if (theResult < 0) {
			Integer.toString(theResult);
        }
    }



    /**
     * This test does server client streaming.
     */
    public void streaming() throws Exception {
        Writer current = outWriter;
        if (isRunFromExternal) {
            setWriter(null);
        }
        Random fortune = new Random();
        int lines = 1024, columns = 1024;
        int repeats = 10; // amount of MB transfer data
        print("");
        for (int r = 0; r < repeats; r++) {
            print(commentStart);
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < columns; j++) {
                    outWriter.write(fortune.nextInt(26) + (fortune.nextBoolean() ? 65 : 97));
                }
                outWriter.write(newLine);
            }
            print(commentEnd);
        }
        print("");
        if (isRunFromExternal) {
            outWriter = current;
        }
        outWriter.flush();
    }

    /**
     * Write a lot of small files (2,5KB each)
     */
    public void writeSmallFiles() throws Exception {

        char[] content = getContent(2500); // 2500 bytes

        File theDir = ensureTempDir();

        for (int i=0; i<3000; i++) {
            File       theFile = File.createTempFile(FILE_PREFIX + "SmallFile", String.valueOf(i), theDir);
            FileWriter theOut  = new FileWriter(theFile);
            theOut.write(content);
            theOut.flush();
            theOut.close();
        }
    }

    /**
     * Write some large files (300MB each)
     */
    public void writeLargeFiles() throws Exception {

        char[] content = getContent(1000); // 1000 bytes

        File theDir = ensureTempDir();

        for (int i=0; i<2; i++) {
            File       theFile = File.createTempFile(FILE_PREFIX + "LargeFile", String.valueOf(i), theDir);
            FileWriter theOut  = new FileWriter(theFile);
            for (int k=0; k<300; k++) { // 200 * 1000 * 1000 Byte = 200 MB
                for (int l=0; l<1000; l++) { // 1000 * 1000 Byte = 1 MB
                    theOut.write(content);
                }
                theOut.flush();
            }
            theOut.close();
        }
    }

    /**
	 * Read all files starting with {@link #FILE_PREFIX} in the {@link Config#TEMP_DIR}
	 */
    public void readFiles() throws Exception {
        File[] theFiles = this.getTestFiles();
        char[] theBuf   = new char[4000];
        for (int i=0; theFiles != null && i<theFiles.length; i++) {
            File theFile = theFiles[i];
            FileReader theReader = new FileReader(theFile);
            while (theReader.read(theBuf, 0, 4000) > 0) { // $codepro.audit.disable emptyWhileStatement
                // just read on...
            }
            theReader.close();
        }
    }

    /**
	 * Get or create the {@link Config#TEMP_DIR}.
	 */
    private File ensureTempDir() throws Exception {
		String tempDir = getTempDir();
		File theDir = new File(tempDir);
        if (theDir.exists() && ! theDir.isDirectory()) {
            throw new FileNotFoundException(theDir.getAbsolutePath() + " is no directory!");
        }
        if (! theDir.isDirectory()) {
            theDir.mkdir();
        }
        return theDir;
    }

    /**
	 * Get all files in the {@link Config#TEMP_DIR} starting with {@link #FILE_PREFIX}
	 */
    private File[] getTestFiles() throws Exception {
        File theDir = ensureTempDir();
        return theDir.listFiles(new FilenameFilter() {
            @Override
			public boolean accept(File aDir, String aName) {
                return aName.startsWith(FILE_PREFIX);
            }
        });
    }

    /**
	 * Delete all files in the {@link Config#TEMP_DIR}, both do not delete the directory itself
	 */
    public void deleteFiles() throws Exception {
        File[] theFiles = this.getTestFiles();
        for (int i=0; theFiles != null && i<theFiles.length; i++) {
            theFiles[i].delete();
        }
    }

    /**
     * Returns a char[] of the given size
     */
    private char[] getContent(int aSize) {
        char[] content = new char[aSize];
        for (int i=0; i<aSize; i++) {
            content[i] = 'x';
        }
        return content;
    }

	/**
	 * Getter for the configuration.
	 */
	public Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(Config.class);
	}

	/**
	 * Getter for {@link Config#TEMP_DIR}.
	 */
	public String getTempDir() {
		return getConfig().getTempDir();
	}

}
