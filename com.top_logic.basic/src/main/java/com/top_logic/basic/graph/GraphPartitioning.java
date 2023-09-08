/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Algorithm for partitioning graphs into graphs of independent subgraphs.
 * 
 * @see #partition(Graph, Iterable)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphPartitioning<V, E> {

	private Map<V, Graph<V, E>> _componentGraphByVertex = new HashMap<>();

	/**
	 * Creates a {@link GraphPartitioning}.
	 * 
	 * <p>
	 * A {@link GraphPartitioning} instance must only be used for one
	 * {@link #decompose(Graph, Iterable)} operation.
	 * </p>
	 * 
	 * @see #partition(Graph, Iterable)
	 */
	protected GraphPartitioning() {
		super();
	}

	/**
	 * Partitions the given {@link Graph} into independent subgraphs.
	 * 
	 * @param graph
	 *        The input {@link Graph}.
	 * @param components
	 *        A partitioning of vertices of the given graph. Each vertex set builds up its own
	 *        independent subgraph in the result.
	 * @return A graph with subgraphs as its vertices. Edges in the input graph are either connect
	 *         subgraphs in the result graph, or vertices in the same independent subgraph.
	 */
	public static <V, E> Graph<Graph<V, E>, E> partition(Graph<V, E> graph, Iterable<Set<V>> components) {
		return new GraphPartitioning<V, E>().decompose(graph, components);
	}

	/**
	 * Decomposes the given graph into independent subgraphs.
	 * 
	 * @see #partition(Graph, Iterable)
	 */
	protected final Graph<Graph<V, E>, E> decompose(Graph<V, E> graph, Iterable<Set<V>> components) {
		Graph<Graph<V, E>, E> result = newResultGraph();

		// Build subgraphs.
		for (Set<V> component : components) {
			Graph<V, E> componentGraph = newComponentGraph();
			for (V vertex : component) {
				componentGraph.add(vertex);
				addComponentGraph(vertex, componentGraph);
			}
			result.add(componentGraph);
		}

		// Link components.
		for (V source : graph.vertices()) {
			Graph<V, E> sourceGraph = lookupComponentGraph(source);
			for (V target : graph.outgoing(source)) {
				E edge = graph.edge(source, target);

				Graph<V, E> targetGraph = lookupComponentGraph(target);
				if (targetGraph == sourceGraph) {
					sourceGraph.connect(source, target, edge);
				} else {
					result.connect(sourceGraph, targetGraph, edge);
				}
			}
		}

		return result;
	}

	/**
	 * Creates the result {@link Graph}.
	 */
	protected Graph<Graph<V, E>, E> newResultGraph() {
		return new HashGraph<>();
	}

	/**
	 * Creates a component {@link Graph}.
	 */
	protected Graph<V, E> newComponentGraph() {
		return new HashGraph<>();
	}

	private void addComponentGraph(V vertex,
			Graph<V, E> componentGraph) {
		Graph<V, E> clash = _componentGraphByVertex.put(vertex, componentGraph);
		if (clash != null) {
			throw new IllegalArgumentException("Not a partitioning, vertex '" + vertex
				+ "' in more than one component.");
		}
	}

	private Graph<V, E> lookupComponentGraph(V vertex) {
		Graph<V, E> componentGraph = _componentGraphByVertex.get(vertex);
		if (componentGraph == null) {
			throw new IllegalArgumentException("Not a partitioning, vertex '" + vertex
				+ "' in no component.");
		}
		return componentGraph;
	}

}
