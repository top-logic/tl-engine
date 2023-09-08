/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.bean;

import java.beans.IntrospectionException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.bean.BeanDataObject;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;

/**
 * Testcase for {@link com.top_logic.dob.bean.BeanDataObject}.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestBeanDataObject extends TestCase implements BeanInterface {

	/** Attribute used for using this class as a Bean */
	int		anInt	= 40733;

	/** Attribute used for using this class as a Bean */
	String	aString = "Testing";

    /**
     * Empty Constructor neede for Bean pattern
     */
    public TestBeanDataObject () {
        super ("used by BeandataObjectFactory");
    }

    /**
     * Default Constructor.
     *
     * @param name  name of testFunction to perform. 
     */
    public TestBeanDataObject (String name) {
        super (name);
    }

	/** A indexed accessor method to Test the BeanMO */
	public int getIndexed(int ind) {
		return 4711;	
	}

	/** A simple accessor method to Test the BeanDO */
	public int getInt() {
		return anInt;	
	}

	/** A simple accessor method to Test the BeanDO */
	public void setInt(int i) {
		anInt = i;	
	}

	/** A simple accessor method to Test the BeanDO */
	@Override
	public String getString() {
		return aString;
	}

	/** A simple accessor method to Test the BeanDO */
	@Override
	public void setString(String s) {
		aString = s;
	}

    /** This should result in a Security Exception */
    @Override
	public String getNothing() {
        throw new NullPointerException("Access Denied");
    }

    /**
     * Main testcase for now.
     */
    public void testMain() throws Exception {
    	BeanDataObject bdo = new BeanDataObject(this);
		MetaObject 	   mo  = bdo.tTable();

		assertTrue(bdo.isInstanceOf(mo));
		assertTrue(bdo.isInstanceOf(this.getClass().getName()));

		// Just call these since implementation is trivial (for now)
		assertNotNull(bdo.getAttributes());
		
		assertEquals(Integer.valueOf(anInt), bdo.getAttributeValue("int"));
		assertEquals(aString           ,bdo.getAttributeValue("string"));

        try{		
            bdo.getAttributeValue("nothing");
            fail("Expected NoSuchAttributeException");
        }
        catch (NoSuchAttributeException expected) { /* expected */ }

		bdo.setAttributeValue("int", Integer.valueOf(111222));
		assertEquals(111222, anInt);

		bdo.setAttributeValue("string", "hey thats hot !");
		assertEquals("hey thats hot !", aString);

        try{        
            bdo.setAttributeValue("int", Boolean.TRUE);
            fail("Expected IncompatibleTypeException");
        }
        catch (IncompatibleTypeException expected) { /* expected */ }

        try{        
            bdo.setAttributeValue("nothing", "Is nich");
            fail("Expected NoSuchAttributeException");
        }
        catch (NoSuchAttributeException expected) { /* expected */ }
    }

    /**
     * test createing a BeanDataObject with some interface.
     */
    public void testInterface() throws Exception {

        try {        
            new BeanDataObject(this, Comparable.class);
            fail("Inteface does not match");
        }
        catch (IntrospectionException expected) { /* expected */ }
        
        BeanDataObject bdo = new BeanDataObject(this, BeanInterface.class);
        MetaObject     mo  = bdo.tTable();

        assertTrue(bdo.isInstanceOf(mo));
        assertTrue(bdo.isInstanceOf(this.getClass().getName()));

        // Just call these since implementation is trivial (for now)
        assertNotNull(bdo.getAttributes());
        
        try {        
            bdo.getAttributeValue("int");
            fail("Inteface does not support get/setInt()");
        }
        catch (NoSuchAttributeException expected) { /* expected */ }
        
        assertEquals(aString           ,bdo.getAttributeValue("string"));

        try {
			bdo.setAttributeValue("int", Integer.valueOf(111222));
            fail("Inteface does not support get/setInt()");
        }
        catch (NoSuchAttributeException expected) { /* expected */ }

        bdo.setAttributeValue("string", "hey thats cool !");
        assertEquals("hey thats cool !", aString);
    }
    
    /**
     * Testcases to cover the lines not covered elsewheer
     * 
     * #author BHA/KHA
     */
    public void testTheLastLinie() throws Exception {
    	BeanDataObject bdo = new BeanDataObject(this);
    	
		bdo.setIdentifier(StringID.valueOf("BlahBlah")); // another noop
        assertNotNull(bdo.getIdentifier());
        
        // bdo.isInstanceOf with null is not allowed
        // boolean test = bdo.isInstanceOf((String)null);
    	// assertFalse ("Error by isInstanceOf(String)!", test);
    	
        try  {
            bdo.getAttributeValue((String)null);
            fail("Expected NoSuchAttributeException");
        } catch (NoSuchAttributeException expected) { /* expected */ } 
    	
        try  {
            bdo.setAttributeValue("test", bdo);
            fail("Expected NoSuchAttributeException");
        } catch (NoSuchAttributeException expected) { /* expected */ } 

        try  {
            bdo.getAttributeValue("test");
            fail("Expected NoSuchAttributeException");
        } catch (NoSuchAttributeException expected) { /* expected */ } 
    	
    }

    /**
     * the suite of tests to perform.
     */
    public static Test suite () {
        // return new TestBeanDataObject("testInterface");
        return new TestSuite (TestBeanDataObject.class);
    }


    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
