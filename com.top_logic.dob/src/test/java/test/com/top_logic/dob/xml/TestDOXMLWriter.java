/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOStructureImpl;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.sql.DBMORepository;
import com.top_logic.dob.sql.DBMetaObject;
import com.top_logic.dob.xml.DOXMLWriter;

/**
 * Test the {@link DOXMLWriter} and the {@link DOImporter}.
 * 
 * @author Holger Borchard
 * @author <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestDOXMLWriter extends BasicTestCase {

	/** A current Date used as attribute */
	protected Date date;
	
	protected File testFile;

	/**
	 * Standard Constructor for Testcases.
	 * 
	 * @param aName
	 *            (Function) name of Test to execute
	 */
	public TestDOXMLWriter(String aName) {
		super(aName);
	}

	/**
	 * Set date to the current Date.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		date = new Date();
	}

	/**
	 * Clear {@link #date}.
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		date = null;
		if (testFile != null) {
		    testFile.delete();
		    testFile = null;
		}
	}

	/**
	 * Test the
	 * {@link com.top_logic.dob.xml.DOXMLWriter#writeDO2XML(com.top_logic.dob.DataObject,java.io.OutputStream)}
	 * Method
	 */
	public void testWriteDO2XML() throws DataObjectException, IOException {
		ExampleDataObject theExampleDataObject = this.createExampleDataObject();
		FileOutputStream theOutputStream = new FileOutputStream(
		        testFile = createNamedTestFile("testWriteDO2XML.xml"));
		try {
			new DOXMLWriter().writeDO2XML(theExampleDataObject, theOutputStream);
		} finally {
			theOutputStream.close();
		}

		DOImporter theImporter = new DOImporter();
		theImporter.doImport(testFile);
		List theDOs = theImporter.getDoList();
		assertEquals(1, theDOs.size());
		Iterator theIter = theDOs.iterator();

		assertTrue(theIter.hasNext());
		ExampleDataObject theResultObject = (ExampleDataObject) theIter.next();

		assertEquals("Schulte", theResultObject.getAttributeValue("Karl"));
		assertEquals("Meier", theResultObject.getAttributeValue("Franz"));
		assertEquals("Müller", theResultObject.getAttributeValue("Fritz"));

		ExampleDataObject theInternalObject = (ExampleDataObject) theResultObject
				.getAttributeValue("InternalObject");
		assertNotNull(theInternalObject);
		assertEquals(this.date, theInternalObject
				.getAttributeValue("Aktuelles Datum"));
		assertEquals(Boolean.TRUE, theInternalObject
				.getAttributeValue("True"));
		assertEquals(Integer.valueOf(1), theInternalObject
				.getAttributeValue("Eins"));

		assertFalse(theIter.hasNext());

	}

	/**
	 * Create an ExampleDataObject, that contains another ExampleDataObject.
	 * 
	 */
	private ExampleDataObject createExampleDataObject() {

		Map theMap = new HashMap();
		theMap.put("Karl", "Schulte");
		theMap.put("Franz", "Meier");
		theMap.put("Fritz", "Müller");
		theMap.put("InternalObject", this.createInternalDO());
		return new TestingExampleDataObject(theMap);
	}

	/**
	 * Create an ExampleDataObject.
	 */
	private ExampleDataObject createInternalDO() {
		Map theMap = new HashMap();
		theMap.put("Aktuelles Datum", this.date);
		theMap.put("True", Boolean.TRUE);
		theMap.put("Eins", Integer.valueOf(1));
		return new TestingExampleDataObject(theMap);
	}
	
	/**
     * Test writing {@link DBMetaObject}s via the DOXmlWriter.
     */
    public void testWriteMetaObjects() throws DataObjectException, IOException, SQLException {
        MOStructureImpl moi = new MOStructureImpl("struct", 10);
        
        moi.addAttribute(new MOAttributeImpl("int" , MOPrimitive.INTEGER));
        moi.addAttribute(new MOAttributeImpl("string", MOPrimitive.STRING));
        moi.addAttribute(new MOAttributeImpl("double", MOPrimitive.DOUBLE));
        moi.addAttribute(new MOAttributeImpl("date", "DB_DATE", MOPrimitive.DATE, 
			false, false, DBType.DATE, 10, 2));
        moi.addAttribute(new MOAttributeImpl("time", "DB_TIME", MOPrimitive.SQL_TIMESTAMP, 
			false, false, DBType.DATETIME, 0, 0));
        moi.addAttribute(new MOAttributeImpl("float", "DB_FLOAT", MOPrimitive.FLOAT, 
			false, false, DBType.FLOAT, 12, 6));
        moi.addAttribute(new MOAttributeImpl("bool", "DB_BOOL", MOPrimitive.BOOLEAN, 
			false, false, DBType.BOOLEAN, 0, 0));
        
        DBMORepository repository = new DBMORepository();
        repository.addMetaObject(moi);
        
        FileWriter out = new FileWriter(
                testFile = createNamedTestFile("testWriteMetaObjects.xml"));
        
        new DOXMLWriter().writeMetaObjects(repository, out);
        out.flush();
        
        // MORepositoryImporter ... // Cannot use this as this is defined in top_logic, mmh
    }


	/**
	 * Subclass of ExampleDataObject, that is used to set another
	 * type-attribute.
	 */
	static class TestingExampleDataObject extends ExampleDataObject {

		public TestingExampleDataObject(Map aMap) {
			super(aMap);
		}

		@Override
		public String getMetaObjectName() {
			return "DataObject";
		}
	}

   /**
     * The method constructing a test suite for this class.
     * 
     * @return The test to be executed.
     */
    public static Test suite() {
        return DOBTestSetup.createDOBTestSetup(new TestSuite(TestDOXMLWriter.class));
        // return new TestDOXMLWriter("testWriteMetaObjects");
    }

	/**
	 * The main program for executing this test also from console.
	 * 
	 * @param args
	 *            Will be ignored.
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

}
