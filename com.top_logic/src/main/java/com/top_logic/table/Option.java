/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.basic.util.ResKey;

/**
 * A selectable value of an option-style {@link FilterInput.Options column filter}.
 *
 * <p>
 * The number of rows matching an option is not part of the option itself; it is provided
 * separately via {@link MatchCounts} so that counts can be recomputed as other filters
 * change without rebuilding the option list.
 * </p>
 *
 * @param value
 *        The domain value to filter by.
 * @param label
 *        The display label for the value.
 */
public record Option(Object value, ResKey label) {

	/**
	 * The {@link #value()} of the option standing for the cells holding no value: an empty cell,
	 * or a cell holding an empty collection of values.
	 *
	 * <p>
	 * It is a facet key of its own, counted as every other one, and it is never a value of a
	 * column.
	 * </p>
	 */
	public static final Object NO_VALUE = new Object() {
		@Override
		public String toString() {
			return "NO_VALUE";
		}
	};

}
