/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.ResourceView;

/**
 * {@link PropertyListener} interface for observing {@link TableData#getTableResources()} changes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableResourcesListener extends PropertyListener {

	/**
	 * Informs about a changes of table resources.
	 * 
	 * @param sender
	 *        The changed {@link TableData}.
	 * @param oldValue
	 *        The old resources.
	 * @param newValue
	 *        The new resources.
	 */
	void notifyTableResourcesChanged(TableData sender, ResourceView oldValue, ResourceView newValue);

}
