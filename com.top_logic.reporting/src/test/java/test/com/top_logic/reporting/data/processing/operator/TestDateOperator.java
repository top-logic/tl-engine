/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.processing.operator;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.DateValue;
import com.top_logic.reporting.data.base.value.common.NullValue;
import com.top_logic.reporting.data.processing.operator.AbstractDateOperator;
import com.top_logic.reporting.data.processing.operator.common.DateMaxOperator;
import com.top_logic.reporting.data.processing.operator.common.DateMinOperator;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestDateOperator extends TestCase {

    private static final DateValue MIN_VALUE = new DateValue(new Date(0));

    private static final DateValue MAX_VALUE = new DateValue(new Date(999999));

    private static final Value[] VALUES_EMPTY = new Value[0];

    private static final DateValue[] VALUES_MAX = {MIN_VALUE,
                                               new DateValue(new Date(232223)),
                                               MAX_VALUE,
                                               new DateValue(new Date(100450)),
                                               };
    private static final DateValue[] VALUES_MIN = {new DateValue(new Date(232223)),
                                               MAX_VALUE,
                                               MIN_VALUE,
                                               new DateValue(new Date(100450)),
                                               };
    private static final DateValue[] VALUES_NULL = {new DateValue(new Date(232223)),
                                               MAX_VALUE,
                                               null,
                                               MIN_VALUE,
                                               };
    
    private static final Class[] TYPES = {DateValue.class};

    private AbstractDateOperator minOperator;

    private AbstractDateOperator maxOperator;

    /**
     * Constructor for TestDateOperator.
     */
    public TestDateOperator(String name) {
        super(name);
    }

    public void testEmpty() {
        try {
            new DateMaxOperator(null);

            fail("DateMaxOperator with null name created!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new DateMaxOperator("");

            fail("DateMaxOperator with empty name created!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new DateMinOperator(null);

            fail("DateMinOperator with null name created!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new DateMinOperator("");

            fail("DateMinOperator with empty name created!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    /*
     * Test for Value process(Value[])
     */
    public void testProcessValueArray() {
        Value theValue = this.minOperator.process(VALUES_MIN);

        assertEquals("Minimum operation fails.", MIN_VALUE, theValue);

        theValue = this.maxOperator.process(VALUES_MAX);

        assertEquals("Maximum operation fails.", MAX_VALUE, theValue);

        theValue = this.minOperator.process(null);

        assertEquals("Minimum operation fails.", NullValue.INSTANCE , theValue);

        theValue = this.maxOperator.process(null);

        assertEquals("Maximum operation fails.", NullValue.INSTANCE , theValue);

        theValue = this.minOperator.process(VALUES_EMPTY);

        assertEquals("Minimum operation fails.", NullValue.INSTANCE , theValue);

        theValue = this.maxOperator.process(VALUES_EMPTY);

        assertEquals("Maximum operation fails.", NullValue.INSTANCE , theValue);

        theValue = this.minOperator.process(VALUES_NULL);

        assertEquals("Minimum operation fails.", MIN_VALUE, theValue);

        theValue = this.maxOperator.process(VALUES_NULL);

        assertEquals("Maximum operation fails.", MAX_VALUE, theValue);
    }

    public void testGetName() {
        assertNotNull("Name of " + this.minOperator + " is null!",
                           this.minOperator.getName());
        assertNotNull("Name of " + this.maxOperator + " is null!",
                           this.maxOperator.getName());
    }


    public void testGetNeutralValue() {
        assertNotNull("Neutral value of " + this.minOperator + " is null!",
                           this.minOperator.getNeutralValue());
        assertNotNull("Neutral value of " + this.maxOperator + " is null!",
                           this.maxOperator.getNeutralValue());
    }

    public void testGetDisplayName() {
        assertNotNull("Display name of " + this.minOperator + " is null!",
                           this.minOperator.getDisplayName());
        assertNotNull("Display name of " + this.maxOperator + " is null!",
                           this.maxOperator.getDisplayName());
    }

    public void testGetSupportedTypes() {
        assertEquals("Wrong supported typed in " + this.minOperator + '!', 
                          TYPES[0], this.minOperator.getSupportedTypes()[0]);
        assertEquals("Wrong supported typed in " + this.maxOperator + '!', 
                          TYPES[0], this.minOperator.getSupportedTypes()[0]);
    }

    @Override
	protected void setUp() {
        this.minOperator  = new DateMinOperator("MIN");
        this.maxOperator  = new DateMaxOperator("MAX");
    }

    @Override
	protected void tearDown() {
        this.minOperator  = null;
        this.maxOperator  = null;
    }

    public static Test suite () {
        return new TestSuite(TestDateOperator.class);
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
