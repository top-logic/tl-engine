/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link ConfigurationItem} containing a list of {@link ColumnConfig}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ColumnContainerConfig extends ConfigurationItem {

	/**
	 * @see #getColumns()
	 */
	String COLUMNS = "columns";

	/**
	 * Entry tag for {@link #getColumns()}.
	 */
	String COLUMN_TAG = "column";

	/**
	 * Columns available in this container.
	 * 
	 * @see TableConfiguration#getDeclaredColumns()
	 */
	@Name(COLUMNS)
	@EntryTag(COLUMN_TAG)
	@Key(ColumnConfig.NAME_ATTRIBUTE)
	List<ColumnConfig> getColumns();

	/**
	 * Indexed access to property {@link #getColumns()}.
	 */
	@Indexed(collection = COLUMNS)
	ColumnConfig getCol(String name);

}

