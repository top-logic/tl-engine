/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * Utility class for tailing a text file. 
 * 
 * It'll write out all the things, you write into the given file.
 *
 * Minor comment changes.
 *
 * @author    Michael Gänsler
 */
public class Tail {

    /** Time to sleep, when no new data at in (in millisec). */
    private static final long SLEEP_TIME = 100;

    /** The observed file. */
    private BufferedReader file;

    /** The stream to write the info to. */
    private PrintWriter out;

    /** The stream to write the info to. */
    private boolean stop;

    /**
     * Main CTor actually used in the end.
     *
     * @param    aReader  Reader we read from.
     * @param    anOut    The writer to write data to.
     * @throws   IllegalArgumentException    If given file doesn't exists.
     *
     * @since 1.2
     */
    private Tail (Reader aReader, PrintWriter anOut) {
        if (aReader instanceof BufferedReader) {
            this.file = (BufferedReader) aReader;
        } else {
            this.file = new BufferedReader (aReader);
        }
        this.out  = anOut;
    }

    /**
     * String representation of this instance. This can be used for debugging.
     *
     * @return    The string representation of this instance.
     */
    @Override
	public String toString () {
        return (this.getClass ().getName () + " ["
                        + "file: " + this.file
                        + ", out: " + this.out
                        + ']');
    }

    /**
     * Read lines from input and write anything not filtered to output
     */
    public void observe () {
        try {
            while (!stop) {
                String theString = this.file.readLine ();

                if (theString != null || !this.filter (theString)) {
                    this.out.println (theString);
                }
                else {
                    try {
                        Thread.sleep (SLEEP_TIME);
                    } 
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            this.file.close();
        }
        catch (Exception ex) {
             ex.printStackTrace();
        }
    }

    /**
     * Checks, whether a given string should be filtered of written.
     *
     * @param    aString    The string (couldn't be null!) to be inspected.
     * @return   true, if string should not be written.
     */
    protected boolean filter (String aString) {
        return (true);
    }

    /**
     * Will stop observing the current File.
     */
    public void stop() {
        stop = true;
    }

    /**
     * Allow call as main function.
     */
    public static void main (String [] args) throws Exception {
        switch (args.length) {
            case 1:  runTail(args[0]);
                     break;
            case 2:  runTail(args [0], args [1]);
                     break;
            default: System.out.println ("\nWrite data from the given " +
                                "input file to an output file position:\n\n" +
                                "java " + Tail.class.getName () +
                                " <input-file> [<output-file>]\n");
                     break;
        }
    }
    
	/**
	 * Runs a Tail on the file with the given file name and prints the result to
	 * {@link System#out standard out}.
	 */
    public static void runTail(String file) throws IOException {
    	final FileReader input = new FileReader(file);
    	try {
    		final PrintWriter output = new PrintWriter (System.out, /* autoflush */ true);
    		final Tail tail = createTail(input, output);
    		tail.observe();
		} finally {
			input.close();
		}
    }
    
    /**
     * Runs a Tail on the file with the given file name and prints the result to the given output file.
     */
    public static void runTail(String inputFile, String outputFile) throws IOException {
    	final FileReader input = new FileReader (inputFile);
    	try {
    		final PrintWriter output = new PrintWriter (new FileWriter(outputFile));
    		try {
    			final Tail tail = createTail(input, output);
    			tail.observe();
			} finally {
				output.close();
			}
		} finally {
			input.close();
		}
    }
    
	public static Tail createTail(Reader aReader, PrintWriter anOut) {
		return new Tail(aReader, anOut);
	}
}
