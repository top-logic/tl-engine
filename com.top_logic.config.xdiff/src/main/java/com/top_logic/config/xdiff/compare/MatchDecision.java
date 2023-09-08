/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.compare;

import com.top_logic.config.xdiff.model.Node;

/**
 * Algorithm that decides whether {@link Node}s can be transformed into each other (without deleting
 * one node and inserting the other).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface MatchDecision {

	/**
	 * Whether the first node can be transformed into the second.
	 * 
	 * @param node1
	 *        The first node.
	 * @param node2
	 *        The second node.
	 * @return Whether <code>node1</code> can be transformed into <code>node2</code>.
	 */
	boolean canMatch(Node node1, Node node2);

}
