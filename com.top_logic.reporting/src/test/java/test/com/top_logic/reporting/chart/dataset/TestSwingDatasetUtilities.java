/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.dataset;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.data.Range;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.dataset.SwingDatasetUtilities;
import com.top_logic.reporting.chart.info.swing.NegativeBarInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;
import com.top_logic.reporting.chart.info.swing.PositiveBarInfo;

/**
 * The TestSwingDatasetUtilities tests the class 
 * {@link com.top_logic.reporting.chart.dataset.SwingDatasetUtilities}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestSwingDatasetUtilities extends TestCase {
    
    /**
     * This method tests the
     * {@link com.top_logic.reporting.chart.dataset.SwingDatasetUtilities#getKey(int, int)}.
     */
    public void testGetKey() {
        assertEquals("5,7", SwingDatasetUtilities.getKey(5, 7));
    }
    
    /**
     * This method tests the
     * {@link SwingDatasetUtilities#findRange(SwingDataset, int, double, double)}.
     */
    public void testFindRange() {
        SwingDataset dataset = new SwingDataset();
        int i = 0; /* Unique category name which are not displayed. */
        dataset.setValue( 1.0, "0", getSpaces(++i), new NormalBarInfo  (1, 0));
        dataset.setValue( 2.0, "0", getSpaces(++i), new PositiveBarInfo(1, 0));
        dataset.setValue( 3.0, "0", "HKprop",       new PositiveBarInfo(1, 0));
        dataset.setValue( 4.0, "1", "HKprop",       new PositiveBarInfo(1, 0));
        dataset.setValue(-5.0, "0", getSpaces(++i), new NegativeBarInfo(1, 0));
        dataset.setValue(-6.0, "1", getSpaces(  i), new NegativeBarInfo(1, 0));
        dataset.setValue(-7.0, "0", getSpaces(++i), new NormalBarInfo  (1, 0));
        Range range = SwingDatasetUtilities.findRange(dataset, 0, 0, 0);
        assertEquals(10.0, range.getUpperBound(), BasicTestCase.EPSILON);
		assertEquals(-8.0, range.getLowerBound(), BasicTestCase.EPSILON);
        
        dataset.setValue( 1.0, "0", getSpaces(++i), new NormalBarInfo  (1, 1));
        dataset.setValue( 1.0, "0", getSpaces(++i), new NegativeBarInfo(1, 1));
        dataset.setValue( 1.0, "0", "Invest",       new PositiveBarInfo(1, 1));
        dataset.setValue( 1.0, "1", "Invest",       new PositiveBarInfo(1, 1));
        dataset.setValue(-1.0, "0", getSpaces(++i), new NegativeBarInfo(1, 1));
        dataset.setValue(-1.0, "1", getSpaces(  i), new NegativeBarInfo(1, 1));
        dataset.setValue( 7.0, "0", getSpaces(++i), new NormalBarInfo  (1, 1));
        range = SwingDatasetUtilities.findRange(dataset, 1, 0, 0);
        assertEquals( 7.0, range.getUpperBound(), BasicTestCase.EPSILON);
		assertEquals(-1.0, range.getLowerBound(), BasicTestCase.EPSILON);
        
        dataset.setValue(  2.0, "0", getSpaces(++i), new NormalBarInfo  (1, 2));
        dataset.setValue(  4.0, "0", getSpaces(++i), new PositiveBarInfo(1, 2));
        dataset.setValue(  4.0, "0", "Entwicklung" , new PositiveBarInfo(1, 2));
        dataset.setValue(  0.0, "1", "Entwicklung",  new PositiveBarInfo(1, 2));
        dataset.setValue(-10.0, "0", getSpaces(++i), new NegativeBarInfo(1, 2));
        dataset.setValue(-10.0, "1", getSpaces(  i), new NegativeBarInfo(1, 2));
        dataset.setValue(  0.0, "0", getSpaces(++i), new NormalBarInfo  (1, 2));
        range = SwingDatasetUtilities.findRange(dataset, 2, 0.2, 0.1);
		assertEquals(12.6, range.getUpperBound(), BasicTestCase.EPSILON);
		assertEquals(-21.2, range.getLowerBound(), BasicTestCase.EPSILON);
    }
    
    /**
     * This method tests the
     * {@link SwingDatasetUtilities#synchronizeYAxes(SwingDataset, int)}.
     */
    public void testSynchronizeYAxes() {
        SwingDataset dataset = new SwingDataset();
        int i = 0; /* Unique category name which are not displayed. */
        dataset.setValue( 1.0, "0", getSpaces(++i), new NormalBarInfo  (1, 0));
        dataset.setValue( 2.0, "0", getSpaces(++i), new PositiveBarInfo(1, 0));
        dataset.setValue( 3.0, "0", "HKprop",       new PositiveBarInfo(1, 0));
        dataset.setValue( 4.0, "1", "HKprop",       new PositiveBarInfo(1, 0));
        dataset.setValue(-5.0, "0", getSpaces(++i), new NegativeBarInfo(1, 0));
        dataset.setValue(-6.0, "1", getSpaces(  i), new NegativeBarInfo(1, 0));
        dataset.setValue(-7.0, "0", getSpaces(++i), new NormalBarInfo  (1, 0));
        
        dataset.setValue( 1.0, "0", getSpaces(++i), new NormalBarInfo  (1, 1));
        dataset.setValue( 1.0, "0", getSpaces(++i), new NegativeBarInfo(1, 1));
        dataset.setValue( 1.0, "0", "Invest",       new PositiveBarInfo(1, 1));
        dataset.setValue( 1.0, "1", "Invest",       new PositiveBarInfo(1, 1));
        dataset.setValue(-1.0, "0", getSpaces(++i), new NegativeBarInfo(1, 1));
        dataset.setValue(-1.0, "1", getSpaces(  i), new NegativeBarInfo(1, 1));
        dataset.setValue( 7.0, "0", getSpaces(++i), new NormalBarInfo  (1, 1));
        
        dataset.setValue(  2.0, "0", getSpaces(++i), new NormalBarInfo  (1, 2));
        dataset.setValue(  4.0, "0", getSpaces(++i), new PositiveBarInfo(1, 2));
        dataset.setValue(  4.0, "0", "Entwicklung" , new PositiveBarInfo(1, 2));
        dataset.setValue(  0.0, "1", "Entwicklung",  new PositiveBarInfo(1, 2));
        dataset.setValue(-10.0, "0", getSpaces(++i), new NegativeBarInfo(1, 2));
        dataset.setValue(-10.0, "1", getSpaces(  i), new NegativeBarInfo(1, 2));
        dataset.setValue(  0.0, "0", getSpaces(++i), new NormalBarInfo  (1, 2));

        SwingDatasetUtilities.synchronizeYAxes(dataset, 3);
        Range range = SwingDatasetUtilities.findRange(dataset, 0, 0, 0);
        assertEquals( 10.0, range.getUpperBound(), BasicTestCase.EPSILON);
        assertEquals(-10.0, range.getLowerBound(), BasicTestCase.EPSILON);

        range = SwingDatasetUtilities.findRange(dataset, 1, 0, 0);
        assertEquals( 7.0, range.getUpperBound(), BasicTestCase.EPSILON);
        assertEquals(-7.0, range.getLowerBound(), BasicTestCase.EPSILON);

        range = SwingDatasetUtilities.findRange(dataset, 2, 0.2, 0.1);
		assertEquals(19.2, range.getUpperBound(), BasicTestCase.EPSILON);
		assertEquals(-22.4, range.getLowerBound(), BasicTestCase.EPSILON);
    }
    
    /**
     * This method returns a string with the given count on spaces.
     * 
     * @param aCount A count.
     * @return Returns a string with the given count on spaces.
     */
    private static String getSpaces(int aCount) {
        StringBuffer buffer = new StringBuffer(aCount);
        for (int i = 0; i < aCount; i++) {
            buffer.append(' ');
        }
        return buffer.toString();
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestSwingDatasetUtilities.class);
    }
    
    /**
     * This method can be used for direct testing.
     * 
     * @param args A string array of argument.
     */
    public static void main (String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite());
    }
    
}

