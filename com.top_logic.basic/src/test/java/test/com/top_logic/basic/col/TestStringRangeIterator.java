/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.StringRangeIterator;

/**
 * This class tests the class {@link com.top_logic.basic.col.StringRangeIterator}.
 * 
 * @author     <a href="mailto:tdi@top-logic.com">tdi</a>
 */
public class TestStringRangeIterator extends BasicTestCase {

    /**
     * This method starts all tests for the class
     * {@link com.top_logic.basic.col.StringRangeIterator}.
     */
    public void testAll(){
        String[]            strings  = new String[]{"Was", "soll", "ich", "nehmen"};
        StringRangeIterator iterator  = new StringRangeIterator(strings);
        assertNotNull(iterator);
        
        ArrayList           list     = new ArrayList();
        StringRangeIterator iterator2 = new StringRangeIterator(list);
        assertNotNull(iterator2);
        
        String first  = (String)iterator.nextObject();
        assertNotNull(first);
        assertEquals(first,  iterator.getIDFor(first));
        assertEquals(first,  iterator.getUIStringFor(first));
        
        String second = (String)iterator.nextObject();
        assertNotNull(second);
        assertEquals(second, iterator.getIDFor(second));
        assertEquals(second, iterator.getUIStringFor(second));
        
        String third  = (String)iterator.nextObject(); 
        assertNotNull(third);
        assertEquals(third,  iterator.getIDFor(third));
        assertEquals(third,  iterator.getUIStringFor(third));
        
        String fourth = (String)iterator.nextObject(); 
        assertNotNull(fourth);
        assertNull(iterator.nextObject());
        assertEquals(fourth, iterator.getIDFor(fourth));
        assertEquals(fourth, iterator.getUIStringFor(fourth));
        
        assertNull(iterator.nextObject());
        assertNull(iterator.nextObject());
        
        try {
            iterator.getIDFor(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            /* expected */
        }
        try {
            iterator.getIDFor("notThere");
            fail();
        }
        catch (IllegalArgumentException e) {
            /* expected */
        }
        
        try {
            iterator.getUIStringFor(null);
            fail();
        }
        catch (IllegalArgumentException e) {
            /* expected */
        }
        try {
            iterator.getUIStringFor("notThereToo");
            fail();
        }
        catch (IllegalArgumentException e) {
            /* expected */
        }
        
    }
    
    /**
     * Returns a suite of tests.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStringRangeIterator.class));
    }

    /**
     * This main function is for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }    
    
}
