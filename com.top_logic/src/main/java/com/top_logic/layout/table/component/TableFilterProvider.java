/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.layout.table.TableFilter;
import com.top_logic.layout.table.TableViewModel;

/**
 * A TableFilterProvider is responsible for creating {@link com.top_logic.layout.table.TableFilter}
 * for a table. It can be configured in a table configuration by the attribute 'filterProvider'.
 * 
 * @author <a href="mailto:jco@top-logic.com">jco</a>
 */
public interface TableFilterProvider {

	/**
	 * Initialize the filters for the column with the given column number.
	 * 
	 * @param aTableViewModel
	 *        The table model to which a {@link TableFilter} shall be applied to.
	 * @param filterPosition
	 *        Position in table hierarchy, where the filter shall be applied to(whether unique
	 *        column name or global id).
	 * 
	 * @return a {@link TableFilter}
	 */
	TableFilter createTableFilter(TableViewModel aTableViewModel, String filterPosition);
    
}

