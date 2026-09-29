/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * One step of a (possibly multi-column) sort order: a column and its direction.
 *
 * @param column
 *        The {@link Column#name() name} of the column to sort by.
 * @param ascending
 *        Whether to sort ascending ({@code true}) or descending ({@code false}).
 */
public record SortColumn(String column, boolean ascending) {
	// Pure value type.
}
