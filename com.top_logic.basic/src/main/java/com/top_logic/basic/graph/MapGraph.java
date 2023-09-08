/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.col.Filter;

/**
 * Wrapper that builds a {@link GraphAccess} upon a multi-map.
 * 
 * @param <T> The node type of the graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MapGraph<T> implements GraphAccess<T> {
	
	private final Map<T, ? extends Collection<T>> edges;

	/**
	 * Creates a {@link MapGraph}.
	 * 
	 * @param edges
	 *        The map of nodes to directly reachable nodes.
	 */
	public MapGraph(Map<T, ? extends Collection<T>> edges) {
		this.edges = edges;
	}

	@Override
	public Collection<? extends T> next(T node, Filter<? super T> filter) {
		Collection<T> result = edges.get(node);
		if (result == null) {
			return Collections.emptySet();
		} 
		
		return result;
	}

}
