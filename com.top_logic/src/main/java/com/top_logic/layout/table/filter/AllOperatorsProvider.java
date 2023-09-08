/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Arrays;
import java.util.List;

import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;

/**
 * {@link ComparisonOperatorsProvider}, that will provide all available {@link Operators}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class AllOperatorsProvider implements ComparisonOperatorsProvider {

	/** Singleton instance of {@link AllOperatorsProvider} */
	public static final ComparisonOperatorsProvider INSTANCE = new AllOperatorsProvider();

	@Override
	public List<Operators> getComparisonOperators() {
		return Arrays.asList(Operators.values());
	}

	@Override
	public Operators getDefaultOperator() {
		return Operators.EQUALS;
	}
}
