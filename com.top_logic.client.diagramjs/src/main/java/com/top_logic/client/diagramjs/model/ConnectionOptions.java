/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.util.Waypoint;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;

/**
 * Options for a {@link Connection}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class ConnectionOptions extends GraphPartOptions {

	/**
	 * Creates a {@link ConnectionOptions}.
	 */
	protected ConnectionOptions() {
		super();
	}

	/**
	 * Type of this {@link Connection}.
	 */
	public native String getType() /*-{
		return this.connectionType;
	}-*/;

	/**
	 * {@link #getType()}
	 */
	public native void setType(String type) /*-{
		this.connectionType = type;
	}-*/;

	/**
	 * @see Connection#getWaypoints()
	 */
	public native Waypoint[] getWaypoints() /*-{
		return this.waypoints;
	}-*/;

	/**
	 * @see #getWaypoints()
	 * @see DiagramJSObjectUtil#setWaypoints(JavaScriptObject, Waypoint[])
	 */
	public void setWaypoints(Waypoint[] waypoints) {
		DiagramJSObjectUtil.setWaypoints(this, waypoints);
	}

}
