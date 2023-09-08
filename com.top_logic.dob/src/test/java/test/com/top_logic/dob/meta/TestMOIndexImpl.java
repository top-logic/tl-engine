/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dob.meta;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.MOIndex;
import com.top_logic.dob.meta.MOIndexImpl;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * TestCase for {@link com.top_logic.dob.meta.MOIndexImpl}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestMOIndexImpl extends TestCase {

    /**
     * Constructor for TestMOIndexImpl.
     * 
     * @param name function to execute for testing
     */
    public TestMOIndexImpl(String name) {
        super(name);
    }

    /** 
     * Main test for now 
     */
    public void testMain() throws DataObjectException {
        MOClass     moc = new MOClassImpl("moc");
        MOAttribute a1  = new MOAttributeImpl("a1" , MOPrimitive.STRING, MOAttribute.MANDATORY);
        MOAttribute a2  = new MOAttributeImpl("a2" , MOPrimitive.INTEGER);
        moc.addAttribute(a1);
        moc.addAttribute(a2);
        
        DBAttribute idx[] = a1.getDbMapping(); 
		MOIndexImpl moi =
			new MOIndexImpl("idx", "IDX", idx, MOIndex.UNIQUE, DBIndex.CUSTOM, MOIndex.IN_MEMORY, DBIndex.NO_COMPRESS);
        
        assertEquals("idx", moi.getName());
        assertEquals("IDX", moi.getDBName());

        assertTrue(Arrays.deepEquals(idx, moi.getKeyAttributes().toArray()));

        assertTrue  (moi.isUnique  ());
		assertTrue(moi.isCustom());
        assertTrue  (moi.isInMemory());
    }

    /** 
     * Unique indexes require mandatory Attributes. 
     */
    public void testUnique() throws DataObjectException {
        MOClass     moc = new MOClassImpl("moc");
        MOAttribute a1  = new MOAttributeImpl("a1" , MOPrimitive.STRING);
        moc.addAttribute(a1);
        
        DBAttribute idx[] = a1.getDbMapping(); 
        try {
			new MOIndexImpl("idx", "IDX", idx, MOIndex.UNIQUE, DBIndex.CUSTOM, MOIndex.IN_MEMORY, DBIndex.NO_COMPRESS);
			fail();
        } catch (IncompatibleTypeException expected) { /* expected */ } 

        try {
			new MOIndexImpl("idx", "IDX", idx, MOIndex.UNIQUE, !MOIndex.IN_MEMORY, DBIndex.NO_COMPRESS);
            fail();
        } catch (IncompatibleTypeException expected) { /* expected */ } 
        
    }

    /**
     * the suite of Tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestMOIndexImpl.class);
        return suite;
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
