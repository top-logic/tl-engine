/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.util.Waypoint;

/**
 * Layouts diagramJS components.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Layouter extends JavaScriptObject {

	/**
	 * Creates a {@link Layouter}.
	 */
	protected Layouter() {
		super();
	}

	/**
	 * Layout the given connection.
	 */
	public final native Waypoint[] layoutConnection(Connection connection) /*-{
		return this.layoutConnection(connection);
	}-*/;

	/**
	 * True if diagram elements that are not visible (i. e. <code>!element.isVisible</code>) should
	 * be shown with a special styling in order to recognize them, otherwise false.
	 * 
	 * <p>
	 * see <code>SVGDrawUtil.js#setVisibilityStyles</code>
	 * </p>
	 */
	public final native boolean showHiddenElements() /*-{
		return this.showHiddenElements;
	}-*/;

	/**
	 * @see #showHiddenElements()
	 */
	public final native void setShowHiddenElements(boolean showHiddenElements) /*-{
		this.showHiddenElements = showHiddenElements;
	}-*/;
}
