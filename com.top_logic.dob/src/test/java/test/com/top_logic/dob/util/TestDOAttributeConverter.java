/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.util;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.data.DefaultDataObject;
import com.top_logic.dob.data.Link;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.util.DOAttributeConverter;

/** 
 * testcase for the {@link com.top_logic.dob.util.DOAttributeConverter}.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestDOAttributeConverter extends TestCase {

    /**
     * Constructor for TestDOAttributeConverter.
     */
    public TestDOAttributeConverter(String name) {
        super(name);
    }

    /**
     * Test for {@link DOAttributeConverter#toString}
     */
    public void testToStringObject() {
        this._testIt("Hallo");
        this._testIt("");
        this._testIt(new Date());
		this._testIt(Long.valueOf(Long.MAX_VALUE));
		this._testIt(Integer.valueOf(Integer.MAX_VALUE));
		this._testIt(Float.valueOf(Float.MAX_VALUE));
		this._testIt(Double.valueOf(Double.MAX_VALUE));
        this._testIt(Boolean.TRUE);
        
        this._testIt(new DefaultDataObject(new MOClassImpl("Test")));
    }

    /**
     * Covert anObject to String and back ...
     */
    protected void _testIt(Object anObject) {
        Object theResult;
        String theValue = DOAttributeConverter.toString(anObject);

        assertNotNull("Unable to convert object " + theValue, theValue);

        theResult = DOAttributeConverter.toObject(theValue);

        assertNotNull("Reconverting fails (no object returned)!", theResult);
        if (! (theResult instanceof Link)) {
            assertEquals("Failed " + theValue, anObject, theResult);
        } else {
            Link       theLink = (Link) theResult;
            DataObject theBase = (DataObject) anObject;
            assertEquals(theLink.getID()  , theBase.getIdentifier());
            assertEquals(theLink.getType(), theBase.tTable().getName());
        }
    }

    /**
     * the suite of tests to perform
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestDOAttributeConverter.class);

        return suite;
    }

    /**
     * main function for direct Testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (TestDOAttributeConverter.suite ());
    }
}
