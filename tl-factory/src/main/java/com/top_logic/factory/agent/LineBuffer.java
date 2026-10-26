/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import java.util.function.Consumer;

import com.top_logic.factory.workspace.ExecOutput;

/**
 * Splits the output of a command, which an {@link ExecOutput} receives in chunks of any length,
 * into lines.
 *
 * <p>
 * A line is handed on without its line end; a carriage return before the line feed is dropped as
 * well. Thread-safe.
 * </p>
 */
public final class LineBuffer {

	private static final char LINE_FEED = '\n';

	private static final char CARRIAGE_RETURN = '\r';

	private final StringBuilder _pending = new StringBuilder();

	private final Consumer<String> _lines;

	/**
	 * Creates a {@link LineBuffer}.
	 *
	 * @param lines
	 *        Receives each complete line.
	 */
	public LineBuffer(Consumer<String> lines) {
		_lines = lines;
	}

	/**
	 * Adds a chunk of output, handing on the lines it completes.
	 */
	public synchronized void append(String chunk) {
		int start = 0;
		for (int n = 0; n < chunk.length(); n++) {
			if (chunk.charAt(n) == LINE_FEED) {
				_pending.append(chunk, start, n);
				emit();
				start = n + 1;
			}
		}
		_pending.append(chunk, start, chunk.length());
	}

	/**
	 * Hands on the rest of the output that ended without a line end.
	 */
	public synchronized void finish() {
		if (_pending.length() > 0) {
			emit();
		}
	}

	private void emit() {
		int length = _pending.length();
		if (length > 0 && _pending.charAt(length - 1) == CARRIAGE_RETURN) {
			_pending.setLength(length - 1);
		}
		String line = _pending.toString();
		_pending.setLength(0);
		_lines.accept(line);
	}

}
