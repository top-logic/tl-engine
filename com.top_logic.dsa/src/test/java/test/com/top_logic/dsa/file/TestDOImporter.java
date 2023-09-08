/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.file;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.DOList;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOCollection;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.xml.DOXMLWriter;
import com.top_logic.dsa.DataAccessException;
import com.top_logic.dsa.file.DOImporter;

/**
 * Test The {@link DOImporter}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestDOImporter extends TestCase {

    /** 
     * Create a new TestDOImporter for aName.
     */
    public TestDOImporter(String aName) {
        super(aName);
    }

    /**
     * Test a DOImporter with an empty configuration.
     */
    public void testUnconfigured() {
        // The triggered warning is intended 
        DOImporter doimp = new DOImporter("NoSuchConfig");
		File file = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dsa/file/doData.xml");
        assertTrue(doimp.doImport(file));
        Collection theCol = doimp.getDoColl();
        assertEquals(0, theCol.size());
    }

    /**
     * Test a DOImporter with the default configuration.
     */
    public void testDOImporter() throws DataObjectException {
        DOImporter doimp = new DOImporter();
		File file = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dsa/file/doImporter.xml");
        assertTrue(doimp.doImport(file));
        List theList = (List) doimp.getDoColl();
        assertEquals(2, theList.size());

        DataObject theDO = (DataObject) theList.get(0);
        assertEquals("x64"          , theDO.getAttributeValue("string"));
		assertEquals(Integer.valueOf(42), theDO.getAttributeValue("int"));

        theDO = (DataObject) theList.get(1);
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2007, Calendar.DECEMBER, 31, 23, 59, 55);
		calendar.set(Calendar.MILLISECOND, 44);
		assertEquals(calendar.getTime(), theDO.getAttributeValue("date"));
		calendar.set(2008, Calendar.JANUARY, 4, 15, 5, 55);
		calendar.set(Calendar.MILLISECOND, 444);
		assertEquals(calendar.getTime(), theDO.getAttributeValue("strictDate"));
		assertEquals(Double.valueOf(Math.E), theDO.getAttributeValue("double"));
		assertEquals(Float.valueOf((float) Math.PI), theDO.getAttributeValue("float"));
		assertEquals(Long.valueOf(3141592653589793238L), theDO.getAttributeValue("long"));
		assertEquals(Integer.valueOf(271828182), theDO.getAttributeValue("int"));
    }

    /**
     * Test a DOImporter with additional CharacterEvents.
     */
    public void testDOImporterCharacterEvents () throws DataObjectException {
        DOImporter doimp = new DOImporter(/* needsCharacterEvents */ true);
		File file = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/dsa/file/doImporter.xml");
        assertTrue(doimp.doImport(file));
        List theList = (List) doimp.getDoColl();
        assertEquals(2, theList.size());
        
        DataObject theDO = (DataObject) theList.get(0);
        assertEquals("x64"          , theDO.getAttributeValue("string"));
		assertEquals(Integer.valueOf(42), theDO.getAttributeValue("int"));

        theDO = (DataObject) theList.get(1);
		Calendar calendar = CalendarUtil.createCalendar();
		calendar.set(2007, Calendar.DECEMBER, 31, 23, 59, 55);
		calendar.set(Calendar.MILLISECOND, 44);
		assertEquals(calendar.getTime(), theDO.getAttributeValue("date"));
		calendar.set(2008, Calendar.JANUARY, 4, 15, 5, 55);
		calendar.set(Calendar.MILLISECOND, 444);
		assertEquals(calendar.getTime(), theDO.getAttributeValue("strictDate"));
		assertEquals(Double.valueOf(Math.E), theDO.getAttributeValue("double"));
		assertEquals(Float.valueOf((float) Math.PI), theDO.getAttributeValue("float"));
		assertEquals(Long.valueOf(3141592653589793238L), theDO.getAttributeValue("long"));
		assertEquals(Integer.valueOf(271828182), theDO.getAttributeValue("int"));
    }

    public void testListAttributeImport() throws DataObjectException, DataAccessException, UnsupportedEncodingException {
    	MOClassImpl A = new MOClassImpl("A");
    	MOClassImpl B = new MOClassImpl("B");
    	MOCollection listOfB = MOCollectionImpl.createListType(B);
		A.addAttribute(new MOAttributeImpl("bs", listOfB));
		A.freeze();
    	B.addAttribute(new MOAttributeImpl("x", MOPrimitive.INTEGER));
    	B.addAttribute(new MOAttributeImpl("y", MOPrimitive.STRING));
    	B.freeze();
    	
    	DefaultDataObject a = new DefaultDataObject(A);
    	List bs = new DOList(listOfB);
    	{
    		DefaultDataObject b1 = new DefaultDataObject(B);
    		b1.setAttributeValue("x", 42);
    		b1.setAttributeValue("y", "Hello world!");
    		bs.add(b1);
    	}
    	{
    		DefaultDataObject b2 = new DefaultDataObject(B);
    		b2.setAttributeValue("x", 13);
    		b2.setAttributeValue("y", "<some 'special' \"characters\".>");
    		bs.add(b2);
    	}
		a.setAttributeValue("bs", bs);
		
		String aSerialized = new DOXMLWriter().convertDO2XML(a);
		
		DOImporter importer = new DOImporter();
		importer.doImport(new ByteArrayInputStream(aSerialized.getBytes("utf-8")));
		DataObject aImported = (DataObject) importer.getDoColl().iterator().next();
		
		assertEquals(a, aImported);
    }
    
    /**
     * the suite of tests to execute.
     */
    public static Test suite () {
    	return DSATestSetup.createDSATestSetup(new TestSuite (TestDOImporter.class));
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        Logger.configureStdout();   // "INFO"
        TestRunner.run (suite ());
    }


}

