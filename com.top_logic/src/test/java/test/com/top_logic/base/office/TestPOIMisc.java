/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import test.com.top_logic.TLTestSetup;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Simple test cases showing the general usage of the POI adaption for the excel
 * interface.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
 */
public class TestPOIMisc extends AbstractPOIExcelTest {
    
    /* ---------------------------------------------------------------------- *
     * Static Attributes
     * ---------------------------------------------------------------------- */
    private static final boolean PERFORMANCE_TEST = false;
    
    /* ---------------------------------------------------------------------- *
     * Test Methods for writing Excel files
     * ---------------------------------------------------------------------- */
    public void testSimplePOI() throws Exception {
        /* Load the excel file and initialise it. */
		FileInputStream is = new FileInputStream(
			ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/TestExcel.xls");
		Workbook theWorkbook = POIUtil.getWorkbook(is);
		Sheet theSheet1 = POIUtil.createIfNull("Sheet1", theWorkbook);
        /* Set the values. */
		Row theRow1 = POIUtil.createIfNull(0, theSheet1);
		Cell theCellA1 = POIUtil.createIfNull(0, theRow1);
		theCellA1.setCellValue(POIUtil.newRichTextString(theWorkbook, "Wert A.1"));

		Row theRow9 = POIUtil.createIfNull(9, theSheet1);
		Cell theCellC9 = POIUtil.createIfNull(2, theRow9);
        theCellC9.setCellValue(22);

		Row theRow2 = POIUtil.createIfNull(1, theSheet1);
		Cell theCellF2 = POIUtil.createIfNull(5, theRow2);
		theCellF2.setCellValue(CalendarUtil.createCalendar());
		POIUtil.formatCellToDate(theCellF2, theWorkbook, ExcelValue.DATE_FORMAT_STANDARD);

		Cell theCellC1 = POIUtil.createIfNull(2, theRow1);
		theCellC1.setCellValue(POIUtil.newRichTextString(theWorkbook, "Wert C.1"));
        /* Save the modified excel file. */
		FileOutputStream theOut = new FileOutputStream("./test/temp/Result-Simple-" + new Date().getTime() + ".xls");
        theWorkbook.write(theOut);
        theOut.close();
    }
    
    public void testSetValues() throws Exception {
        /* Create a map with values. */
        Map theMap = new HashMap();
        theMap.put("Sheet1!A1", "Christian Was Here");
        theMap.put("Sheet1!C9", Double.valueOf(1234));
        theMap.put("Sheet1!F2", new Date());
        theMap.put("Sheet2!C1", "Christian Was Here Too");
        /* Store the map. */
		ExcelAccess theExcel = ExcelAccess.newInstance();
		File source =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/TestExcel.xls");
        assertTrue("File '" + source.getAbsolutePath() + "' does not exist", source.exists());
		FileInputStream theStream = new FileInputStream(source);
		try {
			theExcel.setValues(theStream,
				new File("./test/temp/Result-SetValues_" + new Date().getTime() + ".xls"), theMap);
		}
		finally {
			theStream.close();
		}
    }
    
    public void testPerformance() throws Exception {
        if (PERFORMANCE_TEST) {
            System.out.println("Creating map with 3550 rows and 60 columns.");
            Map theMap = new TreeMap();

            for (int i = 1; i < 3501; i++) {
                for (int j = 1; j < 61; j++) {
                    theMap.put(toCoor(j, i), "Ich bin ein Wert.");
                }
            }

            System.out.println("Map creation finished. Writing to excel file.");
            long theStart = System.currentTimeMillis();
			ExcelAccess theExcel = ExcelAccess.newInstance();
            theExcel.setValues((InputStream) null,
				new File("./test/temp/Result-Performance_" + new Date().getTime() + ".xls"),
                               theMap);
            long theStop = System.currentTimeMillis();
            long theAmount = theStop - theStart;
            System.out.println("Test took " + theAmount + " milliseconds.");
        }
    }
    
    /* ---------------------------------------------------------------------- *
     * Test Methods for reading Excel files
     * ---------------------------------------------------------------------- */
    public void testGetValues() throws Exception {
		BinaryData theFile =
			FileManager.getInstance().getData(
				ModuleLayoutConstants.PATH_TO_MODULE_ROOT + "/" + ModuleLayoutConstants.SRC_TEST_DIR
					+ "/test/com/top_logic/base/office/data/TestExcel.xls");
		ExcelAccess theExcel = ExcelAccess.newInstance();
        Map theValues = theExcel.getValues(theFile);

        assertNotNull(theValues);
        assertFalse(theValues.isEmpty());

        Set theKeys = theValues.keySet();
        List theList = new ArrayList(theKeys);
        Collections.sort(theList);

        for (Iterator theIt = theList.iterator(); theIt.hasNext(); ) {
            Object theKey = theIt.next();
            assertTrue(theKey instanceof String);
            Object theValue = theValues.get(theKey);
            assertNotNull(theValue);
        }

        doTest(theValues, "Sheet1!A1", "Wert A.1");
        doTest(theValues, "Sheet1!A2", "Wert A.2");
        doTest(theValues, "Sheet1!C9", Double.valueOf(1000));
        doTest(theValues, "Sheet1!C4", Double.valueOf(1000));
        doTest(theValues, "Sheet2!C1", Double.valueOf(1235));
        doTest(theValues, "Sheet2!D2", Double.valueOf(8));
    }
    
    /* ---------------------------------------------------------------------- *
     * Private Methods
     * ---------------------------------------------------------------------- */
    private void doTest(Map aMap, String aKey, Object aValue) {
        assertEquals(aValue, aMap.get(aKey));
    }
    
    private String toCoor(int aColumn, int aRow) {
        int theColumnParam = aColumn;
        StringBuffer theBuffer = new StringBuffer();

        if (theColumnParam > 26) {
            int theColumn = (theColumnParam / 26);

            theColumnParam = (theColumnParam % 26);

            theBuffer.append(this.toValue(theColumn));
        }

        String theString = theBuffer.append(this.toValue(theColumnParam)).
                          append(Integer.toString(aRow)).toString();
        return (theString);
    }
    
    /**
     * This function deserves an explanation. 
     */
    private String toValue(int aNumber) {
        return "" + (char) ('@' + aNumber);
    }
    
    public static Test suite () {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestPOIMisc.class));
    }

    public static void main(String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite ());
    }


}
