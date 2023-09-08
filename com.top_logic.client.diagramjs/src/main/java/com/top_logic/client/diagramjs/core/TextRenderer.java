/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.util.Dimension;

/**
 * Rendering utility for text.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class TextRenderer extends JavaScriptObject {

	/**
	 * Creates a {@link TextRenderer}
	 */
	protected TextRenderer() {
		super();
	}

	/**
	 * Graphical dimensions of the rectangle surrounding the given text.
	 */
	public final native Dimension getDimensions(String text) /*-{
		return this.getDimensions(text);
	}-*/;

	/**
	 * Height of the given text.
	 * 
	 * @see #getDimensions(String)
	 * @see Dimension#getHeight()
	 */
	public final double getHeight(String text) {
		return getDimensions(text).getHeight();
	}

	/**
	 * Width of the given text.
	 * 
	 * @see #getDimensions(String)
	 * @see Dimension#getWidth()
	 */
	public final double getWidth(String text) {
		return getDimensions(text).getWidth();
	}
}
