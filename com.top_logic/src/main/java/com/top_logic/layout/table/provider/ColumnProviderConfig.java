/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.table.model.TableConfigurationProvider;

/**
 * Base-configuration for all {@link TableConfigurationProvider}s defining single new columns.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ColumnProviderConfig extends ConfigurationItem {

	/**
	 * @see #getColumnId()
	 */
	String COLUMN_ID = "columnId";

	/**
	 * @see #getColumnLabel()
	 */
	String COLUMN_LABEL = "columnLabel";

	/**
	 * Technical name of the defined column.
	 */
	@Name(COLUMN_ID)
	@Hidden
	@ValueInitializer(UUIDInitializer.class)
	@Mandatory
	String getColumnId();

	/**
	 * Label to be displayed in the column header.
	 */
	@Name(COLUMN_LABEL)
	@Mandatory
	ResKey getColumnLabel();

}
