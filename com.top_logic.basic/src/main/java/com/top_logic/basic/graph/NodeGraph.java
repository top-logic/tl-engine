/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
 * @see HashGraph
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeGraph<V, E> implements Graph<V, E> {

	/**
	 * Object representing a vertex in a {@link NodeGraph}.
	 */
	public static class Node<V, E> {

		private final NodeGraph<V, E> _graph;

		private final V _userObject;

		private Map<V, Edge<V, E>> _outgoing;

		private Map<V, Edge<V, E>> _incomming;

		/**
		 * Creates a {@link NodeGraph.Node}.
		 * 
		 * @param userObject
		 *        See {@link #userObject()}.
		 */
		protected Node(NodeGraph<V, E> graph, V userObject) {
			_graph = graph;
			_userObject = userObject;
		}

		/**
		 * The {@link NodeGraph} this {@link NodeGraph.Node} belongs to.
		 */
		public final NodeGraph<V, E> graph() {
			return _graph;
		}

		/**
		 * The application value of this {@link NodeGraph}.
		 */
		public final V userObject() {
			return _userObject;
		}

		/**
		 * Creates an {@link NodeGraph.Edge} between this {@link NodeGraph.Node} and the given
		 * target {@link NodeGraph.Node}.
		 * 
		 * @param target
		 *        The {@link NodeGraph.Edge#target()} of the {@link NodeGraph.Edge} to create.
		 * @return The created (or already existing) {@link NodeGraph.Edge}.
		 * 
		 * @see #edgeTo(Object)
		 */
		public Edge<V, E> connectTo(Node<V,E> target) {
			assert sameGraph(target);

			Edge<V, E> existingEdge = edgeTo(target.userObject());
			if (existingEdge != null) {
				return existingEdge;
			}

			Edge<V, E> newEdge = newEdge(target);
			addOutgoing(newEdge);
			target.addIncomming(newEdge);
			return newEdge;
		}

		private Edge<V, E> newEdge(Node<V,E> target) {
			return _graph.newEdge(this, target);
		}

		/**
		 * Removes an {@link NodeGraph.Edge} from this {@link NodeGraph.Node} to the given target
		 * {@link NodeGraph.Node}.
		 * 
		 * @param target
		 *        The target to remove an {@link NodeGraph.Edge} from.
		 * @return The removed {@link NodeGraph.Edge}, or <code>null</code> if no such
		 *         {@link NodeGraph.Edge} did exist.
		 */
		public Edge<V, E> disconnectTarget(Node<V,E> target) {
			assert sameGraph(target);

			Edge<V, E> edge = unlinkOutgoing(target);
			if (edge != null) {
				target.unlinkIncomming(this);
			}
			return edge;
		}

		/**
		 * Finds an already existing {@link NodeGraph.Edge} between this {@link NodeGraph.Node} and
		 * the {@link NodeGraph.Node} with the given user object.
		 * 
		 * @param target
		 *        The user object of the target node to find an Edge to.
		 * @return The existing {@link NodeGraph.Edge}, or <code>null</code>, if no edge already
		 *         exists.
		 * 
		 * @see #connectTo(NodeGraph.Node)
		 * @see #edgeFrom(Object)
		 */
		public Edge<V, E> edgeTo(V target) {
			return lazyOutgoing().get(target);
		}

		/**
		 * Finds an already existing {@link NodeGraph.Edge} between the {@link NodeGraph.Node} with
		 * the given user object and this {@link NodeGraph.Node}.
		 * 
		 * @param source
		 *        The user object of the source node to find an Edge from.
		 * @return The existing {@link NodeGraph.Edge}, or <code>null</code>, if no edge already
		 *         exists.
		 * 
		 * @see #edgeTo(Object)
		 */
		public Edge<V, E> edgeFrom(V source) {
			return lazyIncomming().get(source);
		}

		/**
		 * {@link NodeGraph.Edge}s with this {@link NodeGraph.Node} as
		 * {@link NodeGraph.Edge#source()}.
		 */
		public Collection<Edge<V, E>> outgoingEdges() {
			return lazyOutgoing().values();
		}

		/**
		 * {@link NodeGraph.Edge}s with this {@link NodeGraph.Node} as
		 * {@link NodeGraph.Edge#target()}.
		 */
		public Collection<Edge<V, E>> incomingEdges() {
			return lazyIncomming().values();
		}

		/**
		 * {@link NodeGraph.Node}s reachable over {@link #outgoing()} {@link NodeGraph.Edge}s.
		 */
		public Set<V> outgoing() {
			return lazyOutgoing().keySet();
		}

		/**
		 * {@link NodeGraph.Node}s that point to this {@link NodeGraph.Node}.
		 */
		public Set<V> incoming() {
			return lazyIncomming().keySet();
		}

		/**
		 * Drops all connections to other nodes.
		 */
		public void unlink() {
			for (Edge<V, E> outgoing : lazyOutgoing().values()) {
				outgoing.target().unlinkIncomming(this);
			}
			for (Edge<V, E> incomming : lazyIncomming().values()) {
				incomming.source().unlinkOutgoing(this);
			}
		}

		final void addOutgoing(Edge<V, E> edge) {
			makeOutgoing().put(edge.target().userObject(), edge);
		}

		final void addIncomming(Edge<V, E> edge) {
			makeIncomming().put(edge.source().userObject(), edge);
		}

		final Edge<V, E> unlinkOutgoing(Node<V, E> node) {
			if (_outgoing == null) {
				return null;
			}
			Edge<V, E> result = _outgoing.remove(node.userObject());
			if (_outgoing.isEmpty()) {
				_outgoing = null;
			}
			return result;
		}

		final Edge<V, E> unlinkIncomming(Node<V, E> node) {
			if (_incomming == null) {
				return null;
			}
			Edge<V, E> result = _incomming.remove(node.userObject());
			if (_incomming.isEmpty()) {
				_incomming = null;
			}
			return result;
		}

		private Map<V, Edge<V, E>> lazyOutgoing() {
			if (_outgoing == null) {
				return Collections.emptyMap();
			}
			return _outgoing;
		}

		private Map<V, Edge<V, E>> makeOutgoing() {
			if (_outgoing == null) {
				_outgoing = new HashMap<>();
			}
			return _outgoing;
		}

		private Map<V, Edge<V, E>> lazyIncomming() {
			if (_incomming == null) {
				return Collections.emptyMap();
			}
			return _incomming;
		}

		private Map<V, Edge<V, E>> makeIncomming() {
			if (_incomming == null) {
				_incomming = new HashMap<>();
			}
			return _incomming;
		}

		private boolean sameGraph(Node<V,E> other) {
			return _graph.ownNode(other);
		}
	}

	/**
	 * A connection between a {@link NodeGraph.Edge#source()} and {@link NodeGraph.Edge#target()}
	 * node.
	 */
	public static class Edge<V, E> {

		private final Node<V,E> _source;

		private final Node<V,E> _target;

		private E _userObject;

		/**
		 * Creates a {@link NodeGraph.Edge}.
		 * 
		 * @param source
		 *        See {@link #source()}.
		 * @param target
		 *        See {@link #target()}.
		 */
		protected Edge(Node<V,E> source, Node<V,E> target) {
			_source = source;
			_target = target;
		}

		/**
		 * The source {@link NodeGraph.Node} this {@link NodeGraph.Edge} starts with.
		 */
		public Node<V,E> source() {
			return _source;
		}

		/**
		 * The target {@link NodeGraph.Node} this {@link NodeGraph.Edge} ends in.
		 */
		public Node<V,E> target() {
			return _target;
		}

		/**
		 * The user object associated with this {@link NodeGraph.Edge}.
		 */
		public E userObject() {
			return _userObject;
		}

		/**
		 * @see #userObject()
		 */
		public void setUserObject(E userObject) {
			_userObject = userObject;
		}
	}

	private final Map<V, Node<V,E>> _nodes = new HashMap<>();

	@Override
	public Set<V> vertices() {
		return _nodes.keySet();
	}

	/**
	 * All {@link Node}s of this {@link NodeGraph}.
	 */
	public Collection<Node<V,E>> nodes() {
		return _nodes.values();
	}

	/**
	 * The {@link Node} representing the given user object.
	 * 
	 * @param vertex
	 *        The {@link Node#userObject()} of the {@link Node} to find.
	 * @return The {@link Node} with the given user object, or <code>null</code>, if no such
	 *         <xmp>Node<V,E></xmp> exists.
	 */
	public Node<V,E> node(final V vertex) {
		return _nodes.get(vertex);
	}

	@Override
	public boolean contains(final V vertex) {
		return _nodes.containsKey(vertex);
	}

	@Override
	public void add(V vertex) {
		makeNode(vertex);
	}

	/**
	 * Adds a {@link Node} representing the given user object, if no such node exists yet.
	 * 
	 * @param vertex
	 *        The user object to add a {@link Node} for.
	 * @return The existing or newly created {@link Node} object.
	 */
	public Node<V,E> makeNode(final V vertex) {
		Node<V,E> existingNode = node(vertex);
		if (existingNode != null) {
			return existingNode;
		}
	
		Node<V,E> newNode = newNode(vertex);
		_nodes.put(vertex, newNode);
		return newNode;
	}

	@Override
	public void remove(final V vertex) {
		Node<V,E> removed = _nodes.remove(vertex);
		if (removed != null) {
			removed.unlink();
		}
	}

	@Override
	public Set<V> outgoing(V vertex) {
		Node<V,E> node = node(vertex);
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
	public Collection<Edge<V, E>> outgoingEdges(final V vertex) {
		Node<V,E> node = node(vertex);
		if (node == null) {
			return Collections.emptyList();
		}
	
		return node.outgoingEdges();
	}

	@Override
	public Set<V> incoming(V vertex) {
		Node<V,E> node = node(vertex);
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
	public Collection<Edge<V, E>> incomingEdges(final V vertex) {
		Node<V,E> node = node(vertex);
		if (node == null) {
			return Collections.emptyList();
		}
	
		return node.incomingEdges();
	}

	@Override
	public E edge(final V source, final V target) {
		Node<V,E> sourceNode = _nodes.get(source);
		if (sourceNode == null) {
			return null;
		}
	
		Node<V,E> targetNode = _nodes.get(target);
		if (targetNode == null) {
			return null;
		}
	
		Edge<V, E> edge = sourceNode.edgeTo(target);
		if (edge == null) {
			return null;
		}
	
		return edge.userObject();
	}

	@Override
	public void connect(final V source, final V target, final E userObject) {
		Edge<V, E> edge = connect(source, target);
		edge.setUserObject(userObject);
	}

	/**
	 * Creates an {@link Edge} between the given source and target nodes.
	 * 
	 * @param source
	 *        The user object of the source {@link Node}.
	 * @param target
	 *        The user object of the target {@link Node}.
	 * @return The created {@link Edge} object.
	 * 
	 * @see Node#connectTo(NodeGraph.Node)
	 */
	public Edge<V, E> connect(final V source, final V target) {
		Node<V,E> sourceNode = makeNode(source);
		Node<V,E> targetNode = makeNode(target);
		return sourceNode.connectTo(targetNode);
	}

	@Override
	public E disconnect(V source, V target) {
		Node<V,E> sourceNode = node(source);
		if (sourceNode == null) {
			return null;
		}
		Node<V,E> targetNode = node(target);
		if (targetNode == null) {
			return null;
		}

		Edge<V, E> edge = sourceNode.disconnectTarget(targetNode);
		if (edge == null) {
			return null;
		}

		return edge.userObject();
	}

	/**
	 * Creates a new {@link Node} instance.
	 */
	protected Node<V, E> newNode(V vertex) {
		return new Node<>(this, vertex);
	}

	/**
	 * Creates a new {@link Edge} instance.
	 */
	protected Edge<V, E> newEdge(Node<V,E> source, Node<V,E> target) {
		assert ownNode(source);
		assert ownNode(target);

		return new Edge<>(source, target);
	}

	final boolean ownNode(Node<V,E> source) {
		return source.graph() == this;
	}

}
