/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Layout bounds for an object.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Bounds extends JavaScriptObject {

	/**
	 * Creates the {@link Bounds}.
	 */
	protected Bounds() {
		super();
	}

	/**
	 * @see Position
	 */
	public final native Position getPosition() /*-{
		return {
			x : this.x,
			y : this.y
		}
	}-*/;

	/**
	 * @see #getPosition()
	 */
	public final void setPosition(Position position) {
		setX(position.getX());
		setY(position.getY());
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
	 * @see Dimension
	 */
	public final native Dimension getDimension() /*-{
		return {
			width : this.width,
			height : this.height
		}
	}-*/;

	/**
	 * @see #getDimension()
	 */
	public final void setDimension(Dimension dimensions) {
		setWidth(dimensions.getWidth());
		setHeight(dimensions.getHeight());
	}

	/**
	 * Width of the given object.
	 */
	public final native double getWidth() /*-{
		return this.width;
	}-*/;

	/**
	 * @see #getWidth()
	 */
	public final native double setWidth(double width) /*-{
		this.width = width;
	}-*/;

	/**
	 * Height of the given object.
	 */
	public final native double getHeight() /*-{
		return this.height;
	}-*/;

	/**
	 * @see #getHeight()
	 */
	public final native double setHeight(double height) /*-{
		this.height = height;
	}-*/;

	/**
	 * True if the 2 dimensional box given by these {@link Bounds} contains the given
	 *         {@link Position}, otherwise false.
	 */
	public final boolean contains(Position position) {
		double x = position.getX();
		double y = position.getY();

		return contains(x, y);
	}

	/**
	 * @see #contains(Position)
	 */
	public final native boolean contains(double x, double y) /*-{
		if (x > this.x && x < this.x + this.width) {
			return (y > this.y && y < this.y + this.height);
		}

		return false;
	}-*/;
}
