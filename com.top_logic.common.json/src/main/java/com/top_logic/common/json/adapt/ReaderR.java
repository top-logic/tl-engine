/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.json.adapt;

import java.io.IOException;
import java.io.Reader;

import com.top_logic.basic.shared.io.R;

/**
 * Adapter making {@link Reader} compatible with {@link R}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ReaderR implements R {

	private final Reader _reader;

	/**
	 * Creates a {@link ReaderR}.
	 *
	 */
	public ReaderR(Reader reader) {
		_reader = reader;
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		return _reader.read(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		_reader.close();
	}

}
