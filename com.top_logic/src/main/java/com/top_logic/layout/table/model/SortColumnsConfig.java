/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.table.SortConfig;

/**
 * Configuration to set the sort order of the table rows.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface SortColumnsConfig extends ConfigurationItem {

	/**
	 * Configuration name.
	 */
	public static final String SORT_COLUMNS = "sort-columns";

	/**
	 * @see #getOrder()
	 */
	public static final String ORDER = "order";

	/**
	 * Columns order by which the rows are sorted.
	 */
	@Name(ORDER)
	@Format(DefaultSortOrderFormat.class)
	List<SortConfig> getOrder();

}
