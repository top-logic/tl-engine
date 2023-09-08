/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collection;

import com.top_logic.basic.col.Filter;

/**
 * Encapsulation of graph edge traversal for a {@link Traversal} algorithm.
 * 
 * @param <T> The node type of the graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphAccess<T> {

	/**
	 * Report all nodes that are directly reachable by the given node.
	 * 
	 * @param node
	 *        The node of a graph from which directly reachable nodes should be
	 *        reported.
	 * @param filter
	 *        An optional filter that limits reported children.
	 *        <code>null</code> means all nodes.
	 * @return The nodes that are directly reachable from the given node.
	 */
	Collection<? extends T> next(T node, Filter<? super T> filter);
	
}
