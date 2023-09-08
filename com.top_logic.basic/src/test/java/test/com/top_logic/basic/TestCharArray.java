/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Random;

import junit.framework.TestCase;

import com.top_logic.basic.CharArray;

/**
 * Test for {@link CharArray}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCharArray extends TestCase {

	public void testLength() {
		char[] source = newSource(15);
		assertEquals(15, new CharArray(source).length());
		assertEquals(12, new CharArray(source, 3, 12).length());
		assertEquals(0, new CharArray(source, 3, 0).length());
	}

	public void testSubSequence() {
		char[] source = newSource(15);
		CharArray charArray = new CharArray(source);
		assertValueEquals(source, charArray.subSequence(0, 15), 0, source.length);
		assertValueEquals(source, charArray.subSequence(0, 13), 0, 13);
		assertValueEquals(source, charArray.subSequence(3, 15), 3, 12);
		assertValueEquals(source, charArray.subSequence(13, 13), 13, 0);
		assertValueEquals(source, charArray.subSequence(3, 5), 3, 2);
	}

	public void testCharAt() {
		char[] source = newSource(15);
		CharArray charArray = new CharArray(source);
		assertValueEquals(source, charArray, 0, source.length);

		try {
			charArray.charAt(-1);
			fail("Negative index");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		try {
			charArray.charAt(charArray.length());
			fail("Index to large");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
		try {
			charArray.subSequence(2, 3).charAt(7);
			fail("Index larger than length");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
	}

	private void assertValueEquals(char[] source, CharSequence sequence, int offset, int length) {
		assertEquals(length, sequence.length());
		for (int i = 0; i < length; i++) {
			assertEquals(source[offset + i], sequence.charAt(i));
		}
	}

	private char[] newSource(int size) {
		char[] source = new char[size];
		fillRandomDataData(source);
		return source;
	}

	private void fillRandomDataData(char[] source) {
		Random r = new Random(47);
		for (int i = 0; i < source.length; i++) {
			source[i] = (char) r.nextInt();
		}
	}

	@SuppressWarnings("unused")
	public void testConstructors() {
		char[] source = new char[15];
		new CharArray(source);
		new CharArray(source, 3, 12);
		try {
			new CharArray(source, -1, 3);
			fail("Offset must not be negative");
		} catch (IndexOutOfBoundsException ex) {
			// expected;
		}
		try {
			new CharArray(source, 3, -1);
			fail("Length must not be negative");
		} catch (IndexOutOfBoundsException ex) {
			// expected;
		}
		try {
			new CharArray(source, 3, -1);
			fail("Length must not be negative");
		} catch (IndexOutOfBoundsException ex) {
			// expected;
		}
		try {
			new CharArray(source, 3, 15);
			fail("Array to small");
		} catch (IndexOutOfBoundsException ex) {
			// expected;
		}
		try {
			new CharArray(source, 17, 3);
			fail("Array to small");
		} catch (IndexOutOfBoundsException ex) {
			// expected;
		}
	}

	public void testModifiable() {
		char[] source = newSource(15);
		CharArray charArray = new CharArray(source);
		assertValueEquals(source, charArray, 0, 15);
		fillRandomDataData(source);
		assertValueEquals(source, charArray, 0, 15);
	}

}

