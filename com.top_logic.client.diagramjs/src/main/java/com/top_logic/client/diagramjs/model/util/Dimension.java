/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.model.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Vertical and horizontal dimensions of an object.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Dimension extends JavaScriptObject {

	/**
	 * Creates a {@link Dimension}.
	 */
	protected Dimension() {
		super();
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
}
