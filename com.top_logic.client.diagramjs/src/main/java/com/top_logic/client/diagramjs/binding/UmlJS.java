/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.binding;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import com.top_logic.client.diagramjs.core.Diagram;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Facade for native <code>diagram-js</code> functionality.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@JsType(namespace = JsPackage.GLOBAL, name = "UmlJS")
public class UmlJS {

	/**
	 * Creates a new {@link Diagram} within the given {@link Element}.
	 */
	public static native Diagram createDiagram(Element parent, String containerID) /*-{
		return new $wnd.UmlJS({
			parent : parent,
			containerID : containerID
		});
	}-*/;

	/**
	 * Utility to create a {@link Diagram} in an element identified by the given ID.
	 *
	 * @param parentID
	 *        The ID of the parent element.
	 * @return The created {@link Diagram}.
	 */
	public static Diagram createDiagramId(String parentID, String containerID) {
		Element parent = Document.get().getElementById(parentID);

		return createDiagram(parent, containerID);
	}
}
