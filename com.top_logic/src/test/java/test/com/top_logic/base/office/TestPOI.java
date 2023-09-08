/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import test.com.top_logic.TLTestSetup;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.base.office.style.FontStyle;
import com.top_logic.base.office.style.TextOffset;
import com.top_logic.base.office.style.Underline;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test case for the Apache POI way of writing Excel Sheets.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>Christian Domsch</a>
 */
@SuppressWarnings("javadoc")
public class TestPOI extends AbstractPOIExcelTest {
    
     // Static Attributes
    
    /* Cell references. */
    private static final String SHEET1 = "Sheet1";
    private static final String SHEET1_A1 = "Sheet1!A1";
    private static final String SHEET1_A2 = "Sheet1!A2";
    private static final String SHEET1_A3 = "Sheet1!A3";
    private static final String SHEET1_A4 = "Sheet1!A4";
    private static final String SHEET1_A5 = "Sheet1!A5";

	private static final String SHEET1_A6 = "Sheet1!A6";
    
    /* File prefixes and suffixes. */
	private static final String FILE_PREFIX = "./" + ModuleLayoutConstants.SRC_TEST_DIR;
    private static final String TEST_PREFIX = "/test/com/top_logic/base/office/data/JUnit-Excel-";
    private static final String TEMP_PREFIX = "./test/temp/JUnit-Excel-";
    private static final String EXCEL_SUFFIX = ".xls";

	private static final File FILE_WITH_CHART = new File(
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/TestPOI_TestChartExtract.xls");

    /* The delta to compare doubles. */
    private static final double DELTA = 0.0001d;
    private static final IllegalStream ILLEGAL_STREAM = new IllegalStream();

	private static final boolean WEIGHT_BOLD = true;

	private static final boolean WEIGHT_NORMAL = false;

    /* ---------------------------------------------------------------------- *
     * Positive Test Methods
     * ---------------------------------------------------------------------- */
    public void testReadAndWriteBasicTypes() throws Exception {
        Date date1 = DateUtil.adjustToDayBegin(new Date());
        Date date2 = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy").parse("27.04.1981");

        /* Initialise the map. */
        Map theValues = new HashMap();
        theValues.put(SHEET1_A1, "String");
        theValues.put(SHEET1_A2, Double.valueOf(1234));
        theValues.put(SHEET1_A3, Boolean.TRUE);
        theValues.put(SHEET1_A4, date1);
        theValues.put(SHEET1_A5, date2);
        /* Set the values in the excel file. */
		ExcelAccess theExcel = ExcelAccess.newInstance();
        theExcel.setValues((InputStream)null, getTempFile("basic-types"), theValues);
        /* Now load the file again. */
		Map theResult = theExcel.getValues(BinaryDataFactory.createBinaryData(getTempFile("basic-types")));
        /* Check the result*/
        assertNotNull(theResult);
        assertFalse(theResult.isEmpty());
        assertEquals(theValues.get(SHEET1_A1), theResult.get(SHEET1_A1));
        assertEquals(theValues.get(SHEET1_A2), theResult.get(SHEET1_A2));
        assertEquals(theValues.get(SHEET1_A3), theResult.get(SHEET1_A3));
        assertEquals(theValues.get(SHEET1_A4), theResult.get(SHEET1_A4));
        assertEquals(theValues.get(SHEET1_A5), theResult.get(SHEET1_A5));
    }

	public void testFormatCaching() throws Exception {
		Date date = DateUtil.adjustToDayBegin(new Date());
		Double doubl = Double.valueOf(12.123d);

		List<ExcelValue> values = new ArrayList<>();
		ExcelValue a1Value = new ExcelValue(SHEET1, "A1", doubl);
		a1Value.setDataFormat("#,##0.0");
		values.add(a1Value);
		ExcelValue a2Value = new ExcelValue(SHEET1, "A2", doubl);
		a2Value.setDataFormat("#,##0.000");
		values.add(a2Value);
		ExcelValue a4Value = new ExcelValue(SHEET1, "A4", date);
		a4Value.setDataFormat(ExcelValue.DATE_FORMAT_D_MMM_YY);
		values.add(a4Value);
		ExcelValue a5Value = new ExcelValue(SHEET1, "A5", date);
		values.add(a5Value);
		a5Value.setDataFormat(ExcelValue.DATE_FORMAT_MMM_YY);

		Workbook workbook = writeReadWorkbook(values, "format-caching");

		Cell a5cell = getCell(workbook, SHEET1, "A5");
		assertEquals(ExcelValue.DATE_FORMAT_MMM_YY, a5cell.getCellStyle().getDataFormatString());
		Cell a4cell = getCell(workbook, SHEET1, "A4");
		assertEquals("Dataformat does not take part in caching mechanism", ExcelValue.DATE_FORMAT_D_MMM_YY, a4cell
			.getCellStyle().getDataFormatString());
		Cell a1cell = getCell(workbook, SHEET1, "A1");
		assertEquals("#,##0.0", a1cell.getCellStyle().getDataFormatString());
		Cell a2cell = getCell(workbook, SHEET1, "A2");
		assertEquals("#,##0.000", a2cell.getCellStyle().getDataFormatString());
	}

	public void testFontStyleCopyOnWrite() {
		FontStyle s0 = FontStyle.NONE;
		FontStyle s1 = s0.setBold();
		FontStyle s2 = s1.copy();

		assertNotEquals(s0, s1);
		assertEquals(s1, s2);
		assertTrue(s0.isShared());
		assertFalse(s1.isShared());

		s1 = s1.freeze();
		assertTrue(s1.isShared());
		assertFalse(s2.isShared());

		s2 = s2.setItalic();
		assertNotEquals(s1, s2);

		s1 = s1.setItalic();
		assertEquals(s1, s2);
	}

	public void testReadAndWriteFormats() throws Exception {
        Date date1 = DateUtil.adjustToDayBegin(new Date());
        Date date2 = CalendarUtil.newSimpleDateFormat("dd.MM.yyyy").parse("27.04.1981");

        ExcelValue value;
        List<ExcelValue> values = new ArrayList<>();
        value = new ExcelValue(SHEET1, "A1", "String");
        value.setBackgroundColor(Color.RED);
        values.add(value);
        value = new ExcelValue(SHEET1, "A2", Double.valueOf(1234));
        value.setBackgroundColor(Color.GREEN);
        values.add(value);
        value = new ExcelValue(SHEET1, "A3", Boolean.TRUE);
        value.setBorderTop(ExcelValue.BORDER_THIN);
        values.add(value);
        value = new ExcelValue(SHEET1, "A4", date1);
		value.setBold();
        values.add(value);
		value.setDataFormat(ExcelValue.DATE_FORMAT_D_MMM_YY);
        value = new ExcelValue(SHEET1, "A5", date2);
        values.add(value);
		value.setDataFormat(ExcelValue.DATE_FORMAT_MMM_YY);

		value = new ExcelValue(SHEET1, "A6", "Font");
		value.setTextColor(Color.GREEN);
		value.setFontName("Courier");
		value.setFontSize(20.0);
		value.setBold();
		value.setItalic();
		value.setUnderline(Underline.DOUBLE);
		value.setStrikeout();
		value.setTextOffset(TextOffset.SUPER);
		values.add(value);

		Map theResult = readWriteValues(values, "basic-types");
        /* Check the result*/
        assertNotNull(theResult);
        assertFalse(theResult.isEmpty());
        assertEquals(values.get(0).getValue(), theResult.get(SHEET1_A1));
        assertEquals(values.get(1).getValue(), theResult.get(SHEET1_A2));
        assertEquals(values.get(2).getValue(), theResult.get(SHEET1_A3));
        assertEquals(values.get(3).getValue(), theResult.get(SHEET1_A4));
        assertEquals(values.get(4).getValue(), theResult.get(SHEET1_A5));
		assertEquals(values.get(5).getValue(), theResult.get(SHEET1_A6));

        
        // check formats:
        
		Cell theCell;
		CellStyle theStyle;
		Font theFont;
        HSSFColor colorRed = POIUtil.getNearestColor(Color.RED), colorGreen = POIUtil.getNearestColor(Color.GREEN);
		Workbook theWorkbook = writeReadWorkbook(values, "basic-types");
        
        theCell = getCell(theWorkbook, SHEET1, "A1");
        theStyle = theCell.getCellStyle();
        theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
        assertTrue(theStyle.getFillForegroundColor() == colorRed.getIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorGreen.getIndex());
		assertTrue(theStyle.getBorderTop() != BorderStyle.THIN);
		assertTrue(theStyle.getBorderBottom() == BorderStyle.NONE);
		assertTrue(theFont.getBold() == WEIGHT_NORMAL);
        
        theCell = getCell(theWorkbook, SHEET1, "A2");
        theStyle = theCell.getCellStyle();
        theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorRed.getIndex());
        assertTrue(theStyle.getFillForegroundColor() == colorGreen.getIndex());
		assertTrue(theStyle.getBorderTop() != BorderStyle.THIN);
		assertTrue(theStyle.getBorderBottom() == BorderStyle.NONE);
		assertTrue(theFont.getBold() == WEIGHT_NORMAL);

        theCell = getCell(theWorkbook, SHEET1, "A3");
        theStyle = theCell.getCellStyle();
        theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorRed.getIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorGreen.getIndex());
		assertTrue(theStyle.getBorderTop() == BorderStyle.THIN);
		assertTrue(theStyle.getBorderBottom() == BorderStyle.NONE);
		assertTrue(theFont.getBold() == WEIGHT_NORMAL);

        theCell = getCell(theWorkbook, SHEET1, "A4");
        theStyle = theCell.getCellStyle();
        theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorRed.getIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorGreen.getIndex());
		assertTrue(theStyle.getBorderTop() != BorderStyle.THIN);
		assertTrue(theStyle.getBorderBottom() == BorderStyle.NONE);
		assertTrue(theFont.getBold() == WEIGHT_BOLD);

        theCell = getCell(theWorkbook, SHEET1, "A5");
        theStyle = theCell.getCellStyle();
        theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorRed.getIndex());
        assertTrue(theStyle.getFillForegroundColor() != colorGreen.getIndex());
		assertTrue(theStyle.getBorderTop() != BorderStyle.THIN);
		assertTrue(theStyle.getBorderBottom() == BorderStyle.NONE);
		assertTrue(theFont.getBold() == WEIGHT_NORMAL);

		theCell = getCell(theWorkbook, SHEET1, "A6");
		theStyle = theCell.getCellStyle();
		theFont = theWorkbook.getFontAt(theStyle.getFontIndex());
		assertEquals(colorGreen.getIndex(), theFont.getColor());
		assertEquals("Courier", theFont.getFontName());
		assertTrue(theFont.getItalic());
		assertTrue(theFont.getStrikeout());
		assertEquals(WEIGHT_BOLD, theFont.getBold());
		assertEquals(20, theFont.getFontHeightInPoints());
		assertEquals(Font.SS_SUPER, theFont.getTypeOffset());
		assertEquals(Font.U_DOUBLE, theFont.getUnderline());

    }

	private Workbook writeReadWorkbook(List<ExcelValue> values, String tmpFileKey) throws OfficeException,
			IOException, FileNotFoundException {
		File tempFile = getTempFile(tmpFileKey);
		ExcelAccess theExcel = ExcelAccess.newInstance();
		/* Set the values in the excel file. */
		theExcel.setValuesDirect((InputStream) null, tempFile, ExcelValue.toArray(values), true);
		/* Now load the file again. */
		return POIUtil.getWorkbook(new FileInputStream(tempFile));
	}

	private Map readWriteValues(List<ExcelValue> values, String tmpFileKey) throws OfficeException {
		ExcelAccess theExcel = ExcelAccess.newInstance();
		/* Set the values in the excel file. */
		theExcel.setValuesDirect((InputStream) null, getTempFile(tmpFileKey),
			ExcelValue.toArray(values), true);
		/* Now load the file again. */
		return theExcel.getValues(BinaryDataFactory.createBinaryData(getTempFile(tmpFileKey)));
	}

	private Cell getCell(Workbook aWorkbook, String aSheetName, String aCell) {
        int[] rowCol = POIUtil.convertCellName(aCell);
		Sheet theSheet = aWorkbook.getSheet(aSheetName);
		Row theRow = theSheet.getRow(rowCol[0]);
		Cell theCell = theRow.getCell(rowCol[1]);
        return theCell;
    }


    public void testReadFormulas() throws Exception {
        /* Set the values in the excel file. */
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Now load the file again. */
		File f = new File(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/JUnit-Excel-formulas.xls");
		assertTrue("The file '" + f.getAbsolutePath() + "' does not exist", f.exists());
		
		InputStream testResource = new FileInputStream(f);
        assertNotNull(testResource);

        Map theResult = theExcel.getValues(testResource);
        /* Check the result*/
        assertNotNull(theResult);
        assertFalse(theResult.isEmpty());
        assertEquals(3d, ((Double) theResult.get("Sheet1!C1")).doubleValue(), DELTA);
        assertEquals(7d, ((Double) theResult.get("Sheet1!C2")).doubleValue(), DELTA);
        assertEquals(21d, ((Double) theResult.get("Sheet1!C3")).doubleValue(), DELTA);
        assertEquals("Hallo Welt", theResult.get("Sheet1!C5").toString());
        assertFalse(((Boolean) theResult.get("Sheet1!C7")).booleanValue());
    }
    
    /* ---------------------------------------------------------------------- *
     * Negative Test Methods
     * ---------------------------------------------------------------------- */
    public void testGetValuesFileString() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with a null file. */
        try {
			theExcel.getValues(null, "Sheet1");
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with an invalid file. */
        try {
			theExcel.getValues(BinaryDataFactory.createBinaryData(new File("C:/invalid")), "Sheet1");
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        }
        try {
            theExcel.getValues(getTestFileAsFile("formulas"), "Sheet");
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
    }
    
    public void testGetValuesFile() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with a null file. */
        try {
			theExcel.getValues((BinaryData) null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with an invalid file. */
        try {
			theExcel.getValues(BinaryDataFactory.createBinaryData(new File("C:/invalid")));
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        }
    }
    
    public void testGetValuesInputStream() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with a null inputstream. */
        try {
            theExcel.getValues((InputStream) null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with a invalid inputstream. */
        try {
            theExcel.getValues(ILLEGAL_STREAM);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(IOException.class, theOE.getCause().getClass());
        }
    }
    
    public void testSetValuesDirectInputStreamStringExcelValue() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with null array. */
        try {
            theExcel.setValuesDirect(null, getTempFile("illegal"), null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with null filename. */
        try {
            theExcel.setValuesDirect(null, (File) null, new ExcelValue[0]);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with readonly filename. */
        try {
            theExcel.setValuesDirect(null, getReadOnlyTempFile("illegal"), new ExcelValue[0]);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        } finally {
            /* We have to delete it, because it is readonly. */
            getTempFile("illegal").delete();
        }
        /* Test with illegal stream. */
        try {
            theExcel.setValuesDirect(ILLEGAL_STREAM, getTempFile("illegal"), new ExcelValue[0]);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(IOException.class, theOE.getCause().getClass());
        }
    }
    
    public void testSetValuesDirectInputStreamFileExcelValue() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with null array. */
        try {
            theExcel.setValuesDirect(null, getTempFile("illegal"), null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with null file. */
        try {
            theExcel.setValuesDirect(null, (File) null, new ExcelValue[0]);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException texpected) { /* expected */ }
        /* Test with readonly file. */
        try {
            theExcel.setValuesDirect(null, getReadOnlyTempFile("illegal"), new ExcelValue[0]);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        } finally {
            /* We have to delete it, because it is readonly. */
            getTempFile("illegal").delete();
        }
        /* Test with illegal stream. */
        try {
            theExcel.setValuesDirect(ILLEGAL_STREAM, getTempFile("illegal"), new ExcelValue[0]);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(IOException.class, theOE.getCause().getClass());
        }
    }
    
    public void testSetValuesInputStreamStringMap() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with null map. */
        try {
            theExcel.setValues((InputStream) null, getTempFile("illegal"), null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with null filename. */
        try {
            theExcel.setValues((InputStream) null, (File) null, Collections.EMPTY_MAP);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException texpected) { /* expected */ }
        /* Test with readonly filename. */
        try {
            theExcel.setValues((InputStream) null, getReadOnlyTempFile("illegal"), Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        } finally {
            /* We have to delete it, because it is readonly. */
            getTempFile("illegal").delete();
        }
        /* Test with illegal stream. */
        try {
            theExcel.setValues(ILLEGAL_STREAM, getTempFile("illegal"), Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(IOException.class, theOE.getCause().getClass());
        }
    }
    
    public void testSetValuesInputStreamFileMap() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with null map. */
        try {
            theExcel.setValues((InputStream) null, getTempFile("illegal"), null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with null file. */
        try {
            theExcel.setValues((InputStream) null, (File) null, Collections.EMPTY_MAP);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with readonly file. */
        try {
            theExcel.setValues((InputStream) null, getReadOnlyTempFile("illegal"), Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        } finally {
            /* We have to delete it, because it is readonly. */
            getTempFile("illegal").delete();
        }
        /* Test with illegal stream. */
        try {
            theExcel.setValues(ILLEGAL_STREAM, getTempFile("illegal"), Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(IOException.class, theOE.getCause().getClass());
        }
    }
    
    public void testSetValuesFileStringMap() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();
        /* Test with null map. */
        try {
			theExcel.setValues((BinaryData) null, getTempFile("illegal"), null);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with null filename. */
        try {
			theExcel.setValues((BinaryData) null, (File) null, Collections.EMPTY_MAP);
            fail("A NullPointerException should be thrown.");
        } catch (NullPointerException expected) { /* expected */ }
        /* Test with readonly filename. */
        try {
			theExcel.setValues((BinaryData) null, getReadOnlyTempFile("illegal"), Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        } finally {
            /* We have to delete it, because it is readonly. */
            getTempFile("illegal").delete();
        }
        /* Test with illegal file to read from. */
        try {
			theExcel.setValues(BinaryDataFactory.createBinaryData(new File("")), getTempFile("illegal"),
				Collections.EMPTY_MAP);
            fail("An OfficeException should be thrown.");
        } catch (OfficeException theOE) {
            assertEquals(FileNotFoundException.class, theOE.getCause().getClass());
        }
    }
    
	/**
	 * Test extracting chart from {@link #FILE_WITH_CHART}.
	 */
	public void testRemoteChartExtract() {

		assertTrue("TestFile was removed?", FILE_WITH_CHART.exists());

		String choord = "RM!chart!Chart 7";
		Collection<String> chartCoord = Collections.singletonList(choord);

		ExcelAccess theExcel = ExcelAccess.newInstance();
		Map values = theExcel.getValues(BinaryDataFactory.createBinaryData(FILE_WITH_CHART), chartCoord);

		if (values.size() == 0) {
			/* Exptected due to the known bug in ticket #9333: Charts are not extracted. */
		} else {
			fail("Test should fail due to the known bug in ticket #9333: Charts are not extracted.");

			File theFile = (File) values.get(chartCoord);
			assertTrue(theFile.exists());
			assertTrue(theFile.getName().endsWith(".png"));
			assertTrue("Failed to delete '" + theFile + "'", theFile.delete());
		}
	}

    /* ---------------------------------------------------------------------- *
     * Private Methods
     * ---------------------------------------------------------------------- */
	private BinaryData getTestFileAsFile(String anIndex) {
		return BinaryDataFactory.createBinaryData(new File(FILE_PREFIX + TEST_PREFIX + anIndex + EXCEL_SUFFIX));
    }
    
    private String getTempFilename(String anIndex) {
        return TEMP_PREFIX + anIndex + EXCEL_SUFFIX;
    }
    
    private File getTempFile(String anIndex) {
        return new File(getTempFilename(anIndex));
    }
    
    private File getReadOnlyTempFile(String anIndex) throws IOException {
        File theFile = getTempFile(anIndex);
        theFile.createNewFile();
        theFile.setReadOnly();
        return theFile;
    }

    /*package protected*/ static class IllegalStream extends InputStream {
        
        // implementation of InputStream
        
        /** {@inheritDoc} */
        @Override
		public int read() throws IOException {
            throw new IOException();
        }
    }

    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestPOI.class));
    }

    public static void main(String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite());
    }
    


}
