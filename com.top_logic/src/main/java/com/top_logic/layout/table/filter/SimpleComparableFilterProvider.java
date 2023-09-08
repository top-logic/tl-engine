/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;


/**
 * {@link ComparableTableFilterProvider} for non-mandatory columns.
 * 
 * @see MandatoryComparableFilterProvider
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class SimpleComparableFilterProvider extends NumberTableFilterProvider {

	/**
	 * Singleton {@link SimpleComparableFilterProvider} instance.
	 */
	public static final SimpleComparableFilterProvider INSTANCE = new SimpleComparableFilterProvider();

	private SimpleComparableFilterProvider() {
		super(false, AllOperatorsProvider.INSTANCE);
	}

}
