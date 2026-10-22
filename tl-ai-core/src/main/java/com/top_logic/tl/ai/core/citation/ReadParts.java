/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.ArrayList;
import java.util.List;

/**
 * The parts of one long document a run has read, and the text they make up together.
 *
 * <p>
 * A tool shows a long document in windows, and the run reads the windows it asks for - all of them in
 * order, or the first and the last. What a citation may quote, and what the archived copy shows, is
 * what the run read: these windows, in document order, with a marker where a part was left out. A
 * passage that crosses a part nobody read is therefore not found, which is right - the run never saw
 * it.
 * </p>
 *
 * <p>
 * The parts are positions in one text. When the document is read again and its text has changed, the
 * positions mean nothing in the new text, so the parts start over. Not thread-safe; the owning
 * {@link ReadingList} synchronizes.
 * </p>
 */
final class ReadParts {

	/** What stands where a part of the document was not read. */
	static final String GAP = "[...]";

	private final List<int[]> _ranges = new ArrayList<>();

	private int _length = -1;

	private int _hash;

	/**
	 * Adds the part {@code [start, end)} of {@code document} and answers the text read so far.
	 *
	 * @param document
	 *        The whole document the part was cut from.
	 * @param start
	 *        Where the part starts.
	 * @param end
	 *        Where it ends, exclusive.
	 * @param maxChars
	 *        How long the answer may be; a longer one is cut at the end.
	 * @return Every part read, in document order, separated by {@link #GAP} where something was left
	 *         out.
	 */
	String add(String document, int start, int end, int maxChars) {
		if (document.length() != _length || document.hashCode() != _hash) {
			_ranges.clear();
			_length = document.length();
			_hash = document.hashCode();
		}
		int from = Math.max(0, Math.min(start, _length));
		int to = Math.max(from, Math.min(end, _length));
		if (to > from) {
			insert(from, to);
		}
		return render(document, maxChars);
	}

	/** Inserts {@code [from, to)} and merges it with every range it touches. */
	private void insert(int from, int to) {
		int newFrom = from;
		int newTo = to;
		List<int[]> merged = new ArrayList<>(_ranges.size() + 1);
		boolean placed = false;
		for (int[] range : _ranges) {
			if (range[1] < newFrom) {
				merged.add(range);
			} else if (range[0] > newTo) {
				if (!placed) {
					merged.add(new int[] { newFrom, newTo });
					placed = true;
				}
				merged.add(range);
			} else {
				newFrom = Math.min(newFrom, range[0]);
				newTo = Math.max(newTo, range[1]);
			}
		}
		if (!placed) {
			merged.add(new int[] { newFrom, newTo });
		}
		_ranges.clear();
		_ranges.addAll(merged);
	}

	private String render(String document, int maxChars) {
		StringBuilder text = new StringBuilder();
		int previousEnd = 0;
		for (int[] range : _ranges) {
			if (range[0] > previousEnd) {
				if (text.length() > 0) {
					text.append("\n\n");
				}
				text.append(GAP).append("\n\n");
			}
			text.append(document, range[0], range[1]);
			previousEnd = range[1];
			if (text.length() >= maxChars) {
				return text.substring(0, maxChars);
			}
		}
		if (previousEnd < _length) {
			text.append("\n\n").append(GAP);
		}
		return text.length() > maxChars ? text.substring(0, maxChars) : text.toString();
	}
}
