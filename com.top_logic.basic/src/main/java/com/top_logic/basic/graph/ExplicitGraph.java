/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A {@link Graph} with explicit {@link Node} and {@link Edge} objects.
 * 
 * @param <V>
 *        The vertex user object type.
 * @param <E>
 *        The edge user object type.
 * 
 * @see NodeGraph
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ExplicitGraph<V extends ExplicitGraph<V, E>.Node, E extends ExplicitGraph<V, E>.Edge> {
	/**
	 * Object representing a vertex in a {@link ExplicitGraph}.
	 */
	public class Node {

		private Map<V, Set<E>> _outgoing;

		private Map<V, Set<E>> _incomming;

		/**
		 * The {@link ExplicitGraph} this {@link ExplicitGraph.Node} belongs to.
		 */
		public final ExplicitGraph<V, E> graph() {
			return ExplicitGraph.this;
		}

		/**
		 * Removes an {@link ExplicitGraph.Edge} from this {@link ExplicitGraph.Node} to the given target
		 * {@link ExplicitGraph.Node}.
		 * 
		 * @param target
		 *        The target to remove an {@link ExplicitGraph.Edge} from.
		 * @return The removed {@link ExplicitGraph.Edge}, or <code>null</code> if no such
		 *         {@link ExplicitGraph.Edge} did exist.
		 */
		public Set<E> disconnectTarget(V target) {
			assert sameGraph(target);

			Set<E> edge = unlinkOutgoing(target);
			if (!edge.isEmpty()) {
				target.unlinkIncomming(this);
			}
			return edge;
		}

		/**
		 * Finds an already existing {@link ExplicitGraph.Edge} between this
		 * {@link ExplicitGraph.Node} and the given {@link ExplicitGraph.Node}.
		 * 
		 * @param target
		 *        The target node to find an {@link ExplicitGraph.Edge} to.
		 * @return The existing {@link ExplicitGraph.Edge}, or <code>null</code>, if no edge already
		 *         exists.
		 * 
		 * @see #edgesFrom(ExplicitGraph.Node)
		 */
		public Set<E> edgesTo(V target) {
			return nonNull(lazyOutgoing().get(target));
		}

		private <T> Set<T> nonNull(Set<T> set) {
			return set == null ? Collections.emptySet() : set;
		}

		/**
		 * Finds an already existing {@link ExplicitGraph.Edge} between the given
		 * {@link ExplicitGraph.Node} and this {@link ExplicitGraph.Node}.
		 * 
		 * @param source
		 *        The source node to find an {@link ExplicitGraph.Edge} from.
		 * @return The existing {@link ExplicitGraph.Edge}, or <code>null</code>, if no edge already
		 *         exists.
		 * 
		 * @see #edgesTo(ExplicitGraph.Node)
		 */
		public Set<E> edgesFrom(V source) {
			return nonNull(lazyIncomming().get(source));
		}

		/**
		 * {@link ExplicitGraph.Edge}s with this {@link ExplicitGraph.Node} as
		 * {@link ExplicitGraph.Edge#source()}.
		 */
		public Collection<E> outgoingEdges() {
			return flatten(lazyOutgoing().values());
		}

		/**
		 * {@link ExplicitGraph.Edge}s with this {@link ExplicitGraph.Node} as
		 * {@link ExplicitGraph.Edge#target()}.
		 */
		public Collection<E> incomingEdges() {
			return flatten(lazyIncomming().values());
		}

		private Collection<E> flatten(Collection<Set<E>> values) {
			ArrayList<E> result = new ArrayList<>();
			for (Set<E> edges : values) {
				result.addAll(edges);
			}
			return result;
		}

		/**
		 * {@link ExplicitGraph.Node}s reachable over {@link #outgoing()} {@link ExplicitGraph.Edge}s.
		 */
		public Set<V> outgoing() {
			return lazyOutgoing().keySet();
		}

		/**
		 * {@link ExplicitGraph.Node}s that point to this {@link ExplicitGraph.Node}.
		 */
		public Set<V> incoming() {
			return lazyIncomming().keySet();
		}

		/**
		 * Drops all connections to other nodes.
		 */
		public void unlink() {
			for (Set<E> outgoing : lazyOutgoing().values()) {
				for (E edge : outgoing) {
					edge.target().unlinkIncomming(this);
				}
			}
			for (Set<E> incomming : lazyIncomming().values()) {
				for (E edge : incomming) {
					edge.source().unlinkOutgoing(this);
				}
			}
			_outgoing = null;
			_incomming = null;
		}

		/**
		 * Removes this {@link ExplicitGraph.Node} from the {@link #graph()}.
		 */
		public void delete() {
			graph().remove(this);
		}

		/**
		 * True if this node is a sink, otherwise false.
		 */
		public boolean isSink() {
			return outgoing().isEmpty();
		}

		/**
		 * True if this node is a source, otherwise false.
		 */
		public boolean isSource() {
			return incoming().isEmpty();
		}

		final void destroy() {
		}

		final void addOutgoing(E edge) {
			indexEdge(makeOutgoing(), edge, edge.target());
		}

		final void addIncomming(E edge) {
			indexEdge(makeIncomming(), edge, edge.source());
		}

		final boolean removeOutgoing(Edge edge) {
			Map<V, Set<E>> outgoings = lazyOutgoing();
			Set<E> outgoingEdges = outgoings.get(edge.target());
			if (outgoingEdges == null) {
				return false;
			}
			boolean isRemoved = outgoingEdges.remove(edge);
			if (outgoingEdges.isEmpty()) {
				outgoings.remove(edge.target());
			}
			return isRemoved;
		}

		final boolean removeIncoming(Edge edge) {
			Map<V, Set<E>> incomings = lazyIncomming();
			Set<E> incomingEdges = incomings.get(edge.source());
			if (incomingEdges == null) {
				return false;
			}
			boolean isRemoved = incomingEdges.remove(edge);
			if (incomingEdges.isEmpty()) {
				incomings.remove(edge.source());
			}
			return isRemoved;
		}

		private void indexEdge(Map<V, Set<E>> edgeMap, E edge, V target) {
			Set<E> edges = edgeMap.get(target);
			if (edges == null) {
				edges = new LinkedHashSet<>();
				edgeMap.put(target, edges);
			}
			edges.add(edge);
		}

		final Set<E> unlinkOutgoing(Node node) {
			if (_outgoing == null) {
				return Collections.emptySet();
			}
			Set<E> edges = _outgoing.remove(node);
			if (edges == null) {
				return Collections.emptySet();
			}
			if (_outgoing.isEmpty()) {
				_outgoing = null;
			}
			return edges;
		}

		final Set<E> unlinkIncomming(Node node) {
			if (_incomming == null) {
				return Collections.emptySet();
			}
			Set<E> edges = _incomming.remove(node);
			if (edges == null) {
				return Collections.emptySet();
			}
			if (_incomming.isEmpty()) {
				_incomming = null;
			}
			return edges;
		}

		private Map<V, Set<E>> lazyOutgoing() {
			if (_outgoing == null) {
				return Collections.emptyMap();
			}
			return _outgoing;
		}

		private Map<V, Set<E>> makeOutgoing() {
			if (_outgoing == null) {
				_outgoing = new LinkedHashMap<>();
			}
			return _outgoing;
		}

		private Map<V, Set<E>> lazyIncomming() {
			if (_incomming == null) {
				return Collections.emptyMap();
			}
			return _incomming;
		}

		private Map<V, Set<E>> makeIncomming() {
			if (_incomming == null) {
				_incomming = new LinkedHashMap<>();
			}
			return _incomming;
		}

		boolean sameGraph(V other) {
			return graph().ownNode(other);
		}
	}

	/**
	 * A connection between a {@link ExplicitGraph.Edge#source()} and {@link ExplicitGraph.Edge#target()}
	 * node.
	 */
	public class Edge {

		protected final V _source;

		protected final V _target;

		/**
		 * Creates a {@link ExplicitGraph.Edge}.
		 * 
		 * @param source
		 *        See {@link #source()}.
		 * @param target
		 *        See {@link #target()}.
		 */
		protected Edge(V source, V target) {
			_source = source;
			_target = target;
		}

		/**
		 * The source {@link ExplicitGraph.Node} this {@link ExplicitGraph.Edge} starts with.
		 */
		public V source() {
			return _source;
		}

		/**
		 * The target {@link ExplicitGraph.Node} this {@link ExplicitGraph.Edge} ends in.
		 */
		public V target() {
			return _target;
		}

		/**
		 * True if this edge could be removed, otherwise false.
		 */
		public boolean remove() {
			return _source.removeOutgoing(this) && _target.removeIncoming(this);
		}

	}

	private final Set<V> _nodes = new LinkedHashSet<>();

	/**
	 * All {@link Node}s of this {@link ExplicitGraph}.
	 */
	public Set<V> nodes() {
		return _nodes;
	}

	/**
	 * The given node, if linked to this graph.
	 */
	private V node(final V node) {
		if (node == null) {
			return null;
		}
		if (node.graph() != this) {
			return null;
		}
		return node;
	}

	/**
	 * Checks if the given vertex is part of this graph.
	 * 
	 * @param vertex
	 *        the vertex to check
	 * @return {@code true} if the given vertex was {@link #add(Node) added} to this graph,
	 *         {@code false} otherwise
	 */
	public boolean containsNode(final V vertex) {
		return _nodes.contains(vertex);
	}

	/**
	 * Adds a {@link Node} representing the given user object.
	 * 
	 * @param vertex
	 *        The user object to add a {@link Node} for.
	 * @return The newly created {@link Node} object.
	 */
	public V add(V vertex) {
		_nodes.add(vertex);
		return vertex;
	}

	/**
	 * Removes the given vertex from this graph.
	 * 
	 * <p>
	 * <b>Note: </b>All connections from and to the given vertex will be removed as well.
	 * </p>
	 * 
	 * @param vertex
	 *        the vertex to be removed from the graph
	 */
	public void remove(final Node vertex) {
		Node removed = dropNode(vertex);
		if (removed != null) {
			removed.unlink();
			removed.destroy();
		}
	}

	/**
	 * True if this edge could be removed, otherwise false.
	 */
	public boolean remove(E edge) {
		return edge.remove();
	}

	final Node dropNode(final Node vertex) {
		if (_nodes.remove(vertex)) {
			return vertex;
		}
		return null;
	}

	/**
	 * Resolve all vertices the given vertex points to.
	 * 
	 * @param vertex
	 *        the vertex to resolve the outgoing connections for
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as source.
	 */
	public Set<V> outgoing(V vertex) {
		V node = node(vertex);
		if (node == null) {
			return Collections.emptySet();
		}
	
		return node.outgoing();
	}

	/**
	 * Resolve all vertices the given vertex points to.
	 * 
	 * @param vertex
	 *        the vertex to resolve the outgoing connections for
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as source.
	 */
	public Collection<E> outgoingEdges(final V vertex) {
		V node = node(vertex);
		if (node == null) {
			return Collections.emptyList();
		}
	
		return node.outgoingEdges();
	}

	/**
	 * Resolves all vertices pointing to the given one.
	 * 
	 * @param vertex
	 *        the vertex to resolve the incoming connections for
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as target.
	 */
	public Set<V> incoming(V vertex) {
		V node = node(vertex);
		if (node == null) {
			return Collections.emptySet();
		}
	
		return node.incoming();
	}

	/**
	 * Resolves all {@link Edge}s pointing to the given one.
	 * 
	 * @param vertex
	 *        The vertex to resolve the incoming connections for.
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as target.
	 */
	public Collection<E> incomingEdges(final V vertex) {
		V node = node(vertex);
		if (node == null) {
			return Collections.emptyList();
		}
	
		return node.incomingEdges();
	}

	/**
	 * Returns the connections from the given source to the given target vertex.
	 * 
	 * @param source
	 *        the connection source
	 * @param target
	 *        the connection target
	 * @return the connections between the vertices or {@code null} if no connection exists
	 */
	public Set<E> edges(final V source, final V target) {
		V sourceNode = node(source);
		if (sourceNode == null) {
			return Collections.emptySet();
		}
	
		V targetNode = node(target);
		if (targetNode == null) {
			return Collections.emptySet();
		}
	
		return sourceNode.edgesTo(target);
	}
	
	/**
	 * Creates an {@link Edge} between the given source and target nodes.
	 * 
	 * @param source
	 *        The source {@link Node}.
	 * @param target
	 *        The target {@link Node}.
	 * @return The created {@link Edge} object.
	 */
	public E connect(final V source, final V target) {
		assert source.sameGraph(target);

		E newEdge = createEdge(source, target);
		source.addOutgoing(newEdge);
		target.addIncomming(newEdge);
		return newEdge;
	}


	/**
	 * Disconnects the given source and target vertices by deleting an existing edge between them.
	 * 
	 * <p>
	 * <b>Note: </b>If a no connection from the source to the target vertex exists, nothing changes.
	 * </p>
	 * 
	 * @param source
	 *        the vertex being the connection source
	 * @param target
	 *        the vertex being the connection target
	 */
	public Set<E> disconnect(V source, V target) {
		V sourceNode = node(source);
		if (sourceNode == null) {
			return Collections.emptySet();
		}
		V targetNode = node(target);
		if (targetNode == null) {
			return Collections.emptySet();
		}

		return sourceNode.disconnectTarget(targetNode);
	}

	/**
	 * Creates a new {@link Edge} instance.
	 */
	protected E createEdge(V source, V target) {
		assert ownNode(source);
		assert ownNode(target);

		return newEdge(source, target);
	}

	protected abstract E newEdge(V source, V target);

	public abstract V newNode();

	final boolean ownNode(V source) {
		return source.graph() == this;
	}

}
