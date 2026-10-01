/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} delivering at most a given number of bytes from an underlying stream.
 *
 * <p>
 * The stream reports its end after the limit is reached, even if the underlying stream has more
 * content. Closing this stream closes the underlying stream.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LimitedInputStream extends FilterInputStream {

	private long _remaining;

	/**
	 * Creates a {@link LimitedInputStream}.
	 *
	 * @param in
	 *        The underlying stream to read from.
	 * @param limit
	 *        The maximum number of bytes to deliver. Must not be negative.
	 */
	public LimitedInputStream(InputStream in, long limit) {
		super(in);
		if (limit < 0) {
			throw new IllegalArgumentException("Negative limit: " + limit);
		}
		_remaining = limit;
	}

	@Override
	public int read() throws IOException {
		if (_remaining <= 0) {
			return -1;
		}
		int result = in.read();
		if (result >= 0) {
			_remaining--;
		}
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return 0;
		}
		if (_remaining <= 0) {
			return -1;
		}
		int result = in.read(b, off, (int) Math.min(len, _remaining));
		if (result > 0) {
			_remaining -= result;
		}
		return result;
	}

	@Override
	public long skip(long n) throws IOException {
		long result = in.skip(Math.min(n, _remaining));
		if (result > 0) {
			_remaining -= result;
		}
		return result;
	}

	@Override
	public int available() throws IOException {
		return (int) Math.min(in.available(), _remaining);
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public synchronized void mark(int readlimit) {
		// Not supported.
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new IOException("Mark is not supported.");
	}

}
