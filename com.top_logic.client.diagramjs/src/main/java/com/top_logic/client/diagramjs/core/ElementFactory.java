/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.core;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.Root;
import com.top_logic.client.diagramjs.model.Shape;

/**
 * Factory for {@link Diagram} components.
 * 
 * @see Diagram#getElementFactory()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ElementFactory extends JavaScriptObject {

	/**
	 * Creates a {@link ElementFactory}.
	 */
	protected ElementFactory() {
		super();
	}

	/**
	 * Creates a {@link Root} component of a {@link Diagram}.
	 * 
	 * @see Canvas#setRootElement(Root)
	 */
	public native Root createRoot() /*-{
		var root = this.createRoot();

		root.isVisible = true;

		return root;
	}-*/;

	/**
	 * Creates a {@link Label} component of a {@link Diagram}.
	 */
	public final native Label createLabel(JavaScriptObject attributes) /*-{
		return this.createUmlLabel(attributes);
	}-*/;

	/**
	 * Creates a {@link Shape} component of a {@link Diagram}.
	 */
	public final native Shape createShape(JavaScriptObject attributes) /*-{
		return this.createShape(attributes);
	}-*/;

	/**
	 * Creates a {@link Class} component of a {@link Diagram}.
	 */
	public final native Shape createClass(JavaScriptObject attributes) /*-{
		return this.createClass(attributes);
	}-*/;

	/**
	 * Creates a {@link Connection} in a {@link Diagram}.
	 */
	public final native Connection createConnection(JavaScriptObject attributes) /*-{
		return this.createConnection(attributes);
	}-*/;

}
