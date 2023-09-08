/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.javadoc;

/**
 * Variable length buffer of <code>int</code> values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IntBuffer {

	private int _length = 0;

	private int[] _buffer = new int[256];

	/**
	 * Resets the {@link #length()} to zero.
	 */
	public void clear() {
		_length = 0;
	}

	/**
	 * Adds a new value at the end of this {@link IntBuffer}.
	 * 
	 * @param value
	 *        The value to add.
	 */
	public void add(int value) {
		if (_length == _buffer.length) {
			int[] newBuffer = new int[_buffer.length * 2];
			System.arraycopy(_buffer, 0, newBuffer, 0, _length);
			_buffer = newBuffer;
		}
		_buffer[_length++] = value;
	}

	/**
	 * Retrieves the value at the given index.
	 *
	 * @param index
	 *        The index to access.
	 * @return The value at the given index.
	 */
	public int get(int index) throws IndexOutOfBoundsException {
		if (index >= _length) {
			throw new IndexOutOfBoundsException();
		}
		return _buffer[index];
	}

	/**
	 * Copies the given {@link IntBuffer} into this one.
	 * 
	 * <p>
	 * After this call returns, the {@link #length()} of this {@link IntBuffer} is the same as the
	 * {@link #length()} of the other.
	 * </p>
	 *
	 * @param other
	 *        The contents to copy from.
	 */
	public void copy(IntBuffer other) {
		System.arraycopy(other._buffer, 0, _buffer, 0, other._length);
		_length = other._length;
	}

	/**
	 * Makes the contents of this {@link IntBuffer} a delta to the given {@link IntBuffer}.
	 * 
	 * <p>
	 * State before (index-wise plus) state after = the given buffer.
	 * </p>
	 */
	public void delta(IntBuffer other) {
		while (_length < other._length) {
			add(0);
		}
		_length = other._length;
		for (int n = 0, cnt = _length; n < cnt; n++) {
			_buffer[n] = other._buffer[n] - _buffer[n];
		}
	}

	/**
	 * Number of values in this {@link IntBuffer}.
	 */
	public int length() {
		return _length;
	}

}
