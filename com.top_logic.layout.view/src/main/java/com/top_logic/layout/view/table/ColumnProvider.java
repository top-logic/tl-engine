/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Column;

/**
 * Strategy that builds a green-field {@link Column} (accessor, renderer, comparator and filter) for
 * values of one model type.
 *
 * <p>
 * Resolved per value type by {@link ColumnProviderService}, so the mapping from a datatype to its
 * table affordances is an extensible registry rather than a hard-coded type switch: an application
 * can register a provider for its own type without touching the table layer.
 * </p>
 *
 * <p>
 * A provider knows nothing of a cell the user may not read: such a cell reaches it as an empty
 * value, and the {@link ColumnProviderService} shows it and makes it searchable by the placeholder of
 * {@link ColumnProviderService#UNREADABLE} around the column the provider builds. The provider's
 * sort and filter see such a cell as an empty one, unlike the built-in columns, which sort it after
 * every value and match it by no criterion.
 * </p>
 */
public interface ColumnProvider {

	/**
	 * Builds the column showing the described values.
	 *
	 * @param name
	 *        The column name, which for a column over a model attribute is the attribute name.
	 * @param label
	 *        The resolved column header label.
	 * @param type
	 *        What the column's values are, see {@link ColumnType}.
	 * @param value
	 *        Reads the cell value from a row. Never the {@link ColumnProviderService#UNREADABLE}
	 *        marker: a cell the user may not read is read as {@code null} here.
	 * @return The column definition.
	 */
	Column<Object, ?> createColumn(String name, ResKey label, ColumnType type, Function<Object, Object> value);

}
