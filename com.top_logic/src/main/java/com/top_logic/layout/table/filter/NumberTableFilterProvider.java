/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.table.model.ColumnConfiguration;

/**
 * {@link ComparableTableFilterProvider} filtering numbers.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NumberTableFilterProvider extends ComparableTableFilterProvider {

	/**
	 * Creates a {@link NumberTableFilterProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NumberTableFilterProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Creates a {@link NumberTableFilterProvider}.
	 * 
	 * @param mandatory
	 *        Whether the column value cannot be empty.
	 * @param operatorsProvider
	 *        Selection of compare operations to use.
	 */
	public NumberTableFilterProvider(boolean mandatory, ComparisonOperatorsProvider operatorsProvider) {
		super(mandatory, operatorsProvider);
	}

	@Override
	protected FilterComparator getComparator() {
		return NumberComparator.getInstance();
	}

	@Override
	protected FilterFieldProvider createFieldProvider(ColumnConfiguration column) {
		FormatProvider formatProvider = getFormatProvider();
		if (formatProvider.getFormat(column) != null) {
			return new NumberFieldProvider(formatProvider.getFormat(column));
		} else {
			return NumberFieldProvider.INSTANCE;
		}
	}

}
