/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.List;

import com.top_logic.layout.scripting.action.ModelAction;
import com.top_logic.layout.table.SortConfig;

/**
 * Configuration of a {@link SortTableColumnOp}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SortTableColumn extends ModelAction {

	/**
	 * The list of {@link SortConfig}s defining the sort order.
	 */
	List<SortConfig> getSortOrders();

	/**
	 * Setter for {@link #getSortOrders()}.
	 */
	void setSortOrders(List<SortConfig> sortOrder);

	/**
	 * Whether the names in the {@link SortConfig}s are labels.
	 */
	boolean isLabel();

	/** @see #isLabel() */
	void setLabel(boolean value);

}
