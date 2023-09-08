/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.report.model.aggregation;

import java.util.Set;

import junit.framework.Test;
import junit.textui.TestRunner;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.reporting.report.model.aggregation.AggregationFunctionConfiguration;
import com.top_logic.reporting.report.model.aggregation.AggregationFunctionFactory;

/**
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestAggregationFunctionFactory extends BasicTestCase {

    /**
     * Test Main fucntion of factory.
     */
    public void testGetFunctionList() throws Exception {
        AggregationFunctionFactory aff = AggregationFunctionFactory.getInstance();
        Set<Class> functions   = aff.getImplementationClasses(null);
        String  thePath     = "TestAggregationFunctionFactory";
        for (Class theClass : functions) {
            AggregationFunctionConfiguration theConf = aff.createAggregationFunctionConfiguration(theClass);
            theConf.setAttributePath(thePath);
            
            assertNotNull(aff.getAggregationFunction(theConf));
        }
    }

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestAggregationFunctionFactory.class);
    }

    public static void main(String[] args) {
        TestRunner theRunner = new TestRunner();
        theRunner.doRun(suite());
    }

}

