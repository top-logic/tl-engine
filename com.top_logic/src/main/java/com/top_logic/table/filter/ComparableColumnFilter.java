/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterState;

/**
 * A {@link ColumnFilter} comparing the cell value against a {@link RangeFilterState} using
 * a {@link Comparator}. A {@code null} cell value never matches.
 *
 * @param <V>
 *        The compared value type.
 */
public class ComparableColumnFilter<V> implements ColumnFilter<V> {

	/** JSON key of {@link RangeFilterState#operator()}. */
	public static final String OPERATOR = "operator";

	/** JSON key of {@link RangeFilterState#primary()}. */
	public static final String PRIMARY = "primary";

	/** JSON key of {@link RangeFilterState#secondary()}. */
	public static final String SECONDARY = "secondary";

	private final Comparator<? super V> _comparator;

	private final Function<String, ? extends V> _parser;

	/**
	 * Creates a {@link ComparableColumnFilter} with the given comparator and no editor
	 * support.
	 */
	public ComparableColumnFilter(Comparator<? super V> comparator) {
		this(comparator, null);
	}

	/**
	 * Creates a {@link ComparableColumnFilter} that can build a filter editor.
	 *
	 * @param comparator
	 *        The value ordering.
	 * @param parser
	 *        Parses user input into a bound value (enables a filter editor), or
	 *        {@code null}.
	 */
	public ComparableColumnFilter(Comparator<? super V> comparator, Function<String, ? extends V> parser) {
		_comparator = comparator;
		_parser = parser;
	}

	/**
	 * A {@link ComparableColumnFilter} using the natural ordering of the value type.
	 */
	public static <V extends Comparable<? super V>> ComparableColumnFilter<V> natural() {
		return new ComparableColumnFilter<>(Comparator.naturalOrder());
	}

	/**
	 * A {@link ComparableColumnFilter} for {@link Integer}-valued columns.
	 */
	public static ComparableColumnFilter<Integer> integers() {
		return new ComparableColumnFilter<>(Comparator.naturalOrder(), Integer::valueOf);
	}

	/**
	 * Parses user input into a bound value, or {@code null} if no editor is supported.
	 */
	public Function<String, ? extends V> parser() {
		return _parser;
	}

	@Override
	public FilterInput input() {
		return new FilterInput.Range();
	}

	@Override
	public boolean supportsInversion() {
		return true;
	}

	/**
	 * Serializes the range as the operator name plus the textual form of the bounds. Only filters
	 * with a {@link #parser() parser} can be restored, so a parser-less filter declines persistence.
	 */
	@Override
	public Object toJson(FilterState state) {
		if (_parser == null) {
			return null;
		}
		RangeFilterState<?> range = (RangeFilterState<?>) state;
		if (range.operator() == null) {
			return null;
		}
		Map<String, Object> json = new LinkedHashMap<>();
		json.put(OPERATOR, range.operator().name());
		json.put(PRIMARY, text(range.primary()));
		json.put(SECONDARY, text(range.secondary()));
		return json;
	}

	@Override
	public FilterState fromJson(Object json) {
		if (_parser == null || !(json instanceof Map<?, ?> map)) {
			return null;
		}
		Object operatorName = map.get(OPERATOR);
		if (operatorName == null) {
			return null;
		}
		ComparisonOperator operator;
		try {
			operator = ComparisonOperator.valueOf(String.valueOf(operatorName));
		} catch (IllegalArgumentException ex) {
			return null;
		}
		return new RangeFilterState<>(operator, parse(map.get(PRIMARY)), parse(map.get(SECONDARY)));
	}

	/**
	 * A {@link ComparisonOperator#BETWEEN} range from a two-element collection, or an
	 * {@link ComparisonOperator#EQ equality} comparison with a single value.
	 *
	 * <p>
	 * A collection of two elements bounds the range by its first element (lower, inclusive) and its
	 * second one (upper, inclusive), in that order; a single value - given as such or as a
	 * one-element collection - is compared for equality. A collection of any other size is
	 * rejected, and so is a value this filter's {@link Comparator} cannot compare.
	 * </p>
	 */
	@Override
	public FilterState stateFor(Object value) {
		if (value instanceof Collection<?> values) {
			List<?> bounds = new ArrayList<>(values);
			switch (bounds.size()) {
				case 1:
					return equality(bounds.get(0));
				case 2:
					return range(bounds.get(0), bounds.get(1));
				default:
					return null;
			}
		}
		return equality(value);
	}

	private FilterState equality(Object value) {
		V bound = comparable(value);
		return bound == null ? null : RangeFilterState.of(ComparisonOperator.EQ, bound);
	}

	private FilterState range(Object lower, Object upper) {
		V lowerBound = comparable(lower);
		V upperBound = comparable(upper);
		if (lowerBound == null || upperBound == null) {
			return null;
		}
		return RangeFilterState.between(lowerBound, upperBound);
	}

	/**
	 * The given value as a bound of this filter, or {@code null} if it is none: no value at all, or
	 * one this filter's {@link Comparator} cannot compare.
	 */
	private V comparable(Object value) {
		if (value == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		V bound = (V) value;
		try {
			_comparator.compare(bound, bound);
		} catch (ClassCastException ex) {
			return null;
		}
		return bound;
	}

	private static String text(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	private V parse(Object value) {
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value).trim();
		if (text.isEmpty()) {
			return null;
		}
		try {
			return _parser.apply(text);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	@Override
	public Predicate<V> predicate(FilterState state) {
		@SuppressWarnings("unchecked")
		RangeFilterState<V> range = (RangeFilterState<V>) state;
		ComparisonOperator operator = range.operator();
		V primary = range.primary();
		V secondary = range.secondary();
		return value -> {
			if (value == null) {
				return false;
			}
			int toPrimary = _comparator.compare(value, primary);
			switch (operator) {
				case EQ:
					return toPrimary == 0;
				case NE:
					return toPrimary != 0;
				case LT:
					return toPrimary < 0;
				case LE:
					return toPrimary <= 0;
				case GT:
					return toPrimary > 0;
				case GE:
					return toPrimary >= 0;
				case BETWEEN:
					return toPrimary >= 0 && _comparator.compare(value, secondary) <= 0;
				default:
					return false;
			}
		};
	}

}
