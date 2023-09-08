/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * {@link BooleanTableFilterProvider} for columns with mandatory (tri-state) boolean values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TristateBooleanFilterProvider extends BooleanTableFilterProvider {

	/**
	 * Singleton {@link TristateBooleanFilterProvider} instance.
	 */
	public static final TristateBooleanFilterProvider INSTANCE = new TristateBooleanFilterProvider();

	private TristateBooleanFilterProvider() {
		super(true);
	}

}
