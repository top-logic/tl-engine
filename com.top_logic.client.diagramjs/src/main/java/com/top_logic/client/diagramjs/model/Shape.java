/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.core.ElementFactory;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Position;

/**
 * A node in a {@link Diagram} that can be connected with {@link Connection}s.
 * 
 * @see ElementFactory#createShape(JavaScriptObject)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Shape extends Base {

	/**
	 * Creates a {@link Shape}.
	 */
	protected Shape() {
		super();
	}

	/**
	 * The X coordinate of the top-left corner of this {@link Shape}.
	 */
	public final native double getX() /*-{
		return this.x;
	}-*/;

	/**
	 * @see #getX()
	 */
	public final native void setX(double x) /*-{
		this.x = x;
	}-*/;

	/**
	 * The Y coordinate of the top-left corner of this {@link Shape}.
	 */
	public final native double getY() /*-{
		return this.y;
	}-*/;

	/**
	 * @see #getY()
	 */
	public final native void setY(double y) /*-{
		this.y = y;
	}-*/;

	/**
	 * The width of this {@link Shape}.
	 */
	public final native double getWidth() /*-{
		return this.width;
	}-*/;

	/**
	 * @see #getWidth()
	 */
	public final native void setWidth(double width) /*-{
		this.width = width;
	}-*/;

	/**
	 * The height of this {@link Shape}.
	 */
	public final native double getHeight() /*-{
		return this.height;
	}-*/;

	/**
	 * @see #getHeight()
	 */
	public final native void setHeight(double height) /*-{
		this.height = height;
	}-*/;

	/**
	 * @see Bounds
	 */
	public final native Bounds getBounds() /*-{
		return {
			x : this.x,
			y : this.y,
			width : this.width,
			height : this.height
		}
	}-*/;

	/**
	 * Position of the current {@link Shape}.
	 */
	public final native Position getPosition() /*-{
		return {
			x : this.x,
			y : this.y
		}
	}-*/;

	/**
	 * Whether this object is resized.
	 */
	public final native boolean isResized() /*-{
		return this.isResized;
	}-*/;

	/**
	 * @see #isResized()
	 */
	public final native void setIsResized(boolean isResized) /*-{
		this.isResized = isResized;
	}-*/;
}
