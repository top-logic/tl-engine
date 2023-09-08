/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.table.TableFilter;

/**
 * A common interface for observers of a {@link TableFilter}.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface TableFilterListener {
	
	/**
	 * This method handles events, which occur in a {@link TableFilter}.
	 * 
	 * @param event - the table filter event, which occured
	 */
	public void handleTableFilterEvent(TableFilterEvent event);
}
