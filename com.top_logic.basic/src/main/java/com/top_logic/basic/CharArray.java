/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link CharSequence} based on a char array.
 * 
 * <p>
 * Changes in the source array reflect to {@link #charAt(int)}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CharArray implements CharSequence {

	private final char[] _array;

	private final int _offset;

	private final int _length;

	/**
	 * Creates a new {@link CharArray}.
	 */
	public CharArray(char[] array) {
		this(array, 0, array.length);
	}

	/**
	 * Creates a new {@link CharArray}.
	 * 
	 * @param offset
	 *        Offset in the array to access.
	 * @param length
	 *        Value of {@link #length()}.
	 * @throws IndexOutOfBoundsException
	 *         iff offset or length is negative, or length + offset larger then array length.
	 */
	public CharArray(char[] array, int offset, int length) {
		_array = array;
		_offset = offset;
		_length = length;
		if (_offset < 0) {
			throw new IndexOutOfBoundsException("Offset " + _offset + " must not be negative.");
		}
		if (_length < 0) {
			throw new IndexOutOfBoundsException("Offset " + _offset + " must not be negative.");
		}
		if (_length + _offset > array.length) {
			throw new IndexOutOfBoundsException("Length " + _length + " too large.");
		}
	}

	@Override
	public int length() {
		return _length;
	}

	@Override
	public char charAt(int index) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Index " + index + " must not be negative.");
		}
		if (index > length()) {
			throw new IndexOutOfBoundsException("Index " + index + " must be less than length.");
		}
		return _array[_offset + index];
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (start == 0 && end == length()) {
			return this;
		}
		if (end == start) {
			return StringServices.EMPTY_STRING;
		}
		return new CharArray(_array, start, end - start);
	}

	@Override
	public String toString() {
		return new String(_array, _offset, _length);
	}

}

