/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.sched.CleanUpTask;
import com.top_logic.util.sched.CleanUpTask.Config;

import test.DetailedResultPrinter;

/**
 * TestCase for CleanUpTask.
 *
 * @author    Alice Scheerer
 */
public class TestCleanUpTask extends BasicTestCase {
    
    /**
     * The base directory
     */
    public final static File PATH = BasicTestCase.createNamedTestFile("cleanup");
    
    /**
     * The files to create and delete
     */
    public static File theFirstOne;
    public static File theSecondOne;
    public static File theThirdOne;


    /**
     * Constructor for TestCleanUpTask.
     * 
     * @param aName the name of the TestCase
     */
    public TestCleanUpTask(String aName) {
        super(aName);                        
    }

    /**
     * Overwritten to create the base directory.
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        FileUtilities.deleteR(PATH);
        
        if (!PATH.exists()) {
            assertTrue("Could not create base directory",PATH.mkdir());
        }

    }
    /**
     * Overwritten to reset static variables.
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        theFirstOne  = null;
        theSecondOne = null;
        theThirdOne  = null;
        
        super.tearDown();
    }

    /**
     * Creates three files in the base directory and a task to clean it.
     * Sleeps some minutes before running the task.
     * 
     * @throws InterruptedException     if an Exception occurs during sleeping.
     */
    public void testRun() throws Exception {
        ThreadContext.pushSuperUser();
        
        try {
            // Create the Files
            theFirstOne  = new File(PATH, "eins.txt") ;
            theSecondOne = new File(PATH, "zwei.txt") ;
            theThirdOne  = new File(PATH, "drei.txt") ;
            if(!theFirstOne.exists()) {
                assertTrue("Creating first File",theFirstOne.createNewFile());
            }
            if(!theSecondOne.exists()) {
                assertTrue("Creating second File",theSecondOne.createNewFile());
            }    
            if(!theThirdOne.exists()) {
                assertTrue("Creating third File",theThirdOne.createNewFile());
            }
            // ensure that they exist
            assertTrue(theFirstOne .exists());                           
            assertTrue(theSecondOne.exists());    
            assertTrue(theThirdOne .exists());         
    
            // Create the Task
			CleanUpTask theTask = newCleanupTask(1000);
    
            // wait to ensure that the maximum file age is reached
			Thread.sleep(2000);
            
            // call the Task --> hour setting etc not needed   
            theTask.run();
             
            // ensure that the files are swept away
            assertFalse(theFirstOne.exists());                           
            assertFalse(theSecondOne.exists());    
            assertFalse(theThirdOne.exists()); 
        }  finally {
            ThreadContext.popSuperUser();
        }

    }

	private CleanUpTask newCleanupTask(long timeout) {
		Config config = TypedConfiguration.newConfigItem(CleanUpTask.Config.class);
		config.setName("TestCleanUpTask");
		config.setCleanupPath("file://" + PATH.getAbsolutePath());
		config.setTimeout(timeout);

		CleanUpTask task = TypedConfigUtil.createInstance(config);
		TaskTestUtil.initTaskLog(task);
		return task;
	}

    /** Return the suite of tests to execute.
     */
    public static Test suite() {
       
        TestSuite test = new TestSuite(TestCleanUpTask.class);
		Test startApplicationMonitor = ServiceTestSetup.createSetup(test, ApplicationMonitor.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(startApplicationMonitor);
    }
 
    /** Main function for direct execution */
    public static void main (String[] args) {
        //    Logger.configureStdout();
        TestRunner trunner = new TestRunner(
               new DetailedResultPrinter(System.out)
        );
        // In case you want to the the errors right away and more ....
        trunner.doRun(suite ());
    }    
   


}
