/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.TransformIterator;

/**
 * Tests the {@link TransformIterator}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestTransformIterator extends TestCase {

	/**
	 * Tests {@link Iterator#remove()} to work correctly.
	 */
	public void testRemove() {
		List<Integer> l = new ArrayList<>(list(1, 9, 2, 8, 3));
		Iterator<Integer> source = l.iterator();
		FilterIterator<Integer> iterator = new FilterIterator<>(source, new Filter<Integer>() {

			@Override
			public boolean accept(Integer anObject) {
				return anObject > 5;
			}
		});

		assertTrue(iterator.hasNext());
		assertEquals(Integer.valueOf(9), iterator.next());
		iterator.remove();
		assertTrue(iterator.hasNext());
		assertEquals(Integer.valueOf(8), iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
		assertEquals(list(1, 2, 3), l);

		try {
			iterator.remove();
			fail("There is no next element in iterator.");
		} catch (Exception ex) {
			// Expected.
		}
		assertEquals("Source must not be changed during problem.", list(1, 2, 3), l);
	}

}

