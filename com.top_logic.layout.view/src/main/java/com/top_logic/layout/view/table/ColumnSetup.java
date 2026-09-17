/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.table.Aggregator;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DelegatingColumn;

/**
 * The resolved descriptor of one table column, passed to its {@link ColumnBinding} to build the
 * runtime column and contribute any per-session UI.
 *
 * <p>
 * What the column shows is described twice over: {@link #type()} says what kind of value a cell
 * holds, {@link #value()} reads that value from a row. Neither assumes a model attribute, so a
 * column over a computed value is described the same way as one over an attribute.
 * </p>
 *
 * @param name
 *        The column name, which for a column over a model attribute is the attribute name.
 * @param label
 *        The resolved column header label.
 * @param type
 *        What the column's values are, deciding its display, sort, filter and search.
 * @param value
 *        Reads the cell value from a row.
 * @param viewContext
 *        The per-session context, e.g. for resolving channel references.
 * @param binding
 *        The strategy turning this descriptor into a column (and optional UI).
 * @param width
 *        The configured default display width in pixels, or {@code 0} to keep the width the column
 *        brings itself.
 * @param editing
 *        How a cell of this column is edited, or {@code null} for a column that is displayed but
 *        not edited.
 * @param aggregate
 *        What the column shows for a group of rows, computed from the group's member rows, or
 *        {@code null} for a column that leaves its group cell empty.
 * @param hiddenByDefault
 *        Whether the column is displayed only once the user selects it in the column selection.
 */
public record ColumnSetup(
		String name,
		ResKey label,
		ColumnType type,
		Function<Object, Object> value,
		ViewContext viewContext,
		ColumnBinding binding,
		int width,
		CellEditing editing,
		Function<List<Object>, Object> aggregate,
		boolean hiddenByDefault) {

	/**
	 * Creates a {@link ColumnSetup} of a column that is displayed from the start, is not edited and
	 * aggregates nothing.
	 *
	 * @see ColumnSetup The full descriptor.
	 */
	public ColumnSetup(String name, ResKey label, ColumnType type, Function<Object, Object> value,
			ViewContext viewContext, ColumnBinding binding, int width) {
		this(name, label, type, value, viewContext, binding, width, null, null, false);
	}

	/**
	 * The runtime column for this descriptor: the column its {@link #binding()} builds, displayed
	 * in the {@link #width() configured width} when there is one, and showing the
	 * {@link #aggregate()} in the header row of a group when it computes one.
	 */
	public Column<Object, ?> buildColumn() {
		return decorate(binding().createColumn(this));
	}

	/**
	 * The given column with what this descriptor decides over the column its binding built.
	 */
	private <V> Column<Object, V> decorate(Column<Object, V> column) {
		Column<Object, V> result = column;
		if (width > 0) {
			result = DelegatingColumn.withDefaultWidth(result, width);
		}
		if (aggregate != null) {
			ColumnType columnType = type;
			Function<List<Object>, Object> function = aggregate;
			Aggregator<Object, V> aggregator =
				group -> ColumnProviderService.displayContent(columnType, function.apply(group.members()));
			result = new DelegatingColumn<>(result) {
				@Override
				public Optional<Aggregator<Object, V>> aggregate() {
					return Optional.of(aggregator);
				}
			};
		}
		return result;
	}

}
