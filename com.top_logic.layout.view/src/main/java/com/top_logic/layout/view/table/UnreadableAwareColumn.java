/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.function.Function;

import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.impl.DelegatingColumn;

/**
 * A {@link Column} built by a {@link ColumnProvider} of an application, showing the cells of a
 * value the user may not read by the placeholder of the {@link ColumnProviderService#UNREADABLE}
 * marker.
 *
 * <p>
 * Such a provider knows nothing of the marker: it is given the readable values only, an unreadable
 * cell reaches it as an empty one. Its sort and filter therefore see an unreadable cell as empty -
 * the documented exception to the built-in columns, which sort it after every value and match it by
 * no criterion. What the cell displays and is searched by is the placeholder, decided here before
 * the provider's column is asked.
 * </p>
 *
 * @param <V>
 *        The type of the values of the wrapped column.
 */
final class UnreadableAwareColumn<V> extends DelegatingColumn<Object, V> {

	private final Function<Object, Object> _value;

	/**
	 * Creates an {@link UnreadableAwareColumn}.
	 *
	 * @param inner
	 *        The column built by the provider.
	 * @param value
	 *        Reads the cell value from a row, possibly the
	 *        {@link ColumnProviderService#UNREADABLE} marker.
	 */
	UnreadableAwareColumn(Column<Object, V> inner, Function<Object, Object> value) {
		super(inner);
		_value = value;
	}

	@Override
	public CellContent renderCell(Object row) {
		return isUnreadable(row) ? CellContent.text(placeholder()) : inner().renderCell(row);
	}

	@Override
	public String searchText(Object row) {
		return isUnreadable(row) ? placeholder() : inner().searchText(row);
	}

	private boolean isUnreadable(Object row) {
		return ColumnProviderService.isUnreadable(_value.apply(row));
	}

	private static String placeholder() {
		return ColumnProviderService.label(ColumnProviderService.UNREADABLE);
	}

}
