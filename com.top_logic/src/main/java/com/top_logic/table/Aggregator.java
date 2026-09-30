/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * The aggregation capability of a {@link Column}: compute a footer/group-total cell over a
 * {@link Group} of rows (sum, average, min, max, count, median, ...).
 *
 * @param <R>
 *        The row business object type.
 * @param <V>
 *        The cell value type.
 */
@FunctionalInterface
public interface Aggregator<R, V> {

	/**
	 * Computes the aggregate cell content for the given group.
	 */
	CellContent over(Group<R> group);

}
