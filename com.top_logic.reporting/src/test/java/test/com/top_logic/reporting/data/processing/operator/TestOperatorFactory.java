/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.processing.operator;

import junit.framework.Test;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.processing.operator.Operator;
import com.top_logic.reporting.data.processing.operator.OperatorFactory;

/** 
 * Test the {@link OperatorFactory}.
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestOperatorFactory extends BasicTestCase {

    /**
     * Create TestOperatorFactory for given test-name
     */
    public TestOperatorFactory(String name) {
        super(name);
    }

    /**
     * Test for Operator[] getOperators(Class)
     */
    public void testGetOperatorsClass() {
        final OperatorFactory theOF = OperatorFactory.getInstance();
        Operator[] theOps = theOF.getOperators(NumericValue.class);

        assertNotNull("Unable to get operators for NumericValue!", theOps);

        assertTrue("Unable to find operators for NumericValue!",
                        theOps.length > 0);

        theOps = theOF.getOperators(TestOperatorFactory.class);

        assertNotNull("Unable to get operators for TestOperatorFactory!", 
                           theOps);

        assertTrue("Found operators for TestOperatorFactory!",
                        theOps.length == 0);
    }

    public void testReloadable() {
        OperatorFactory theFactory = OperatorFactory.getInstance();
        String          theName    = theFactory.getName();
        
        assertNotNull("Name of factory is null!", theName);
        assertTrue("Name of factory is empty!", theName.length() > 0);

        theName = theFactory.getDescription();

        assertNotNull("Description of factory is null!", theName);
        assertTrue("Description of factory is empty!", theName.length() > 0);

        assertTrue("Factory didn't use XMLProperties!", 
                        theFactory.usesXMLProperties());

        assertTrue(theFactory.reload());
    }

    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestOperatorFactory.class);
    }
    
    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        // SHOW_TIME        = false;     // for debugging
    
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }
}
