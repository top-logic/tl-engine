/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import java.util.List;

/**
 * Event object to inform {@link ColumnChangeVetoListener} about an upcoming change of the columns
 * in a {@link TableModel}.
 * 
 * @see ColumnChangeVetoListener
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ColumnChangeEvt {

	/**
	 * List of columns before the upcoming change
	 */
	List<String> oldValue();

	/**
	 * List of columns after the upcoming change
	 */
	List<String> newValue();

	/**
	 * {@link TableModel} whose columns will be changed
	 */
	TableModel source();
}

