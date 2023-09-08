/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.knowledge.dummy.DummyKnowledgeObject;
import test.com.top_logic.knowledge.dummy.DummyWrapper;

import com.top_logic.knowledge.wrap.WrapperComparator;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link WrapperComparator} that ensures transitivity of the comparison.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestWrapperComparatorTransitivity extends TestCase {

	public void testTransitivity() {
		Comparator<TLObject> comparator =
			new WrapperComparator("notCare", WrapperComparator.ASCENDING);

		List<TLObject> objs = createTestObjects();

		// May fail with certain JDKs, if comparator violates the transitivity property.
		// Collections.sort(objs, comparator);

		assertTransitivity(comparator, objs);
	}

	public static List<TLObject> createTestObjects() {
		ArrayList<TLObject> objs = new ArrayList<>();
		final int objCnt = 10;
		for (int n = 1; n < objCnt; n++) {
			final int id = objCnt + 1 - n;

			DummyWrapper obj = new DummyWrapper(DummyKnowledgeObject.item("foo")) {
				@Override
				public Object getValue(String attributeName) {
					return "always the same.";
				}
				
				@Override
				public int hashCode() {
					// Produce objects with a wide range of hash codes.
					return Integer.MIN_VALUE + Integer.MAX_VALUE / (objCnt / 2) * id;
				}
			};
			objs.add(obj);
		}
		return objs;
	}

	public static <T> void assertTransitivity(Comparator<T> comparator, List<? extends T> examples) {
		for (int cnt = examples.size(), n = 0; n < cnt; n++) {
			T x = examples.get(n);
			for (int m = 0; m < n; m++) {
				T y = examples.get(m);
				int xy = comparator.compare(x, y);

				T a;
				T b;
				if (xy < 0) {
					a = x;
					b = y;
				} else {
					a = y;
					b = x;
				}

				for (int j = 0; j < m; j++) {
					T c = examples.get(j);

					assert comparator.compare(a, b) <= 0 : "Objects a and b are in ascending order.";
					int ca = comparator.compare(c, a);
					int cb = comparator.compare(c, b);

					if (ca < 0) {
						assertTrue("If c is smaller than a, then it must also be smaller than b.", cb < 0);
					}
					else if (cb > 0) {
						assertTrue("If c is larger than b, then it must also be larger than a.", ca > 0);
					}
				}
			}
		}
	}

}
