/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.io;

import java.io.IOException;

/**
 * String {@link R reader}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringR implements R {

	private final char[] _data;

	private int _pos;

	/**
	 * Creates a {@link StringR}.
	 *
	 * @param data
	 *        The {@link String} to read from.
	 */
	public StringR(String data) {
		_data = data.toCharArray();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int avail = _data.length - _pos;
		if (avail == 0) {
			return -1;
		}
		int cnt = Math.min(avail, len);
		System.arraycopy(_data, _pos, cbuf, off, cnt);
		_pos += cnt;
		return cnt;
	}

	@Override
	public void close() {
		// Ignore.
	}

}
