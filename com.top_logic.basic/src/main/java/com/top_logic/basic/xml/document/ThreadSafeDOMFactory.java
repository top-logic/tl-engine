/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Factory to create thread safe DOM implementations.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ThreadSafeDOMFactory {

	/**
	 * Creates a thread safe copy of the given document.
	 */
	public static TSDocument importDocument(Document sourceDocument) {
		return new TSDocument(sourceDocument);
	}

	/**
	 * Creates a thread safe copy of the given attribute.
	 */
	public static TSAttr importAttr(Attr anAttr) {
		return new TSAttr(anAttr);
	}

	/**
	 * Creates a thread safe copy of the given element.
	 */
	public static TSElement importElement(Element sourceElement, Node parent) {
		return new TSElement(sourceElement, parent);
	}

	/**
	 * Creates a thread safe copy of the given attribute map.
	 */
	public static NamedNodeMap importNamedNodeMap(NamedNodeMap map) {
		return new TSNamedNodeMap(map);
	}

	/**
	 * Creates a thread safe copy of the given text node.
	 */
	public static TSText importText(Text sourceNode, Node parent) {
		return new TSText(sourceNode, parent);
	}

	/**
	 * Creates a thread safe copy of the given node list.
	 */
	public static NodeList importSourceList(NodeList sourceList, Node newParent) {
		return new TSNodeList(sourceList, newParent);
	}

}
