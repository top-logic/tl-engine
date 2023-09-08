/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.remote.client;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.com.top_logic.reporting.ReportingSetup;
import test.com.top_logic.reporting.remote.AbstractReporterTest;

import com.top_logic.reporting.queue.QueueReporter;
import com.top_logic.reporting.remote.ReportDescriptor;
import com.top_logic.reporting.remote.ReportResult;
import com.top_logic.reporting.remote.ReportStatus;
import com.top_logic.reporting.remote.ReportTicket;
import com.top_logic.reporting.remote.Reporter;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestQueueReporter extends AbstractReporterTest{
	
    public TestQueueReporter(String aName) {
        super(aName);
    }

    /**
     * Tests the initiation of a report-creation:
     * - a ticket is returned (not null)
     * - ticket contains status and ID
     * - status is not error
     */
    public void testCreateReport()throws Exception {
        ReportDescriptor desc = getReportDescriptor();
        
        ReportTicket   ticket = reporter.createReport(desc);
        
        assertNotNull(ticket);
        
        // check Ticket data
        ReportStatus status = ticket.getStatus();
        String id = ticket.getReportID();
        assertNotNull(status);
        assertNotNull(id);
        assertFalse(status.isError()); 
    }

    /**
     * Tests the getReport-method
     * 
     * Getting a finished report:
     * - ReportResult not null
     * - contained status is in state isDone
     * - InputStream not null
     * 
     * Trying to get a report for a not existing ID:
     * - ReportResult not null
     * - contained status is in state not done
     * - contained status is in state isError
     */
    public void testGetReport()throws Exception {

        ReportTicket ticket = createNewReport();
        String id = ticket.getReportID();
        
        ReportStatus status = null;
        ReportResult result = null;
        
        // wait for the creator-runner to create the report...
        // (we can't be 100 % sure this will always work fine, but this is the only way to test)
        Thread.sleep(3000);
        
		// report is now created: status in state isDone, we don't expect any errors
		status = reporter.getStatus(id);
		assertFalse(status.isError());
		assertTrue(status.isDone());
		assertEquals(0, status.getExpectedDuration());

		// report is done -> getting the report. we expect to get a stream of the finished report
		result = reporter.getReport(id);
		assertNotNull(result);
		assertNotNull(result.getReport());
		assertTrue(result.getReportStatus().isDone());
        
        // trying to get a report for a not existing ID
        result = reporter.getReport("doesNotExist");
        assertNotNull(result);
        assertFalse(result.getReportStatus().isDone());
        assertTrue(result.getReportStatus().isError());
    }
    
	@Override
	protected Reporter createReporter() {
	    return new QueueReporter();
	}

	@Override
	protected boolean cleanDirectories() {
		return true;
    }

    @Override
	protected boolean isClientServerTest() {
        return false;
    }

    public static Test suite() {
        TestSuite theSuite = new TestSuite(TestQueueReporter.class);
        return ReportingSetup.createReportingSetup(theSuite);
    }
    
    public static void main(String[] args) {
        TestRunner trunner = new TestRunner();
        // In case you want to the the errors right away and more ....
        // new DetailedResultPrinter(System.out));
        trunner.doRun(suite ());
    }


}
