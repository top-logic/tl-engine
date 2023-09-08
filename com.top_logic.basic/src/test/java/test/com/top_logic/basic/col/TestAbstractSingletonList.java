/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.AbstractSingletonList;

/**
 * The class {@link TestAbstractSingletonList} tests functionality of {@link AbstractSingletonList}
 * 
 * @since 5.8.0
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestAbstractSingletonList extends TestCase {

	private List<Object> _list;

	Object _content;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_content = "content";
		_list = new AbstractSingletonList<>() {

			@Override
			protected Object internalGet() {
				return _content;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		_list = null;
		super.tearDown();
	}

	/**
	 * Tests {@link List#containsAll(java.util.Collection)}
	 */
	public void testContainsAll() {
		assertTrue(_list.containsAll(Collections.emptyList()));
		assertTrue(_list.containsAll(Collections.singletonList(_content)));
		assertFalse(_list.containsAll(Collections.singletonList("not contained")));
		assertFalse(_list.containsAll(Arrays.asList(_content, "not_contained")));
	}

	/**
	 * Tests {@link List#indexOf(Object)}
	 */
	public void testIndexOf() {
		assertEquals(0, _list.indexOf(_content));
		assertEquals(-1, _list.indexOf("not contained"));
		assertEquals(-1, _list.indexOf(null));
	}

	/**
	 * Tests {@link List#contains(Object)}
	 */
	public void testContains() {
		assertTrue(_list.contains(_content));
		assertFalse(_list.contains("not contained"));
	}

	/**
	 * Tests {@link List#get(int)}
	 */
	public void testGet() {
		try {
			_list.get(-1);
			fail("No element with index -1");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		try {
			_list.get(1);
			fail("No element with index 1");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		assertEquals(_content, _list.get(0));
		assertFalse(_list.contains("not contained"));
	}

}

