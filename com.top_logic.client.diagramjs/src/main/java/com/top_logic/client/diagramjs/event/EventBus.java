/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.event;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * {@link EventBus} is used to communicate across a diagram instance. Other parts of a diagram can
 * use it to listen to and broadcast events.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class EventBus extends JavaScriptObject {

	/**
	 * Creates a {@link EventBus}.
	 */
	protected EventBus() {
		super();
	}

	/**
	 * Adds an {@link EventHandler} for the given diagramjs event.
	 */
	public native void addEventHandler(String event, EventHandler handler) /*-{
		this.on(event, handler);
	}-*/;

}
