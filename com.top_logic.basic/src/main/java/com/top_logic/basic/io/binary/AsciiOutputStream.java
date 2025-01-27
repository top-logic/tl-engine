/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Stream for directly writing (ASCII encoded) bytes as text to a writer.
 * 
 * <p>
 * Note: Use this adapter only, if the byte data written to this stream is de-facto ASCII encoded
 * (values in the range 0,..127).
 * </p>
 */
public class AsciiOutputStream extends OutputStream {
	private final Writer _out;

	private final char[] _buffer = new char[1024];

	private int _pos = 0;

	/**
	 * Creates a {@link AsciiOutputStream}.
	 *
	 * @param out
	 *        The {@link Writer} to write ASCII data to.
	 */
	public AsciiOutputStream(Writer out) {
		_out = out;
	}

	@Override
	public void write(int b) throws IOException {
		flush(1);
		enter(b);
	}

	@Override
	public void write(byte[] b, int offset, final int len) throws IOException {
		final int stop = offset + len;

		int index = offset;
		while (index < stop) {
			int remaining = stop - index;
			int chunkSize = Math.min(_buffer.length, remaining);
			flush(chunkSize);

			int limit = index + chunkSize;
			while (index < limit) {
				enter(b[index++]);
			}
		}
	}

	private void enter(int b) throws IOException {
		if (b > 127) {
			throw new IOException("Not an ASCII character: " + b);
		}
		_buffer[_pos++] = (char) b;
	}

	/**
	 * Flushes the internal buffer to the output, if there is less capacity left in the buffer than
	 * the given number of characters to reserve.
	 */
	private void flush(int reserve) throws IOException {
		if (_pos + reserve > _buffer.length) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		if (_pos > 0) {
			_out.write(_buffer, 0, _pos);
			_pos = 0;
		}
	}

	@Override
	public void close() throws IOException {
		flush();
		super.close();
	}
}