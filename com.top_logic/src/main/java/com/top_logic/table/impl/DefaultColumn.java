/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.Optional;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Sort;

/**
 * A straightforward, immutable {@link Column} implementation built via a fluent
 * {@link Builder}.
 *
 * <p>
 * Required: a {@link #name()} and a value function. A text renderer and a label derived
 * from the name are used unless overridden. Capabilities (sort, filter, aggregate) are
 * optional and absent unless set.
 * </p>
 *
 * @param <R>
 *        The row business object type.
 * @param <V>
 *        The cell value type.
 */
public final class DefaultColumn<R, V> implements Column<R, V> {

	private final String _name;

	private final ResKey _label;

	private final Function<? super R, ? extends V> _value;

	private final CellRenderer<V> _renderer;

	private final Function<? super V, String> _searchText;

	private final Sort<V> _sort;

	private final ColumnFilter<V> _filter;

	private final Aggregator<R, V> _aggregate;

	private final int _width;

	private final boolean _frozenEligible;

	private final boolean _selectable;

	private final boolean _pinnedEnd;

	private final String _cssClass;

	private final Function<? super R, String> _css;

	private final CellExistence<R> _existence;

	private DefaultColumn(Builder<R, V> builder) {
		_name = builder._name;
		_label = builder._label != null ? builder._label : ResKey.text(builder._name);
		_value = builder._value;
		_renderer = builder._renderer != null ? builder._renderer
			: value -> CellContent.text(String.valueOf(value));
		_searchText = builder._searchText;
		_sort = builder._sort;
		_filter = builder._filter;
		_aggregate = builder._aggregate;
		_width = builder._width;
		_pinnedEnd = builder._pinnedEnd;
		// A pinned column is the table's own: it sits at the end whatever the user arranges, and it
		// is visible there at every scroll position already.
		_frozenEligible = builder._frozenEligible && !_pinnedEnd;
		_selectable = builder._selectable && !_pinnedEnd;
		_cssClass = builder._cssClass;
		_css = builder._css;
		_existence = builder._existence;
	}

	@Override
	public String name() {
		return _name;
	}

	@Override
	public ResKey label() {
		return _label;
	}

	@Override
	public V value(R row) {
		return _value.apply(row);
	}

	@Override
	public CellRenderer<V> renderer() {
		return _renderer;
	}

	/**
	 * The text of the cell value as {@link Builder#searchText(Function) configured}, or the text of
	 * the rendered cell content when this column configures none.
	 */
	@Override
	public String searchText(R row) {
		return _searchText != null ? _searchText.apply(value(row)) : Column.super.searchText(row);
	}

	@Override
	public Optional<Sort<V>> sort() {
		return Optional.ofNullable(_sort);
	}

	@Override
	public Optional<ColumnFilter<V>> filter() {
		return Optional.ofNullable(_filter);
	}

	@Override
	public Optional<Aggregator<R, V>> aggregate() {
		return Optional.ofNullable(_aggregate);
	}

	@Override
	public int defaultWidth() {
		return _width;
	}

	@Override
	public boolean frozenEligible() {
		return _frozenEligible;
	}

	@Override
	public boolean selectable() {
		return _selectable;
	}

	@Override
	public boolean pinnedEnd() {
		return _pinnedEnd;
	}

	@Override
	public String cssClass() {
		return _cssClass;
	}

	@Override
	public String cssClass(R row) {
		return _css == null ? null : _css.apply(row);
	}

	@Override
	public Optional<CellExistence<R>> existence() {
		return Optional.ofNullable(_existence);
	}

	/**
	 * Starts building a {@link DefaultColumn}.
	 *
	 * @param name
	 *        The unique column name.
	 * @param value
	 *        Extracts the cell value from a row.
	 */
	public static <R, V> Builder<R, V> builder(String name, Function<? super R, ? extends V> value) {
		return new Builder<>(name, value);
	}

	/**
	 * Fluent builder for {@link DefaultColumn}.
	 *
	 * @param <R>
	 *        The row business object type.
	 * @param <V>
	 *        The cell value type.
	 */
	public static final class Builder<R, V> {

		final String _name;

		final Function<? super R, ? extends V> _value;

		ResKey _label;

		CellRenderer<V> _renderer;

		Function<? super V, String> _searchText;

		Sort<V> _sort;

		ColumnFilter<V> _filter;

		Aggregator<R, V> _aggregate;

		int _width = 150;

		String _cssClass;

		boolean _frozenEligible = true;

		boolean _selectable = true;

		boolean _pinnedEnd;

		Function<? super R, String> _css;

		CellExistence<R> _existence;

		Builder(String name, Function<? super R, ? extends V> value) {
			_name = name;
			_value = value;
		}

		/**
		 * Sets the column header label.
		 */
		public Builder<R, V> label(ResKey label) {
			_label = label;
			return this;
		}

		/**
		 * Sets the cell renderer.
		 */
		public Builder<R, V> renderer(CellRenderer<V> renderer) {
			_renderer = renderer;
			return this;
		}

		/**
		 * Sets the text of a cell value the free-text {@link com.top_logic.table.SearchSpec search}
		 * examines.
		 *
		 * <p>
		 * A column whose {@link #renderer(CellRenderer) renderer} produces a control instead of
		 * text ({@link CellContent.Raw}) takes part in a search only through this text: the
		 * rendered content carries none. Set it to the same text the column displays, so that the
		 * search finds what the user reads.
		 * </p>
		 */
		public Builder<R, V> searchText(Function<? super V, String> searchText) {
			_searchText = searchText;
			return this;
		}

		/**
		 * Makes the column sortable with the given sort capability.
		 */
		public Builder<R, V> sort(Sort<V> sort) {
			_sort = sort;
			return this;
		}

		/**
		 * Makes the column filterable with the given filter capability.
		 */
		public Builder<R, V> filter(ColumnFilter<V> filter) {
			_filter = filter;
			return this;
		}

		/**
		 * Adds an aggregation (footer/group total) capability.
		 */
		public Builder<R, V> aggregate(Aggregator<R, V> aggregate) {
			_aggregate = aggregate;
			return this;
		}

		/**
		 * Sets the default display width in pixels.
		 */
		public Builder<R, V> width(int width) {
			_width = width;
			return this;
		}

		/**
		 * Sets whether the column may be frozen.
		 */
		public Builder<R, V> frozenEligible(boolean frozenEligible) {
			_frozenEligible = frozenEligible;
			return this;
		}

		/**
		 * Sets whether the user may show, hide and move the column.
		 *
		 * @see Column#selectable()
		 */
		public Builder<R, V> selectable(boolean selectable) {
			_selectable = selectable;
			return this;
		}

		/**
		 * Sets whether the column keeps its place at the end of the table.
		 *
		 * <p>
		 * A pinned column is neither {@link #frozenEligible(boolean) frozen} nor
		 * {@link #selectable(boolean) selectable}, whatever those are set to.
		 * </p>
		 *
		 * @see Column#pinnedEnd()
		 */
		public Builder<R, V> pinnedEnd(boolean pinnedEnd) {
			_pinnedEnd = pinnedEnd;
			return this;
		}

		/**
		 * Sets the CSS class every cell of the column carries.
		 *
		 * @see Column#cssClass()
		 */
		public Builder<R, V> cssClass(String cssClass) {
			_cssClass = cssClass;
			return this;
		}

		/**
		 * Sets a per-row CSS class provider.
		 *
		 * @see Column#cssClass(Object)
		 */
		public Builder<R, V> css(Function<? super R, String> css) {
			_css = css;
			return this;
		}

		/**
		 * Sets a cell-existence test gating filter visibility.
		 */
		public Builder<R, V> existence(CellExistence<R> existence) {
			_existence = existence;
			return this;
		}

		/**
		 * Builds the immutable {@link DefaultColumn}.
		 */
		public DefaultColumn<R, V> build() {
			return new DefaultColumn<>(this);
		}

	}

}
