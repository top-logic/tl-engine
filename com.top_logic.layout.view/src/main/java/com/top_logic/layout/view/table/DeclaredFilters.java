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
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextFilterState;

/**
 * Turns the filter criteria a table declares into the {@link NamedFilter}s it offers.
 *
 * <p>
 * A declaration names a criterion per column, in one of two forms. A
 * {@link DeclaredFilters.Criterion.Value value} leaves it to the column's own
 * {@link ColumnFilter} to decide what selecting that value means, so which value shapes such a
 * criterion may use is part of the contract of the filter it addresses. A
 * {@link DeclaredFilters.Criterion.State state} says it in the form of the filter itself - a
 * pattern with its matching options, a comparison, a selection - and therefore has to be of the
 * same kind as the filter of its column.
 * </p>
 *
 * <p>
 * Either way the criterion is brought into the form the filter itself produces, by the same
 * serialization the user's own filters are persisted with: a criterion declared as the whole number
 * 50 filters a column of fractional numbers exactly as one the user enters there, so that the chip
 * of a preset is recognized as the active one - across a reload of the page, too.
 * </p>
 *
 * <p>
 * The criteria are materialized here, when the table is built and whenever one of its inputs
 * changes: a chip the user clicks then applies states that are already computed, and the table can
 * tell which of its named filters is active by comparing them with its live ones.
 * </p>
 *
 * <p>
 * A criterion that cannot be materialized - an unknown column, a column that cannot be filtered, a
 * criterion of another kind than the column's filter, or one the filter cannot express - is a
 * declaration error: it is reported to the {@link Log}, and the named filter it belongs to is not
 * offered rather than offered with a criterion silently missing.
 * </p>
 *
 * <p>
 * A criterion selecting nothing is no error but no criterion either: an input nothing is selected in
 * leaves its column unfiltered. A declaration all of whose criteria select nothing is withheld -
 * nothing is left of what it says - and offered again as soon as its inputs select something. A
 * declaration that names no criterion at all is a different thing: it says "no filter", is offered
 * as such, and is the active one exactly while the table is unfiltered.
 * </p>
 */
public class DeclaredFilters {

	/**
	 * One criterion of a {@link Declaration}: what is selected in one column.
	 */
	public sealed interface Criterion {

		/**
		 * The {@link Column#name() name} of the column this criterion applies to.
		 */
		String column();

		/**
		 * Whether the column accepts exactly the rows this criterion does <em>not</em> select.
		 */
		boolean inverted();

		/**
		 * A criterion naming the value to select, translated by the column's own filter.
		 *
		 * @param column
		 *        The {@link Column#name() name} of the column the value applies to.
		 * @param value
		 *        The selected value, evaluated by the declaring tier and handed to the column's
		 *        {@link ColumnFilter#stateFor(Object)}.
		 * @param inverted
		 *        Whether the column accepts exactly the rows the value does not select.
		 */
		record Value(String column, Object value, boolean inverted) implements Criterion {
			// Pure data carrier.
		}

		/**
		 * A criterion given in the form of the column's filter.
		 *
		 * @param column
		 *        The {@link Column#name() name} of the column the state applies to.
		 * @param state
		 *        The selected state, evaluated by the declaring tier; has to be of the kind the
		 *        column's filter works in.
		 * @param inverted
		 *        Whether the column accepts exactly the rows the state does not select.
		 */
		record State(String column, FilterState state, boolean inverted) implements Criterion {
			// Pure data carrier.
		}

		/**
		 * A {@link Criterion.Value} that is not inverted.
		 */
		static Criterion value(String column, Object value) {
			return new Value(column, value, false);
		}

		/**
		 * A {@link Criterion.State} that is not inverted.
		 */
		static Criterion state(String column, FilterState state) {
			return new State(column, state, false);
		}
	}

	/**
	 * The evaluated declaration of one {@link NamedFilter}.
	 *
	 * @param id
	 *        The {@link NamedFilter#id() identifier} of the resulting filter.
	 * @param label
	 *        The name to display.
	 * @param criteria
	 *        What is selected per column.
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
	 *        All columns of the table, whose filters translate the criteria.
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
			if (filters.isEmpty() && !declaration.criteria().isEmpty()) {
				// Every criterion of this declaration selects nothing, so there is nothing left of
				// what it says: it is withheld rather than offered as a filter by nothing, and
				// offered again as soon as its inputs select something. A declaration that names no
				// criterion in the first place is a different thing - it says "no filter", and is
				// offered as such.
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
			Optional<? extends ColumnFilter<?>> optionalFilter = column.filter();
			if (optionalFilter.isEmpty()) {
				log.error(problem(table, declaration, columnName) + " that column cannot be filtered.");
				return null;
			}
			ColumnFilter<?> filter = optionalFilter.get();

			FilterState declared = declared(log, table, declaration, filter, criterion);
			if (declared == null) {
				return null;
			}
			if (declared.isEmpty()) {
				// Nothing is selected in that column, which is no criterion at all - and no error:
				// a criterion selecting what is displayed elsewhere selects nothing while nothing is
				// displayed there.
				continue;
			}
			if (criterion.inverted() && !filter.supportsInversion()) {
				log.error(problem(table, declaration, columnName)
					+ " the filter of that column cannot be inverted.");
				return null;
			}
			FilterState normalized = normalize(filter, declared);
			Set<Object> unknown = unselected(declared, normalized);
			if (!unknown.isEmpty()) {
				log.error(problem(table, declaration, columnName) + " that column offers no option "
					+ unknown + ".");
				return null;
			}
			if (normalized == null || normalized.isEmpty()) {
				log.error(problem(table, declaration, columnName) + " the filter of that column cannot express "
					+ describe(criterion) + ".");
				return null;
			}
			filters.put(columnName, criterion.inverted() ? new NegatedFilterState(normalized) : normalized);
		}
		return filters;
	}

	/**
	 * The state a criterion declares, before it is brought into the form the filter produces, or
	 * {@code null} if the criterion is none of that filter's (then the problem has been reported).
	 */
	private static FilterState declared(Log log, String table, Declaration declaration, ColumnFilter<?> filter,
			Criterion criterion) {
		if (criterion instanceof Criterion.State state) {
			FilterInput input = filter.input();
			if (!fits(input, state.state())) {
				log.error(problem(table, declaration, criterion.column()) + " a criterion of the form '"
					+ form(state.state()) + "' does not fit that column, which is filtered by "
					+ describe(input) + ".");
				return null;
			}
			return state.state();
		}
		Object value = ((Criterion.Value) criterion).value();
		FilterState result = filter.stateFor(value);
		if (result == null) {
			log.error(problem(table, declaration, criterion.column())
				+ " the filter of that column cannot express the value " + describe(value) + ".");
		}
		return result;
	}

	/**
	 * The declared values a selection lost while being brought into the form of the filter, i.e. the
	 * ones the column offers no option for.
	 *
	 * <p>
	 * A selection missing one of its values matches other rows than the declared one, so it is not
	 * the declared criterion at all - the same reason a value naming an unknown option is rejected
	 * by the filter itself.
	 * </p>
	 */
	private static Set<Object> unselected(FilterState declared, FilterState normalized) {
		if (!(declared instanceof OptionsFilterState declaredOptions)) {
			return Set.of();
		}
		Set<Object> missing = new LinkedHashSet<>(declaredOptions.selected());
		if (normalized instanceof OptionsFilterState normalizedOptions) {
			missing.removeAll(normalizedOptions.selected());
		}
		return missing;
	}

	/**
	 * The given state in the form the given filter itself produces, or {@code null} if the filter
	 * cannot express it.
	 *
	 * <p>
	 * The round trip through the filter's own serialization is what a persisted filter goes through,
	 * so a declared criterion and the same criterion set by the user - or restored in the next
	 * session - are the same value and compare equal.
	 * </p>
	 */
	private static FilterState normalize(ColumnFilter<?> filter, FilterState state) {
		Object json = filter.toJson(state);
		if (json == null) {
			return null;
		}
		return filter.fromJson(json);
	}

	/**
	 * Whether a state of the given kind is what the given filter input is edited into.
	 */
	private static boolean fits(FilterInput input, FilterState state) {
		if (input instanceof FilterInput.Text) {
			return state instanceof TextFilterState;
		}
		if (input instanceof FilterInput.Range) {
			return state instanceof RangeFilterState<?>;
		}
		if (input instanceof FilterInput.Options) {
			return state instanceof OptionsFilterState;
		}
		if (input instanceof FilterInput.Bool) {
			return state instanceof BooleanFilterState;
		}
		// A filter bringing its own form is edited by that form alone, so there is no declared
		// criterion of its kind.
		return false;
	}

	/**
	 * The form of a declared state, as an error message names it.
	 */
	private static String form(FilterState state) {
		if (state instanceof TextFilterState) {
			return FilterStateConfig.TextConfig.TAG_NAME;
		}
		if (state instanceof RangeFilterState<?>) {
			return FilterStateConfig.RangeConfig.TAG_NAME;
		}
		if (state instanceof OptionsFilterState) {
			return FilterStateConfig.OptionsConfig.TAG_NAME;
		}
		if (state instanceof BooleanFilterState) {
			return FilterStateConfig.BooleanConfig.TAG_NAME;
		}
		return String.valueOf(state);
	}

	/**
	 * How a column is filtered, as an error message names it.
	 */
	private static String describe(FilterInput input) {
		if (input instanceof FilterInput.Text) {
			return "a text pattern ('" + FilterStateConfig.TextConfig.TAG_NAME + "')";
		}
		if (input instanceof FilterInput.Range) {
			return "a comparison ('" + FilterStateConfig.RangeConfig.TAG_NAME + "')";
		}
		if (input instanceof FilterInput.Options) {
			return "a selection of options ('" + FilterStateConfig.OptionsConfig.TAG_NAME + "')";
		}
		if (input instanceof FilterInput.Bool) {
			return "a truth value ('" + FilterStateConfig.BooleanConfig.TAG_NAME + "')";
		}
		return "a form of its own, which no criterion can be declared for";
	}

	/**
	 * The criterion, as an error message names it.
	 */
	private static String describe(Criterion criterion) {
		if (criterion instanceof Criterion.State state) {
			return "the criterion " + state.state();
		}
		return "the value " + describe(((Criterion.Value) criterion).value());
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
