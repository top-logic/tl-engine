/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.POIUtil;

/**
 * Test case for {@linkplain POIUtil}.
 * 
 * @author    <a href=mailto:cdo@top-logic.com>cdo</a>
 */
public class TestPOIUtil extends BasicTestCase {
    
    public TestPOIUtil(String aName) {
        super(aName);
    }

    public void testExtractSheetName() {
        assertEquals("Sheet1", POIUtil.extractSheetName("Sheet1!A2"));
        assertNull(POIUtil.extractSheetName("A2"));
    }
    
    public void testExtractCellName() {
        assertEquals("A2", POIUtil.extractCellName("Sheet1!A2"));
        assertEquals("A2", POIUtil.extractCellName("A2"));
    }
    
    public void testConvertCellName() {
        int[] theCellInfo = POIUtil.convertCellName("A2");
        assertEquals(1, theCellInfo[0]);
        assertEquals(0, theCellInfo[1]);
        
        theCellInfo = POIUtil.convertCellName("F5");
        assertEquals(4, theCellInfo[0]);
        assertEquals(5, theCellInfo[1]);

        theCellInfo = POIUtil.convertCellName("ZZ999");
        assertEquals(998, theCellInfo[0]);
        assertEquals(701, theCellInfo[1]);

        startTime();
        for (int i=1; i < 99999; i++) {
            theCellInfo = POIUtil.convertCellName("ZZ" + i );
            assertEquals(i - 1, theCellInfo[0]);
            assertEquals(701  , theCellInfo[1]);
        }
        logTime("convertCellName");
    }
    
    public void testConvertToCellName() {
        assertEquals("B3", POIUtil.convertToCellName(null, 2, 1));
        assertEquals("Sheet1!D4", POIUtil.convertToCellName("Sheet1", 3, 3));
    }
    
    public static Test suite () {
        // SHOW_TIME = true;
        // return new TestPOIUtil("testConvertCellName");
        return TLTestSetup.createTLTestSetup(TestPOIUtil.class);
    }

}
