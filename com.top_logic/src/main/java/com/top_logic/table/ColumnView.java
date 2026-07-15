/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.basic.util.ResKey;

/**
 * The UI-facing descriptor of one visible column: its current display metadata and
 * interaction state, derived from the {@link Column} definition and the
 * {@link TableViewState}.
 *
 * @param name
 *        The {@link Column#name() column name}.
 * @param label
 *        The column header label.
 * @param width
 *        The current display width in pixels.
 * @param sortable
 *        Whether the user can sort by this column.
 * @param filterable
 *        Whether the user can filter this column.
 * @param editable
 *        Whether cells in this column are inline-editable.
 * @param frozen
 *        Whether this column is currently frozen (fixed).
 * @param sortDirection
 *        The current sort direction, or {@code null} if this column is not part of the
 *        sort order.
 * @param sortPriority
 *        The 1-based position of this column in a multi-column sort, or {@code 0} if
 *        unsorted.
 */
public record ColumnView(
		String name,
		ResKey label,
		int width,
		boolean sortable,
		boolean filterable,
		boolean editable,
		boolean frozen,
		SortDirection sortDirection,
		int sortPriority) {
	// Pure value type.
}
