/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Unmodifiable {@link NamedNodeMap} without any node.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyNamedNodeMap implements NamedNodeMap {

	private static final Node NO_SUCH_NODE = null;
	
	/**
	 * Singleton {@link EmptyNamedNodeMap} instance.
	 */
	public static final EmptyNamedNodeMap INSTANCE = new EmptyNamedNodeMap();

	private EmptyNamedNodeMap() {
		// singleton instance
	}

	@Override
	public int getLength() {
		return 0;
	}
	
	@Override
	public Node getNamedItem(String name) {
		return NO_SUCH_NODE;
	}

	@Override
	public Node item(int index) {
		return NO_SUCH_NODE;
	}

	@Override
	public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
		return NO_SUCH_NODE;
	}

	@Override
	public Node setNamedItem(Node arg) throws DOMException {
		throw AbstractTSNode.noModificationAllowedErr();
	}
	
	@Override
	public Node removeNamedItem(String name) throws DOMException {
		throw AbstractTSNode.noModificationAllowedErr();
	}
	
	@Override
	public Node setNamedItemNS(Node arg) throws DOMException {
		throw AbstractTSNode.noModificationAllowedErr();
	}

	@Override
	public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
		throw AbstractTSNode.noModificationAllowedErr();
	}
	
	@Override
	public String toString() {
		return "{}";
	}

}

