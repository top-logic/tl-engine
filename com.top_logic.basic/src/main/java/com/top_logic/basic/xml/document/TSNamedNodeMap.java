/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import static com.top_logic.basic.xml.document.AbstractTSNode.*;

import java.util.HashMap;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.top_logic.basic.util.Utils;

/**
 * Unmodifiable thread safe {@link NamedNodeMap} implementation.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TSNamedNodeMap extends HashMap<String, Node> implements NamedNodeMap {

	private final Node[] nodes;

	protected TSNamedNodeMap(NamedNodeMap map) {
		int length = map.getLength();
		Node[] _nodes = new Node[length];
		for (int i = 0; i < length; i++) {
			Node item = map.item(i);
			Node myItem = createItem(item);
			put(item.getNodeName(), myItem);
			_nodes[i] = myItem;
		}
		this.nodes = _nodes;
	}

	@Override
	public Node getNamedItem(String name) {
		return get(name);
	}

	@Override
	public Node setNamedItem(Node arg) throws DOMException {
		throw noModificationAllowedErr();
	}

	private Node createItem(Node item) {
		if (item instanceof Attr) {
			return ThreadSafeDOMFactory.importAttr((Attr) item);
		}

		throw new UnsupportedOperationException("Node " + item + " not supported");
	}

	@Override
	public Node removeNamedItem(String name) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node item(int index) {
		if (index < 0 || index >= nodes.length) {
			return null;
		}
		return nodes[index];

	}

	@Override
	public int getLength() {
		return nodes.length;
	}

	@Override
	public Node getNamedItemNS(String namespaceURI, String localName) throws DOMException {
		if (localName == null) {
			return null;
		}
		for (Node n : nodes) {
			if (Utils.equals(namespaceURI, n.getNamespaceURI()) && localName.equals(n.getLocalName())) {
				return n;
			}
		}
		return null;
	}

	@Override
	public Node setNamedItemNS(Node arg) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
		throw noModificationAllowedErr();
	}

}
