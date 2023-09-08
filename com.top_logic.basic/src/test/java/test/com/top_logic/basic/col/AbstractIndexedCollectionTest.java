/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Mapping;

/**
 * Base class for test cases for indexed collections.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractIndexedCollectionTest<C extends Collection<AbstractIndexedCollectionTest.A>> extends
		TestCase {

	protected Map<String, A> _index;

	protected C _collection;

	static class A {

		public static final Mapping<A, String> GET_NAME = new Mapping<>() {
			@Override
			public String map(A input) {
				return input.getName();
			}
		};

		private final String _name;

		public A(String name) {
			_name = name;
		}

		protected String getName() {
			return _name;
		}

		@Override
		public String toString() {
			return "A(" + _name + ")";
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_index = new HashMap<>();
		_collection = createIndexedCollection(_index, A.GET_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		_index = null;
		_collection = null;

		super.tearDown();
	}

	public void testIndexing() {
		assertContents(set());

		final A a1 = new A("a1");
		final A a2 = new A("a2");
		final A a3 = new A("a3");
		final A a4 = new A("a4");
		final A a5 = new A("a5");
		final A a6 = new A("a6");
		final A a7 = new A("a7");
		final A notAnElement = new A("not-an-element");

		_collection.add(a1);
		assertContents(set(a1));
		assertEquals(a1, _index.get(a1.getName()));

		_collection.add(a2);
		assertContents(set(a1, a2));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));

		_collection.add(a3);
		assertContents(set(a3, a1, a2));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.addAll(list(a4, a5));
		assertContents(set(a3, a4, a5, a1, a2));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertEquals(a4, _index.get(a4.getName()));
		assertEquals(a5, _index.get(a5.getName()));

		_collection.addAll(list(a6, a7));
		assertContents(set(a3, a4, a5, a1, a2, a6, a7));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertEquals(a4, _index.get(a4.getName()));
		assertEquals(a5, _index.get(a5.getName()));
		assertEquals(a6, _index.get(a6.getName()));
		assertEquals(a7, _index.get(a7.getName()));

		_collection.retainAll(set(a1, a2, a3, a6, a7, notAnElement));
		assertContents(set(a1, a2, a3, a6, a7));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertNull(_index.get(a4.getName()));
		assertNull(_index.get(a5.getName()));
		assertEquals(a6, _index.get(a6.getName()));
		assertEquals(a7, _index.get(a7.getName()));
		assertNull(_index.get(notAnElement.getName()));

		_collection.removeAll(set(a6, a7));
		assertContents(set(a3, a1, a2));
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertNull(_index.get(a6.getName()));
		assertNull(_index.get(a7.getName()));

		_collection.remove(a1);
		assertContents(set(a3, a2));
		assertNull(_index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.remove(a2);
		assertContents(set(a3));
		assertNull(_index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.remove(notAnElement);
		assertContents(set(a3));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.clear();
		assertContents(set());
		assertNull(_index.get(a3.getName()));
	}

	public void testIterator() {
		final A a1 = new A("a1");
		final A a2 = new A("a2");
		final A a3 = new A("a3");

		_collection.addAll(list(a1, a2, a3));

		HashSet<A> allValues = new HashSet<>();
		Iterator<A> iterator = _collection.iterator();
		while (iterator.hasNext()) {
			A value = iterator.next();
			allValues.add(value);
			if (value.getName().equals(a2.getName())) {
				iterator.remove();
			}
		}
		assertEquals("Not all values reported.", set(a1, a2, a3), allValues);
		assertContents(set(a1, a3));
	}

	public void testDuplicateAdd() {
		final A a1 = new A("a1");
		final A a2 = new A("a2");
		final A a1Clash = new A("a1");

		_collection.add(a1);
		_collection.add(a2);
		assertContents(set(a1, a2));

		try {
			_collection.add(a1Clash);
			fail("Indexed collection must reject a duplicate add.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	/**
	 * Create the indexed collection under test.
	 */
	protected abstract C createIndexedCollection(Map<String, A> indexMap, Mapping<A, String> keyMapping);

	protected void assertContents(Set<?> expectedContents) {
		BasicTestCase.assertEquals("Contents missmatch.", expectedContents, new HashSet<>(_collection));
		BasicTestCase.assertEquals("Index values missmatch.", expectedContents, new HashSet<>(_index.values()));
	}

}
