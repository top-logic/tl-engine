/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Comparator;

import junit.framework.TestCase;

import com.top_logic.basic.col.NullSafeComparator;

/**
 * Test case for {@link NullSafeComparator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestNullSafeComparator extends TestCase {
	
	private static final Comparator<Integer> COMP = new NullSafeComparator<>(new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 < o2) {
				return -1;
			} else if (o1 > o2) {
				return 1;
			} else {
				return 0;
			}
		}
	}, true);

	public void testIt() {
		doTest(0, null, null);
		doTest(-1, null, 0);
		doTest(-1, null, 5);
		doTest(0, 0, 0);
		doTest(-1, 0, 5);
		doTest(0, 5, 5);
	}

	private void doTest(int expected, Integer o1, Integer o2) {
		assertEquals(expected, COMP.compare(o1, o2));
		assertEquals(-expected, COMP.compare(o2, o1));
	}

}
