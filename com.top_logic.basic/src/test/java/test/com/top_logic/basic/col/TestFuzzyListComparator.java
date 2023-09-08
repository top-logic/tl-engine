/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Comparator;

import junit.framework.TestCase;

import com.top_logic.basic.col.FuzzyListComparator;

/**
 * Test for the {@link FuzzyListComparator}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFuzzyListComparator extends TestCase {
	
	private Comparator<Object> _comp = new FuzzyListComparator() {

		@Override
		protected int elementCompare(Object o1, Object o2) {
			return ((Integer) o1).compareTo((Integer) o2);
		}
	};
	
	public void testListObjectCompare() {
		assertCompareEquals("Objst is treated as singleton list.", list(1), 1);
		assertLess("Singleton list entry less than object.", list(1), 2);
		assertGreater("Entry is grater than object", list(2), 1);

		assertLess("Empty list less than any object.", list(), 0);

		assertGreater("First entry is equal but lenth is greater than on2.", list(0, 1), 0);
		assertGreater("First entry is greater.", list(2, 1), 0);
		assertLess("First list entry less than object.", list(2, 1), 3);
	}

	public void testObjectObjectCompare() {
		assertCompareLess("First is less than second.", 1, 3);
		Integer elem = Integer.valueOf(1);
		assertCompareEquals("Elements are equal.", elem, elem);
	}

	public void testListNullCompare() {
		assertCompareEquals("List equal null, therefore 'compare equal'.", null, list());
		assertLess("List equal null, therefore 'compare equal'.", list(), 2);
		assertLess("null equals list() < 2, therfore null < 2", null, 2);
		assertLess("First list entry less than object.", null, list(2));
	}

	public void testListListCompare() {
		assertCompareEquals("List are equal, therefore 'compare equal'.", list(1, 2, 3), list(1, 2, 3));
		assertLess("List are equal up to its min length, first list is smaller", list(1, 2, 3), list(1, 2, 3, 4));
		assertLess("First not equal element is less in first list", list(1, 2, 3, 4), list(1, 3, 4));
	}

	private void assertCompareEquals(String msg, Object o1, Object o2) {
		assertTrue(msg, _comp.compare(o1, o2) == 0);
		assertTrue("Comparator is symmetric", _comp.compare(o2, o1) == 0);
		assertTrue("Element " + o1 + " is equal to itself", _comp.compare(o1, o1) == 0);
		assertTrue("Element " + o2 + " is equal to itself", _comp.compare(o2, o2) == 0);
	}

	private void assertLess(String msg, Object o1, Object o2) {
		assertCompareLess(msg, o1, o2);
		assertCompareGreater("Inverted compare: " + msg, o2, o1);
	}

	private void assertGreater(String msg, Object o1, Object o2) {
		assertCompareGreater(msg, o1, o2);
		assertCompareLess("Inverted compare: " + msg, o2, o1);
	}

	private void assertCompareLess(String msg, Object o1, Object o2) {
		assertTrue(msg, _comp.compare(o1, o2) < 0);
	}

	private void assertCompareGreater(String msg, Object o1, Object o2) {
		assertTrue(msg, _comp.compare(o1, o2) > 0);
	}

}

