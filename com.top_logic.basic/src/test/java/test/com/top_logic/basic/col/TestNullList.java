/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.NullList;

/**
 * Testacse for {@link NullList}.
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestNullList extends TestCase {

    /**
     * Test an empty NullList.
     */
    public void testEmpty() {
        NullList empty = new NullList(0);
        assertEquals(0, empty.size());
        try {
            empty.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) { /* expected */ }
        try {
            empty.get(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) { /* expected */ }
    }

    /**
     * Test some "filled" NullList.
     */
    public void testFilled() {
        NullList empty = new NullList(17);
        assertEquals(17, empty.size());
        try {
            empty.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) { /* expected */ }
        try {
            empty.get(17);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) { /* expected */ }
        
        for (Iterator iter = empty.iterator(); iter.hasNext();) {
            assertNull(iter.next());
        }
    }
    
      /** 
     * Return the suite of Test to execute.
     */
    public static Test suite() {
        return new TestSuite(TestNullList.class);
        // return new TestFileManager("testXXX"));
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite ());
    }    


}
