/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.io.IOException;
import java.io.Reader;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;

/**
 * Default {@link FullTextBuBuffer} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFullTextBuffer extends AbstractFullTextBuffer {

	private static final char FULL_TEXT_SEPARATOR = ' ';

	private final StringBuilder _buffer = new StringBuilder();

	@Override
	public void add(CharSequence text) {
		if (!StringServices.isEmpty(text)) {
			if (_buffer.length() > 0) {
				_buffer.append(FULL_TEXT_SEPARATOR);
			}
			_buffer.append(text);
		}
	}

	@Override
	public void add(Reader reader) {
		_buffer.append(FULL_TEXT_SEPARATOR);
		int ch;
		try {
			while ((ch = reader.read()) > -1) {
				_buffer.append((char) ch);
			}
		} catch (IOException ex) {
			Logger.error("Failed to read full-text contents.", ex, DefaultFullTextBuffer.class);
		}
	}

	@Override
	public String toString() {
		return _buffer.toString();
	}

}
