/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.stacktrace.internal;

import java.util.Comparator;

/**
 * An integer range of values.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Range {
	
	/**
	 * {@link Comparator} of {@link Range}s based on their {@link #getStart()} value.
	 */
	public static final Comparator<Range> ORDER = (r1, r2) -> Integer.compare(r1.getStart(), r2.getStart());
	
	private final int _start;
	private final int _stop;

	/**
	 * Creates a {@link Range}.
	 *
	 * @param start
	 *        See {@link #getStart()}.
	 * @param stop
	 *        See {@link #getStop()}.
	 */
	public Range(int start, int stop) {
		_start = start;
		_stop = stop;
	}

	/**
	 * The first value in the range (inclusive).
	 */
	public int getStart() {
		return _start;
	}

	/**
	 * The end of the range (exclusive).
	 */
	public int getStop() {
		return _stop;
	}

	/**
	 * The {@link Range} containing this {@link Range} and the given one.
	 *
	 * @param other
	 *        The other range to unite with this one.
	 * @return The {@link Range} spawning this and the other one.
	 * 
	 * @see #canCombine(Range, Range)
	 */
	public Range join(Range other) {
		return new Range(Math.min(getStart(), other.getStart()), Math.max(getStop(), other.getStop()));
	}

	/**
	 * Whether the given {@link Range}s are adjacent or {@link #overlaps(Range, Range) overlap}.
	 * 
	 * <p>
	 * Only overlapping {@link Range}s can be {@link #join(Range) joned} without adding additional
	 * values.
	 * </p>
	 */
	public static boolean canCombine(Range r1, Range r2) {
		if (r1.getStart() <= r2.getStart()) {
			return r1.getStop() >= r2.getStart();
		} else {
			return canCombine(r2, r1);
		}
	}
	
	/**
	 * Whether there are common values in the given two ranges.
	 */
	public static boolean overlaps(Range r1, Range r2) {
		if (r1.getStart() <= r2.getStart()) {
			return r1.getStop() > r2.getStart();
		} else {
			return canCombine(r2, r1);
		}
	}
}