/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.CustomComparator;

import junit.framework.TestCase;

/**
 * Tests {@link CustomComparator}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCustomComparator extends TestCase {

	public void testGeneral() {
		Object wildcard = CustomComparator.wildcard();
		List<Object> customOrder = list(2, 8, wildcard, 9);

		Comparator<Object> comparator = CustomComparator.newCustomComparator(customOrder);

		List<Integer> list = list(8, 5, 9, 2, 3, 156);
		Collections.sort(list, comparator);
		assertEquals(list(2, 8, 5, 3, 156, 9), list);
	}

	public void testConsecutiveWildcard() {
		Object wildcard = CustomComparator.wildcard();

		// Consecutive wild card should not be a problem
		List<Object> customOrder = list(2, 8, wildcard, wildcard, 9);
		Comparator<Object> comparator = CustomComparator.newCustomComparator(customOrder);
		assertNotNull(comparator);
	}

	public void testDuplicateWildcard() {
		Object wildcard = CustomComparator.wildcard();
		// Consecutive wild card should not be a problem
		List<Object> customOrder = list(2, 8, wildcard, "name", wildcard, 9);
		try {
			Comparator<Object> comparator = CustomComparator.newCustomComparator(customOrder);
			fail("In custom order with duplicate wildcard is it not possible to detect correct ordinal of unknown objects.");
			assertNull(comparator);
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}

}

