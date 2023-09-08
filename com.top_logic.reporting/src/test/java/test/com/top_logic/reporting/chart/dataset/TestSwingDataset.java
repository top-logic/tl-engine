/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.dataset;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.dataset.SwingDatasetUtilities;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;

/**
 * The TestSwingDataset tests the class 
 * {@link com.top_logic.reporting.chart.dataset.SwingDataset}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestSwingDataset extends TestCase {

    /**
     * This method tests the {@link com.top_logic.reporting.chart.dataset.SwingDataset}.
     */
    public void testSwingDataset() {
        SwingDataset dataset = new SwingDataset();
        assertNotNull(dataset);
        
        NormalBarInfo infoA = new NormalBarInfo(2.0);
        dataset.setValue(1.0, "0", "A", infoA);
        assertEquals(1.0, getDouble(dataset.getValue(0, 0)), BasicTestCase.EPSILON);
        assertSame(infoA, dataset.getRenderingInfo(0, 0));
        assertSame(infoA, dataset.getRenderingInfo(SwingDatasetUtilities.getKey(0, 0)));
        
        NormalBarInfo infoB = new NormalBarInfo(6.0);
        dataset.addValue(5.0, "0", "B", infoB);
        assertEquals(5.0, getDouble(dataset.getValue(0, 1)), BasicTestCase.EPSILON);
        assertSame(infoB, dataset.getRenderingInfo(0, 1));
        assertSame(infoB, dataset.getRenderingInfo(SwingDatasetUtilities.getKey(0, 1)));
        
        assertNull(dataset.getRenderingInfo(0, 100));
        dataset.setValue(8.0, "0", "C", null);
        assertNull(dataset.getRenderingInfo(0, 2));
    }
    
    /**
     * This method returns a double.
     * 
     * @param aNumber A Double.
     * @return returns a double.
     */
    private double getDouble(Number aNumber) {
        return ((Double)aNumber).doubleValue();
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestSwingDataset.class);
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

