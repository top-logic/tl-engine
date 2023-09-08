/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.ConcatenatedIterable;

/**
 * Test case for {@link ConcatenatedIterable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestConcatenatedIterable extends BasicTestCase {

	public void testConcatenation() {
		assertEquals(list(), toList(ConcatenatedIterable.concat(Collections.<Iterable<?>>emptyList())));
		assertEquals(list(), toList(ConcatenatedIterable.concat(list(list()))));
		assertEquals(list("a"), toList(ConcatenatedIterable.concat(list(list("a")))));
		assertEquals(list("a1", "a2"), toList(ConcatenatedIterable.concat(list(list("a1", "a2")))));
		assertEquals(list("a", "b"), toList(ConcatenatedIterable.concat(list(list("a"), list("b")))));
		assertEquals(list("a1", "a2", "b1", "b2"), toList(ConcatenatedIterable.concat(list(list("a1", "a2"), list("b1", "b2")))));
		assertEquals(list("a1", "a2", "b1", "b2"), toList(ConcatenatedIterable.concat(list(
			BasicTestCase.<String>list(), 
			list("a1", "a2"), 
			BasicTestCase.<String>list(), 
			BasicTestCase.<String>list(), 
			list("b1", "b2"), 
			BasicTestCase.<String>list()))));
	}
	
	public void testDynamics() {
		List<List<String>> ll = new ArrayList<>();
		Iterable<String> concatenation = ConcatenatedIterable.concat(ll);
		assertEquals(list(), toList(concatenation));
		
		List<String> l1 = new ArrayList<>();
		ll.add(l1);
		assertEquals(list(), toList(concatenation));
		
		List<String> l2 = new ArrayList<>();
		ll.add(l2);
		assertEquals(list(), toList(concatenation));
		
		List<String> l3 = new ArrayList<>();
		ll.add(l3);
		assertEquals(list(), toList(concatenation));
		
		l3.add("s3");
		assertEquals(list("s3"), toList(concatenation));

		l2.add("s2");
		assertEquals(list("s2", "s3"), toList(concatenation));
		
		l1.add("s1");
		assertEquals(list("s1", "s2", "s3"), toList(concatenation));
		
		l2.add("s22");
		assertEquals(list("s1", "s2", "s22", "s3"), toList(concatenation));
	}
	
	public void testDynamics1() {
		List<String> l1 = new ArrayList<>();
		Iterable<String> concatenation = ConcatenatedIterable.concat(list(l1));
		assertEquals(list(), toList(concatenation));
		
		l1.add("s1");
		assertEquals(list("s1"), toList(concatenation));
	}
	
	public void testDynamics2() {
		List<String> l1 = new ArrayList<>();
		List<String> l2 = new ArrayList<>();
		Iterable<String> concatenation = ConcatenatedIterable.concat(list(l1, l2));
		assertEquals(list(), toList(concatenation));
		
		l1.add("s1");
		assertEquals(list("s1"), toList(concatenation));
		
		l2.add("s2");
		assertEquals(list("s1", "s2"), toList(concatenation));
		
		l1.clear();
		assertEquals(list("s2"), toList(concatenation));
	}
	
	
    /**
     * The test suite.
     */
    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConcatenatedIterable.class));
    }

}
