/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.buffer;

import static com.top_logic.basic.col.buffer.ByteBufferUtil.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.col.buffer.ByteBufferUtil;

/**
 * Tests for: {@link ByteBufferUtil}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestByteBufferUtil extends TestCase {

	/**
	 * There are three states of a buffer besides capacity: mark, position and limit. The capacity
	 * has to be large enough that there can at least one byte before, after and between them.
	 */
	private static final int MAX_CAPACITY = 7;

	/**
	 * Test for {@link ByteBufferUtil#join(ByteBuffer, ByteBuffer, boolean)}.
	 */
	public void testJoin() {
		testJoin(false);
		testJoin(true);
		testJoinFirstIsNull(false);
		testJoinFirstIsNull(true);
		testJoinSecondIsNull(false);
		testJoinSecondIsNull(true);
		testJoinBothAreNull(false);
		testJoinBothAreNull(true);
	}

	private void testJoin(boolean fromPosition) {
		ByteBuffer first = toBuffer(-1, 2, 4, 6, new byte[] { 0, 1, 2, 3 });
		ByteBuffer second = toBuffer(-1, 3, 6, 9, new byte[] { 10, 11, 12, 13, 14, 15 });
		ByteBuffer actualResult = join(first, second, fromPosition);
		byte[] expectedResult =
			fromPosition ? new byte[] { 2, 3, 13, 14, 15 } : new byte[] { 0, 1, 2, 3, 10, 11, 12, 13, 14, 15 };
		assertEquals("Parameters: fromPosition: " + fromPosition + ".", expectedResult, toArray(actualResult, false));
	}

	private void testJoinFirstIsNull(boolean fromPosition) {
		ByteBuffer first = toBuffer(-1, 2, 4, 6, new byte[] { 0, 1, 2, 3 });
		ByteBuffer second = null;
		ByteBuffer actualResult = join(first, second, fromPosition);
		byte[] expectedResult = fromPosition ? new byte[] { 2, 3 } : new byte[] { 0, 1, 2, 3 };
		assertEquals("Parameters: fromPosition: " + fromPosition + ".", expectedResult, toArray(actualResult, false));
	}

	private void testJoinSecondIsNull(boolean fromPosition) {
		ByteBuffer first = null;
		ByteBuffer second = toBuffer(-1, 2, 4, 6, new byte[] { 0, 1, 2, 3 });
		ByteBuffer actualResult = join(first, second, fromPosition);
		byte[] expectedResult = fromPosition ? new byte[] { 2, 3 } : new byte[] { 0, 1, 2, 3 };
		assertEquals("Parameters: fromPosition: " + fromPosition + ".", expectedResult, toArray(actualResult, false));
	}

	private void testJoinBothAreNull(boolean fromPosition) {
		ByteBuffer first = null;
		ByteBuffer second = null;
		ByteBuffer actualResult = join(first, second, fromPosition);
		byte[] expectedResult = ArrayUtil.EMPTY_BYTE_ARRAY;
		assertEquals("Parameters: fromPosition: " + fromPosition + ".", expectedResult, toArray(actualResult, false));
	}

	/**
	 * Test for {@link ByteBufferUtil#toArray(ByteBuffer, boolean)}.
	 * <p>
	 * Exhaustive test for all possible parameter combinations for buffers of size
	 * {@value #MAX_CAPACITY} and smaller. (The bytes in the buffer are not altered, as they
	 * irrelevant and cause way too much combinations.)
	 * </p>
	 */
	public void testToArray() {
		testToArray(false);
		testToArray(true);
	}

	private void testToArray(boolean fromPosition) {
		for (int capacity = 0; capacity <= MAX_CAPACITY; capacity++) {
			for (int limit = 0; limit <= capacity; limit++) {
				for (int position = 0; position <= limit; position++) {
					/* "-1" means: "mark" is set to "undefined". */
					for (int mark = -1; mark <= position; mark++) {
						testToArray(capacity, limit, position, mark, fromPosition);
					}
				}
			}
		}
	}

	private void testToArray(int capacity, int limit, int position, int mark, boolean fromPosition) {
		byte[] content = createArray(limit);
		ByteBuffer byteBuffer = toBuffer(mark, position, limit, capacity, content);
		String message = "Parameters: " + mark + "; " + position + "; " + limit + "; " + capacity;
		int expectedStart = fromPosition ? position : 0;
		byte[] expectedContent = Arrays.copyOfRange(content, expectedStart, limit);
		assertEquals(message, expectedContent, toArray(byteBuffer, fromPosition));
		assertBufferStateIsUnchanged(capacity, limit, position, mark, byteBuffer);
	}

	private void assertBufferStateIsUnchanged(int capacity, int limit, int position, int mark, ByteBuffer byteBuffer) {
		assertEquals(position, byteBuffer.position());
		if (mark != -1) {
			/* "-1" means the mark is not set. "reset()" throws an exception if the mark is not
			 * set. */
			/* The only way to get the "mark" is to reset the buffer and read the position. */
			byteBuffer.reset();
			assertEquals(mark, byteBuffer.position());
		}
		assertEquals(limit, byteBuffer.limit());
		assertEquals(capacity, byteBuffer.capacity());
	}

	private byte[] createArray(int capacity) {
		/* The content does not matter, as long as every byte has a different value. */
		byte[] content = new byte[capacity];
		for (byte i = 0; i < capacity; i++) {
			content[i] = i;
		}
		return content;
	}

	private ByteBuffer toBuffer(int mark, int position, int limit, int capacity, byte... content) {
		ByteBuffer buffer = ByteBuffer.allocate(capacity);
		buffer.put(content.clone());
		if (mark == -1) {
			/* The only way to reset the "mark" is to "rewind()" it. */
			buffer.rewind();
		} else {
			/* The only way to set the "mark" is to set the "position" and then "mark()" it. */
			buffer.position(mark);
			buffer.mark();
		}
		buffer.position(position);
		buffer.limit(limit);
		return buffer;
	}

	private void assertEquals(String message, byte[] expected, byte[] actual) {
		String fullMessage = " Expected: " + Arrays.toString(expected)
			+ ". Actual: " + Arrays.toString(actual) + ". " + message;
		assertTrue(fullMessage, Arrays.equals(expected, actual));
	}

}
