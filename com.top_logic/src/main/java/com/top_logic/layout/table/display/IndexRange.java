/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

/**
 * Definition of visible indices at client side's viewport (e.g. rows or columns).
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class IndexRange {

	private static final IndexRange UNDEFINED = IndexRange.singleIndex(-1);

	private int firstIndex;
	private int lastIndex;
	private int forcedVisibleIndexInRange;

	private IndexRange(int firstIndex, int lastIndex, int forcedVisibleIndexInRange) {
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
		this.forcedVisibleIndexInRange = forcedVisibleIndexInRange;
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public int getLastIndex() {
		return lastIndex;
	}

	/**
	 * index, which shall be visible at least, even in case the range is greater than the
	 *         client side's viewport.
	 */
	public int getForcedVisibleIndexInRange() {
		return forcedVisibleIndexInRange;
	}

	public static IndexRange undefined() {
		return UNDEFINED;
	}

	public static IndexRange singleIndex(int visibleIndex) {
		return IndexRange.multiIndex(visibleIndex, visibleIndex, visibleIndex);
	}

	/**
	 * a {@link IndexRange}, whereby the first index will be marked as visible, even in case
	 *         the range is larger than the client side's viewport
	 */
	public static IndexRange multiIndex(int firstVisibleIndex, int lastVisibleIndex) {
		return IndexRange.multiIndex(firstVisibleIndex, lastVisibleIndex, firstVisibleIndex);
	}

	/**
	 * @param forcedVisibleIndexInRange
	 *        - index, which shall be visible at least, even in case the range is larger than the client
	 *        side's viewport
	 * @return a {@link IndexRange}, whereby a specified index will be marked as visible, even in case
	 *         the range is larger than the client side's viewport
	 */
	public static IndexRange multiIndex(int firstVisibleIndex, int lastVisibleIndex, int forcedVisibleIndexInRange) {
		validateArguments(firstVisibleIndex, lastVisibleIndex, forcedVisibleIndexInRange);
		return new IndexRange(firstVisibleIndex, lastVisibleIndex, forcedVisibleIndexInRange);
	}

	private static void validateArguments(int firstVisibleIndex, int lastVisibleIndex, int forcedVisibleIndexInRange) {
		validateRangeDefinition(firstVisibleIndex, lastVisibleIndex, forcedVisibleIndexInRange);
		validateVisibleIndexDefinition(firstVisibleIndex, lastVisibleIndex, forcedVisibleIndexInRange);
	}

	private static void validateRangeDefinition(int firstVisibleIndex, int lastVisibleIndex,
			int forcedVisibleIndexInRange) {
		if (lastVisibleIndex < firstVisibleIndex) {
			throw new IllegalArgumentException(
				"For a valid range definition the last index must equal or greater than first index!"
					+
												getFormattedArguments(firstVisibleIndex, lastVisibleIndex,
													forcedVisibleIndexInRange));
		}
	}

	private static void validateVisibleIndexDefinition(int firstVisibleIndex, int lastVisibleIndex,
			int forcedVisibleIndexInRange) {
		if (forcedVisibleIndexInRange < firstVisibleIndex || forcedVisibleIndexInRange > lastVisibleIndex) {
			throw new IllegalArgumentException("The at least visible index must be within defined range!"
				+ getFormattedArguments(firstVisibleIndex, lastVisibleIndex, forcedVisibleIndexInRange));
		}
	}

	private static String getFormattedArguments(int firstVisibleIndex, int lastVisibleIndex,
			int forcedVisibleIndexInRange) {
		return " [FirstIndex: " + firstVisibleIndex + ", " +
							"LastIndex: " + lastVisibleIndex + ", " +
							"ForcesVisibleIndex: " + forcedVisibleIndexInRange + "]";
	}

}
