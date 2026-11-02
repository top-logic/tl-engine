/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.EditLevel;
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

	/**
	 * How a cell written back by the given update is edited: not at all for a column writing
	 * nothing back, and not at all for one whose values are of an unknown type - which control
	 * enters a value follows from that type.
	 *
	 * <p>
	 * This is the one place deciding whether a column over a computed value offers an edit, so that
	 * a single column and a whole set of them answer it alike.
	 * </p>
	 *
	 * @param type
	 *        What the column's values are, deciding which control edits them.
	 * @param value
	 *        Reads the cell value from a row, as the column displays it.
	 * @param update
	 *        Writes an edited value, receiving the row and the new value, or {@code null} for a
	 *        column that is displayed but not edited.
	 * @param canUpdate
	 *        Which rows offer the edit, or {@code null} where every row of the column does.
	 */
	public static CellEditing forUpdate(ColumnType type, Function<Object, Object> value,
			BiConsumer<Object, Object> update, Predicate<Object> canUpdate) {
		if (update == null || !type.resolved()) {
			return null;
		}
		return new ValueCellEditing(type, value, update, canUpdate == null ? row -> true : canUpdate);
	}

	@Override
	public boolean canEdit(Object row) {
		return _canUpdate.test(row);
	}

	@Override
	public BoundFieldModel createModel(Object row, FormControl form, EditLevel level) {
		return new FunctionFieldModel(row, _value, _update);
	}

	/**
	 * Creates the field model of a cell edited in place, outside a form: an entered value is
	 * written at once, in a transaction of its own.
	 *
	 * <p>
	 * There is no buffer to apply on save, so the update function writes where the value lives
	 * when the user enters it. A failing write is rolled back and leaves the field as it was.
	 * </p>
	 *
	 * @param row
	 *        The row whose cell is edited, for which {@link #canEdit(Object)} holds.
	 * @param written
	 *        Called after a value was written, when the field shows it, e.g. to build the other
	 *        cells of the row again.
	 */
	public BoundFieldModel createInPlaceModel(Object row, Runnable written) {
		BiConsumer<Object, Object> update = _update;
		return new FunctionFieldModel(row, _value, (object, value) -> inTransaction(() -> update.accept(object, value))) {
			@Override
			public void setValue(Object value) {
				super.setValue(value);
				written.run();
			}
		};
	}

	/**
	 * Creates the field model of a cell edited in place that its row does not offer the edit on:
	 * the cell shows its value with the control of the column, but disabled.
	 *
	 * <p>
	 * A matrix edited in place thereby looks the same in every cell - a check box -, while a cell
	 * whose value is fixed (derived from another one, say) is visibly and semantically not
	 * editable.
	 * </p>
	 *
	 * @param row
	 *        The row whose cell is displayed, for which {@link #canEdit(Object)} does not hold.
	 */
	public BoundFieldModel createReadOnlyModel(Object row) {
		BoundFieldModel result = new FunctionFieldModel(row, _value, (object, value) -> {
			throw new IllegalStateException("The cell is not editable: " + object);
		});
		// Disabled rather than only read-only, so that the control looks and announces itself as
		// not editable.
		result.setEditable(false);
		result.setDisabled(true);
		return result;
	}

	/**
	 * The value the cell of the given row shows.
	 */
	public Object value(Object row) {
		return _value.apply(row);
	}

	private static void inTransaction(Runnable write) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			write.run();
			tx.commit();
		}
	}

	@Override
	public ReactControl createControl(ReactContext context, Object row, BoundFieldModel model) {
		// The column header says what the cell holds, so the field itself carries no label.
		FieldSpec field = FieldControlService.cellSpec(
			FieldControlService.fieldSpec(_type.type(), _type.annotations(), null, _type.multiple(), model));
		return FieldControlService.getInstance().createFieldControl(context, _type.type(), field, model);
	}

}
