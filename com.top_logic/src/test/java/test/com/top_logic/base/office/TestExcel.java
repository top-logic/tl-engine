/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.AbstractOffice;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test case for {@link ExcelAccess}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestExcel extends BasicTestCase {

    /**
     * Constructor for TestPowerpoint.
     */
    public TestExcel(String arg0) {
        super(arg0);
    }

    public void testGetValuesArray() throws Exception {
		ExcelAccess theExcel = ExcelAccess.newInstance();

        Object[][] theArray = theExcel.getValues(this.getExcelFile(), "Sheet2");

        double sum = 0.0;
        for (int theX = 0; theX < theArray.length; theX++) {
            Object[] theSub = theArray[theX];

            for (int theY = 0; theY < theSub.length; theY++) {
                sum += ((Double) theSub[theY]).doubleValue();
                // System.out.println("(" + theX + "," + theY + "): " + theSub[theY]);
            }
        }
        assertEquals(36968.0, sum, EPSILON);
    }

    public void testGetValues() throws Exception {
        Object      theKey;
        Object      theValue;

		ExcelAccess theExcel = ExcelAccess.newInstance();
        Map         theResult = theExcel.getValues(this.getExcelFile());

        assertNotNull("getValues(File) returns null (should be a map)!", theResult);
        assertTrue("getValues(File) returns an empty map!", !theResult.isEmpty());

        Set  theKeys = theResult.keySet();
        List theList = new ArrayList(theKeys);

        Collections.sort(theList);

        for (Iterator theIt = theList.iterator(); theIt.hasNext(); ) {
            theKey = theIt.next();

            assertTrue("Key in map is no string, but " + theKey.getClass().getName(), 
                       theKey instanceof String);

            theValue = theResult.get(theKey);

            assertNotNull("Value for key '" + theKey + "' in map is null", theValue);
        }

        this.doTest(theResult, "Sheet1!A1", "Wert A.1");
        this.doTest(theResult, "Sheet1!A2", "Wert A.2");
        this.doTest(theResult, "Sheet1!C9", Double.valueOf(1000));
        this.doTest(theResult, "Sheet1!C4", Double.valueOf(1000));
        this.doTest(theResult, "Sheet1!F2", this.getTestDate());
        this.doTest(theResult, "Sheet2!C1", Double.valueOf(1235));
        this.doTest(theResult, "Sheet2!D2", Double.valueOf(8));
    }

    protected Date getTestDate() {
        return (this.getTestDate(2004, Calendar.FEBRUARY, 1));
    }

    protected Date getTestDate(int aYear, int aMonth, int aDay) {
		Calendar theCal = newCalendar();

        theCal.set(Calendar.YEAR, aYear);
        theCal.set(Calendar.MONTH, aMonth);
        theCal.set(Calendar.DAY_OF_MONTH, aDay);

        return (this.getTestDate(theCal));
    }

	private Calendar newCalendar() {
		return CalendarUtil.createCalendar();
	}

    protected Date getTestDate(Date aDate) {
		Calendar theCal = newCalendar();

        if (aDate != null) {
            theCal.setTime(aDate);
        }

        return (this.getTestDate(theCal));
    }

    protected Date getTestDate(Calendar aCal) {
		DateUtil.adjustTime(aCal, 0, 0, 0, 0);
        return (aCal.getTime());
    }

    public void testSetValues() throws Exception {
        Map            theMap    = new HashMap();
		AbstractOffice theExcel = ExcelAccess.newInstance();
        File           theTarget = this.getTargetFile();
        Date           theDate   = this.getTestDate(new Date());

        theMap.put("Sheet1!A1", "Test Wert 1");
        theMap.put("Sheet1!C9", Double.valueOf(20));
        theMap.put("Sheet1!F2", theDate);
        theMap.put("Sheet2!C1", "Test Wert 2");
        
        theExcel.setValues(this.getExcelFile(), theTarget, theMap);

        assertTrue("Excel file not written!", theTarget.exists());
        assertTrue("Excel file is empty!", theTarget.length() > 0);

		Map theResult = theExcel.getValues(BinaryDataFactory.createBinaryData(theTarget));

        this.doTest(theResult, "Wert A.1", false);
        this.doTest(theResult, Double.valueOf(1235), false);
        this.doTest(theResult, this.getTestDate(), false);

        this.doTest(theResult, "Test Wert 1", true);
        this.doTest(theResult, "Test Wert 2", true);
        this.doTest(theResult, theDate, true);
        this.doTest(theResult, Double.valueOf(20), true);

        assertTrue("Unable to delete generated Excel file!", 
        		theTarget.delete());
    }


    protected void doTest(Map aMap, String aKey, Object aValue) {
        assertEquals(aValue, aMap.get(aKey));
    }

    /**
     * Test, if the given map contains the given value.
     * 
     * @param    aMap      The map containing the values to be checked.
     * @param    aValue    The value to be found in the map.
     * @param    isTrue    Flag, if the value has to be in the map.
     */
    protected void doTest(Map aMap, Object aValue, boolean isTrue) {
        if (isTrue) {
            assertTrue("Generated Excel file doesn't contain value '" +  
                       aValue + "'!", aMap.containsValue(aValue));
        }
        else {
            assertTrue("Generated Excel file contain value '" +  
                       aValue + "'!", !aMap.containsValue(aValue));
        }
    }

    /**
     * The file to be used for testing.
     */
	protected BinaryData getExcelFile() throws Exception {
		File theFile =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/office/data/TestExcel.xls");
        assertTrue("Missing TestExcel.xls", theFile.exists());
		return BinaryDataFactory.createBinaryData(theFile);
    }

    /**
     * The filename of the resulting word file.
     */
    protected File getTargetFile() throws IOException {
		return new File("./test/temp/Result_" + new Date().getTime() + ExcelAccess.XLS_EXT);
    }

    public static Test suite () {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestExcel.class));
    }

    public static void main(String[] args) {
        Logger.configureStdout("WARN");
        junit.textui.TestRunner.run(suite ());
    }
}
