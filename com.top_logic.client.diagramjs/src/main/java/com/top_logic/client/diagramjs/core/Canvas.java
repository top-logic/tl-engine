/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.Root;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;

/**
 * Drawing area of a {@link Diagram}.
 * 
 * @see Diagram#getCanvas()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Canvas extends JavaScriptObject {

	/**
	 * Creates a {@link Diagram}.
	 */
	protected Canvas() {
		super();
	}

	/**
	 * Installs the root element.
	 */
	public native void setRootElement(Root root)/*-{
		this.setRootElement(root);
	}-*/;

	/**
	 * Get the current root element of this {@link Canvas}.
	 */
	public native Root getRootElement()/*-{
		this.getRootElement();
	}-*/;

	/**
	 * Adds the given {@link Shape} to the {@link Root} of a {@link Diagram}.
	 */
	public native void addShape(Shape shape, Base parent) /*-{
		this.addShape(shape, parent);
	}-*/;

	/**
	 * Adds the given {@link Connection} to the {@link Root} of a {@link Diagram}.
	 */
	public native void addConnection(Connection connection, Base parent) /*-{
		this.addConnection(connection, parent);
	}-*/;

	/**
	 * Current viewbox.
	 */
	public native Bounds getViewbox() /*-{
		return this.viewbox();
	}-*/;

	/**
	 * @see #getViewbox()
	 */
	public native void setViewbox(Bounds bounds) /*-{
		this.viewbox(bounds);
	}-*/;

	/**
	 * Fit the current viewport with the given center.
	 */
	public native void fitViewport(Position center) /*-{
		this.zoom('fit-viewport', center);
	}-*/;

	/**
	 * Return the absolute bounding box for the given element.
	 */
	public native Bounds getAbsoluteBBox(Base element) /*-{
		return this.getAbsoluteBBox(element);
	}-*/;

	/**
	 * The current {@link Canvas} width and height.
	 */
	public native Dimension getSize() /*-{
		return this.getSize();
	}-*/;

	/**
	 * True if the given element origins displayed in the current viewbox, otherwise false.
	 */
	public boolean inViewbox(List<Base> elements) {
		for (Base element : elements) {
			if (!inViewbox(element)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * True if the given element origin displayed in the current viewbox, otherwise false.
	 */
	public boolean inViewbox(Base element) {
		if (DiagramJSObjectUtil.isShape(element)) {
			return inViewbox((Shape) element);
		} else if (DiagramJSObjectUtil.isConnection(element)) {
			return inViewbox((Connection) element);
		} else if (DiagramJSObjectUtil.isLabel(element)) {
			return inViewbox((Shape) element);
		}

		return false;
	}

	/**
	 * True if the given shape origin displayed in the current viewbox, otherwise false.
	 */
	public boolean inViewbox(Shape shape) {
		return getViewbox().contains(shape.getPosition());
	}

	/**
	 * True if the source or target {@link Shape} origin of given connection displayed in
	 *         the current viewbox, otherwise false.
	 */
	public boolean inViewbox(Connection connection) {
		Shape source = connection.getSource();
		Shape target = connection.getTarget();

		return getViewbox().contains(source.getPosition()) || getViewbox().contains(target.getPosition());
	}
}
