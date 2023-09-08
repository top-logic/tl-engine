/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart.info;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.common.tree.TreeInfo;

/**
 * The TestTreeInfo test the class {@link com.top_logic.reporting.common.tree.TreeInfo}.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class TestTreeInfo extends TestCase {

    /** 
     * This method tests the accessor methods.
     */
    public void testAccessorMethods() {
        TreeInfo info = new TreeInfo("Specification", 0, "iconTooltip", "iconUrl", "labelTooltip", "labelUrl");
                 info.setDepth (3);
                 info.setParent(1);
        
        assertEquals("Specification", info.getLabel());          
        assertEquals(0,               info.getIconIndex());   
        assertEquals("iconTooltip",   info.getIconTooltip());
        assertEquals("iconUrl",       info.getIconUrl());
        assertEquals("labelTooltip",  info.getLabelTooltip());
        assertEquals("labelUrl",      info.getLabelUrl());
        assertEquals(3,               info.getDepth());
        assertEquals(1,               info.getParent());
        
        info.setLabel       ("Specification 2");
        info.setIconIndex   (1);
        info.setIconTooltip ("iconTooltip 2");
        info.setIconUrl     ("iconUrl 2");
        info.setLabelTooltip("labelTooltip 2");
        info.setLabelUrl    ("labelUrl 2");
        
        assertEquals("Specification 2", info.getLabel());          
        assertEquals(1,                 info.getIconIndex());   
        assertEquals("iconTooltip 2",   info.getIconTooltip());
        assertEquals("iconUrl 2",       info.getIconUrl());
        assertEquals("labelTooltip 2",  info.getLabelTooltip());
        assertEquals("labelUrl 2",      info.getLabelUrl());
    }
    
    /** 
     * This method returns the test in a suite.
     * 
     * @return Returns the test in a suite.
     */
    public static Test suite () {
        return new TestSuite(TestTreeInfo.class);
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

