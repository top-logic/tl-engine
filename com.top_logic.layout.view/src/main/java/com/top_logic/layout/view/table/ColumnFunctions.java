/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The functions a column computes its cells from: the value a row shows, what an edit of that value
 * writes, and which rows offer the edit.
 *
 * <p>
 * Every function is an {@link InputScript} and sees the values of the inputs, then the row, then
 * whatever the column binds behind it - the object one column of a computed set stands for, for
 * instance. An update sees the entered value last. A column computing its value from the row alone
 * binds nothing and is described by the same three functions.
 * </p>
 */
public class ColumnFunctions {

	private final QueryExecutor _value;

	private final QueryExecutor _update;

	private final QueryExecutor _canUpdate;

	private final List<InputRef> _inputs;

	/**
	 * Creates a {@link ColumnFunctions} from the declared expressions.
	 *
	 * @param config
	 *        The configuration of the column holding the expressions.
	 * @param value
	 *        The name of the property computing the cell value, applied to every row of the column.
	 * @param update
	 *        The name of the property writing an edited cell value, holding nothing for a column
	 *        that is displayed but not edited.
	 * @param canUpdate
	 *        The name of the property deciding whether the cell of a row is edited, holding nothing
	 *        where every row of an edited column offers the edit.
	 */
	public static ColumnFunctions compile(ConfigurationItem config, String value, String update,
			String canUpdate) {
		return new ColumnFunctions(InputScript.compile(config, value), InputScript.compile(config, update),
			InputScript.compile(config, canUpdate),
			InputScript.inputs(config, config.descriptor().getProperty(value)));
	}

	private ColumnFunctions(QueryExecutor value, QueryExecutor update, QueryExecutor canUpdate,
			List<InputRef> inputs) {
		_value = value;
		_update = update;
		_canUpdate = canUpdate;
		_inputs = inputs;
	}

	/**
	 * Whether an edited cell value is written back, so that the column is edited at all.
	 */
	public boolean updates() {
		return _update != null;
	}

	/**
	 * These functions with the declared inputs resolved to the channels they name.
	 *
	 * @param scope
	 *        What the column is resolved against, holding the session the channels belong to.
	 */
	public Resolved resolve(ColumnResolution scope) {
		return new Resolved(ChannelInputs.resolve(scope.context(), _inputs));
	}

	/**
	 * The functions of a column with its inputs at hand, as the table applies them to its rows.
	 *
	 * <p>
	 * Each of them takes the arguments the column binds behind the row, so that one declaration
	 * standing for a whole set of columns tells its columns apart by what it binds.
	 * </p>
	 */
	public class Resolved {

		private final List<ViewChannel> _channels;

		/**
		 * Creates a {@link Resolved} reading the given channels.
		 */
		Resolved(List<ViewChannel> channels) {
			_channels = channels;
		}

		/**
		 * Reads the cell value from a row.
		 *
		 * @param bound
		 *        The arguments the column binds behind the row.
		 */
		public Function<Object, Object> value(Object... bound) {
			QueryExecutor value = _value;
			return row -> value.execute(arguments(row, bound));
		}

		/**
		 * Writes an edited cell value, receiving the row and the entered value, or {@code null} for
		 * a column that is displayed but not edited.
		 *
		 * @param bound
		 *        The arguments the column binds behind the row, ahead of the entered value.
		 */
		public BiConsumer<Object, Object> update(Object... bound) {
			QueryExecutor update = _update;
			if (update == null) {
				return null;
			}
			return (row, edited) -> update.execute(arguments(row, bound, edited));
		}

		/**
		 * Which rows offer the edit, or {@code null} where every row does.
		 *
		 * @param bound
		 *        The arguments the column binds behind the row.
		 */
		public Predicate<Object> canUpdate(Object... bound) {
			QueryExecutor canUpdate = _canUpdate;
			if (canUpdate == null) {
				return null;
			}
			return row -> SearchExpression.isTrue(canUpdate.execute(arguments(row, bound)));
		}

		/**
		 * How a cell of the column is edited: through the update function, on the rows the
		 * predicate accepts.
		 *
		 * @param type
		 *        What the column's values are, deciding which control enters them.
		 * @param value
		 *        Reads the cell value from a row, so that the edited field shows what the column
		 *        does.
		 * @param bound
		 *        The arguments the column binds behind the row.
		 * @return {@code null} for a column that is not edited, see
		 *         {@link ValueCellEditing#forUpdate(ColumnType, Function, BiConsumer, Predicate)}.
		 */
		public CellEditing editing(ColumnType type, Function<Object, Object> value, Object... bound) {
			return ValueCellEditing.forUpdate(type, value, update(bound), canUpdate(bound));
		}

		/**
		 * The arguments one of the functions is called with: the values of the inputs, the row, what
		 * the column binds, and what the table appends.
		 *
		 * @param row
		 *        The row the function is applied to.
		 * @param bound
		 *        The arguments the column binds behind the row.
		 * @param appended
		 *        The arguments behind those, e.g. the value an update writes.
		 */
		private Object[] arguments(Object row, Object[] bound, Object... appended) {
			Object[] trailing = new Object[1 + bound.length + appended.length];
			trailing[0] = row;
			System.arraycopy(bound, 0, trailing, 1, bound.length);
			System.arraycopy(appended, 0, trailing, 1 + bound.length, appended.length);
			return ChannelInputs.arguments(_channels, trailing);
		}

		/**
		 * The values of the inputs, and the given arguments behind them.
		 *
		 * @param trailing
		 *        The arguments behind the inputs, in the order the function takes them.
		 */
		public Object[] inputs(Object... trailing) {
			return ChannelInputs.arguments(_channels, trailing);
		}

	}

}
