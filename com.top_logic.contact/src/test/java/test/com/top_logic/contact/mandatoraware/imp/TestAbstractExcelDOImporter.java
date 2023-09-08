/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.mandatoraware.imp;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.util.sched.model.TaskTestUtil;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.contact.mandatoraware.imp.AbstractExcelDOImporter;
import com.top_logic.dob.DataObject;
import com.top_logic.element.structured.wrap.Mandator;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestAbstractExcelDOImporter extends BasicTestCase {

    private static final String EXCEL_NULL = "";
    
    public void testRun() throws Exception {
        
        URL theURL = TestedAbstractExcelDOImporter.class.getResource("TestAbstractExcelDOImporter.xls");
        File theFile = new File(theURL.toURI());
        
        TestedAbstractExcelDOImporter importer = new TestedAbstractExcelDOImporter(theFile, "Tabelle1", false);
		TaskTestUtil.initTaskLog(importer);
        importer.run();
        
        assertEquals(6, importer.results.size());
        for (int iRow=0; iRow<6; iRow++) {
            DataObject dataobject = importer.results.get(iRow);
            for (int iCol=0; iCol<3; iCol++) {
                assertEquals("Tabelle1" + (char)(65+iCol) + "" + (iRow+2), dataobject.getAttributeValue("Spalte " + (char)(65+iCol)));
            }
        }
        
        // Sheet not found
        importer = new TestedAbstractExcelDOImporter(theFile, "Gibts nicht", false);
		TaskTestUtil.initTaskLog(importer);
        importer.run();
        assertEquals(0, importer.results.size());
        
        // Invalid columns
        importer = new TestedAbstractExcelDOImporter(theFile, "Tabelle3", false);
		TaskTestUtil.initTaskLog(importer);
        importer.run();
        assertEquals(0, importer.results.size());
        
        importer = new TestedAbstractExcelDOImporter(theFile, "Tabelle2", false);
		TaskTestUtil.initTaskLog(importer);
        importer.run();
        assertEquals(8, importer.results.size());
        for (int iRow=0; iRow<8; iRow++) {
            DataObject row = importer.results.get(iRow);
            assertNotNull(row);
            for (int iCol=0; iCol<3; iCol++) {
                String actual = (String) row.getAttributeValue("Spalte " + (char)(65+iCol)); 
                String value = EXCEL_NULL;
                if (iRow == 0 && iCol == 0) {
                    assertEquals(value, actual);
                }
                else if (iRow == 0 && iCol == 1) {
                    assertEquals(value, actual);
                }
                else if (iRow == 1 && iCol == 0) {
                    assertEquals(value, actual);
                }
                else if (iRow == 6 && iCol == 2) {
                    assertEquals(value, actual);
                }
                else if (iRow == 7 && iCol == 1) {
                    assertEquals(value, actual);
                }
                else if (iRow == 7 && iCol == 2) {
                    assertEquals(value, actual);
                }
                else {
                    value = "Tabelle2" + (char)(65+iCol) + "" + (iRow+2);
                    assertEquals(value, actual);
                }
            }
        }
        
        // table empty rows
        importer = new TestedAbstractExcelDOImporter(theFile, "Tabelle4", false);
		TaskTestUtil.initTaskLog(importer);
        importer.run();
        assertEquals(9, importer.results.size());
        for (int iRow=0; iRow<9; iRow++) {
            DataObject row = importer.results.get(iRow);
            assertNotNull(row);
            
            String value = (String) row.getAttributeValue("Spalte A");
            if (iRow == 3 || iRow==4 || iRow ==7) {
                assertEquals(EXCEL_NULL, value);
            }
            else {
                assertEquals("Tabelle4A" + (iRow+2), value);
            }
        }
        
    }
    
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestAbstractExcelDOImporter.class));
    }
    
    private class TestedAbstractExcelDOImporter extends AbstractExcelDOImporter {
        
        private final List<DataObject> results = new ArrayList<>();
        
		public TestedAbstractExcelDOImporter(File importSource, String aSheetName, boolean doDeleteWhenDone) {
			this(BinaryDataFactory.createBinaryData(importSource), aSheetName, doDeleteWhenDone);
		}

		public TestedAbstractExcelDOImporter(BinaryData importSource, String aSheetName, boolean doDeleteWhenDone) {
            super(importSource, aSheetName, doDeleteWhenDone);
        }
        
        @Override
        protected String[] getExpectedColumnNames() {
            return new String[] {"Spalte A", "Spalte B", "Spalte C"};
        }
        
        @Override
        protected void importItem(DataObject aDo, Mandator aMandator) throws Exception {
            this.results.add(aDo);
        }
    }
}
