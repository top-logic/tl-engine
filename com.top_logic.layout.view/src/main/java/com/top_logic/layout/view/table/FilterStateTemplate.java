/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.table;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.table.FilterState;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextFilterState;

/**
 * A declared filter criterion whose value expressions are compiled, ready to be evaluated into the
 * {@link FilterState} the criterion selects.
 *
 * <p>
 * A criterion is declared once and applies to every session, while the values it selects by are
 * computed per session and per input: the expressions are therefore compiled when the declaration is
 * read, and evaluated whenever the table is built or one of its inputs changes. The arguments of an
 * evaluation are the input values of the table, in the order the table declares its inputs - the
 * same arguments its rows are computed from.
 * </p>
 *
 * @see FilterStateConfig The declaration this is compiled from.
 */
@FunctionalInterface
public interface FilterStateTemplate {

	/**
	 * The {@link FilterState} this criterion selects for the given input values.
	 *
	 * <p>
	 * An {@link FilterState#isEmpty() empty} state is the answer to values that select nothing,
	 * which leaves the column unfiltered rather than being an error: an input nothing is selected in
	 * yields no criterion.
	 * </p>
	 *
	 * @param arguments
	 *        The input values of the table, in declaration order.
	 */
	FilterState evaluate(Object[] arguments);

	/**
	 * Compiles the value expressions of the given declaration.
	 */
	static FilterStateTemplate compile(FilterStateConfig config) {
		if (config instanceof FilterStateConfig.TextConfig text) {
			QueryExecutor pattern = QueryExecutor.compile(text.getPattern());
			boolean caseSensitive = text.getCaseSensitive();
			boolean regexp = text.getRegexp();
			boolean wholeField = text.getWholeField();
			return arguments -> new TextFilterState(SearchExpression.asString(pattern.execute(arguments)),
				caseSensitive, regexp, wholeField);
		}
		if (config instanceof FilterStateConfig.RangeConfig range) {
			QueryExecutor primary = QueryExecutor.compile(range.getPrimary());
			QueryExecutor secondary =
				range.getSecondary() == null ? null : QueryExecutor.compile(range.getSecondary());
			return arguments -> new RangeFilterState<>(range.getOperator(), primary.execute(arguments),
				secondary == null ? null : secondary.execute(arguments));
		}
		if (config instanceof FilterStateConfig.OptionsConfig options) {
			QueryExecutor selected = QueryExecutor.compile(options.getSelected());
			return arguments -> new OptionsFilterState(values(selected.execute(arguments)));
		}
		if (config instanceof FilterStateConfig.BooleanConfig bool) {
			QueryExecutor accept = QueryExecutor.compile(bool.getAccept());
			return arguments -> booleanState(accept.execute(arguments));
		}
		throw new IllegalArgumentException("No filter criterion for declaration: " + config);
	}

	/**
	 * The values to select: the elements of a collection, or the single value itself.
	 *
	 * <p>
	 * A value that is not there selects nothing, so it is no value of its own.
	 * </p>
	 */
	private static Set<Object> values(Object value) {
		Set<Object> result = new LinkedHashSet<>();
		for (Object element : elements(value)) {
			if (element != null) {
				result.add(element);
			}
		}
		return result;
	}

	/**
	 * Which of the three logical values the given value accepts.
	 *
	 * <p>
	 * A value that is not there stands for the cells without a value; every other value is read as
	 * a truth value the way TL-Script reads one.
	 * </p>
	 */
	private static BooleanFilterState booleanState(Object value) {
		boolean acceptTrue = false;
		boolean acceptFalse = false;
		boolean acceptNull = false;
		for (Object element : elements(value)) {
			if (element == null) {
				acceptNull = true;
			} else if (SearchExpression.isTrue(element)) {
				acceptTrue = true;
			} else {
				acceptFalse = true;
			}
		}
		return new BooleanFilterState(acceptTrue, acceptFalse, acceptNull);
	}

	/**
	 * The declared values: the elements of a collection, or the given value as the single one.
	 */
	private static Collection<?> elements(Object value) {
		return value instanceof Collection<?> collection ? collection : Collections.singletonList(value);
	}

}
