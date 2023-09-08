/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;

/**
 * {@link ComparisonOperatorsProvider}, that provide {@link Operators}, that are suitable for float
 * value comparisons (less or equals, greater or equals, range).
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FloatValueOperatorsProvider implements ComparisonOperatorsProvider {

	/** Singleton instance of {@link FloatValueOperatorsProvider} */
	public static final ComparisonOperatorsProvider INSTANCE = new FloatValueOperatorsProvider();

	@Override
	public List<Operators> getComparisonOperators() {
		List<Operators> operators = new ArrayList<>(3);
		operators.add(Operators.LESS_OR_EQUALS);
		operators.add(Operators.GREATER_OR_EQUALS);
		operators.add(Operators.BETWEEN);
		return operators;
	}

	@Override
	public Operators getDefaultOperator() {
		return Operators.LESS_OR_EQUALS;
	}
}
