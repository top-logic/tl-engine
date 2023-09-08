/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.Mapping;

/**
 * Utility for topologically sorting graphs.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphUtil {

	/**
	 * Create a topological sort of the given acyclic {@link Graph}.
	 * 
	 * @param graph
	 *        An acyclic graph.
	 * @return A list of nodes in which a node occurs before all nodes that have outgoing edges
	 *         pointing to that node.
	 * 
	 * @see CollectionUtil#topsort(java.util.function.Function, Collection, boolean)
	 */
	public static <T> List<T> sortTopologically(final Graph<T, ?> graph) {
		return CollectionUtil.topsort(outgoingRelation(graph), graph.vertices(), false);
	}

	/**
	 * {@link Mapping} view of the {@link Graph#outgoing(Object)} relation of the given
	 * {@link Graph}.
	 */
	public static <T> Mapping<T, Collection<T>> outgoingRelation(final Graph<T, ?> graph) {
		return new Mapping<>() {
			@Override
			public Collection<T> map(T vertex) {
				return graph.outgoing(vertex);
			}
		};
	}

	/**
	 * {@link Mapping} view of the {@link Graph#outgoing(Object)} relation of the given
	 * {@link Graph}.
	 */
	public static <T> Mapping<T, Collection<T>> incommingRelation(final Graph<T, ?> graph) {
		return new Mapping<>() {
			@Override
			public Collection<T> map(T vertex) {
				return graph.incoming(vertex);
			}
		};
	}

	/**
	 * Adapter of a {@link Graph} seen as {@link GraphAccess} to the outgoing edges.
	 */
	public static <T> GraphAccess<T> asGraphAccess(final Graph<T, ?> graph) {
		return new GraphAccess<>() {
			@Override
			public Collection<? extends T> next(T node, Filter<? super T> filter) {
				return FilterUtil.filterList(filter, graph.outgoing(node));
			}
		};
	}

}
