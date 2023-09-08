/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.mig.util.StringHolder;

/**
 * TestCase for StringHolder
 *
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TestStringHolder extends TestCase {

    /**
     * Constructor for TestStringHolder.
     */
    public TestStringHolder(String arg0) {
        super(arg0);
    }
    
    public void testAll() throws Exception {
        StringHolder theHolder = new StringHolder();
        assertNull(theHolder.getValue());
        theHolder.setValue("aValue");
        assertEquals("aValue", theHolder.getValue());
        
        theHolder = new StringHolder("aValue");
        assertEquals("aValue", theHolder.getValue());
        
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestStringHolder.class);
    }


    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestStringHolder.class);
    }

}
