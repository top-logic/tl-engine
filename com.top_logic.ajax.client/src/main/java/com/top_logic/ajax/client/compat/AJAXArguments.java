/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.compat;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * The arguments object for {@link AJAX#execute(String, AJAXArguments)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class AJAXArguments extends JavaScriptObject {

	/**
	 * The constructors of GWT {@link JavaScriptObject}s need to be exactly protected and empty.
	 */
	protected AJAXArguments() {
	}

	public final native String setControlCommand(String commandID) /*-{
		return this.controlCommand = commandID;
	}-*/;

	public final native String setControlID(String controlID) /*-{
		return this.controlID = controlID;
	}-*/;

}
