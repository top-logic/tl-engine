/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Horizontal and vertical origin coordinate of this object.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Position extends JavaScriptObject {

	/**
	 * Creates the {@link Position}.
	 */
	protected Position() {
		super();
	}

	/**
	 * Horizontal coordinate origin.
	 */
	public final native double getX() /*-{
		return this.x;
	}-*/;

	/**
	 * @see #getX()
	 */
	public final native double setX(double x) /*-{
		this.x = x;
	}-*/;

	/**
	 * Vertical coordinate origin.
	 */
	public final native double getY() /*-{
		return this.y;
	}-*/;

	/**
	 * @see #getY()
	 */
	public final native double setY(double y) /*-{
		this.y = y;
	}-*/;

	/**
	 * Move the position by the given delta.
	 */
	public final void move(double deltaX, double deltaY) {
		setX(getX() + deltaX);
		setY(getY() + deltaY);
	}

}
