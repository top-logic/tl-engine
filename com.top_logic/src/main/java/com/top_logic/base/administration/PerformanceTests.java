/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.administration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.NullWriter;

/**
 * This is the abstract super class of all performance test classes which holds test cases
 * which can be run from the PerformanceTest maintenance page. This class contains some utility
 * methods for the test cases. See the comment of {@link TLPerformanceTests} for a
 * description of the test cases.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public abstract class PerformanceTests {

    /** The line separator of the operating system. */
    public static final String newLine = System.getProperty("line.separator", "\n");

    /**
     * Map containing the active tests and their descriptions, which are accessed by the
     * PerformanceTest maintenance page.
     */
	private final Map<String, String> descriptionMap = Collections.unmodifiableMap(createDescriptionMap());

    /** Map containing the tests and their expected average durations (for information only). */
	private final Map<String, String> durationMap = Collections.unmodifiableMap(createDurationMap());

    /** Saves the writer for test outputs. */
    protected Writer outWriter = NullWriter.INSTANCE;

    /** Indicates whether the test are executed from external (from main method). */
    protected boolean isRunFromExternal = false;


    // Special symbols:
    protected String space = "&nbsp;";
    protected String lineBreak = "<br/>";
    protected String hLine = "<hr/>";
    protected String commentStart = "<!--";
    protected String commentEnd = "-->";


    /**
	 * Creates the test description map.
	 * 
	 * <p>
	 * Each entry has the form:
	 * </p>
	 * 
	 * <code>
	 * test method name --> test description.
	 * </code>
	 */
	protected abstract Map<String, String> createDescriptionMap();

    /**
	 * Creates the test duration map.
	 * 
	 * <p>
	 * Each entry has the form:
	 * </p>
	 * 
	 * <pre>
	 * test method name --> expected average test duration
	 * </pre>
	 * 
	 * <p>
	 * The map may contain no value for some tests to indicate no expected average duration is
	 * available for that test. As default no duration information is available. Subclasses may
	 * override this method.
	 * </p>
	 */
	protected Map<String, String> createDurationMap() {
		return new HashMap<>();
    }


    /**
     * Gets the test description map, which contains entries of this form:<br/>
     * test method name --> test description
     */
    public final Map getDescriptionMap() {
        return descriptionMap;
    }

    /**
     * Gets the test duration map, which contains entries of this form:<br/>
     * test method name --> expected average test duration.
     */
    public final Map getDurationMap() {
        return durationMap;
    }



    // Test run methods

    /**
     * Sets the writer for test outputs.
     *
     * @param aWriter
     *            the writer to use for test outputs.
     */
    public final void setWriter(Writer aWriter) {
        outWriter = (aWriter == null ? NullWriter.INSTANCE : aWriter);
    }


    /**
     * Prepares the given test for run. Per default nothing is done. Subclasses may override.
     * This method gets called before time measurement for the test starts.
     *
     * @param aTest
     *            the test to prepare
     * @throws Exception
     *             if the preparing fails
     */
    protected void prepareTest(String aTest) throws Exception {
        // Nothing to do per default.
    }


    /**
     * Cleans up the given test after run. Per default nothing is done. Subclasses may override.
     * This method gets called after time measurement for the test ends.
     *
     * @param aTest
     *            the test to clean up
     * @throws Exception
     *             if the cleanup fails
     */
    protected void cleanUpTest(String aTest) throws Exception {
        // Nothing to do per default.
    }


    /**
	 * Runs the tests specified by the given collection. Note: Call {@link #setWriter(Writer)} to
	 * configure the output first!
	 *
	 * @param aTestsList
	 *        a Collection of tests to run
	 * @throws IOException
	 *         If an I/O error occurs
	 */
	public void runTests(Collection<String> aTestsList) throws IOException {
        int[] results = new int[4]; // testCount: 0; successCount: 1; failedCount: 2; errorCount: 3;
		Iterator<String> it = aTestsList.iterator();

        long testStartTime = System.currentTimeMillis();
        long time = testStartTime;
        logMessage(time, "Starting tests at " + DebugHelper.formatTime(time) + ".");
        print(hLine);

        while (it.hasNext()) {
			String theName = it.next();
            newLine();
            runTest(theName, results);
        }

        if (results[0] == 0) {
            newLine();
            logMessage("ERROR: No tests were selected!");
        }

        newLine();
        time = System.currentTimeMillis();
        print(hLine + newLine);
        logMessage(time, "Test" + (results[0] == 1 ? "" : "s") + " run: " + results[0] + " (Successes: " + results[1] + ", Failures: " + results[2] + ", Errors: " + results[3] + ").");
        logMessage(time, "Tests finished at " + DebugHelper.formatTime(time) + ". Duration: " + DebugHelper.getTime(System.currentTimeMillis() - testStartTime));
    }

    /**
     * Runs the given test.
     *
     * @param aName
     *            the name of the test to run
     * @param results
     *            an int array with 4 elements, containing 4 counters:<br/>
     *            results[0] = testCounter;<br/>
     *            results[1] = successCounter;<br/>
     *            results[2] = failedCounter;<br/>
     *            results[3] = errorCounter;
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    private void runTest(String aName, int[] results) throws IOException {
        if (aName == null) return;
        logMessage("Running test '" + aName + "'...");
        writeLine();
        try {
            prepareTest(aName);
        }
        catch (Exception e) {
            results[3]++;
            writeLine();
            logMessage("ERROR: Error while preparing the test: " + e.getClass().getName());
            return;
        }
        long time = System.currentTimeMillis();
        results[0]++;
        try {
            Method theTest = getClass().getMethod(aName, new Class[0]);
            Object theResult = theTest.invoke(this, new Object[0]);
            if (theResult == null || Boolean.TRUE.equals(theResult)) {
                results[1]++;
                writeLine();
                logMessage("Test run successfully.");
            }
            else {
                results[2]++;
                writeLine();
                logMessage("WARNING: Test failed.");
            }
        }
        catch (InvocationTargetException e) {
            results[2]++;
            writeLine();
            logMessage("WARNING: Test failed with an exception: " + e.getTargetException().getClass().getName());
            logMessage(0, "Exception message: " + e.getTargetException().getMessage());
        }
        catch (NoSuchMethodException e) {
            results[3]++;
            writeLine();
            logMessage("ERROR: Test not found in class '" + this.getClass().getSimpleName() + "' or super classes.");
        }
        catch (Exception e) {
            results[3]++;
            writeLine();
            logMessage("ERROR: Error while running the test: " + e.getClass().getName());
            logMessage(0, "Exception message: " + e.getMessage());
        }
        finally {
            logMessage("Test duration: " + DebugHelper.getTime(System.currentTimeMillis() - time));
            String theDuration = (String)durationMap.get(aName);
            if (!StringServices.isEmpty(theDuration)) {
                logMessage("Expected average duration: " + theDuration);
            }
        }
        try {
            cleanUpTest(aName);
        }
        catch (Exception e) {
            logMessage("ERROR: Error while cleaning up the test: " + e.getClass().getName());
        }
        newLine();
    }

    /**
     * Runs the given test.
     * Note: Call {@link #setWriter(Writer)} to configure the output first!
     *
     * @param aTest
     *            the test to run
     * @throws IOException
     *             If an I/O error occurs
     */
    public void runTest(String aTest) throws IOException {
        runTest(aTest, new int[4]);
    }


    /**
     * Runs all tests specified by the description map.
     * Note: Call {@link #setWriter(Writer)} to configure the output first!
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    public void runAllTests() throws IOException {
        runTests(getDescriptionMap().keySet());
    }


    /**
     * Runs all tests specified by the description map from the console.<br/>
     * Call this from the main method of the desired PerformanceTests class.
     */
    public void runTestsExternal() {
        // Save current values
        String currentSpace = this.space;
        String currentLineBreak = this.lineBreak;
        String currentLine = this.hLine;
        Writer currentOutWriter = this.outWriter;

        // Set new values
        PrintWriter theWriter = new PrintWriter(System.out);
        this.space = " ";
        this.lineBreak = "";
        this.hLine = "--------------------------------------------------------------------------------";
        this.outWriter = theWriter;
        isRunFromExternal = true;

        // Perform tests
        try {
            runAllTests();
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
        finally {
            theWriter.close();

            // Restore current values
            this.space = currentSpace;
            this.lineBreak = currentLineBreak;
            this.hLine = currentLine;
            this.outWriter = currentOutWriter;
            isRunFromExternal = false;
        }
    }

    /**
     * Writes a string to the output.
     *
     * @param s
     *            the string to write
     * @throws IOException
     *             If an I/O error occurs
     */
    public void print(String s) throws IOException {
        outWriter.write(s);
        outWriter.write(newLine);
		outWriter.flush();
    }

    /**
     * Writes a string to the output and appends a line break.
     *
     * @param s
     *            the string to write
     * @throws IOException
     *             If an I/O error occurs
     */
    public void println(String s) throws IOException {
        outWriter.write(s);
        newLine();
    }

    /**
     * Writes a line break to the output.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    public void newLine() throws IOException {
        outWriter.write(lineBreak);
        outWriter.write(newLine);
		outWriter.flush();
    }

    /**
     * Writes a separator line without time stamp.
     *
     * @throws IOException
     *             If an I/O error occurs
     */
    private void writeLine() throws IOException {
        logMessage(0, "------------------------------------------------------------");
    }

    /**
     * Logs a message preceded by the current time.
     *
     * @param message
     *            the message to log
     * @throws IOException
     *             If an I/O error occurs
     */
    public void logMessage(String message) throws IOException {
        logMessage(System.currentTimeMillis(), message);
    }

    /**
     * Logs a message preceded by a message time.
     *
     * @param time
     *            the time stamp in milliseconds (as given by {@link System#currentTimeMillis()})
     *            to write before the message; may be <= 0 to disable
     * @param message
     *            the message to log
     * @throws IOException
     *             If an I/O error occurs
     */
    public void logMessage(long time, String message) throws IOException {
        if (time > 0) {
            outWriter.write('[');
            outWriter.write(DebugHelper.formatTime(time));
            outWriter.write("]: ");
        }
        else {
            for (int i = 0; i < 16; i++) {
                outWriter.write(space);
            }
        }
        outWriter.write(message);
        newLine();
    }

    /**
     * Logs a time duration.
     *
     * @param starttime
     *            the time in milliseconds when the action to log started (as given by {@link System#currentTimeMillis()})
     * @throws IOException
     *             If an I/O error occurs
     */
    public void logTime(long starttime) throws IOException {
        logMessage("Duration: " + DebugHelper.getTime(System.currentTimeMillis() - starttime));
    }

    /**
     * Logs a time duration.
     *
     * @param message
     *            the message to log with the time
     * @param starttime
     *            the time in milliseconds when the action to log started (as given by {@link System#currentTimeMillis()})
     * @throws IOException
     *             If an I/O error occurs
     */
    public void logTime(String message, long starttime) throws IOException {
        logMessage(message + DebugHelper.getTime(System.currentTimeMillis() - starttime));
    }

}
