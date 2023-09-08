/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.shared.io;

import java.io.IOException;

/**
 * String {@link W writer}.
 * 
 * <p>
 * The collected output can be retrieved using {@link #toString()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringW implements W {

	StringBuilder _data = new StringBuilder();

	@Override
	public void write(String value) {
		_data.append(value);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		_data.append(str.substring(off, off + len));
	}

	@Override
	public void write(char ch) {
		_data.append(ch);
	}

	@Override
	public void flush() {
		// Ignore.
	}

	@Override
	public void close() {
		// Ignore.
	}

	@Override
	public String toString() {
		return _data.toString();
	}

}
