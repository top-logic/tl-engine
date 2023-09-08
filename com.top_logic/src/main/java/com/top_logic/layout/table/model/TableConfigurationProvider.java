/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;



/**
 * Supporter class for getting table relevant rendering information.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface TableConfigurationProvider {

	/** Empty {@link TableConfigurationProvider} array. */
	TableConfigurationProvider[] EMPTY_PROVIDERS_ARRAY = new TableConfigurationProvider[0];

	/**
	 * Adjusts the given {@link TableConfiguration}, except the default column.
	 * 
	 * @see #adaptDefaultColumn(ColumnConfiguration)
	 */
	void adaptConfigurationTo(TableConfiguration table);

	/**
	 * Adjusts the default column of the {@link TableConfiguration}.
	 * 
	 * @see #adaptConfigurationTo(TableConfiguration)
	 */
	void adaptDefaultColumn(ColumnConfiguration defaultColumn);

}
