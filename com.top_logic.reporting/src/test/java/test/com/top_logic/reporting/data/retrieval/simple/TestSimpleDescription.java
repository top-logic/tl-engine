/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.retrieval.simple;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.reporting.ReportingSetup;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.description.simple.SimpleDescription;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestSimpleDescription extends BasicTestCase {

    /**
     * Constructor for TestSimpleDescription.
     */
    public TestSimpleDescription(String name) {
        super(name);
    }

    public void testGetType() {
        SimpleDescription theDesc  = new SimpleDescription("Test 1", String.class);
        Class             theClass = theDesc.getType();

        assertNotNull("Type is null!", theClass);
        assertEquals("Type is not the expected one!",
                          String.class, theClass);

        try {
            theDesc = new SimpleDescription("Test 1", null);
            fail("Constructor with type null must fail!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testGetName() {
        SimpleDescription theDesc  = new SimpleDescription("Test 1", String.class);
        String            theName  = theDesc.getName();

        assertNotNull("Name is null!", theName);
        assertEquals("Name is not the expected one!",
                          "Test 1", theName);

        try {
            theDesc = new SimpleDescription(null, String.class);
            fail("Constructor with name null must fail!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testGetDisplayName() {
        SimpleDescription theDesc  = new SimpleDescription("Test 1", String.class);
        String            theName  = theDesc.getDisplayName();

        assertNotNull("Name is null!", theName);
        assertEquals("Name is not the expected one!",
                          "Test 1", theName);

        try {
            theDesc = new SimpleDescription(null, String.class);
            fail("Constructor with name null must fail!");
        }
        catch (IllegalArgumentException ex) {
        }
    }
//
//    public void testGetValueEntryNames() {
//        SimpleDescription theDesc  = new SimpleDescription("Test 1", String.class);
//        String[]          theNames = theDesc.getValueEntryNames();
//        
//        this.assertNotNull("Entry names are null!", theNames);
//        this.assertEquals("Entry names are not empty!", 1, theNames.length);
//        this.assertEquals("Entry name is not correct!", "Test 1", theNames[0]);
//    }

    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestSimpleDescription.class);
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
