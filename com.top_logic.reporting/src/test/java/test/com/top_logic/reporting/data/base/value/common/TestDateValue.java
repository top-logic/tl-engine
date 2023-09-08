/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.base.value.common;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.common.DateValue;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestDateValue extends TestCase {

    private static final Date START_DATE = new Date(1);

    private static final Date NOW_DATE   = new Date();

    private static final DateValue NOW   = new DateValue(NOW_DATE);

    private static final DateValue START = new DateValue(START_DATE);

    private static final DateValue END   = new DateValue(new Date(Long.MAX_VALUE));

    /**
     * Constructor for TestDateValue.
     */
    public TestDateValue(String name) {
        super(name);
    }

    public void testEmpty() {
        try {
            new DateValue(null);

            fail("Constructor with empty parameter must fail!");
        }
        catch (IllegalArgumentException expected) { /* expected */ }
    }

    public void testGetEntryAt() {
        Date theValue = (Date) NOW.getEntryAt(0);

        assertNotNull("Returned date is null!", theValue);
        assertEquals("Returned date is not the given one!", 
                          NOW_DATE, theValue);
    }

    public void testGetEntryAsString() {
        String theValue = NOW.getEntryAsString(0);

        assertNotNull("Returned date is null!", theValue);
        assertTrue("Returned date is empty!", 
                        theValue.length() > 0);

        theValue = NOW.getEntryAsString(1);

        assertNotNull("Returned date is null!", theValue);
        assertTrue("Returned date is not empty!", 
                        theValue.length() == 0);
    }

    public void testSetEntryAt() {
        DateValue theValue = new DateValue(START_DATE);

        theValue.setEntryAt(1, NOW_DATE);

        assertEquals("Returned date for 0 is not the original one!", 
                          START_DATE, theValue.getEntryAt(0));

        theValue.setEntryAt(0, null);

        assertEquals("Returned date for 0 is not the original one!", 
                          START_DATE, theValue.getEntryAt(0));

        assertEquals("Returned date for 1 is not null!", 
                          null, theValue.getEntryAt(1));

        theValue.setEntryAt(0, NOW_DATE);

        assertEquals("Returned date for 0 is not the new one!", 
                          NOW_DATE, theValue.getEntryAt(0));

        assertEquals("Returned date for 1 is not null!", 
                          null, theValue.getEntryAt(1));
    }

    public void testGetAllEntriesAsString() {
        String theName = START.getAllEntriesAsString();

        assertNotNull("Entry returns string of null!", theName);
        assertTrue("Entry returns empty string!", theName.length() > 0);
        assertTrue("Entry returns wrong string ('" + theName + 
                        "')!", theName.indexOf("70") > 0);
    }

    public void testGetNumberEntries() {
        assertEquals("Entry must return one entry!",
                          1, START.getNumberEntries());
    }

    public void testToString() {
        assertNotNull("toString() returns null string!", START.toString());
        assertTrue("toString() returns empty string!", 
                        START.toString().length() > 0);
    }

    public void testEquals() {
        assertTrue("equals() fails for two different objects!",
                        !START.equals("Erna"));
        assertTrue("equals() fails for two different objects!",
                        !START.equals(END));
        assertEquals("equals() fails for two equal objects!",
                          START, START);
    }

    public void testHashCode() {
        assertTrue("hashCode() fails for two different objects!",
                        START.hashCode() != END.hashCode());
        assertEquals("hashCode() fails for two equal objects!",
                          START.hashCode(), START.hashCode());
    }

    public static Test suite () {
        return new TestSuite(TestDateValue.class);
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
