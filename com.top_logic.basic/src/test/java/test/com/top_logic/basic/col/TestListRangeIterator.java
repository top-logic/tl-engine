/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ListRangeIterator;

/**
 * Test the {@link ListRangeIterator}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestListRangeIterator extends TestCase {

    public TestListRangeIterator(String name) {
        super(name);
    }

    /**
     * Test Using an empty List.
     */
    public void testEmpty() {
        
        ListRangeIterator lri = new TestedListRangeIterator(Collections.EMPTY_LIST);
        assertNull(lri.nextObject());
        lri.reset();
        assertNull(lri.nextObject());
        assertEquals(0, lri.createCoords().length);
    }

    /**
     * Test with a singleton List
     */
    public void testNextObject() {
        ListRangeIterator lri = new TestedListRangeIterator(
                   Collections.singletonList(this));
        assertSame(this, lri.nextObject());
        assertNull(lri.nextObject());
        lri.reset();
        assertSame(this, lri.nextObject());
        assertNull(lri.nextObject());
        assertEquals(1, lri.createCoords().length);
    }

    /**
     * Implement IDs by using {@link Object#toString()}.
     */
    static class TestedListRangeIterator extends ListRangeIterator {

         public TestedListRangeIterator(List aList) {
            super(aList);
        }

        @Override
		public Object getIDFor(Object obj) {
            return obj.toString();
        }

        @Override
		public String getUIStringFor(Object obj) {
			return obj.toString();
        } 
        
    }
    
    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite(TestListRangeIterator.class);
    }


}

