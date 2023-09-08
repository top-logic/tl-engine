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
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;

/**
 * testCase for {@link com.top_logic.dob.attr.MOAttributeImpl}
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestMOAttributeImpl extends TestCase {

    /**
     * Constructor for TestMOAttributeImp.
     * 
     * @param name fucntion to execute for testing
     */
    public TestMOAttributeImpl(String name) {
        super(name);
    }

    /** Main testcase for now */
    public void testMain() {
        MOAttributeImpl moa = new MOAttributeImpl(
            "simple", MOPrimitive.STRING);
            
        assertEquals("simple"           ,moa.getName());
        assertEquals(MOPrimitive.STRING ,moa.getMetaObject()); 
        assertTrue  (!moa.isMandatory());

        moa = new MOAttributeImpl(
            "mandatory", MOPrimitive.INTEGER, MOAttribute.MANDATORY);            
            
        assertEquals("mandatory",moa.getName());
        assertEquals(MOPrimitive.INTEGER ,moa.getMetaObject()); 
        assertTrue  (moa.isMandatory());

        moa = new MOAttributeImpl(
            "immutable", MOPrimitive.BOOLEAN, 
            !MOAttribute.MANDATORY, MOAttribute.IMMUTABLE);

        assertEquals("immutable",moa.getName());
        assertEquals(MOPrimitive.BOOLEAN ,moa.getMetaObject()); 
        assertTrue  (!moa.isMandatory());
        assertTrue  (moa.isImmutable());
        
        moa = new MOAttributeImpl(
            "dabAttribute", "Dont Do This !",MOPrimitive.LONG, 
             MOAttribute.MANDATORY, !MOAttribute.IMMUTABLE,
			DBType.LONG, 10, 2);
             
        assertEquals("dabAttribute"     ,moa.getName());
        assertEquals("Dont Do This !"   ,moa.getDBName());
        assertEquals(MOPrimitive.LONG   ,moa.getMetaObject()); 
        assertTrue  (moa.isMandatory());
        assertTrue  (!moa.isImmutable());
        assertEquals(DBType.LONG, moa.getSQLType());
        assertEquals(10          , moa.getSQLSize());
        assertEquals(2           , moa.getSQLPrecision());

        moa.setDBName("BETTER_THIS_WAY");
        assertEquals("BETTER_THIS_WAY"   ,moa.getDBName());

        moa.setMandatory(false);
        assertTrue  (!moa.isMandatory());

        moa.setImmutable(true);
        assertTrue  (moa.isImmutable());

		moa.setSQLType(DBType.DECIMAL);
		assertEquals(DBType.DECIMAL, moa.getSQLType());
        
        moa.setSQLSize(42);
        assertEquals(42          , moa.getSQLSize());

        moa.setSQLPrecision(32);
        assertEquals(32          , moa.getSQLPrecision());
    }

    /*
     * Test for MOAttributeImpl makeAttribute(String, xxx).
     */
    public void testMakeAttributeStringObject() {
        MOAttributeImpl.makeAttribute("someName", Boolean.class);
        MOAttributeImpl.makeAttribute("someName", "");

        try {
			MOAttributeImpl.makeAttribute("someName", this.getClass());
			fail("Unsupported type, illegal argument exception expected.");
		} catch (IllegalArgumentException e) {
			// Expected.
		}
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMOAttributeImpl.class);
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
