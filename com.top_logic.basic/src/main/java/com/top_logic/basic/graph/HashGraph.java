/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link HashMap}-based implementation of {@link Graph} without explicit node objects.
 * 
 * @param <V>
 *        defines the vertex object type
 * @param <E>
 *        defines the edge object type
 * 
 * @see NodeGraph
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class HashGraph<V, E> implements Graph<V, E> {

	/**
	 * All {@link #vertices()} mapped to their outgoing edges (represented as mapping from target
	 * nodes to edge values).
	 */
	private final Map<V, Map<V, E>> _outgoing = new HashMap<>();

	/**
	 * For all nodes with incoming edges, a mapping from that node to the incoming edges to that
	 * node.
	 */
	private final Map<V, Map<V, E>> _incoming = new HashMap<>();

	@Override
	public Set<V> vertices() {
		return _outgoing.keySet();
	}

	@Override
	public boolean contains(final V vertex) {
		return _outgoing.containsKey(vertex);
	}

	@Override
	public void add(final V vertex) {
		if (!contains(vertex)) {
			_outgoing.put(vertex, null);
		}
	}

	@Override
	public void remove(final V vertex) {
		Map<V, E> outgoingConnections = _outgoing.remove(vertex);
		if (outgoingConnections != null) {
			// Note: All vertices are contained in the outgoing map.
			for (Entry<V, E> entry : outgoingConnections.entrySet()) {
				V target = entry.getKey();
				disconnectIncomming(target, vertex);
			}
	
			Map<V, E> incommingConnections = _incoming.remove(vertex);
			if (incommingConnections != null) {
				for (Entry<V, E> entry : incommingConnections.entrySet()) {
					V source = entry.getKey();
					disconnectOutgoing(source, vertex);
				}
			}
		}
	}

	@Override
	public Set<V> outgoing(final V vertex) {
		final Map<V, E> edges = _outgoing.get(vertex);
		if (edges == null) {
			return Collections.emptySet();
		}
		return edges.keySet();
	}

	@Override
	public Set<V> incoming(final V vertex) {
		final Map<V, E> edges = _incoming.get(vertex);
		if (edges == null) {
			return Collections.emptySet();
		}
		return edges.keySet();
	}

	@Override
	public E edge(final V source, final V target) {
		final Map<V, E> fromSource = _outgoing.get(source);
	
		if (fromSource != null) {
			return fromSource.get(target);
		} else {
			return null;
		}
	}

	@Override
	public void connect(final V source, final V target, final E edge) {
		connect(_outgoing, source, target, edge);
		connect(_incoming, target, source, edge);

		// All vertices are encoded in the key set of the outgoing edges, this is adjusted during
		// connection, but the target may not yet be part of this graph.
		add(target);
	}

	@Override
	public E disconnect(V source, V target) {
		E result = disconnectOutgoing(source, target);
		if (result != null) {
			disconnectIncomming(target, source);
		}
		return result;
	}

	/**
	 * Register the given edge as connection from the given source to the given target vertex in the
	 * given connection index.
	 * 
	 * @param conections
	 *        the connection index to register the edge in
	 * @param self
	 *        the vertex being connected
	 * @param other
	 *        the connection destination vertex
	 * @param edge
	 *        the edge value between the source and target vertex
	 */
	private void connect(final Map<V, Map<V, E>> conections, final V self, final V other, final E edge) {
		Map<V, E> edges = conections.get(self);
		if (edges == null) {
			edges = new HashMap<>(0);
			conections.put(self, edges);
		}

		edges.put(other, edge);
	}

	private E disconnectOutgoing(V source, V target) {
		Map<V, E> edges = _outgoing.get(source);
		if (edges == null) {
			return null;
		}
		
		E edge = edges.remove(target);
		if (edges.isEmpty()) {
			// Last incoming edge is gone, remove the whole edges map, but keep the vertex in this
			// graph.
			_outgoing.put(source, null);
		}
		return edge;
	}

	private E disconnectIncomming(V target, V source) {
		Map<V, E> edges = _incoming.get(target);
		if (edges == null) {
			return null;
		}
		
		E edge = edges.remove(source);
		if (edges.isEmpty()) {
			// Last incoming edge is gone, remove the whole edges map.
			_incoming.remove(target);
		}
		return edge;
	}
}
