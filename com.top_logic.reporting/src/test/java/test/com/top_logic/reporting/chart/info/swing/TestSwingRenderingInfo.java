/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.chart.info.swing.SwingRenderingInfo;
import com.top_logic.reporting.chart.renderer.SwingRenderer;

/**
 * The TestSwingRenderingInfo tests the class 
 * {@link com.top_logic.reporting.chart.info.swing.SwingRenderingInfo}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestSwingRenderingInfo extends BasicTestCase{

    /**
     * This method tests the accessor methods.
     */
    public void testGetterAndSetter() {
        SwingRenderingInfo info = new SwingRenderingInfo();
        
        info.setColor(Color.PINK);
        info.setDrawLineToPrev(true);
        info.setIconOrientation(SwingRenderer.LEFT);
        info.setNormalBar(true);
        info.setShowAsIcon(true);
        info.setValue(5.0);
        info.setValueAxisIndex(1);
        info.setVisible(true);
        info.setOverwritePrev(true);
        assertSame  (Color.PINK, info.getColor());
        assertTrue  (info.isDrawLineToPrev());
        assertEquals(SwingRenderer.LEFT, info.getIconOrientation());
        assertTrue  (info.isNormalBar());
        assertTrue  (info.isShowAsIcon());
        assertEquals(5.0, info.getValue(), EPSILON);
        assertEquals(1, info.getValueAxisIndex());
        assertTrue  (info.isVisible());
        assertTrue  (info.isOverwritePrev());
        
        info.setColor(Color.BLUE);
        info.setDrawLineToPrev(false);
        info.setIconOrientation(SwingRenderer.RIGHT);
        info.setNormalBar(false);
        info.setShowAsIcon(false);
        info.setValue(0.0);
        info.setValueAxisIndex(0);
        info.setVisible(false);
        info.setOverwritePrev(false);
        assertSame  (Color.BLUE, info.getColor());
        assertFalse (info.isDrawLineToPrev());
        assertEquals(SwingRenderer.RIGHT, info.getIconOrientation());
        assertFalse (info.isNormalBar());
        assertFalse (info.isShowAsIcon());
        assertEquals(0.0, info.getValue(), EPSILON);
        assertEquals(0, info.getValueAxisIndex());
        assertFalse (info.isVisible());
        assertFalse (info.isOverwritePrev());
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestSwingRenderingInfo.class);
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

