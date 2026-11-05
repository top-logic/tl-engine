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

	/**
	 * Sets the name of the command to execute on the target control.
	 *
	 * @param commandID
	 *        The command name.
	 * @return The given command name.
	 */
	public final native String setControlCommand(String commandID) /*-{
		return this.controlCommand = commandID;
	}-*/;

	/**
	 * Sets the ID of the control that is the target of the request.
	 *
	 * @param controlID
	 *        The ID of the target control.
	 * @return The given control ID.
	 */
	public final native String setControlID(String controlID) /*-{
		return this.controlID = controlID;
	}-*/;

}
