/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.CopyOnChangeListProvider;

/**
 * {@link TestCopyOnChangeList} tests {@link CopyOnChangeListProvider}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCopyOnChangeList extends TestCase {

	public void testAddIfAbsent() {
		CopyOnChangeListProvider<String> l = new CopyOnChangeListProvider<>();
		assertTrue(l.add("first"));
		assertTrue(l.addIfAbsent("second"));
		assertFalse(l.addIfAbsent("first"));

		List<String> current = l.get();
		assertEquals(list("first", "second"), current);
	}

	public void testAdd() {
		CopyOnChangeListProvider<String> l = new CopyOnChangeListProvider<>();
		l.add("first");
		l.add("second");

		List<String> current = l.get();
		assertEquals(list("first", "second"), current);

		l.add("third");
		assertEquals("List was modified", list("first", "second"), current);
		assertEquals("Provider was not updated", list("first", "second", "third"), l.get());
	}

	public void testRemove() {
		CopyOnChangeListProvider<Integer> l = new CopyOnChangeListProvider<>();
		l.add(Integer.valueOf(1));
		l.add(Integer.valueOf(2));
		l.add(null);
		l.add(Integer.valueOf(1));
		l.add(null);

		List<Integer> current = l.get();
		assertEquals(list(1, 2, null, 1, null), current);

		l.remove(Integer.valueOf(1));
		assertEquals("List was modified", list(1, 2, null, 1, null), current);
		assertEquals("List was modified", list(2, null, 1, null), l.get());

		l.remove(Integer.valueOf(3));
		assertEquals("List was modified", list(2, null, 1, null), l.get());

		l.remove(null);
		assertEquals("List was modified", list(2, 1, null), l.get());

		l.remove(null);
		assertEquals("List was modified", list(2, 1), l.get());

	}

}

