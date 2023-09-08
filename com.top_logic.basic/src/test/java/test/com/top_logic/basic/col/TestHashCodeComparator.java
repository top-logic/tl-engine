/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.HashCodeComparator;

/**
 * BasicTestCase for {@link HashCodeComparator}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestHashCodeComparator extends TestCase {

	public void testEquality() {
		Object o1 = new TestObject(0);
		Object o2 = new TestObject(0);
		assertEquals("Same hash code must be treat as equal!", 0, HashCodeComparator.INSTANCE.compare(o1, o2));
	}

	public void testGreaterThan() {
		Object o1 = new TestObject(1);
		Object o2 = new TestObject(0);
		assertIsGreater(o1, o2);
	}

	public void testLowerThan() {
		Object o1 = new TestObject(0);
		Object o2 = new TestObject(1);
		assertIsLower(o1, o2);
	}

	public void testTransitivity() {
		Object o1 = new TestObject(0);
		Object o2 = new TestObject(1);
		Object o3 = new TestObject(2);

		assertIsLower(o1, o2);
		assertIsLower(o2, o3);
		assertIsLower(o1, o3);

		assertIsGreater(o2, o1);
		assertIsGreater(o3, o2);
		assertIsGreater(o3, o1);
	}

	private void assertIsGreater(Object o1, Object o2) {
		int result = HashCodeComparator.INSTANCE.compare(o1, o2);
		assertTrue("Object with greater hash code must be treated as greater object!", result > 0);
	}

	private void assertIsLower(Object o1, Object o2) {
		int result = HashCodeComparator.INSTANCE.compare(o1, o2);
		assertTrue("Object with lower hash code must be treated as lower object!", result < 0);
	}

	public static Test suite() {
		return new TestSuite(TestHashCodeComparator.class);
	}

	static class TestObject extends Object {
		private int _hashCode;

		public TestObject(int hashCode) {
			_hashCode = hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			return this.hashCode() == obj.hashCode();
		}

		@Override
		public int hashCode() {
			return _hashCode;
		}
	}
}
