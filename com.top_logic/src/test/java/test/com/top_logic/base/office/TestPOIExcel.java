/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.excel.ExcelValue.CellPosition;
import com.top_logic.base.office.excel.ExcelValue.MergeRegion;
import com.top_logic.base.office.excel.POIColumnFilterDescription;
import com.top_logic.base.office.excel.POIColumnWidthDescription;
import com.top_logic.base.office.excel.POIExcelAccess;
import com.top_logic.base.office.excel.POIExcelUtil;
import com.top_logic.base.office.excel.POIRowGroupDescription;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.NumberUtil;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.util.MimeTypes;

/**
 * Tests for the POI excel classes.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@SuppressWarnings("javadoc")
public class TestPOIExcel extends AbstractPOIExcelTest {

	/** Date format used by test case. */
	private static final String DATE_TIME_FORMAT = "yyyyMMdd-HHmmss";

	/** File to read data from. */
	private static final String TEST_FILE = "TestFile.xlsx";

	/** Template to be used for creating a new excel file. */
	private static final String TEMPLATE_FILE = "TestTemplate.xlsx";

	/** Path to excel files needed by this test. */
	private static final String FILE_PATH =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/";

	/** Template sheet for formula handling. */
	private static final String FORMULA_SHEET = "Ausfüllen";

//	/** Template sheet for filling in data. */
//	private static final String FILL_SHEET = "Befüllen";

	/** Header text for a merge region. */
	private static final String TEST_VALUES_HEADER = "Testdaten programmatisch gesetzt";

	/** Start row of the */
	private static final int START_TEST_VALUES = 20;

	/** Some test values. */
    private static final Map<String, Object> TEST_VALUES;

    static {
		try {
			Date theDate = CalendarUtil.newSimpleDateFormat(DATE_TIME_FORMAT, Locale.GERMAN).parse("20160101-133030");
			Calendar theCal = CalendarUtil.createCalendar(theDate, Locale.GERMAN);

			TEST_VALUES = new MapBuilder<String, Object>(true)
					.put("Boolean",   Boolean.TRUE)
					.put("Calendar", theCal)
					.put("Character", 'A')
					.put("Date", theDate)
//        		.put("Formular", "=12*23")
					.put("Image", new File(FILE_PATH + "TestImage.jpg"))
				.put("BinaryData", BinaryDataFactory.createBinaryData(new File(FILE_PATH + "TestImage2.png")))
					.put("Integer", Integer.valueOf(1234))
					.put("Long", Long.valueOf(12345))
					.put("NullValue", null)
					.put("Float", Float.valueOf(12.34f))
					.put("RichText", new XSSFRichTextString("Orwell war ein Optimist"))
					.toMap();
		} 
		catch (ParseException ex) {
			throw new ConfigurationError("Failed to initialize test class", ex);
		}
   	}

	/** Date formatter for this test case. */
	protected final SimpleDateFormat dateFormat = CalendarUtil.newSimpleDateFormat(DATE_TIME_FORMAT, Locale.GERMAN);

	public void testRowGroup() throws Exception {
		POIExcelAccess theAccess = this.createExcelAccess();
		BinaryData theTemplate = this.getExcelFile();
		File result = this.getTargetFile("ResultRowGroup");

		try (InputStream theStream = theTemplate.getStream()) {
			List<POIColumnWidthDescription> colDescriptions = Collections.emptyList();
			POIRowGroupDescription poiRowGroupDescription = new POIRowGroupDescription("Test RowGroup");
			poiRowGroupDescription.addGroup(0, 3, true);
			poiRowGroupDescription.addGroup(8, 11, true);
			poiRowGroupDescription.addGroup(13, 13, false);
			poiRowGroupDescription.addGroup(16, 20, true);
			try {
				poiRowGroupDescription.addGroup(-1, 15, true);
				fail("No row -1");
			} catch (IllegalArgumentException ex) {
				// expected.
			}
			try {
				poiRowGroupDescription.addGroup(16, 15, true);
				fail("End row must not be grater than start row");
			} catch (IllegalArgumentException ex) {
				// expected.
			}
			List<POIRowGroupDescription> someGroups = list(poiRowGroupDescription);
			POIColumnFilterDescription colFilterDescription = null;
			theAccess.setValuesDirect(theStream, result, new ExcelValue[0], true, colDescriptions, someGroups,
				colFilterDescription);
		}
		// groups can not be checked using POI. Must be checked manually.
	}

	public void testColumnWidth() throws Exception {
		POIExcelAccess theAccess = this.createExcelAccess();
		BinaryData theTemplate = this.getExcelFile();
		File result = this.getTargetFile("ResultColumnWith");

		String sheetName = "Test ColumnWidth 1";
		String sheet2Name = "Test ColumnWidth 2";
		List<POIRowGroupDescription> someGroups = Collections.emptyList();
		List<POIColumnWidthDescription> colDescriptions = list(
			POIColumnWidthDescription.newCharCountDescription(sheetName, 0, 20),
			POIColumnWidthDescription.newDescription(sheetName, 3, 500),
			POIColumnWidthDescription.newDescription(sheet2Name, 0, 700));
		POIColumnFilterDescription colFilterDescription = null;
		try (InputStream theStream = theTemplate.getStream()) {
			// column width are only applied on "non autofit" excel.
			boolean autofit = false;
			theAccess.setValuesDirect(theStream, result, new ExcelValue[0], autofit, colDescriptions, someGroups,
				colFilterDescription);
		}

		FileInputStream resultStream = new FileInputStream(result);
		try {
			Workbook workbook = POIUtil.getWorkbook(resultStream, FilenameUtils.getExtension(result.getName()));
			Sheet sheet1 = workbook.getSheet(sheetName);
			assertNotNull(sheet1);
			assertEquals(20 * POIColumnWidthDescription.WIDTH_FACTOR, sheet1.getColumnWidth(0));
			assertEquals(500, sheet1.getColumnWidth(3));
			Sheet sheet2 = workbook.getSheet(sheet2Name);
			assertNotNull(sheet2);
			assertEquals(700, sheet2.getColumnWidth(0));
		} finally {
			resultStream.close();
		}
	}

	public void testColumnAutoFilter() throws Exception {
		POIExcelAccess theAccess = this.createExcelAccess();
		BinaryData theTemplate = this.getExcelFile();
		File result = this.getTargetFile("ResultColumnFilter");

		String sheetName = "Test AutoFilter";
		List<POIColumnWidthDescription> colDescriptions = Collections.emptyList();
		List<POIRowGroupDescription> someGroups = Collections.emptyList();
		POIColumnFilterDescription colFilterDescription = new POIColumnFilterDescription(sheetName, 1, 4, 6, 8);
		try (InputStream theStream = theTemplate.getStream()) {
			theAccess.setValuesDirect(theStream, result, new ExcelValue[0], true, colDescriptions, someGroups,
				colFilterDescription);
		}

		// filter can not be checked using POI. Must be checked manually.
	}

    public void testCreateFile() throws Exception {
    	POIExcelAccess      theAccess   = this.createExcelAccess();
		BinaryData theTemplate = this.getExcelTemplate();
    	File                theResult1  = this.getTargetFile("ResultSimple");
    	File                theResult2  = this.getTargetFile("ResultDirect");
    	Map<String, Object> theData     = new HashMap<>();

    	this.putValue(FORMULA_SHEET, "B13", 10l, theData);
    	this.putValue(FORMULA_SHEET, "C13", 10l, theData);

    	this.putValue(FORMULA_SHEET, "B14", 10l, theData);
    	this.putValue(FORMULA_SHEET, "C14", 10l, theData);

    	this.putValue(FORMULA_SHEET, "B15", 10l, theData);
    	this.putValue(FORMULA_SHEET, "C15", 10l, theData);

    	this.putValue(FORMULA_SHEET, "B16", 10l, theData);
    	this.putValue(FORMULA_SHEET, "C16", 10l, theData);

    	int thePos = START_TEST_VALUES;
    	for (Entry<String, Object> theEntry : TEST_VALUES.entrySet()) {
    		this.putValue(FORMULA_SHEET, "A" + thePos, theEntry.getKey(),   theData);
    		this.putValue(FORMULA_SHEET, "B" + thePos, theEntry.getValue(), theData);

    		thePos++;
    	}

		this.putValue(FORMULA_SHEET, "A" + thePos, "DAP", theData);
		this.putValue(FORMULA_SHEET, "B" + thePos, new DataAccessProxy("file://" + FILE_PATH + "TestImage.jpg"), theData);
		thePos++;

		theAccess.setValues(theTemplate, theResult1, theData);

    	ExcelValue theComment = new ExcelValue(FORMULA_SHEET + "!B" + thePos, "Commented cell");
    	theComment.setComment("This is a comment at the cell " + theComment.getCellPos(), Color.GREEN);
		theComment.setBackgroundColor(new Color(221, 217, 196));

    	this.putValue(FORMULA_SHEET, "A" + thePos, "Comment", theData);
    	this.putValue(FORMULA_SHEET, "B" + thePos, theComment, theData);

    	ExcelValue theStyleValue = new ExcelValue(FORMULA_SHEET + "!D" + thePos, "Reference style");
    	theStyleValue.setCellStyleFrom(new CellPosition("A13"));
    	theStyleValue.setAutoWidth(false); 

    	this.putValue(FORMULA_SHEET, "D" + thePos, theStyleValue, theData);

    	ExcelValue theMerge = new ExcelValue(FORMULA_SHEET + "!A12", TEST_VALUES_HEADER);

    	theMerge.setMergeRegion(new MergeRegion(11, 0, 11, 4));
    	theMerge.setComment("Header area with comment!");
		theMerge.setCellAlignment(HorizontalAlignment.CENTER);
		theMerge.setBackgroundColor(new Color(221, 217, 196));

    	this.putValue(FORMULA_SHEET, "A12", theMerge, theData);

		try (InputStream theStream = theTemplate.getStream()) {
			theAccess.setValuesDirect(theStream, theResult2, this.toExcelValues(theData));
		}

		this.doTestCreatedFile(BinaryDataFactory.createBinaryData(theResult1), false);
		this.doTestCreatedFile(BinaryDataFactory.createBinaryData(theResult2), true);
    }

    public void testReadFileSimple() throws Exception {
    	POIExcelAccess      theAccess = this.createExcelAccess();
    	Map<String, Object> theResult = theAccess.getValues(this.getExcelFile());

    	assertNotNull("Reading values from '" + TEST_FILE + "' failed!", theResult);
    	assertNotEquals("File '" + TEST_FILE + "' contains no values!", 0, theResult.size());

    	this.doTest(theResult, "Tabelle1!A10", "Zeile 9");
    	this.doTest(theResult, "Tabelle1!B10", this.getTestDate("20040201-000000"));
    	this.doTest(theResult, "Tabelle1!C10", "Wert C.10");
    	this.doTest(theResult, "Tabelle1!D10", "Wert D.10");
    	this.doTest(theResult, "Tabelle1!E10", this.getTestDate("20040202-120000"));

    	for (int thePos = 2; thePos < 10; thePos++) {
    		String theRow    = Integer.toString(thePos);
    		int    theValue  = thePos - 1;

    		this.doTest(theResult, "Tabelle1!B" + theRow, Double.valueOf(theValue));
    		this.doTest(theResult, "Tabelle1!C" + theRow, Double.valueOf(theValue * 10));
			this.doTest(theResult, "Tabelle1!D" + theRow, Double.valueOf(theValue * 100));
			this.doTest(theResult, "Tabelle1!E" + theRow, Double.valueOf(theValue * 1000.1d));
    	}
    }
	
	public void testReadFileArray() throws Exception {
		POIExcelAccess theAccess = this.createExcelAccess();
		Object[][]     theResult = theAccess.getValues(this.getExcelFile(), "Tabelle1");

		assertNotNull("Reading values from '" + TEST_FILE + "' failed!", theResult);
		assertNotEquals("File '" + TEST_FILE + "' contains no values!", 0, theResult.length);
		
		for (int thePos = 1; thePos < 9; thePos++) {
			this.doTest(theResult[0], thePos, "Zeile " + thePos);
			this.doTest(theResult[1], thePos, Double.valueOf(thePos));
			this.doTest(theResult[2], thePos, Double.valueOf(thePos * 10));
			this.doTest(theResult[3], thePos, Double.valueOf(thePos * 100));
			this.doTest(theResult[4], thePos, Double.valueOf(thePos * 1000.1d));
		}
	}
	
	public void testReadFileSelective() {
		POIExcelAccess           theAccess = this.createExcelAccess();
		Map<String, Set<String>> theCoords = new HashMap<>();

		MapUtil.addObjectToSet(theCoords, "Tabelle1", "A2");
		MapUtil.addObjectToSet(theCoords, "Tabelle1", "B2");
		MapUtil.addObjectToSet(theCoords, "Tabelle1", "C2");

		Map<String, Object>      theResult = theAccess.getValues(this.getExcelFile(), theCoords);

		assertNotNull("Reading values from '" + TEST_FILE + "' failed!", theResult);
		assertNotEquals("File '" + TEST_FILE + "' contains no values!", 0, theResult.size());

		this.doTest(theResult, "Tabelle1", "A2", "Zeile 1");
		this.doTest(theResult, "Tabelle1", "B2", Double.valueOf(1));
		this.doTest(theResult, "Tabelle1", "C2", Double.valueOf(10));
	}

    public void testReadFileComplex() throws Exception {
		BinaryData theFile = this.getExcelFile();
		try (InputStream theStream = theFile.getStream()) {
			Workbook theResult = POIUtil.getWorkbook(theStream, TEST_FILE.substring(TEST_FILE.lastIndexOf('.')));

			assertNotNull("Reading values from '" + TEST_FILE + "' failed!", theResult);

			this.doTest(theResult, "Tabelle1!A10", "Zeile 9", HorizontalAlignment.GENERAL, null);
			this.doTest(theResult, "Tabelle1!B10", this.getTestDate("20040201-000000"));
			this.doTest(theResult, "Tabelle1!C10", "Wert C.10", HorizontalAlignment.RIGHT, null);
			this.doTest(theResult, "Tabelle1!D10", "Wert D.10", HorizontalAlignment.CENTER, null);
			this.doTest(theResult, "Tabelle1!E10", this.getTestDate("20040202-120000"));

			for (int thePos = 2; thePos < 10; thePos++) {
				String theRow    = Integer.toString(thePos);
				int    theValue  = thePos - 1;

				this.doTest(theResult, "Tabelle1!B" + theRow, Integer.valueOf(theValue));
				this.doTest(theResult, "Tabelle1!C" + theRow, Integer.valueOf(theValue * 10));
				this.doTest(theResult, "Tabelle1!D" + theRow, Integer.valueOf(theValue * 100));
				this.doTest(theResult, "Tabelle1!E" + theRow, Double.valueOf(theValue * 1000.1d));
			}

    	}
    }

	/** 
	 * Return the implementation to be used.
	 * 
	 * @return    The new created instance.
	 */
	protected POIExcelAccess createExcelAccess() {
		return (POIExcelAccess) ExcelAccess.newInstance();
	}

	/** 
	 * Convert map of raw values into an array of {@link ExcelValue}s.
	 * 
	 * @param    aMap   The map to be converted.
	 * @return   The converted {@link ExcelValue}s.
	 */
	private ExcelValue[] toExcelValues(Map<String, Object> aMap) {
		List<ExcelValue> theResult = new ArrayList<>(aMap.size());
		
		for (Entry<String, Object> theEntry : aMap.entrySet()) {
			Object theValue = theEntry.getValue();

			if (theValue instanceof ExcelValue) {
				theResult.add((ExcelValue) theValue);
			}
			else {
				theResult.add(new ExcelValue(theEntry.getKey(), theValue));
			}
		}

		return theResult.toArray(new ExcelValue[theResult.size()]);
	}

	/** 
	 * Put a test value into the map of raw values.
	 * 
	 * @param aSheet    The sheet the cell lives in.
	 * @param aCoord    The local coordinate of the cell.
	 * @param aValue    The value to be set.
	 * @param someData  The map of values to be stored.
	 */
	protected void putValue(String aSheet, String aCoord, Object aValue, Map<String, Object> someData) {
		someData.put(aSheet + '!' + aCoord, aValue);
	}

    /** 
     * Provide the date from the given string representation (pattern: {@value #DATE_TIME_FORMAT}).
     *
     * @param     aDate    The requested date as string.
     * @return    The requested date.
     * @throws    ParseException    When paring date fails.
     */
    protected Date getTestDate(String aDate) throws ParseException {
    	return this.dateFormat.parse(aDate);
    }

    /** 
     * Check, if the value in the requested cell matched the expected one.
     * 
     * @param aMap      The map to get the values from.
     * @param aCell     The requested cell name (in excel format).
     * @param aValue    The expected value.
     */
	protected void doTest(Map<String, Object> aMap, String aCell, Object aValue) {
        assertEquals("Value in cell " + aCell + " doesn't match!", aValue, aMap.get(aCell));
    }

	/** 
	 * Test if the given value is contained in the given map.
	 * 
	 * @param aMap      The map of values read.
	 * @param aSheet    The sheet the cell lives in.
	 * @param aCoord    The local coordinate of the cell.
	 * @param aValue    The value to be tested.
	 */
	protected void doTest(Map<String, Object> aMap, String aSheet, String aCoord, Object aValue) {
		if (aValue instanceof Number) {
			// Excel will always return double values.
			this.doTest(aMap, aSheet + '!' + aCoord, NumberUtil.getDouble((Number) aValue));
		}
		else if (aValue instanceof RichTextString) {
			// RichTextString will be converted to String internally
			this.doTest(aMap, aSheet + '!' + aCoord, ((RichTextString) aValue).getString());
		}
		else if (aValue instanceof Character) {
			// Excel will always return string values.
			this.doTest(aMap, aSheet + '!' + aCoord, Character.toString((Character) aValue));
		}
		else if (aValue instanceof Calendar) {
			// Calendar will be converted to Date internally
			Calendar copy = CalendarUtil.convertToTimeZone((Calendar) aValue, TimeZones.systemTimeZone());
			this.doTest(aMap, aSheet + '!' + aCoord, copy.getTime());
		}
		else if (aValue instanceof File) {
			// Ignore files in here.
		}
		else if (aValue instanceof BinaryData) {
			// Ignore binary data in here.
		}
		else if (aValue == null) {
			// Null will be converted to String internally
			this.doTest(aMap, aSheet + '!' + aCoord, "");
		}
		else { 
			this.doTest(aMap, aSheet + '!' + aCoord, aValue);
		}
	}

	/** 
	 * Check, if the value in the requested cell matched the expected one.
	 * 
	 * @param aBook     The workbook to get the values from.
	 * @param aCell     The requested cell name (in excel format).
	 * @param aValue    The expected value.
	 */
	protected void doTest(Workbook aBook, String aCell, Object aValue) {
		this.doTest(aBook, aCell, aValue, null, null);
	}

    /** 
	 * Check, if the value in the requested cell matched the expected one.
	 * 
	 * @param someValues   The column of read values.
	 * @param aPos         The position to be checked.
	 * @param aValue       The expected value.
	 */
	protected void doTest(Object[] someValues, int aPos, Object aValue) {
		assertEquals("Value at position " + aPos + " doesn't match!", aValue, someValues[aPos]);
	}

	/** 
	 * Check, if the value in the requested cell matched the expected one (including format of the cell).
	 * 
	 * @param aBook                The workbook to get the values from.
	 * @param aCell                The requested cell name (in excel format).
	 * @param aValue               The expected value.
	 * @param anHorizontalAlign    The expected horizontal alignment (null will ignore that test). 
	 * @param aVerticalAlign       The expected vertical alignment (null will ignore that test). 
	 */
	protected void doTest(Workbook aBook, String aCell, Object aValue, HorizontalAlignment anHorizontalAlign,
			VerticalAlignment aVerticalAlign) {
		Cell      theCell  = POIExcelUtil.getCell(aBook, new CellReference(aCell));
		CellStyle theStyle = theCell.getCellStyle();
		Object    theValue = null;

		try {
			theValue = this.getValue(theCell, aValue.getClass());
		}
		catch (Exception ex) {
			fail("Value in cell " + aCell + " cannot be read!", ex);
		}

		assertEquals("Value in cell " + aCell + " doesn't match!", aValue, theValue);

		if (anHorizontalAlign != null) { 
			assertEquals("Horizontal align in cell " + aCell + " doesn't match!", anHorizontalAlign,
				theStyle.getAlignment());
		}

		if (aVerticalAlign != null) { 
			assertEquals("Vertical align in cell " + aCell + " doesn't match!", aVerticalAlign,
				theStyle.getVerticalAlignment());
		}
	}

	protected void doTestCreatedFile(BinaryData aFile, boolean withMergeRegion) throws Exception, OfficeException {
    	POIExcelAccess      theAccess = this.createExcelAccess();
		Map<String, Object> theResult = theAccess.getValues(aFile);

		if (withMergeRegion) { 
			this.doTest(theResult, FORMULA_SHEET, "A12", TEST_VALUES_HEADER);
		}

		this.doTest(theResult, FORMULA_SHEET, "B13", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "C13", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "D13", 20d);
    	this.doTest(theResult, FORMULA_SHEET, "B14", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "C14", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "D14", 100d);
    	this.doTest(theResult, FORMULA_SHEET, "B15", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "C15", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "D15", 0d);
    	this.doTest(theResult, FORMULA_SHEET, "B16", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "C16", 10d);
    	this.doTest(theResult, FORMULA_SHEET, "D16", 1d);

    	int thePos = START_TEST_VALUES;
    	for (Entry<String, Object> theEntry : TEST_VALUES.entrySet()) {
    		this.doTest(theResult, FORMULA_SHEET, "A" + thePos, theEntry.getKey());
    		this.doTest(theResult, FORMULA_SHEET, "B" + thePos, theEntry.getValue());

    		thePos++;
    	}
	}

    /** 
	 * Return the value of the given cell in the requested format.
	 * 
	 * @param    aCell     The cell to get the value from.
	 * @param    aClass    The requested type of value.
	 * @return   The requested value.
	 */
	protected Object getValue(Cell aCell, Class<? extends Object> aClass) {
		if (aClass == Date.class) {
			return aCell.getDateCellValue();
		}
		else if (Number.class.isAssignableFrom(aClass)) {
			Double theValue = Double.valueOf(aCell.getNumericCellValue());

			if (aClass == Double.class) {
				return theValue;
			}
			else if (aClass == Long.class) {
				return theValue.longValue();
			}
			else if (aClass == Integer.class) {
				return theValue.intValue();
			}
			else if (aClass == Float.class) {
				return theValue.floatValue();
			}
		}

		return aCell.getStringCellValue();
	}

	/**
     * Test, if the given map contains the given value.
     * 
     * @param    aMap      The map containing the values to be checked.
     * @param    aValue    The value to be found in the map.
     * @param    isTrue    Flag, if the value has to be in the map.
     */
    protected void doTest(Map<String, Object> aMap, Object aValue, boolean isTrue) {
        if (isTrue) {
            assertTrue("Generated Excel file doesn't contain value '" + aValue + "'!", aMap.containsValue(aValue));
        }
        else {
            assertTrue("Generated Excel file contain value '" + aValue + "'!", !aMap.containsValue(aValue));
        }
    }

    /**
     * The test file to be used for testing.
     * @see       #testReadFileSimple()
     * @see       #testReadFileComplex()
     */
	protected BinaryData getExcelFile() {
    	return this.getFile(TEST_FILE);
    }

    /**
     * The template file to be used for testing.
     * @see       #testCreateFile()
     */
	protected BinaryData getExcelTemplate() {
    	return this.getFile(TEMPLATE_FILE);
    }

    /**
     * The file to be used for testing.
     */
	protected BinaryData getFile(String aName) {
    	File theFile = new File(FILE_PATH + aName);
    	
    	assertTrue("Missing " + aName, theFile.exists());
    	
		return BinaryDataFactory.createBinaryData(theFile);
    }

    /**
     * The filename of the resulting excel file.
     */
    protected File getTargetFile(String aName) {
		return new File("./test/temp/" + aName + this.dateFormat.format(new Date()) + POIUtil.XLSX_SUFFIX);
    }

	@SuppressWarnings({ "unused" })
    public static Test suite() {
		Test test;
		if (true) {
			test = new TestSuite(TestPOIExcel.class);
		} else {
			test = TestSuite.createTest(TestPOIExcel.class, "testColumnAutoFilter");
		}
		test = ServiceTestSetup.createSetup(test, MimeTypes.Module.INSTANCE, DataAccessService.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
    }

}
