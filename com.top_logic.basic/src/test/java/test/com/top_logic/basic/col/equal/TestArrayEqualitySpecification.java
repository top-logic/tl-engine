/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.equal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.equal.ArrayEqualitySpecification;
import com.top_logic.basic.col.equal.EqualitySpecification;

/**
 * Test for {@link ArrayEqualitySpecification}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestArrayEqualitySpecification extends TestCase {

	private EqualitySpecification<Object> _specification = ArrayEqualitySpecification.INSTANCE;

	public void testSimple() {
		assertFalse(_specification.equals(null, new String[] {}));
		assertFalse(_specification.equals(new String[] {}, null));
		Object o = new String[] {};
		assertTrue(_specification.equals(o, o));
		o = new int[] { 1, 2 };
		assertTrue(_specification.equals(o, o));
	}

	public void testObjectArray() {
		assertEquals(true, new String[] { "a" }, new String[] { "a" });
		assertEquals(true, new String[] { "a" }, new Object[] { "a" });
		assertEquals(false, new String[] { "a" }, new String[] { "b" });
		assertEquals(false, new String[] { "a" }, new String[] { "a", "b" });
		assertEquals(false, new String[] { "7" }, new Integer[] { 7 });
	}

	private void assertEquals(boolean expectEquals, Object left, Object right) {
		if (expectEquals) {
			assertTrue(_specification.equals(left, right));
			assertTrue(_specification.equals(right, left));
		} else {
			assertFalse(_specification.equals(left, right));
			assertFalse(_specification.equals(right, left));
		}
	}

	public void testPrimitiveArray() {
		assertEquals(true, new boolean[] { true }, new boolean[] { true });
		assertEquals(true, new int[] { 7 }, new int[] { 7 });
		assertEquals(true, new long[] { 7, 6 }, new long[] { 7, 6 });
		assertEquals(true, new char[] { '.' }, new char[] { '.' });
		assertEquals(true, new byte[] { 34 }, new byte[] { 34 });
		assertEquals(true, new short[] { 34 }, new short[] { 34 });
		assertEquals(true, new double[] { 34.45d }, new double[] { 34.45d });
		assertEquals(true, new float[] { 34.45f }, new float[] { 34.45f });

		assertEquals(false, new long[] { '.' }, new char[] { '.' });
		assertEquals(false, new int[] { 7, 6 }, new short[] { 7, 6 });
		assertEquals(false, new double[] { 7.5F }, new float[] { 7.5F });
		assertEquals(false, new Boolean[] { true }, new boolean[] { true });
		assertEquals(false, new String[] { "7" }, new byte[] { 7 });
	}

	public void testDeepEquality() {
		assertEquals(true,
			new Object[] { new Boolean[] { true }, new Object[] { new int[] { 5, 6 }, StringServices.EMPTY_STRING } },
			new Object[] { new Boolean[] { true }, new Object[] { new int[] { 5, 6 }, StringServices.EMPTY_STRING } });
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestArrayEqualitySpecification}.
	 */
	public static Test suite() {
		return new TestSuite(TestArrayEqualitySpecification.class);
	}

}

