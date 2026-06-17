/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Translates a column's active {@link FilterState} into a query contribution, so that a
 * query-backed {@link RowSource} can filter rows in the data tier instead of in memory.
 *
 * @see ColumnFilter#pushdown(FilterState)
 */
@FunctionalInterface
public interface FilterPushdown {

	/**
	 * Contributes this column's filter restriction to the given {@link QuerySink}.
	 */
	void contribute(QuerySink sink);

}
