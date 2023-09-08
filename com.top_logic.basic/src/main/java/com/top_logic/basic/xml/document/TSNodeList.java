/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml.document;

import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Thread safe immutable {@link NodeList} implementation.
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class TSNodeList implements NodeList {

	private final Node[] nodes;

	protected TSNodeList(NodeList sourceChildNodes, Node parent) {
		this.nodes = createChildNodes(sourceChildNodes, parent);
	}

	private Node[] createChildNodes(NodeList sourceChildNodes, Node parent) {
		int length = sourceChildNodes.getLength();
		Node[] childNodes = new Node[length];

		AbstractTSNode prevSibl = null;
		for (int i = 0; i < length; i++) {
			AbstractTSNode createdItem = AbstractTSNode.createItem(sourceChildNodes.item(i), parent);
			if (prevSibl != null) {
				prevSibl.setNextSibling(createdItem);
				createdItem.setPreviousSibling(prevSibl);
			}
			prevSibl = createdItem;
			childNodes[i] = createdItem;
		}
		return childNodes;
	}

	public TSNodeList(List<? extends Node> sourceChildNodes, Node parent) {
		int length = sourceChildNodes.size();
		Node[] childNodes = new Node[length];

		AbstractTSNode prevSibl = null;
		int i = 0;
		for (Node theNode : sourceChildNodes) {
			AbstractTSNode createdItem = AbstractTSNode.createItem(theNode, parent);
			if (prevSibl != null) {
				prevSibl.setNextSibling(createdItem);
				createdItem.setPreviousSibling(prevSibl);
			}
			prevSibl = createdItem;
			childNodes[i++] = createdItem;
		}
		this.nodes = childNodes;
	}

	@Override
	public int getLength() {
		return this.nodes.length;
	}

	@Override
	public Node item(int index) {
		if (index < 0 || index >= getLength()) {
			// null at invalid index as API says
			return null;
		}
		return this.nodes[index];
	}
	
	@Override
	public String toString() {
		return Arrays.toString(nodes);
	}
}