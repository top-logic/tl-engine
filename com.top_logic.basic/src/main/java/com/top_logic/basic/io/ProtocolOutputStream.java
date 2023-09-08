/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.top_logic.basic.Protocol;

/**
 * The class {@link ProtocolOutputStream} writes the content to a {@link Protocol}.
 * 
 * <p>
 * The content written to this {@link OutputStream} is forwarded to
 * {@link Protocol#info(String, int)} as soon as '\n' is written. A '\r' written directly before the
 * line feed is suppressed. Moreover if {@link #flush()} is called. All currently not flushed
 * content is logged.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtocolOutputStream extends OutputStream {

	private static final int CARRIAGE_RETURN = '\r';

	private static final int LINE_FEED = '\n';

	private static final int NO_BYTE_WRITTEN = Integer.MAX_VALUE;

	private final ByteArrayOutputStream _buffer = new ByteArrayOutputStream();

	private final Protocol _protocol;

	private final int _verbosityLevel;
	
	private int _lastWritten = NO_BYTE_WRITTEN;

	/**
	 * Creates a new {@link ProtocolOutputStream} that logs with verbosity {@link Protocol#INFO}.
	 */
	public ProtocolOutputStream(Protocol protocol) {
		this(protocol, Protocol.INFO);
	}

	/**
	 * Creates a new {@link ProtocolOutputStream} that logs with given verbosity.
	 */
	public ProtocolOutputStream(Protocol protocol, int verbosityLevel) {
		_protocol = protocol;
		_verbosityLevel = verbosityLevel;
	}

	@Override
	public void write(int b) {
		if (noContent()) {
			_lastWritten = b;
			return;
		}
		if (b == LINE_FEED) {
			if (_lastWritten != CARRIAGE_RETURN) {
				_buffer.write(_lastWritten);
			}
			flushBuffer();
			return;
		} else {
			_buffer.write(_lastWritten);
			_lastWritten = b;
		}
	}

	/**
	 * Writes the content not formerly logged to the underlying {@link Protocol}.
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void flush() {
		if (noContent()) {
			return;
		}
		_buffer.write(_lastWritten);
		flushBuffer();
	}

	private void flushBuffer() {
		_protocol.info(_buffer.toString(), _verbosityLevel);
		_buffer.reset();
		_lastWritten = NO_BYTE_WRITTEN;
	}

	private boolean noContent() {
		return _lastWritten == NO_BYTE_WRITTEN;
	}

	/**
	 * Flushes and closed this {@link OutputStream}.
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() {
		flush();
	}

}
