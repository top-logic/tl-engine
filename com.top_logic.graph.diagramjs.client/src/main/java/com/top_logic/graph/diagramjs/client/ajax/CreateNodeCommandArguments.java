/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.ajax;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.ajax.client.compat.AJAXArguments;

/**
 * Arguments to create a node.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateNodeCommandArguments extends AJAXArguments {

	/**
	 * The constructors of GWT {@link JavaScriptObject}s need to be exactly protected and empty.
	 */
	protected CreateNodeCommandArguments() {

	}

	/**
	 * Expected x coordinate for the node.
	 */
	public final native void setX(double x) /*-{
		this.x = x;
	}-*/;

	/**
	 * Expected y coordinate for the node.
	 */
	public final native void setY(double y) /*-{
		this.y = y;
	}-*/;

	/**
	 * Expected height for the node.
	 */
	public final native void setHeight(double height) /*-{
		this.height = height;
	}-*/;

	/**
	 * Expected width for the node.
	 */
	public final native void setWidth(double width) /*-{
		this.width = width;
	}-*/;
}
