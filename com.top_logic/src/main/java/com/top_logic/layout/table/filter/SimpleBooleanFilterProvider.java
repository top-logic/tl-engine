/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;


/**
 * {@link BooleanTableFilterProvider} for {@link Boolean} values that interprets <code>null</code>
 * as <code>false</code>.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SimpleBooleanFilterProvider extends BooleanTableFilterProvider {

	/**
	 * Singleton {@link SimpleBooleanFilterProvider} instance.
	 */
	public static final SimpleBooleanFilterProvider INSTANCE = new SimpleBooleanFilterProvider();

	private SimpleBooleanFilterProvider() {
		super(false);
	}

}

