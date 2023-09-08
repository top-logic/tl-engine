/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

/**
 * Callback for a {@link Traversal} algorithm. 
 * 
 * @param <T> The node type of the traversed graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TraversalListener<T> {

	/**
	 * Called in a graph {@link Traversal#traverse(Object, TraversalListener)
	 * traversal} for each node selected by the {@link Traversal}.
	 * 
	 * @param node
	 *        The selected node of the {@link Traversal}.
	 * @param depth
	 *        The length of the path from the {@link Traversal} start node to
	 *        the reported node.
	 * @return Whether traversal should continue normally, <code>false</code> to
	 *         stop traversal immediately after visiting the given node.
	 */
	boolean onVisit(T node, int depth);
	
}
