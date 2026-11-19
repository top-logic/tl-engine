/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.function.Predicate;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.form.AttributeChanges;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.FormControl;

/**
 * {@link CellEditing} offering the edit of another one only on the rows a condition accepts as
 * well.
 *
 * @see CellEditing#restrictedTo(Predicate)
 */
final class RestrictedCellEditing implements CellEditing {

	private final CellEditing _editing;

	private final Predicate<Object> _rows;

	/**
	 * Creates a {@link RestrictedCellEditing}.
	 *
	 * @param editing
	 *        How a cell is edited on a row offering the edit.
	 * @param rows
	 *        Whether a row offers the edit, asked only for a row the given editing offers it on.
	 */
	RestrictedCellEditing(CellEditing editing, Predicate<Object> rows) {
		_editing = editing;
		_rows = rows;
	}

	@Override
	public boolean canEdit(Object row) {
		return _editing.canEdit(row) && _rows.test(row);
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form, EditLevel level) {
		return _editing.createModel(row, form, level);
	}

	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		return _editing.createControl(context, row, model);
	}

	@Override
	public void followRow(Object row, BoundFieldModel model, AttributeChanges rowChanges) {
		_editing.followRow(row, model, rowChanges);
	}

	@Override
	public CellEditing restrictedTo(Predicate<Object> rows) {
		return new RestrictedCellEditing(_editing, _rows.and(rows));
	}

}
