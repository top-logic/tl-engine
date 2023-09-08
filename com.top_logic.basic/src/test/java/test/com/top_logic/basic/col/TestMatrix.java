/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;

import com.top_logic.basic.col.Matrix;

/**
 * Test case for {@link Matrix}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMatrix extends TestCase {

	public void testMatrix() {
		Matrix<String> matrix = new Matrix<>(2, 3);
		assertEquals(2, matrix.getRows());
		assertEquals(3, matrix.getColumns());
		assertNull(matrix.get(0, 0));
		assertContents("[[null,null,null],[null,null,null]]", matrix);

		matrix.fill("A");
		assertEquals("A", matrix.get(0, 0));
		assertEquals("A", matrix.get(1, 2));
		assertContents("[[A,A,A],[A,A,A]]", matrix);

		matrix.fillRow(1, "B");
		assertEquals("A", matrix.get(0, 0));
		assertEquals("B", matrix.get(1, 2));
		assertContents("[[A,A,A],[B,B,B]]", matrix);

		matrix.fillColumn(0, "C");
		assertEquals("C", matrix.get(0, 0));
		assertEquals("B", matrix.get(1, 2));
		assertContents("[[C,A,A],[C,B,B]]", matrix);

		assertEquals("C", matrix.set(1, 0, "D"));
		assertEquals("D", matrix.get(1, 0));
		assertContents("[[C,A,A],[D,B,B]]", matrix);
	}

	public void testOutOfBounds() {
		Matrix<String> matrix = new Matrix<>(2, 3);
		try {
			matrix.get(0, -1);
			fail();
		} catch (IndexOutOfBoundsException ex) {
			// expected.
		}

		try {
			matrix.get(-1, 0);
			fail();
		} catch (IndexOutOfBoundsException ex) {
			// expected.
		}

		try {
			matrix.get(0, 3);
			fail();
		} catch (IndexOutOfBoundsException ex) {
			// expected.
		}

		try {
			matrix.get(2, 0);
			fail();
		} catch (IndexOutOfBoundsException ex) {
			// expected.
		}
	}

	private void assertContents(String expected, Matrix<String> matrix) {
		assertEquals(expected, normalizedToString(matrix));
	}

	private String normalizedToString(Matrix<String> matrix) {
		return matrix.toString().replace("\n", "").replace("\t", "");
	}

}
