/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provider to allow setting of {@link TableConfigurationProvider} depending on a concrete
 * {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ComponentTableConfigProvider {

	/**
	 * Creates a {@link TableConfigurationProvider} for the given {@link LayoutComponent}.
	 * 
	 * @param context
	 *        The context component.
	 * @param tableName
	 *        Name of the table to create {@link TableConfigurationProvider} for. May be
	 *        <code>null</code> when the table does not have a special name, e.g. table in
	 *        {@link TableComponent}.
	 */
	TableConfigurationProvider getTableConfigProvider(LayoutComponent context, String tableName);

}

