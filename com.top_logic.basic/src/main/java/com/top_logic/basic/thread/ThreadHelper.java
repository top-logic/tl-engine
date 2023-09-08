/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

import java.io.PrintStream;

/** 
 * Debugging Helper to dump Stacktraces of all Threads.
 *
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class ThreadHelper extends Thread {

    /** Maximum Number of Threas to show */
    public static final int MAX_THREAD = 200;

    /** Milliseconds to sleep before dumping all Threads. */
    long sleep;

    /**
     * Create a new Threadhelper.
     *
     * @param    aSleep    The time to sleep in the thread when called.
     */
    public ThreadHelper (long aSleep) {
        super ("ThreadHelper");

        this.sleep = aSleep;
    }

    /** 
     * Sleep for the defined time and display all threads on stderr.
     */
    @Override
	public void run () {
        System.err.println ("*** ThreadHelper starts");

        try {
            while (true) {
                sleep (sleep);
                showAllThreads (this);
            }
        }
        catch (InterruptedException ignored) {
            // Ignored.
        }

        System.err.println ("*** ThreadHelper end");
    }

    /**
     * Static helper function to be used directly.
     *
     * This one forwards the request to the method with the same name
     * using stderr for output.
     *
     * @param    tg    The thread group to be used.
     */
    public static void showThreads (ThreadGroup tg) {
        showThreads (tg, System.err);
    }

    /**
     * Static helper function to be used directly.
     *
     * @param    tg         The thread group to be used.
     * @param    aStream    The stream to be used for output.
     */
    public static void showThreads (ThreadGroup tg, PrintStream aStream) {
        Thread  list [] = new Thread [MAX_THREAD];
        boolean recurse = true;
        int     size    = tg.enumerate (list, recurse);

        if (size > MAX_THREAD) {
            aStream.println (size + " are more Threads than the maximum " +
                             "allowed of " + MAX_THREAD);
        }

        for (int i=0; i < size; i++) {
            aStream.println ("*** Thread '" + list [i].getName () + "' ***");
        }
    }
    
    /** 
     * Static helper function to be used directly.
     *
     * This one forwards the request to the method with the same name
     * using stderr for output.
     *
     * @param    start      The thread to start with.
     */
    public static void showSiblingThreads (Thread start) {
        showSiblingThreads (start, System.err);
    }

    /** 
     * Static helper function to be used directly.
     *
     * @param    start      The thread to start with.
     * @param    aStream    The stream to be used for output.
     */
    public static void showSiblingThreads (Thread start, PrintStream aStream) {
        showThreads (start.getThreadGroup (), aStream);
    }

    /** 
     * Static helper function to be used directly.
     *
     * This one forwards the request to the method with the same name
     * using stderr for output.
     *
     * @param    start      The thread to start with.
     */
    public static void showAllThreads (Thread start) {
        showAllThreads (start, System.err);
    }

    /** 
     * Static helper function to be used directly.
     *
     * @param    start      The thread to start with.
     * @param    aStream    The stream to be used for output.
     */
    public static void showAllThreads (Thread start, PrintStream aStream) {
        ThreadGroup parent = start.getThreadGroup ();
        ThreadGroup tg     = parent;

        while (parent != null)  {
            tg     = parent;
            parent = tg.getParent ();
        } 

        showThreads (tg, aStream);
    }

    /** 
     * Static helper function to be used directly.
     *
     * This one forwards the request to the method with the same name
     * using stderr for output.
     */
    public static void showAllThreads () {
        showAllThreads (System.err);
    }

    /** 
     * Static helper function to be used directly.
     *
     * This one forwards the request to the method with the same name
     * using stderr for output.
     *
     * @param    aStream    The stream to be used for output.
     */
    public static void showAllThreads (PrintStream aStream) {
        showAllThreads (Thread.currentThread (), aStream);
    }
}

