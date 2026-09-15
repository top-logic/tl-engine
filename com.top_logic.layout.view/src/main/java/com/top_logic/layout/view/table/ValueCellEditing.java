/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FunctionFieldModel;

/**
 * {@link CellEditing} of a cell whose value is read from the row and written back to it by
 * functions.
 *
 * <p>
 * Nothing but the two functions ties such a cell to its row, so a column over a value no attribute
 * holds is edited like one over an attribute: what the cell shows is what the value function
 * yields, and an edit is what the update function does with it.
 * </p>
 *
 * <p>
 * The control the value is entered with follows from the type of the column's values, which
 * therefore has to be known: a column whose values are of an unknown type cannot be edited.
 * </p>
 */
public class ValueCellEditing implements CellEditing {

	private final ColumnType _type;

	private final Function<Object, Object> _value;

	private final BiConsumer<Object, Object> _update;

	private final Predicate<Object> _canUpdate;

	/**
	 * Creates a {@link ValueCellEditing}.
	 *
	 * @param type
	 *        What the column's values are, deciding which control edits them. Must be
	 *        {@link ColumnType#resolved() resolved}.
	 * @param value
	 *        Reads the cell value from a row, as the column displays it.
	 * @param update
	 *        Writes an edited value, receiving the row and the new value.
	 * @param canUpdate
	 *        Which rows offer the edit.
	 */
	public ValueCellEditing(ColumnType type, Function<Object, Object> value, BiConsumer<Object, Object> update,
			Predicate<Object> canUpdate) {
		_type = type;
		_value = value;
		_update = update;
		_canUpdate = canUpdate;
	}

	@Override
	public boolean canEdit(Object row) {
		return _canUpdate.test(row);
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form) {
		return new FunctionFieldModel(row, _value, _update);
	}

	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		// The column header says what the cell holds, so the field itself carries no label.
		FieldSpec field =
			FieldControlService.fieldSpec(_type.type(), _type.annotations(), null, _type.multiple(), model);
		return FieldControlService.getInstance().createFieldControl(context, _type.type(), field, model);
	}

}
