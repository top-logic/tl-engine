/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.IndexedList;
import com.top_logic.basic.col.Mapping;

/**
 * Test case for {@link IndexedList}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestIndexedList extends AbstractIndexedCollectionTest<List<AbstractIndexedCollectionTest.A>> {

	public void testListIndexing() {
		assertEquals(list(), _collection);

		final A a1 = new A("a1");
		final A a2 = new A("a2");
		final A a3 = new A("a3");
		final A a4 = new A("a4");
		final A a5 = new A("a5");
		final A a6 = new A("a6");
		final A a7 = new A("a7");
		final A notAnElement = new A("not-an-element");

		_collection.add(a1);
		assertEquals(list(a1), _collection);
		assertEquals(a1, _index.get(a1.getName()));

		_collection.add(a2);
		assertEquals(list(a1, a2), _collection);
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));

		_collection.add(0, a3);
		assertEquals(list(a3, a1, a2), _collection);
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.addAll(1, list(a4, a5));
		assertEquals(list(a3, a4, a5, a1, a2), _collection);
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertEquals(a4, _index.get(a4.getName()));
		assertEquals(a5, _index.get(a5.getName()));

		_collection.addAll(list(a6, a7));
		assertEquals(list(a3, a4, a5, a1, a2, a6, a7), _collection);
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertEquals(a4, _index.get(a4.getName()));
		assertEquals(a5, _index.get(a5.getName()));
		assertEquals(a6, _index.get(a6.getName()));
		assertEquals(a7, _index.get(a7.getName()));

		_collection.removeAll(set(a4, a5, a6, a7));
		assertEquals(list(a3, a1, a2), _collection);
		assertEquals(a1, _index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));
		assertNull(_index.get(a4.getName()));
		assertNull(_index.get(a5.getName()));
		assertNull(_index.get(a6.getName()));
		assertNull(_index.get(a7.getName()));

		_collection.remove(1);
		assertEquals(list(a3, a2), _collection);
		assertNull(_index.get(a1.getName()));
		assertEquals(a2, _index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.remove(a2);
		assertEquals(list(a3), _collection);
		assertNull(_index.get(a2.getName()));
		assertEquals(a3, _index.get(a3.getName()));

		_collection.remove(notAnElement);
		assertEquals(list(a3), _collection);
		assertEquals(a3, _index.get(a3.getName()));

		_collection.clear();
		assertEquals(list(), _collection);
		assertNull(_index.get(a3.getName()));
	}

	@Override
	protected List<A> createIndexedCollection(Map<String, A> indexMap, Mapping<A, String> keyMapping) {
		return new IndexedList<>(indexMap, keyMapping);
	}
}
