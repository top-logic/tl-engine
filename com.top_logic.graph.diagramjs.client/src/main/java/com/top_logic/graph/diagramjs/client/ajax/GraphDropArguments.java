/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.ajax;

import com.top_logic.ajax.client.compat.AJAXArguments;
import com.top_logic.graph.common.util.GraphControlCommon;

/**
 * Arguments value holder for the {@link GraphControlCommon#GRAPH_DROP_COMMAND}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class GraphDropArguments extends AJAXArguments {

	/**
	 * Must not be invoked.
	 */
	protected GraphDropArguments() {
		super();
	}

	/**
	 * The X coordinate in graph world coordinates where the drop operation happened.
	 */
	public native void setX(double x) /*-{
		this.x = x;
	}-*/;

	/**
	 * The Y coordinate in graph world coordinates where the drop operation happened.
	 */
	public native void setY(double y) /*-{
		this.y = y;
	}-*/;

	/**
	 * The drag data that was transmitted.
	 */
	public native void setData(String data) /*-{
		this.data = data;
	}-*/;

}

