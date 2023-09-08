/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.json.adapt;

import java.io.IOException;
import java.io.Writer;

import com.top_logic.basic.shared.io.W;

/**
 * {@link Writer} adapter to {@link W}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WriterW implements W {

	private final Writer _out;

	/**
	 * Creates a {@link WriterW}.
	 *
	 * @param out
	 *        The target {@link Writer} to use.
	 */
	public WriterW(Writer out) {
		_out = out;
	}

	@Override
	public void write(String value) throws IOException {
		_out.write(value);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		_out.write(str, off, len);
	}

	@Override
	public void write(char ch) throws IOException {
		_out.write(ch);
	}

	@Override
	public void flush() throws IOException {
		_out.flush();
	}

	@Override
	public void close() throws IOException {
		_out.close();
	}

}
