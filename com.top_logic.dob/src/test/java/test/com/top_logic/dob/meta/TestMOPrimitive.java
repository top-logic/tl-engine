/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Tescase for {@link com.top_logic.dob.attr.MOPrimitive}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestMOPrimitive extends TestCase {

    /**
     * Constructor for TestMOPrimitive.
     * 
     * @param name function to execute for testing
     */
    public TestMOPrimitive(String name) {
        super(name);
    }

    /**
     * Test for some boring aspects of MOPrimitive.
     */
    public void testIsIt () {
        
        MOPrimitive primiv = MOPrimitive.SHORT;
        
        assertTrue(MetaObjectUtils.isPrimitive(primiv));
        assertTrue(!MetaObjectUtils.isClass(primiv));
        assertTrue(!MetaObjectUtils.isStructure(primiv));
        
        try {
            MetaObjectUtils.getAttribute(primiv, "any");
            fail("Expected NoSuchAttributeException");
        } catch (NoSuchAttributeException expected) { /* excpected */ }

        assertNull(MetaObjectUtils.getAttributeNames(primiv));
        assertTrue(MetaObjectUtils.getAttributes(primiv).isEmpty());
        assertTrue(!MetaObjectUtils.hasAttribute(primiv, "any"));
        // just to call int one ....
        primiv.hashCode();
        assertTrue(primiv.isSubtypeOf(primiv));
    }

    /*
     * Test for MOPrimitive getPrimitive(String)
     */
    public void testGetPrimitiveString() {
        MOPrimitive string1 = MOPrimitive.getPrimitive("String");
        assertTrue(MetaObjectUtils.isPrimitive(string1));

        MOPrimitive string2 = MOPrimitive.getPrimitive("java.lang.String");
        assertTrue(MetaObjectUtils.isPrimitive(string2));
        
        assertEquals(string1,string2);
        
		assertEquals(DBType.STRING, string1.getDefaultSQLType());
        string1.getDefaultSQLSize();
        string1.getDefaultSQLPrecision();
        
        assertNull(MOPrimitive.getPrimitive("Not a Primitive Type"));
    }

    /*
     * Test for MOPrimitive getPrimitive(Class)
     */
    public void testGetPrimitiveClass() {
        MOPrimitive string = MOPrimitive.getPrimitive(String.class);
        assertTrue(MetaObjectUtils.isPrimitive(string));

        assertNull(MOPrimitive.getPrimitive(this.getClass()));
    }
    
    public void testIsSubtype() {
    	MOPrimitive.DATE.isSubtypeOf(MetaObject.ANY_TYPE);

    	MOPrimitive.DATE.isSubtypeOf(MOPrimitive.DATE);
    	MOPrimitive.DATE.isSubtypeOf(MOPrimitive.SQL_DATE);
    	MOPrimitive.DATE.isSubtypeOf(MOPrimitive.SQL_TIME);
    	MOPrimitive.DATE.isSubtypeOf(MOPrimitive.SQL_TIMESTAMP);
    	MOPrimitive.SQL_DATE.isSubtypeOf(MOPrimitive.DATE);
    	MOPrimitive.SQL_DATE.isSubtypeOf(MOPrimitive.SQL_DATE);
    	MOPrimitive.SQL_DATE.isSubtypeOf(MOPrimitive.SQL_TIME);
    	MOPrimitive.SQL_DATE.isSubtypeOf(MOPrimitive.SQL_TIMESTAMP);
    	MOPrimitive.SQL_TIME.isSubtypeOf(MOPrimitive.DATE);
    	MOPrimitive.SQL_TIME.isSubtypeOf(MOPrimitive.SQL_DATE);
    	MOPrimitive.SQL_TIME.isSubtypeOf(MOPrimitive.SQL_TIME);
    	MOPrimitive.SQL_TIME.isSubtypeOf(MOPrimitive.SQL_TIMESTAMP);
    	MOPrimitive.SQL_TIMESTAMP.isSubtypeOf(MOPrimitive.DATE);
    	MOPrimitive.SQL_TIMESTAMP.isSubtypeOf(MOPrimitive.SQL_DATE);
    	MOPrimitive.SQL_TIMESTAMP.isSubtypeOf(MOPrimitive.SQL_TIME);
    	MOPrimitive.SQL_TIMESTAMP.isSubtypeOf(MOPrimitive.SQL_TIMESTAMP);
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMOPrimitive.class);
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
