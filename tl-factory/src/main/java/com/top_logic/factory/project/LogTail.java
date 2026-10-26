/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The last lines of an output that arrives in chunks not split at line ends.
 *
 * <p>
 * Keeps at most a given number of complete lines plus the line still being written; older lines
 * are dropped. Line ends are {@code \n}, a {@code \r} before it is dropped. Not thread-safe.
 * </p>
 */
public class LogTail {

	private final int _maxLines;

	private final Deque<String> _lines = new ArrayDeque<>();

	private final StringBuilder _current = new StringBuilder();

	/**
	 * Creates a {@link LogTail}.
	 *
	 * @param maxLines
	 *        The number of lines kept, at least one.
	 */
	public LogTail(int maxLines) {
		_maxLines = Math.max(1, maxLines);
	}

	/**
	 * Appends a chunk of the output.
	 */
	public void append(String chunk) {
		int start = 0;
		int end;
		while ((end = chunk.indexOf('\n', start)) >= 0) {
			_current.append(chunk, start, end);
			int length = _current.length();
			if (length > 0 && _current.charAt(length - 1) == '\r') {
				_current.setLength(length - 1);
			}
			_lines.addLast(_current.toString());
			_current.setLength(0);
			start = end + 1;
		}
		_current.append(chunk, start, chunk.length());
		int kept = _current.length() > 0 ? _maxLines - 1 : _maxLines;
		while (_lines.size() > kept) {
			_lines.removeFirst();
		}
	}

	/**
	 * Drops everything kept.
	 */
	public void clear() {
		_lines.clear();
		_current.setLength(0);
	}

	/**
	 * The kept lines, separated by {@code \n}, the line still being written last.
	 */
	public String text() {
		StringBuilder result = new StringBuilder();
		for (String line : _lines) {
			if (result.length() > 0) {
				result.append('\n');
			}
			result.append(line);
		}
		if (_current.length() > 0) {
			if (result.length() > 0) {
				result.append('\n');
			}
			result.append(_current);
		}
		return result.toString();
	}

}
