/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link DateTableFilterProvider} for mandatory columns.
 * 
 * @see SimpleDateFilterProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MandatoryDateFilterProvider extends DateTableFilterProvider {

	/**
	 * Singleton {@link MandatoryDateFilterProvider} instance.
	 */
	public static final MandatoryDateFilterProvider INSTANCE = new MandatoryDateFilterProvider();

	/**
	 * Typed configuration interface definition for {@link MandatoryDateFilterProvider}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends DateTableFilterProvider.Config {

		@Override
		@BooleanDefault(true)
		boolean isMandatory();
	}

	private MandatoryDateFilterProvider() {
		super(true);
	}

	/**
	 * Create a {@link MandatoryDateFilterProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public MandatoryDateFilterProvider(final InstantiationContext context, final Config config) {
		super(context, config);
	}

}
