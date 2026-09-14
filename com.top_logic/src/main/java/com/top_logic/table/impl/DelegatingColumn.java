/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Sort;

/**
 * A {@link Column} passing every aspect on to an inner column.
 *
 * <p>
 * The base class for a column that adopts another column and changes single aspects of it: a
 * subclass overrides the methods it wants to decide itself and inherits the rest from the column it
 * wraps. Every method of {@link Column} is delegated, the ones with a default implementation
 * included, so a wrapped column keeps whatever it implements itself.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 * @param <V>
 *        The cell value type.
 */
public class DelegatingColumn<R, V> implements Column<R, V> {

	private final Column<R, V> _inner;

	/**
	 * Creates a {@link DelegatingColumn} passing everything on to the given column.
	 *
	 * @param inner
	 *        The column this one takes its aspects from.
	 */
	public DelegatingColumn(Column<R, V> inner) {
		_inner = inner;
	}

	/**
	 * The column this one takes the aspects it does not decide itself from.
	 */
	protected final Column<R, V> inner() {
		return _inner;
	}

	@Override
	public String name() {
		return _inner.name();
	}

	@Override
	public ResKey label() {
		return _inner.label();
	}

	@Override
	public V value(R row) {
		return _inner.value(row);
	}

	@Override
	public CellRenderer<V> renderer() {
		return _inner.renderer();
	}

	@Override
	public CellContent renderCell(R row) {
		return _inner.renderCell(row);
	}

	@Override
	public String searchText(R row) {
		return _inner.searchText(row);
	}

	@Override
	public Optional<Sort<V>> sort() {
		return _inner.sort();
	}

	@Override
	public Optional<ColumnFilter<V>> filter() {
		return _inner.filter();
	}

	@Override
	public Optional<Aggregator<R, V>> aggregate() {
		return _inner.aggregate();
	}

	@Override
	public int defaultWidth() {
		return _inner.defaultWidth();
	}

	@Override
	public boolean frozenEligible() {
		return _inner.frozenEligible();
	}

	@Override
	public boolean selectable() {
		return _inner.selectable();
	}

	@Override
	public boolean pinnedEnd() {
		return _inner.pinnedEnd();
	}

	@Override
	public String cssClass() {
		return _inner.cssClass();
	}

	@Override
	public String cssClass(R row) {
		return _inner.cssClass(row);
	}

	@Override
	public Optional<CellExistence<R>> existence() {
		return _inner.existence();
	}

	/**
	 * The given column with another {@link Column#defaultWidth() default width}.
	 *
	 * @param column
	 *        The column to display in the given width.
	 * @param width
	 *        The default display width in pixels.
	 * @return A column identical to the given one, except for its
	 *         {@link Column#defaultWidth() default width}.
	 */
	public static <R, V> Column<R, V> withDefaultWidth(Column<R, V> column, int width) {
		return new DelegatingColumn<>(column) {
			@Override
			public int defaultWidth() {
				return width;
			}
		};
	}

}
