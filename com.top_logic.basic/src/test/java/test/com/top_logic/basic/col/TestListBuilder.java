/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ListBuilder;

/**
 * Test the {@link ListBuilder}. 
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestListBuilder extends TestCase {

    /**
     * This test should result in a complete coverage.
     */
	public void testMain() {
	    ListBuilder lbd = new ListBuilder();
	    assertSame(lbd, lbd.add(this));
        assertTrue(lbd.toList().contains(this));
        assertTrue(lbd.toList().contains(this));
        assertSame(lbd, lbd.add("That"));
        assertTrue(lbd.toList().contains(this));
        assertTrue(lbd.toList().contains("That"));
	}
	
	/**
	 * Provide an Example of some reasonable usage.
	 */
	public void testUsage() {
	    List result = new ListBuilder()
	        .add("Eins").add("Zwei").add("Drei").add("Vier").toList();
	    assertEquals(4, result.size());
	}

	
    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite(TestListBuilder.class);
    }

}
