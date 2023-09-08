/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.File;
import java.util.Calendar;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.BasicFileLog;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Test of {@link BasicFileLog}.
 * 
 * @author <a href=mailto:fma@top-logic.com>fma</a>
 */
public class TestBasicFileLog extends BasicTestCase {

	private File basicPath;

	private static String THE_TEXT = "Müller und daß Änliche mit Üblen Umläutern";

	public TestBasicFileLog(String aName) {
		super(aName);
	}
	
	private BasicFileLog logger;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.logger = BasicFileLog.getInstance();
		
		this.basicPath = createNamedTestFile("basicFileLog");
		if (! this.basicPath.exists()){
			assertTrue(this.basicPath.mkdir());
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		FileUtilities.deleteR(this.basicPath);
		super.tearDown();
	}
	
	public void testLogIntoNewFile() throws Exception {
		String LOG_TYPE = "test"; // type of test, must be set in config file
		File theDir = new File(basicPath, LOG_TYPE);
		
		assertFalse("File '" + theDir + "' already exists", theDir.exists());
		
		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);

		// now dir exists
		assertTrue("File '" + theDir + "' does not exist", theDir.exists());
		assertTrue(theDir.listFiles().length > 0);

		// delete all files in theDir
		FileUtilities.deleteR(theDir);

		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);
		File[] listFiles = theDir.listFiles();
		assertEquals(1, listFiles.length);
		File theFile = listFiles[0];
		assertTrue(theFile.isFile());
		String theContent = FileUtilities.readFileToString(theFile);
		assertEquals(THE_TEXT, theContent);

		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);
		assertEquals(2, theDir.listFiles().length);
	}

	public void testAppendIntoLogFile() throws Exception {
		String LOG_TYPE = "test1"; // type of test, must be set in config file
		File theDir = new File(basicPath, LOG_TYPE);
		
		assertFalse("File '" + theDir + "' already exists", theDir.exists());
		
		logger.appendIntoLogFile(LOG_TYPE, THE_TEXT);

		// now dir exists
		assertTrue("File '" + theDir + "' does not exist", theDir.exists());
		assertTrue(theDir.listFiles().length > 0);

		// delete all files in theDir
		FileUtilities.deleteR(theDir);

		logger.appendIntoLogFile(LOG_TYPE, THE_TEXT);
		logger.appendIntoLogFile(LOG_TYPE, THE_TEXT);
		File[] listFiles = theDir.listFiles();
		assertEquals(1, listFiles.length);
		File theFile = listFiles[0];
		assertTrue(theFile.isFile());
		String theContent = FileUtilities.readFileToString(theFile);
		assertEquals(THE_TEXT + THE_TEXT, theContent);

		logger.appendIntoLogFile(LOG_TYPE, THE_TEXT);
		assertTrue(theDir.listFiles().length == 1);
	}

	public void testYearMonthFolder() throws Exception {
		String LOG_TYPE = "test2"; // type of test, must be set in config file
		Calendar cal = CalendarUtil.createCalendar();
		String theMonth = "" + (1 + cal.get(Calendar.MONTH));
		if (theMonth.length() == 1) {
			theMonth = "0" + theMonth;
		}

		File theDir = new File(basicPath, LOG_TYPE + File.separator + cal.get(Calendar.YEAR) + File.separator + theMonth);
		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);

		// now dir exists
		assertTrue("File '" + theDir + "' does not exist", theDir.exists());
		assertTrue(theDir.listFiles().length > 0);

		// delete all files in theDir
		FileUtilities.deleteR(theDir);

		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);
		assertTrue(theDir.listFiles().length == 1);
		File theFile = theDir.listFiles()[0];
		assertTrue(theFile.isFile());
		String theContent = FileUtilities.readFileToString(theFile);
		assertEquals(THE_TEXT, theContent);

		logger.logIntoNewFile(LOG_TYPE, THE_TEXT);
		assertTrue(theDir.listFiles().length == 2);
	}

	/** Test massive usage to get a performance Idea. */
	public void testMassiveNewFile() throws Exception {
		final int TEST_COUNT = 120; // Result in about 1 second

		String LOG_TYPE = "test"; // type of test, must be set in config file
		File theDir = new File(basicPath, LOG_TYPE);

		// delete all files in theDir
		FileUtilities.deleteR(theDir);

		startTime();
		for (int i = 0; i < TEST_COUNT; i++) {
			logger.logIntoNewFile(LOG_TYPE, THE_TEXT + i);
			assertTrue("File '" + theDir + "' does not exist", theDir.exists());
			assertEquals(i + 1, theDir.listFiles().length);
		}
		logTime("Time for " + TEST_COUNT + "x logIntoNewFile");

		// now dir exists
		assertTrue(theDir.exists());
	}

	/**
	 * Return the suite of tests to execute.
	 */
	public static Test suite() {
		return BasicTestSetup
			.createBasicTestSetup(ServiceTestSetup.createSetup(TestBasicFileLog.class, BasicFileLog.Module.INSTANCE));
	}

	/**
	 * main function for direct testing.
	 */
	public static void main(String[] args) {
		// SHOW_TIME = true;
		junit.textui.TestRunner.run(suite());
	}

}
