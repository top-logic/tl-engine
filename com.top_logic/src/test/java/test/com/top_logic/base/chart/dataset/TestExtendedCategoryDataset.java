/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.chart.dataset;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;

/**
 * The TestExtendedCategoryDataset tests the class {@link ExtendedCategoryDataset}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestExtendedCategoryDataset extends BasicTestCase {

    private static final Integer SERIES_0   = Integer.valueOf(0);
    private static final Integer SERIES_1   = Integer.valueOf(1);
    private static final String  CATEGORY_A = "Category A";
    private static final String  CATEGORY_B = "Category B";
    
    /**
     * This method creates a {@link ExtendedCategoryDataset} and stores values
     * and objects for two series and two categories. The test checks if the
     * stored values and objects are correctly returned.
     */
    public void testDataset() {
        ExtendedCategoryDataset theDataset = new ExtendedCategoryDataset();
        assertNotNull(theDataset);
        
        String theTestStr1 = "Heinz";
        String theTestStr2 = "Otto";
        String theTestStr3 = "Karl";
        String theTestStr4 = "Willi";
        theDataset.addValue(1, SERIES_0, CATEGORY_A, theTestStr1);
        theDataset.addValue(2, SERIES_0, CATEGORY_B, theTestStr2);
        theDataset.addValue(3, SERIES_1, CATEGORY_A, theTestStr3);
        theDataset.addValue(4, SERIES_1, CATEGORY_B, theTestStr4);
        
        assertEquals(1, theDataset.getValue(SERIES_0, CATEGORY_A).intValue());
        assertEquals(2, theDataset.getValue(SERIES_0, CATEGORY_B).intValue());
        assertEquals(3, theDataset.getValue(SERIES_1, CATEGORY_A).intValue());
        assertEquals(4, theDataset.getValue(SERIES_1, CATEGORY_B).intValue());
        
        assertEquals(theTestStr1, theDataset.getObject(SERIES_0, CATEGORY_A));
        assertEquals(theTestStr2, theDataset.getObject(SERIES_0, CATEGORY_B));
        assertEquals(theTestStr3, theDataset.getObject(SERIES_1, CATEGORY_A));
        assertEquals(theTestStr4, theDataset.getObject(SERIES_1, CATEGORY_B));
        
        assertEquals(theTestStr1, theDataset.getObject(0, 0));
        assertEquals(theTestStr2, theDataset.getObject(0, 1));
        assertEquals(theTestStr3, theDataset.getObject(1, 0));
        assertEquals(theTestStr4, theDataset.getObject(1, 1));
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestExtendedCategoryDataset.class));
    }
    
    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }

}

