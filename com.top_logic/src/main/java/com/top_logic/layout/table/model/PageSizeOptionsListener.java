/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles change of the page size options of a {@link PagingModel}.
 * 
 * @see PagingModel#PAGE_SIZE_OPTIONS_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PageSizeOptionsListener extends PropertyListener {

	/**
	 * Handles change of the page size options the given {@link PagingModel}.
	 * 
	 * @param sender
	 *        {@link PagingModel} whose page size options changed.
	 * @param oldValue
	 *        Former page size options.
	 * @param newValue
	 *        Current page size options.
	 * 
	 * @see PagingModel#getPageSizeOptions()
	 */
	void handlePageSizeOptionsChanged(PagingModel sender, int[] oldValue, int[] newValue);

}

