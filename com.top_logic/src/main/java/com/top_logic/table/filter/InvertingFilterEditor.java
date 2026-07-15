/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;

/**
 * A {@link FilterEditor} decorator that adds an "invert" checkbox below an inner editor's fields.
 *
 * <p>
 * When the checkbox is set, {@link #read()} wraps the inner editor's state in a
 * {@link NegatedFilterState} so the column accepts exactly the rows the inner filter rejects. The
 * decorator is type-agnostic: it composes with any inner editor whose filter
 * {@link com.top_logic.table.ColumnFilter#supportsInversion() supports inversion}.
 * </p>
 */
public class InvertingFilterEditor implements FilterEditor {

	private final FilterEditor _inner;

	private final AbstractFieldModel _invert;

	/**
	 * Creates an {@link InvertingFilterEditor}.
	 *
	 * @param inner
	 *        The wrapped editor providing the value fields.
	 * @param inverted
	 *        The initial state of the invert checkbox.
	 */
	public InvertingFilterEditor(FilterEditor inner, boolean inverted) {
		_inner = inner;
		_invert = new AbstractFieldModel(inverted);
	}

	@Override
	public List<FilterField> fields() {
		List<FilterField> fields = new ArrayList<>(_inner.fields());
		fields.add(new FilterField(I18NConstants.FILTER_INVERT, _invert));
		return fields;
	}

	@Override
	public FilterState read() {
		FilterState inner = _inner.read();
		// Inverting an empty selection stays empty (an inactive filter); only wrap a real selection.
		if (inner.isEmpty() || !TextFilterEditor.bool(_invert)) {
			return inner;
		}
		return new NegatedFilterState(inner);
	}

}
