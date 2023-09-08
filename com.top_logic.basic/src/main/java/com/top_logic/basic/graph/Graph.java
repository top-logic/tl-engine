/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.graph;

import java.util.Set;

/**
 * Instances of this class represent a directed graph.
 * 
 * @param <V>
 *        defines the vertex object type
 * @param <E>
 *        defines the edge object type
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public interface Graph<V, E> {

	/**
	 * a (possibly empty) {@link Set} of all vertices in this {@link Graph}
	 */
	Set<V> vertices();

	/**
	 * Checks if the given vertex is part of this graph.
	 * 
	 * @param vertex
	 *        the vertex to check
	 * @return {@code true} if the given vertex was {@link #add(Object) added} to this graph,
	 *         {@code false} otherwise
	 */
	boolean contains(final V vertex);

	/**
	 * Add the given vertex to this graph.
	 * 
	 * @param vertex
	 *        the vertex to add to the graph
	 */
	void add(final V vertex);

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
	void remove(final V vertex);

	/**
	 * Resolve all vertices the given vertex points to.
	 * 
	 * @param vertex
	 *        the vertex to resolve the outgoing connections for
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as source.
	 */
	Set<V> outgoing(final V vertex);

	/**
	 * Resolves all vertices pointing to the given one.
	 * 
	 * @param vertex
	 *        the vertex to resolve the incoming connections for
	 * @return a (possibly empty) {@link Set} of edges having the given vertex as target.
	 */
	Set<V> incoming(final V vertex);

	/**
	 * Returns the connection from the given source to the given target vertex.
	 * 
	 * @param source
	 *        the connection source
	 * @param target
	 *        the connection target
	 * @return the connection between the vertices or {@code null} if no connection exists
	 */
	E edge(final V source, final V target);

	/**
	 * Connects the given source to the given target vertex via the given edge.
	 * 
	 * <p>
	 * <b>Note: </b>If a connection from the source to the target vertex already exists, it is
	 * overwritten with the given edge.
	 * </p>
	 * 
	 * @param source
	 *        the vertex being the connection source
	 * @param target
	 *        the vertex being the connection target
	 * @param edge
	 *        the edge connecting the given vertices
	 */
	void connect(final V source, final V target, final E edge);

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
	E disconnect(final V source, final V target);

}