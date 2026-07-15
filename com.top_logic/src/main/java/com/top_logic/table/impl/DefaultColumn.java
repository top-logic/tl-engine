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
import com.top_logic.table.CellEditor;
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
 * from the name are used unless overridden. Capabilities (sort, filter, editor, aggregate)
 * are optional and absent unless set.
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

	private final Sort<V> _sort;

	private final ColumnFilter<V> _filter;

	private final CellEditor<R, V> _editor;

	private final Aggregator<R, V> _aggregate;

	private final int _width;

	private final boolean _frozenEligible;

	private final Function<? super R, String> _css;

	private final CellExistence<R> _existence;

	private DefaultColumn(Builder<R, V> builder) {
		_name = builder._name;
		_label = builder._label != null ? builder._label : ResKey.text(builder._name);
		_value = builder._value;
		_renderer = builder._renderer != null ? builder._renderer
			: value -> CellContent.text(String.valueOf(value));
		_sort = builder._sort;
		_filter = builder._filter;
		_editor = builder._editor;
		_aggregate = builder._aggregate;
		_width = builder._width;
		_frozenEligible = builder._frozenEligible;
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

	@Override
	public Optional<Sort<V>> sort() {
		return Optional.ofNullable(_sort);
	}

	@Override
	public Optional<ColumnFilter<V>> filter() {
		return Optional.ofNullable(_filter);
	}

	@Override
	public Optional<CellEditor<R, V>> editor() {
		return Optional.ofNullable(_editor);
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

		Sort<V> _sort;

		ColumnFilter<V> _filter;

		CellEditor<R, V> _editor;

		Aggregator<R, V> _aggregate;

		int _width = 150;

		boolean _frozenEligible = true;

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
		 * Makes the column inline-editable with the given editor.
		 */
		public Builder<R, V> editor(CellEditor<R, V> editor) {
			_editor = editor;
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
		 * Sets a per-row CSS class provider.
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
