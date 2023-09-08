/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.info.swing;

import java.awt.Color;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.chart.info.swing.NegativeBarInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarIconInfo;
import com.top_logic.reporting.chart.info.swing.NormalBarInfo;
import com.top_logic.reporting.chart.info.swing.OverwriteBarInfo;
import com.top_logic.reporting.chart.info.swing.PositiveBarInfo;
import com.top_logic.reporting.chart.info.swing.SpecialBarInfo;
import com.top_logic.reporting.chart.renderer.SwingRenderer;

/**
 * The TestInfos tests all swing infos.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestInfos extends TestCase {

    /** 
     * This method tests the 
     * {@link com.top_logic.reporting.chart.info.swing.NegativeBarInfo}.
     */
    public void testNegativeBarInfo() {
        NegativeBarInfo infoA = new NegativeBarInfo(-5.0);
        assertSame  (infoA.getColor(), Color.GREEN);
        assertTrue  (infoA.isDrawLineToPrev());
        assertFalse (infoA.isNormalBar());
        assertFalse (infoA.isShowAsIcon());
        assertEquals(infoA.getValue(), -5.0, BasicTestCase.EPSILON);
        assertEquals(infoA.getValueAxisIndex(), 0);
        assertTrue  (infoA.isVisible());
        assertFalse (infoA.isOverwritePrev());
        
        NegativeBarInfo infoB = new NegativeBarInfo(-1.0, 1);
        assertSame  (infoB.getColor(), Color.GREEN);
        assertTrue  (infoB.isDrawLineToPrev());
        assertFalse (infoB.isNormalBar());
        assertFalse (infoB.isShowAsIcon());
        assertEquals(infoB.getValue(), -1.0, BasicTestCase.EPSILON);
        assertEquals(infoB.getValueAxisIndex(), 1);
        assertTrue  (infoB.isVisible());
        assertFalse (infoB.isOverwritePrev());
    }
    
    /** 
     * This method tests 
     * {@link com.top_logic.reporting.chart.info.swing.NormalBarIconInfo}.
     */
    public void testNormalBarIconInfo() {
        NormalBarIconInfo infoA = new NormalBarIconInfo(2.0,    SwingRenderer.RIGHT);
        assertSame  (infoA.getColor(), Color.LIGHT_GRAY);
        assertTrue  (infoA.isDrawLineToPrev());
        assertTrue  (infoA.isNormalBar());
        assertTrue  (infoA.isShowAsIcon());
        assertEquals(infoA.getValue(), 2.0, BasicTestCase.EPSILON);
        assertEquals(infoA.getValueAxisIndex(), 0);
        assertTrue  (infoA.isVisible());
        assertFalse (infoA.isOverwritePrev());
        assertEquals(infoA.getIconOrientation(), SwingRenderer.RIGHT);
        
        NormalBarIconInfo infoB = new NormalBarIconInfo(1.0, 1, SwingRenderer.LEFT);
        assertSame  (infoB.getColor(), Color.LIGHT_GRAY);
        assertTrue  (infoB.isDrawLineToPrev());
        assertTrue  (infoB.isNormalBar());
        assertTrue  (infoB.isShowAsIcon());
        assertEquals(infoB.getValue(), 1.0, BasicTestCase.EPSILON);
        assertEquals(infoB.getValueAxisIndex(), 1);
        assertTrue  (infoB.isVisible());
        assertFalse (infoB.isOverwritePrev());
        assertEquals(infoB.getIconOrientation(), SwingRenderer.LEFT);
    }
    
    /** 
     * This method tests {@link com.top_logic.reporting.chart.info.swing.NormalBarInfo}.
     */
    public void testNormalBarInfo() {
        NormalBarInfo infoA = new NormalBarInfo(1.0);
        assertSame  (infoA.getColor(), Color.LIGHT_GRAY);
        assertTrue  (infoA.isDrawLineToPrev());
        assertTrue  (infoA.isNormalBar());
        assertFalse (infoA.isShowAsIcon());
        assertEquals(infoA.getValue(), 1.0, BasicTestCase.EPSILON);
        assertEquals(infoA.getValueAxisIndex(), 0);
        assertTrue  (infoA.isVisible());
        assertFalse (infoA.isOverwritePrev());

        NormalBarInfo infoB = new NormalBarInfo(2.0, true);
        assertSame  (infoB.getColor(), Color.LIGHT_GRAY);
        assertTrue  (infoB.isDrawLineToPrev());
        assertTrue  (infoB.isNormalBar());
        assertFalse (infoB.isShowAsIcon());
        assertEquals(infoB.getValue(), 2.0, BasicTestCase.EPSILON);
        assertEquals(infoB.getValueAxisIndex(), 0);
        assertTrue  (infoB.isVisible());
        assertFalse (infoB.isOverwritePrev());
        
        NormalBarInfo infoC = new NormalBarInfo(3.0, 1);
        assertSame  (infoC.getColor(), Color.LIGHT_GRAY);
        assertTrue  (infoC.isDrawLineToPrev());
        assertTrue  (infoC.isNormalBar());
        assertFalse (infoC.isShowAsIcon());
        assertEquals(infoC.getValue(), 3.0, BasicTestCase.EPSILON);
        assertEquals(infoC.getValueAxisIndex(), 1);
        assertTrue  (infoC.isVisible());
        assertFalse (infoC.isOverwritePrev());
    }
    
    /** 
     * This method tests {@link com.top_logic.reporting.chart.info.swing.PositiveBarInfo}.
     */
    public void testPositiveBarInfo() {
        PositiveBarInfo infoA = new PositiveBarInfo(1.0);
        assertSame  (infoA.getColor(), Color.RED);
        assertTrue  (infoA.isDrawLineToPrev());
        assertFalse (infoA.isNormalBar());
        assertFalse (infoA.isShowAsIcon());
        assertEquals(infoA.getValue(), 1.0, BasicTestCase.EPSILON);
        assertEquals(infoA.getValueAxisIndex(), 0);
        assertTrue  (infoA.isVisible());
        assertFalse (infoA.isOverwritePrev());
        
        PositiveBarInfo infoB = new PositiveBarInfo(2.0, 1);
        assertSame  (infoB.getColor(), Color.RED);
        assertTrue  (infoB.isDrawLineToPrev());
        assertFalse (infoB.isNormalBar());
        assertFalse (infoB.isShowAsIcon());
        assertEquals(infoB.getValue(), 2.0, BasicTestCase.EPSILON);
        assertEquals(infoB.getValueAxisIndex(), 1);
        assertTrue  (infoB.isVisible());
        assertFalse (infoB.isOverwritePrev());
    }
    
    /** 
     * This method tests {@link com.top_logic.reporting.chart.info.swing.SpecialBarInfo}.
     */
    public void testSpecialBarInfo() {
        SpecialBarInfo info = new SpecialBarInfo(2.0, Color.MAGENTA);
        assertSame  (info.getColor(), Color.MAGENTA);
        assertTrue  (info.isDrawLineToPrev());
        assertFalse (info.isNormalBar());
        assertFalse (info.isShowAsIcon());
        assertEquals(info.getValue(), 2.0, BasicTestCase.EPSILON);
        assertEquals(info.getValueAxisIndex(), 0);
        assertTrue  (info.isVisible());
        assertFalse (info.isOverwritePrev());
    }
    
    /** 
     * This method tests 
     * {@link com.top_logic.reporting.chart.info.swing.OverwriteBarInfo}.
     */
    public void testOverwriteInfo() {
        OverwriteBarInfo infoA = new OverwriteBarInfo(1.0, Color.CYAN);
        assertSame  (infoA.getColor(), Color.CYAN);
        assertTrue  (infoA.isDrawLineToPrev());
        assertFalse (infoA.isNormalBar());
        assertFalse (infoA.isShowAsIcon());
        assertEquals(infoA.getValue(), 1.0, BasicTestCase.EPSILON);
        assertEquals(infoA.getValueAxisIndex(), 0);
        assertTrue  (infoA.isVisible());
        assertTrue  (infoA.isOverwritePrev());
        
        OverwriteBarInfo infoB = new OverwriteBarInfo(1.0, 1, Color.CYAN);
        assertSame  (infoB.getColor(), Color.CYAN);
        assertTrue  (infoB.isDrawLineToPrev());
        assertFalse (infoB.isNormalBar());
        assertFalse (infoB.isShowAsIcon());
        assertEquals(infoB.getValue(), 1.0, BasicTestCase.EPSILON);
        assertEquals(infoB.getValueAxisIndex(), 1);
        assertTrue  (infoB.isVisible());
        assertTrue  (infoB.isOverwritePrev());
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestInfos.class);
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

