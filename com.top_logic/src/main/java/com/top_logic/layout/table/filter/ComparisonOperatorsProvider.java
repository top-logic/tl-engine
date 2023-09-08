/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;

/**
 * Provider for {@link Operators comparison operators}, used by {@link ComparableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface ComparisonOperatorsProvider {

	/**
	 * {@link Operators}, that shall be available to the {@link ComparableFilter}.
	 */
	List<Operators> getComparisonOperators();

	/**
	 * {@link Operators operator}, that shall be treated as default (e.g. displayed, if no
	 *         {@link Operators operator} is selected explicitly).
	 */
	Operators getDefaultOperator();
}
