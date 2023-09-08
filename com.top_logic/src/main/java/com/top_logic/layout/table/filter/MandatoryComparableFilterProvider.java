/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link ComparableTableFilterProvider} for columns displaying mandatory values.
 * 
 * @see SimpleComparableFilterProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryComparableFilterProvider extends NumberTableFilterProvider {

	/**
	 * Singleton {@link MandatoryComparableFilterProvider} instance.
	 */
	public static final MandatoryComparableFilterProvider INSTANCE = new MandatoryComparableFilterProvider();

	private MandatoryComparableFilterProvider() {
		super(true, AllOperatorsProvider.INSTANCE);
	}

}
