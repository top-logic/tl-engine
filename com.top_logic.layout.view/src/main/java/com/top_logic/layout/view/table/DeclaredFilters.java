/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterState;
import com.top_logic.table.NamedFilter;

/**
 * Turns the filter criteria a table declares into the {@link NamedFilter}s it offers.
 *
 * <p>
 * A declaration names a value per column; each column's own {@link ColumnFilter} translates that
 * value into its {@link FilterState} through {@link ColumnFilter#stateFor(Object)}, so which value
 * shapes a criterion may use is decided by the filter of the column it addresses. The criteria are
 * materialized here, once, when the table is built: a chip the user clicks then applies states that
 * are already computed, and the table can tell which of its named filters is active by comparing
 * them with its live ones.
 * </p>
 *
 * <p>
 * A criterion that cannot be materialized - an unknown column, a column that cannot be filtered, or
 * a value the column's filter cannot express - is a declaration error: it is reported to the
 * {@link Log}, and the named filter it belongs to is not offered rather than offered with a
 * criterion silently missing.
 * </p>
 */
public class DeclaredFilters {

	/**
	 * One criterion of a {@link Declaration}: the value selected in one column.
	 *
	 * @param column
	 *        The {@link Column#name() name} of the column the value applies to.
	 * @param value
	 *        The selected value, evaluated by the declaring tier and handed to the column's
	 *        {@link ColumnFilter#stateFor(Object)}.
	 */
	public record Criterion(String column, Object value) {
		// Pure data carrier.
	}

	/**
	 * The evaluated declaration of one {@link NamedFilter}.
	 *
	 * @param id
	 *        The {@link NamedFilter#id() identifier} of the resulting filter.
	 * @param label
	 *        The name to display.
	 * @param criteria
	 *        The value selected per column.
	 */
	public record Declaration(String id, ResKey label, List<Criterion> criteria) {
		// Pure data carrier.
	}

	/**
	 * Materializes the given declarations over the given columns.
	 *
	 * @param log
	 *        Where declaration errors are reported.
	 * @param table
	 *        How the table is named in an error message, so that a developer finds the declaration.
	 * @param declarations
	 *        The declarations to materialize, in the order the table offers them.
	 * @param columns
	 *        All columns of the table, whose filters translate the criterion values.
	 * @return One {@link NamedFilter.Origin#DECLARED} filter per declaration that materialized
	 *         completely.
	 */
	public static List<NamedFilter> resolve(Log log, String table, List<Declaration> declarations,
			List<? extends Column<?, ?>> columns) {
		Map<String, Column<?, ?>> columnsByName = new LinkedHashMap<>();
		for (Column<?, ?> column : columns) {
			columnsByName.put(column.name(), column);
		}

		List<NamedFilter> result = new ArrayList<>(declarations.size());
		Set<String> ids = new LinkedHashSet<>();
		for (Declaration declaration : declarations) {
			String id = declaration.id();
			if (!ids.add(id)) {
				log.error("Table '" + table + "' declares more than one named filter '" + id
					+ "'. Only the first one is offered.");
				continue;
			}
			Map<String, FilterState> filters = resolveCriteria(log, table, declaration, columnsByName);
			if (filters == null) {
				continue;
			}
			result.add(NamedFilter.declared(id, declaration.label(), filters, null));
		}
		return result;
	}

	/**
	 * The {@link FilterState} per column of the given declaration, or {@code null} if a criterion
	 * could not be materialized (then the problem has been reported).
	 */
	private static Map<String, FilterState> resolveCriteria(Log log, String table, Declaration declaration,
			Map<String, Column<?, ?>> columnsByName) {
		Map<String, FilterState> filters = new LinkedHashMap<>();
		for (Criterion criterion : declaration.criteria()) {
			String columnName = criterion.column();
			Column<?, ?> column = columnsByName.get(columnName);
			if (column == null) {
				log.error(problem(table, declaration, columnName)
					+ " the table has no such column. Its columns are: " + columnsByName.keySet() + ".");
				return null;
			}
			Optional<? extends ColumnFilter<?>> filter = column.filter();
			if (filter.isEmpty()) {
				log.error(problem(table, declaration, columnName) + " that column cannot be filtered.");
				return null;
			}
			FilterState state = filter.get().stateFor(criterion.value());
			if (state == null) {
				log.error(problem(table, declaration, columnName) + " the filter of that column cannot express the "
					+ "value " + describe(criterion.value()) + ".");
				return null;
			}
			filters.put(columnName, state);
		}
		return filters;
	}

	/**
	 * The common prefix of the error messages: which criterion of which declaration is at fault,
	 * and that the whole filter is dropped because of it.
	 */
	private static String problem(String table, Declaration declaration, String columnName) {
		return "The named filter '" + declaration.id() + "' of table '" + table
			+ "' is not offered, because its criterion for the column '" + columnName + "' cannot be applied:";
	}

	/**
	 * A value and its type, as an error message names it.
	 */
	private static String describe(Object value) {
		if (value == null) {
			return "'null'";
		}
		return "'" + value + "' (" + value.getClass().getName() + ")";
	}

}
