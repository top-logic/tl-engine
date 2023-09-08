/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import com.top_logic.config.xdiff.model.Element;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.model.NodeType;
import com.top_logic.config.xdiff.util.Utils;

/**
 * {@link MatchDecision} that produces the most general differences expressible with the
 * {@link XDiffSchema} language.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultMatchDecision implements MatchDecision {

	/**
	 * Singleton {@link DefaultMatchDecision} instance.
	 */
	public static final MatchDecision INSTANCE = new DefaultMatchDecision();

	private DefaultMatchDecision() {
		// Singleton constructor.
	}

	@Override
	public boolean canMatch(Node node1, Node node2) {
		return bothElementNodes(node1, node2) && canMatchElements((Element) node1, (Element) node2);
	}

	private static boolean canMatchElements(Element element1, Element element2) {
		return element1.getLocalName().equals(element2.getLocalName()) &&
			Utils.equalsNullsafe(element1.getNamespace(), element2.getNamespace());
	}

	private static boolean bothElementNodes(Node node1, Node node2) {
		return node1.getNodeType() == NodeType.ELEMENT && node2.getNodeType() == NodeType.ELEMENT;
	}
}
