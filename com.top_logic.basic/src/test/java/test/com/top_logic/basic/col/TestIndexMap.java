/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;
import test.com.top_logic.basic.col.AbstractIndexedCollectionTest.A;

import com.top_logic.basic.col.IndexMap;
import com.top_logic.basic.col.IndexedCollection;

/**
 * Test case for {@link IndexMap}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestIndexMap extends TestCase {

	public void testCheck() {
		A a1 = new A("a1");
		A a2 = new A("a2");
		A a3 = new A("a3");
		A a3Clash = new A("a3");
		A a4 = new A("a4");

		IndexMap<String, A> map = new IndexMap<>(A.GET_NAME);

		IndexedCollection<String, A> indexedCollection = new IndexedCollection<>(map, A.GET_NAME);

		indexedCollection.add(a1);
		indexedCollection.add(a2);
		indexedCollection.add(a3);

		try {
			indexedCollection.add(a3Clash);
			fail("Must prevent clash.");
		} catch (RuntimeException ex) {
			// Expected.
		}

		try {
			map.put("not-a4", a4);
			fail("Must prevent clash.");
		} catch (RuntimeException ex) {
			// Expected.
		}

	}

}
