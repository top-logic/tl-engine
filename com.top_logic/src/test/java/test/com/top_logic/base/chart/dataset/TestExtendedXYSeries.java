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

import com.top_logic.base.chart.dataset.ExtendedXYSeries;

/**
 * The TestExtendedXYSeries tests the class {@link ExtendedXYSeries}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestExtendedXYSeries extends BasicTestCase {

    /**
     * This method tests the {@link ExtendedXYSeries}.
     */
    public void testExtendedXYSeries() {
        ExtendedXYSeries theSeries = new ExtendedXYSeries("Test");
        String           theStr1   = "test string 5";
        String           theStr2   = "test string 1";
        String           theStr3   = "test string 3";
        String           theStr4   = "test string 2";
        String           theStr5   = "test string 4";
        
        theSeries.add(0, 0, theStr1); 
        theSeries.add(0, 0, theStr2);
        theSeries.add(0, 0, theStr3);
        theSeries.add(0, 0, theStr4);
        theSeries.add(0, 0, theStr5);
        
        assertEquals(theStr1, theSeries.getObject(0));
        assertEquals(theStr2, theSeries.getObject(1));
        assertEquals(theStr3, theSeries.getObject(2));
        assertEquals(theStr4, theSeries.getObject(3));
        assertEquals(theStr5, theSeries.getObject(4));
        
        
        Number theX = Double.valueOf(5);
        Number theY = Double.valueOf(6);
        theSeries.add(theX, theY, null);
        
        assertEquals(theX, theSeries.getX(5));
        assertEquals(theY, theSeries.getY(5));
    }
    
    /** 
     * This method returns the test suite.
     */
    public static Test suite() {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestExtendedXYSeries.class));
    }

    /**
     * The main function can be used for direct testing.
     */
    public static void main(String[] args) {
        // Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
    
}

