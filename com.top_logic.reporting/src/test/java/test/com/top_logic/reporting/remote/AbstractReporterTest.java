/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.remote;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.reporting.queue.QueueReporter;
import com.top_logic.reporting.queue.ReportQueueConstants;
import com.top_logic.reporting.remote.ReportDescriptor;
import com.top_logic.reporting.remote.ReportStatus;
import com.top_logic.reporting.remote.ReportTicket;
import com.top_logic.reporting.remote.Reporter;
import com.top_logic.reporting.remote.ReporterState;
import com.top_logic.reporting.remote.common.ReportFactory;


/**
 * Base class for test cases for {@link Reporter}.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractReporterTest  extends BasicTestCase implements ReportQueueConstants {

    /*************************************************************************************
     * Attention: Most of these tests assume, that no other person/test adds jobs while  *
     * this test is running                                                              *
     *************************************************************************************/
    protected static final int AVERAGE_REPORT_DURATION = QueueReporter.AVERAGE_REPORT_DURATION;

    protected Reporter reporter=null;

	protected abstract Reporter createReporter() throws Exception;

	protected abstract boolean isClientServerTest();

	protected abstract boolean cleanDirectories();

	public AbstractReporterTest(String aName) {
        super(aName);
    }

    @Override
	protected void setUp() throws Exception {
	    super.setUp();
	    if (cleanDirectories()) {cleanUpDirectories();}
	    if (reporter == null) {
	        reporter = createReporter();
		    assertTrue(reporter.isValid());
	        reporter.init();
	    }
	}

	@Override
	protected void tearDown() throws Exception {
		reporter.shutDown();
        if (cleanDirectories()) {cleanUpDirectories();}
        super.tearDown();
    }

	/**
	 * Tests the getStatus-method.
	 * A returned ReportStatus for a given ID is never null!
	 * If no element for the ID exists, ReportStatus is in state error
	 */
	public void testGetStatus()throws Exception {
		ReportTicket ticket = createNewReport();
		String id = ticket.getReportID();

		ReportStatus status = reporter.getStatus(id);
		assertNotNull(status);
		assertFalse(status.isError());

		status = reporter.getStatus("doesNotExist");
		assertNotNull(status);
		assertTrue(status.isError());
		assertEquals(-1, status.getExpectedDuration());
	}

	/**
	 * Tests the canelReport-method.
	 * The according file for the canceled report will be deleted.
	 * if no file for the ID is found - nothing happens
	 */
	public void testCancelReport()throws Exception {

	    // creating a new report
		ReportTicket ticket = createNewReport();
		String id = ticket.getReportID();

		// report should be fine
		ReportStatus status = reporter.getStatus(id);
		assertFalse(status.isError());

		// cancel report
		reporter.cancelReport(id);

		// report is canceled -> file is deleted -> status for not existing file is error
		status = reporter.getStatus(id);
		assertTrue(status.isError());

		// trying to cancel a report with a never existing id -> nothing happens
		reporter.cancelReport("doesNotExist");
	}

	protected ReportTicket createNewReport() throws Exception {
		ReportDescriptor desc = getReportDescriptor();
		return reporter.createReport(desc);
	}

	protected ReportDescriptor getReportDescriptor() throws Exception {
	    return getTestReportDescriptor(ReportDescriptor.PPT, ReportDescriptor.MODE_SETVALUES);
	}

	public static ReportDescriptor getTestReportDescriptor(String aType, String aMode) throws Exception {

        String templateFileName = null;
        Map        valueMap = new HashMap();
        valueMap.put("$param",    "Meier");
        valueMap.put("Sheet1!A1", "Schulze");
        valueMap.put("key3",      "value3");
        valueMap.put("jitter",    new JitterBug());
        if(ReportDescriptor.PPT.equals(aType)){
			templateFileName = "./" + ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/../com.top_logic.reporting/"
				+ ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/templates/ppt/simple.ppt";
        }
        if(ReportDescriptor.EXCEL.equals(aType)){
        	if (ReportDescriptor.MODE_GETVALUES.equals(aMode)) {
				templateFileName = "./" + ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/../com.top_logic.reporting/"
					+ ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/templates/xls/simple.xls";
        		valueMap = null; // new MapBuilder().put("Tabelle1", Collections.singleton("A1")).toMap();
        	}
        	else {
				templateFileName = "./" + ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/../com.top_logic.reporting/"
					+ ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/templates/xls/simple.xls";
        	}
        }
        if(ReportDescriptor.WORD.equals(aType)){
			templateFileName = "./" + ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/../com.top_logic.reporting/"
				+ ModuleLayoutConstants.SRC_TEST_DIR + "/test/data/templates/doc/simple.doc";
        }

		BinaryData templateFile = FileManager.getInstance().getData(templateFileName);

		byte[] template = StreamUtilities.readStreamContents(templateFile);
        String templateName = "templateName" + aType;

        ReportDescriptor rd = ReportFactory.getNewReportDescriptor(template, templateName, valueMap, aType, aMode);
        return rd;
    }

	protected void checkChange(ReporterState oldState,
	        ReporterState newState, int openChange, int doneChange, int errorChange) {

	    int openDelta = newState.getNumberOfOpenReports() - oldState.getNumberOfOpenReports();
	    int doneDelta = newState.getNumberOfDoneReports() - oldState.getNumberOfDoneReports();
	    int errorDelta =newState.getNumberOfErrorReports() - oldState.getNumberOfErrorReports();
	    assertEquals( "open reports did not change as excpected",openChange, openDelta);
	    assertEquals("done reports did not change as excpected",doneChange, doneDelta );
	    assertEquals("error reports did not change as excpected",errorChange, errorDelta );

	}

	private void cleanUpDirectories() {
	    cleanUpDir(DONE_PATH);
	    cleanUpDir(ERROR_PATH);
	    cleanUpDir(WORK_PATH);
	    cleanUpDir(OPEN_PATH);
	}

	private void cleanUpDir(String aPath) {
	    File cleanupDir = new File(aPath);
	    if (cleanupDir.exists()) {
	    	assertTrue("Failed to cleanup '" + cleanupDir.getAbsolutePath() + "'" , FileUtilities.deleteR(cleanupDir));
	    }
	}
}


