/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.ElementFactory;
import com.top_logic.client.diagramjs.model.util.Waypoint;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;

/**
 * Connection between {@link Shape}s.
 * 
 * @see ElementFactory#createConnection(JavaScriptObject)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Connection extends Base {
	/**
	 * Creates a {@link Connection}.
	 */
	protected Connection() {
		super();
	}

	/**
	 * Waypoints connected through straight lines to form this {@link Connection}.
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

	/**
	 * Type of this Edge, for example inheritance, aggregation, etc.
	 */
	public native String getType() /*-{
		return this.connectionType;
	}-*/;

	/**
	 * Source {@link Shape} of this {@link Connection}.
	 */
	public native Shape getSource() /*-{
		return this.source;
	}-*/;

	/**
	 * Target {@link Shape} of this {@link Connection}.
	 */
	public native Shape getTarget() /*-{
		return this.target;
	}-*/;
}
