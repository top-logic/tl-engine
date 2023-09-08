/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.stacktrace.internal;

import junit.framework.TestCase;

import com.top_logic.tool.stacktrace.internal.IntRanges;

/**
 * Test case for {@link IntRanges}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestIntRanges extends TestCase {

	public void testRanges1() {
		IntRanges ranges = new IntRanges();
		ranges.addRange(0, 10);
		ranges.addRange(100, 101);
		ranges.addRange(20, 30);
		ranges.addRange(10, 20);
		
		assertTrue(ranges.contains(0));
		assertTrue(ranges.contains(1));
		assertTrue(ranges.contains(5));
		assertTrue(ranges.contains(28));
		assertTrue(ranges.contains(29));
		assertTrue(!ranges.contains(30));
		assertTrue(!ranges.contains(99));
		assertTrue(ranges.contains(100));
		assertTrue(!ranges.contains(101));
		
		assertEquals("{[0, 29], [100, 100]}", ranges.toString());
	}
	
	public void testRanges2() {
		IntRanges ranges = new IntRanges();
		ranges.addRange(40, 50);
		ranges.addRange(20, 30);
		ranges.addRange(0, 100);
		
		assertEquals("{[0, 99]}", ranges.toString());
	}
	
}
