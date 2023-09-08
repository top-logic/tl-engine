/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;

/**
 * {@link Writer} that buffers written data in a {@link CharBuffer}.
 * 
 * <p>
 * In contrast to {@link BufferedWriter}, this class is prepared to directly feed an
 * {@link CharsetEncoder} for true allocation-free writing in a future version. Additionally, it has
 * a public API to just flush the buffer instead (without the underlying writer, see
 * {@link #flushBuffer()}).
 * </p>
 * 
 * @see #flushBuffer()
 * @see CharsetEncoder#encode(CharBuffer, java.nio.ByteBuffer, boolean)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BufferingWriter extends Writer {

	private static int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * The {@link Writer} to forward data to.
	 */
	private Writer _out;

	/**
	 * Temporary buffer.
	 */
	private CharBuffer _buffer;

	/**
	 * Size limit for buffered operations.
	 */
	private final int _bufferingLimit;

	/**
	 * Creates a {@link BufferingWriter} with default buffer size..
	 * 
	 * @param out
	 *        A {@link Writer} to forward buffered output to.
	 */
	public BufferingWriter(Writer out) {
		this(out, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Creates a {@link BufferingWriter}.
	 * 
	 * @param out
	 *        A {@link Writer} to forward buffered output to.
	 * @param bufferSize
	 *        {@link CharBuffer} size.
	 */
	public BufferingWriter(Writer out, int bufferSize) {
		this(out, bufferSize, bufferSize / 8);
	}

	/**
	 * Creates a {@link BufferingWriter}.
	 * 
	 * @param out
	 *        A {@link Writer} to forward buffered output to.
	 * @param bufferSize
	 *        {@link CharBuffer} size.
	 * @param bufferingLimit
	 *        Size limit of a buffered write request. A write request larger that this size will be
	 *        directly flushed.
	 */
	public BufferingWriter(Writer out, int bufferSize, int bufferingLimit) {
		super(out);

		_out = out;
		_buffer = CharBuffer.allocate(bufferSize);
		_bufferingLimit = Math.min(bufferSize, bufferingLimit);
	}

	@Override
	public void write(int c) throws IOException {
		ensureOpen();
		if (_buffer.remaining() == 0) {
			flushBuffer();
		}
		_buffer.put((char) c);
	}

	@Override
	public void write(char cbuf[], int offset, int length) throws IOException {
		ensureOpen();
		if (length == 0) {
			return;
		}

		if (length > _bufferingLimit) {
			flushBuffer();
			_out.write(cbuf, offset, length);
			return;
		}

		if (length > _buffer.remaining()) {
			flushBuffer();
		}

		_buffer.put(cbuf, offset, length);
	}

	@Override
	public void write(String s, int offset, int length) throws IOException {
		ensureOpen();
		if (length == 0) {
			return;
		}

		if (length > _bufferingLimit) {
			flushBuffer();
			_out.write(s, offset, length);
			return;
		}

		if (length > _buffer.remaining()) {
			flushBuffer();
		}

		_buffer.put(s, offset, offset + length);
	}

	/**
	 * Flushes the writer.
	 * 
	 * @exception IOException
	 *            If an I/O error occurs.
	 */
	@Override
	public void flush() throws IOException {
		flushBuffer();
		_out.flush();
	}

	@Override
	public void close() throws IOException {
		if (_out == null) {
			return;
		}
		try {
			flushBuffer();
		} finally {
			_out.close();
			_out = null;
			_buffer = null;
		}
	}

	/** Make sure that the writer has not been closed. */
	private void ensureOpen() throws IOException {
		if (_out == null) {
			throw new IOException("Writer closed.");
		}
	}

	/**
	 * Flushes the output buffer to the underlying {@link Writer} without flushing the underlying
	 * {@link Writer} itself.
	 */
	public final void flushBuffer() throws IOException {
		ensureOpen();
		if (_buffer.position() == 0) {
			return;
		}
		_out.write(_buffer.array(), 0, _buffer.position());
		_buffer.clear();
	}

}
