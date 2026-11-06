/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;



/**
 * Implementation of events that are sent in response to changes of a
 * {@link TableFilterModel}.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TableFilterModelEvent {
	
	private FilterModelEventTypes eventType;
	
	/**
	 * Create a new {@link TableFilterModelEvent}
	 * 
	 * @param eventType - the event type of the table filter model event
	 */
	protected TableFilterModelEvent(FilterModelEventTypes eventType) {
		this.eventType = eventType;
	}
	
	/**
	 * Getter for event type
	 * 
	 * @return - the event type of the table filter model event
	 */
	public FilterModelEventTypes getEventType() {
		return eventType;
	}
	
	/**
	 * Enumeration of event types, which may occur in a {@link TableFilterModel}.
	 * 
	 * @author    <a href=mailto:sts@top-logic.com>sts</a>
	 */
	public enum FilterModelEventTypes {
		/** The configuration of the filter model has changed. */
		CONFIGURATION_UPDATE,
		/** The filter model shall be applied again. */
		RE_APPLIANCE,
		/** The filter model has been deactivated. */
		DEACTIVATION
	}
}
