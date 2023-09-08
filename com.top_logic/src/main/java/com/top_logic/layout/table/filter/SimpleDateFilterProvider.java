/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.config.InstantiationContext;

/**
 * {@link DateTableFilterProvider} for non-mandatory columns.
 * 
 * @see MandatoryDateFilterProvider
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class SimpleDateFilterProvider extends DateTableFilterProvider {

	/**
	 * Singleton {@link SimpleDateFilterProvider} instance.
	 */
	public static final SimpleDateFilterProvider INSTANCE = new SimpleDateFilterProvider();

	private SimpleDateFilterProvider() {
		super(false);
	}
	
	/**
	 * Create a {@link SimpleDateFilterProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SimpleDateFilterProvider(final InstantiationContext context, final Config config) {
		super(context, config);
	}

}
