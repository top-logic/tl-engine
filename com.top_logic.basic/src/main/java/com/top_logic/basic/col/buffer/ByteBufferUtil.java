/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.buffer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.top_logic.basic.ArrayUtil;

/**
 * Useful methods for working with {@link ByteBuffer}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ByteBufferUtil {

	/**
	 * Creates a {@link ByteBuffer} with the concatenated content of the two given
	 * {@link ByteBuffer}s.
	 * <p>
	 * The given {@link ByteBuffer}s are not changed by this call. Their content, position, mark,
	 * limit and capacity remain unchanged.
	 * </p>
	 * <p>
	 * The capacity, limit and position of the new {@link ByteBuffer} are the sum of the sizes
	 * ({@link Buffer#remaining()} or {@link Buffer#limit()}, depending on the parameter
	 * "fromPosition") of the given {@link ByteBuffer}s. It has no mark.
	 * </p>
	 * 
	 * @param first
	 *        Null is equivalent to an empty buffer.
	 * @param second
	 *        Null is equivalent to an empty buffer.
	 * @param fromPosition
	 *        Whether the content should be copied from the beginning of the buffer or from its
	 *        "position".
	 * @return Never null. Always a new and independent {@link ByteBuffer}.
	 */
	public static ByteBuffer join(ByteBuffer first, ByteBuffer second, boolean fromPosition) {
		if ((first == null) && (second == null)) {
			return ByteBuffer.allocate(0);
		}
		if (first == null) {
			return copyContent(second, fromPosition);
		}
		if (second == null) {
			return copyContent(first, fromPosition);
		}
		return joinNonNull(first, second, fromPosition);
	}

	private static ByteBuffer joinNonNull(ByteBuffer first, ByteBuffer second, boolean fromPosition) {
		int joinedSize = getSize(first, fromPosition) + getSize(second, fromPosition);
		return ByteBuffer.allocate(joinedSize)
			.put(toArray(first, fromPosition))
			.put(toArray(second, fromPosition));
	}

	/**
	 * Returns a new {@link ByteBuffer} with the given {@link ByteBuffer}'s content.
	 * <p>
	 * The capacity, limit and position of the new {@link ByteBuffer} are the
	 * ({@link Buffer#remaining()} bytes or the {@link Buffer#limit()}, (depending on the parameter
	 * "fromPosition") of the given {@link ByteBuffer}. It has no mark.
	 * </p>
	 * <p>
	 * The given {@link ByteBuffer} is not changed by this call. Its content, position, mark, limit
	 * and capacity remain unchanged.
	 * </p>
	 *
	 * @param buffer
	 *        Null is equivalent to an empty buffer.
	 * @param fromPosition
	 *        Whether the content should be copied from the beginning of the buffer or from its
	 *        "position".
	 * @return Never null. Always a new and independent {@link ByteBuffer}.
	 */
	public static ByteBuffer copyContent(ByteBuffer buffer, boolean fromPosition) {
		byte[] array = toArray(buffer, fromPosition);
		return ByteBuffer.allocate(array.length).put(array);
	}

	/**
	 * Copies the content of the given {@link ByteBuffer} to a new array.
	 * <p>
	 * Does not change the state of the given buffer: It's content, capacity, limit, position and
	 * mark remain the same.
	 * </p>
	 * 
	 * @param buffer
	 *        Null is equivalent to an empty buffer.
	 * @param fromPosition
	 *        Whether the content should be copied from the beginning of the buffer or from its
	 *        "position".
	 * @return Never null.
	 */
	public static byte[] toArray(ByteBuffer buffer, boolean fromPosition) {
		if (buffer == null) {
			return ArrayUtil.EMPTY_BYTE_ARRAY;
		}
		ByteBuffer view = buffer.asReadOnlyBuffer();
		byte[] bytes = new byte[getSize(view, fromPosition)];
		if (!fromPosition) {
			view.position(0);
		}
		view.get(bytes);
		return bytes;
	}

	private static int getSize(Buffer buffer, boolean fromPosition) {
		if (fromPosition) {
			return buffer.remaining();
		} else {
			return buffer.limit();
		}
	}

}
