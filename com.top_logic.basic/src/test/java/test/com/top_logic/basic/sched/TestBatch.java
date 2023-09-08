/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sched;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sched.ScheduledThread;

/**
 * Tests for {@link com.top_logic.basic.sched.Batch} class.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestBatch extends TestCase {
    
    /**
     * Constructor for TestBatch.
     * 
     * @param name method name of the test to execute.
     */
    public TestBatch(String  name) {
        super(name);
    }

    /** Main TestCase for now (class is actually very simple). */
    public void testSignalStop() throws InterruptedException {
        
        long now = System.currentTimeMillis();
        
        ExampleBatch theBatch = new ExampleBatch("Example1");
        ScheduledThread sThread = new ScheduledThread(theBatch);
        sThread.start(System.currentTimeMillis());
        assertTrue(sThread.signalStop());
        assertTrue(theBatch.getShouldStop());
        Thread.sleep(50); // allow propagation of interrupt
        assertTrue(theBatch.interrupted);
        assertTrue(theBatch.getStarted() >= now);
        assertTrue(theBatch.getStarted() <= System.currentTimeMillis());
        sThread.join();
        
        now = System.currentTimeMillis();
        theBatch = new ExampleBatch("Example2");
        theBatch.allowStop = false;
        sThread = new ScheduledThread(theBatch);
        sThread.start(System.currentTimeMillis());
        assertTrue(!sThread.signalStop());
        assertTrue(theBatch.getShouldStop());
        Thread.sleep(50); // allow propagation of interrupt
        assertTrue("Batch not interrupted ?", theBatch.interrupted);
        assertTrue(theBatch.getStarted() >= now);
        assertTrue(theBatch.getStarted() <= System.currentTimeMillis());
        sThread.join();
   }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {

        return new TestSuite (TestBatch.class);
        // return new TestBatch("testSignalStop");
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
    
}
