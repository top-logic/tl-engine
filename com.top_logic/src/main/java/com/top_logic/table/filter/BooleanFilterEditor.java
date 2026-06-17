/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.table.FilterState;

/**
 * {@link FilterEditor} for a {@link BooleanColumnFilter}: three checkboxes selecting which
 * of {@code true} / {@code false} / no-value are accepted.
 */
public class BooleanFilterEditor implements FilterEditor {

	private final AbstractFieldModel _acceptTrue;

	private final AbstractFieldModel _acceptFalse;

	private final AbstractFieldModel _acceptNull;

	/**
	 * Creates a {@link BooleanFilterEditor} seeded from the current state (or all unchecked).
	 */
	public BooleanFilterEditor(BooleanFilterState current) {
		_acceptTrue = new AbstractFieldModel(current != null && current.acceptTrue());
		_acceptFalse = new AbstractFieldModel(current != null && current.acceptFalse());
		_acceptNull = new AbstractFieldModel(current != null && current.acceptNull());
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(I18NConstants.VALUE_TRUE, _acceptTrue, FilterFieldKind.CHECKBOX),
			new FilterField(I18NConstants.VALUE_FALSE, _acceptFalse, FilterFieldKind.CHECKBOX),
			new FilterField(I18NConstants.VALUE_NONE, _acceptNull, FilterFieldKind.CHECKBOX));
	}

	@Override
	public FilterState read() {
		return new BooleanFilterState(
			TextFilterEditor.bool(_acceptTrue),
			TextFilterEditor.bool(_acceptFalse),
			TextFilterEditor.bool(_acceptNull));
	}

}
