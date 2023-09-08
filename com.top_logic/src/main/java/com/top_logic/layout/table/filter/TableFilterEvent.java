/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import com.top_logic.layout.table.TableFilter;

/**
 * Implementation of events that are sent in response to changes of a
 * {@link TableFilter}.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class TableFilterEvent {
	
	private FilterEventTypes eventType;
	private TableFilter sourceFilter;
	
	/**
	 * Create a new TableFilterEvent
	 * 
	 * @param source - the {@link TableFilter}, which fires the event
	 * @param eventType - the event type of the table filter event
	 */
	public TableFilterEvent(TableFilter source, FilterEventTypes eventType) {
		this.sourceFilter = source;
		this.eventType = eventType;
	}
	
	/**
	 * Getter for event type
	 * 
	 * @return - the event type of the table filter event
	 */
	public FilterEventTypes getEventType() {
		return eventType;
	}
	
	/**
	 * Getter for the {@link TableFilter}, which fired the event
	 * 
	 * @return table filter, which fired the event
	 */
	public TableFilter getSource() {
		return sourceFilter;
	}
	
	/**
	 * Enumeration of event types, which may occur in a {@link TableFilter}.
	 * 
	 * @author    <a href=mailto:sts@top-logic.com>sts</a>
	 */
	public enum FilterEventTypes {
		CONFIGURATION_UPDATE,
		RE_APPLIANCE,
		DEACTIVATION,
		ERROR
	}
}
