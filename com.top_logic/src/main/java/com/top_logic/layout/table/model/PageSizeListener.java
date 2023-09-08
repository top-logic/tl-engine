/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the page size of a {@link PagingModel}.
 * 
 * @see PagingModel#PAGE_SIZE_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageSizeListener extends PropertyListener {

	/**
	 * Handles change of the page size the given {@link PagingModel}.
	 * 
	 * @param sender
	 *        {@link PagingModel} whose page size changed.
	 * @param oldValue
	 *        Former page size.
	 * @param newValue
	 *        Current page size.
	 * 
	 * @see PagingModel#getPageSizeSpec()
	 */
	void handlePageSizeChanged(PagingModel sender, Integer oldValue, Integer newValue);

}

