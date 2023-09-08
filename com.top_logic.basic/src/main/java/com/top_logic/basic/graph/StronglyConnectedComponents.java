/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.util.Computation;

/**
 * Implementation of <a href=
 * "http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm" >Tarjan's
 * algorithm</a> for resolving all strongly connected components in a {@link Graph}.
 * 
 * @param <V>
 *        See {@link Graph}.
 * @param <E>
 *        See {@link Graph}.
 * 
 * @see #findComponents(Graph)
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class StronglyConnectedComponents<V, E> implements Computation<List<Set<V>>> {

	/**
	 * The {@link Graph} to resolve the strongly connected components for.
	 */
	private final Graph<V, E> _graph;

	/**
	 * The {@link IndexedStack} of vertices representing the current exploration depth.
	 */
	private IndexedStack<V> _stack;

	/**
	 * The next DFS index to assign to a discovered vertex.
	 */
	private int _nextIndex;

	/**
	 * Information assigned to each vertex during computation.
	 */
	private Map<V, VertexInfo> _infoByVertex;

	/**
	 * The found components of the graph.
	 */
	private List<Set<V>> _components;

	/**
	 * Create a new {@link StronglyConnectedComponents} for the given graph.
	 * 
	 * <p>
	 * Better use utility method {@link #findComponents(Graph)}.
	 * </p>
	 * 
	 * @param graph
	 *        the {@link Graph} to resolve the components for
	 */
	public StronglyConnectedComponents(final Graph<V, E> graph) {
		// remember the given graph for internal access
		_graph = graph;
	}

	/**
	 * Partitions the vertices of the given {@link Graph} into a topologically sorted list of
	 * strongly connected components.
	 * 
	 * @param graph
	 *        The input {@link Graph}.
	 * @return The resulting list of strongly connected components. See
	 *         {@link GraphPartitioning#partition(Graph, Iterable)} for further processing.
	 */
	public static <V, E> List<Set<V>> findComponents(Graph<V, E> graph) {
		return new StronglyConnectedComponents<>(graph).run();
	}

	@Override
	public List<Set<V>> run() {
		setup();
		try {
			return explore();
		} finally {
			teardown();
		}
	}

	/**
	 * Setup the instance for computation.
	 */
	private void setup() {
		_nextIndex = 0;
		_stack = new IndexedStack<>();
		_infoByVertex = new HashMap<>();
		_components = new ArrayList<>();
	}

	/**
	 * Tear-down the instance after computation.
	 */
	private void teardown() {
		_nextIndex = 0;
		_stack = null;
		_infoByVertex = null;
		_components = null;
	}

	/**
	 * Explore the graph and resolve the strongly connected components.
	 * 
	 * @return a topologically sorted {@link List} of strongly connected components
	 */
	private List<Set<V>> explore() {
		// Go through all vertices in the graph and explore the subgraph starting at this vertex.
		for (final V vertex : _graph.vertices()) {
			VertexInfo info = getInfo(vertex);
			if (info == null) {
				// Visit each vertex exactly once.
				dfs(vertex);
			}
		}

		// Since Tarjan's algorithm finds the strongly connected components
		// in the reverse topological order, the list must be reversed to get the components in
		// topological order.
		Collections.reverse(_components);

		return _components;
	}

	/**
	 * Perform the depth-first search traversal starting at the given vertex.
	 * 
	 * @param current
	 *        the vertex to start the depth-first search from
	 * @return The information computed by visiting the given node.
	 */
	private VertexInfo dfs(final V current) {
		// Index the given vertex using the current vertex index.
		VertexInfo info = discover(current);

		// Push the given vertex on top of the stack.
		_stack.push(current);

		// Navigate from the current vertex using outgoing edges.
		for (final V target : _graph.outgoing(current)) {
			VertexInfo targetInfo = getInfo(target);
			if (targetInfo == null) {
				// The target vertex has not yet been visited, since its info object is not yet
				// installed, descend.
				targetInfo = dfs(target);

				// Update the current vertex' link depth.
				info.updateLowLink(targetInfo.lowLink());
			} else if (_stack.contains(target)) {
				// The target vertex is on the stack - also currently visited. The current edge is a
				// backwards edge.
				info.updateLowLink(targetInfo.index());
			}
		}

		if (info.isComponentRoot()) {
			// A root of a strongly connected component has been found, extract the complete
			// component.
			Set<V> component = popComponent(current);
			_components.add(component);
		}

		return info;
	}

	/**
	 * Installs an {@link VertexInfo} for the given vertex.
	 * 
	 * @param vertex
	 *        The vertex to set the info for.
	 * @return The initial {@link VertexInfo} for the given vertex.
	 */
	private VertexInfo discover(final V vertex) {
		VertexInfo info = new VertexInfo(_nextIndex++);
		_infoByVertex.put(vertex, info);
		return info;
	}

	private VertexInfo getInfo(final V vertex) {
		return _infoByVertex.get(vertex);
	}

	/**
	 * Pops all vertices from the exploration stack up (and including) the given root vertex and add
	 * them to a new component.
	 * 
	 * @param root
	 *        The the topmost vertex on the exploration stack which is part of the resolved
	 *        component.
	 * @return The strongly connected component.
	 */
	private Set<V> popComponent(final V root) {
		// create a new sub-graph representing the strongly connected component
		// starting at the given vertex.
		final Set<V> component = new HashSet<>();

		// temporary variable for the while-loop
		V vertex;

		// remove all vertices from the stack up to (and including) the given one
		// and add them to the component.
		do {
			vertex = _stack.pop();
			component.add(vertex);

		} while (vertex != root);

		return component;
	}

	/**
	 * Information stored for each vertex during computation.
	 */
	private static final class VertexInfo {

		private final int _index;

		private int _lowLink;

		public VertexInfo(Integer dfsIndex) {
			_index = dfsIndex;
			_lowLink = dfsIndex;
		}

		public boolean isComponentRoot() {
			return lowLink() == index();
		}

		public void updateLowLink(int reachableLowLink) {
			_lowLink = Math.min(_lowLink, reachableLowLink);
		}

		/**
		 * The DFS index of the vertex.
		 */
		public final int index() {
			return _index;
		}

		/**
		 * The smallest vertex {@link #index()} of all vertices reachable from the subgraph rooted
		 * at the vertex this {@link VertexInfo} is assigned to.
		 */
		public final int lowLink() {
			return _lowLink;
		}

	}

	/**
	 * Stack with O(1) {@link StronglyConnectedComponents.IndexedStack#contains(Object) conains}
	 * implementation.
	 */
	private static final class IndexedStack<T> {

		List<T> _buffer = new ArrayList<>();

		Set<T> _index = new HashSet<>();

		public IndexedStack() {
			super();
		}

		public void push(T value) {
			_buffer.add(value);
			_index.add(value);
		}

		public boolean contains(T value) {
			return _index.contains(value);
		}

		public T pop() {
			T top = _buffer.remove(_buffer.size() - 1);
			_index.remove(top);
			return top;
		}
	}
}
