/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Comparator;

import junit.framework.TestCase;

import com.top_logic.basic.Named;
import com.top_logic.basic.NamedComparator;

/**
 * Test for {@link NamedComparator}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestNamedComparator extends TestCase {

	/**
	 * Simple test implementation of {@link Named}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class NamedImpl implements Named {

		private final String _name;

		public NamedImpl(String name) {
			_name = name;
		}

		@Override
		public String getName() {
			return _name;
		}

	}

	public void testCompare() {
		assertEquals(-1, compare("a", "ab"));
		assertEquals(0, compare("a", "a"));
		assertEquals(0, compare("abc", "abc"));
		assertEquals(-1, compare("a", "b"));
		assertEquals(1, compare("b", "a"));
		assertEquals(1, compare("ab", "a"));
	}

	public void testCompareWithNull() {
		assertEquals(-1, compare(null, "ab"));
		assertEquals(0, compare(null, null));
		assertEquals(1, compare("b", null));
	}

	public void testCompareNullLast() {
		assertEquals(-1, compareNullLast("b", null));
		assertEquals(0, compareNullLast(null, null));
		assertEquals(1, compareNullLast(null, "d"));
	}

	private int compare(String name1, String name2) {
		return compare(NamedComparator.INSTANCE, name1, name2);
	}

	private int compareNullLast(String name1, String name2) {
		return compare(NamedComparator.NullLast.INSTANCE, name1, name2);
	}

	private int compare(Comparator<? super Named> comparator, String name1, String name2) {
		NamedImpl o1 = new NamedImpl(name1);
		NamedImpl o2 = new NamedImpl(name2);
		int value = comparator.compare(o1, o2);
		if (value == 0) {
			assertTrue(comparator.reversed().compare(o1, o2) == 0);
		} else if (value > 0) {
			assertTrue(comparator.reversed().compare(o1, o2) < 0);
		} else {
			assertTrue(comparator.reversed().compare(o1, o2) > 0);
		}
		return value;
	}

}

