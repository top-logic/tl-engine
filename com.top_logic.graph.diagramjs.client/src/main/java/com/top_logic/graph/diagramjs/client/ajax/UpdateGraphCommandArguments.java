/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.ajax;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAXArguments;

/**
 * The arguments for calling the GraphUpdateCommand.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UpdateGraphCommandArguments extends AJAXArguments {

	/**
	 * The constructors of GWT {@link JavaScriptObject}s need to be exactly protected and empty.
	 */
	protected UpdateGraphCommandArguments() {

	}

	/**
	 * The description of the update as JSON object.
	 * <p>
	 * This parameter is mandatory.
	 * </p>
	 */
	public final native String setGraphUpdate(String graphUpdate) /*-{
		return this.graphUpdate = graphUpdate;
	}-*/;

}
