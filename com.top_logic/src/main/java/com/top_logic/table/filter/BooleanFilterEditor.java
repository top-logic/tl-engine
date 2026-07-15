/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.table.FilterState;

/**
 * {@link FilterEditor} for a {@link BooleanColumnFilter}: three checkboxes selecting which
 * of {@code true} / {@code false} / no-value are accepted. The {@code true} / {@code false}
 * checkboxes are labelled as the filter's column renders those values.
 */
public class BooleanFilterEditor implements FilterEditor {

	private final AbstractFieldModel _acceptTrue;

	private final AbstractFieldModel _acceptFalse;

	private final AbstractFieldModel _acceptNull;

	private final ResKey _trueLabel;

	private final ResKey _falseLabel;

	/**
	 * Creates a {@link BooleanFilterEditor} seeded from the current state (or all unchecked).
	 *
	 * @param trueLabel
	 *        Label for the {@code true} option (matching the column's value rendering).
	 * @param falseLabel
	 *        Label for the {@code false} option.
	 */
	public BooleanFilterEditor(BooleanFilterState current, ResKey trueLabel, ResKey falseLabel) {
		_acceptTrue = new AbstractFieldModel(current != null && current.acceptTrue());
		_acceptFalse = new AbstractFieldModel(current != null && current.acceptFalse());
		_acceptNull = new AbstractFieldModel(current != null && current.acceptNull());
		_trueLabel = trueLabel;
		_falseLabel = falseLabel;
	}

	@Override
	public List<FilterField> fields() {
		return List.of(
			new FilterField(_trueLabel, _acceptTrue),
			new FilterField(_falseLabel, _acceptFalse),
			new FilterField(I18NConstants.VALUE_NONE, _acceptNull));
	}

	@Override
	public FilterState read() {
		return new BooleanFilterState(
			TextFilterEditor.bool(_acceptTrue),
			TextFilterEditor.bool(_acceptFalse),
			TextFilterEditor.bool(_acceptNull));
	}

}
