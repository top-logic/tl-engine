/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.retrieval;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NumberValue;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.ValueHolder;
import com.top_logic.reporting.data.retrieval.simple.SimpleRange;
import com.top_logic.reporting.data.retrieval.simple.SimpleValueHolder;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestValueHolder extends TestCase {

    /**
     * Constructor for TestValueHolder.
     */
    public TestValueHolder(String name) {
        super(name);
    }

    public void testGetRange() {
        ValueHolder  theHolder = this.createValueHolder();
        Comparable[] theRange  = theHolder.getRange();

        assertNotNull("Range is null!", theRange);

        for (int thePos = 0; thePos < theRange.length; thePos++) {
            assertNotNull("Range[" + thePos + "] is null!", 
                               theRange[thePos]);
        }

        try {
            this.createWrongValueHolder2();

            fail("ValueHolder without range created!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testGetValue() {
        Value       theValue;
        ValueHolder theHolder = this.createValueHolder();
        Range[]     theRange  = theHolder.getRange();

        assertNotNull("Range is null!", theRange);

        theValue = theHolder.getValue(null);

        assertNull("Failed to get value for null!", theValue);

        for (int thePos = 0; thePos < theRange.length; thePos++) {
            theValue = theHolder.getValue(theRange[thePos]);

            assertNotNull("Value for range[" + thePos + "] is null!", 
                               theValue);
        }

        try {
            this.createWrongValueHolder3();

            fail("ValueHolder without value created!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            this.createWrongValueHolder4();

            fail("ValueHolder with wrong sizes created!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testGetDisplayName() {
        String theName = this.createValueHolder().getDisplayName();

        assertNotNull("Display name is null!", theName);
        assertTrue("Display name is empty!", theName.length() > 0);

        try {
            this.createWrongValueHolder1();

            fail("ValueHolder without display name created!");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    protected ValueHolder createValueHolder() {
        Range[] theRange  = new Range[] {
                                    new SimpleRange(Integer.valueOf(2002)),
                                    new SimpleRange(Integer.valueOf(2003)),
                                    new SimpleRange(Integer.valueOf(2004)),
                                    new SimpleRange(Integer.valueOf(2006))};
        Value[] theValues = new Value[] {
                                    new NumberValue(10000),
                                    new NumberValue(20000),
                                    new NumberValue(30000),
                                    new NumberValue(40000)};

        return (new SimpleValueHolder("Costs", theRange, theValues));
    }

    protected ValueHolder createWrongValueHolder1() {
        Range[] theRange  = new Range[] {
                                    new SimpleRange(Integer.valueOf(2002)),
                                    new SimpleRange(Integer.valueOf(2003)),
                                    new SimpleRange(Integer.valueOf(2004)),
                                    new SimpleRange(Integer.valueOf(2006))};
        Value[] theValues = new Value[] {
                                    new NumberValue(10000),
                                    new NumberValue(20000),
                                    new NumberValue(30000),
                                    new NumberValue(40000)};

        return (new SimpleValueHolder(null, theRange, theValues));
    }

    protected ValueHolder createWrongValueHolder2() {
        /* Range[] theRange  =  new Range[] {
                                    new SimpleRange(Integer.valueOf(2002)),
                                    new SimpleRange(Integer.valueOf(2003)),
                                    new SimpleRange(Integer.valueOf(2004)),
                                    new SimpleRange(Integer.valueOf(2006))}; */
        Value[] theValues = new Value[] {
                                    new NumberValue(10000),
                                    new NumberValue(20000),
                                    new NumberValue(30000),
                                    new NumberValue(40000)};

        return (new SimpleValueHolder("Costs", null, theValues));
    }

    protected ValueHolder createWrongValueHolder3() {
        Range[] theRange  = new Range[] {
                                    new SimpleRange(Integer.valueOf(2002)),
                                    new SimpleRange(Integer.valueOf(2003)),
                                    new SimpleRange(Integer.valueOf(2004)),
                                    new SimpleRange(Integer.valueOf(2006))};
        /* Value[] theValues = new Value[] {
                                    new NumberValue(10000),
                                    new NumberValue(20000),
                                    new NumberValue(30000),
                                    new NumberValue(40000)}; */
        return (new SimpleValueHolder("Costs", theRange, null));
    }

    protected ValueHolder createWrongValueHolder4() {
        Range[] theRange  = new Range[] {
                                    new SimpleRange(Integer.valueOf(2002)),
                                    new SimpleRange(Integer.valueOf(2003)),
                                    new SimpleRange(Integer.valueOf(2004)),
                                    new SimpleRange(Integer.valueOf(2006))};
        Value[] theValues = new Value[] {
                                    new NumberValue(10000),
                                    new NumberValue(30000),
                                    new NumberValue(40000)};

        return (new SimpleValueHolder("Costs", theRange, theValues));
    }

    public static Test suite () {
        return new TestSuite(TestValueHolder.class);
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
