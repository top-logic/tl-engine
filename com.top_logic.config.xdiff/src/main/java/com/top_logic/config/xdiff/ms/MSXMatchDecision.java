/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.ms;

import static com.top_logic.config.xdiff.util.Utils.*;

import com.top_logic.config.xdiff.compare.DefaultMatchDecision;
import com.top_logic.config.xdiff.compare.MatchDecision;
import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.model.QNameOrder;

/**
 * {@link MatchDecision} that restricts the difference to one that is expressible with the
 * {@link MSXDiffSchema} language.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MSXMatchDecision implements MatchDecision {

	/**
	 * Singleton {@link MSXMatchDecision} instance.
	 */
	public static final MatchDecision INSTANCE = new MSXMatchDecision();

	private MSXMatchDecision() {
		// Singleton constructor.
	}

	@Override
	public boolean canMatch(Node node1, Node node2) {
		boolean generalMatch = DefaultMatchDecision.INSTANCE.canMatch(node1, node2);
		if (!generalMatch) {
			return false;
		}

		Element element1 = (Element) node1;
		Element element2 = (Element) node2;
		boolean hasAttributeRemoves =
			!isSubset(QNameOrder.INSTANCE, element1.getOrderedAttributes(), element2.getOrderedAttributes());
		if (hasAttributeRemoves) {
			return false;
		}

		if (element1.equals(element2)) {
			return true;
		}

		NodeType type1 = consistentContentType(element1);
		NodeType type2 = consistentContentType(element2);

		return (type1 == null && (type2 == null || type2 == NodeType.ELEMENT || type2 == NodeType.TEXT)) ||
			(type1 == NodeType.TEXT && (type2 == null || type2 == NodeType.TEXT)) ||
			(type1 == NodeType.ELEMENT && (type2 == null || type2 == NodeType.ELEMENT));
	}

	private static NodeType consistentContentType(Element element) {
		NodeType nodeType = null;
		for (Node child : element.getChildren()) {
			if (nodeType == NodeType.TEXT) {
				// No the only text node.
				return NodeType.FRAGMENT;
			}
			if (child.getNodeType() != nodeType) {
				if (nodeType == null) {
					// The first node.
					nodeType = child.getNodeType();
				} else {
					// Incompatible contents.
					return NodeType.FRAGMENT;
				}
			}
		}
		return nodeType;
	}


}
