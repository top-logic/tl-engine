/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * {@link TableConfigurationProvider} that remains the table untouched.
 * 
 * @see TableConfigurationFactory#emptyProvider()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class EmptyTableConfigurationProvider implements TableConfigurationProvider {

	/** Singleton {@link EmptyTableConfigurationProvider} instance. */
	public static final EmptyTableConfigurationProvider INSTANCE = new EmptyTableConfigurationProvider();

	private EmptyTableConfigurationProvider() {
		// singleton instance
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		// no table adaption
	}

	@Override
	public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
		// no table adaption
	}

}
