/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.ColumnFilterState;
import com.top_logic.table.FilterState;
import com.top_logic.table.MatchCounts;

/**
 * The {@link FilterEditor} of a column: the editor of its active {@link Column#filters() kind of
 * filter} plus an "invert" checkbox.
 *
 * <p>
 * The column is filtered by one of its kinds at a time. {@link #setActiveKind(String)} switches
 * the kind the editor edits; the editors of the kinds keep their input while another kind is
 * active, so switching back restores it. {@link #read()} produces the state of the active kind in
 * the canonical form of a {@link ColumnFilterState}.
 * </p>
 */
public class ColumnFilterEditor implements FilterEditor {

	private final List<? extends ColumnFilter<?>> _kinds;

	private final Function<String, MatchCounts> _counts;

	private final Map<String, FilterEditor> _editors = new HashMap<>();

	private final AbstractFieldModel _invert;

	private final String _initialKind;

	private final FilterState _initialState;

	private String _activeKind;

	/**
	 * Creates a {@link ColumnFilterEditor}.
	 *
	 * @param kinds
	 *        The kinds of filter of the column, see {@link Column#filters()}; not empty.
	 * @param current
	 *        The current state of the column, or {@code null} if the column is not filtered.
	 * @param counts
	 *        The facet counts of the column as a given {@link ColumnFilter#kind() kind} buckets
	 *        the values, or {@link MatchCounts#NONE}.
	 */
	public ColumnFilterEditor(List<? extends ColumnFilter<?>> kinds, FilterState current,
			Function<String, MatchCounts> counts) {
		if (kinds.isEmpty()) {
			throw new IllegalArgumentException("A column without filter has no filter editor.");
		}
		_kinds = List.copyOf(kinds);
		_counts = counts;
		ColumnFilter<?> currentKind = current == null ? null : find(ColumnFilterState.kindOf(current));
		if (currentKind == null) {
			// A state of a kind the column does not offer (any longer) is not edited.
			_initialKind = _kinds.get(0).kind();
			_initialState = null;
		} else {
			_initialKind = currentKind.kind();
			_initialState = ColumnFilterState.innerOf(current);
		}
		_activeKind = _initialKind;
		_invert = new AbstractFieldModel(currentKind != null && ColumnFilterState.isInverted(current));
	}

	/**
	 * The kinds of filter this editor switches between.
	 */
	public List<? extends ColumnFilter<?>> kinds() {
		return _kinds;
	}

	/**
	 * The {@link ColumnFilter#kind()} of the kind of filter currently edited.
	 */
	public String activeKind() {
		return _activeKind;
	}

	/**
	 * Switches to the given kind of filter.
	 *
	 * @param kind
	 *        The {@link ColumnFilter#kind()} of one of the {@link #kinds()}.
	 */
	public void setActiveKind(String kind) {
		if (find(kind) == null) {
			throw new IllegalArgumentException("No filter of kind '" + kind + "'.");
		}
		_activeKind = kind;
	}

	/**
	 * The editor of the {@link #activeKind() active kind of filter}.
	 */
	public FilterEditor activeEditor() {
		return _editors.computeIfAbsent(_activeKind, this::createEditor);
	}

	private FilterEditor createEditor(String kind) {
		FilterState seed = kind.equals(_initialKind) ? _initialState : null;
		return FilterEditors.kindEditor(find(kind), seed, _counts.apply(kind));
	}

	/**
	 * The fields of the {@link #activeEditor() active editor}, followed by the "invert" checkbox.
	 */
	@Override
	public List<FilterField> fields() {
		List<FilterField> fields = new ArrayList<>(activeEditor().fields());
		fields.add(new FilterField(I18NConstants.FILTER_INVERT, _invert));
		return fields;
	}

	@Override
	public FilterState read() {
		FilterState inner = activeEditor().read();
		// Inverting an empty selection stays empty (an inactive filter); only wrap a real selection.
		if (inner.isEmpty()) {
			return inner;
		}
		return ColumnFilterState.of(_kinds, _activeKind, inner, TextFilterEditor.bool(_invert));
	}

	private ColumnFilter<?> find(String kind) {
		if (kind == null) {
			return _kinds.get(0);
		}
		for (ColumnFilter<?> filter : _kinds) {
			if (filter.kind().equals(kind)) {
				return filter;
			}
		}
		return null;
	}

}
