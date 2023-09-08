/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter.model;

import com.top_logic.reporting.filter.FormFilterComponent;

/**
 * {@link FilterModelListener}s are informed about changes of the 
 * {@link FormFilterComponent}. If a filter (or more) of the {@link FormFilterComponent}
 * is changed a {@link FilterModelEvent} is send. 
 *   
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface FilterModelListener {

	/**
	 * This method is called if a {@link FilterModelEvent} was raised by a
	 * {@link FormFilterComponent}.
	 * 
	 * @param modelEvent
	 *            A {@link FilterModelEvent} must NOT be <code>null</code>.
	 */
	public void handleModelEvent(FilterModelEvent modelEvent);
	
}
