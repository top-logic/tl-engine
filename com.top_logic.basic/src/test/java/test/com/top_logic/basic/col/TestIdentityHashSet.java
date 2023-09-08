/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.IdentityHashMap;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.col.IdentityHashSet;

/**
 * Tests for {@link IdentityHashMap}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestIdentityHashSet extends TestCase {

	/**
	 * Tests that an {@link IdentityHashSet} may contain different equal objects.
	 */
	public void testIdenitity() {
		Set<String> set = new IdentityHashSet<>();

		assertTrue(set.isEmpty());

		// Intentional constructor use
		String someString = new String("S15");
		String equalString = new String("S15");

		set.add(someString);
		assertEquals(1, set.size());

		set.add(someString);
		assertEquals(1, set.size());

		assertEquals("Test for identity equality needs equal objects.", someString, equalString);
		assertNotSame("Test for identity equality needs equal objects.", someString, equalString);
		set.add(equalString);
		assertEquals(2, set.size());
	}

}
