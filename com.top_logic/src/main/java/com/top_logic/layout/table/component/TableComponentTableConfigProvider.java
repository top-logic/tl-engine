/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.table.model.SetTableResPrefix;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Default {@link ComponentTableConfigProvider} for a {@link TableComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableComponentTableConfigProvider implements ComponentTableConfigProvider {

	/** Singleton {@link TableComponentTableConfigProvider} instance. */
	public static final TableComponentTableConfigProvider INSTANCE = new TableComponentTableConfigProvider();

	/**
	 * Creates a new {@link TableComponentTableConfigProvider}.
	 */
	protected TableComponentTableConfigProvider() {
		// singleton instance
	}

	@Override
	public TableConfigurationProvider getTableConfigProvider(LayoutComponent context, String tableName) {
		return new SetTableResPrefix(context.getResPrefix());
	}

}

