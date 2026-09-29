/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Translates a column's sort order into a query contribution, so that a query-backed
 * {@link RowSource} can order rows in the data tier instead of in memory.
 *
 * @see Sort#pushdown()
 */
@FunctionalInterface
public interface OrderPushdown {

	/**
	 * Contributes this column's ordering to the given {@link QuerySink}.
	 *
	 * @param sink
	 *        The backend-specific query sink.
	 * @param ascending
	 *        Whether to order ascending ({@code true}) or descending ({@code false}).
	 */
	void contribute(QuerySink sink, boolean ascending);

}
