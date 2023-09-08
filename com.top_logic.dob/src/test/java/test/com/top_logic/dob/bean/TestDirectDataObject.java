/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.bean;

import java.beans.IntrospectionException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.bean.DirectDataObject;

/**
 * Tests the functionality of {@link com.top_logic.dob.bean.DirectDataObject}.
 * 
 * @author    <a href="mailto:asc@top-logic.com">Alice Scheerer</a>
 */
public class TestDirectDataObject extends TestCase {

    /**
     * Creates an instance of the intern class <code>TestedDirectDO</code>
     * sets and reads variables and finally checks if they
     * aren't getting lost.
     */
    public void testIt() throws Exception {
        
        TestedDirectDO aTestDO = new TestedDirectDO();
        
        aTestDO.intVal = 77;
        aTestDO.strVal = "String for TestDirectDataObject";
        
		assertEquals(Integer.valueOf(77),
                     aTestDO.getAttributeValue("int"));
        assertEquals("String for TestDirectDataObject", 
                    aTestDO.getAttributeValue("str"));
        
		aTestDO.setAttributeValue("int", Integer.valueOf(99));
        aTestDO.setAttributeValue("str", "Some other String");
       
        assertEquals(99,aTestDO.intVal);
        assertEquals("Some other String", aTestDO.strVal);
    }


    /**
     * This class contains two fields and accordant getter and
     * setter methods. It extends DirectDataObject and is used
     * for testing purposes only.
     *
     * @author    <a href="mailto:asc@top-logic.com">asc</a>
     */
   public static class TestedDirectDO extends DirectDataObject {
        /** Integer variable */
        int     intVal;
        
        /** String variable */
        String  strVal;
        
        /**
         * Creates an instance of TestedDirectDO by calling <b>super</b>.
         * @throws IntrospectionException if no instance of <code>TestedDirectDO()</code> could be created.
         */
        public TestedDirectDO() throws IntrospectionException {
            super();
        }
        

        /**
         * Returns the value of the integer variable.
         * @return the value of the integer variable as <b>int</b>.
         */
        public int getInt() {
            return intVal;
        }

        /**
         * Returns the value of the String variable.
         * @return the value of the String variable as <code>String</code>.* 
         */
        public String getStr() {
            return strVal;
        }

        /**
         * Sets the value of the integer variable.
         * @param i  the value of the integer variable as <b>int</b>.
         */
        public void setInt(int i) {
            intVal = i;
        }

        /**
         * Sets the value of the String variable.
         * @param string the value of the String variable as <b>String</b>.
         */
        public void setStr(String string) {
            strVal = string;
        }

    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return new TestSuite (TestDirectDataObject.class);
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
   
}
