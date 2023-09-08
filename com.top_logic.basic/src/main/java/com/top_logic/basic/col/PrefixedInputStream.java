/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream that consists of a given prefix and the contents of another stream.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PrefixedInputStream extends InputStream {

	private int _pos = 0;

	private final byte[] _prefix;

	private final InputStream _postfix;

	/**
	 * Creates a {@link PrefixedInputStream} that consists of the concatenation of prefix and
	 * postfix.
	 * 
	 * @param prefix
	 *        The prefix contents.
	 * @param postfix
	 *        The postfix contents.
	 */
	public PrefixedInputStream(byte[] prefix, InputStream postfix) {
		_prefix = prefix;
		_postfix = postfix;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int avail = _prefix.length - _pos;
		if (avail > 0) {
			int direct = Math.min(len, avail);
			System.arraycopy(_prefix, _pos, b, off, direct);
			_pos += direct;
			return direct;
		} else {
			return _postfix.read(b, off, len);
		}
	}

	@Override
	public int read() throws IOException {
		if (_pos < _prefix.length) {
			return _prefix[_pos++];
		} else {
			return _postfix.read();
		}
	}

	@Override
	public void close() throws IOException {
		super.close();

		_postfix.close();
	}

}
