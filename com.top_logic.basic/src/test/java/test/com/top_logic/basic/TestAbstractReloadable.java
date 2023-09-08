/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.AbstractReloadable;

/**
 * Testcase for {@link com.top_logic.basic.AbstractReloadable}.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestAbstractReloadable extends TestCase {

    /** Default CTor, create a test with this Function Name */
    public TestAbstractReloadable(String name) {
        super(name);
    }

    /** Single Testcase for now */
    public void testMain() {
        AbstractReloadable ser = new AbstractReloadable() {
			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public String getName() {
				return null;
			}
		};  
        
        assertNotNull(ser.toString());
        assertTrue(ser.reload());
        assertNull(ser.getName());
        assertNull(ser.getDescription());
        assertEquals(0, ser.getConfiguration().getNames().size());
        
        assertEquals("seitwärts", ser.getProperty("vormärz" , "seitwärts"));
        
        assertTrue(ser.usesXMLProperties());
    }


    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestAbstractReloadable.class));
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
