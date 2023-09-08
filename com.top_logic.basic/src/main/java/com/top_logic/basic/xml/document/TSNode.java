/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unmodifiable thread safe {@link AbstractTSNode} which has may have children.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public abstract class TSNode extends AbstractTSNode {

	private final NodeList children;
	private final Node parentNode;

	public TSNode(Node sourceNode, Node parent) {
		super(sourceNode);
		this.parentNode = parent;
		if (sourceNode.hasChildNodes()) {
			NodeList sourceList = sourceNode.getChildNodes();
			this.children = ThreadSafeDOMFactory.importSourceList(sourceList, this);
		} else {
			this.children = EmptyNodeList.INSTANCE;
		}
	}

	@Override
	public NodeList getChildNodes() {
		return this.children;
	}

	@Override
	public Node getFirstChild() {
		if (this.children.getLength() == 0) {
			return null;
		} else {
			return this.children.item(0);
		}
	}

	@Override
	public Node getLastChild() {
		if (this.children.getLength() == 0) {
			return null;
		} else {
			return this.children.item(this.children.getLength() - 1);
		}
	}

	@Override
	public boolean hasChildNodes() {
		return this.children.getLength() > 0;
	}

	@Override
	public Node replaceChild(Node aNewChild, Node aOldChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node removeChild(Node aOldChild) throws DOMException {
		throw noModificationAllowedErr();
	}

	@Override
	public Node appendChild(Node aNewChild) throws DOMException {
		throw noModificationAllowedErr();
	}
	
	@Override
	public Node getParentNode() {
		return parentNode;
	}
}