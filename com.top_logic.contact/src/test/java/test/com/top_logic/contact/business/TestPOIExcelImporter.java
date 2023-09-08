/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.contact.business;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.contact.business.POIExcelImporter;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestPOIExcelImporter extends BasicTestCase {

    private static final String EXCEL_NULL = "excelNull";
    
    public void testRun() throws Exception {
		BinaryData data = BinaryDataFactory.createBinaryData(TestedPOIExcelImporter.class, "TestPOIExcelImporter.xls");
		TestedPOIExcelImporter importer = new TestedPOIExcelImporter(data);
        importer.run();
        
        assertNotNull(importer.importedSheetNames);
        assertNotNull(importer.results);
        
        // import tree tables
        assertEquals(3, importer.importedSheetNames.size());
        assertEquals("Tabelle1", importer.importedSheetNames.get(0));
        assertEquals("Tabelle2", importer.importedSheetNames.get(1));
        assertNull(importer.importedSheetNames.get(2));
        assertEquals("Tabelle4", importer.importedSheetNames.get(3));
        
        assertEquals(3, importer.results.size());
        assertNotNull(importer.results.get(0));
        assertNotNull(importer.results.get(1));
        assertNull(importer.results.get(2));
        assertNotNull(importer.results.get(3));
        
        // simple table
        Map<Integer, Map<Integer,String>> sheet = importer.results.get(0);
        assertEquals(6, sheet.size());
        for (int iRow=0; iRow<5; iRow++) {
            Map<Integer, String> row = sheet.get(iRow);
            assertNotNull(row);
            for (int iCol=0; iCol<3; iCol++) {
                String value = "Tabelle1" + (char)(65+iCol) + "" + (iRow+1);
                assertEquals(value, row.get(iCol));
            }
        }
        
        // table with empty trailing cells
        sheet = importer.results.get(1);
        assertEquals(8, sheet.size());
        for (int iRow=0; iRow<8; iRow++) {
            Map<Integer, String> row = sheet.get(iRow);
            assertNotNull(row);
            for (int iCol=0; iCol<3; iCol++) {
                String actual = row.get(iCol); 
                String value = EXCEL_NULL;
                // This is a very strange behavior of POI, 
                // Don't know how row.getFirstCellNum() and row.getLastCellNum() are calculated
                if (iRow == 0 && iCol == 0) {
                    assertEquals(null, actual);
                }
                else if (iRow == 0 && iCol == 1) {
                    assertEquals(null, actual);
                }
                else if (iRow == 1 && iCol == 0) {
                    assertEquals(null, actual);
                }
                else if (iRow == 6 && iCol == 2) {
                    assertEquals(value, actual);
                }
                else if (iRow == 7 && iCol == 1) {
                    assertEquals(value, actual);
                }
                else if (iRow == 7 && iCol == 2) {
                    assertEquals(null, actual);
                }
                else {
                    value = "Tabelle2" + (char)(65+iCol) + "" + (iRow+1);
                    assertEquals(value, actual);
                }
            }
        }
        
        // table empty rows
        sheet = importer.results.get(3);
        assertEquals(6, sheet.size());
        for (int iRow=0; iRow<9; iRow++) {
            Map<Integer, String> row = sheet.get(iRow);
            
            if (iRow == 3 || iRow==4 || iRow ==7) {
                assertNull(row);
                continue;
            }
            
            assertNotNull(row);
            assertEquals("Tabelle4A" + (iRow+1), row.get(0)); 
        }
        
    }
    
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPOIExcelImporter.class));
    }
    
    private class TestedPOIExcelImporter extends POIExcelImporter {
        
        private final Map<Integer, String> importedSheetNames;
        private final Map<Integer, Map<Integer, Map<Integer, String>>> results;
        
		public TestedPOIExcelImporter(BinaryData importSource) {
			super(importSource);
            importedSheetNames = new HashMap<>();
            results = new HashMap<>();
        }
        
        @Override
        protected boolean checkColumnFormat(int aSheetNum, String aSheetname, Row aRow) throws IOException {
            return false;
        }
        
        @Override
        protected boolean shoudImportSheet(int numSheet, String sheetName) {
            return numSheet != 2; 
        }
        
        @Override
        protected void importRow(int aSheetNum, String aSheetname, Row aRow) throws Exception {
            
            this.addSheetName(aSheetNum, aSheetname);
            
            if (aRow == null) {
                return;
            }
            
            int   iRow      = aRow.getRowNum();
            short firstCell = aRow.getFirstCellNum();
            short lastCell  = aRow.getLastCellNum();
            
            for (int iCol=firstCell; iCol<=lastCell; iCol++) {
                this.addResult(aSheetNum, iRow, iCol, getString(aRow.getCell(iCol)));
            }
        }
        
        private void addSheetName(int aSheetNum, String aSheetName) {
            this.importedSheetNames.put(aSheetNum, aSheetName);
        }
        
        private void addResult(int aSheetNum, int iRow, int iCol, String value) {
            Map<Integer, Map<Integer, String>> sheet = results.get(aSheetNum);
            if (sheet == null) {
                sheet = new HashMap<>();
                results.put(aSheetNum, sheet);
            }
            
            Map<Integer, String> row = sheet.get(iRow);
            if (row == null) {
                row = new HashMap<>();
                sheet.put(iRow, row);
            }
            
            row.put(iCol, value);
        }
        
		private String getString(Cell aCell) {
            if (aCell == null) {
                return EXCEL_NULL;
            }
			RichTextString theText = aCell.getRichStringCellValue();
            return theText != null ? StringServices.checkOnNullAndTrim(theText.getString()) : StringServices.EMPTY_STRING;
        }
    }
    
}
