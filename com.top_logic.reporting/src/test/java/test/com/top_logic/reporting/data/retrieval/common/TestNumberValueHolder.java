/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.retrieval.common;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.common.NumberValue;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.ValueHolder;
import com.top_logic.reporting.data.retrieval.common.NumberValueHolder;
import com.top_logic.reporting.data.retrieval.simple.SimpleRange;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestNumberValueHolder extends TestCase {

    /**
     * Constructor for TestNumberValueHolder.
     */
    public TestNumberValueHolder(String name) {
        super(name);
    }

    public void testEmpty() {
        try {
            new NumberValueHolder(null, "Test 1");

            fail("Constructor with map null should fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        HashMap theMap = new HashMap();

        try {
            new NumberValueHolder(theMap, null);

            fail("Constructor with name null should fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new NumberValueHolder(theMap, "Test 1");

            fail("Constructor with name null should fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new NumberValueHolder(theMap, "");

            fail("Constructor with empty name should fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            theMap.put("Key", "Value");

            new NumberValueHolder(theMap, "Test 1");

            fail("Constructor with wrong map should fail!");
        }
        catch (ClassCastException ex) {
        }
    }

    public void testGetRange() {
        this._testGetRange(this.createValueHolderNumber());
        this._testGetRange(this.createValueHolderDouble());
        this._testGetRange(this.createValueHolderFloat());
        this._testGetRange(this.createValueHolderInt());
        this._testGetRange(this.createValueHolder2());
    }

    public void testGetValue() {
        this._testGetValue(this.createValueHolderNumber());
        this._testGetValue(this.createValueHolderDouble());
        this._testGetValue(this.createValueHolderFloat());
        this._testGetValue(this.createValueHolderInt());
        this._testGetValue(this.createValueHolder2());
    }

    public void testGetDisplayName() {
        this._testGetDisplayName(this.createValueHolderNumber());
        this._testGetDisplayName(this.createValueHolderDouble());
        this._testGetDisplayName(this.createValueHolderFloat());
        this._testGetDisplayName(this.createValueHolderInt());
        this._testGetDisplayName(this.createValueHolder2());
    }

    protected void _testGetRange(ValueHolder aHolder) {
        Range[]     theRange  = aHolder.getRange();

        assertEquals("Invalid length of range array", 3, theRange.length);

        assertEquals("Invalid value at position 0", 
                          "2002", theRange[0].getComparable() );

        assertEquals("Invalid value at position 1", 
                          "2003", theRange[1].getComparable());

        assertEquals("Invalid value at position 2", 
                          "2004", theRange[2].getComparable());
    }

    protected void _testGetValue(ValueHolder aHolder) {
        Range[] theRange = aHolder.getRange();

        assertEquals("Invalid length of range array", 3, theRange.length);

        assertEquals("Invalid value at position 0", 
                          ((Number) new NumberValue(1002).getEntryAt(0)).doubleValue(),
                          ((Number) aHolder.getValue(theRange[0]).getEntryAt(0)).doubleValue(),
                          0.1);

        assertEquals("Invalid value at position 1", 
                          ((Number) new NumberValue(1003).getEntryAt(0)).doubleValue(),
                          ((Number) aHolder.getValue(theRange[1]).getEntryAt(0)).doubleValue(),
                          0.1);

        assertEquals("Invalid value at position 2", 
                          ((Number) new NumberValue(1004).getEntryAt(0)).doubleValue(),
                          ((Number) aHolder.getValue(theRange[2]).getEntryAt(0)).doubleValue(),
                          0.1);
    }

    protected void _testGetDisplayName(ValueHolder aHolder) {
        assertEquals("Invalid name of holder", 
                          "Demo", aHolder.getDisplayName());
    }
    
    protected ValueHolder createValueHolderNumber() {
        Map theMap = new TreeMap();

        theMap.put(new SimpleRange("2002"), new NumberValue(Double.valueOf(1002d)));
        theMap.put(new SimpleRange("2003"), new NumberValue(Double.valueOf(1003d)));
        theMap.put(new SimpleRange("2004"), new NumberValue(Double.valueOf(1004d)));

        return (new NumberValueHolder(theMap, "Demo"));
    }
    
    protected ValueHolder createValueHolderDouble() {
        Map theMap = new TreeMap();

        theMap.put(new SimpleRange("2002"), new NumberValue(1002d));
        theMap.put(new SimpleRange("2003"), new NumberValue(1003d));
        theMap.put(new SimpleRange("2004"), new NumberValue(1004d));

        return (new NumberValueHolder(theMap, "Demo"));
    }
    
    protected ValueHolder createValueHolderFloat() {
        Map theMap = new TreeMap();

        theMap.put(new SimpleRange("2002"), new NumberValue(1002f));
        theMap.put(new SimpleRange("2003"), new NumberValue(1003f));
        theMap.put(new SimpleRange("2004"), new NumberValue(1004f));

        return (new NumberValueHolder(theMap, "Demo"));
    }

    protected ValueHolder createValueHolderInt() {
        Map theMap = new TreeMap();

        theMap.put(new SimpleRange("2002"), new NumberValue(1002));
        theMap.put(new SimpleRange("2003"), new NumberValue(1003));
        theMap.put(new SimpleRange("2004"), new NumberValue(1004));

        return (new NumberValueHolder(theMap, "Demo"));
    }
    
    protected ValueHolder createValueHolder2() {
        Range[]        theRange  = new Range[] {new SimpleRange("2002"),
                                         new SimpleRange("2003"),
                                         new SimpleRange("2004")};
        NumericValue[] theValues = new NumericValue[] {new NumberValue(1002),
                                         new NumberValue(1003),
                                         new NumberValue(1004)};

        return (new NumberValueHolder("Demo", theRange, theValues));
    }

    public static Test suite () {
        return new TestSuite(TestNumberValueHolder.class);
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
