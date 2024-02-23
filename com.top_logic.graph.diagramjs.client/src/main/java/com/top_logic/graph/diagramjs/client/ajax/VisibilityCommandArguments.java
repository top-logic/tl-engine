/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.ajax;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAXArguments;
import com.top_logic.graph.diagramjs.client.service.event.ElementVisibilityEventHandler;

/**
 * The arguments for the command that is executed when a collection of diagram elements have changed
 * their visibility.
 * 
 * @see ElementVisibilityEventHandler
 * 
 * @author <a href="mailto:sven.foerster@top-logic.com">Sven F�rster</a>
 */
public class VisibilityCommandArguments extends AJAXArguments {

	/**
	 * The constructors of GWT {@link JavaScriptObject}s need to be exactly protected and empty.
	 */
	protected VisibilityCommandArguments() {

	}

	/**
	 * The elements visibility state.
	 */
	public final native void setVisibility(boolean isVisible) /*-{
		this.visibility = isVisible;
	}-*/;

	/**
	 * The ids of the elements that have changed their visibility.
	 */
	public final native void setIDs(String[] ids) /*-{
		this.ids = ids;
	}-*/;

}
