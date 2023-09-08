/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.sched;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.sched.CleanSysTmpTask;
import com.top_logic.util.sched.CleanUpTask;
import com.top_logic.util.sched.CleanUpTask.Config;

/**
 * TestCase for {@link CleanSysTmpTask}
 * 
 * @author    <a href=mailto:klaus.halfmann@top-logic.com>Klaus Halfmann</a>
 */
public class TestCleanSysTmpTask extends BasicTestCase {

    /**
     * Constructor for TestCleanSysTmpTask.
     * 
     * @param aName the name of the TestCase
     */
    public TestCleanSysTmpTask(String aName) {
        super(aName);                        
    }

    /**
     * Creates three files in the base directory and a task to clean it.
     * Sleeps some minutes before running the task.
     * 
     * @throws InterruptedException     if an Exception occurs during sleeping.
     */
    public void testRun() throws Exception {
		CleanUpTask theTask = newCleanSysTempTask();

		assertNotNull(theTask.getCleanUpDir());
		assertTrue("Task tries to clean the wrong directory!",
			theTask.getCleanUpDir().equals(Settings.getInstance().getTempDir()));

        // Create the Files
		File theFirstOne = new File(theTask.getCleanUpDir(), "eins.txt");
		File theSecondOne = new File(theTask.getCleanUpDir(), "zwei.txt");
		File theThirdOne = new File(theTask.getCleanUpDir(), "drei.txt");
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

        theTask.run(); // Should be a noop ...

        // wait to ensure that the maximum file age is reached
		Thread.sleep(2000);
        
         // call the Task --> hour setting etc not needed   
        theTask.run();
         
        // ensure that the files are swept away
        assertFalse(theFirstOne.exists());                           
        assertFalse(theSecondOne.exists());    
        assertFalse(theThirdOne.exists()); 
    }

    /**
	 * Test what happens when the system temporary directory is cleaned up (See #1351).
	 */
	public void testRunDel() {
		// Create the task
		CleanUpTask task = newCleanSysTempTask();
    
		assertNotNull(task.getCleanUpDir());
    
		// Simulate a cron job cleaning up /tmp
		FileUtilities.deleteR(task.getCleanUpDir());
        
		assertNotNull(task.getCleanUpDir());
		assertTrue(task.getCleanUpDir().exists());
		assertTrue(task.getCleanUpDir().isDirectory());
    }

	private CleanUpTask newCleanSysTempTask() {
		Config config = TypedConfiguration.newConfigItem(CleanSysTmpTask.Config.class);
		config.setImplementationClass((Class<? extends CleanSysTmpTask>) CleanSysTmpTask.class);
		config.setName("TestCleanSysTmpTask");
		config.setTimeout(1000);

		CleanUpTask task = TypedConfigUtil.createInstance(config);
		TaskTestUtil.initTaskLog(task);
		return task;
	}

    /** 
     * Return the suite of tests to execute.
     */
    public static Test suite() {
		TestSuite test = new TestSuite(TestCleanSysTmpTask.class);
		Test startApplicationMonitor = ServiceTestSetup.createSetup(test, ApplicationMonitor.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(startApplicationMonitor);
    }
 
}
