/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.data.base.value.common;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.reporting.data.base.value.common.NumericTuple;
import com.top_logic.reporting.data.base.value.common.NumericValue;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestNumericTuple extends TestCase {

    private static final Number[] EMPTY_ARRAY = {null};

    private static final Number[] SIMPLE_1 = {Double.valueOf(2),
                                              Double.valueOf(3),
                                              Double.valueOf(4),
                                              Double.valueOf(5)};

    private static final Number[] SIMPLE_2 = {Double.valueOf(12),
                                              Double.valueOf(13),
                                              Double.valueOf(14),
                                              Double.valueOf(15)};

    private static final Number[] SIMPLE_3 = {Double.valueOf(12),
                                              null,
                                              Double.valueOf(14),
                                              Double.valueOf(15)};

    private static final Number[] SIMPLE_4 = {Double.valueOf(12),
                                              Double.valueOf(13),
                                              Double.valueOf(14)};

    private static final Number[] SIMPLE_5 = {Double.valueOf(12),
                                              null,
                                              Double.valueOf(14)};

    /**
     * Constructor for TestNumericTuple.
     */
    public TestNumericTuple(String name) {
        super(name);
    }

    public void testEmpty() {
        this._testEmpty(new NumericTuple());
    }

    public void testFloat() {
        NumericValue theValue = new NumericTuple(new float[0]);

        this._testEmpty(theValue);
        
        theValue = new NumericTuple(new float[] {1f, 34f});

        assertEquals("Values for float are illegal!", 
                          1d, 
                          ((Number) theValue.getEntryAt(0)).doubleValue(),
                          0.1);

        assertEquals("Values for float are illegal!", 
                          34d, 
                          ((Number) theValue.getEntryAt(1)).doubleValue(),
                          0.1);
    }

    public void testBasics() {
        NumericValue theObj1a = this.createNumericValue(SIMPLE_1);
        NumericValue theObj1b = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2  = this.createNumericValue(SIMPLE_2);
        
        assertEquals(theObj1a, theObj1b);
        assertEquals(theObj1a.hashCode(), theObj1b.hashCode());
        assertTrue(theObj1a.getAllEntriesAsString() + " is equal " +
                        theObj2.getAllEntriesAsString(), 
                        !theObj1a.equals(theObj2));
        assertTrue(theObj1a.getAllEntriesAsString() + " has same hashcode as " +
                        theObj2.getAllEntriesAsString(), 
                        theObj1a.hashCode() != theObj2.hashCode());
    }

    public void testPlus() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        NumericValue theObj4 = this.createNumericValue(SIMPLE_4);
        NumericValue theObj5 = this.createNumericValue(SIMPLE_5);

        theResult = theObj2.plus(null);
        assertEquals("Wrong size of result", 
                      SIMPLE_2.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(0), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(1), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(2), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(3), theResult.getEntryAt(3));

        theResult = theObj1.plus(theObj2);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(14), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(16), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(18), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(20), theResult.getEntryAt(3));

        theResult = theObj1.plus(theObj3);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(14), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(18), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(20), theResult.getEntryAt(3));

        theResult = theObj1.plus(theObj4);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(14), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(16), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(18), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(5), theResult.getEntryAt(3));

        theResult = theObj1.plus(theObj4);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(14), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(16), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(18), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(5), theResult.getEntryAt(3));

        theResult = theObj1.plus(theObj5);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(14), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(18), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(5), theResult.getEntryAt(3));

        NumericValue theResult2 = theObj5.plus(theObj1);

        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Commutative rule fails (" + theResult.getAllEntriesAsString() + " != " +
                      theResult2.getAllEntriesAsString() + ")!", 
                          theResult, theResult2);
    }

    public void testMinus() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        NumericValue theObj4 = this.createNumericValue(SIMPLE_4);
        /* NumericValue theObj5 = */ this.createNumericValue(SIMPLE_5);

        theResult = theObj2.minus(null);
        assertEquals("Wrong size of result", 
                      SIMPLE_2.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(0), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(1), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(2), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(3), theResult.getEntryAt(3));

        theResult = theObj2.minus(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(3));

        theResult = theObj3.minus(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(-3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(3));

        theResult = theObj4.minus(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(10), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(-5), theResult.getEntryAt(3));
    }

    public void testMultiply() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        /* NumericValue theObj4 = */ this.createNumericValue(SIMPLE_4);
        /* NumericValue theObj5 = */ this.createNumericValue(SIMPLE_5);

        theResult = theObj2.multiply(null);
        assertEquals("Wrong size of result", 
                      SIMPLE_2.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(0), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(1), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(2), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(3), theResult.getEntryAt(3));

        theResult = theObj2.multiply(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(24), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(39), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(56), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(75), theResult.getEntryAt(3));

        theResult = theObj3.multiply(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(24), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(56), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(75), theResult.getEntryAt(3));
    }

    public void testDevide() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        /* NumericValue theObj4 = */ this.createNumericValue(SIMPLE_4);
        /* NumericValue theObj5 = */ this.createNumericValue(SIMPLE_5);

        theResult = theObj2.devide(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(6), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf((double) 13 / 3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf((double) 14 / 4), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(3), theResult.getEntryAt(3));

        theResult = theObj3.devide(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(6), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf((double) 1 / 3), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf((double) 14 / 4), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      Double.valueOf(3), theResult.getEntryAt(3));
    }

    public void testMax() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        NumericValue theObj4 = this.createNumericValue(SIMPLE_4);
        NumericValue theObj5 = this.createNumericValue(SIMPLE_5);

        theResult = theObj2.max(null);
        assertEquals("Wrong size of result", 
                      SIMPLE_2.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(0), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(1), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(2), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(3), theResult.getEntryAt(3));

        theResult = theObj2.max(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[3], theResult.getEntryAt(3));

        theResult = theObj3.max(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_2[3], theResult.getEntryAt(3));

        theResult = theObj4.max(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_4[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_4[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_4[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));

        theResult = theObj5.max(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_5[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_5[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));
    }

    public void testMin() {
        NumericValue theResult;
        NumericValue theObj1 = this.createNumericValue(SIMPLE_1);
        NumericValue theObj2 = this.createNumericValue(SIMPLE_2);
        NumericValue theObj3 = this.createNumericValue(SIMPLE_3);
        NumericValue theObj4 = this.createNumericValue(SIMPLE_4);
        NumericValue theObj5 = this.createNumericValue(SIMPLE_5);

        theResult = theObj2.min(null);
        assertEquals("Wrong size of result", 
                      SIMPLE_2.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(0), theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(1), theResult.getEntryAt(1));
        assertEquals("Wrong result in position 3 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(2), theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      theObj2.getEntryAt(3), theResult.getEntryAt(3));

        theResult = theObj2.min(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));

        theResult = theObj3.min(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));

        theResult = theObj4.min(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));

        theResult = theObj5.min(theObj1);
        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theResult.getNumberEntries());
        assertEquals("Wrong result in position 1 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[0], theResult.getEntryAt(0));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[1], theResult.getEntryAt(1));
        assertEquals("Wrong result in position 2 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[2], theResult.getEntryAt(2));
        assertEquals("Wrong result in position 4 (" + theResult.getAllEntriesAsString() + ")!", 
                      SIMPLE_1[3], theResult.getEntryAt(3));
    }

    public void testGetEntryAsString() {
        Number       theValue;
        NumericValue theObject = this.createNumericValue(new Number[3]);

        theValue = Double.valueOf(1);
        theObject.setEntryAt(0, theValue);
        assertEquals("Found entry as string is not \"1\"!", 
                      "1.0", theObject.getEntryAsString(0));

        theValue = Double.valueOf(2);
        theObject.setEntryAt(1, theValue);
        assertEquals("Found entry as string is not \"2\"!", 
                      "2.0", theObject.getEntryAsString(1));

        theValue = Double.valueOf(3);
        theObject.setEntryAt(2, theValue);
        assertEquals("Found entry as string is not \"3\"!", 
                      "3.0", theObject.getEntryAsString(2));
    }

    public void testGetEntryAt() {
        Number       theValue;
        NumericValue theObject = this.createNumericValue(new Number[3]);

        theValue = Double.valueOf(1);
        theObject.setEntryAt(0, theValue);
        assertEquals("Setting the value at 0 fails!", 
                      theValue, theObject.getEntryAt(0));

        theValue = Double.valueOf(2);
        theObject.setEntryAt(1, theValue);
        assertEquals("Setting the value at 1 fails!", 
                      theValue, theObject.getEntryAt(1));

        theValue = Double.valueOf(3);
        theObject.setEntryAt(2, theValue);
        assertEquals("Setting the value at 2 fails!", 
                      theValue, theObject.getEntryAt(2));

        try {
            theValue = Double.valueOf(4);
            theObject.setEntryAt(3, theValue);

            fail("Not allowed to set entry at position out of range!");
        }
        catch (Exception ex) {
            // Expected behavior...
        }

        try {
            theValue = Double.valueOf(4);
            theObject.setEntryAt(-1, theValue);

            fail("Not allowed to set entry at position out of range!");
        }
        catch (Exception ex) {
            // Expected behavior...
        }
    }

    public void testGetAllEntriesAsString() {
        NumericValue theObject = this.createNumericValue(SIMPLE_5);

        assertNotNull(theObject.getAllEntriesAsString());

        theObject = this.createNumericValue(EMPTY_ARRAY);

        assertNotNull(theObject.getAllEntriesAsString());
    }

    public void testGetNumberEntries() {
        NumericValue theObject = this.createNumericValue(SIMPLE_1);

        assertEquals("Wrong size of result", 
                      SIMPLE_1.length, theObject.getNumberEntries());
    }

    protected void _testEmpty(NumericValue aValue) {
        String theName  = aValue.getEntryAsString(0);

        assertEquals("Wrong number of entries!",
                      0, aValue.getNumberEntries());

        assertNull("Wrong entries in empty numeric value!",
                    aValue.getEntryAt(0));

        assertNotNull("null for string representation of empty numeric value!",
                       theName);

        assertEquals("Wrong string for empty numeric value!",
                      0, theName.length());

        assertTrue("Equals method works wrong!",
                    !aValue.equals("Erna2"));

        theName = aValue.getAllEntriesAsString();

        assertNotNull("Entries as string returns null!", theName);

        assertEquals("Entries as string returns no empty result!", 
                      0, theName.length());
    }

    protected NumericValue createNumericValue() {
        return (new NumericTuple());
    }

    protected NumericValue createNumericValue(Number[] someValues) {
        return (new NumericTuple(someValues));
    }

    public static Test suite () {
        return new TestSuite(TestNumericTuple.class);
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
