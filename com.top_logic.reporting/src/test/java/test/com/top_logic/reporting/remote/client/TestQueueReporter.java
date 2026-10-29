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

	/**
	 * Maximum time in milliseconds to wait for the report creator to finish a report.
	 */
	private static final long REPORT_TIMEOUT_MS = 60000;

	/**
	 * Time in milliseconds between two status checks while waiting for a report.
	 */
	private static final long POLL_INTERVAL_MS = 100;

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

		ReportStatus status = waitForReport(id);

		// report is now created: status in state isDone, we don't expect any errors
		assertFalse("Report '" + id + "' failed: " + describe(status), status.isError());
		assertTrue("Report '" + id + "' not done within " + REPORT_TIMEOUT_MS + "ms: " + describe(status),
			status.isDone());
		assertEquals("Expected duration of finished report '" + id + "'.", 0, status.getExpectedDuration());

		// report is done -> getting the report. we expect to get a stream of the finished report
		ReportResult result = reporter.getReport(id);
		assertNotNull("No result for report '" + id + "'.", result);
		assertNotNull("No report data for report '" + id + "': " + describe(result.getReportStatus()),
			result.getReport());
		assertTrue("Status of result for report '" + id + "' is not done: " + describe(result.getReportStatus()),
			result.getReportStatus().isDone());

        // trying to get a report for a not existing ID
        result = reporter.getReport("doesNotExist");
		assertNotNull("No result for non-existing report.", result);
		assertFalse("Non-existing report is done.", result.getReportStatus().isDone());
		assertTrue("Non-existing report is not in error state.", result.getReportStatus().isError());
    }

	private static String describe(ReportStatus status) {
		return "expected duration " + status.getExpectedDuration() + ", exception " + status.getException();
	}

	/**
	 * Waits until the report with the given ID is done or failed, at most
	 * {@link #REPORT_TIMEOUT_MS}.
	 * 
	 * @return The last status of the report.
	 */
	private ReportStatus waitForReport(String id) throws InterruptedException {
		long timeout = System.currentTimeMillis() + REPORT_TIMEOUT_MS;
		while (true) {
			ReportStatus status = reporter.getStatus(id);
			if (status.isDone() || status.isError() || System.currentTimeMillis() > timeout) {
				return status;
			}
			Thread.sleep(POLL_INTERVAL_MS);
		}
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
