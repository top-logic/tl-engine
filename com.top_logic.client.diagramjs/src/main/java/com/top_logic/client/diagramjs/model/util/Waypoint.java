/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Connection;

/**
 * Two dimensional point describing a waypoint for a {@link Connection}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class Waypoint extends JavaScriptObject {

	/**
	 * Creates a {@link Waypoint}.
	 */
	protected Waypoint() {
		super();
	}

	/**
	 * The X coordinate.
	 */
	public native double getX() /*-{
		return this.x;
	}-*/;

	/**
	 * @see #getX()
	 */
	public native double setX(double x) /*-{
		this.x = x;
	}-*/;

	/**
	 * The Y coordinate.
	 */
	public native double getY() /*-{
		return this.y;
	}-*/;

	/**
	 * @see #getY()
	 */
	public native double setY(double y) /*-{
		this.y = y;
	}-*/;
}
