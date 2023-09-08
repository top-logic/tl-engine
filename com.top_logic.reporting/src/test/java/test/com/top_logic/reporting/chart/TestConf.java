/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.chart;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.reporting.report.model.ReportConfiguration;


/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestConf extends BasicTestCase {

    /** 
     * Creates a {@link TestConf}.
     */
    public TestConf(String name) {
        super(name);
    }

    public void testConfig() throws Exception {
        
        ReportConfiguration     theConf = TypedConfiguration.newConfigItem(ReportConfiguration.class);
        
        theConf.setChartType("123");
//        theConf.setPartitionConfiguration(theConf);
        
        assertEquals("123", theConf.getChartType());
//        assertEquals(1, theConf.getPartitionConfiguration());
        
        
    }
    
    public static Test suite() {
    	return ReportingSetup.createReportingSetup(TestConf.class);
    }
    
}

