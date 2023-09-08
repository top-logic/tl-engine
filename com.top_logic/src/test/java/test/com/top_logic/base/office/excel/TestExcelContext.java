/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.top_logic.base.office.excel.ExcelContext;
import com.top_logic.base.office.excel.POIExcelContext;
import com.top_logic.base.office.excel.ProxyExcelContext;
import com.top_logic.basic.io.CSVReader;

/**
 * Test case for the excel context.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestExcelContext extends TestCase {

	public void testFailures() throws Exception {
		try {
			ExcelContext theContext = ExcelContext.getInstance("");
			fail("Returned " + theContext + " for String (which is not supported)!");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	/**
	 * Test excel links as FORMULA #19976.
	 */
	public void testExcelLink() throws Exception {
		final Workbook book;
		final InputStream stream = getClass().getResourceAsStream("TestExcelContext.xlsx");
		try {
			book = WorkbookFactory.create(stream);
		} finally {
			stream.close();
		}

		final POIExcelContext context = (POIExcelContext) POIExcelContext.getInstance(book.getSheet("ExcelLink"));
		assertEquals("Ticket #20629: Workaround for #19976 removed?", 42d, context.row(0).column(0).value());
	}

	public void testReadCSV() throws Exception {
		InputStream theStream = this.getInputStream(".csv");
		try {
			Reader theInput = new InputStreamReader(theStream, "Cp1252");
			try {
				CSVReader theReader = new CSVReader(theInput, ';');

				this.doTestReadData(theReader, true);
			} finally {
				theInput.close();
			}
		} finally {
			theStream.close();
		}
	}

	public void testReadXLSX() throws Exception {
		InputStream theStream = this.getInputStream(".xlsx");
		try {

			Workbook theWorkbook = WorkbookFactory.create(theStream);
			Sheet    theSheet    = theWorkbook.getSheetAt(0);

			this.doTestReadData(theSheet, false);
		}
		finally {
			theStream.close();
		}
	}

	public void testReadProxyCSV() throws Exception {
		InputStream theStream = this.getInputStream(".csv");
		try {
			Reader theInput = new InputStreamReader(theStream, "Cp1252");
			try {
				CSVReader theReader = new CSVReader(theInput, ';');

				ProxyExcelContext theContext = new ProxyExcelContext(theReader);
				try {
					this.doTestReadExcelContext(theContext, true);
				} finally {
					theContext.close();
				}
			} finally {
				theInput.close();
			}
		}
		finally {
			theStream.close();
		}
	}

	public void testReadProxyXLSX() throws Exception {
		InputStream theStream = this.getInputStream(".xlsx");
		try {
			Workbook theWorkbook = WorkbookFactory.create(theStream);
			Sheet theSheet = theWorkbook.getSheetAt(0);

			ProxyExcelContext theContext = new ProxyExcelContext(theSheet);
			try {
				this.doTestReadExcelContext(theContext, false);
			} finally {
				theContext.close();
			}
		} finally {
			theStream.close();
		}
	}

	/** 
	 * Test the current row of the given excel context.
	 * 
	 * @param aContext    The excel context to be checked.
	 * @param isCSV       Flag, if context covers a CSV file.
	 */
	protected void doTestExcelRow(ExcelContext aContext, boolean isCSV) {
		Assert.assertTrue(aContext.hasColumn("String"));
		Assert.assertFalse(aContext.hasColumn("Ist nicht da"));
		Assert.assertEquals("String", aContext.getColumnNr(0));
		Assert.assertEquals(null, aContext.getColumnNr(-1));

		Assert.assertNotNull(aContext.getString("String"));
		Assert.assertNotNull(aContext.getInt("Integer"));
		Assert.assertNotNull(aContext.getLong("Long"));
		Assert.assertNotNull(aContext.getDouble("Double"));
		Assert.assertNotNull(aContext.getBoolean("Boolean"));
		Assert.assertNotNull(aContext.getString("URL"));

		if (!isCSV) {
			Assert.assertTrue(aContext.isInvalid("Ungültig"));
			Assert.assertNotNull(aContext.getURL("URL"));
		}
		else { 
			Assert.assertFalse(aContext.isInvalid("Ungültig"));
			Assert.assertNull(aContext.getURL("URL"));
		}
	}

	protected void doTestExcelNaming(ExcelContext aContext) {
		aContext.prepareColNamesAsHeaders();

		Assert.assertEquals("A", aContext.getColumnNr(0));
		Assert.assertEquals("B", aContext.getColumnNr(1));
		Assert.assertEquals("C", aContext.getColumnNr(2));
		Assert.assertEquals("D", aContext.getColumnNr(3));
		Assert.assertEquals("E", aContext.getColumnNr(4));
		Assert.assertEquals("F", aContext.getColumnNr(5));
	}
	/** 
	 * Return the test data as stream.
	 * 
	 * @param    aSuffix    The suffix (extension) of the test data.
	 * @return   The requested test data as stream.
	 */
	protected InputStream getInputStream(String aSuffix) {
		return this.getClass().getResourceAsStream("TestExcelContext" + aSuffix);
	}

	/** 
	 * Return the expected number of rows in the excel file.
	 * 
	 * @return    The expected number of rows.
	 */
	protected int getExpectedRows() {
		return 3;
	}

	private void doTestReadData(Object aSource, boolean isCSV) throws IOException {
		Assert.assertNotNull(aSource);

		ExcelContext theContext = null;
		try {
			theContext = ExcelContext.getInstance(aSource);
			this.doTestReadExcelContext(theContext, isCSV);
		}
		finally {
			if (theContext != null) {
				theContext.close();
			}
		}
	}

	private void doTestReadExcelContext(ExcelContext aContext, boolean isCSV) {
		Assert.assertNotNull(aContext);

		this.doTestExcelNaming(aContext);
		aContext.prepareHeaderRows();
		aContext.down();

		int lastCellNum = aContext.getLastCellNum();
		Assert.assertTrue("getLastCellNum() smaller than 5 (is '" + lastCellNum + "')!", lastCellNum >= 5);

		while (aContext.hasMoreRows()) {
			this.doTestExcelRow(aContext, isCSV);

			aContext.down();
		}

		Assert.assertEquals("Test data didn't contain the expected rows!", this.getExpectedRows(), aContext.row());
		
		if (isCSV) {
			this.doTestCSVSpecial(aContext);
		}
	}


	private void doTestCSVSpecial(ExcelContext aContext) {
		try {
			aContext.up();
			fail("Up is not possible in CSV!");
		}
		catch (UnsupportedOperationException ex) {
			// expected
		}

		try {
			aContext.row(1);
			fail("Step back to previous row is not possible in CSV!");
		}
		catch (UnsupportedOperationException ex) {
			// expected
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestExcelContext}.
	 */
	public static Test suite() {
		return new TestSuite(TestExcelContext.class);
	}

    /**
     * Main function for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run(suite());
    }
}

