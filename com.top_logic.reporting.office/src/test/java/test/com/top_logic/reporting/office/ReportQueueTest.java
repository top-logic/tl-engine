/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.office.ReportQueue;
import com.top_logic.reporting.office.ReportToken;


/**
 * Test the report queue with arbitrary tokens.
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class ReportQueueTest extends BasicTestCase {

	private ReportQueue _queue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new ReportQueue("TestQueue");
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.terminateImmediately();
		_queue.join(1000);
		super.tearDown();
		if (_queue.isAlive()) {
			throw new RuntimeException("Failure stopping Queue.");
		}
	}

    public void testConstructor () throws Exception {
        
        ReportToken theToken = ReportToken.createToken("noopID",null,null);
        
		assertFalse(_queue.jobQueued(theToken));
		assertFalse(_queue.jobExecuted(theToken));
    }
    
    public void testAddJob() throws Exception {
        ReportToken theToken = ReportToken.createToken("noopID",null,null);
        
        Logger.configureStdout("FATAL");
		// Suppress (correct) ERROR message from ReportGenerator
        try {
			_queue.addJob(theToken);
            
			assertTrue(_queue.jobQueued(theToken));
            Thread.sleep(500);
            
			assertTrue(_queue.jobExecuted(theToken));
        } finally {
            Logger.configureStdout();
        }
        
    }

    /** Return the suite of Tests to perform */
    public static Test suite () {
		return PersonManagerSetup.createPersonManagerSetup(new TestSuite(ReportQueueTest.class));
    }
 
    /** Main method for direct testing */
    public static void main(String[] args) {
        Logger.configureStdout();
        KBSetup.setCreateTables(false); // avoids reset of database
        junit.textui.TestRunner.run(suite());
    }

}
