/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link ComparableTableFilterProvider}, that provides a {@link ComparableFilter}, with suitable
 * comparison options for float values.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FloatValuesTableFilterProvider extends NumberTableFilterProvider {

	/** Instance of {@link FloatValueOperatorsProvider} */
	public static final FloatValuesTableFilterProvider INSTANCE = new FloatValuesTableFilterProvider();

	private FloatValuesTableFilterProvider() {
		super(false, FloatValueOperatorsProvider.INSTANCE);
	}
}
