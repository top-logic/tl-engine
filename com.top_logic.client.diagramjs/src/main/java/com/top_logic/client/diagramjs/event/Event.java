/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.event;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Event object passed to the {@link EventHandler} containing all necessary informations about this
 * emitted event.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class Event extends JavaScriptObject {

	/**
	 * Creates a {@link Event}.
	 */
	protected Event() {
		super();
	}

	/**
	 * Affected element for this event.
	 */
	public native JavaScriptObject getElement() /*-{
		return this.element || {};
	}-*/;
}
