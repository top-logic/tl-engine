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
import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.base.description.simple.SimpleDescription;
import com.top_logic.reporting.data.base.value.Value;
import com.top_logic.reporting.data.base.value.common.NumericValue;
import com.top_logic.reporting.data.retrieval.DataHandler;
import com.top_logic.reporting.data.retrieval.Range;
import com.top_logic.reporting.data.retrieval.ValueHolder;
import com.top_logic.reporting.data.retrieval.simple.SimpleDataHandler;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestExampleDataHandler extends BasicTestCase {

    /**
     * Constructor for TestExampleDataHandler.
     */
    public TestExampleDataHandler(String name) {
        super(name);
    }

    public void testGetSimple() {
        Description[]     theDesc    = new Description[] {new SimpleDescription("Test 1",
                                                                                NumericValue.class)};
        SimpleDataHandler theHandler = new SimpleDataHandler("Test 1", theDesc);

        assertEquals("Simple handler has children!", 
                          0, theHandler.getMaximumDepth());

        assertNull("Simple handler has children!", 
                        theHandler.getChildren());

        theHandler = new SimpleDataHandler("Test 1",theDesc, -1, null);

        assertEquals("Simple handler has children!", 
                          0, theHandler.getMaximumDepth());

        assertNull("Simple handler has children!", 
                        theHandler.getChildren());

        theHandler = new SimpleDataHandler("Test 1",theDesc, 0, new DataHandler[0]);

        assertEquals("Simple handler has children!", 
                          0, theHandler.getMaximumDepth());

        assertNull("Simple handler has children!", theHandler.getChildren());

        try {
            new SimpleDataHandler("Test 1",theDesc, 
                                  -1, new DataHandler[] {theHandler});

            fail("Expected exception when wrong depth for a data handler");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new SimpleDataHandler("Test 1",theDesc, 1, null);

            fail("Expected exception when wrong depth for a data handler");
        }
        catch (IllegalArgumentException ex) {
        }
    }

    public void testGetExample() {
        try {
            new ExampleDataHandler(null, 
                                   new Description[] {new SimpleDescription("Test 1",
                                                                            NumericValue.class)});

            fail("Constructor with null for name must fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new ExampleDataHandler("Test", null);

            fail("Constructor with null for description must fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new ExampleDataHandler("", null);

            fail("Constructor with empty name must fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        try {
            new ExampleDataHandler("Test", new Description[0]);

            fail("Constructor with empty description must fail!");
        }
        catch (IllegalArgumentException ex) {
        }

        this._testGetExample(ExampleDataHandler.getExample());
        this._testGetExample(ExampleDataHandler.getExample(1));
        this._testGetExample(ExampleDataHandler.getExample(2));
    }

    protected void _testGetExample(DataHandler aHandler) {
        /*
        Operator[]    theOperation;
        Value[]       theValues;
        Operator      theOp;
        Value         theValue;
        */
        ValueHolder   theHolder;
        String        theDisplay;
        Range[]       theRange;

        assertNotNull("DataHandler is null!", aHandler);

        Description[] theDesc     = aHandler.getDescriptions();
        int           theDepth    = aHandler.getMaximumDepth();
        DataHandler[] theChildren = aHandler.getChildren();
        String        theName     = aHandler.getDisplayName();

        assertNotNull("Display name is null!", theName);
        assertTrue("Display name is empty!", theName.length() > 0);

        if (theDepth > 0) {
            assertNotNull("Depth larger than zero but children are null!", 
                               theChildren);

            assertTrue("Depth larger than zero but children are empty!", 
                            theChildren.length > 0);
        }
        else {
            assertNull("Depth is zero but children are not null!", 
                            theChildren);
        }

        assertNotNull("Descriptions are null!", theDesc);
        assertTrue("Descriptions are empty!", theDesc.length > 0);

        for (int thePos = 0; thePos < theDesc.length; thePos++) {
            theDisplay   = theDesc[thePos].getDisplayName();


            assertNotNull("Description is null for " + theDesc[thePos] + '!', 
                               theDisplay);

            assertTrue("Description is empty for " + theDesc[thePos] + '!', 
                             theDisplay.length() > 0);

        }

        for (int thePos = 0; thePos < theDesc.length; thePos++) {
            theHolder    = aHandler.getValueHolder(theDesc[thePos]);
            theRange     = theHolder.getRange();

            Logger.debug("Inspecting " + theHolder + " for " + theDesc[thePos], 
                         this);

            assertNotNull("ValueHolder is null for " + theDesc[thePos] + '!', 
                               theHolder);

            theName = theHolder.getDisplayName();

            assertNotNull("Name of ValueHolder is null for " + theDesc[thePos] + '!', 
                               theName);
            assertTrue("Name of ValueHolder is empty for " + theDesc[thePos] + '!', 
                            theName.length() > 0);

            for (int thePos2 = 0; thePos2 < theRange.length; thePos2++) {
                assertNotNull("Range " + thePos2 + " of " + theHolder + 
                                   " is null!", 
                                   theRange[thePos2]);

                theName = theRange[thePos2].getDisplayName();

                assertNotNull("Name of range " + thePos2 + " of " + theHolder + 
                                   " is null!", 
                                   theName);

                assertTrue("Name of range " + thePos2 + " of " + theHolder + 
                                " is empty!", 
                                theName.length() > 0);
            }

        }

        if ((aHandler instanceof SimpleDataHandler) && (theDesc.length > 0)) {
            theHolder = ((SimpleDataHandler) aHandler).removeValue(theDesc[0]);

            assertNotNull("Removing of first value holder returned null!",
                               theHolder);
        }
    }

    /**
     * Return the values from a value holder.
     * 
     * @param    aHolder    The value holder to be inspected.
     * @return   The values from the value holder.
     */
    protected Value[] getValues(ValueHolder aHolder) {
        Range[] theRange  = aHolder.getRange();
        Value[] theValues = new Value[theRange.length];

        for (int thePos = 0; thePos < theRange.length; thePos++) {
            theValues[thePos] = aHolder.getValue(theRange[thePos]);
        }

        return (theValues);
    }

    public static Test suite () {
        return ReportingSetup.createReportingSetup(TestExampleDataHandler.class);
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
