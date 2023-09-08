/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.chart.dataset.SwingDataset;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;
import com.top_logic.reporting.chart.util.SwingToolTipGenerator;

/**
 * The TestSwingToolTipGenerator test the class 
 * {@link com.top_logic.reporting.chart.util.SwingToolTipGenerator}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestSwingToolTipGenerator extends TestCase {

    /** 
     * This method tests the tooltips.
     */
    public void testTooltip() {
        SwingDataset       dataset      = new SwingDataset();
        double             tooltipValue = 500.123;
        SwingRenderingInfo info         = new NormalBarInfo(tooltipValue);
        
        dataset.setValue(1, "0", "A", info);
        String tooltipResult = new SwingToolTipGenerator().generateToolTip(dataset, 0, 0);
        assertEquals(String.valueOf(tooltipValue), tooltipResult);
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestSwingToolTipGenerator.class);
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

