/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.bean;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.dob.DOBTestSetup;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.bean.BeanMetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Test the {@link com.top_logic.dob.bean.BeanMetaObject}.
 *
 * @author  <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class TestBeanMetaObject extends BasicTestCase {

    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestBeanMetaObject (String name) {
        super (name);
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        return DOBTestSetup.createDOBTestSetup(new TestSuite (TestBeanMetaObject.class));
    }

	/** A simple accessor method to Test the BeanMO */
	public int getInt() {
		return 4711;	
	}

	/** A indexed accessor method to Test the BeanMO */
	public int getIndexed(int ind) {
		return 4711;	
	}

	/** A simple accessor method to Test the BeanMO */
	public String getString() {
		return "A simple accessor method to Test the BeanMO";
	}

    /**
     * Main tescase for now.
     */
    public void testMain() throws Exception {
    	BeanMetaObject bmo = new BeanMetaObject(this.getClass());
    	
    	MOClass superMO = bmo.getSuperclass();
    	assertEquals(this.getClass().getSuperclass().getName(),
		    		 superMO.getName());
		    		 
		MOAttribute intAttr = bmo.getAttribute("int");
		MOAttribute strAttr = bmo.getAttribute("string");
		    		 
		assertTrue(bmo.getDeclaredAttributes().contains(intAttr));
		assertTrue(bmo.isInherited(superMO));
		assertTrue(bmo.isInherited(superMO.getName()));
		assertTrue(bmo.getAttributes().contains(strAttr));
		assertTrue(bmo.hasAttribute("int"));
		assertTrue(bmo.hasAttribute("string"));
		
		assertTrue(Arrays.asList(bmo.getAttributeNames())
					.contains("string"));
    }

    /**
     * Test DOFactory part of BeanMetaObejct.
     */
    public void testDOFactory() throws Exception {
        BeanMetaObject bmo = new BeanMetaObject(this.getClass());
        
        // Must fail since this class has no default CTor
        try {
            bmo.createEmptyObject();
            fail("Cannot create empty object");
        } catch (UnknownTypeException expected) { /* expected */  }
            
        bmo = new BeanMetaObject(TestBeanDataObject.class);
        bmo.createEmptyObject();
    }

    /**
     * Test some (more or less) trivial fucntions.
     */
    public void testTrivial() throws Exception {
        BeanMetaObject bmo = new BeanMetaObject(this.getClass());
        
        bmo.setSuperclass(null); // actually a noop
        
        assertNull(bmo.createDOCollection("Blah"));
        assertNull(bmo.createMOStructure ());
        assertTrue(bmo.getIndexes().isEmpty());

        assertTrue(MetaObjectUtils.isClass(bmo));
        assertTrue(bmo.isSubtypeOf(bmo));
        assertTrue(bmo.isSubtypeOf(this.getClass().getName()));

        assertTrue(!MetaObjectUtils.isCollection(bmo));
        assertTrue(!MetaObjectUtils.isPrimitive(bmo));
        
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
