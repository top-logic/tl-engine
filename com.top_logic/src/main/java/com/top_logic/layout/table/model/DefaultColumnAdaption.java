/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

/**
 * {@link TableConfigurationProvider} that only adapts the default column by default.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DefaultColumnAdaption implements TableConfigurationProvider {

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		// Only default column is adapted.
	}

}

