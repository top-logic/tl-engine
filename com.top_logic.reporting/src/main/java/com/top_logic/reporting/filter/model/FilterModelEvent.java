/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.filter.model;

/**
 * A {@link FilterModelEvent} works together with {@link FilterModelListener} and
 * {@link FilterModel}. All implementations of this interface must have a unique type. 
 * Only so it is possible to cast the events to the right class.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public interface FilterModelEvent {
	
	/**
	 * This method returns the unique event type. All implementations of this
	 * interface must have a unique type. Only so it is possible to cast the
	 * events to the right class.
	 * 
	 * @return Returns the unique event type.
	 */
	public int getEventType();
	
}
