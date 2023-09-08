/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.ajax;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAXArguments;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.SharedObject;
import com.top_logic.graph.common.model.GraphPart;

/**
 * The arguments for a {@link GraphPart}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class GraphPartCommandArguments extends AJAXArguments {

	/**
	 * The constructors of GWT {@link JavaScriptObject}s need to be exactly protected and empty.
	 */
	protected GraphPartCommandArguments() {

	}

	/**
	 * {@link ObjectData#id()} of the {@link SharedObject} to resolve the sharet object on server
	 * side.
	 */
	public final native void setID(String id) /*-{
		this.id = id;
	}-*/;
}
