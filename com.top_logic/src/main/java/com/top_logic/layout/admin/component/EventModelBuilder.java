/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.Collection;
import java.util.List;

import com.top_logic.base.monitor.bus.EventMonitor;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Returns the list of all events given by {@link EventMonitor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EventModelBuilder implements ListModelBuilder {

	/** Singleton {@link EventModelBuilder} instance. */
	public static final EventModelBuilder INSTANCE = new EventModelBuilder();

	private EventModelBuilder() {
		// singleton instance
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return allEvents();
	}

	private List allEvents() {
		return EventMonitor.getInstance().getEvents();
	}

	/**
	 * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, LayoutComponent)
	 */
	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return allEvents().contains(listElement);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}

