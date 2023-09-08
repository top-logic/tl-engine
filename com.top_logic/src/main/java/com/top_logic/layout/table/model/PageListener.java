/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the page of a {@link PagingModel}.
 * 
 * @see PagingModel#PAGE_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageListener extends PropertyListener {

	/**
	 * Handles change of the page of the given {@link PagingModel}.
	 * 
	 * @param sender
	 *        {@link PagingModel} whose page changed.
	 * @param oldValue
	 *        Former page.
	 * @param newValue
	 *        Current page.
	 * 
	 * @see PagingModel#getPage()
	 */
	void handlePageChanged(PagingModel sender, Integer oldValue, Integer newValue);

}

