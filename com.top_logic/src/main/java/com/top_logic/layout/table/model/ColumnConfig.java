/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.config.NamedConfiguration;

/**
 * Configuration of a table column.
 * 
 * @see TableConfig
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ColumnConfig extends ColumnBaseConfig, ColumnContainerConfig, NamedConfiguration {
	
	/**
	 * Name of the configured column.
	 * 
	 * @see ColumnConfiguration#getName()
	 */
	@Override
	String getName();
	
}

