/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.character;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link Writer} that is a proxy for another {@link Writer} that does not forward {@link #close()}
 * calls.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class NonClosingProxyWriter extends Writer {
	private final Writer _out;

	/** 
	 * Creates a {@link NonClosingProxyWriter}.
	 */
	public NonClosingProxyWriter(Writer out) {
		_out = out;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		_out.write(cbuf, off, len);
	}

	@Override
	public void flush() throws IOException {
		_out.flush();
	}

	@Override
	public void close() throws IOException {
		// Ignore.
	}

	@Override
	public Writer append(char c) throws IOException {
		return _out.append(c);
	}

	@Override
	public Writer append(CharSequence csq) throws IOException {
		return _out.append(csq);
	}

	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {
		return _out.append(csq, start, end);
	}
}