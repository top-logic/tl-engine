/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

import com.top_logic.basic.util.ResKey;

/**
 * One choice offered by the column selection: a column the table can display, and whether it is
 * currently displayed.
 *
 * <p>
 * Unlike {@link ColumnView}, which describes a column the user currently sees (with width, sort
 * state and frozen flag), this is the descriptor of a column as an <em>option</em> - it also covers
 * the columns that exist but are hidden, for which none of that display state is meaningful.
 * </p>
 *
 * @param name
 *        The {@link Column#name() column name}.
 * @param label
 *        The column header label.
 * @param visible
 *        Whether the column is part of the displayed columns.
 * @see TableView#columnOptions()
 */
public record ColumnOption(String name, ResKey label, boolean visible) {
	// Pure value type.
}
