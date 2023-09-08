/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * A common interface for observers of a {@link TableFilterModel}.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface TableFilterModelListener {
	
	/**
	 * This method handles events, which occur in a {@link TableFilterModel}.
	 * 
	 * @param event - the table filter model event, which occured
	 */
	public void handleTableFilterModelEvent(TableFilterModelEvent event);
}
