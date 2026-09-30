/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

import com.top_logic.table.FilterState;

/**
 * {@link FilterState} of a {@link ComparableColumnFilter}: a {@link ComparisonOperator} and
 * one or two bound values.
 *
 * @param <V>
 *        The compared value type.
 * @param operator
 *        The comparison operator.
 * @param primary
 *        The primary bound (and the only bound for unary operators).
 * @param secondary
 *        The secondary bound, used only by {@link ComparisonOperator#BETWEEN}.
 */
public record RangeFilterState<V>(ComparisonOperator operator, V primary, V secondary) implements FilterState {

	@Override
	public boolean isEmpty() {
		return operator == null || primary == null
			|| (operator == ComparisonOperator.BETWEEN && secondary == null);
	}

	/**
	 * A unary {@link RangeFilterState} (no secondary bound).
	 */
	public static <V> RangeFilterState<V> of(ComparisonOperator operator, V primary) {
		return new RangeFilterState<>(operator, primary, null);
	}

	/**
	 * A {@link ComparisonOperator#BETWEEN} {@link RangeFilterState}.
	 */
	public static <V> RangeFilterState<V> between(V lower, V upper) {
		return new RangeFilterState<>(ComparisonOperator.BETWEEN, lower, upper);
	}

}
