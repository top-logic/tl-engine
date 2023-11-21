/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import java.util.Arrays;
import java.util.List;

import com.top_logic.client.diagramjs.model.util.Waypoint;

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
	 * Business object of this {@link Connection}.
	 */
	public native Object getSharedGraphPart() /*-{
		return this.sharedGraphPart;
	}-*/;

	/**
	 * {@link #getSharedGraphPart()}
	 */
	public native void setSharedGraphPart(Object sharedGraphPart) /*-{
		this.sharedGraphPart = sharedGraphPart;
	}-*/;

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
	public List<Waypoint> getWaypoints() {
		return Arrays.asList(getWaypointsInternal());
	}

	private native Waypoint[] getWaypointsInternal() /*-{
		return this.waypoints;
	}-*/;

	/**
	 * @see #getWaypoints()
	 */
	public native void setWaypoints(Waypoint[] waypoints) /*-{
		this.waypoints = waypoints;
	}-*/;

}
