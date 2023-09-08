/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of page count of a {@link PagingModel}.
 * 
 * @see PagingModel#PAGE_COUNT_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageCountListener extends PropertyListener {

	/**
	 * Handles change of the page count of the given {@link PagingModel}.
	 * 
	 * @param sender
	 *        {@link PagingModel} whose page count changed.
	 * @param oldValue
	 *        Former page count.
	 * @param newValue
	 *        Current page count.
	 * 
	 * @see PagingModel#getPageCount()
	 */
	void handlePageCountChanged(PagingModel sender, Integer oldValue, Integer newValue);

}

