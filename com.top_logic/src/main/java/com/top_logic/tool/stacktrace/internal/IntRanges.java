/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.stacktrace.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Collection of integer intervals.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IntRanges implements Iterable<Range> {
	
	private List<Range> _ranges = new ArrayList<>();
	
	@Override
	public Iterator<Range> iterator() {
		return _ranges.iterator();
	}
	
	/**
	 * Adds the given range.
	 *
	 * @param start
	 *        The first value of the range (inclusive).
	 * @param stop
	 *        The end of the range (exclusive).
	 */
	public void addRange(int start, int stop) {
		if (stop <= start) {
			return;
		}
		Range range = new Range(start, stop);
		int index = Collections.binarySearch(_ranges, range, Range.ORDER);
		if (index >= 0) {
			joinAt(index, range);
		} else {
			int insertionPoint = insertionPoint(index);
			if (insertionPoint > 0 && Range.canCombine(range(insertionPoint - 1), range)) {
				joinAt(insertionPoint - 1, range);
			} else if (insertionPoint < _ranges.size() && Range.canCombine(range(insertionPoint), range)) {
				joinAt(insertionPoint, range);
			} else {
				_ranges.add(insertionPoint, range);
			}
		}
	}

	private int insertionPoint(int index) {
		return -index - 1;
	}

	private void joinAt(int index, Range range) {
		set(index, range(index).join(range));
		while (index + 1 < _ranges.size() && Range.canCombine(range(index), range(index + 1))) {
			set(index, range(index).join(range(index + 1)));
			_ranges.remove(index + 1);
		}
	}

	private void set(int index, Range range) {
		_ranges.set(index, range);
	}

	private Range range(int index) {
		return _ranges.get(index);
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append('{');
		for (Range range : _ranges) {
			if (result.length() > 1) {
				result.append(',');
				result.append(' ');
			}
			result.append('[');
			result.append(range.getStart());
			result.append(',');
			result.append(' ');
			result.append(range.getStop() - 1);
			result.append(']');
		}
		result.append('}');
		return result.toString();
	}

	/**
	 * Whether the given value is contained in any {@link #addRange(int, int) range}.
	 */
	public boolean contains(int value) {
		Range range = new Range(value, value + 1);
		int index = Collections.binarySearch(_ranges, range, Range.ORDER);
		if (index >= 0) {
			return true;
		} else {
			int insertionPoint = insertionPoint(index);
			if (insertionPoint > 0) {
				return Range.overlaps(range(insertionPoint - 1), range);
			} else {
				return false;
			}
		}
	}

	/**
	 * Whether no value is {@link #contains(int) contained} in this set.
	 */
	public boolean isEmpty() {
		return _ranges.isEmpty();
	}

}
