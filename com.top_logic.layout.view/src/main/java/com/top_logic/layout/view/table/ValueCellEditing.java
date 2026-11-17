/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.view.form.AttributeOptions;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FunctionFieldModel;
import com.top_logic.layout.view.form.FunctionSelectFieldModel;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.query.QueryExecutor;

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
 * therefore has to be known: a column whose values are of an unknown type cannot be edited. A value
 * chosen from options - an object of a class, a classifier of an enumeration - is chosen from the
 * options computed for the row when its cell is edited.
 * </p>
 */
public class ValueCellEditing implements CellEditing {

	/**
	 * The options the value of a cell is chosen from.
	 *
	 * @param forRow
	 *        Computes the options of the cell of a row, when the cell goes into editing.
	 * @param asComputed
	 *        Whether the options are offered in the order they are computed in, rather than in the
	 *        order of the type of the values, see {@link AttributeOptions#orderFor(TLType)}.
	 */
	public record Options(Function<Object, List<?>> forRow, boolean asComputed) {
		// Pure data.
	}

	private final ColumnType _type;

	private final Function<Object, Object> _value;

	private final BiConsumer<Object, Object> _update;

	private final Predicate<Object> _canUpdate;

	private final Options _options;

	/**
	 * Creates a {@link ValueCellEditing} of a value that is entered rather than chosen from
	 * options.
	 *
	 * @see #ValueCellEditing(ColumnType, Function, BiConsumer, Predicate, Options)
	 */
	public ValueCellEditing(ColumnType type, Function<Object, Object> value, BiConsumer<Object, Object> update,
			Predicate<Object> canUpdate) {
		this(type, value, update, canUpdate, null);
	}

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
	 * @param options
	 *        The options the value of a row is chosen from, {@code null} for a value that is
	 *        entered rather than chosen.
	 */
	public ValueCellEditing(ColumnType type, Function<Object, Object> value, BiConsumer<Object, Object> update,
			Predicate<Object> canUpdate, Options options) {
		_type = type;
		_value = value;
		_update = update;
		_canUpdate = canUpdate;
		_options = options;
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
	 * @param options
	 *        The options the value of a row is chosen from, {@code null} for a value that is
	 *        entered rather than chosen.
	 */
	public static CellEditing forUpdate(ColumnType type, Function<Object, Object> value,
			BiConsumer<Object, Object> update, Predicate<Object> canUpdate, Options options) {
		if (update == null || !type.resolved()) {
			return null;
		}
		return new ValueCellEditing(type, value, update, canUpdate == null ? row -> true : canUpdate, options);
	}

	/**
	 * The options the edited value of a row is chosen from, or {@code null} where a value of the
	 * given type is entered rather than chosen.
	 *
	 * <p>
	 * A value of a class or an enumeration is chosen from what the given expression computes for
	 * the row, in the order it computes them, or, without an expression, from the options of the
	 * type, in the order of the type, see
	 * {@link AttributeOptions#optionsFor(TLType, QueryExecutor, Object...)}.
	 * </p>
	 *
	 * @param type
	 *        What the column's values are.
	 * @param options
	 *        The expression computing the options, {@code null} for the options of the type.
	 * @param arguments
	 *        The arguments the expression is applied to for a row.
	 */
	public static Options options(ColumnType type, QueryExecutor options, Function<Object, Object[]> arguments) {
		if (!type.resolved() || !AttributeOptions.isStructuralSelect(type.type())) {
			return null;
		}
		TLType valueType = type.type();
		return new Options(row -> AttributeOptions.optionsFor(valueType, options, arguments.apply(row)),
			options != null);
	}

	@Override
	public boolean canEdit(Object row) {
		return _canUpdate.test(row);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * A value chosen from options is edited by a {@link FunctionSelectFieldModel} offering the
	 * options computed for the row now.
	 * </p>
	 */
	@Override
	public BoundFieldModel createModel(Object row, FormControl form, EditLevel level) {
		return newModel(row, _update, options(row));
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
		FunctionFieldModel result =
			newModel(row, (object, value) -> inTransaction(() -> update.accept(object, value)), options(row));
		result.setAfterSetValue(written);
		return result;
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
		// Nothing is chosen, so no options are offered besides the value shown.
		BoundFieldModel result = newModel(row, (object, value) -> {
			throw new IllegalStateException("The cell is not editable: " + object);
		}, _options == null ? null : Collections.emptyList());
		// Disabled rather than only read-only, so that the control looks and announces itself as
		// not editable.
		result.setEditable(false);
		result.setDisabled(true);
		return result;
	}

	/**
	 * The options the value of the given row is chosen from, {@code null} for a value that is
	 * entered.
	 */
	private List<?> options(Object row) {
		return _options == null ? null : _options.forRow().apply(row);
	}

	/**
	 * The field model of the cell of the given row, writing through the given function.
	 *
	 * @param options
	 *        The options the value is chosen from, {@code null} for a value that is entered.
	 */
	private FunctionFieldModel newModel(Object row, BiConsumer<Object, Object> write, List<?> options) {
		if (options == null) {
			return new FunctionFieldModel(row, _value, write);
		}
		return new FunctionSelectFieldModel(row, _value, write, options, _type.multiple());
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
		if (_options != null && _options.asComputed()) {
			field.setOptionOrder(null);
		}
		return FieldControlService.getInstance().createFieldControl(context, _type.type(), field, model);
	}

}
